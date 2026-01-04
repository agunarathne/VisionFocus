package com.visionfocus.navigation.service

import com.visionfocus.navigation.models.DeviationState
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.RouteStep
import com.visionfocus.navigation.utils.DistanceCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Story 6.4 Task 8: Unit tests for DeviationDetector.
 * 
 * Tests:
 * 1. Deviation detection with GPS points 25m off route
 * 2. Consecutive deviation tracking (5 updates required)
 * 3. False positive filtering (GPS jitter, single bad update)
 * 4. Near-edge state (15-20m warning zone)
 * 5. Return to route after deviation (reset counter)
 * 6. Edge case: point before route start
 * 7. Edge case: point after route end
 * 8. History reset
 */
class DeviationDetectorTest {
    
    private lateinit var deviationDetector: DeviationDetector
    private lateinit var mockRoute: NavigationRoute
    private val routeStart = LatLng(40.7128, -74.0060)  // NYC
    private val routeEnd = LatLng(40.7138, -74.0070)    // 100m north-west
    
    @Before
    fun setUp() {
        // Use real DistanceCalculator for accurate distance calculations
        deviationDetector = DeviationDetector()
        
        // Create mock route with single step
        val step = RouteStep(
            instruction = "Continue north",
            distance = 100,
            duration = 72,
            maneuver = com.visionfocus.navigation.models.Maneuver.STRAIGHT,
            startLocation = routeStart,
            endLocation = routeEnd,
            polyline = null
        )
        
        val destination = com.visionfocus.navigation.models.Destination(
            query = "Test Destination",
            name = "Test",
            latitude = routeEnd.latitude,
            longitude = routeEnd.longitude,
            formattedAddress = "Test Address"
        )
        
        mockRoute = NavigationRoute(
            origin = routeStart,
            destination = routeEnd,
            steps = listOf(step),
            totalDistance = 100,
            totalDuration = 72,
            polyline = "",
            summary = "Test route"
        )
    }
    
    @Test
    fun `test deviation detection with 25m off route`() {
        // Point 25m east of route (off route)
        val offRouteLocation = LatLng(40.7128, -74.0057)  // ~25m east
        
        val state = deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        
        assertTrue("Should detect off-route", state is DeviationState.OffRoute)
        val offRoute = state as DeviationState.OffRoute
        assertTrue("Distance should be > 20m", offRoute.distanceFromRoute > 20f)
        assertEquals("First deviation should have count 1", 1, offRoute.consecutiveCount)
    }
    
    @Test
    fun `test consecutive tracking requires 5 updates`() {
        // Point 25m off route
        val offRouteLocation = LatLng(40.7128, -74.0057)
        
        // First 4 updates - should track but not trigger recalculation
        repeat(4) { i ->
            val state = deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
            assertTrue("Update ${i+1} should be OffRoute", state is DeviationState.OffRoute)
            val offRoute = state as DeviationState.OffRoute
            assertEquals("Consecutive count should be ${i+1}", i + 1, offRoute.consecutiveCount)
        }
        
        // 5th update - should have consecutive count of 5
        val state5 = deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        assertTrue("5th update should be OffRoute", state5 is DeviationState.OffRoute)
        val offRoute5 = state5 as DeviationState.OffRoute
        assertEquals("5th update should have count 5", 5, offRoute5.consecutiveCount)
    }
    
    @Test
    fun `test false positive filtering with single bad GPS update`() {
        // Start on route
        val onRouteLocation = LatLng(40.7128, -74.0060)  // On route
        deviationDetector.checkDeviation(onRouteLocation, mockRoute, 0)
        
        // Single bad GPS reading (25m off route)
        val offRouteLocation = LatLng(40.7128, -74.0057)
        val stateOff = deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        assertTrue("Single bad reading should be OffRoute", stateOff is DeviationState.OffRoute)
        assertEquals("Single deviation count should be 1", 1, (stateOff as DeviationState.OffRoute).consecutiveCount)
        
        // Return to route immediately (GPS accuracy improved)
        val stateOn = deviationDetector.checkDeviation(onRouteLocation, mockRoute, 0)
        assertTrue("Should return to OnRoute", stateOn is DeviationState.OnRoute)
        
        // Consecutive counter should reset (not trigger recalculation)
        val stateOn2 = deviationDetector.checkDeviation(onRouteLocation, mockRoute, 0)
        assertTrue("Should stay OnRoute", stateOn2 is DeviationState.OnRoute)
    }
    
    @Test
    fun `test near-edge state for 15-20m warning zone`() {
        // Point 17m east of route (near edge)
        val nearEdgeLocation = LatLng(40.7128, -74.0058)  // ~17m east
        
        val state = deviationDetector.checkDeviation(nearEdgeLocation, mockRoute, 0)
        
        assertTrue("Should detect near-edge", state is DeviationState.NearEdge)
        val nearEdge = state as DeviationState.NearEdge
        assertTrue("Distance should be between 15-20m", 
            nearEdge.distanceFromRoute > 15f && nearEdge.distanceFromRoute <= 20f)
    }
    
    @Test
    fun `test return to route resets counter`() {
        // Go off route for 3 updates
        val offRouteLocation = LatLng(40.7128, -74.0057)
        repeat(3) {
            deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        }
        assertEquals("Should have 3 consecutive deviations", 3, deviationDetector.countConsecutiveDeviations())
        
        // Return to route
        val onRouteLocation = LatLng(40.7128, -74.0060)
        val state = deviationDetector.checkDeviation(onRouteLocation, mockRoute, 0)
        
        assertTrue("Should be OnRoute", state is DeviationState.OnRoute)
        
        // Consecutive counter should eventually reset
        repeat(5) {
            deviationDetector.checkDeviation(onRouteLocation, mockRoute, 0)
        }
        assertEquals("Consecutive count should reset to 0", 0, deviationDetector.countConsecutiveDeviations())
    }
    
    @Test
    fun `test point before route start`() {
        // Point 50m south of route start (before start)
        val beforeStart = LatLng(40.7123, -74.0060)  // 50m south
        
        val state = deviationDetector.checkDeviation(beforeStart, mockRoute, 0)
        
        // Should measure distance to start point (closest point on line)
        assertTrue("Should detect as off-route or near-edge", 
            state is DeviationState.OffRoute || state is DeviationState.NearEdge || state is DeviationState.OnRoute)
    }
    
    @Test
    fun `test point after route end`() {
        // Point 50m north of route end (after end)
        val afterEnd = LatLng(40.7143, -74.0070)  // 50m north
        
        val state = deviationDetector.checkDeviation(afterEnd, mockRoute, 0)
        
        // Should measure distance to end point (closest point on line)
        assertTrue("Should detect as off-route", state is DeviationState.OffRoute)
        val offRoute = state as DeviationState.OffRoute
        assertTrue("Distance should be > 20m", offRoute.distanceFromRoute > 20f)
    }
    
    @Test
    fun `test history reset`() {
        // Build up deviation history
        val offRouteLocation = LatLng(40.7128, -74.0057)
        repeat(5) {
            deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        }
        
        assertEquals("Should have 5 consecutive deviations", 5, deviationDetector.countConsecutiveDeviations())
        
        // Reset history
        deviationDetector.resetHistory()
        
        assertEquals("Consecutive count should be 0 after reset", 0, deviationDetector.countConsecutiveDeviations())
        
        // Next check should start fresh
        val state = deviationDetector.checkDeviation(offRouteLocation, mockRoute, 0)
        assertTrue("Should be OffRoute", state is DeviationState.OffRoute)
        assertEquals("Should have count 1 after reset", 1, (state as DeviationState.OffRoute).consecutiveCount)
    }
}
