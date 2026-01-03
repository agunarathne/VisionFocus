package com.visionfocus.navigation.models

/**
 * Result of destination validation.
 * 
 * Story 6.1: Destination Input via Voice and Text
 * Used to communicate validation outcomes from repository to UI.
 */
sealed class ValidationResult {
    /** Empty input field */
    object Empty : ValidationResult()
    
    /** Input too short (< 3 characters) */
    object TooShort : ValidationResult()
    
    /** Valid destination with geocoded coordinates */
    data class Valid(val destination: Destination) : ValidationResult()
    
    /** Multiple matching locations found */
    data class Ambiguous(val options: List<Destination>) : ValidationResult()
    
    /** Invalid destination with reason */
    data class Invalid(val reason: String) : ValidationResult()
}

/**
 * Navigation route from origin to destination.
 * 
 * Story 6.2: Google Maps Directions API Integration
 * Placeholder for turn-by-turn directions.
 */
data class NavigationRoute(
    val origin: Destination,
    val destination: Destination,
    val waypoints: List<Destination> = emptyList(),
    val distanceMeters: Int = 0,
    val durationSeconds: Int = 0
)
