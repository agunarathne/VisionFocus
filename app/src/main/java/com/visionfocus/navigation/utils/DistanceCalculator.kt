package com.visionfocus.navigation.utils

import com.visionfocus.navigation.models.LatLng
import kotlin.math.*

/**
 * Story 6.3 Task 7: Haversine distance calculator.
 * 
 * Calculates great-circle distance between two GPS coordinates
 * using the Haversine formula. Used for route following and
 * turn warning distance calculations.
 * 
 * Formula:
 * a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
 * c = 2 ⋅ atan2(√a, √(1−a))
 * d = R ⋅ c
 * 
 * where φ is latitude, λ is longitude, R is earth's radius
 */
object DistanceCalculator {
    
    private const val TAG = "DistanceCalculator"
    
    /** Earth's radius in meters (mean radius) */
    private const val EARTH_RADIUS_METERS = 6371000.0
    
    /**
     * Calculate distance between two GPS coordinates.
     * 
     * @param from Starting location
     * @param to Ending location
     * @return Distance in meters
     */
    fun calculateDistance(from: LatLng, to: LatLng): Float {
        return calculateDistance(
            from.latitude, from.longitude,
            to.latitude, to.longitude
        )
    }
    
    /**
     * Calculate distance between two GPS coordinates.
     * 
     * @param lat1 Starting latitude in degrees
     * @param lon1 Starting longitude in degrees
     * @param lat2 Ending latitude in degrees
     * @param lon2 Ending longitude in degrees
     * @return Distance in meters
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        // Convert degrees to radians
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        
        // Haversine formula
        val a = sin(deltaPhi / 2).pow(2) +
                cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        val distanceMeters = EARTH_RADIUS_METERS * c
        
        return distanceMeters.toFloat()
    }
}
