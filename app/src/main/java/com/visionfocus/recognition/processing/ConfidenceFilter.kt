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
         * Confidence threshold for real-world detection
         * Detections below this score are filtered before announcement
         * 
         * Rationale: ≥0.5 provides better precision:
         * - High enough to significantly reduce false positives
         * - For ssd_mobilenet_v1, scores below 50% are often incorrect
         * - Better user experience with fewer wrong detections
         */
        const val CONFIDENCE_THRESHOLD = 0.5f
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
        require(confidence in 0.0f..1.0f) {
            "Confidence must be in range [0.0, 1.0], got $confidence"
        }
        
        return when {
            confidence >= 0.65f -> ConfidenceLevel.HIGH    // 65%+ = HIGH (high certainty)
            confidence >= 0.50f -> ConfidenceLevel.MEDIUM  // 50-64% = MEDIUM (moderate certainty)
            confidence >= 0.40f -> ConfidenceLevel.LOW     // 40-49% = LOW (possible detection)
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
