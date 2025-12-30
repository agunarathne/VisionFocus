package com.visionfocus.recognition.repository

import android.util.Log
import com.visionfocus.recognition.models.RecognitionResult
import com.visionfocus.recognition.service.ObjectRecognitionService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecognitionRepository
 * 
 * Story 2.1: In-memory storage only
 * Story 4.2: Will add Room database for recognition history (last 50 results)
 */
@Singleton
class RecognitionRepositoryImpl @Inject constructor(
    private val objectRecognitionService: ObjectRecognitionService
) : RecognitionRepository {
    
    companion object {
        private const val TAG = "RecognitionRepositoryImpl"
    }
    
    // In-memory storage (Story 2.1 - no persistence yet)
    private var lastResult: RecognitionResult? = null
    
    override suspend fun performRecognition(): RecognitionResult {
        Log.d(TAG, "Performing recognition...")
        
        val result = objectRecognitionService.recognizeObject()
        
        // Store in-memory (Story 2.1 scope)
        lastResult = result
        
        Log.d(TAG, "Recognition completed: ${result.detections.size} detections, ${result.latencyMs}ms")
        
        return result
    }
    
    override fun getLastResult(): RecognitionResult? {
        return lastResult
    }
}
