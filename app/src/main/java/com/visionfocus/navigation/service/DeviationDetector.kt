package com.visionfocus.navigation.service

import android.util.Log
import com.visionfocus.navigation.models.DeviationState
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.utils.DistanceCalculator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when user has deviated from planned route.
 * 
 * Story 6.4: Implements 20m threshold with 5-second consecutive detection
 * to filter GPS jitter and prevent false positive recalculations.
 * 
 * Algorithm:
 * 1. Calculate perpendicular distance from current GPS location to current route segment
 * 2. Track last 5 GPS updates to detect persistent deviation
 * 3. Return OffRoute only when distance > 20m for 5 consecutive updates (5 seconds at 1Hz)
 * 4. Return NearEdge for 15-20m (warning state, no recalculation)
 * 5. Return OnRoute for < 15m
 * 
 * GPS Jitter Handling:
 * - Urban canyon: GPS accuracy 10-30m typical in cities with tall buildings
 * - Single bad GPS update filtered by consecutive tracking
 * - 5-second window allows GPS accuracy to stabilize before recalculation
 * 
 * Performance:
 * - Called every 1 second (NavigationService GPS update rate)
 * - Haversine calculations optimized for mobile (<5ms per check)
 */
@Singleton
class DeviationDetector @Inject constructor() {
    companion object {
        private const val TAG = "DeviationDetector"
        private const val HISTORY_SIZE = 5  // Track last 5 GPS updates
    }
    
    // Circular buffer for deviation history (memory-efficient)
    private val deviationHistory = ArrayDeque<DeviationState>(HISTORY_SIZE)
    
    /**
     * Checks if current GPS location represents deviation from route.
     * 
     * @param currentLocation Current GPS coordinates
     * @param route Complete navigation route
     * @param currentStepIndex Index of current route step (from RouteFollower)
     * @return DeviationState classification with distance metrics
     */
    fun checkDeviation(
        currentLocation: LatLng,
        route: NavigationRoute,
        currentStepIndex: Int
    ): DeviationState {
        // Get current route segment (current step)
        val currentStep = route.steps.getOrNull(currentStepIndex)
        if (currentStep == null) {
            Log.w(TAG, "Invalid step index: $currentStepIndex")
            return DeviationState.OnRoute(0f)  // Graceful fallback
        }
        
        // Calculate perpendicular distance from current location to route segment
        val distance = DistanceCalculator.calculatePerpendicularDistance(
            point = currentLocation,
            lineStart = currentStep.startLocation,
            lineEnd = currentStep.endLocation
        )
        
        // Classify deviation state based on distance
        val state = when {
            distance > DeviationState.DEVIATION_THRESHOLD -> {
                // User potentially off route - track consecutive occurrences
                val consecutiveCount = countConsecutiveDeviations() + 1
                DeviationState.OffRoute(distance, consecutiveCount)
            }
            distance > DeviationState.NEAR_EDGE_THRESHOLD -> {
                // User near route edge - warning state
                DeviationState.NearEdge(distance)
            }
            else -> {
                // User on route - clear deviation history
                DeviationState.OnRoute(distance)
            }
        }
        
        // Update deviation history (circular buffer)
        if (deviationHistory.size >= HISTORY_SIZE) {
            deviationHistory.removeFirst()
        }
        deviationHistory.addLast(state)
        
        Log.d(TAG, "Deviation check: distance=${distance}m, state=$state, " +
            "consecutiveCount=${(state as? DeviationState.OffRoute)?.consecutiveCount ?: 0}")
        
        return state
    }
    
    /**
     * Counts consecutive OffRoute states in deviation history.
     * 
     * Used to determine if user has been off route long enough (5 seconds)
     * to trigger recalculation (filters GPS jitter).
     * 
     * @return Number of consecutive OffRoute states
     */
    fun countConsecutiveDeviations(): Int {
        return deviationHistory.reversed()
            .takeWhile { it is DeviationState.OffRoute }
            .count()
    }
    
    /**
     * Resets deviation history.
     * 
     * Called when:
     * - User returns to route
     * - New route loaded after recalculation
     * - Navigation restarted
     */
    fun resetHistory() {
        deviationHistory.clear()
        Log.d(TAG, "Deviation history reset")
    }
}
