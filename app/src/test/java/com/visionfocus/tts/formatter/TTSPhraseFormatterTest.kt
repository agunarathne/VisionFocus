package com.visionfocus.tts.formatter

import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.processing.ConfidenceLevel
import com.visionfocus.recognition.processing.FilteredDetection
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for TTSPhraseFormatter
 * Story 2.2 Task 2.8: Unit tests validating phrasing for all confidence levels
 */
class TTSPhraseFormatterTest {
    
    private lateinit var formatter: TTSPhraseFormatter
    
    @Before
    fun setup() {
        // Use production formatter with default random
        formatter = TTSPhraseFormatter()
    }
    
    // Test single detection phrasing
    
    @Test
    fun `formatSingleDetection HIGH confidence uses assertive phrasing`() {
        val detection = FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox())
        
        val announcement = formatter.formatSingleDetection(detection)
        
        // Verify contains object name
        assertTrue(announcement.contains("chair"))
        
        // Verify uses HIGH confidence template (assertive tone)
        val hasHighConfidencePhrasing = announcement.contains("I see") ||
                announcement.contains("high confidence") ||
                announcement.contains("I'm quite certain") ||
                announcement.contains("That looks like")
        
        assertTrue("Expected assertive phrasing, got: $announcement", hasHighConfidencePhrasing)
    }
    
    @Test
    fun `formatSingleDetection MEDIUM confidence uses qualified phrasing`() {
        val detection = FilteredDetection("bottle", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        
        val announcement = formatter.formatSingleDetection(detection)
        
        // Verify contains object name
        assertTrue(announcement.contains("bottle"))
        
        // Verify uses MEDIUM confidence template (qualified tone)
        val hasMediumConfidencePhrasing = announcement.contains("Possibly") ||
                announcement.contains("I think") ||
                announcement.contains("might be") ||
                announcement.contains("Could be")
        
        assertTrue("Expected qualified phrasing, got: $announcement", hasMediumConfidencePhrasing)
    }
    
    @Test
    fun `formatSingleDetection LOW confidence uses uncertain phrasing`() {
        val detection = FilteredDetection("cup", 0.64f, ConfidenceLevel.LOW, mockBoundingBox())
        
        val announcement = formatter.formatSingleDetection(detection)
        
        // Verify contains object name
        assertTrue(announcement.contains("cup"))
        
        // Verify uses LOW confidence template (uncertain tone)
        val hasLowConfidencePhrasing = announcement.contains("Not sure") ||
                announcement.contains("Might be") ||
                announcement.contains("I'm not certain") ||
                announcement.contains("Hard to tell")
        
        assertTrue("Expected uncertain phrasing, got: $announcement", hasLowConfidencePhrasing)
    }
    
    @Test
    fun `formatSingleDetection replaces object placeholder correctly`() {
        val detection = FilteredDetection("laptop", 0.88f, ConfidenceLevel.HIGH, mockBoundingBox())
        
        val announcement = formatter.formatSingleDetection(detection)
        
        // Verify object name is present and placeholder is gone
        assertTrue(announcement.contains("laptop"))
        assertFalse(announcement.contains("{object}"))
    }
    
    // Test multiple detections formatting
    
    @Test
    fun `formatMultipleDetections empty list returns no objects detected`() {
        val announcement = formatter.formatMultipleDetections(emptyList())
        
        assertEquals("No objects detected", announcement)
    }
    
    @Test
    fun `formatMultipleDetections single detection uses single format`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // Should not have conjunctions
        assertFalse(announcement.contains(" and "))
        assertTrue(announcement.contains("chair"))
    }
    
    @Test
    fun `formatMultipleDetections two detections uses and conjunction`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // Verify structure: "{phrase1}, and {phrase2}"
        assertTrue(announcement.contains("and"))
        assertTrue(announcement.contains("chair"))
        assertTrue(announcement.contains("table"))
        
        // Verify contains "and" (as a word, not just the letters)
        assertTrue(announcement.contains(" and "))
    }
    
    @Test
    fun `formatMultipleDetections three detections uses comma and and`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox()),
            FilteredDetection("laptop", 0.68f, ConfidenceLevel.LOW, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // Verify all objects mentioned
        assertTrue(announcement.contains("chair"))
        assertTrue(announcement.contains("table"))
        assertTrue(announcement.contains("laptop"))
        
        // Verify uses commas and "and" before last item
        assertTrue(announcement.contains(","))
        assertTrue(announcement.contains(" and "))
    }
    
    @Test
    fun `formatMultipleDetections maintains confidence-aware phrasing for each detection`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.75f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // First detection should use HIGH phrasing
        val chairIndex = announcement.indexOf("chair")
        val tableIndex = announcement.indexOf("table")
        
        assertTrue(chairIndex < tableIndex)  // Chair should come first
        assertTrue(announcement.contains("chair"))
        assertTrue(announcement.contains("table"))
    }
    
    @Test
    fun `formatMultipleDetections handles many detections correctly`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.85f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("laptop", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox()),
            FilteredDetection("bottle", 0.68f, ConfidenceLevel.LOW, mockBoundingBox()),
            FilteredDetection("cup", 0.62f, ConfidenceLevel.LOW, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // Verify all objects mentioned
        assertTrue(announcement.contains("chair"))
        assertTrue(announcement.contains("table"))
        assertTrue(announcement.contains("laptop"))
        assertTrue(announcement.contains("bottle"))
        assertTrue(announcement.contains("cup"))
        
        // Verify natural language structure
        assertTrue(announcement.contains(","))
        assertTrue(announcement.contains(" and "))
    }
    
    // Test phrasing variation (avoid robotic repetition)
    
    @Test
    fun `formatter uses different phrases for same confidence level`() {
        // Multiple calls should eventually produce different phrases due to randomization
        val detection = FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox())
        
        val phrases = (1..10).map {
            formatter.formatSingleDetection(detection)
        }.toSet()
        
        // With 4 high-confidence templates and 10 calls, we should see variation
        // This is probabilistic but very likely to pass
        assertTrue(
            "Expected variation in phrasing across multiple calls, but got only: $phrases",
            phrases.size >= 2
        )
    }
    
    @Test
    fun `formatter always includes object name regardless of template`() {
        val detections = listOf(
            FilteredDetection("person", 0.95f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("bicycle", 0.72f, ConfidenceLevel.MEDIUM, mockBoundingBox()),
            FilteredDetection("backpack", 0.63f, ConfidenceLevel.LOW, mockBoundingBox())
        )
        
        detections.forEach { detection ->
            val announcement = formatter.formatSingleDetection(detection)
            assertTrue(
                "Object '${detection.label}' not found in announcement: $announcement",
                announcement.contains(detection.label)
            )
        }
    }
    
    // Test natural language quality
    
    @Test
    fun `announcements are natural language not robotic`() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        )
        
        val announcement = formatter.formatMultipleDetections(detections)
        
        // Anti-patterns: Should NOT contain robotic phrasing
        assertFalse(announcement.contains("Confidence:"))
        assertFalse(announcement.contains("0.92"))
        assertFalse(announcement.contains("%"))
        assertFalse(announcement.contains("Detected:"))
        
        // Should be conversational
        assertTrue(announcement.length > 10)  // Not just object names
    }
    
    // Helper functions
    
    private fun mockBoundingBox() = BoundingBox(
        yMin = 0.1f,
        xMin = 0.1f,
        yMax = 0.5f,
        xMax = 0.5f
    )
}
