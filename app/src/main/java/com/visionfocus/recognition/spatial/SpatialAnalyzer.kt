package com.visionfocus.recognition.spatial

import com.visionfocus.recognition.models.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes bounding boxes to determine spatial position and distance
 * 
 * Story 4.5 AC: Position and distance calculations
 * 
 * Position Calculation:
 * - Screen divided into 3x3 grid
 * - Horizontal zones: LEFT (0-33%), CENTER (33-66%), RIGHT (66-100%)
 * - Vertical zones: TOP (0-33%), CENTER (33-66%), BOTTOM (66-100%)
 * - Uses ±5% tolerance to prevent boundary jitter
 * 
 * Distance Calculation:
 * - Based on bounding box area as percentage of screen
 * - CLOSE: >40% (within arm's reach ~0.5-1.5m)
 * - MEDIUM: 20-40% (a few steps away ~1.5-3.0m)
 * - FAR: <20% (across room >3.0m)
 * 
 * Thread-safe singleton managed by Hilt
 */
@Singleton
class SpatialAnalyzer @Inject constructor() {
    
    companion object {
        // Horizontal position boundaries
        private const val LEFT_BOUNDARY = 0.33f
        private const val RIGHT_BOUNDARY = 0.66f
        
        // Vertical position boundaries
        private const val TOP_BOUNDARY = 0.33f
        private const val BOTTOM_BOUNDARY = 0.66f
        
        // Tolerance to prevent boundary jitter (±5%)
        private const val TOLERANCE = 0.05f
    }
    
    /**
     * Analyze bounding box to determine spatial information
     * 
     * Story 4.5 AC: Position and distance calculations
     * 
     * @param boundingBox Normalized bounding box from TFLite (0.0-1.0 range)
     * @param screenSize Screen dimensions in pixels
     * @return SpatialInfo with position and distance
     */
    fun analyze(boundingBox: BoundingBox, screenSize: Size): SpatialInfo {
        val position = calculatePosition(boundingBox)
        val distance = calculateDistance(boundingBox)
        
        return SpatialInfo(
            position = position,
            distance = distance,
            boundingBox = boundingBox,
            screenSize = screenSize
        )
    }
    
    /**
     * Calculate position from bounding box center point
     * 
     * Story 4.5 AC: Position determination (left/center/right, top/bottom)
     * 
     * Screen divided into 9 zones (3x3 grid):
     * - Horizontal: LEFT (0-33%), CENTER (33-66%), RIGHT (66-100%)
     * - Vertical: TOP (0-33%), CENTER (33-66%), BOTTOM (66-100%)
     * 
     * Tolerance zones (±5%) prevent boundary jitter when object moves slightly
     * 
     * @param boundingBox Normalized bounding box (0.0-1.0 coordinates)
     * @return Position enum indicating screen zone
     */
    private fun calculatePosition(boundingBox: BoundingBox): Position {
        // Calculate center point (normalized 0.0-1.0)
        val centerX = (boundingBox.xMin + boundingBox.xMax) / 2f
        val centerY = (boundingBox.yMin + boundingBox.yMax) / 2f
        
        // Determine horizontal zone with tolerance
        val horizontalZone = when {
            centerX < LEFT_BOUNDARY - TOLERANCE -> HorizontalZone.LEFT
            centerX > RIGHT_BOUNDARY + TOLERANCE -> HorizontalZone.RIGHT
            else -> HorizontalZone.CENTER
        }
        
        // Determine vertical zone with tolerance
        val verticalZone = when {
            centerY < TOP_BOUNDARY - TOLERANCE -> VerticalZone.TOP
            centerY > BOTTOM_BOUNDARY + TOLERANCE -> VerticalZone.BOTTOM
            else -> VerticalZone.CENTER
        }
        
        // Combine zones into Position enum
        return when (horizontalZone) {
            HorizontalZone.LEFT -> when (verticalZone) {
                VerticalZone.TOP -> Position.TOP_LEFT
                VerticalZone.CENTER -> Position.CENTER_LEFT
                VerticalZone.BOTTOM -> Position.BOTTOM_LEFT
            }
            HorizontalZone.CENTER -> when (verticalZone) {
                VerticalZone.TOP -> Position.TOP_CENTER
                VerticalZone.CENTER -> Position.CENTER_CENTER
                VerticalZone.BOTTOM -> Position.BOTTOM_CENTER
            }
            HorizontalZone.RIGHT -> when (verticalZone) {
                VerticalZone.TOP -> Position.TOP_RIGHT
                VerticalZone.CENTER -> Position.CENTER_RIGHT
                VerticalZone.BOTTOM -> Position.BOTTOM_RIGHT
            }
        }
    }
    
    /**
     * Calculate distance from bounding box area
     * 
     * Story 4.5 AC: Distance estimation based on box size percentage
     * 
     * Distance estimation based on box size percentage:
     * - CLOSE: >40% of screen (object fills significant portion)
     * - MEDIUM: 20-40% of screen (moderate size)
     * - FAR: <20% of screen (small object)
     * 
     * Calibrated for typical object sizes:
     * - Chair at 1m: ~40% (CLOSE)
     * - Chair at 2m: ~25% (MEDIUM)
     * - Chair at 4m: ~10% (FAR)
     * 
     * @param boundingBox Normalized bounding box (0.0-1.0 coordinates)
     * @return Distance enum (CLOSE/MEDIUM/FAR)
     */
    private fun calculateDistance(boundingBox: BoundingBox): Distance {
        // Calculate box area as percentage of screen
        val boxWidth = boundingBox.xMax - boundingBox.xMin
        val boxHeight = boundingBox.yMax - boundingBox.yMin
        
        // Edge case: Invalid bounding box dimensions (corrupted TFLite output)
        if (boxWidth <= 0 || boxHeight <= 0) {
            android.util.Log.w("SpatialAnalyzer", "Invalid bounding box: w=$boxWidth, h=$boxHeight")
            return Distance.FAR // Default to far for invalid boxes
        }
        
        val boxArea = boxWidth * boxHeight  // Normalized area (0.0-1.0)
        
        return when {
            boxArea > Distance.CLOSE_THRESHOLD -> Distance.CLOSE
            boxArea > Distance.MEDIUM_THRESHOLD -> Distance.MEDIUM
            else -> Distance.FAR
        }
    }
    
    /**
     * Internal enum for horizontal position zones
     */
    private enum class HorizontalZone { LEFT, CENTER, RIGHT }
    
    /**
     * Internal enum for vertical position zones
     */
    private enum class VerticalZone { TOP, CENTER, BOTTOM }
}
