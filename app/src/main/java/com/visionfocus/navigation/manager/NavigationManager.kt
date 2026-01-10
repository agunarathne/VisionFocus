package com.visionfocus.navigation.manager

import com.visionfocus.navigation.models.NavigationMode
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.LatLng
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing navigation functionality.
 * 
 * Story 7.2: Integration point for saved locations navigation
 * Story 6.x: Full implementation with Google Maps Directions API
 * Story 7.5: Automatic mode switching between online/offline navigation
 * 
 * Routing Strategy Pattern:
 * - Online: Google Maps API with live traffic
 * - Offline: Mapbox offline tiles with static routes
 * - Unavailable: No routing capability
 */
interface NavigationManager {
    /**
     * Start turn-by-turn navigation to destination.
     * 
     * @param destinationLatitude GPS latitude of destination
     * @param destinationLongitude GPS longitude of destination
     * @param destinationName User-facing name for announcements
     * @return Result.success if navigation started, Result.failure if GPS/permissions unavailable
     */
    suspend fun startNavigation(
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationName: String
    ): Result<Unit>
    
    /**
     * Story 7.5: Observe current navigation mode (online/offline/unavailable).
     * 
     * UI can observe this StateFlow to display mode indicator and update capabilities.
     */
    val navigationMode: StateFlow<NavigationMode>
    
    /**
     * Story 7.5: Determine appropriate navigation mode based on context.
     * 
     * @param isOnline Whether network is available
     * @param hasOfflineMaps Whether destination has offline maps downloaded
     * @return Appropriate navigation mode
     */
    fun determineNavigationMode(
        isOnline: Boolean,
        hasOfflineMaps: Boolean
    ): NavigationMode
    
    /**
     * Story 7.5: Get offline route from Mapbox SDK.
     * 
     * @param origin Starting location
     * @param destination Ending location
     * @return Result containing NavigationRoute or error
     */
    suspend fun getOfflineRoute(
        origin: LatLng,
        destination: LatLng
    ): Result<NavigationRoute>
}
