package com.visionfocus.recognition.repository

import android.util.Log
import com.visionfocus.recognition.models.RecognitionResult
import com.visionfocus.recognition.processing.ConfidenceFilter
import com.visionfocus.recognition.processing.NonMaximumSuppression
import com.visionfocus.recognition.service.ObjectRecognitionService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecognitionRepository
 * 
 * Story 2.1: In-memory storage only
 * Story 2.2: Added confidence filtering + NMS post-processing pipeline
 * Story 4.2: Will add Room database for recognition history (last 50 results)
 */
@Singleton
class RecognitionRepositoryImpl @Inject constructor(
    private val objectRecognitionService: ObjectRecognitionService,
    private val confidenceFilter: ConfidenceFilter,
    private val nonMaximumSuppression: NonMaximumSuppression
) : RecognitionRepository {
    
    companion object {
        private const val TAG = "RecognitionRepositoryImpl"
    }
    
    // In-memory storage (Story 2.1 - no persistence yet)
    private var lastResult: RecognitionResult? = null
    
    override suspend fun performRecognition(): RecognitionResult {
        // Story 2.1: Raw TFLite inference
        val rawResult = objectRecognitionService.recognizeObject()
        
        // Story 2.2: Apply confidence filtering (≥0.6 threshold)
        val filtered = confidenceFilter.filter(rawResult.detections)
        
        // Story 2.2: Apply Non-Maximum Suppression (remove overlapping duplicates)
        val deduplicated = nonMaximumSuppression.apply(filtered)
        
        // Create filtered result
        val result = RecognitionResult(
            detections = deduplicated,
            timestampMs = rawResult.timestampMs,
            latencyMs = rawResult.latencyMs
        )
        
        // Store in-memory (Story 2.1 scope)
        lastResult = result
        
        return result
    }
    
    override fun getLastResult(): RecognitionResult? {
        return lastResult
    }
}
