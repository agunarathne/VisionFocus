package com.visionfocus.navigation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.visionfocus.MainActivity
import com.visionfocus.R
import com.visionfocus.navigation.location.LocationManager
import com.visionfocus.navigation.models.DeviationState
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationProgress
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.TurnWarning
import com.visionfocus.navigation.repository.NavigationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * Story 6.3 Task 3: NavigationService - Foreground service for turn-by-turn navigation.
 * 
 * Manages GPS tracking (1Hz), route following, turn warnings, and TTS announcements.
 * Runs as foreground service to ensure GPS updates continue when screen locked.
 * 
 * **Lifecycle:**
 * - Started by NavigationActiveFragment when route ready
 * - Stopped on arrival, cancellation, or app termination
 * - Survives screen lock and app backgrounding (foreground service)
 * 
 * **GPS Tracking:**
 * - Location updates at 1Hz (LocationManager.getLocationUpdates())
 * - High accuracy mode for pedestrian navigation
 * - Continues in background via foreground service
 * 
 * **Turn-by-Turn Logic:**
 * 1. GPS update received → RouteFollower calculates progress
 * 2. TurnWarningCalculator checks for warnings (advance, immediate, arrival)
 * 3. NavigationAnnouncementManager announces via TTS with audio priority
 * 4. Progress broadcast to NavigationActiveViewModel for UI updates
 * 
 * **Audio Priority (Story 6.3 AC #6):**
 * - Navigation announcements interrupt recognition (QUEUE_FLUSH)
 * - 10% increased volume for safety-critical turn warnings
 * - Story 8.1 will formalize audio priority queue
 */
@AndroidEntryPoint
class NavigationService : Service() {
    
    companion object {
        private const val TAG = "NavigationService"
        
        // Service actions
        const val ACTION_START_NAVIGATION = "com.visionfocus.navigation.START_NAVIGATION"
        const val ACTION_STOP_NAVIGATION = "com.visionfocus.navigation.STOP_NAVIGATION"
        
        // Intent extras
        const val EXTRA_ROUTE = "EXTRA_ROUTE"
        const val EXTRA_DESTINATION = "EXTRA_DESTINATION"
        
        // Notification
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "navigation_channel"
        
        // Turn warning thresholds (based on walking speed ~1.4 m/s)
        private const val ADVANCE_WARNING_DISTANCE = 70f  // meters (~50 seconds = 5-7 second warning)
        private const val IMMEDIATE_WARNING_DISTANCE = 15f // meters
        private const val ARRIVAL_THRESHOLD_DISTANCE = 10f // meters
        
        // Checkpoint distance for straight sections
        private const val CHECKPOINT_DISTANCE = 200f // meters
        
        // Story 6.4: Recalculation constants
        private const val RECALCULATION_TIMEOUT = 3000L  // 3 seconds (AC #4)
        private const val EXCESSIVE_RECALC_THRESHOLD = 3  // >3 recalcs (AC #7)
        private const val EXCESSIVE_RECALC_WINDOW = 120_000L  // 2 minutes in milliseconds
    }
    
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var routeFollower: RouteFollower
    @Inject lateinit var turnWarningCalculator: TurnWarningCalculator
    @Inject lateinit var announcementManager: NavigationAnnouncementManager
    @Inject lateinit var deviationDetector: DeviationDetector
    @Inject lateinit var navigationRepository: NavigationRepository
    
    // Coroutine scope for GPS updates
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationUpdatesJob: Job? = null
    
    // Current navigation state
    private var navigationRoute: NavigationRoute? = null
    private var originalDestination: Destination? = null
    private var previousProgress: NavigationProgress? = null
    private var isNavigating = false
    
    // Story 6.4: Recalculation tracking
    private var recalculationCount = 0
    private var recalculationWindowStart: Long = 0
    private var isRecalculating = false
    
    // StateFlow for progress broadcasting to UI
    private val _navigationProgress = MutableStateFlow<NavigationProgress?>(null)
    val navigationProgress: StateFlow<NavigationProgress?> = _navigationProgress.asStateFlow()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NavigationService created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_NAVIGATION -> {
                val route = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_ROUTE, NavigationRoute::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_ROUTE)
                }
                
                val destination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DESTINATION, Destination::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DESTINATION)
                }
                
                if (route != null && destination != null) {
                    startNavigation(route, destination)
                } else {
                    Log.e(TAG, "No route or destination provided in intent")
                    stopSelf()
                }
            }
            ACTION_STOP_NAVIGATION -> {
                stopNavigationAndService()
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent?.action}")
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /**
     * Start navigation with the provided route.
     * 
     * 1. Start foreground service with notification
     * 2. Announce navigation start via TTS
     * 3. Begin GPS location updates at 1Hz
     * 
     * Story 6.4: Store destination for recalculation
     */
    private fun startNavigation(route: NavigationRoute, destination: Destination) {
        Log.d(TAG, "Starting navigation: ${route.steps.size} steps, ${route.totalDistance}m to ${destination.name}")
        
        navigationRoute = route
        originalDestination = destination
        previousProgress = null
        isNavigating = true
        recalculationCount = 0
        recalculationWindowStart = System.currentTimeMillis()
        
        // Reset deviation detector
        deviationDetector.resetHistory()
        
        // Start foreground service with notification
        val notification = createNavigationNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Announce navigation start
        serviceScope.launch {
            announcementManager.announceNavigationStart(route.totalDistance, route.totalDuration)
        }
        
        // Start GPS location updates (1Hz)
        startLocationUpdates(route)
    }
    
    /**
     * Start GPS location updates and process each update.
     */
    private fun startLocationUpdates(route: NavigationRoute) {
        locationUpdatesJob?.cancel()
        
        locationUpdatesJob = serviceScope.launch {
            try {
                locationManager.getLocationUpdates().collect { currentLocation ->
                    if (isNavigating) {
                        handleLocationUpdate(currentLocation, route)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in location updates", e)
                
                // MEDIUM Issue #13: Announce GPS failure to user
                announcementManager.announceWithPriority("GPS signal lost. Searching for signal...")
                
                stopNavigationAndService()
            }
        }
    }
    
    /**
     * Handle GPS location update - core navigation logic.
     * 
     * 1. Calculate progress via RouteFollower
     * 2. Check for turn warnings via TurnWarningCalculator
     * 3. Announce warnings via NavigationAnnouncementManager
     * 4. Update progress (will be broadcast to UI in future task)
     * 
     * Story 6.4: Check for route deviation and recalculate if needed
     */
    private suspend fun handleLocationUpdate(currentLocation: LatLng, route: NavigationRoute) {
        // Calculate navigation progress
        val progress = routeFollower.calculateProgress(currentLocation, route, previousProgress)
        
        Log.d(TAG, "Location update: step ${progress.currentStepIndex}/${route.steps.size}, " +
                "distance ${progress.distanceToCurrentStep.toInt()}m, " +
                "totalRemaining ${progress.totalDistanceRemaining.toInt()}m")
        
        // Story 6.4: Check for route deviation
        val deviationState = deviationDetector.checkDeviation(
            currentLocation,
            route,
            progress.currentStepIndex
        )
        
        when (deviationState) {
            is DeviationState.OffRoute -> {
                // Deviation persisted for 5 seconds - trigger recalculation
                if (deviationState.consecutiveCount >= DeviationState.CONSECUTIVE_REQUIRED) {
                    handleRouteDeviation(currentLocation)
                    return  // Don't process turn warnings during recalculation
                }
            }
            is DeviationState.NearEdge -> {
                // User near edge - log warning but don't recalculate
                Log.w(TAG, "User near route edge: ${deviationState.distanceFromRoute}m")
            }
            is DeviationState.OnRoute -> {
                // User on route - reset deviation history if previously deviated
                if (deviationDetector.countConsecutiveDeviations() > 0) {
                    Log.d(TAG, "User returned to route")
                    deviationDetector.resetHistory()
                }
            }
        }
        
        // Check for turn warnings
        val warning = turnWarningCalculator.checkForWarning(progress, route)
        
        // Announce warnings via TTS
        when (warning) {
            is TurnWarning.Advance -> {
                if (!progress.hasGivenAdvanceWarning) {
                    announcementManager.announceAdvanceWarning(warning.step, warning.distanceMeters)
                    previousProgress = routeFollower.markAdvanceWarningGiven(progress)
                    return
                }
            }
            is TurnWarning.Immediate -> {
                if (!progress.hasGivenImmediateWarning) {
                    announcementManager.announceImmediateTurn(warning.step)
                    previousProgress = routeFollower.markImmediateWarningGiven(progress)
                    return
                }
            }
            is TurnWarning.Checkpoint -> {
                announcementManager.announceStraightCheckpoint(warning.distanceMeters)
            }
            is TurnWarning.Arrival -> {
                announcementManager.announceArrival()
                stopNavigationAndService()
                return
            }
            null -> {
                // No warning needed
            }
        }
        
        // Update previous progress
        previousProgress = progress
        
        // Broadcast progress to NavigationActiveViewModel (CRITICAL Issue #1 fix)
        _navigationProgress.value = progress
    }
    
    /**
     * Handles route deviation by recalculating route and resuming guidance.
     * 
     * Story 6.4 AC #2, #3, #4, #5, #7
     */
    private suspend fun handleRouteDeviation(currentLocation: LatLng) {
        val destination = originalDestination
        if (destination == null) {
            Log.e(TAG, "Cannot recalculate: no destination stored")
            return
        }
        
        // Prevent multiple simultaneous recalculations
        if (isRecalculating) {
            Log.w(TAG, "Recalculation already in progress, skipping")
            return
        }
        
        Log.i(TAG, "Route deviation detected. Recalculating from $currentLocation to ${destination.name}")
        
        // Check for excessive recalculations (AC #7)
        val now = System.currentTimeMillis()
        
        // Increment count first
        recalculationCount++
        
        // Check if this is the first recalc or if window expired
        if (recalculationWindowStart == 0L || now - recalculationWindowStart > EXCESSIVE_RECALC_WINDOW) {
            // Start new window
            recalculationWindowStart = now
            recalculationCount = 1  // Reset count for new window
        }
        
        // Check threshold (>3 means 4th recalc within 2 minutes)
        if (recalculationCount > EXCESSIVE_RECALC_THRESHOLD) {
            // User having trouble staying on route - provide guidance
            announcementManager.announceExcessiveRecalculations()
            // Reset counter to avoid repeating guidance immediately
            recalculationCount = 0
            recalculationWindowStart = now
        }
        
        // Announce deviation (AC #2)
        announcementManager.announceDeviation()
        
        // Set recalculating flag
        isRecalculating = true
        
        try {
            // Recalculate route with 3-second timeout (AC #4)
            val newRoute = withTimeout(RECALCULATION_TIMEOUT) {
                val result = navigationRepository.recalculateRoute(currentLocation, destination)
                when {
                    result.isSuccess -> result.getOrThrow()
                    else -> throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
            
            // Replace route and reset route follower (AC #5)
            navigationRoute = newRoute
            previousProgress = routeFollower.replaceRoute(newRoute)  // Store for immediate UI update
            deviationDetector.resetHistory()
            
            // Broadcast progress immediately (AC #5: guidance resumes immediately)
            _navigationProgress.value = previousProgress
            
            // Announce success
            announcementManager.announceRecalculationSuccess()
            
            Log.i(TAG, "Route recalculated successfully. New route: ${newRoute.steps.size} steps")
            
        } catch (e: TimeoutCancellationException) {
            // Recalculation timed out (>3 seconds)
            Log.e(TAG, "Route recalculation timed out", e)
            announcementManager.announceRecalculationError("Recalculation is taking too long. Continuing with original route.")
        } catch (e: Exception) {
            // Network error, API failure, or other error
            Log.e(TAG, "Route recalculation failed", e)
            announcementManager.announceRecalculationError("Cannot recalculate route. Check internet connection.")
        } finally {
            isRecalculating = false
        }
    }
    
    /**
     * Stop navigation service and cleanup.
     */
    private fun stopNavigationAndService() {
        Log.d(TAG, "Stopping navigation service")
        
        isNavigating = false
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
        
        // Restore original TTS volume
        announcementManager.restoreOriginalVolume()
        
        // Stop foreground and remove notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        stopSelf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NavigationService destroyed")
        
        isNavigating = false
        locationUpdatesJob?.cancel()
        serviceScope.cancel()
        
        announcementManager.restoreOriginalVolume()
    }
    
    /**
     * Create notification channel for navigation (API 26+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navigation",
                NotificationManager.IMPORTANCE_LOW  // Low importance = no sound/vibration
            ).apply {
                description = "Turn-by-turn navigation in progress"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create ongoing notification for foreground service.
     */
    private fun createNavigationNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel action
        val cancelIntent = Intent(this, NavigationService::class.java).apply {
            action = ACTION_STOP_NAVIGATION
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Navigation Active")
            .setContentText("Turn-by-turn guidance in progress")
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)  // Cannot be dismissed
            .addAction(
                R.drawable.ic_close,
                "Cancel",
                cancelPendingIntent
            )
            .build()
    }
}
