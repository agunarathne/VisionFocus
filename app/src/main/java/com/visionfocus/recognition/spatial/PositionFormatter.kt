package com.visionfocus.recognition.spatial

/**
 * Formatter for converting Position enum to natural language phrases
 * 
 * Story 4.5 AC: Spatial announcements use natural language (avoid robotic phrasing)
 * 
 * Examples:
 * - Position.CENTER_CENTER → "in center of view"
 * - Position.LEFT → "on the left"
 * - Position.TOP_RIGHT → "on the right side, near the top"
 * 
 * Avoids robotic phrases like "X: 150 pixels, Y: 200 pixels"
 * Phrases are concise and TalkBack-friendly
 */
object PositionFormatter {
    
    /**
     * Convert Position enum to natural, TalkBack-friendly phrase
     * 
     * Story 4.5 AC: Natural language phrasing
     * 
     * Prioritizes horizontal over vertical in announcements:
     * - "on the left side, near the top" (not "top-left")
     * 
     * @param position Position enum from spatial analysis
     * @return Natural language position phrase
     */
    fun toNaturalLanguage(position: Position): String {
        return when (position) {
            // Center vertical row (horizontal position is primary)
            Position.CENTER_LEFT -> "on the left"
            Position.CENTER_CENTER -> "in center of view"
            Position.CENTER_RIGHT -> "on the right"
            
            // Top positions
            Position.TOP_LEFT -> "on the left side, near the top"
            Position.TOP_CENTER -> "in the center, near the top"
            Position.TOP_RIGHT -> "on the right side, near the top"
            
            // Bottom positions
            Position.BOTTOM_LEFT -> "on the left side, near the bottom"
            Position.BOTTOM_CENTER -> "in the center, near the bottom"
            Position.BOTTOM_RIGHT -> "on the right side, near the bottom"
        }
    }
}
