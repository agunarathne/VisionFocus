package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.DetectionResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Confidence filtering for object detection results
 * 
 * Story 2.2 AC1: Filter detections below confidence threshold
 * Research-validated threshold ≥0.6 for optimal recall/precision balance
 * 
 * Usage:
 * ```
 * val filtered = confidenceFilter.filter(rawDetections)
 * ```
 */
@Singleton
class ConfidenceFilter @Inject constructor() {
    
    companion object {
        /**
         * Research-validated confidence threshold
         * Detections below this score are filtered before announcement
         * 
         * Rationale: ≥0.6 provides optimal balance:
         * - Recall: Captures most valid detections
         * - Precision: False positive rate ≤10% for HIGH confidence
         */
        const val CONFIDENCE_THRESHOLD = 0.6f
    }
    
    /**
     * Filter detections below confidence threshold
     * 
     * @param detections Raw detection results from TFLite inference
     * @return Filtered list containing only detections ≥0.6 confidence
     */
    fun filter(detections: List<DetectionResult>): List<DetectionResult> {
        // Fast path: empty input
        if (detections.isEmpty()) return emptyList()
        
        return detections.filter { it.confidence >= CONFIDENCE_THRESHOLD }
    }
    
    /**
     * Categorize confidence score into HIGH/MEDIUM/LOW level
     * 
     * Story 2.2 AC3: Confidence level categorization
     * 
     * @param confidence Detection confidence score
     * @return ConfidenceLevel enum (HIGH/MEDIUM/LOW)
     * @throws IllegalArgumentException if confidence below threshold
     */
    fun categorizeConfidence(confidence: Float): ConfidenceLevel {
        return when {
            confidence >= 0.85f -> ConfidenceLevel.HIGH
            confidence >= 0.70f -> ConfidenceLevel.MEDIUM
            confidence >= 0.60f -> ConfidenceLevel.LOW
            else -> throw IllegalArgumentException(
                "Confidence $confidence below threshold $CONFIDENCE_THRESHOLD"
            )
        }
    }
    
    /**
     * Convert DetectionResult to FilteredDetection with confidence categorization
     * 
     * @param detection Raw detection result
     * @return FilteredDetection with confidence level
     * @throws IllegalArgumentException if confidence below threshold
     */
    fun toFilteredDetection(detection: DetectionResult): FilteredDetection {
        return FilteredDetection(
            label = detection.label,
            confidence = detection.confidence,
            confidenceLevel = categorizeConfidence(detection.confidence),
            boundingBox = detection.boundingBox
        )
    }
}
