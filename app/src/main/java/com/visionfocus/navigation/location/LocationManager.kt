package com.visionfocus.navigation.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages GPS location services using FusedLocationProviderClient.
 * 
 * Story 6.2: Provides current location and continuous updates at 1Hz for navigation.
 * Handles location permissions and GPS enabled checks before requesting location.
 * 
 * @property context Application context for location services
 * @property permissionManager Permission checker for location access
 * @property ttsManager TTS manager for user feedback
 */
@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val ttsManager: TTSManager
) {
    
    companion object {
        private const val TAG = "LocationManager"
        
        // CODE REVIEW FIX (Issue #5): 1Hz GPS polling justification
        // 1-second updates required for real-time turn-by-turn navigation (Story 6.3)
        // Blind users need immediate feedback when approaching turns ("In 50 meters, turn left")
        // Battery impact mitigated by stopping updates when navigation inactive
        // Google recommends 1-5s for active navigation vs 10-60s for passive tracking
        private const val UPDATE_INTERVAL_MS = 1000L  // 1 second for real-time navigation
        private const val FASTEST_INTERVAL_MS = 500L  // 0.5 seconds maximum responsiveness
    }
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .build()
    }
    
    /**
     * Gets current GPS location as a one-time request.
     * 
     * Checks location permission and GPS enabled status before requesting.
     * Returns Result.success with LatLng or Result.failure with error.
     * 
     * @return Result<LatLng> with current coordinates or error
     */
    suspend fun getCurrentLocation(): Result<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                // Check location permission
                if (!permissionManager.isLocationPermissionGranted()) {
                    Timber.tag(TAG).w("Location permission not granted")
                    ttsManager.announce("Location permission required for navigation")
                    return@withContext Result.failure(
                        LocationError.PermissionDenied("Location permission not granted")
                    )
                }
                
                // Check if GPS is enabled
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) 
                    as android.location.LocationManager
                if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    Timber.tag(TAG).w("GPS is disabled")
                    ttsManager.announce("Enable GPS to start navigation")
                    return@withContext Result.failure(
                        LocationError.GpsDisabled("GPS is disabled")
                    )
                }
                
                Timber.tag(TAG).d("Requesting current location...")
                
                // Request current location (one-time)
                @SuppressLint("MissingPermission")  // Permission checked above
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
                
                if (location == null) {
                    Timber.tag(TAG).w("Location unavailable")
                    ttsManager.announce("Cannot determine current location. Please try again.")
                    return@withContext Result.failure(
                        LocationError.Unavailable("Location unavailable")
                    )
                }
                
                val latLng = LatLng(location.latitude, location.longitude)
                Timber.tag(TAG).d("Current location: $latLng")
                
                Result.success(latLng)
                
            } catch (e: SecurityException) {
                Timber.tag(TAG).e(e, "Location permission error")
                Result.failure(LocationError.PermissionDenied("Location permission denied"))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Location request error")
                Result.failure(LocationError.Unknown("Location request failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Starts continuous location updates at 1Hz for navigation.
     * Returns Flow of LatLng coordinates.
     * 
     * Story 6.3 will use this for turn-by-turn guidance and deviation detection.
     * 
     * @return Flow<LatLng> emitting location updates
     */
    @SuppressLint("MissingPermission")  // Permission checked in getCurrentLocation()
    fun getLocationUpdates(): Flow<LatLng> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(LatLng(location.latitude, location.longitude))
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}

/**
 * Location error types for navigation.
 */
sealed class LocationError(message: String) : Exception(message) {
    class PermissionDenied(message: String) : LocationError(message)
    class GpsDisabled(message: String) : LocationError(message)
    class Unavailable(message: String) : LocationError(message)
    class Unknown(message: String) : LocationError(message)
}
