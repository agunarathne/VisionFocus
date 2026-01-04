package com.visionfocus.navigation.utils

import com.visionfocus.navigation.models.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Story 6.3 Task 7: Unit tests for DistanceCalculator.
 * 
 * Tests Haversine formula accuracy using known GPS coordinates.
 * 
 * Test cases:
 * - Zero distance (same location)
 * - Short distance (~100m)
 * - Medium distance (~1km)
 * - Long distance (~100km)
 * - Cross-hemisphere distance
 */
class DistanceCalculatorTest {
    
    companion object {
        // Allow 1m tolerance for floating-point precision
        private const val TOLERANCE_METERS = 1.0f
        
        // Test locations (NYC area)
        private val TIMES_SQUARE = LatLng(40.758896, -73.985130)
        private val EMPIRE_STATE = LatLng(40.748817, -73.985428)
        private val CENTRAL_PARK = LatLng(40.785091, -73.968285)
    }
    
    @Test
    fun `calculate distance - same location returns zero`() {
        // Arrange
        val location = LatLng(40.7580, -73.9855)
        
        // Act
        val distance = DistanceCalculator.calculateDistance(location, location)
        
        // Assert
        assertEquals(0f, distance, TOLERANCE_METERS)
    }
    
    @Test
    fun `calculate distance - Times Square to Empire State Building approximately 1100m`() {
        // Arrange (known distance: ~1.1 km)
        // Expected: ~1100 meters
        
        // Act
        val distance = DistanceCalculator.calculateDistance(TIMES_SQUARE, EMPIRE_STATE)
        
        // Assert (allow 50m tolerance for GPS accuracy)
        assertTrue("Distance should be ~1100m, got $distance", 
            distance in 1050f..1150f)
    }
    
    @Test
    fun `calculate distance - Times Square to Central Park approximately 3000m`() {
        // Arrange (known distance: ~3 km)
        // Expected: ~3000 meters
        
        // Act
        val distance = DistanceCalculator.calculateDistance(TIMES_SQUARE, CENTRAL_PARK)
        
        // Assert (allow 100m tolerance)
        assertTrue("Distance should be ~3000m, got $distance", 
            distance in 2900f..3100f)
    }
    
    @Test
    fun `calculate distance - NYC to LA approximately 3940km`() {
        // Arrange
        val nyc = LatLng(40.7128, -74.0060)
        val la = LatLng(34.0522, -118.2437)
        
        // Expected: ~3940 km
        
        // Act
        val distanceMeters = DistanceCalculator.calculateDistance(nyc, la)
        val distanceKm = distanceMeters / 1000f
        
        // Assert (allow 50km tolerance for long distances)
        assertTrue("Distance should be ~3940km, got ${distanceKm}km", 
            distanceKm in 3890f..3990f)
    }
    
    @Test
    fun `calculate distance - cross hemisphere works correctly`() {
        // Arrange
        val newYork = LatLng(40.7128, -74.0060)  // Northern hemisphere
        val sydney = LatLng(-33.8688, 151.2093)  // Southern hemisphere
        
        // Expected: ~16000 km
        
        // Act
        val distanceMeters = DistanceCalculator.calculateDistance(newYork, sydney)
        val distanceKm = distanceMeters / 1000f
        
        // Assert (allow 100km tolerance for very long distances)
        assertTrue("Distance should be ~16000km, got ${distanceKm}km", 
            distanceKm in 15900f..16100f)
    }
    
    @Test
    fun `calculate distance - symmetric (A to B equals B to A)`() {
        // Arrange
        val pointA = LatLng(40.7580, -73.9855)
        val pointB = LatLng(40.7488, -73.9854)
        
        // Act
        val distanceAB = DistanceCalculator.calculateDistance(pointA, pointB)
        val distanceBA = DistanceCalculator.calculateDistance(pointB, pointA)
        
        // Assert
        assertEquals(distanceAB, distanceBA, TOLERANCE_METERS)
    }
    
    @Test
    fun `calculate distance - short distance 100m accuracy`() {
        // Arrange: Two points ~100m apart
        val start = LatLng(40.7580, -73.9855)
        val end = LatLng(40.7589, -73.9855)  // ~100m north
        
        // Act
        val distance = DistanceCalculator.calculateDistance(start, end)
        
        // Assert
        assertTrue("Distance should be ~100m, got $distance", 
            distance in 95f..105f)
    }
    
    // ==================================================================================
    // Story 6.4 Task 9: Point-to-Line Distance Tests
    // ==================================================================================
    
    @Test
    fun `perpendicular distance - point on line returns zero`() {
        // Arrange: Point directly on line
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0070)
        val pointOnLine = LatLng(40.7133, -74.0065)  // Midpoint of line
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(pointOnLine, lineStart, lineEnd)
        
        // Assert (allow small tolerance for floating point)
        assertTrue("Point on line should have distance ~0m, got $distance", distance < 5f)
    }
    
    @Test
    fun `perpendicular distance - point perpendicular to midpoint`() {
        // Arrange: Point perpendicular to line midpoint
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0070)
        val pointPerpendicular = LatLng(40.7133, -74.0055)  // ~40m east of midpoint
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(pointPerpendicular, lineStart, lineEnd)
        
        // Assert (should be ~40m)
        assertTrue("Perpendicular distance should be ~40m, got $distance", distance in 35f..45f)
    }
    
    @Test
    fun `perpendicular distance - point before line start returns distance to start`() {
        // Arrange: Point before line start
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0070)
        val pointBefore = LatLng(40.7118, -74.0060)  // ~100m south of start
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(pointBefore, lineStart, lineEnd)
        val expectedDistance = DistanceCalculator.calculateDistance(pointBefore, lineStart)
        
        // Assert (should equal distance to start point)
        assertTrue("Distance should be ~100m to start point, got $distance", distance in 95f..115f)
        assertEquals(expectedDistance, distance, 5f)
    }
    
    @Test
    fun `perpendicular distance - point after line end returns distance to end`() {
        // Arrange: Point after line end
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0070)
        val pointAfter = LatLng(40.7148, -74.0070)  // ~100m north of end
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(pointAfter, lineStart, lineEnd)
        val expectedDistance = DistanceCalculator.calculateDistance(pointAfter, lineEnd)
        
        // Assert (should equal distance to end point)
        assertTrue("Distance should be ~100m to end point, got $distance", distance in 95f..115f)
        assertEquals(expectedDistance, distance, 5f)
    }
    
    @Test
    fun `perpendicular distance - real GPS coordinates for deviation scenario`() {
        // Arrange: Realistic navigation scenario
        val lineStart = LatLng(40.7580, -73.9855)  // Times Square start
        val lineEnd = LatLng(40.7589, -73.9855)    // 100m north
        val userLocation = LatLng(40.7585, -73.9852)  // ~25m east of route midpoint
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(userLocation, lineStart, lineEnd)
        
        // Assert (should be ~25m - triggers deviation threshold)
        assertTrue("User 25m off route, got $distance", distance in 20f..30f)
    }
    
    @Test
    fun `perpendicular distance - 25m off route triggers deviation threshold`() {
        // Arrange: Route segment
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0060)
        val offRoutePoint = LatLng(40.7133, -74.0057)  // ~25m east
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(offRoutePoint, lineStart, lineEnd)
        
        // Assert (should exceed 20m deviation threshold)
        assertTrue("Distance should be >20m (deviation threshold), got $distance", distance > 20f)
        assertTrue("Distance should be ~25m, got $distance", distance in 20f..30f)
    }
    
    @Test
    fun `perpendicular distance - 15m near edge warning zone`() {
        // Arrange: Near-edge scenario (15-20m)
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7138, -74.0060)
        val nearEdgePoint = LatLng(40.7133, -74.0058)  // ~17m east
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(nearEdgePoint, lineStart, lineEnd)
        
        // Assert (should be in 15-20m warning zone)
        assertTrue("Distance should be 15-20m (near-edge), got $distance", distance in 15f..20f)
    }
    
    @Test
    fun `perpendicular distance - long line segment 500m`() {
        // Arrange: Long route segment
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7173, -74.0060)  // ~500m north
        val userLocation = LatLng(40.7150, -74.0057)  // Midpoint, ~25m east
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(userLocation, lineStart, lineEnd)
        
        // Assert (should be ~25m regardless of line length)
        assertTrue("Distance should be ~25m, got $distance", distance in 20f..30f)
    }
    
    @Test
    fun `perpendicular distance - short line segment 10m`() {
        // Arrange: Short route segment
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7129, -74.0060)  // ~10m north
        val userLocation = LatLng(40.7128, -74.0057)  // Near start, ~25m east
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(userLocation, lineStart, lineEnd)
        
        // Assert (should measure to closest point on short segment)
        assertTrue("Distance should be ~25m, got $distance", distance in 20f..30f)
    }
    
    @Test
    fun `perpendicular distance - degenerate line (same start and end)`() {
        // Arrange: Line with same start and end (point)
        val lineStart = LatLng(40.7128, -74.0060)
        val lineEnd = LatLng(40.7128, -74.0060)
        val userLocation = LatLng(40.7128, -74.0057)  // ~25m east
        
        // Act
        val distance = DistanceCalculator.calculatePerpendicularDistance(userLocation, lineStart, lineEnd)
        
        // Assert (should equal distance to point)
        val expectedDistance = DistanceCalculator.calculateDistance(userLocation, lineStart)
        assertEquals(expectedDistance, distance, 5f)
        assertTrue("Distance should be ~25m, got $distance", distance in 20f..30f)
    }
}

