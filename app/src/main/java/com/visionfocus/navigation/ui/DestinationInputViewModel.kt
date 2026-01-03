package com.visionfocus.navigation.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.navigation.api.DirectionsError
import com.visionfocus.navigation.consent.NetworkConsentManager
import com.visionfocus.navigation.location.LocationError
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.navigation.repository.NavigationRepository
import com.visionfocus.navigation.validation.DestinationValidator
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for destination input screen.
 * 
 * Story 6.1: Destination Input via Voice and Text
 * Story 6.2: Google Maps Directions API integration with navigation state management
 */
@HiltViewModel
class DestinationInputViewModel @Inject constructor(
    private val destinationValidator: DestinationValidator,
    private val navigationRepository: NavigationRepository,
    private val networkConsentManager: NetworkConsentManager,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "DestinationInputViewModel"
    }
    
    val destinationText = MutableLiveData<String>("")
    
    private val _validationState = MutableStateFlow<ValidationResult>(ValidationResult.Empty)
    val validationState: StateFlow<ValidationResult> = _validationState.asStateFlow()
    
    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()
    
    // Story 6.2: Navigation state for route downloading
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()
    
    fun onVoiceInputComplete(transcribedText: String) {
        destinationText.value = transcribedText
        
        viewModelScope.launch {
            ttsManager.announce("You said: $transcribedText")
        }
        
        validateDestination(transcribedText)
    }
    
    fun validateDestination(query: String) {
        val trimmedQuery = query.trim()
        
        if (trimmedQuery.isBlank()) {
            _validationState.value = ValidationResult.Empty
            return
        }
        
        if (trimmedQuery.length < 3) {
            _validationState.value = ValidationResult.TooShort
            viewModelScope.launch {
                ttsManager.announce("Destination too short. Please say more.")
            }
            return
        }
        
        viewModelScope.launch {
            _isValidating.value = true
            
            val result = destinationValidator.validateDestination(trimmedQuery)
            _validationState.value = result
            
            when (result) {
                is ValidationResult.Valid -> {
                    ttsManager.announce("Destination: ${result.destination.name}")
                }
                is ValidationResult.Ambiguous -> {
                    // MEDIUM-2 FIX: Handle variable number of options safely
                    val optionsList = when (result.options.size) {
                        0 -> ""
                        1 -> result.options[0].name
                        2 -> "${result.options[0].name}, or ${result.options[1].name}"
                        else -> "${result.options[0].name}, ${result.options[1].name}, or ${result.options[2].name}"
                    }
                    ttsManager.announce("Multiple locations found. Did you mean $optionsList?")
                    _navigationEvent.emit(NavigationEvent.ShowClarificationDialog(result.options))
                }
                is ValidationResult.Invalid -> {
                    ttsManager.announce("Invalid destination. ${result.reason}")
                }
                else -> {
                    // Empty or TooShort already handled above
                }
            }
            
            _isValidating.value = false
        }
    }
    
    fun onGoClicked() {
        val currentState = _validationState.value
        val currentText = destinationText.value.orEmpty().trim()
        
        if (currentText.isBlank()) {
            viewModelScope.launch {
                ttsManager.announce("Please enter a destination")
            }
            return
        }
        
        if (currentState is ValidationResult.Valid) {
            requestRoute(currentState.destination)
        } else {
            validateDestination(currentText)
        }
    }
    
    /**
     * Story 6.2: Request route from current location to destination.
     * Handles network consent, downloads directions, and navigates on success.
     */
    private fun requestRoute(destination: Destination) {
        viewModelScope.launch {
            try {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
                
                // Check network consent before requesting route
                if (!networkConsentManager.hasConsent()) {
                    Timber.tag(TAG).d("Network consent required")
                    _navigationEvent.emit(NavigationEvent.ShowNetworkConsentDialog)
                    return@launch
                }
                
                // Show loading state
                _navigationState.value = NavigationState.RequestingRoute
                ttsManager.announce("Downloading directions")
                
                // Request route from repository
                // CODE REVIEW FIX (Issue #9): Uses GPS location as origin (signature updated)
                val routeResult = navigationRepository.getRoute(
                    destination = destination
                )
                
                if (routeResult.isSuccess) {
                    val route = routeResult.getOrThrow()
                    Timber.tag(TAG).d("Route received: ${route.steps.size} steps, ${route.totalDistance}m")
                    
                    _navigationState.value = NavigationState.RouteReady(route)
                    
                    // Story 6.3: Will navigate to NavigationActiveFragment
                    // For Story 6.2: Just announce placeholder
                    ttsManager.announce("Route calculated. Navigation feature in Story 6.3")
                    _navigationEvent.emit(NavigationEvent.StartNavigation(destination))
                    
                } else {
                    val error = routeResult.exceptionOrNull()
                    Timber.tag(TAG).e(error, "Failed to get route")
                    
                    _navigationState.value = NavigationState.Error(getErrorMessage(error))
                    announceError(error)
                }
                
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected error requesting route")
                _navigationState.value = NavigationState.Error("Unexpected error occurred")
                ttsManager.announce("Unexpected error occurred. Please try again.")
            }
        }
    }
    
    /**
     * Story 6.2: Convert error to user-friendly message.
     */
    private fun getErrorMessage(error: Throwable?): String {
        return when (error) {
            is DirectionsError.NetworkUnavailable -> "Cannot download directions. Check internet connection."
            is DirectionsError.InvalidApiKey -> "Navigation service unavailable."
            is DirectionsError.QuotaExceeded -> "Navigation service unavailable."
            is DirectionsError.Timeout -> "Request timed out."
            is LocationError.PermissionDenied -> "Location permission required for navigation."
            is LocationError.GpsDisabled -> "Enable GPS to start navigation."
            is LocationError.Unavailable -> "Cannot determine current location."
            else -> "Navigation service unavailable. Please try again later."
        }
    }
    
    /**
     * Story 6.2: Announce error via TTS.
     */
    private suspend fun announceError(error: Throwable?) {
        when (error) {
            is DirectionsError.NetworkUnavailable -> 
                ttsManager.announce("Cannot download directions. Check internet connection.")
            is DirectionsError.InvalidApiKey, is DirectionsError.QuotaExceeded -> 
                ttsManager.announce("Navigation service unavailable. Please try again later.")
            is DirectionsError.Timeout -> 
                ttsManager.announce("Request timed out. Please try again.")
            is LocationError.PermissionDenied -> 
                ttsManager.announce("Location permission required for navigation")
            is LocationError.GpsDisabled -> 
                ttsManager.announce("Enable GPS to start navigation")
            is LocationError.Unavailable -> 
                ttsManager.announce("Cannot determine current location. Please try again.")
            else -> 
                ttsManager.announce("Navigation service unavailable. Please try again later.")
        }
    }
    
    fun onNetworkConsentGranted() {
        val currentState = _validationState.value
        if (currentState is ValidationResult.Valid) {
            requestRoute(currentState.destination)
        }
    }
    
    fun onBackPressed() {
        viewModelScope.launch {
            ttsManager.announce("Navigation cancelled")
        }
    }
    
    fun onClarificationSelected(selectedDestination: Destination) {
        destinationText.value = selectedDestination.name
        _validationState.value = ValidationResult.Valid(selectedDestination)
        
        viewModelScope.launch {
            ttsManager.announce("Selected: ${selectedDestination.name}")
        }
    }
}

sealed class NavigationEvent {
    data class StartNavigation(val destination: Destination) : NavigationEvent()
    data class ShowClarificationDialog(val options: List<Destination>) : NavigationEvent()
    object ShowNetworkConsentDialog : NavigationEvent()  // Story 6.2
}

/**
 * Story 6.2: Navigation state for route downloading.
 */
sealed class NavigationState {
    object Idle : NavigationState()
    object RequestingRoute : NavigationState()
    data class RouteReady(val route: NavigationRoute) : NavigationState()
    data class Error(val message: String) : NavigationState()
}
