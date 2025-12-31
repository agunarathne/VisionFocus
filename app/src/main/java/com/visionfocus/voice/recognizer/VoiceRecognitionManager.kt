package com.visionfocus.voice.recognizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        
        // Error messages for user announcement (AC: 10, 11)
        private const val ERROR_MSG_NO_MATCH = "Didn't catch that. Please try again."
        private const val ERROR_MSG_TIMEOUT = "Voice command timed out"
        private const val ERROR_MSG_AUDIO = "Microphone error. Please check device settings."
        private const val ERROR_MSG_NETWORK = "Network unavailable. Voice recognition requires internet."
        private const val ERROR_MSG_GENERIC = "Voice recognition error. Please try again."
    }
    
    // State management
    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()
    
    // SpeechRecognizer instance
    private var speechRecognizer: SpeechRecognizer? = null
    
    // Callback for recognized text (passed to command processor in Story 3.2)
    private var onRecognizedTextCallback: ((String) -> Unit)? = null
    
    // Callback for state changes (used by ViewModel for TTS announcements)
    private var onStateChangeCallback: ((VoiceRecognitionState) -> Unit)? = null
    
    /**
     * Initialize SpeechRecognizer and configure recognition parameters.
     * Story 3.1 Task 1.3: Create and configure SpeechRecognizer instance
     */
    private fun initializeSpeechRecognizer() {
        // Clean up existing recognizer
        speechRecognizer?.destroy()
        
        // Create new SpeechRecognizer instance
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }
        
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
            
            // Prefer on-device recognition (API 33+) for privacy and offline support
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _state.value = VoiceRecognitionState.Error(
                errorCode = -1,
                errorMessage = ERROR_MSG_GENERIC
            )
            onStateChangeCallback?.invoke(_state.value)
        }
    }
    
    /**
     * Stop listening and cancel recognition.
     * Story 3.1 Task 7.5: Manual cancellation support
     */
    fun stopListening() {
        Log.d(TAG, "stopListening() called")
        speechRecognizer?.stopListening()
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
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        Log.d(TAG, "ERROR_NO_MATCH: No recognition results")
                        ERROR_MSG_NO_MATCH
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        Log.d(TAG, "ERROR_SPEECH_TIMEOUT: No speech detected")
                        ERROR_MSG_TIMEOUT
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        Log.e(TAG, "ERROR_AUDIO: Microphone error")
                        ERROR_MSG_AUDIO
                    }
                    SpeechRecognizer.ERROR_NETWORK -> {
                        Log.w(TAG, "ERROR_NETWORK: Network unavailable")
                        ERROR_MSG_NETWORK
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        Log.e(TAG, "ERROR_INSUFFICIENT_PERMISSIONS: Microphone permission denied")
                        "Microphone permission required for voice commands"
                    }
                    else -> {
                        Log.e(TAG, "Unknown error code: $error")
                        ERROR_MSG_GENERIC
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
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (matches.isNullOrEmpty()) {
                    Log.w(TAG, "onResults: No matches found")
                    _state.value = VoiceRecognitionState.Error(
                        SpeechRecognizer.ERROR_NO_MATCH,
                        ERROR_MSG_NO_MATCH
                    )
                    onStateChangeCallback?.invoke(_state.value)
                    return
                }
                
                // Get first result and convert to lowercase (AC: 9)
                val transcription = matches[0].lowercase(Locale.US)
                Log.d(TAG, "onResults: transcription = \"$transcription\"")
                
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
     */
    fun destroy() {
        Log.d(TAG, "destroy() called - cleaning up SpeechRecognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
        _state.value = VoiceRecognitionState.Idle
    }
}
