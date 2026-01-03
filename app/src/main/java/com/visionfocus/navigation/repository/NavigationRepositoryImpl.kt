package com.visionfocus.navigation.repository

import android.content.Context
import android.util.Log
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.ValidationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigation repository implementation.
 * 
 * Story 6.1: Mock validation (no network calls, basic validation only)
 * Story 6.2: Google Maps Geocoding API + Directions API integration
 */
@Singleton
class NavigationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NavigationRepository {
    
    companion object {
        private const val TAG = "NavigationRepositoryImpl"
        private const val MIN_QUERY_LENGTH = 3
        
        // Mock coordinates for Story 6.1 (NYC)
        private const val MOCK_LATITUDE = 40.7128
        private const val MOCK_LONGITUDE = -74.0060
    }
    
    /**
     * Mock destination validation for Story 6.1.
     * 
     * Story 6.2 will replace this with Google Maps Geocoding API calls.
     * 
     * @param query User-entered destination
     * @return ValidationResult with mock coordinates
     */
    override suspend fun validateDestination(query: String): ValidationResult {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Validating destination: $query")
            
            val trimmedQuery = query.trim()
            
            // Empty check
            if (trimmedQuery.isBlank()) {
                return@withContext ValidationResult.Empty
            }
            
            // Length check (minimum 3 characters)
            if (trimmedQuery.length < MIN_QUERY_LENGTH) {
                return@withContext ValidationResult.TooShort
            }
            
            // Story 6.1: Mock implementation - accept any valid query
            // Story 6.2: Replace with Google Maps Geocoding API
            
            // Simulate potential ambiguous destinations for testing
            if (trimmedQuery.contains("central park", ignoreCase = true)) {
                return@withContext ValidationResult.Ambiguous(
                    options = listOf(
                        Destination(
                            query = trimmedQuery,
                            name = "Central Park, New York",
                            latitude = 40.785091,
                            longitude = -73.968285,
                            formattedAddress = "Central Park, New York, NY, USA"
                        ),
                        Destination(
                            query = trimmedQuery,
                            name = "Central Park, Sacramento",
                            latitude = 38.595371,
                            longitude = -121.428337,
                            formattedAddress = "Central Park, Sacramento, CA, USA"
                        )
                    )
                )
            }
            
            // Default: return valid destination with mock coordinates
            ValidationResult.Valid(
                Destination(
                    query = trimmedQuery,
                    name = trimmedQuery,
                    latitude = MOCK_LATITUDE,
                    longitude = MOCK_LONGITUDE,
                    formattedAddress = "$trimmedQuery (Mock Location)"
                )
            )
        }
    }
    
    /**
     * Get route from origin to destination.
     * 
     * Story 6.2: Google Maps Directions API integration
     * Story 6.1: Throws NotImplementedError (placeholder)
     */
    override suspend fun getRoute(
        origin: Destination,
        destination: Destination
    ): NavigationRoute {
        throw NotImplementedError("Route calculation will be implemented in Story 6.2")
    }
}
