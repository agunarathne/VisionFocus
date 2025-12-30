package com.visionfocus.recognition.repository

import com.visionfocus.recognition.models.RecognitionResult

/**
 * Repository interface for object recognition operations
 * 
 * Provides abstraction layer between UI/ViewModel and recognition service
 * Story 4.2 will add persistence layer (Room database) for recognition history
 */
interface RecognitionRepository {
    
    /**
     * Perform object recognition on current camera frame
     * 
     * @return RecognitionResult with detected objects and timing info
     * @throws SecurityException if camera permission not granted
     * @throws IllegalStateException if service not ready
     */
    suspend fun performRecognition(): RecognitionResult
    
    /**
     * Get the last recognition result (in-memory only for Story 2.1)
     * Story 4.2 will enhance this with Room database persistence
     * 
     * @return Last RecognitionResult or null if no recognition performed yet
     */
    fun getLastResult(): RecognitionResult?
}
