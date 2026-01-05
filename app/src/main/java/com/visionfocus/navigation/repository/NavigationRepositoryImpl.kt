package com.visionfocus.navigation.repository

import android.content.Context
import com.visionfocus.navigation.api.DirectionsApiService
import com.visionfocus.navigation.api.DirectionsError
import com.visionfocus.navigation.consent.NetworkConsentManager
import com.visionfocus.navigation.location.LocationError
import com.visionfocus.navigation.location.LocationManager
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.TravelMode
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.network.exceptions.NetworkUnavailableException
import com.visionfocus.network.monitor.NetworkStateMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigation repository implementation.
 * 
 * Story 6.1: Mock validation (no network calls, basic validation only)
 * Story 6.2: Google Maps Directions API integration (full implementation)
 * Story 6.6: Network state monitoring before API calls (graceful offline fallback)
 * 
 * @property context Application context
 * @property directionsApiService Google Maps Directions API client
 * @property geocodingApiService Google Maps Geocoding API client
 * @property locationManager GPS location provider
 * @property networkConsentManager Network consent checker
 * @property networkStateMonitor Network state monitor for connectivity checks
 */
@Singleton
class NavigationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directionsApiService: DirectionsApiService,
    private val geocodingApiService: com.visionfocus.navigation.api.GeocodingApiService,
    private val locationManager: LocationManager,
    private val networkConsentManager: NetworkConsentManager,
    private val networkStateMonitor: NetworkStateMonitor
) : NavigationRepository {
    
    companion object {
        private const val TAG = "NavigationRepositoryImpl"
        private const val MIN_QUERY_LENGTH = 3
    }
    
    /**
     * Destination validation using Google Maps Geocoding API.
     * 
     * Story 6.2 FIX: Now properly geocodes destination names to coordinates.
     * 
     * @param query User-entered destination
     * @return ValidationResult with real coordinates from Geocoding API
     */
    override suspend fun validateDestination(query: String): ValidationResult {
        return withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("Validating destination: $query")
            
            val trimmedQuery = query.trim()
            
            // Empty check
            if (trimmedQuery.isBlank()) {
                return@withContext ValidationResult.Empty
            }
            
            // Length check (minimum 3 characters)
            if (trimmedQuery.length < MIN_QUERY_LENGTH) {
                return@withContext ValidationResult.TooShort
            }
            
            // Use Google Geocoding API to convert destination name to coordinates
            val geocodeResult = geocodingApiService.geocode(trimmedQuery)
            
            if (geocodeResult.isSuccess) {
                val coordinates = geocodeResult.getOrNull()!!
                ValidationResult.Valid(
                    Destination(
                        query = trimmedQuery,
                        name = trimmedQuery,
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        formattedAddress = trimmedQuery
                    )
                )
            } else {
                // Geocoding failed - location not found
                ValidationResult.Invalid(
                    reason = geocodeResult.exceptionOrNull()?.message 
                        ?: "Location not found: $trimmedQuery"
                )
            }
        }
    }
    
    /**
     * Get route from current GPS location to destination.
     * 
     * Story 6.2: Google Maps Directions API integration with full error handling.
     * Story 6.6: Network state check before API call with graceful offline fallback.
     * CODE REVIEW FIX (Issue #3): Changed signature to accept only destination.
     * Origin is always current GPS location from LocationManager.
     * 
     * @param destination Ending location from user input
     * @return Result<NavigationRoute> with turn-by-turn steps or error
     */
    override suspend fun getRoute(
        destination: Destination
    ): Result<NavigationRoute> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.tag(TAG).d("Getting route to: ${destination.name}")
                
                // Step 1: Check network state (Story 6.6)
                if (!networkStateMonitor.getCurrentNetworkState()) {
                    Timber.tag(TAG).d("Network unavailable for live directions")
                    // TODO: Check offline map availability when Story 7.4 implemented
                    return@withContext Result.failure(
                        NetworkUnavailableException.noConnection(hasOfflineMaps = false)
                    )
                }
                
                // Step 2: Check network consent
                if (!networkConsentManager.hasConsent()) {
                    Timber.tag(TAG).d("Network consent required")
                    return@withContext Result.failure(
                        DirectionsError.ConsentRequired("Network consent required for live directions")
                    )
                }
                
                // Step 3: Get current GPS location as origin
                val currentLocation = locationManager.getCurrentLocation()
                if (currentLocation.isFailure) {
                    Timber.tag(TAG).e("Failed to get current location")
                    return@withContext Result.failure(
                        currentLocation.exceptionOrNull() ?: LocationError.Unknown("Unknown location error")
                    )
                }
                
                val originLatLng = currentLocation.getOrThrow()
                Timber.tag(TAG).d("Current location: $originLatLng")
                
                // Step 4: Call Directions API
                val destinationLatLng = LatLng(destination.latitude, destination.longitude)
                val routeResult = directionsApiService.getDirections(
                    origin = originLatLng,
                    destination = destinationLatLng,
                    travelMode = TravelMode.WALKING  // Default walking mode for accessibility
                )
                
                if (routeResult.isFailure) {
                    Timber.tag(TAG).e("Directions API failed", routeResult.exceptionOrNull())
                    return@withContext routeResult
                }
                
                val route = routeResult.getOrThrow()
                Timber.tag(TAG).d("Route received: ${route.steps.size} steps, ${route.totalDistance}m, ${route.totalDuration}s")
                
                // Story 6.3 will use this route for turn-by-turn guidance
                Result.success(route)
                
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected error in getRoute")
                Result.failure(DirectionsError.Unknown("Route calculation failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Recalculates route from current location to original destination.
     * 
     * Story 6.4: Reuses existing DirectionsApiService from Story 6.2.
     * Story 6.6: Network state check before recalculation API call.
     * Called when user deviates from route (>20m for 5 seconds).
     * 
     * @param origin Current GPS location (where user deviated)
     * @param destination Original destination (unchanged)
     * @return Result<NavigationRoute> with new route or error
     */
    override suspend fun recalculateRoute(
        origin: LatLng,
        destination: Destination
    ): Result<NavigationRoute> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.tag(TAG).d("Recalculating route from $origin to ${destination.name}")
                
                // Check network state before recalculation (Story 6.6)
                if (!networkStateMonitor.getCurrentNetworkState()) {
                    Timber.tag(TAG).d("Network unavailable for recalculation")
                    // TODO: Fall back to offline maps when Story 7.4 implemented
                    return@withContext Result.failure(
                        NetworkUnavailableException.noConnection(hasOfflineMaps = false)
                    )
                }
                
                // Check network consent (reuse Story 6.2 logic)
                if (!networkConsentManager.hasConsent()) {
                    Timber.tag(TAG).d("Network consent required for recalculation")
                    return@withContext Result.failure(
                        DirectionsError.ConsentRequired("Network consent required for recalculation")
                    )
                }
                
                // Call Google Maps Directions API (reuse Story 6.2 service)
                val destinationLatLng = LatLng(destination.latitude, destination.longitude)
                val routeResult = directionsApiService.getDirections(
                    origin = origin,
                    destination = destinationLatLng,
                    travelMode = TravelMode.WALKING  // Story 6.4: Walking mode only (transit in future)
                )
                
                if (routeResult.isFailure) {
                    Timber.tag(TAG).e("Recalculation failed", routeResult.exceptionOrNull())
                    return@withContext routeResult
                }
                
                val route = routeResult.getOrThrow()
                Timber.tag(TAG).d("Route recalculated: ${route.totalDistance}m, ${route.totalDuration}s, ${route.steps.size} steps")
                
                Result.success(route)
                
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Recalculation error")
                Result.failure(DirectionsError.Unknown("Recalculation failed: ${e.message}"))
            }
        }
    }
}
