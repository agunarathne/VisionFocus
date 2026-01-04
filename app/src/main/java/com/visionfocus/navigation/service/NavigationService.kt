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
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationProgress
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.TurnWarning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        
        // Notification
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "navigation_channel"
        
        // Turn warning thresholds (based on walking speed ~1.4 m/s)
        private const val ADVANCE_WARNING_DISTANCE = 70f  // meters (~50 seconds = 5-7 second warning)
        private const val IMMEDIATE_WARNING_DISTANCE = 15f // meters
        private const val ARRIVAL_THRESHOLD_DISTANCE = 10f // meters
        
        // Checkpoint distance for straight sections
        private const val CHECKPOINT_DISTANCE = 200f // meters
    }
    
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var routeFollower: RouteFollower
    @Inject lateinit var turnWarningCalculator: TurnWarningCalculator
    @Inject lateinit var announcementManager: NavigationAnnouncementManager
    
    // Coroutine scope for GPS updates
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationUpdatesJob: Job? = null
    
    // Current navigation state
    private var navigationRoute: NavigationRoute? = null
    private var previousProgress: NavigationProgress? = null
    private var isNavigating = false
    
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
                
                if (route != null) {
                    startNavigation(route)
                } else {
                    Log.e(TAG, "No route provided in intent")
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
     */
    private fun startNavigation(route: NavigationRoute) {
        Log.d(TAG, "Starting navigation: ${route.steps.size} steps, ${route.totalDistance}m")
        
        navigationRoute = route
        previousProgress = null
        isNavigating = true
        
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
     */
    private suspend fun handleLocationUpdate(currentLocation: LatLng, route: NavigationRoute) {
        // Calculate navigation progress
        val progress = routeFollower.calculateProgress(currentLocation, route, previousProgress)
        
        Log.d(TAG, "Location update: step ${progress.currentStepIndex}/${route.steps.size}, " +
                "distance ${progress.distanceToCurrentStep.toInt()}m, " +
                "totalRemaining ${progress.totalDistanceRemaining.toInt()}m")
        
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
