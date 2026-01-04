package com.visionfocus.navigation.service

import android.util.Log
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationProgress
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.utils.BearingCalculator
import com.visionfocus.navigation.utils.DistanceCalculator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 6.3 Task 4: RouteFollower for progress calculations.
 * 
 * Tracks user's position along navigation route and calculates:
 * - Distance to next step
 * - Total distance remaining
 * - Estimated time remaining
 * - Current step index
 * - Warning flags for TTS announcements
 * 
 * Updated on each GPS location update (1Hz).
 */
@Singleton
class RouteFollower @Inject constructor() {
    
    companion object {
        private const val TAG = "RouteFollower"
        
        /** Walking speed assumption: ~1.4 m/s (~3.1 mph, ~5 km/h) */
        private const val WALKING_SPEED_MPS = 1.4f
        
        /** Distance threshold to consider a step completed (meters) */
        private const val STEP_COMPLETION_THRESHOLD = 20f
    }
    
    /**
     * Calculate navigation progress from current GPS location.
     * 
     * @param currentLocation User's current GPS coordinates
     * @param route Complete navigation route
     * @return NavigationProgress with current step, distances, time, warnings
     */
    fun calculateProgress(
        currentLocation: LatLng,
        route: NavigationRoute,
        previousProgress: NavigationProgress? = null
    ): NavigationProgress {
        // Determine current step index
        val currentStepIndex = determineCurrentStep(currentLocation, route, previousProgress)
        
        if (currentStepIndex >= route.steps.size) {
            // Arrived at destination
            return NavigationProgress(
                currentStepIndex = route.steps.size - 1,
                distanceToCurrentStep = 0f,
                totalDistanceRemaining = 0f,
                estimatedTimeRemaining = 0,
                hasGivenAdvanceWarning = true,
                hasGivenImmediateWarning = true,
                hasCompletedCurrentStep = true
            )
        }
        
        val currentStep = route.steps[currentStepIndex]
        
        // Calculate distance to current step's end location
        val distanceToCurrentStep = DistanceCalculator.calculateDistance(
            currentLocation,
            currentStep.endLocation
        )
        
        // Calculate total distance remaining (current step + all remaining steps)
        var totalDistanceRemaining = distanceToCurrentStep
        for (i in (currentStepIndex + 1) until route.steps.size) {
            totalDistanceRemaining += route.steps[i].distance
        }
        
        // Estimate time remaining based on walking speed
        val estimatedTimeRemaining = (totalDistanceRemaining / WALKING_SPEED_MPS).toInt()
        
        // Calculate bearing to next step
        val bearingToNextStep = BearingCalculator.calculateBearing(
            currentLocation,
            currentStep.endLocation
        )
        
        // Check if current step is completed (user passed endLocation)
        val hasCompletedCurrentStep = distanceToCurrentStep <= STEP_COMPLETION_THRESHOLD
        
        // Preserve warning flags if on same step, reset if new step
        val isSameStep = previousProgress?.currentStepIndex == currentStepIndex
        val hasGivenAdvanceWarning = if (isSameStep) {
            previousProgress?.hasGivenAdvanceWarning ?: false
        } else {
            false
        }
        val hasGivenImmediateWarning = if (isSameStep) {
            previousProgress?.hasGivenImmediateWarning ?: false
        } else {
            false
        }
        
        Log.d(TAG, "Progress: Step $currentStepIndex/${route.steps.size}, " +
                "distanceToStep=${distanceToCurrentStep.toInt()}m, " +
                "totalRemaining=${totalDistanceRemaining.toInt()}m, " +
                "timeRemaining=${estimatedTimeRemaining}s")
        
        return NavigationProgress(
            currentStepIndex = currentStepIndex,
            distanceToCurrentStep = distanceToCurrentStep,
            totalDistanceRemaining = totalDistanceRemaining,
            estimatedTimeRemaining = estimatedTimeRemaining,
            hasGivenAdvanceWarning = hasGivenAdvanceWarning,
            hasGivenImmediateWarning = hasGivenImmediateWarning,
            hasCompletedCurrentStep = hasCompletedCurrentStep,
            bearingToNextStep = bearingToNextStep
        )
    }
    
    /**
     * Determine which route step the user is currently on.
     * 
     * Logic:
     * 1. If previous step completed, advance to next step
     * 2. Otherwise, find closest step based on distance
     * 
     * @param currentLocation User's GPS location
     * @param route Complete route
     * @param previousProgress Previous progress state (optional)
     * @return Current step index (0-based)
     */
    private fun determineCurrentStep(
        currentLocation: LatLng,
        route: NavigationRoute,
        previousProgress: NavigationProgress?
    ): Int {
        // If previous step was completed, advance to next step
        if (previousProgress != null && previousProgress.hasCompletedCurrentStep) {
            val nextStep = previousProgress.currentStepIndex + 1
            if (nextStep < route.steps.size) {
                Log.d(TAG, "Step completed, advancing to step $nextStep")
                return nextStep
            }
        }
        
        // If no previous progress, start from first step
        if (previousProgress == null) {
            return 0
        }
        
        // Otherwise, stay on current step (or find closest if something went wrong)
        val currentIndex = previousProgress.currentStepIndex
        
        // Validate current index is still valid
        return if (currentIndex < route.steps.size) {
            currentIndex
        } else {
            // Fallback: find closest step
            findClosestStep(currentLocation, route)
        }
    }
    
    /**
     * Find the closest route step to current location.
     * 
     * Used as fallback if step tracking gets out of sync.
     * 
     * @return Index of closest step
     */
    private fun findClosestStep(currentLocation: LatLng, route: NavigationRoute): Int {
        var closestIndex = 0
        var closestDistance = Float.MAX_VALUE
        
        route.steps.forEachIndexed { index, step ->
            val distance = DistanceCalculator.calculateDistance(currentLocation, step.endLocation)
            if (distance < closestDistance) {
                closestDistance = distance
                closestIndex = index
            }
        }
        
        Log.d(TAG, "Finding closest step: Step $closestIndex at ${closestDistance.toInt()}m")
        return closestIndex
    }
    
    /**
     * Mark advance warning as given for current progress.
     * 
     * @return Updated progress with advance warning flag set
     */
    fun markAdvanceWarningGiven(progress: NavigationProgress): NavigationProgress {
        return progress.copy(hasGivenAdvanceWarning = true)
    }
    
    /**
     * Mark immediate warning as given for current progress.
     * 
     * @return Updated progress with immediate warning flag set
     */
    fun markImmediateWarningGiven(progress: NavigationProgress): NavigationProgress {
        return progress.copy(hasGivenImmediateWarning = true)
    }
}
