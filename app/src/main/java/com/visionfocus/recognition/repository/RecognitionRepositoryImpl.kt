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
        Log.d(TAG, "Performing recognition...")
        
        // Story 2.1: Raw TFLite inference
        val rawResult = objectRecognitionService.recognizeObject()
        
        Log.d(TAG, "Raw inference: ${rawResult.detections.size} detections")
        
        // Story 2.2: Apply confidence filtering (≥0.6 threshold)
        val filtered = confidenceFilter.filter(rawResult.detections)
        Log.d(TAG, "After confidence filtering: ${filtered.size} detections (removed ${rawResult.detections.size - filtered.size})")
        
        // Story 2.2: Apply Non-Maximum Suppression (remove overlapping duplicates)
        val deduplicated = nonMaximumSuppression.apply(filtered)
        Log.d(TAG, "After NMS: ${deduplicated.size} detections (removed ${filtered.size - deduplicated.size} duplicates)")
        
        // Create filtered result
        val result = RecognitionResult(
            detections = deduplicated,
            timestampMs = rawResult.timestampMs,
            latencyMs = rawResult.latencyMs
        )
        
        // Store in-memory (Story 2.1 scope)
        lastResult = result
        
        Log.d(TAG, "Recognition completed: ${result.detections.size} final detections, ${result.latencyMs}ms")
        
        return result
    }
    
    override fun getLastResult(): RecognitionResult? {
        return lastResult
    }
}
