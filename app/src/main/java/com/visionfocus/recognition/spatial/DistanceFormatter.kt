package com.visionfocus.recognition.spatial

/**
 * Formatter for converting Distance enum to natural language phrases
 * 
 * Story 4.5 AC: Spatial announcements use natural language
 * 
 * Examples:
 * - Distance.CLOSE → "close by"
 * - Distance.MEDIUM → "at medium distance"
 * - Distance.FAR → "far away"
 * 
 * Provides option for prepositional phrases to improve sentence flow
 */
object DistanceFormatter {
    
    /**
     * Convert Distance enum to natural, TalkBack-friendly phrase
     * 
     * Story 4.5 AC: Natural language phrasing
     * 
     * @param distance Distance enum from spatial analysis
     * @param withPreposition If true, includes prepositional phrases ("close by", "at medium distance")
     *                        If false, uses shorter forms ("close", "medium distance")
     * @return Natural language distance phrase
     */
    fun toNaturalLanguage(distance: Distance, withPreposition: Boolean = false): String {
        return when (distance) {
            Distance.CLOSE -> if (withPreposition) "close by" else "close"
            Distance.MEDIUM -> if (withPreposition) "at medium distance" else "medium distance"
            Distance.FAR -> if (withPreposition) "far away" else "far"
        }
    }
}
