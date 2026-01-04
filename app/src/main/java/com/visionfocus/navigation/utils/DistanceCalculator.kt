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
    
    /**
     * Calculates perpendicular distance from GPS point to route line segment.
     * 
     * Story 6.4: Used by DeviationDetector to determine if user is on route.
     * Implements point-to-line distance using vector projection + Haversine formula.
     * 
     * Algorithm:
     * 1. Project point onto line segment using vector dot product
     * 2. Find closest point on line (may be start, end, or midpoint)
     * 3. Calculate Haversine distance from point to closest point on line
     * 
     * Edge Cases:
     * - Point before line start: distance to start point
     * - Point after line end: distance to end point
     * - Point perpendicular to line: true perpendicular distance
     * 
     * @param point GPS coordinates to check (current user location)
     * @param lineStart Start of route segment (step.startLocation)
     * @param lineEnd End of route segment (step.endLocation)
     * @return Distance in meters from point to closest point on line
     */
    fun calculatePerpendicularDistance(
        point: LatLng,
        lineStart: LatLng,
        lineEnd: LatLng
    ): Float {
        // Convert lat/lon to radians for vector calculations
        val pointLat = Math.toRadians(point.latitude)
        val pointLon = Math.toRadians(point.longitude)
        val startLat = Math.toRadians(lineStart.latitude)
        val startLon = Math.toRadians(lineStart.longitude)
        val endLat = Math.toRadians(lineEnd.latitude)
        val endLon = Math.toRadians(lineEnd.longitude)
        
        // Calculate line vector components (approximate on sphere)
        val lineDeltaLat = endLat - startLat
        val lineDeltaLon = (endLon - startLon) * cos((startLat + endLat) / 2)
        val lineLengthSquared = lineDeltaLat * lineDeltaLat + lineDeltaLon * lineDeltaLon
        
        // Handle degenerate case: line start and end are same point
        if (lineLengthSquared < 1e-10) {
            return calculateDistance(point, lineStart)
        }
        
        // Calculate point vector from line start
        val pointDeltaLat = pointLat - startLat
        val pointDeltaLon = (pointLon - startLon) * cos((startLat + pointLat) / 2)
        
        // Project point onto line (dot product normalized by line length)
        val t = ((pointDeltaLat * lineDeltaLat + pointDeltaLon * lineDeltaLon) / lineLengthSquared)
            .coerceIn(0.0, 1.0)  // Clamp to [0, 1] to stay within line segment
        
        // Calculate closest point on line
        // Note: No cosine adjustment on longitude interpolation since lineDeltaLon already accounts for it
        val closestLat = startLat + t * lineDeltaLat
        val closestLon = startLon + t * (endLon - startLon)
        
        // Calculate Haversine distance from point to closest point on line
        val closestLatDeg = Math.toDegrees(closestLat)
        val closestLonDeg = Math.toDegrees(closestLon)
        
        return calculateDistance(point.latitude, point.longitude, closestLatDeg, closestLonDeg)
    }
}
