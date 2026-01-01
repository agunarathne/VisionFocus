package com.visionfocus.recognition.processing

import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Formats recognition results into TTS announcements based on verbosity mode.
 * 
 * Verbosity Modes (FR4, AC2-AC4):
 * - BRIEF: Category only ("Chair")
 * - STANDARD: Category + confidence ("Chair with high confidence")
 * - DETAILED: Category + confidence + position + count 
 *   ("High confidence: chair in center of view. Two chairs detected.")
 * 
 * Confidence Level Mapping:
 * - High: ≥0.85 → "high confidence"
 * - Medium: 0.70-0.84 → "medium confidence"
 * - Low: 0.60-0.69 → "low confidence"
 * 
 * Position Detection (from bounding box center X):
 * - Left: centerX < 0.33
 * - Center: 0.33 ≤ centerX ≤ 0.66
 * - Right: centerX > 0.66
 */
@Singleton
class VerbosityFormatter @Inject constructor() {
    
    companion object {
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.85f
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.70f
        private const val LEFT_BOUNDARY = 0.33f
        private const val RIGHT_BOUNDARY = 0.66f
    }
    
    /**
     * Primary formatting dispatcher.
     * 
     * @param topDetection Top recognition result to announce (highest confidence)
     * @param mode User's verbosity preference
     * @param allDetections All detected objects (for counting in detailed mode)
     * @return TTS-ready announcement string
     */
    fun format(
        topDetection: DetectionResult,
        mode: VerbosityMode,
        allDetections: List<DetectionResult>
    ): String {
        return when (mode) {
            VerbosityMode.BRIEF -> formatBrief(topDetection)
            VerbosityMode.STANDARD -> formatStandard(topDetection)
            VerbosityMode.DETAILED -> formatDetailed(topDetection, allDetections)
        }
    }
    
    /**
     * Brief mode: Category only.
     * 
     * Example: "Chair"
     * 
     * @param detection Detection result to announce
     * @return Category name with first letter capitalized
     */
    fun formatBrief(detection: DetectionResult): String {
        return detection.label.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Standard mode: Category + confidence level.
     * 
     * Examples:
     * - "Chair with high confidence" (≥0.85)
     * - "Bottle with medium confidence" (0.70-0.84)
     * - "Cup with low confidence" (0.60-0.69)
     * 
     * @param detection Detection result to announce
     * @return Formatted announcement with confidence level
     */
    fun formatStandard(detection: DetectionResult): String {
        val confidenceLevel = getConfidenceLevel(detection.confidence)
        val category = detection.label.replaceFirstChar { it.uppercase() }
        return "$category with $confidenceLevel confidence"
    }
    
    /**
     * Detailed mode: Category + confidence + position + count.
     * 
     * Examples:
     * - "High confidence: chair in center of view"
     * - "High confidence: chair on the left. Two chairs detected."
     * - "Medium confidence: person on the right"
     * 
     * @param topDetection Top detection result to announce
     * @param allDetections All detected objects for counting
     * @return Comprehensive formatted announcement
     */
    fun formatDetailed(
        topDetection: DetectionResult,
        allDetections: List<DetectionResult>
    ): String {
        val parts = mutableListOf<String>()
        
        // Confidence + category
        val confidenceLevel = getConfidenceLevel(topDetection.confidence)
        val category = topDetection.label
        parts.add("$confidenceLevel confidence: $category")
        
        // Position
        val position = getPosition(topDetection.boundingBox)
        parts.add(position)
        
        // Count (if multiple detections of same category)
        val count = allDetections.count { it.label == topDetection.label }
        if (count > 1) {
            val countText = when (count) {
                2 -> "Two"
                3 -> "Three"
                4 -> "Four"
                5 -> "Five"
                else -> count.toString()
            }
            val plural = if (topDetection.label.endsWith("s")) {
                topDetection.label
            } else {
                "${topDetection.label}s"
            }
            parts.add("$countText $plural detected")
        }
        
        return parts.joinToString(". ") + "."
    }
    
    /**
     * Maps confidence score to verbal level.
     * 
     * @param confidence Confidence score [0.0-1.0]
     * @return Confidence level description: "High", "Medium", or "Low"
     */
    private fun getConfidenceLevel(confidence: Float): String {
        return when {
            confidence >= HIGH_CONFIDENCE_THRESHOLD -> "High"
            confidence >= MEDIUM_CONFIDENCE_THRESHOLD -> "Medium"
            else -> "Low"
        }
    }
    
    /**
     * Determines object position from bounding box center X coordinate.
     * 
     * @param box Bounding box with normalized coordinates [0-1]
     * @return Position description: "on the left", "in center of view", "on the right"
     */
    private fun getPosition(box: BoundingBox): String {
        val centerX = (box.xMin + box.xMax) / 2.0f
        return when {
            centerX < LEFT_BOUNDARY -> "on the left"
            centerX > RIGHT_BOUNDARY -> "on the right"
            else -> "in center of view"
        }
    }
}
