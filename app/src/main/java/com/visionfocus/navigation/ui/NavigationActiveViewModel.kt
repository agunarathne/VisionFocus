package com.visionfocus.navigation.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.NavigationProgress
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.service.NavigationService
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Story 6.3: ViewModel for NavigationActiveFragment.
 * 
 * Manages navigation state during active turn-by-turn guidance.
 * Coordinates with NavigationService for GPS updates and progress tracking.
 * 
 * State Flows:
 * - navigationProgress: Real-time progress updates from NavigationService
 * - currentInstruction: Current turn instruction for display
 * - isNavigating: Whether navigation is active
 * 
 * Actions:
 * - startNavigation(route): Begin GPS tracking and voice guidance
 * - cancelNavigation(): Stop navigation service and return to input screen
 */
@HiltViewModel
class NavigationActiveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "NavigationActiveViewModel"
    }
    
    // Navigation progress state (updated by NavigationService)
    private val _navigationProgress = MutableStateFlow<NavigationProgress?>(null)
    val navigationProgress: StateFlow<NavigationProgress?> = _navigationProgress.asStateFlow()
    
    // Current instruction text for UI display
    private val _currentInstruction = MutableStateFlow("")
    val currentInstruction: StateFlow<String> = _currentInstruction.asStateFlow()
    
    // Distance remaining formatted for display
    private val _distanceRemaining = MutableStateFlow("")
    val distanceRemaining: StateFlow<String> = _distanceRemaining.asStateFlow()
    
    // Time remaining formatted for display
    private val _timeRemaining = MutableStateFlow("")
    val timeRemaining: StateFlow<String> = _timeRemaining.asStateFlow()
    
    // Navigation active state
    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()
    
    // Current route being followed
    private var currentRoute: NavigationRoute? = null
    
    /**
     * Start navigation with the provided route.
     * 
     * Story 6.3: Initializes navigation state and starts NavigationService
     * for GPS tracking and turn-by-turn voice guidance.
     * 
     * @param route Complete navigation route from Story 6.2
     * @param destinationName Human-readable destination name for announcements
     */
    fun startNavigation(route: NavigationRoute, destinationName: String) {
        viewModelScope.launch {
            currentRoute = route
            _isNavigating.value = true
            
            // Set initial instruction from first step
            if (route.steps.isNotEmpty()) {
                _currentInstruction.value = route.steps[0].instruction
            }
            
            // Initialize progress display
            updateProgressDisplay(
                NavigationProgress(
                    currentStepIndex = 0,
                    distanceToCurrentStep = route.steps.firstOrNull()?.distance?.toFloat() ?: 0f,
                    totalDistanceRemaining = route.totalDistance.toFloat(),
                    estimatedTimeRemaining = route.totalDuration
                )
            )
            
            // Start NavigationService for GPS tracking
            // Create Destination from route coordinates and Safe Args name
            val destination = Destination(
                query = "", // Not needed for service
                name = destinationName,
                latitude = route.destination.latitude,
                longitude = route.destination.longitude
            )
            
            val intent = Intent(context, NavigationService::class.java).apply {
                action = NavigationService.ACTION_START_NAVIGATION
                putExtra(NavigationService.EXTRA_ROUTE, route)
                putExtra(NavigationService.EXTRA_DESTINATION, destination)
            }
            context.startForegroundService(intent)
        }
    }
    
    /**
     * Cancel navigation and return to destination input.
     * 
     * Story 6.3: Stops NavigationService, announces cancellation via TTS,
     * provides haptic feedback, and signals UI to navigate back.
     */
    fun cancelNavigation() {
        viewModelScope.launch {
            // CRITICAL FIX: Stop all ongoing TTS FIRST to prevent continuous announcements
            ttsManager.stop()
            
            // Stop NavigationService immediately (stops GPS updates and recalculation loop)
            val intent = Intent(context, NavigationService::class.java).apply {
                action = NavigationService.ACTION_STOP_NAVIGATION
            }
            context.startService(intent)
            
            // Haptic feedback for cancel action
            hapticFeedbackManager.trigger(com.visionfocus.accessibility.haptic.HapticPattern.ButtonPress)
            
            // TTS announcement (after stopping ongoing speech)
            ttsManager.announce("Navigation cancelled")
            
            // Reset state
            _isNavigating.value = false
            _navigationProgress.value = null
            _currentInstruction.value = ""
            _distanceRemaining.value = ""
            _timeRemaining.value = ""
            currentRoute = null
        }
    }
    
    /**
     * Update navigation progress from NavigationService.
     * 
     * Called by NavigationService when GPS location updates occur (1Hz).
     * Updates UI state flows with formatted progress information.
     * 
     * @param progress Current navigation progress
     */
    fun updateProgress(progress: NavigationProgress) {
        _navigationProgress.value = progress
        updateProgressDisplay(progress)
        updateCurrentInstruction(progress)
    }
    
    /**
     * Format and update progress display values.
     */
    private fun updateProgressDisplay(progress: NavigationProgress) {
        // Format distance remaining (km if >= 1000m, meters otherwise)
        _distanceRemaining.value = if (progress.totalDistanceRemaining >= 1000f) {
            "%.1f km remaining".format(progress.totalDistanceRemaining / 1000f)
        } else {
            "${progress.totalDistanceRemaining.toInt()} m remaining"
        }
        
        // Format time remaining (hours:minutes if >= 60 minutes, minutes otherwise)
        val minutes = progress.estimatedTimeRemaining / 60
        _timeRemaining.value = if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "$hours h $remainingMinutes min"
        } else {
            "$minutes minutes"
        }
    }
    
    /**
     * Update current instruction based on progress.
     */
    private fun updateCurrentInstruction(progress: NavigationProgress) {
        currentRoute?.let { route ->
            val step = route.steps.getOrNull(progress.currentStepIndex)
            if (step != null) {
                _currentInstruction.value = step.instruction
            }
        }
    }
}
