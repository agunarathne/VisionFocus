package com.visionfocus.navigation.models

/**
 * Story 6.3: Real-time navigation progress tracking.
 * 
 * Updated each GPS location update (1Hz) during active navigation.
 * Contains current step, distance remaining, estimated time, and warning state.
 * 
 * @property currentStepIndex Index in route.steps (0-based)
 * @property distanceToCurrentStep Meters remaining to current step
 * @property totalDistanceRemaining Meters remaining in entire route
 * @property estimatedTimeRemaining Seconds estimated to destination
 * @property hasGivenAdvanceWarning True if "In X meters, turn..." already announced
 * @property hasGivenImmediateWarning True if "Turn now" already announced
 * @property hasCompletedCurrentStep True if user passed current step location
 * @property bearingToNextStep Degrees from north (0-360) to next step
 */
data class NavigationProgress(
    val currentStepIndex: Int,
    val distanceToCurrentStep: Float,          // meters
    val totalDistanceRemaining: Float,         // meters
    val estimatedTimeRemaining: Int,           // seconds
    val hasGivenAdvanceWarning: Boolean = false,
    val hasGivenImmediateWarning: Boolean = false,
    val hasCompletedCurrentStep: Boolean = false,
    val bearingToNextStep: Float? = null       // degrees from north
)
