package com.visionfocus.ui.savedlocations

/**
 * UI state for SavedLocationsFragment.
 * Story 7.2: Type-safe state management with sealed class.
 */
sealed class SavedLocationsUiState {
    object Loading : SavedLocationsUiState()
    data class Success(val locations: List<SavedLocationUiModel>) : SavedLocationsUiState()
    object Empty : SavedLocationsUiState()
    data class Error(val message: String) : SavedLocationsUiState()
}

/**
 * One-time events for SavedLocationsFragment.
 * Story 7.2: Events for TalkBack announcements and user feedback.
 */
sealed class SavedLocationsEvent {
    data class NavigationReady(
        val route: com.visionfocus.navigation.models.NavigationRoute,
        val destinationName: String
    ) : SavedLocationsEvent()
    data class NavigationStarted(val locationName: String) : SavedLocationsEvent()
    data class LocationUpdated(val locationName: String) : SavedLocationsEvent()
    data class LocationDeleted(val locationName: String) : SavedLocationsEvent()
    data class Error(val message: String) : SavedLocationsEvent()
}
