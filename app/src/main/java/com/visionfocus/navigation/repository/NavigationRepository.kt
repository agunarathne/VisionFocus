package com.visionfocus.navigation.repository

import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.ValidationResult

/**
 * Navigation repository interface.
 * 
 * Story 6.1: Destination Input via Voice and Text (mock implementation)
 * Story 6.2: Google Maps Directions API Integration (geocoding implementation)
 * Story 6.4: Route recalculation for deviation recovery
 */
interface NavigationRepository {
    /**
     * Validates destination query and returns geocoded result.
     * 
     * Story 6.1: Mock implementation (no network calls)
     * Story 6.2: Google Maps Geocoding API integration
     * 
     * @param query User-entered destination (address, landmark, city)
     * @return ValidationResult with geocoded coordinates or error
     */
    suspend fun validateDestination(query: String): ValidationResult
    
    /**
     * Gets turn-by-turn route from current GPS location to destination.
     * 
     * Story 6.2: Google Maps Directions API integration with error handling.
     * CODE REVIEW FIX (Issue #3): Origin is always current GPS location.
     * Returns Result.success with route or Result.failure with error.
     * 
     * @param destination Target location
     * @return Result<NavigationRoute> with waypoints and directions or error
     */
    suspend fun getRoute(destination: Destination): Result<NavigationRoute>
    
    /**
     * Recalculates route from current location to destination.
     * 
     * Story 6.4: Called when user deviates from route (>20m for 5 seconds).
     * Reuses DirectionsApiService from Story 6.2.
     * 
     * @param origin Current GPS location (where user deviated)
     * @param destination Original destination (unchanged)
     * @return Result<NavigationRoute> with new route or error
     */
    suspend fun recalculateRoute(origin: LatLng, destination: Destination): Result<NavigationRoute>
}
