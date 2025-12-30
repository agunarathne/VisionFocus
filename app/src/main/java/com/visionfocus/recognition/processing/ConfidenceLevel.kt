package com.visionfocus.recognition.processing

/**
 * Confidence level categorization for detection results
 * 
 * Story 2.2: Confidence-aware TTS phrasing system
 * Maps raw confidence scores to human-understandable categories
 * 
 * HIGH: "I see a chair with high confidence"
 * MEDIUM: "Possibly a bottle"
 * LOW: "Not sure, possibly a cup"
 */
enum class ConfidenceLevel {
    /**
     * High confidence: ≥0.85
     * Phrasing: Assertive ("I see a {object}")
     */
    HIGH,
    
    /**
     * Medium confidence: 0.7-0.84
     * Phrasing: Qualified ("Possibly a {object}")
     */
    MEDIUM,
    
    /**
     * Low confidence: 0.6-0.69
     * Phrasing: Uncertain ("Not sure, possibly a {object}")
     */
    LOW
}
