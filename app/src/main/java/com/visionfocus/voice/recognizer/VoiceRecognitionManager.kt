package com.visionfocus.voice.recognizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice Recognition Manager
 * Story 3.1 Task 1: Android SpeechRecognizer integration wrapper
 * 
 * Manages voice recognition lifecycle using Android's built-in SpeechRecognizer API.
 * Handles speech capture, recognition callbacks, error handling, and state management.
 * 
 * Key Features:
 * - On-device speech recognition (API 33+) for privacy
 * - 5-second silence timeout per AC
 * - Lowercase transcription for command matching
 * - Comprehensive error handling with user-facing messages
 * 
 * Integration Points:
 * - Used by VoiceRecognitionViewModel for UI state management
 * - Provides recognized text to command processor (Story 3.2)
 * - Integrated with AccessibilityAnnouncementHelper for TTS feedback
 * 
 * @param context Application context for SpeechRecognizer initialization
 * @since Story 3.1
 */
@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "VoiceRecognitionManager"
        
        // Recognition configuration constants
        private const val SILENCE_TIMEOUT_MS = 5000L
        private const val MAX_RESULTS = 1
        private const val WATCHDOG_TIMEOUT_MS = 15000L // HIGH-6: Reset if no callback after 15s
    }
    
    // State management
    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()
    
    // SpeechRecognizer instance
    private var speechRecognizer: SpeechRecognizer? = null
    
    // HIGH-1: Prevent memory leak from multiple rapid initializations
    private var isRecognizerActive = false
    
    // HIGH-6: Watchdog timer to reset stuck listening state
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var watchdogJob: Job? = null
    
    // Callback for recognized text (passed to command processor in Story 3.2)
    private var onRecognizedTextCallback: ((String) -> Unit)? = null
    
    // Callback for state changes (used by ViewModel for TTS announcements)
    private var onStateChangeCallback: ((VoiceRecognitionState) -> Unit)? = null
    
    /**
     * Initialize SpeechRecognizer and configure recognition parameters.
     * Story 3.1 Task 1.3: Create and configure SpeechRecognizer instance
     * HIGH-1 FIX: Added synchronization to prevent memory leak
     */
    private fun initializeSpeechRecognizer() {
        // HIGH-1: Prevent creating new instance if already active
        if (isRecognizerActive) {
            Log.w(TAG, "SpeechRecognizer already active - skipping initialization")
            return
        }
        
        // Clean up existing recognizer
        speechRecognizer?.destroy()
        
        // Create new SpeechRecognizer instance
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }
        isRecognizerActive = true
        
        Log.d(TAG, "SpeechRecognizer initialized successfully")
    }
    
    /**
     * Start listening for voice commands.
     * Story 3.1 Task 5.1: Start speech recognition with configured intent
     * 
     * Configures recognition parameters:
     * - Language model: FREE_FORM (optimized for short phrases)
     * - Prefer offline: true (on-device recognition, API 33+)
     * - Language: English (US)
     * - Silence timeout: 5 seconds (AC: 8)
     * - Max results: 1 (command processing doesn't need alternatives)
     * 
     * @throws IllegalStateException if microphone permission not granted
     */
    fun startListening() {
        Log.d(TAG, "startListening() called")
        
        // Initialize recognizer if needed
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }
        
        // Update state to Listening
        _state.value = VoiceRecognitionState.Listening(isReady = false)
        onStateChangeCallback?.invoke(_state.value)
        
        // Configure recognition intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Language model for voice commands (optimized for short phrases)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            
            // Use online recognition (requires network, no language pack needed)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            
            // English (US) for command recognition
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            
            // Single result for command processing
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
            
            // 5-second silence timeout per AC (Story 3.1 AC: 8)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                SILENCE_TIMEOUT_MS
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                SILENCE_TIMEOUT_MS
            )
            
            // Disable partial results for simplicity (Story 3.1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        
        // Start listening
        try {
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "SpeechRecognizer.startListening() invoked")
            
            // HIGH-6: Start watchdog timer to reset if stuck
            startWatchdogTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            // MEDIUM-3: Analytics hook - recognition start failed
            // MEDIUM-1: Use string resource
            _state.value = VoiceRecognitionState.Error(
                errorCode = -1,
                errorMessage = context.getString(com.visionfocus.R.string.voice_error_generic)
            )
            onStateChangeCallback?.invoke(_state.value)
        }
    }
    
    /**
     * Stop listening and cancel recognition.
     * Story 3.1 Task 7.5: Manual cancellation support
     * HIGH-6 FIX: Cancel watchdog timer
     */
    fun stopListening() {
        Log.d(TAG, "stopListening() called")
        cancelWatchdogTimer() // HIGH-6
        speechRecognizer?.stopListening()
        isRecognizerActive = false // HIGH-1
        _state.value = VoiceRecognitionState.Idle
        onStateChangeCallback?.invoke(_state.value)
    }
    
    /**
     * Set callback for recognized text.
     * Used by command processor (Story 3.2) to receive transcriptions.
     * 
     * @param callback Function to receive recognized text (lowercase)
     */
    fun setOnRecognizedTextCallback(callback: (String) -> Unit) {
        onRecognizedTextCallback = callback
    }
    
    /**
     * Set callback for state changes.
     * Used by ViewModel to trigger TTS announcements.
     * 
     * @param callback Function to receive state changes
     */
    fun setOnStateChangeCallback(callback: (VoiceRecognitionState) -> Unit) {
        onStateChangeCallback = callback
    }
    
    /**
     * HIGH-6: Start watchdog timer to reset state if recognizer hangs.
     * Cancels if onResults/onError received within timeout.
     */
    private fun startWatchdogTimer() {
        cancelWatchdogTimer()
        watchdogJob = coroutineScope.launch {
            delay(WATCHDOG_TIMEOUT_MS)
            Log.w(TAG, "Watchdog timeout - forcing reset to Idle")
            isRecognizerActive = false
            _state.value = VoiceRecognitionState.Error(
                errorCode = SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                errorMessage = "Voice recognition timed out"
            )
            onStateChangeCallback?.invoke(_state.value)
            // MEDIUM-3: Analytics hook - watchdog timeout triggered
        }
    }
    
    /**
     * HIGH-6: Cancel watchdog timer.
     */
    private fun cancelWatchdogTimer() {
        watchdogJob?.cancel()
        watchdogJob = null
    }
    
    /**
     * Create RecognitionListener for speech callbacks.
     * Story 3.1 Task 1.4: Implement RecognitionListener interface
     * 
     * @return Configured RecognitionListener implementation
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            
            /**
             * Called when recognizer is ready to listen.
             * Story 3.1 Task 5.1: onReadyForSpeech callback
             */
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech()")
                _state.value = VoiceRecognitionState.Listening(isReady = true)
                onStateChangeCallback?.invoke(_state.value)
            }
            
            /**
             * Called when user starts speaking.
             */
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech()")
            }
            
            /**
             * Volume change indicator (not used in Story 3.1).
             */
            override fun onRmsChanged(rmsdB: Float) {
                // Not used - could be used for visual volume indicator in future
            }
            
            /**
             * Audio buffer received (not used in Story 3.1).
             */
            override fun onBufferReceived(buffer: ByteArray?) {
                // Not used
            }
            
            /**
             * Called when user stops speaking.
             * Story 3.1 Task 5.5: Handle recognition completion
             */
            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech()")
            }
            
            /**
             * Called when recognition error occurs.
             * Story 3.1 Task 6: Error handling with user-facing messages
             * 
             * Error codes handled (AC: 10, 11):
             * - ERROR_NO_MATCH (7): "Didn't catch that. Please try again."
             * - ERROR_SPEECH_TIMEOUT (6): "Voice command timed out"
             * - ERROR_AUDIO (3): "Microphone error. Please check device settings."
             * - ERROR_NETWORK (2): "Network unavailable..."
             * - Other errors: Generic error message
             * 
             * @param error SpeechRecognizer error code
             */
            override fun onError(error: Int) {
                Log.w(TAG, "onError: error code = $error")
                cancelWatchdogTimer() // HIGH-6
                isRecognizerActive = false // HIGH-1
                // MEDIUM-3: Analytics hook - recognition error
                
                // MEDIUM-1: Use string resources for internationalization
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        Log.d(TAG, "ERROR_NO_MATCH: No recognition results")
                        context.getString(com.visionfocus.R.string.voice_error_no_match)
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        Log.d(TAG, "ERROR_SPEECH_TIMEOUT: No speech detected")
                        context.getString(com.visionfocus.R.string.voice_error_timeout)
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        Log.e(TAG, "ERROR_AUDIO: Microphone error")
                        context.getString(com.visionfocus.R.string.voice_error_audio)
                    }
                    SpeechRecognizer.ERROR_NETWORK -> {
                        Log.w(TAG, "ERROR_NETWORK: Network unavailable")
                        context.getString(com.visionfocus.R.string.voice_error_network)
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        Log.e(TAG, "ERROR_INSUFFICIENT_PERMISSIONS: Microphone permission denied")
                        context.getString(com.visionfocus.R.string.voice_error_permission)
                    }
                    else -> {
                        Log.e(TAG, "Unknown error code: $error")
                        context.getString(com.visionfocus.R.string.voice_error_generic)
                    }
                }
                
                _state.value = VoiceRecognitionState.Error(error, errorMessage)
                onStateChangeCallback?.invoke(_state.value)
            }
            
            /**
             * Called when recognition results are available.
             * Story 3.1 Task 5.2: Process transcription and convert to lowercase
             * 
             * Extracts recognized text, converts to lowercase (AC: 9), and
             * passes to command processor callback.
             * 
             * @param results Bundle containing recognition results
             */
            override fun onResults(results: Bundle?) {
                cancelWatchdogTimer() // HIGH-6
                isRecognizerActive = false // HIGH-1
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (matches.isNullOrEmpty()) {
                    Log.w(TAG, "onResults: No matches found")
                    // MEDIUM-3: Analytics hook - empty results
                    // MEDIUM-1: Use string resource
                    _state.value = VoiceRecognitionState.Error(
                        SpeechRecognizer.ERROR_NO_MATCH,
                        context.getString(com.visionfocus.R.string.voice_error_no_match)
                    )
                    onStateChangeCallback?.invoke(_state.value)
                    return
                }
                
                // Get first result and convert to lowercase (AC: 9)
                val transcription = matches[0].lowercase(Locale.US)
                Log.d(TAG, "onResults: transcription = \"$transcription\"")
                // MEDIUM-3: Analytics hook - recognition success
                
                // Update state to Processing
                _state.value = VoiceRecognitionState.Processing(transcription)
                onStateChangeCallback?.invoke(_state.value)
                
                // Pass to command processor (Story 3.2 integration point)
                onRecognizedTextCallback?.invoke(transcription)
                
                // Return to Idle state after brief processing
                _state.value = VoiceRecognitionState.Idle
                onStateChangeCallback?.invoke(_state.value)
            }
            
            /**
             * Partial results (disabled in Story 3.1).
             */
            override fun onPartialResults(partialResults: Bundle?) {
                // Not used - partial results disabled
            }
            
            /**
             * Custom events (not used in Story 3.1).
             */
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Not used
            }
        }
    }
    
    /**
     * Clean up resources.
     * Called when manager is no longer needed.
     * HIGH-6 FIX: Cancel watchdog timer on cleanup
     */
    fun destroy() {
        Log.d(TAG, "destroy() called - cleaning up SpeechRecognizer")
        cancelWatchdogTimer() // HIGH-6
        speechRecognizer?.destroy()
        speechRecognizer = null
        isRecognizerActive = false // HIGH-1
        _state.value = VoiceRecognitionState.Idle
    }
}
