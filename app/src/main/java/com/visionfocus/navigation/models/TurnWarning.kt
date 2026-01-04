package com.visionfocus.navigation.models

/**
 * Story 6.3: Turn warning states for audio announcements.
 * 
 * Sealed class representing different types of navigation warnings
 * that trigger TTS announcements during active navigation.
 */
sealed class TurnWarning {
    /**
     * Advance warning: "In 50 meters, turn left onto Main Street"
     * Triggered 5-7 seconds before turn (70m at walking speed ~1.4 m/s)
     */
    data class Advance(val step: RouteStep, val distanceMeters: Int) : TurnWarning()
    
    /**
     * Immediate warning: "Turn left now onto Main Street"
     * Triggered at turn point (15m threshold)
     */
    data class Immediate(val step: RouteStep) : TurnWarning()
    
    /**
     * Straight section checkpoint: "Continue straight for 200 meters"
     * Triggered every 200m on long straight sections
     */
    data class Checkpoint(val distanceMeters: Int) : TurnWarning()
    
    /**
     * Arrival: "You have arrived at your destination"
     * Triggered when within 10m of destination
     */
    data object Arrival : TurnWarning()
}
