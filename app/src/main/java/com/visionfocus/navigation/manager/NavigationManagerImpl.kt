package com.visionfocus.navigation.manager

import android.content.Context
import android.widget.Toast
import com.visionfocus.data.repository.OfflineMapRepository
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationMode
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.network.monitor.NetworkStateMonitor
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NavigationManager for Story 7.2 and 7.5.
 * 
 * Story 7.2: Provides basic TTS announcement for navigation start
 * Story 7.5: Routing strategy pattern with online/offline mode switching
 * Future (Epic 6): Full GPS-based turn-by-turn navigation with Google Maps Directions API
 * 
 * Routing Strategy:
 * - ONLINE: Use Google Maps API (live traffic, recalculation)
 * - OFFLINE: Use Mapbox offline tiles (static route, no recalculation)
 * - UNAVAILABLE: No routing (fallback to basic guidance)
 */
@Singleton
class NavigationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsManager: TTSManager,
    private val offlineMapRepository: OfflineMapRepository,
    private val networkStateMonitor: NetworkStateMonitor
) : NavigationManager {
    
    // Story 7.5: Current navigation mode
    private val _navigationMode = MutableStateFlow(NavigationMode.ONLINE)
    override val navigationMode: StateFlow<NavigationMode> = _navigationMode.asStateFlow()
    
    override suspend fun startNavigation(
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationName: String
    ): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            // Story 7.5: Determine navigation mode
            val isOnline = networkStateMonitor.isNetworkAvailable.value
            val hasOfflineMaps = offlineMapRepository.isOfflineMapAvailable(
                // Note: This is a simplified check - in full implementation,
                // we'd need to check by coordinates, not just location ID
                locationId = 0  // Placeholder
            )
            
            val mode = determineNavigationMode(isOnline, hasOfflineMaps)
            _navigationMode.value = mode
            
            Timber.d("Starting navigation in $mode mode to $destinationName")
            
            // Story 7.2: Announce navigation start via TTS
            ttsManager.announce("Starting navigation to $destinationName")
            
            // Development verification: Show toast with coordinates and mode
            Toast.makeText(
                context,
                "Navigation to $destinationName [$mode]\nLat: $destinationLatitude, Lon: $destinationLongitude",
                Toast.LENGTH_LONG
            ).show()
            
            Timber.d("Navigation started to $destinationName ($destinationLatitude, $destinationLongitude) in $mode mode")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start navigation")
            Result.failure(e)
        }
    }
    
    /**
     * Story 7.5 Task 2.4: Determine navigation mode based on context.
     * 
     * Decision matrix:
     * - isOnline=true, hasOfflineMaps=* → ONLINE (prefer live traffic)
     * - isOnline=false, hasOfflineMaps=true → OFFLINE
     * - isOnline=false, hasOfflineMaps=false → UNAVAILABLE
     */
    override fun determineNavigationMode(
        isOnline: Boolean,
        hasOfflineMaps: Boolean
    ): NavigationMode {
        return when {
            isOnline -> NavigationMode.ONLINE
            hasOfflineMaps -> NavigationMode.OFFLINE
            else -> NavigationMode.UNAVAILABLE
        }
    }
    
    /**
     * Story 7.5 Task 2.5: Get offline route from Mapbox SDK.
     * 
     * NOTE: Story 7.5 not yet implemented - offline routing deferred
     */
    override suspend fun getOfflineRoute(
        origin: LatLng,
        destination: LatLng
    ): Result<NavigationRoute> {
        return Result.failure(NotImplementedError("Story 7.5: Offline routing not yet implemented"))
    }
}
