package com.visionfocus.ui.recognition

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.recognition.processing.ConfidenceFilter
import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.formatter.TTSPhraseFormatter
import com.visionfocus.voice.operation.Operation
import com.visionfocus.voice.operation.OperationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for recognition UI (Story 2.3, 2.4, 2.6, 3.3)
 * 
 * Orchestrates:
 * - Story 2.1: RecognitionRepository.performRecognition() (TFLite inference)
 * - Story 2.2: TTSPhraseFormatter + TTSManager (formatting + announcement)
 * - Story 2.3: UI state management with StateFlow
 * - Story 2.4: Camera lifecycle integration with capture state
 * - Story 2.6: Haptic feedback patterns for recognition events
 * - Story 3.3: Operation cancellation support via OperationManager
 * 
 * State transitions:
 * Idle → Capturing → Recognizing → Announcing → Success → Idle (2s delay)
 * Idle → Capturing → Recognizing → Error → Idle (3s delay)
 * Idle → Capturing → CameraError → Idle (3s delay)
 * Any state → Cancelled → Idle (Story 3.3)
 * 
 * @param recognitionRepository Story 2.1 - Object recognition with TFLite
 * @param confidenceFilter Story 2.2 - Convert DetectionResult to FilteredDetection
 * @param ttsManager Story 2.2 - Text-to-speech engine
 * @param ttsFormatter Story 2.2 - Confidence-aware phrase formatting
 * @param hapticFeedbackManager Story 2.6 - Haptic feedback for deaf-blind users
 * @param operationManager Story 3.3 - Operation cancellation tracking
 */
@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
    private val confidenceFilter: ConfidenceFilter,
    private val ttsManager: TTSManager,
    private val ttsFormatter: TTSPhraseFormatter,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val operationManager: OperationManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "RecognitionViewModel"
        
        /**
         * Delay before returning to Idle state after Success
         * Allows user to see/hear success state briefly
         * 
         * TODO (MEDIUM-5): Make configurable in settings for users who need longer processing time
         * (e.g., deaf-blind users with slower comprehension)
         */
        private const val SUCCESS_DELAY_MS = 2000L
        
        /**
         * Delay before returning to Idle state after Error
         * Allows user to hear error announcement
         * 
         * TODO (MEDIUM-5): Make configurable in settings
         */
        private const val ERROR_DELAY_MS = 3000L
    }
    
    // Story 2.3 Task 1.5: Expose uiState as StateFlow
    // Story 2.4 Task 9.5: Extended with Capturing and CameraError states
    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()
    
    // Story 2.4 Task 9.4: Track recognition job for cancellation
    private var recognitionJob: Job? = null
    
    /**
     * Trigger object recognition pipeline
     * 
     * Story 2.3 Task 1.6: Implement recognizeObject() function
     * Story 2.4: EXTENDED to transition to Capturing state
     * Story 2.6: EXTENDED to trigger RecognitionStart haptic
     * 
     * Flow:
     * 1. Transition to Capturing state (camera frame capture initiated)
     * 2. Trigger RecognitionStart haptic (single 100ms vibration)
     * 3. Fragment captures frame and calls performRecognition(bitmap)
     * 4. Continue with TFLite inference and TTS announcement
     */
    fun recognizeObject() {
        // Story 2.4: Debounce - ignore if already in progress
        if (_uiState.value !is RecognitionUiState.Idle) {
            return
        }
        
        viewModelScope.launch {
            // Story 2.4 Task 9.2: State transition - Idle → Capturing
            _uiState.value = RecognitionUiState.Capturing
            
            // Story 2.6: Trigger recognition start haptic (non-blocking)
            try {
                hapticFeedbackManager.trigger(HapticPattern.RecognitionStart)
            } catch (e: Exception) {
                // Non-blocking error - log but don't fail recognition flow
                android.util.Log.w(TAG, "Haptic feedback failed during recognition start", e)
            }
        }
    }
    
    /**
     * Perform recognition with captured camera frame
     * 
     * Story 2.4 Task 2: Called by fragment after camera capture completes
     * 
     * Pipeline:
     * 1. Transition to Recognizing state
     * 2. Call RecognitionRepository.performRecognition(bitmap) (Story 2.1)
     * 3. Handle empty results (no objects detected)
     * 4. Format announcement with TTSPhraseFormatter (Story 2.2)
     * 5. Transition to Announcing state
     * 6. Call TTSManager.announce() (Story 2.2)
     * 7. Transition to Success state with results
     * 8. Auto-return to Idle after delay
     * 
     * @param bitmap Captured camera frame to analyze
     */
    fun performRecognition(bitmap: Bitmap) {
        recognitionJob = viewModelScope.launch {
            try {
                // Story 3.3 Task 4.2: Register operation BEFORE state transition (race condition fix)
                operationManager.startOperation(
                    Operation.RecognitionOperation(
                        onCancel = { cancelRecognitionInternal() }
                    )
                )
                
                // Story 2.4 Task 9.2: State transition - Capturing → Recognizing
                _uiState.value = RecognitionUiState.Recognizing
                
                // CRITICAL FIX: Ensure service is initialized before recognition
                // Fallback initialization if Application.onCreate() didn't work
                try {
                    recognitionRepository.ensureInitialized()
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to ensure initialization", e)
                    operationManager.completeOperation()
                    handleError("Initialization failed: ${e.message}")
                    return@launch
                }
                
                // Story 2.1: TFLite inference (≤320ms)
                val result = recognitionRepository.performRecognition(bitmap)
                
                // Story 2.4: Handle empty results (no objects detected)
                if (result.detections.isEmpty()) {
                    operationManager.completeOperation()
                    
                    _uiState.value = RecognitionUiState.Error(
                        "No objects detected. Try pointing camera at a different area."
                    )
                    
                    // Story 2.6: Trigger error haptic (long 300ms vibration)
                    try {
                        hapticFeedbackManager.trigger(HapticPattern.RecognitionError)
                    } catch (e: Exception) {
                        android.util.Log.w(TAG, "Haptic feedback failed during error state", e)
                    }
                    
                    delay(ERROR_DELAY_MS)
                    _uiState.value = RecognitionUiState.Idle
                    return@launch
                }
                
                // Convert DetectionResult to FilteredDetection (Story 2.2)
                val filteredDetections = result.detections.map { detection ->
                    confidenceFilter.toFilteredDetection(detection)
                }
                
                // Story 2.2: Format announcement with confidence-aware phrasing
                val announcement = ttsFormatter.formatMultipleDetections(filteredDetections)
                
                // Story 2.4 Task 9.2: State transition - Recognizing → Announcing
                _uiState.value = RecognitionUiState.Announcing
                
                // Story 2.2: TTS announcement (≤200ms initiation)
                ttsManager.announce(announcement)
                
                // Story 2.4 Task 9.2: State transition - Announcing → Success
                _uiState.value = RecognitionUiState.Success(
                    results = filteredDetections,
                    announcement = announcement,
                    latency = result.latencyMs
                )
                
                // Story 2.6: Trigger success haptic (double vibration pattern)
                try {
                    hapticFeedbackManager.trigger(HapticPattern.RecognitionSuccess)
                } catch (e: Exception) {
                    android.util.Log.w(TAG, "Haptic feedback failed during success state", e)
                }
                
                // Story 3.3: Complete operation after success
                operationManager.completeOperation()
                
                // Auto-return to Idle after brief delay (allows user to see success)
                delay(SUCCESS_DELAY_MS)
                _uiState.value = RecognitionUiState.Idle
                
            } catch (e: SecurityException) {
                // Camera permission denied
                operationManager.completeOperation()
                handleError("Camera permission required. Please enable camera access in settings.")
                
            } catch (e: IllegalStateException) {
                // TTS not initialized or camera not ready
                operationManager.completeOperation()
                handleError(e.message ?: "Recognition service not ready. Please try again.")
                
            } catch (e: Exception) {
                // Generic recognition failure
                operationManager.completeOperation()
                handleError(e.message ?: "Recognition failed. Please try again.")
            }
        }
    }
    
    /**
     * Camera initialized successfully
     * 
     * Story 2.4: Called by fragment after CameraX binding completes
     * No action needed, just confirms camera is ready
     */
    fun onCameraReady() {
        // Camera initialization successful
        // Fragment will trigger recognizeObject() on FAB tap
    }
    
    /**
     * Story 2.6: EXTENDED to trigger error haptic
     * 
     * @param message User-friendly error message
     */
    fun onCameraError(message: String) {
        viewModelScope.launch {
            // Story 2.4 Task 9.6: Transition to CameraError state
            _uiState.value = RecognitionUiState.CameraError(message)
            
            // Story 2.6: Trigger error haptic (long 300ms vibration)
            try {
                hapticFeedbackManager.trigger(HapticPattern.RecognitionError)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Haptic feedback failed during camera error", e)
            }
            
            // Auto-return to Idle after error announcement delay
            delay(ERROR_DELAY_MS)
            _uiState.value = RecognitionUiState.Idle
        }
    }
    
    /**
     * Cancel recognition mid-flow
     * 
     * Story 2.4 Task 5: Back button cancellation
     * Story 3.3 Task 4: Enhanced to support voice command cancellation via OperationManager
     * 
     * Public entry point for cancellation - used by voice commands.
     * Delegates to internal cancellation logic.
     */
    fun cancelRecognition() {
        viewModelScope.launch {
            cancelRecognitionInternal()
        }
    }
    
    /**
     * Internal cancellation implementation
     * Story 3.3 Task 4: Cancellation logic invoked by OperationManager
     * 
     * AC: Cancel works mid-recognition (camera, inference, or TTS phase)
     * 
     * Cancellation phases:
     * - Capturing: Cancel before inference starts
     * - Recognizing: Cancel TFLite inference (Job cancellation)
     * - Announcing: Stop TTS mid-announcement
     * 
     * Note: "Cancelled" announcement handled by OperationManager
     */
    private suspend fun cancelRecognitionInternal() {
        android.util.Log.d(TAG, "Cancelling recognition, current state: ${_uiState.value::class.simpleName}")
        
        // Cancel recognition job if running
        recognitionJob?.cancel()
        
        // Stop TTS if announcing
        if (_uiState.value is RecognitionUiState.Announcing) {
            ttsManager.stop()
        }
        
        // Transition directly to Idle (cancellation announced by OperationManager)
        // Note: Removed intermediate Cancelled state to avoid unnecessary StateFlow emission
        _uiState.value = RecognitionUiState.Idle
        
        android.util.Log.d(TAG, "Recognition cancelled successfully")
    }
    
    /**
     * Story 2.6: EXTENDED to trigger error haptic
     * 
     * @param message User-friendly error message
     */
    private suspend fun handleError(message: String) {
        // Transition to Error state
        _uiState.value = RecognitionUiState.Error(message = message)
        
        // Story 2.6: Trigger error haptic (long 300ms vibration)
        try {
            hapticFeedbackManager.trigger(HapticPattern.RecognitionError)
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Haptic feedback failed during error handling", e)
        }
        
        // Auto-return to Idle after error announcement delay
        delay(ERROR_DELAY_MS)
        _uiState.value = RecognitionUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        recognitionJob?.cancel()
    }
}
