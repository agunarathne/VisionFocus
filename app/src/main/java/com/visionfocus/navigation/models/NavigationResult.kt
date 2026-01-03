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

// NavigationRoute moved to its own file: NavigationRoute.kt
// This placeholder is no longer needed - Story 6.2 implemented full model
