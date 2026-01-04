package com.visionfocus.navigation.models

/**
 * Represents current deviation status from planned route.
 * 
 * Story 6.4: Used by DeviationDetector to classify GPS position relative to route.
 * Tracks consecutive deviation to filter GPS jitter and prevent false positives.
 */
sealed class DeviationState {
    /**
     * User is on route (within 20m threshold).
     * 
     * @property distanceFromRoute Distance in meters from current route segment
     */
    data class OnRoute(val distanceFromRoute: Float) : DeviationState()
    
    /**
     * User near route edge (15-20m warning zone).
     * 
     * Prevents immediate recalculation for temporary GPS drift near threshold.
     * Allows grace period for GPS accuracy improvement.
     * 
     * @property distanceFromRoute Distance in meters from current route segment
     */
    data class NearEdge(val distanceFromRoute: Float) : DeviationState()
    
    /**
     * User off route (>20m for 5 consecutive seconds).
     * 
     * Triggers recalculation flow when deviation persists.
     * 
     * @property distanceFromRoute Distance in meters from current route segment
     * @property consecutiveCount Number of consecutive GPS updates showing deviation
     */
    data class OffRoute(
        val distanceFromRoute: Float,
        val consecutiveCount: Int
    ) : DeviationState()
    
    companion object {
        const val DEVIATION_THRESHOLD = 20f  // meters
        const val NEAR_EDGE_THRESHOLD = 15f  // meters
        const val CONSECUTIVE_REQUIRED = 5   // GPS updates (5 seconds at 1Hz)
    }
}
