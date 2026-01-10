package com.visionfocus.navigation.models

/**
 * Story 6.3: Real-time navigation progress tracking.
 * Story 7.5: Added navigation mode and offline capability tracking.
 * 
 * Updated each GPS location update (1Hz) during active navigation.
 * Contains current step, distance remaining, estimated time, and warning state.
 * 
 * @property currentStepIndex Index in route.steps (0-based)
 * @property currentLocation Current GPS location of user
 * @property distanceToCurrentStep Meters remaining to current step
 * @property totalDistanceRemaining Meters remaining in entire route
 * @property estimatedTimeRemaining Seconds estimated to destination
 * @property hasGivenAdvanceWarning True if "In X meters, turn..." already announced
 * @property hasGivenImmediateWarning True if "Turn now" already announced
 * @property hasCompletedCurrentStep True if user passed current step location
 * @property bearingToNextStep Degrees from north (0-360) to next step
 * @property navigationMode Current navigation mode (online/offline/unavailable)
 * @property isOfflineMapAvailable Whether offline maps available for destination
 * @property canRecalculate Whether route recalculation is possible (false when offline)
 */
data class NavigationProgress(
    val currentStepIndex: Int,
    val currentLocation: LatLng? = null,       // Story 7.5: Track current location
    val distanceToCurrentStep: Float,          // meters
    val totalDistanceRemaining: Float,         // meters
    val estimatedTimeRemaining: Int,           // seconds
    val hasGivenAdvanceWarning: Boolean = false,
    val hasGivenImmediateWarning: Boolean = false,
    val hasCompletedCurrentStep: Boolean = false,
    val bearingToNextStep: Float? = null,      // degrees from north
    // Story 7.5 Task 13: Mode tracking fields
    val navigationMode: NavigationMode = NavigationMode.ONLINE,
    val isOfflineMapAvailable: Boolean = false,
    val canRecalculate: Boolean = true         // Derived from mode (false when OFFLINE)
)
