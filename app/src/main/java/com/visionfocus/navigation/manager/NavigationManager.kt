package com.visionfocus.navigation.manager

/**
 * Interface for managing navigation functionality.
 * 
 * Story 7.2: Integration point for saved locations navigation
 * Story 6.x: Full implementation with Google Maps Directions API
 * 
 * Current implementation: Stub for Story 7.2 development
 * Future: Epic 6 will implement full GPS-based turn-by-turn navigation
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
}
