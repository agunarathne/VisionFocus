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
}
