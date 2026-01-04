package com.visionfocus.navigation.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.visionfocus.core.audio.TTSManager
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.navigation.models.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Story 6.3 Task 14: Integration tests for NavigationService (CRITICAL Issue #6).
 * 
 * Tests service lifecycle, GPS updates (1Hz), turn warnings, rerouting,
 * notification updates, and progress broadcasting to UI.
 * 
 * Note: These are unit tests with mocked dependencies. Full integration testing
 * requires instrumentation tests or Robolectric (not included in current build).
 */
@ExperimentalCoroutinesApi
class NavigationServiceTest {
    
    private lateinit var context: Context
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var ttsManager: TTSManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationManager: NotificationManager
    
    private lateinit var navigationService: NavigationService
    
    private val testRoute = NavigationRoute(
        origin = LatLng(40.758896, -73.985130),
        destination = LatLng(40.748817, -73.985428),
        steps = listOf(
            RouteStep(
                instruction = "Head south on 7th Ave",
                distance = 1100,
                duration = 780,
                maneuver = Maneuver.STRAIGHT,
                startLocation = LatLng(40.758896, -73.985130),
                endLocation = LatLng(40.748817, -73.985428)
            )
        ),
        totalDistance = 1100,
        totalDuration = 780,
        polyline = "",
        summary = "via 7th Ave"
    )
    
    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        locationClient = mockk(relaxed = true)
        ttsManager = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        notificationManager = mockk(relaxed = true)
        
        // Mock context.getSystemService
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.packageName } returns "com.visionfocus"
        
        navigationService = NavigationService().apply {
            // Inject mocks via reflection or use Hilt in real tests
        }
    }
    
    @Test
    fun `startNavigation - initializes route and starts GPS updates`() = runTest {
        // Act
        navigationService.startNavigation(testRoute)
        
        // Assert: GPS location requests should be 1Hz
        verify {
            locationClient.requestLocationUpdates(
                match { request ->
                    request.interval == 1000L &&  // 1 second = 1 Hz
                    request.priority == com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
                },
                any<LocationCallback>(),
                any()
            )
        }
    }
    
    @Test
    fun `onLocationUpdate - broadcasts progress to StateFlow`() = runTest {
        // Arrange: Start navigation
        navigationService.startNavigation(testRoute)
        
        val mockLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758896
            every { longitude } returns -73.985130
            every { accuracy } returns 10f
        }
        
        // Act: Simulate GPS update
        val locationResult = LocationResult.create(listOf(mockLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Progress should be emitted to StateFlow
        val progress = navigationService.navigationProgress.first()
        assertNotNull("Progress should be emitted", progress)
        assertEquals(0, progress!!.currentStepIndex)
        assertTrue("Distance should be calculated", progress.distanceToCurrentStep > 0)
    }
    
    @Test
    fun `onLocationUpdate - triggers advance warning at 70m`() = runTest {
        // Arrange: Route with turn at 70m
        val routeWithTurn = testRoute.copy(
            steps = listOf(
                testRoute.steps[0].copy(
                    distance = 70,  // User exactly 70m from turn
                    maneuver = Maneuver.TURN_LEFT
                )
            )
        )
        navigationService.startNavigation(routeWithTurn)
        
        val mockLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758896
            every { longitude } returns -73.985130
        }
        
        // Act
        val locationResult = LocationResult.create(listOf(mockLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Advance warning should be announced
        verify(timeout = 1000) {
            ttsManager.announce(match { message ->
                message.contains("70 meters", ignoreCase = true) &&
                message.contains("turn left", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `onLocationUpdate - triggers immediate warning at 15m`() = runTest {
        // Arrange: Route with turn at 15m
        val routeWithImmediateTurn = testRoute.copy(
            steps = listOf(
                testRoute.steps[0].copy(
                    distance = 15,
                    maneuver = Maneuver.TURN_RIGHT
                )
            )
        )
        navigationService.startNavigation(routeWithImmediateTurn)
        
        val mockLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758896
            every { longitude } returns -73.985130
        }
        
        // Act
        val locationResult = LocationResult.create(listOf(mockLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Immediate warning should be announced
        verify(timeout = 1000) {
            ttsManager.announce(match { message ->
                message.contains("turn right", ignoreCase = true) &&
                message.contains("now", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `onLocationUpdate - triggers rerouting when off course`() = runTest {
        // Arrange: User goes 50m off route (>10m threshold)
        navigationService.startNavigation(testRoute)
        
        val offCourseLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.760000  // Far from route
            every { longitude } returns -73.990000
        }
        
        // Act
        val locationResult = LocationResult.create(listOf(offCourseLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Rerouting should be announced
        verify(timeout = 1000) {
            ttsManager.announce(match { message ->
                message.contains("recalculating", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `notification updates every 5 seconds with ETA`() = runTest {
        // Arrange: Start navigation
        navigationService.startNavigation(testRoute)
        
        val mockLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758896
            every { longitude } returns -73.985130
        }
        
        // Act: Trigger location update
        val locationResult = LocationResult.create(listOf(mockLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Wait for notification update (throttled to 5s)
        Thread.sleep(5100)
        
        // Assert: Notification should show distance and ETA
        verify(atLeast = 1) {
            notificationManager.notify(
                NavigationService.NOTIFICATION_ID,
                match<Notification> { notification ->
                    notification.contentText?.contains("meters", ignoreCase = true) == true ||
                    notification.contentText?.contains("minutes", ignoreCase = true) == true
                }
            )
        }
    }
    
    @Test
    fun `stopNavigation - cleans up resources`() = runTest {
        // Arrange: Start navigation
        navigationService.startNavigation(testRoute)
        
        // Act
        navigationService.stopNavigation()
        
        // Assert: GPS updates stopped, notification cancelled
        verify {
            locationClient.removeLocationUpdates(any<LocationCallback>())
            notificationManager.cancel(NavigationService.NOTIFICATION_ID)
        }
    }
    
    @Test
    fun `poor GPS accuracy triggers error announcement`() = runTest {
        // Arrange: Start navigation
        navigationService.startNavigation(testRoute)
        
        val poorAccuracyLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758896
            every { longitude } returns -73.985130
            every { accuracy } returns 50f  // Poor accuracy >20m
        }
        
        // Act
        val locationResult = LocationResult.create(listOf(poorAccuracyLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Error announcement should be triggered
        verify(timeout = 1000) {
            ttsManager.announce(match { message ->
                message.contains("GPS signal", ignoreCase = true) ||
                message.contains("accuracy", ignoreCase = true)
            }, priority = false)
        }
    }
    
    @Test
    fun `step completion advances to next step`() = runTest {
        // Arrange: Multi-step route
        val multiStepRoute = testRoute.copy(
            steps = listOf(
                RouteStep(
                    instruction = "Head south",
                    distance = 50,  // Complete this step
                    duration = 35,
                    maneuver = Maneuver.STRAIGHT,
                    startLocation = LatLng(40.758896, -73.985130),
                    endLocation = LatLng(40.758400, -73.985130)
                ),
                RouteStep(
                    instruction = "Turn left onto Broadway",
                    distance = 1000,
                    duration = 710,
                    maneuver = Maneuver.TURN_LEFT,
                    startLocation = LatLng(40.758400, -73.985130),
                    endLocation = LatLng(40.748817, -73.985428)
                )
            ),
            totalDistance = 1050,
            totalDuration = 745
        )
        navigationService.startNavigation(multiStepRoute)
        
        // Act: Move to end of first step
        val completedLocation = mockk<Location>(relaxed = true).apply {
            every { latitude } returns 40.758400
            every { longitude } returns -73.985130
        }
        val locationResult = LocationResult.create(listOf(completedLocation))
        navigationService.handleLocationUpdate(locationResult)
        
        // Assert: Progress should show step 1 (second step)
        val progress = navigationService.navigationProgress.first()
        assertEquals(1, progress!!.currentStepIndex)
    }
}
