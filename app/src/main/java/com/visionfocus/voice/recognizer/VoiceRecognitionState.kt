package com.visionfocus.voice.recognizer

/**
 * Voice Recognition State
 * Story 3.1 Task 7: State management for voice recognition lifecycle
 * 
 * Represents the current state of the voice recognition system.
 * Used by VoiceRecognitionViewModel to expose state to UI observers.
 * 
 * @since Story 3.1
 */
sealed class VoiceRecognitionState {
    
    /**
     * Voice recognition is inactive.
     * Button shows normal microphone icon.
     */
    data object Idle : VoiceRecognitionState()
    
    /**
     * Voice recognition is actively listening for speech.
     * Button shows pulsing animation.
     * 
     * @property isReady True when recognizer is ready to receive speech
     */
    data class Listening(val isReady: Boolean = false) : VoiceRecognitionState()
    
    /**
     * Processing recognized speech (brief state before returning to Idle).
     * 
     * @property transcription The recognized text (lowercase)
     */
    data class Processing(val transcription: String) : VoiceRecognitionState()
    
    /**
     * Voice recognition error occurred.
     * 
     * @property errorCode Android SpeechRecognizer error code
     * @property errorMessage User-facing error message for TTS announcement
     */
    data class Error(val errorCode: Int, val errorMessage: String) : VoiceRecognitionState()
}
