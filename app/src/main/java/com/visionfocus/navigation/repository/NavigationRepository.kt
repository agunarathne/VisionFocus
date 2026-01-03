package com.visionfocus.navigation.repository

import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.ValidationResult

/**
 * Navigation repository interface.
 * 
 * Story 6.1: Destination Input via Voice and Text (mock implementation)
 * Story 6.2: Google Maps Directions API Integration (geocoding implementation)
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
}
