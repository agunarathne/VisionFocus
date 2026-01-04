package com.visionfocus.navigation.service

import android.util.Log
import com.visionfocus.navigation.models.Maneuver
import com.visionfocus.navigation.models.NavigationProgress
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.TurnWarning
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 6.3 Task 5: TurnWarningCalculator for anticipatory warnings.
 * 
 * Checks navigation progress and determines if a turn warning should be announced.
 * Implements 5-7 second advance warning system based on walking speed.
 * 
 * Warning thresholds:
 * - Advance warning: 70m before turn (5-7 seconds at 1.4 m/s walking speed)
 * - Immediate warning: 15m before turn (turn now)
 * - Arrival: 10m from destination
 * - Straight checkpoint: Every 200m on long straight sections
 */
@Singleton
class TurnWarningCalculator @Inject constructor() {
    
    companion object {
        private const val TAG = "TurnWarningCalculator"
        
        /** Walking speed: ~1.4 m/s → 70m ≈ 50 seconds (5-7 second warning buffer) */
        private const val ADVANCE_WARNING_DISTANCE = 70f  // meters
        
        /** Immediate turn warning threshold */
        private const val IMMEDIATE_WARNING_DISTANCE = 15f // meters
        
        /** Arrival threshold (destination reached) */
        private const val ARRIVAL_THRESHOLD = 10f // meters
        
        /** Straight section checkpoint distance */
        private const val CHECKPOINT_DISTANCE = 200f // meters
    }
    
    /**
     * Check if a turn warning should be announced.
     * 
     * Logic:
     * 1. Check arrival first (highest priority)
     * 2. Check advance warning (5-7 seconds before turn)
     * 3. Check immediate warning (turn now)
     * 4. Check straight checkpoint (every 200m on long straights)
     * 
     * @param progress Current navigation progress
     * @param route Complete navigation route
     * @return TurnWarning if announcement needed, null otherwise
     */
    fun checkForWarning(
        progress: NavigationProgress,
        route: NavigationRoute
    ): TurnWarning? {
        // Check arrival first (highest priority)
        if (progress.totalDistanceRemaining <= ARRIVAL_THRESHOLD) {
            Log.d(TAG, "Arrival detected: ${progress.totalDistanceRemaining.toInt()}m remaining")
            return TurnWarning.Arrival
        }
        
        // Get current step
        val currentStep = route.steps.getOrNull(progress.currentStepIndex)
        if (currentStep == null) {
            Log.w(TAG, "No current step found at index ${progress.currentStepIndex}")
            return null
        }
        
        val distance = progress.distanceToCurrentStep
        
        // Advance warning (5-7 seconds before turn)
        if (distance <= ADVANCE_WARNING_DISTANCE && !progress.hasGivenAdvanceWarning) {
            Log.d(TAG, "Advance warning: ${distance.toInt()}m to turn, maneuver=${currentStep.maneuver}")
            return TurnWarning.Advance(currentStep, distance.toInt())
        }
        
        // Immediate warning (turn now)
        if (distance <= IMMEDIATE_WARNING_DISTANCE && !progress.hasGivenImmediateWarning) {
            Log.d(TAG, "Immediate warning: ${distance.toInt()}m to turn, maneuver=${currentStep.maneuver}")
            return TurnWarning.Immediate(currentStep)
        }
        
        // Straight section checkpoint (every 200m on long straight sections)
        if (currentStep.maneuver == Maneuver.STRAIGHT && distance > CHECKPOINT_DISTANCE) {
            // Only announce checkpoints if not already close to a turn
            if (distance > ADVANCE_WARNING_DISTANCE) {
                Log.d(TAG, "Straight checkpoint: ${distance.toInt()}m to continue straight")
                return TurnWarning.Checkpoint(distance.toInt())
            }
        }
        
        // No warning needed
        return null
    }
}
