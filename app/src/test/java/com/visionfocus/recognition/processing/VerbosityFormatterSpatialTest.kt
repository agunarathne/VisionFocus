package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import com.visionfocus.recognition.spatial.Distance
import com.visionfocus.recognition.spatial.Position
import com.visionfocus.recognition.spatial.Size
import com.visionfocus.recognition.spatial.SpatialInfo
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for VerbosityFormatter with spatial information
 * 
 * Story 4.5: Tests spatial announcements in detailed mode
 * - Spatial info included in detailed mode
 * - Brief and standard modes do NOT include spatial info
 * - Multi-object announcements sorted by distance then confidence
 * - Natural language phrasing (no robotic coordinates)
 */
class VerbosityFormatterSpatialTest {
    
    private val formatter = VerbosityFormatter()
    private val mockScreenSize = Size(1080, 1920)
    
    @Test
    fun `detailed mode - single object with spatial info`() {
        val detection = DetectionResult(
            label = "chair",
            confidence = 0.95f,
            boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
            spatialInfo = SpatialInfo(
                position = Position.CENTER_CENTER,
                distance = Distance.CLOSE,
                boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
                screenSize = mockScreenSize
            )
        )
        
        val announcement = formatter.format(listOf(detection), VerbosityMode.DETAILED)
        
        assertTrue(announcement.contains("chair"), "Should contain object label")
        assertTrue(announcement.contains("close by"), "Should contain distance")
        assertTrue(announcement.contains("center"), "Should contain position")
        assertNoRoboticPhrasing(announcement)
    }
    
    @Test
    fun `detailed mode - single object without spatial info (backward compatibility)`() {
        val detection = DetectionResult(
            label = "table",
            confidence = 0.85f,
            boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
            spatialInfo = null  // No spatial info
        )
        
        val announcement = formatter.format(listOf(detection), VerbosityMode.DETAILED)
        
        // Should fall back to legacy position format
        assertTrue(announcement.contains("table"), "Should contain object label")
        assertFalse(announcement.contains("close by"), "Should not attempt to add spatial info when null")
    }
    
    @Test
    fun `detailed mode - multiple objects sorted by distance`() {
        val detections = listOf(
            DetectionResult(
                label = "cup",
                confidence = 0.90f,
                boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
                spatialInfo = SpatialInfo(
                    position = Position.CENTER_RIGHT,
                    distance = Distance.FAR,
                    boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
                    screenSize = mockScreenSize
                )
            ),
            DetectionResult(
                label = "phone",
                confidence = 0.85f,
                boundingBox = BoundingBox(0.1f, 0.1f, 0.9f, 0.9f),
                spatialInfo = SpatialInfo(
                    position = Position.CENTER_CENTER,
                    distance = Distance.CLOSE,
                    boundingBox = BoundingBox(0.1f, 0.1f, 0.9f, 0.9f),
                    screenSize = mockScreenSize
                )
            ),
            DetectionResult(
                label = "book",
                confidence = 0.95f,
                boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
                spatialInfo = SpatialInfo(
                    position = Position.CENTER_LEFT,
                    distance = Distance.MEDIUM,
                    boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
                    screenSize = mockScreenSize
                )
            )
        )
        
        val announcement = formatter.format(detections, VerbosityMode.DETAILED)
        
        // Should be sorted: CLOSE (phone) first, then MEDIUM (book), then FAR (cup)
        val phoneIndex = announcement.indexOf("phone")
        val bookIndex = announcement.indexOf("book")
        val cupIndex = announcement.indexOf("cup")
        
        assertTrue(phoneIndex < bookIndex, "CLOSE object should come before MEDIUM")
        assertTrue(bookIndex < cupIndex, "MEDIUM object should come before FAR")
        assertTrue(announcement.contains("I see"), "Should start with 'I see'")
        assertNoRoboticPhrasing(announcement)
    }
    
    @Test
    fun `detailed mode - objects with same distance sorted by confidence`() {
        val detections = listOf(
            DetectionResult(
                label = "bottle",
                confidence = 0.75f,  // Lower confidence
                boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
                spatialInfo = SpatialInfo(
                    position = Position.TOP_LEFT,
                    distance = Distance.MEDIUM,
                    boundingBox = BoundingBox(0.4f, 0.4f, 0.6f, 0.6f),
                    screenSize = mockScreenSize
                )
            ),
            DetectionResult(
                label = "mouse",
                confidence = 0.92f,  // Higher confidence
                boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
                spatialInfo = SpatialInfo(
                    position = Position.TOP_RIGHT,
                    distance = Distance.MEDIUM,  // Same distance
                    boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
                    screenSize = mockScreenSize
                )
            )
        )
        
        val announcement = formatter.format(detections, VerbosityMode.DETAILED)
        
        // Should be sorted by confidence when distance is equal
        val mouseIndex = announcement.indexOf("mouse")
        val bottleIndex = announcement.indexOf("bottle")
        
        assertTrue(mouseIndex < bottleIndex, "Higher confidence object should come first when distance is equal")
    }
    
    @Test
    fun `brief mode - does NOT include spatial info`() {
        val detection = DetectionResult(
            label = "laptop",
            confidence = 0.88f,
            boundingBox = BoundingBox(0.2f, 0.2f, 0.8f, 0.8f),
            spatialInfo = SpatialInfo(
                position = Position.CENTER_CENTER,
                distance = Distance.CLOSE,
                boundingBox = BoundingBox(0.2f, 0.2f, 0.8f, 0.8f),
                screenSize = mockScreenSize
            )
        )
        
        val announcement = formatter.format(listOf(detection), VerbosityMode.BRIEF)
        
        assertEquals("laptop", announcement)
        assertFalse(announcement.contains("close"), "Brief mode should not include distance")
        assertFalse(announcement.contains("center"), "Brief mode should not include position")
    }
    
    @Test
    fun `standard mode - does NOT include spatial info`() {
        val detection = DetectionResult(
            label = "keyboard",
            confidence = 0.91f,
            boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
            spatialInfo = SpatialInfo(
                position = Position.BOTTOM_CENTER,
                distance = Distance.MEDIUM,
                boundingBox = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f),
                screenSize = mockScreenSize
            )
        )
        
        val announcement = formatter.format(listOf(detection), VerbosityMode.STANDARD)
        
        assertTrue(announcement.contains("keyboard"), "Should contain object label")
        assertTrue(announcement.contains("91%"), "Standard mode shows confidence")
        assertFalse(announcement.contains("medium distance"), "Standard mode should not include distance")
        assertFalse(announcement.contains("bottom"), "Standard mode should not include position")
    }
    
    @Test
    fun `detailed mode - compound spatial announcement format`() {
        val detection = DetectionResult(
            label = "monitor",
            confidence = 0.87f,
            boundingBox = BoundingBox(0.1f, 0.75f, 0.3f, 0.95f),
            spatialInfo = SpatialInfo(
                position = Position.BOTTOM_LEFT,
                distance = Distance.FAR,
                boundingBox = BoundingBox(0.1f, 0.75f, 0.3f, 0.95f),
                screenSize = mockScreenSize
            )
        )
        
        val announcement = formatter.format(listOf(detection), VerbosityMode.DETAILED)
        
        // Should contain compound phrase like "far away on the left side, near the bottom"
        assertTrue(announcement.contains("monitor"), "Should contain object label")
        assertTrue(announcement.contains("far"), "Should contain distance")
        assertTrue(announcement.contains("left"), "Should contain horizontal position")
        assertTrue(announcement.contains("bottom"), "Should contain vertical position")
        assertNoRoboticPhrasing(announcement)
    }
    
    private fun assertNoRoboticPhrasing(announcement: String) {
        assertFalse(announcement.contains("X:"), "Should not contain X coordinates")
        assertFalse(announcement.contains("Y:"), "Should not contain Y coordinates")
        assertFalse(announcement.contains("px"), "Should not contain pixel units")
        assertFalse(announcement.contains("0."), "Should not contain decimal coordinates")
    }
}
