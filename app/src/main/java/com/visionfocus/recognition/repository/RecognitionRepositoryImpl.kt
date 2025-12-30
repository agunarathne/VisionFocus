package com.visionfocus.recognition.repository

import android.graphics.Bitmap
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
 * Story 2.4: Added Bitmap parameter for camera-captured frames
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
    
    /**
     * CRITICAL FIX: Ensure ObjectRecognitionService is initialized
     * This is a fallback in case Application.onCreate() didn't run or failed
     */
    override fun ensureInitialized() {
        try {
            Log.d(TAG, "Ensuring ObjectRecognitionService is initialized...")
            objectRecognitionService.initialize()
            Log.d(TAG, "✓ Service initialization confirmed")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Service initialization failed in ensureInitialized()", e)
            throw IllegalStateException("Failed to initialize recognition service: ${e.message}", e)
        }
    }
    
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
    
    /**
     * Story 2.4: Perform recognition on captured Bitmap
     * 
     * @param bitmap Camera-captured frame
     * @return RecognitionResult with detections and timing
     */
    override suspend fun performRecognition(bitmap: Bitmap): RecognitionResult {
        // Story 2.4: Raw TFLite inference with Bitmap
        val rawResult = objectRecognitionService.recognizeObject(bitmap)
        
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
        
        // Store in-memory
        lastResult = result
        
        return result
    }
    
    override fun getLastResult(): RecognitionResult? {
        return lastResult
    }
}
