package com.visionfocus.recognition.spatial

import com.visionfocus.recognition.models.BoundingBox

/**
 * Spatial information for detected objects
 * 
 * Story 4.5 AC: Spatial information includes position and distance
 * 
 * Contains:
 * - position: Screen zone (9 possible zones in 3x3 grid)
 * - distance: Relative distance (CLOSE/MEDIUM/FAR)
 * - boundingBox: Original normalized bounding box from TFLite
 * - screenSize: Screen dimensions used for calculations
 * 
 * Example usage:
 * ```
 * val spatialInfo = analyzer.analyze(boundingBox, screenSize)
 * val description = spatialInfo.toNaturalLanguage() // "close by in center of view"
 * ```
 */
data class SpatialInfo(
    val position: Position,
    val distance: Distance,
    val boundingBox: BoundingBox,
    val screenSize: Size
) {
    /**
     * Convert spatial info to natural language description
     * 
     * Story 4.5 AC: Spatial announcements use natural language
     * 
     * Examples:
     * - "close by in center of view"
     * - "at medium distance on the left"
     * - "far away on the right side, near the top"
     * 
     * @return Natural language spatial description
     */
    fun toNaturalLanguage(): String {
        val distanceText = DistanceFormatter.toNaturalLanguage(distance, withPreposition = true)
        val positionText = PositionFormatter.toNaturalLanguage(position)
        
        return "$distanceText $positionText"
    }
}
