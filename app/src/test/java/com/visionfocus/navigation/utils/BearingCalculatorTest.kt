package com.visionfocus.navigation.utils

import com.visionfocus.navigation.models.LatLng
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Story 6.3 Task 8.5: Unit tests for BearingCalculator (HIGH Issue #10).
 * 
 * Tests bearing calculations (0-360°) and cardinal direction mapping
 * for navigation announcements (e.g., "heading north", "turn south").
 */
class BearingCalculatorTest {
    
    private val bearingCalculator = BearingCalculator()
    
    // Times Square as reference point
    private val timesSquare = LatLng(40.758896, -73.985130)
    
    @Test
    fun `calculateBearing - due north is 0 degrees`() {
        // Arrange: Point directly north of Times Square
        val northPoint = LatLng(40.768896, -73.985130)  // +0.01° latitude
        
        // Act
        val bearing = bearingCalculator.calculateBearing(timesSquare, northPoint)
        
        // Assert: Should be close to 0° (allowing small floating-point error)
        assertTrue("Bearing to north should be ~0°", abs(bearing) < 5f || bearing > 355f)
    }
    
    @Test
    fun `calculateBearing - due east is 90 degrees`() {
        // Arrange: Point directly east of Times Square
        val eastPoint = LatLng(40.758896, -73.975130)  // +0.01° longitude
        
        // Act
        val bearing = bearingCalculator.calculateBearing(timesSquare, eastPoint)
        
        // Assert: Should be close to 90°
        assertTrue("Bearing to east should be ~90°", bearing in 85f..95f)
    }
    
    @Test
    fun `calculateBearing - due south is 180 degrees`() {
        // Arrange: Point directly south of Times Square
        val southPoint = LatLng(40.748896, -73.985130)  // -0.01° latitude
        
        // Act
        val bearing = bearingCalculator.calculateBearing(timesSquare, southPoint)
        
        // Assert: Should be close to 180°
        assertTrue("Bearing to south should be ~180°", bearing in 175f..185f)
    }
    
    @Test
    fun `calculateBearing - due west is 270 degrees`() {
        // Arrange: Point directly west of Times Square
        val westPoint = LatLng(40.758896, -73.995130)  // -0.01° longitude
        
        // Act
        val bearing = bearingCalculator.calculateBearing(timesSquare, westPoint)
        
        // Assert: Should be close to 270°
        assertTrue("Bearing to west should be ~270°", bearing in 265f..275f)
    }
    
    @Test
    fun `getCardinalDirection - maps 0 degrees to North`() {
        // Arrange: Bearing of 0° or 360°
        
        // Act & Assert
        assertEquals("N", bearingCalculator.getCardinalDirection(0f))
        assertEquals("N", bearingCalculator.getCardinalDirection(360f))
        assertEquals("N", bearingCalculator.getCardinalDirection(5f))  // Within tolerance
        assertEquals("N", bearingCalculator.getCardinalDirection(355f))  // Within tolerance
    }
    
    @Test
    fun `getCardinalDirection - maps 45 degrees to Northeast`() {
        // Arrange: Bearing in NE quadrant (22.5° - 67.5°)
        
        // Act & Assert
        assertEquals("NE", bearingCalculator.getCardinalDirection(30f))
        assertEquals("NE", bearingCalculator.getCardinalDirection(45f))
        assertEquals("NE", bearingCalculator.getCardinalDirection(60f))
    }
    
    @Test
    fun `getCardinalDirection - maps 90 degrees to East`() {
        // Arrange: Bearing in E quadrant (67.5° - 112.5°)
        
        // Act & Assert
        assertEquals("E", bearingCalculator.getCardinalDirection(75f))
        assertEquals("E", bearingCalculator.getCardinalDirection(90f))
        assertEquals("E", bearingCalculator.getCardinalDirection(105f))
    }
    
    @Test
    fun `getCardinalDirection - maps 135 degrees to Southeast`() {
        // Arrange: Bearing in SE quadrant (112.5° - 157.5°)
        
        // Act & Assert
        assertEquals("SE", bearingCalculator.getCardinalDirection(120f))
        assertEquals("SE", bearingCalculator.getCardinalDirection(135f))
        assertEquals("SE", bearingCalculator.getCardinalDirection(150f))
    }
    
    @Test
    fun `getCardinalDirection - maps 180 degrees to South`() {
        // Arrange: Bearing in S quadrant (157.5° - 202.5°)
        
        // Act & Assert
        assertEquals("S", bearingCalculator.getCardinalDirection(165f))
        assertEquals("S", bearingCalculator.getCardinalDirection(180f))
        assertEquals("S", bearingCalculator.getCardinalDirection(195f))
    }
    
    @Test
    fun `getCardinalDirection - maps 225 degrees to Southwest`() {
        // Arrange: Bearing in SW quadrant (202.5° - 247.5°)
        
        // Act & Assert
        assertEquals("SW", bearingCalculator.getCardinalDirection(210f))
        assertEquals("SW", bearingCalculator.getCardinalDirection(225f))
        assertEquals("SW", bearingCalculator.getCardinalDirection(240f))
    }
    
    @Test
    fun `getCardinalDirection - maps 270 degrees to West`() {
        // Arrange: Bearing in W quadrant (247.5° - 292.5°)
        
        // Act & Assert
        assertEquals("W", bearingCalculator.getCardinalDirection(255f))
        assertEquals("W", bearingCalculator.getCardinalDirection(270f))
        assertEquals("W", bearingCalculator.getCardinalDirection(285f))
    }
    
    @Test
    fun `getCardinalDirection - maps 315 degrees to Northwest`() {
        // Arrange: Bearing in NW quadrant (292.5° - 337.5°)
        
        // Act & Assert
        assertEquals("NW", bearingCalculator.getCardinalDirection(300f))
        assertEquals("NW", bearingCalculator.getCardinalDirection(315f))
        assertEquals("NW", bearingCalculator.getCardinalDirection(330f))
    }
    
    @Test
    fun `getCardinalDirection - handles edge cases at boundaries`() {
        // Arrange: Boundary values between quadrants
        
        // Act & Assert: Test boundaries (±2.5° tolerance)
        val boundary1 = bearingCalculator.getCardinalDirection(22.5f)  // N/NE boundary
        assertTrue("22.5° should be N or NE", boundary1 == "N" || boundary1 == "NE")
        
        val boundary2 = bearingCalculator.getCardinalDirection(67.5f)  // NE/E boundary
        assertTrue("67.5° should be NE or E", boundary2 == "NE" || boundary2 == "E")
        
        val boundary3 = bearingCalculator.getCardinalDirection(112.5f)  // E/SE boundary
        assertTrue("112.5° should be E or SE", boundary3 == "E" || boundary3 == "SE")
    }
    
    @Test
    fun `calculateBearing - returns value in 0-360 range`() {
        // Arrange: Various test points around Times Square
        val testPoints = listOf(
            LatLng(40.768896, -73.985130),  // North
            LatLng(40.758896, -73.975130),  // East
            LatLng(40.748896, -73.985130),  // South
            LatLng(40.758896, -73.995130),  // West
            LatLng(40.763896, -73.980130),  // Northeast
            LatLng(40.753896, -73.980130),  // Southeast
            LatLng(40.753896, -73.990130),  // Southwest
            LatLng(40.763896, -73.990130)   // Northwest
        )
        
        // Act & Assert: All bearings should be in valid range
        testPoints.forEach { point ->
            val bearing = bearingCalculator.calculateBearing(timesSquare, point)
            assertTrue("Bearing $bearing should be 0-360°", bearing in 0f..360f)
        }
    }
    
    @Test
    fun `getBearingDescription - formats natural language announcement`() {
        // Arrange: Various bearings
        val testCases = mapOf(
            0f to "heading north",
            45f to "heading northeast",
            90f to "heading east",
            135f to "heading southeast",
            180f to "heading south",
            225f to "heading southwest",
            270f to "heading west",
            315f to "heading northwest"
        )
        
        // Act & Assert
        testCases.forEach { (bearing, expectedPhrase) ->
            val description = bearingCalculator.getBearingDescription(bearing)
            assertTrue("Bearing $bearing should describe as '$expectedPhrase'",
                description.contains(expectedPhrase, ignoreCase = true))
        }
    }
    
    @Test
    fun `integration - full bearing calculation to announcement`() {
        // Arrange: Route from Times Square to Central Park (heading north)
        val centralPark = LatLng(40.785091, -73.968285)
        
        // Act
        val bearing = bearingCalculator.calculateBearing(timesSquare, centralPark)
        val cardinal = bearingCalculator.getCardinalDirection(bearing)
        val description = bearingCalculator.getBearingDescription(bearing)
        
        // Assert: Should indicate northerly direction
        assertTrue("Bearing should be northerly", bearing < 45f || bearing > 315f)
        assertTrue("Cardinal should be N or NE", cardinal == "N" || cardinal == "NE")
        assertTrue("Description should mention north", 
            description.contains("north", ignoreCase = true))
    }
}
