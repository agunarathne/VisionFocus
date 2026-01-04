package com.visionfocus.navigation.service

import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.Maneuver
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.RouteStep
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Story 6.3 Task 11: Unit tests for RouteFollower (CRITICAL Issue #3).
 * 
 * Tests route following logic: distance calculations, step progression,
 * time estimation, and warning flag management.
 */
class RouteFollowerTest {
    
    private lateinit var routeFollower: RouteFollower
    
    // Test route: Times Square → Empire State Building (1.1 km)
    private val timesSquare = LatLng(40.758896, -73.985130)
    private val midpoint = LatLng(40.753856, -73.985279)  // ~560m between points
    private val empireState = LatLng(40.748817, -73.985428)
    
    private lateinit var testRoute: NavigationRoute
    
    @Before
    fun setUp() {
        routeFollower = RouteFollower()
        
        // Create simple 2-step route
        testRoute = NavigationRoute(
            origin = timesSquare,
            destination = empireState,
            steps = listOf(
                RouteStep(
                    instruction = "Head south on 7th Ave",
                    distance = 560,  // meters
                    duration = 400,  // seconds
                    maneuver = Maneuver.STRAIGHT,
                    startLocation = timesSquare,
                    endLocation = midpoint
                ),
                RouteStep(
                    instruction = "Turn left onto W 34th St",
                    distance = 550,  // meters
                    duration = 390,  // seconds
                    maneuver = Maneuver.TURN_LEFT,
                    startLocation = midpoint,
                    endLocation = empireState
                )
            ),
            totalDistance = 1110,
            totalDuration = 790,
            polyline = "",
            summary = "via 7th Ave"
        )
    }
    
    @Test
    fun `calculateProgress - initial position at start`() {
        // Arrange: User at Times Square (start)
        val currentLocation = timesSquare
        
        // Act
        val progress = routeFollower.calculateProgress(currentLocation, testRoute)
        
        // Assert
        assertEquals(0, progress.currentStepIndex)
        assertTrue("Distance to step should be ~560m", progress.distanceToCurrentStep in 500f..600f)
        assertTrue("Total remaining should be ~1110m", progress.totalDistanceRemaining in 1050f..1150f)
        assertTrue("Time should be ~790s", progress.estimatedTimeRemaining in 750..830)
        assertFalse(progress.hasGivenAdvanceWarning)
        assertFalse(progress.hasGivenImmediateWarning)
    }
    
    @Test
    fun `calculateProgress - halfway through first step`() {
        // Arrange: User halfway between Times Square and midpoint
        val currentLocation = LatLng(40.756376, -73.985204)  // ~280m to midpoint
        
        // Act
        val progress = routeFollower.calculateProgress(currentLocation, testRoute)
        
        // Assert
        assertEquals(0, progress.currentStepIndex)
        assertTrue("Distance to step should be ~280m", progress.distanceToCurrentStep in 250f..310f)
        assertTrue("Total remaining should be ~830m", progress.totalDistanceRemaining in 800f..860f)
    }
    
    @Test
    fun `calculateProgress - near end of first step`() {
        // Arrange: User very close to midpoint (15m away - triggers immediate warning)
        val nearMidpoint = LatLng(40.753990, -73.985270)
        
        // Act
        val progress = routeFollower.calculateProgress(nearMidpoint, testRoute)
        
        // Assert
        assertEquals(0, progress.currentStepIndex)
        assertTrue("Distance should be <20m", progress.distanceToCurrentStep < 20f)
        assertFalse("Should not be completed yet", progress.hasCompletedCurrentStep)
    }
    
    @Test
    fun `calculateProgress - step completion advances to next step`() {
        // Arrange: User passed midpoint (20m past = step completed)
        val pastMidpoint = LatLng(40.753720, -73.985288)
        
        val previousProgress = routeFollower.calculateProgress(midpoint, testRoute).copy(
            hasCompletedCurrentStep = true
        )
        
        // Act
        val progress = routeFollower.calculateProgress(pastMidpoint, testRoute, previousProgress)
        
        // Assert
        assertEquals(1, progress.currentStepIndex, "Should advance to step 1")
        assertTrue("Distance to step 1 end should be ~550m", progress.distanceToCurrentStep in 500f..600f)
    }
    
    @Test
    fun `calculateProgress - time estimation based on walking speed`() {
        // Arrange: 700 meters remaining at 1.4 m/s = 500 seconds
        val currentLocation = LatLng(40.756376, -73.985204)
        
        // Act
        val progress = routeFollower.calculateProgress(currentLocation, testRoute)
        
        // Assert: Time = distance / 1.4 m/s
        val expectedTime = (progress.totalDistanceRemaining / 1.4f).toInt()
        assertEquals("Time should match walking speed calculation", expectedTime, progress.estimatedTimeRemaining)
    }
    
    @Test
    fun `markAdvanceWarningGiven - sets flag correctly`() {
        // Arrange
        val progress = routeFollower.calculateProgress(timesSquare, testRoute)
        assertFalse(progress.hasGivenAdvanceWarning)
        
        // Act
        val updated = routeFollower.markAdvanceWarningGiven(progress)
        
        // Assert
        assertTrue(updated.hasGivenAdvanceWarning)
        assertFalse(updated.hasGivenImmediateWarning)
    }
    
    @Test
    fun `markImmediateWarningGiven - sets flag correctly`() {
        // Arrange
        val progress = routeFollower.calculateProgress(timesSquare, testRoute)
        assertFalse(progress.hasGivenImmediateWarning)
        
        // Act
        val updated = routeFollower.markImmediateWarningGiven(progress)
        
        // Assert
        assertTrue(updated.hasGivenImmediateWarning)
        assertFalse(updated.hasGivenAdvanceWarning)
    }
    
    @Test
    fun `calculateProgress - warning flags reset on new step`() {
        // Arrange: Complete first step with warnings given
        val previousProgress = routeFollower.calculateProgress(midpoint, testRoute).copy(
            hasGivenAdvanceWarning = true,
            hasGivenImmediateWarning = true,
            hasCompletedCurrentStep = true
        )
        
        // Act: Move to next step
        val newProgress = routeFollower.calculateProgress(midpoint, testRoute, previousProgress)
        
        // Assert: Flags should reset for new step
        assertFalse("Advance warning flag should reset", newProgress.hasGivenAdvanceWarning)
        assertFalse("Immediate warning flag should reset", newProgress.hasGivenImmediateWarning)
    }
    
    @Test
    fun `calculateProgress - bearing calculation included`() {
        // Arrange
        val currentLocation = timesSquare
        
        // Act
        val progress = routeFollower.calculateProgress(currentLocation, testRoute)
        
        // Assert
        assertNotNull("Bearing should be calculated", progress.bearingToNextStep)
        assertTrue("Bearing should be valid (0-360)", progress.bearingToNextStep!! in 0f..360f)
    }
}
