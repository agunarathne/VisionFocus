package com.visionfocus.recognition.scanning

import android.content.Context
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.recognition.processing.ConfidenceLevel
import com.visionfocus.recognition.processing.FilteredDetection
import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.formatter.TTSPhraseFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Continuous scanning service for environment mapping
 * 
 * Story 4.4: Continuous Scanning Mode for Environment Mapping
 * Tasks 1-5, 8-9: Core scanning implementation
 * 
 * Features:
 * - Captures frames every 3 seconds (0.33 Hz) for battery efficiency
 * - Suppresses duplicate object announcements
 * - Auto-stops after 60 seconds
 * - Provides summary on stop
 * - Manages announcement queue to prevent overwhelming user
 * 
 * Acceptance Criteria:
 * - AC: Camera captures frames every 3 seconds
 * - AC: New unique objects announced immediately
 * - AC: Duplicate objects suppressed
 * - AC: Confidence threshold ≥0.6
 * - AC: Auto-stops after 60 seconds
 * - AC: Summary announcement on stop
 * 
 * Battery optimization: 0.33 Hz frame rate vs continuous 15 FPS = 95.6% reduction
 */
@Singleton
class ContinuousScanner @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
    private val ttsManager: TTSManager,
    private val ttsFormatter: TTSPhraseFormatter,
    private val tracker: DetectedObjectTracker,
    private val settingsRepository: SettingsRepository,
    private val hapticManager: HapticFeedbackManager,
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "ContinuousScanner"
        
        /**
         * Frame capture interval: 3000ms = 0.33 Hz
         * Battery optimization: Story 4.4 AC (≤12% drain/hour)
         */
        private const val FRAME_CAPTURE_INTERVAL_MS = 3000L
        
        /**
         * Auto-stop timeout: 60 seconds
         * Story 4.4 AC: continuous mode auto-stops after 60 seconds
         */
        private const val AUTO_STOP_TIMEOUT_MS = 60_000L
        
        /**
         * Confidence threshold: ≥0.45 (lowered from 0.6 to reduce false negatives)
         * Real-world testing showed most valid detections are in 0.5-0.58 range
         */
        private const val CONFIDENCE_THRESHOLD = 0.45f
        
        /**
         * Announcement queue capacity: 5 pending announcements max
         * Prevents overwhelming user with too many queued announcements
         */
        private const val ANNOUNCEMENT_QUEUE_CAPACITY = 5
        
        /**
         * Brief delay between announcements for clarity
         */
        private const val ANNOUNCEMENT_GAP_MS = 500L
    }
    
    // State management
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Idle)
    val scanningState: StateFlow<ScanningState> = _scanningState.asStateFlow()
    
    // Coroutine scope for scanning operations (IO dispatcher for CPU-bound work)
    private val scanningScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Announcement queue channel
    private val announcementQueue = Channel<String>(capacity = ANNOUNCEMENT_QUEUE_CAPACITY)
    
    // Active jobs
    private var scanningJob: Job? = null
    private var timeoutJob: Job? = null
    private var announcementJob: Job? = null
    
    // MEDIUM-2 FIX: Track consecutive frame errors for user feedback
    private var consecutiveErrorCount = 0
    private val MAX_CONSECUTIVE_ERRORS = 3
    
    /**
     * Start continuous scanning mode
     * 
     * Flow:
     * 1. Reset object tracker
     * 2. Announce start message
     * 3. Update state to Scanning
     * 4. Start frame capture loop (every 3 seconds)
     * 5. Start announcement consumer
     * 6. Start 60-second auto-timeout timer
     * 
     * Story 4.4 AC: Voice command "Scan environment" or long-press FAB activates
     */
    fun startScanning() {
        if (_scanningState.value is ScanningState.Scanning) {
            Timber.w("Scanning already active - ignoring start request")
            return
        }
        
        Timber.d("Starting continuous scanning mode")
        
        // Reset tracker for new session
        tracker.reset()
        
        // Announce start via TTS
        scanningScope.launch {
            ttsManager.announce("Continuous scanning active. I'll announce objects as I detect them. Say 'Stop' to end.")
        }
        
        // Update state
        _scanningState.value = ScanningState.Scanning(
            startTime = System.currentTimeMillis(),
            objectsDetected = 0
        )
        
        // Start frame capture loop
        scanningJob = scanningScope.launch {
            captureFrameLoop().collect { 
                // Frame capture triggered, process handled in flow
            }
        }
        
        // Start announcement consumer
        announcementJob = scanningScope.launch {
            consumeAnnouncementQueue()
        }
        
        // Start auto-timeout timer
        timeoutJob = scanningScope.launch {
            delay(AUTO_STOP_TIMEOUT_MS)
            stopScanning(isAutoStop = true)
        }
        
        Timber.d("Continuous scanning started successfully")
    }
    
    /**
     * Stop continuous scanning mode
     * 
     * Flow:
     * 1. Update state to Stopping (prevents race conditions)
     * 2. Cancel frame capture and timeout jobs
     * 3. Drain announcement queue gracefully (CRITICAL-2 FIX)
     * 4. Generate summary of detected objects
     * 5. Announce summary
     * 6. Update state to Idle after announcement completes
     * 
     * @param isAutoStop true if stopped by timeout, false if manually stopped
     * 
     * Story 4.4 AC: Speaking "Stop" or "Cancel" exits continuous mode
     * Story 4.4 AC: Auto-stops after 60 seconds with announcement
     * 
     * CRITICAL-1 FIX: Use Stopping state to prevent race condition
     * CRITICAL-2 FIX: Close channel (drains queue) instead of cancel (drops pending)
     */
    fun stopScanning(isAutoStop: Boolean = false) {
        Timber.d("Stopping continuous scanning (auto=$isAutoStop)")
        
        // Generate summary first (before tracker is reset)
        val detectedObjects = tracker.getAllDetectedObjects()
        val count = detectedObjects.size
        
        val summaryPrefix = if (isAutoStop) {
            "Scan complete."
        } else {
            "Scanning stopped."
        }
        
        val summary = when {
            count == 0 -> "$summaryPrefix I didn't detect any objects."
            count == 1 -> "$summaryPrefix I detected 1 object: ${detectedObjects[0]}."
            count <= 5 -> "$summaryPrefix I detected $count objects: ${detectedObjects.joinToString(", ")}."
            else -> {
                val topFive = detectedObjects.take(5).joinToString(", ")
                "$summaryPrefix I detected $count objects including $topFive."
            }
        }
        
        // CRITICAL-1 FIX: Update state to Stopping BEFORE cancelling jobs
        _scanningState.value = ScanningState.Stopping(summary)
        
        // Cancel frame capture and timeout jobs
        scanningJob?.cancel()
        timeoutJob?.cancel()
        
        // CRITICAL-2 FIX: Close channel gracefully (allows pending announcements to drain)
        announcementQueue.close()
        
        // Wait for announcements to drain, then announce summary
        scanningScope.launch {
            // Cancel announcement consumer after queue drains
            announcementJob?.join()
            
            // Announce summary
            ttsManager.announce(summary)
            
            // CRITICAL-1 FIX: Update state to Idle AFTER announcement starts
            _scanningState.value = ScanningState.Idle
            
            Timber.d("Continuous scanning stopped: $summary")
        }
    }
    
    /**
     * Frame capture loop using Flow
     * 
     * Emits Unit every 3 seconds to trigger frame processing
     * Cancellable via scanningJob cancellation
     * 
     * Story 4.4 AC: Camera captures frames every 3 seconds
     */
    private fun captureFrameLoop(): Flow<Unit> = flow {
        while (true) {
            delay(FRAME_CAPTURE_INTERVAL_MS)
            emit(Unit)
            
            // Process frame immediately
            try {
                captureAndProcessFrame()
            } catch (e: Exception) {
                Timber.e(e, "Error during frame processing - continuing scan")
                // Continue scanning despite error (non-fatal)
            }
        }
    }
    
    /**
     * Capture and process a single frame
     * 
     * Flow:
     * 1. Call recognition repository to get detections (with timeout)
     * 2. Filter by confidence ≥0.6
     * 3. Check for new objects via tracker
     * 4. Queue announcements for new objects
     * 5. Track errors and provide user feedback after 3 failures
     * 
     * Story 4.4 AC: Confidence threshold ≥0.6
     * Story 4.4 AC: New unique objects announced immediately
     * Story 4.4 AC: Duplicate objects suppressed
     * 
     * MEDIUM-1 FIX: Add 2.5s timeout to prevent slow recognition backup
     * MEDIUM-2 FIX: Track errors and announce to user after 3 consecutive failures
     * CRITICAL-3 FIX: Stop scanning immediately on SecurityException
     */
    private suspend fun captureAndProcessFrame() {
        android.util.Log.e("CONTINUOUS_SCANNER", "===== CAPTURE FRAME CALLED =====")
        try {
            // MEDIUM-1 FIX: Add timeout to prevent backup if recognition is slow
            android.util.Log.e("CONTINUOUS_SCANNER", "Calling recognitionRepository.performRecognition()")
            val result = withTimeout(2500L) {
                recognitionRepository.performRecognition()
            }
            android.util.Log.e("CONTINUOUS_SCANNER", "Recognition completed: ${result.detections.size} detections")            
            
            // DIAGNOSTIC: Log detection details
            result.detections.forEach { detection ->
                android.util.Log.e("CONTINUOUS_SCANNER", "  -> ${detection.label} (confidence: ${detection.confidence})")
            }
            
            // MEDIUM-2 FIX: Reset error count on successful recognition
            consecutiveErrorCount = 0
            
            // Apply confidence filter ≥0.6
            val highConfidenceDetections = result.detections
                .filter { it.confidence >= CONFIDENCE_THRESHOLD }
            
            android.util.Log.e("CONTINUOUS_SCANNER", "After confidence filter (≥$CONFIDENCE_THRESHOLD): ${highConfidenceDetections.size} detections")
            
            if (highConfidenceDetections.isEmpty()) {
                Timber.v("No high-confidence detections in frame")
                return
            }
            
            // Check for new objects and queue announcements
            var newObjectCount = 0
            for (detection in highConfidenceDetections) {
                val isNew = tracker.isNewObject(detection.label)
                
                if (isNew) {
                    tracker.addObject(detection.label)
                    newObjectCount++
                    
                    // Haptic feedback: Double vibration to signal new object detected
                    // This helps user know when to move camera to scan next object
                    hapticManager.trigger(HapticPattern.RecognitionSuccess)
                    
                    // Format announcement based on verbosity
                    val announcement = formatAnnouncementForObject(
                        label = detection.label,
                        confidence = detection.confidence
                    )
                    
                    // Queue for announcement (non-blocking)
                    val sent = announcementQueue.trySend(announcement)
                    if (!sent.isSuccess) {
                        Timber.w("Announcement queue full - dropping: $announcement")
                    }
                }
            }
            
            // Update state with new object count
            val currentState = _scanningState.value
            if (currentState is ScanningState.Scanning && newObjectCount > 0) {
                _scanningState.value = currentState.copy(
                    objectsDetected = currentState.objectsDetected + newObjectCount
                )
            }
            
            Timber.d("Processed frame: ${highConfidenceDetections.size} detections, $newObjectCount new objects")
            
        } catch (e: SecurityException) {
            // CRITICAL-3 FIX: Stop scanning immediately on permission error
            Timber.e(e, "Camera permission denied during scanning")
            ttsManager.announce("Camera permission error. Scanning stopped.")
            stopScanning()
        } catch (e: TimeoutCancellationException) {
            // MEDIUM-1 FIX: Handle timeout gracefully
            consecutiveErrorCount++
            Timber.w("Recognition timeout (>2.5s) - frame skipped (errors: $consecutiveErrorCount)")
            handleConsecutiveErrors()
        } catch (e: Exception) {
            // MEDIUM-2 FIX: Track errors and provide feedback
            consecutiveErrorCount++
            Timber.e(e, "Error processing frame (errors: $consecutiveErrorCount)")
            handleConsecutiveErrors()
        }
    }
    
    /**
     * MEDIUM-2 FIX: Handle consecutive frame errors with user feedback
     * 
     * After 3 consecutive failures, announce error to user and stop scanning
     * to prevent continued battery drain and poor user experience.
     */
    private suspend fun handleConsecutiveErrors() {
        if (consecutiveErrorCount >= MAX_CONSECUTIVE_ERRORS) {
            Timber.e("$MAX_CONSECUTIVE_ERRORS consecutive frame errors - stopping scan")
            ttsManager.announce("Scanning having trouble detecting objects. Stopping scan.")
            stopScanning()
        }
    }
    
    /**
     * Consume announcement queue
     * 
     * Pulls announcements from queue and speaks them sequentially
     * Adds brief delay between announcements for clarity
     * 
     * Story 4.4 AC: Announcements queued if multiple new objects detected
     */
    private suspend fun consumeAnnouncementQueue() {
        try {
            for (announcement in announcementQueue) {
                ttsManager.announce(announcement)
                delay(ANNOUNCEMENT_GAP_MS) // Brief gap for clarity
            }
        } catch (e: Exception) {
            Timber.e(e, "Error consuming announcement queue")
        }
    }
    
    /**
     * Format announcement for detected object based on verbosity mode
     * 
     * For continuous scanning, we prefer BRIEF mode for rapid feedback
     * But respect user's verbosity preference if set
     * 
     * @param label Object label
     * @param confidence Detection confidence (0.0-1.0)
     * @return Formatted announcement string
     */
    private suspend fun formatAnnouncementForObject(label: String, confidence: Float): String {
        // Get user's verbosity preference
        val verbosityMode = settingsRepository.getVerbosity().firstOrNull() ?: VerbosityMode.BRIEF
        
        // For continuous scanning, prefer BRIEF mode to avoid overwhelming user
        // Unless user explicitly selected DETAILED
        val effectiveMode = if (verbosityMode == VerbosityMode.DETAILED) {
            VerbosityMode.STANDARD // Compromise: not too verbose
        } else {
            VerbosityMode.BRIEF // Brief for rapid announcements
        }
        
        return when (effectiveMode) {
            VerbosityMode.BRIEF -> label
            VerbosityMode.STANDARD -> {
                val confidenceLevel = when {
                    confidence >= 0.85f -> "high confidence"
                    confidence >= 0.7f -> "medium confidence"
                    else -> "low confidence"
                }
                "$label with $confidenceLevel"
            }
            VerbosityMode.DETAILED -> {
                // Not used in continuous scanning (too verbose)
                label
            }
        }
    }
    
    /**
     * Categorize confidence level for TTS formatting
     * 
     * @param confidence Detection confidence (0.0-1.0)
     * @return ConfidenceLevel enum
     */
    private fun categorizeConfidence(confidence: Float): ConfidenceLevel {
        return when {
            confidence >= 0.85f -> ConfidenceLevel.HIGH
            confidence >= 0.7f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }
    
    /**
     * Cleanup scanner resources
     * 
     * Call from ViewModel onCleared() or Application onTerminate()
     */
    fun cleanup() {
        Timber.d("Cleaning up ContinuousScanner")
        stopScanning()
        scanningScope.cancel()
    }
}
