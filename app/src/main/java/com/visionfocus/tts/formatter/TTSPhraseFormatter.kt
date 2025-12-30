package com.visionfocus.tts.formatter

import com.visionfocus.recognition.processing.ConfidenceLevel
import com.visionfocus.recognition.processing.FilteredDetection
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Confidence-aware TTS phrasing formatter
 * 
 * Story 2.2 AC4, AC8: Natural language announcements with honest confidence levels
 * 
 * Strategy:
 * - HIGH: Assertive phrasing ("I see a chair")
 * - MEDIUM: Qualified phrasing ("Possibly a bottle")
 * - LOW: Uncertain phrasing ("Not sure, possibly a cup")
 * - Variation: Use .random() to avoid robotic repetition
 * 
 * Usage:
 * ```
 * val announcement = formatter.formatMultipleDetections(filteredResults)
 * ttsManager.announce(announcement)
 * ```
 */
@Singleton
class TTSPhraseFormatter @Inject constructor() {
    
    private val random: Random = Random.Default
    
    companion object {
        /**
         * High confidence phrasing templates (≥0.85)
         * Assertive tone: User can trust these detections
         */
        private val HIGH_CONFIDENCE_PHRASES = listOf(
            "I see a {object}",
            "{object} with high confidence",
            "I'm quite certain that's a {object}",
            "That looks like a {object}"
        )
        
        /**
         * Medium confidence phrasing templates (0.7-0.84)
         * Qualified tone: Likely but not certain
         */
        private val MEDIUM_CONFIDENCE_PHRASES = listOf(
            "Possibly a {object}",
            "I think that's a {object}",
            "Looks like it might be a {object}",
            "Could be a {object}"
        )
        
        /**
         * Low confidence phrasing templates (0.6-0.69)
         * Uncertain tone: Honest about uncertainty
         */
        private val LOW_CONFIDENCE_PHRASES = listOf(
            "Not sure, possibly a {object}",
            "Might be a {object}",
            "I'm not certain, but it could be a {object}",
            "Hard to tell, but possibly a {object}"
        )
        
        /**
         * Object placeholder in templates
         */
        private const val OBJECT_PLACEHOLDER = "{object}"
    }
    
    /**
     * Format single detection with confidence-aware phrasing
     * 
     * @param detection Filtered detection result
     * @return Natural language announcement string
     */
    fun formatSingleDetection(detection: FilteredDetection): String {
        val template = selectTemplate(detection.confidenceLevel)
        return template.replace(OBJECT_PLACEHOLDER, detection.label)
    }
    
    /**
     * Format multiple detections with natural language conjunctions
     * 
     * Story 2.2 AC7: Multiple detections announced in priority order (highest confidence first)
     * Story 2.2 AC8: Natural language (not robotic)
     * 
     * Examples:
     * - 1 detection: "I see a chair"
     * - 2 detections: "I see a chair, and possibly a table"
     * - 3+ detections: "I see a chair, possibly a table, and not sure, possibly a cup"
     * - 0 detections: "No objects detected"
     * 
     * @param detections Filtered detection results (should already be sorted by confidence)
     * @return Natural language announcement string
     */
    fun formatMultipleDetections(detections: List<FilteredDetection>): String {
        return when {
            detections.isEmpty() -> "No objects detected"
            detections.size == 1 -> formatSingleDetection(detections[0])
            else -> formatMultipleDetectionsWithConjunctions(detections)
        }
    }
    
    /**
     * Select random template from appropriate confidence level list
     * 
     * @param confidenceLevel HIGH/MEDIUM/LOW categorization
     * @return Random phrase template with {object} placeholder
     */
    private fun selectTemplate(confidenceLevel: ConfidenceLevel): String {
        val templates = when (confidenceLevel) {
            ConfidenceLevel.HIGH -> HIGH_CONFIDENCE_PHRASES
            ConfidenceLevel.MEDIUM -> MEDIUM_CONFIDENCE_PHRASES
            ConfidenceLevel.LOW -> LOW_CONFIDENCE_PHRASES
        }
        
        return templates[random.nextInt(templates.size)]
    }
    
    /**
     * Format multiple detections with natural language conjunctions
     * 
     * Uses "and" before last item, commas between items
     * 
     * @param detections Non-empty list of filtered detections
     * @return Formatted announcement with conjunctions
     */
    private fun formatMultipleDetectionsWithConjunctions(detections: List<FilteredDetection>): String {
        require(detections.size >= 2) { "formatMultipleDetectionsWithConjunctions requires at least 2 detections" }
        
        val phrases = detections.map { formatSingleDetection(it) }
        
        return when (phrases.size) {
            2 -> "${phrases[0]}, and ${phrases[1]}"
            else -> {
                val allButLast = phrases.dropLast(1).joinToString(", ")
                val last = phrases.last()
                "$allButLast, and $last"
            }
        }
    }
}
