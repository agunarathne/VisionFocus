package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConfidenceFilter
 * Story 2.2 Task 1.6: Add unit tests for confidence filtering edge cases
 */
class ConfidenceFilterTest {
    
    private lateinit var filter: ConfidenceFilter
    
    @Before
    fun setup() {
        filter = ConfidenceFilter()
    }
    
    // Test confidence threshold filtering
    
    @Test
    fun `filter removes detections below 0_6 threshold`() {
        val detections = listOf(
            DetectionResult("chair", 0.85f, mockBoundingBox()),
            DetectionResult("table", 0.45f, mockBoundingBox()),  // Below threshold
            DetectionResult("person", 0.72f, mockBoundingBox()),
            DetectionResult("cup", 0.55f, mockBoundingBox())     // Below threshold
        )
        
        val filtered = filter.filter(detections)
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.confidence >= 0.6f })
        assertFalse(filtered.any { it.label == "table" })
        assertFalse(filtered.any { it.label == "cup" })
    }
    
    @Test
    fun `filter keeps all detections when all above threshold`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, mockBoundingBox()),
            DetectionResult("table", 0.78f, mockBoundingBox()),
            DetectionResult("person", 0.64f, mockBoundingBox())  // Boundary: exactly 0.6+
        )
        
        val filtered = filter.filter(detections)
        
        assertEquals(3, filtered.size)
    }
    
    @Test
    fun `filter returns empty list when all below threshold`() {
        val detections = listOf(
            DetectionResult("chair", 0.45f, mockBoundingBox()),
            DetectionResult("table", 0.32f, mockBoundingBox()),
            DetectionResult("person", 0.58f, mockBoundingBox())
        )
        
        val filtered = filter.filter(detections)
        
        assertTrue(filtered.isEmpty())
    }
    
    @Test
    fun `filter handles empty input list`() {
        val filtered = filter.filter(emptyList())
        
        assertTrue(filtered.isEmpty())
    }
    
    @Test
    fun `filter boundary value exactly 0_6 is kept`() {
        val detections = listOf(
            DetectionResult("chair", 0.60f, mockBoundingBox())  // Exact boundary
        )
        
        val filtered = filter.filter(detections)
        
        assertEquals(1, filtered.size)
        assertEquals(0.60f, filtered[0].confidence, 0.001f)
    }
    
    @Test
    fun `filter boundary value just below 0_6 is removed`() {
        val detections = listOf(
            DetectionResult("chair", 0.5999f, mockBoundingBox())  // Just below boundary
        )
        
        val filtered = filter.filter(detections)
        
        assertTrue(filtered.isEmpty())
    }
    
    // Test confidence level categorization
    
    @Test
    fun `categorizeConfidence HIGH for confidence gte 0_85`() {
        assertEquals(ConfidenceLevel.HIGH, filter.categorizeConfidence(0.92f))
        assertEquals(ConfidenceLevel.HIGH, filter.categorizeConfidence(0.85f))  // Boundary
        assertEquals(ConfidenceLevel.HIGH, filter.categorizeConfidence(1.0f))   // Max
    }
    
    @Test
    fun `categorizeConfidence MEDIUM for confidence 0_7 to 0_84`() {
        assertEquals(ConfidenceLevel.MEDIUM, filter.categorizeConfidence(0.78f))
        assertEquals(ConfidenceLevel.MEDIUM, filter.categorizeConfidence(0.70f))  // Lower boundary
        assertEquals(ConfidenceLevel.MEDIUM, filter.categorizeConfidence(0.84f))  // Upper boundary
    }
    
    @Test
    fun `categorizeConfidence LOW for confidence 0_6 to 0_69`() {
        assertEquals(ConfidenceLevel.LOW, filter.categorizeConfidence(0.64f))
        assertEquals(ConfidenceLevel.LOW, filter.categorizeConfidence(0.60f))  // Lower boundary
        assertEquals(ConfidenceLevel.LOW, filter.categorizeConfidence(0.69f))  // Upper boundary
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `categorizeConfidence throws exception for confidence below 0_6`() {
        filter.categorizeConfidence(0.55f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `categorizeConfidence throws exception for very low confidence`() {
        filter.categorizeConfidence(0.25f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `categorizeConfidence throws exception for confidence above 1_0`() {
        filter.categorizeConfidence(1.5f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `categorizeConfidence throws exception for invalid confidence value`() {
        filter.categorizeConfidence(5.0f)
    }
    
    // Test toFilteredDetection conversion
    
    @Test
    fun `toFilteredDetection creates FilteredDetection with correct confidence level`() {
        val highConfidenceDetection = DetectionResult("chair", 0.92f, mockBoundingBox())
        val filtered = filter.toFilteredDetection(highConfidenceDetection)
        
        assertEquals("chair", filtered.label)
        assertEquals(0.92f, filtered.confidence, 0.001f)
        assertEquals(ConfidenceLevel.HIGH, filtered.confidenceLevel)
        assertEquals(highConfidenceDetection.boundingBox, filtered.boundingBox)
    }
    
    @Test
    fun `toFilteredDetection handles medium confidence correctly`() {
        val mediumDetection = DetectionResult("bottle", 0.75f, mockBoundingBox())
        val filtered = filter.toFilteredDetection(mediumDetection)
        
        assertEquals(ConfidenceLevel.MEDIUM, filtered.confidenceLevel)
    }
    
    @Test
    fun `toFilteredDetection handles low confidence correctly`() {
        val lowDetection = DetectionResult("cup", 0.62f, mockBoundingBox())
        val filtered = filter.toFilteredDetection(lowDetection)
        
        assertEquals(ConfidenceLevel.LOW, filtered.confidenceLevel)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `toFilteredDetection throws exception for detection below threshold`() {
        val belowThreshold = DetectionResult("chair", 0.50f, mockBoundingBox())
        filter.toFilteredDetection(belowThreshold)
    }
    
    // Helper functions
    
    private fun mockBoundingBox() = BoundingBox(
        yMin = 0.1f,
        xMin = 0.1f,
        yMax = 0.5f,
        xMax = 0.5f
    )
}
