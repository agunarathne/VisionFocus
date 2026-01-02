package com.visionfocus.recognition.spatial

import com.visionfocus.recognition.models.BoundingBox
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for SpatialAnalyzer
 * 
 * Story 4.5: Tests position and distance calculations
 * - Position calculation for all 9 screen zones
 * - Distance estimation for CLOSE/MEDIUM/FAR thresholds
 * - Edge cases: boundary jitter, full-screen box, tiny box
 * - Orientation support: portrait and landscape
 */
class SpatialAnalyzerTest {
    
    private lateinit var analyzer: SpatialAnalyzer
    private val portraitScreen = Size(1080, 1920)  // Portrait HD
    private val landscapeScreen = Size(1920, 1080)  // Landscape HD
    
    @Before
    fun setup() {
        analyzer = SpatialAnalyzer()
    }
    
    // ========== Position Tests - 9 Screen Zones ==========
    
    @Test
    fun `position calculation - top left corner`() {
        val boundingBox = BoundingBox(
            yMin = 0.05f,
            xMin = 0.05f,
            yMax = 0.25f,
            xMax = 0.25f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.TOP_LEFT, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - top center`() {
        val boundingBox = BoundingBox(
            yMin = 0.05f,
            xMin = 0.4f,
            yMax = 0.25f,
            xMax = 0.6f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.TOP_CENTER, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - top right`() {
        val boundingBox = BoundingBox(
            yMin = 0.05f,
            xMin = 0.75f,
            yMax = 0.25f,
            xMax = 0.95f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.TOP_RIGHT, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - center left`() {
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.05f,
            yMax = 0.6f,
            xMax = 0.25f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.CENTER_LEFT, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - center center`() {
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.4f,
            yMax = 0.6f,
            xMax = 0.6f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.CENTER_CENTER, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - center right`() {
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.75f,
            yMax = 0.6f,
            xMax = 0.95f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.CENTER_RIGHT, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - bottom left`() {
        val boundingBox = BoundingBox(
            yMin = 0.75f,
            xMin = 0.05f,
            yMax = 0.95f,
            xMax = 0.25f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.BOTTOM_LEFT, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - bottom center`() {
        val boundingBox = BoundingBox(
            yMin = 0.75f,
            xMin = 0.4f,
            yMax = 0.95f,
            xMax = 0.6f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.BOTTOM_CENTER, spatialInfo.position)
    }
    
    @Test
    fun `position calculation - bottom right`() {
        val boundingBox = BoundingBox(
            yMin = 0.75f,
            xMin = 0.75f,
            yMax = 0.95f,
            xMax = 0.95f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.BOTTOM_RIGHT, spatialInfo.position)
    }
    
    // ========== Distance Tests ==========
    
    @Test
    fun `distance calculation - close object (greater than 40 percent)`() {
        // Box area: 0.8 * 0.8 = 0.64 (64% of screen)
        val boundingBox = BoundingBox(
            yMin = 0.1f,
            xMin = 0.1f,
            yMax = 0.9f,
            xMax = 0.9f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Distance.CLOSE, spatialInfo.distance)
    }
    
    @Test
    fun `distance calculation - close object at threshold (exactly 40 percent)`() {
        // Box area: sqrt(0.4) * sqrt(0.4) ≈ 0.632 * 0.632 ≈ 0.40 (40% of screen)
        val side = kotlin.math.sqrt(0.40).toFloat()
        val boundingBox = BoundingBox(
            yMin = 0.3f,
            xMin = 0.3f,
            yMax = 0.3f + side,
            xMax = 0.3f + side
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        // Exactly at threshold should be CLOSE (>= check)
        assertEquals(Distance.CLOSE, spatialInfo.distance)
    }
    
    @Test
    fun `distance calculation - medium object (20-40 percent)`() {
        // Box area: 0.5 * 0.5 = 0.25 (25% of screen)
        val boundingBox = BoundingBox(
            yMin = 0.25f,
            xMin = 0.25f,
            yMax = 0.75f,
            xMax = 0.75f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Distance.MEDIUM, spatialInfo.distance)
    }
    
    @Test
    fun `distance calculation - medium object at lower threshold (exactly 20 percent)`() {
        // Box area: sqrt(0.2) * sqrt(0.2) ≈ 0.447 * 0.447 ≈ 0.20 (20% of screen)
        val side = kotlin.math.sqrt(0.20).toFloat()
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.4f,
            yMax = 0.4f + side,
            xMax = 0.4f + side
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        // Exactly at threshold should be MEDIUM (>= check)
        assertEquals(Distance.MEDIUM, spatialInfo.distance)
    }
    
    @Test
    fun `distance calculation - far object (less than 20 percent)`() {
        // Box area: 0.3 * 0.3 = 0.09 (9% of screen)
        val boundingBox = BoundingBox(
            yMin = 0.35f,
            xMin = 0.35f,
            yMax = 0.65f,
            xMax = 0.65f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Distance.FAR, spatialInfo.distance)
    }
    
    @Test
    fun `distance calculation - tiny object (less than 5 percent)`() {
        // Box area: 0.15 * 0.15 = 0.0225 (2.25% of screen)
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.4f,
            yMax = 0.55f,
            xMax = 0.55f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Distance.FAR, spatialInfo.distance)
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    fun `boundary tolerance - prevents jitter at left-center boundary`() {
        // Three boxes near the LEFT/CENTER boundary (x=0.33)
        // Due to ±5% tolerance, all should classify to same zone
        val boundingBox1 = BoundingBox(yMin = 0.4f, xMin = 0.29f, yMax = 0.6f, xMax = 0.31f)  // centerX = 0.30
        val boundingBox2 = BoundingBox(yMin = 0.4f, xMin = 0.32f, yMax = 0.6f, xMax = 0.34f)  // centerX = 0.33
        val boundingBox3 = BoundingBox(yMin = 0.4f, xMin = 0.35f, yMax = 0.6f, xMax = 0.37f)  // centerX = 0.36
        
        val result1 = analyzer.analyze(boundingBox1, portraitScreen)
        val result2 = analyzer.analyze(boundingBox2, portraitScreen)
        val result3 = analyzer.analyze(boundingBox3, portraitScreen)
        
        // All should be in CENTER due to tolerance
        assertEquals(result1.position, result2.position)
        assertEquals(result2.position, result3.position)
    }
    
    @Test
    fun `full-screen bounding box detected as close distance`() {
        // Box covering entire screen (100% area)
        val boundingBox = BoundingBox(
            yMin = 0.0f,
            xMin = 0.0f,
            yMax = 1.0f,
            xMax = 1.0f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Distance.CLOSE, spatialInfo.distance)
        assertEquals(Position.CENTER_CENTER, spatialInfo.position)
    }
    
    @Test
    fun `position calculation handles extreme corner boxes`() {
        // Box in very top-left corner (edge case)
        val boundingBox = BoundingBox(
            yMin = 0.0f,
            xMin = 0.0f,
            yMax = 0.1f,
            xMax = 0.1f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        
        assertEquals(Position.TOP_LEFT, spatialInfo.position)
    }
    
    // ========== Orientation Tests ==========
    
    @Test
    fun `position calculation works in landscape orientation`() {
        // Box on left side in landscape
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.1f,
            yMax = 0.6f,
            xMax = 0.25f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, landscapeScreen)
        
        assertTrue(spatialInfo.position.isLeft())
        assertTrue(spatialInfo.screenSize.isLandscape)
    }
    
    @Test
    fun `distance calculation consistent across orientations`() {
        // Same box area percentage in portrait and landscape
        val portraitBox = BoundingBox(
            yMin = 0.3f,
            xMin = 0.3f,
            yMax = 0.7f,
            xMax = 0.7f
        )
        val landscapeBox = BoundingBox(
            yMin = 0.3f,
            xMin = 0.3f,
            yMax = 0.7f,
            xMax = 0.7f
        )
        
        val portraitResult = analyzer.analyze(portraitBox, portraitScreen)
        val landscapeResult = analyzer.analyze(landscapeBox, landscapeScreen)
        
        // Same relative box size should give same distance classification
        assertEquals(portraitResult.distance, landscapeResult.distance)
    }
    
    // ========== Natural Language Integration Tests ==========
    
    @Test
    fun `spatial info generates natural language description`() {
        val boundingBox = BoundingBox(
            yMin = 0.4f,
            xMin = 0.1f,
            yMax = 0.6f,
            xMax = 0.3f
        )
        
        val spatialInfo = analyzer.analyze(boundingBox, portraitScreen)
        val description = spatialInfo.toNaturalLanguage()
        
        // Should contain both distance and position in natural language
        assertTrue(description.isNotEmpty())
        assertTrue(!description.contains("X:") && !description.contains("Y:"))  // No robotic phrasing
    }
}
