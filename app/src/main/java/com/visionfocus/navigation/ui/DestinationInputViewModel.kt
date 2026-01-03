package com.visionfocus.navigation.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.ValidationResult
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
import javax.inject.Inject

/**
 * ViewModel for destination input screen.
 * 
 * Story 6.1: Destination Input via Voice and Text
 */
@HiltViewModel
class DestinationInputViewModel @Inject constructor(
    private val destinationValidator: DestinationValidator,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) : ViewModel() {
    
    val destinationText = MutableLiveData<String>("")
    
    private val _validationState = MutableStateFlow<ValidationResult>(ValidationResult.Empty)
    val validationState: StateFlow<ValidationResult> = _validationState.asStateFlow()
    
    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()
    
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
            viewModelScope.launch {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
                _navigationEvent.emit(
                    NavigationEvent.StartNavigation(currentState.destination)
                )
            }
        } else {
            validateDestination(currentText)
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
}
