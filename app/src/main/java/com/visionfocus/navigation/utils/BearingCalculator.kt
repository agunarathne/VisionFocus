package com.visionfocus.navigation.utils

import com.visionfocus.navigation.models.LatLng
import kotlin.math.*

/**
 * Story 6.3 Task 8: Bearing calculator for cardinal directions.
 * 
 * Calculates bearing (direction) from one GPS coordinate to another.
 * Converts bearing to cardinal directions (N, NE, E, SE, S, SW, W, NW)
 * for natural language navigation instructions.
 * 
 * Bearing is measured in degrees clockwise from north (0-360).
 */
object BearingCalculator {
    
    private const val TAG = "BearingCalculator"
    
    /**
     * Calculate bearing from one location to another.
     * 
     * @param from Starting location
     * @param to Destination location
     * @return Bearing in degrees (0-360), where 0 = North, 90 = East, 180 = South, 270 = West
     */
    fun calculateBearing(from: LatLng, to: LatLng): Float {
        return calculateBearing(
            from.latitude, from.longitude,
            to.latitude, to.longitude
        )
    }
    
    /**
     * Calculate bearing from one coordinate to another.
     * 
     * Formula:
     * θ = atan2(sin Δλ ⋅ cos φ2, cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ)
     * 
     * @param lat1 Starting latitude in degrees
     * @param lon1 Starting longitude in degrees
     * @param lat2 Ending latitude in degrees
     * @param lon2 Ending longitude in degrees
     * @return Bearing in degrees (0-360)
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        // Convert to radians
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        
        // Calculate bearing
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        
        val bearingRadians = atan2(y, x)
        val bearingDegrees = Math.toDegrees(bearingRadians)
        
        // Normalize to 0-360
        val normalizedBearing = (bearingDegrees + 360) % 360
        
        return normalizedBearing.toFloat()
    }
    
    /**
     * Convert bearing to cardinal direction string.
     * 
     * @param bearing Bearing in degrees (0-360)
     * @return Cardinal direction: "North", "Northeast", "East", "Southeast", 
     *         "South", "Southwest", "West", or "Northwest"
     */
    fun toCardinalDirection(bearing: Float): String {
        return when {
            bearing < 22.5 || bearing >= 337.5 -> "North"
            bearing < 67.5 -> "Northeast"
            bearing < 112.5 -> "East"
            bearing < 157.5 -> "Southeast"
            bearing < 202.5 -> "South"
            bearing < 247.5 -> "Southwest"
            bearing < 292.5 -> "West"
            bearing < 337.5 -> "Northwest"
            else -> "North"  // Fallback (should never reach)
        }
    }
    
    /**
     * Add cardinal direction to instruction when helpful.
     * 
     * Example: "Turn left" + bearing 45° → "Turn left heading Northeast"
     * 
     * @param instruction Base instruction text
     * @param bearing Bearing in degrees (0-360)
     * @return Instruction with cardinal direction appended if helpful
     */
    fun enhanceInstructionWithCardinal(instruction: String, bearing: Float?): String {
        if (bearing == null) return instruction
        
        val cardinal = toCardinalDirection(bearing)
        
        // Add cardinal direction for turn instructions (helpful context)
        return if (instruction.contains("turn", ignoreCase = true)) {
            "$instruction heading $cardinal"
        } else {
            instruction
        }
    }
}
