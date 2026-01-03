package com.visionfocus.navigation.models

/**
 * Navigation state sealed class.
 * 
 * Story 6.1: Destination Input via Voice and Text
 * Used to track the current navigation workflow state.
 */
sealed class NavigationState {
    /** Initial state, no navigation active */
    object Idle : NavigationState()
    
    /** User is inputting destination */
    object InputtingDestination : NavigationState()
    
    /** Validating destination query */
    data class Validating(val query: String) : NavigationState()
    
    /** Active navigation with turn-by-turn guidance (Story 6.3) */
    data class NavigationActive(
        val destination: Destination,
        val currentStep: Int
    ) : NavigationState()
    
    /** Error state with message */
    data class Error(val message: String) : NavigationState()
}
