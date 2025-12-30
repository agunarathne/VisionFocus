package com.visionfocus.ui.recognition

import com.visionfocus.recognition.processing.FilteredDetection

/**
 * UI state representation for recognition screen
 * 
 * Story 2.3 Task 1.4: Define RecognitionUiState sealed class
 * Architecture Decision 2: StateFlow pattern for state management
 * 
 * State transitions:
 * Idle → Recognizing → Announcing → Success → Idle
 * Idle → Recognizing → Error → Idle
 */
sealed class RecognitionUiState {
    
    /**
     * Initial state - ready to perform recognition
     */
    object Idle : RecognitionUiState()
    
    /**
     * Recognition in progress (camera capturing + TFLite inference)
     * Story 2.1: ≤320ms inference latency
     */
    object Recognizing : RecognitionUiState()
    
    /**
     * TTS announcement in progress
     * Story 2.2: ≤200ms TTS initiation
     */
    object Announcing : RecognitionUiState()
    
    /**
     * Recognition completed successfully
     * 
     * @param results Filtered detection results from Story 2.2
     * @param announcement Natural language TTS string
     * @param latency Total pipeline latency in milliseconds
     */
    data class Success(
        val results: List<FilteredDetection>,
        val announcement: String,
        val latency: Long
    ) : RecognitionUiState()
    
    /**
     * Recognition failed with error
     * 
     * @param message User-friendly error message
     */
    data class Error(val message: String) : RecognitionUiState()
}
