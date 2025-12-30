package com.visionfocus.ui.recognition

import com.visionfocus.recognition.processing.FilteredDetection

/**
 * UI state representation for recognition screen
 * 
 * Story 2.3 Task 1.4: Define RecognitionUiState sealed class
 * Story 2.4 Task 9: Extended with camera lifecycle states
 * Architecture Decision 2: StateFlow pattern for state management
 * 
 * State transitions:
 * Idle → Capturing → Recognizing → Announcing → Success → Idle
 * Idle → Capturing → Recognizing → Error → Idle
 * Idle → Capturing → CameraError → Idle
 */
sealed class RecognitionUiState {
    
    /**
     * Initial state - ready to perform recognition
     */
    object Idle : RecognitionUiState()
    
    /**
     * Camera capturing frame (Story 2.4)
     * State between FAB tap and TFLite inference
     * Includes 1-second stabilization delay
     */
    object Capturing : RecognitionUiState()
    
    /**
     * Recognition in progress (TFLite inference)
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
    
    /**
     * Camera initialization or capture error (Story 2.4)
     * 
     * @param message User-friendly camera error message
     */
    data class CameraError(val message: String) : RecognitionUiState()
}
