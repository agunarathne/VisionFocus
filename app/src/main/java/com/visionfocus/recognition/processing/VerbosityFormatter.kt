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
 * - DETAILED: Category + confidence + distance + position 
 *   ("High confidence: chair, close by, in center of view")
 * 
 * Story 4.5: Enhanced detailed mode with spatial information (distance and position)
 * 
 * Confidence Level Mapping:
 * - High: ≥0.85 → "high confidence"
 * - Medium: 0.70-0.84 → "medium confidence"
 * - Low: 0.60-0.69 → "low confidence"
 */
@Singleton
class VerbosityFormatter @Inject constructor() {
    
    companion object {
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.85f
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.70f
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
        allDetections: List<DetectionResult> = listOf(topDetection)
    ): String {
        return when (mode) {
            VerbosityMode.BRIEF -> formatBrief(topDetection)
            VerbosityMode.STANDARD -> formatStandard(topDetection)
            VerbosityMode.DETAILED -> formatDetailed(topDetection, allDetections)
        }
    }
    
    /**
     * Convenience overload for formatting a list of detections.
     * Uses the first detection as the top detection.
     * 
     * @param detections List of detection results
     * @param mode User's verbosity preference
     * @return TTS-ready announcement string
     */
    fun format(
        detections: List<DetectionResult>,
        mode: VerbosityMode
    ): String {
        if (detections.isEmpty()) {
            return "No objects detected"
        }
        return format(detections.first(), mode, detections)
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
     * Detailed mode: Category + confidence + distance + position.
     * 
     * Story 4.5: Enhanced with spatial information
     * 
     * Examples:
     * - "High confidence: chair, close by, in center of view"
     * - "High confidence: table, at medium distance, on the right"
     * - "Medium confidence: person, far away, on the left side, near the top"
     * 
     * Multiple objects example:
     * - "I see a chair close by in the center, and a table at medium distance on the right"
     * 
     * @param topDetection Top detection result to announce
     * @param allDetections All detected objects for multi-object announcements
     * @return Comprehensive formatted announcement with spatial information
     */
    fun formatDetailed(
        topDetection: DetectionResult,
        allDetections: List<DetectionResult>
    ): String {
        // Story 4.5: Check if spatial info is available for multi-object formatting
        // Note: formatMultipleObjectsSpatial uses mapNotNull to handle detections without spatial info
        if (topDetection.spatialInfo != null && allDetections.size > 1) {
            // Format multi-object announcement with spatial organization
            return formatMultipleObjectsSpatial(allDetections)
        }
        
        // Single object or no spatial info - use simpler format
        val parts = mutableListOf<String>()
        
        // Confidence + category
        val confidenceLevel = getConfidenceLevel(topDetection.confidence)
        val category = topDetection.label
        parts.add("$confidenceLevel confidence: $category")
        
        // Story 4.5: Add spatial information if available
        if (topDetection.spatialInfo != null) {
            val spatialDesc = topDetection.spatialInfo.toNaturalLanguage()
            parts.add(spatialDesc)
        } else {
            // Fallback for backward compatibility (Story 4.1):
            // - When screenSize is null (e.g., tests without camera preview)
            // - When recognition occurs before camera preview is measured
            val position = getPositionLegacy(topDetection.boundingBox)
            parts.add(position)
        }
        
        return parts.joinToString(", ")
    }
    
    /**
     * Format multiple objects with spatial organization (Story 4.5)
     * 
     * Example: "I see a chair close by in the center, and a table at medium distance on the right"
     * 
     * @param detections All detected objects with spatial information
     * @return Natural language multi-object announcement
     */
    private fun formatMultipleObjectsSpatial(detections: List<DetectionResult>): String {
        // Sort by distance (CLOSE first) then confidence
        val sortedDetections = detections
            .sortedWith(
                compareBy<DetectionResult> { it.spatialInfo?.distance?.ordinal ?: Int.MAX_VALUE }
                    .thenByDescending { it.confidence }
            )
        
        // Format each object with spatial description
        val formattedObjects = sortedDetections.mapNotNull { detection ->
            detection.spatialInfo?.let { spatialInfo ->
                val article = getArticle(detection.label)
                "$article ${detection.label} ${spatialInfo.toNaturalLanguage()}"
            }
        }
        
        // Build natural sentence
        return when (formattedObjects.size) {
            0 -> "No objects detected"
            1 -> "I see ${formattedObjects[0]}"
            2 -> "I see ${formattedObjects[0]}, and ${formattedObjects[1]}"
            else -> "I see ${formattedObjects.dropLast(1).joinToString(", ")}, and ${formattedObjects.last()}"
        }
    }
    
    /**
     * Get appropriate article for object label.
     * 
     * @param label Object label
     * @return "a" or "an" based on first letter
     */
    private fun getArticle(label: String): String {
        val firstChar = label.firstOrNull()?.lowercaseChar() ?: return "a"
        return if (firstChar in listOf('a', 'e', 'i', 'o', 'u')) "an" else "a"
    }
    
    /**
     * Legacy method for backward compatibility (Story 4.1)
     * Story 4.5 uses SpatialAnalyzer instead when spatial info is available
     * 
     * @param box Bounding box with normalized coordinates [0-1]
     * @return Position description: "on the left", "in center of view", "on the right"
     */
    private fun getPositionLegacy(box: BoundingBox): String {
        val centerX = (box.xMin + box.xMax) / 2.0f
        val leftBoundary = 0.33f
        val rightBoundary = 0.66f
        return when {
            centerX < leftBoundary -> "on the left"
            centerX > rightBoundary -> "on the right"
            else -> "in center of view"
        }
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
}
