package com.visionfocus.voice.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.permissions.manager.AccessibilityAnnouncementHelper
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.recognizer.VoiceRecognitionManager
import com.visionfocus.voice.recognizer.VoiceRecognitionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Voice Recognition ViewModel
 * Story 3.1 Task 7: State management for voice recognition UI
 * 
 * Manages voice recognition lifecycle, permission state, and UI interactions.
 * Integrates with existing accessibility infrastructure (TTSManager, HapticFeedbackManager).
 * 
 * Key Responsibilities:
 * - Expose VoiceRecognitionState to UI via StateFlow
 * - Handle permission state changes (disable button if denied)
 * - Coordinate TTS announcements for state changes
 * - Provide haptic feedback on button interactions
 * - Stop TTS before starting listening (avoid self-recognition)
 * 
 * Integration Points:
 * - VoiceRecognitionManager: Core speech recognition logic
 * - PermissionManager: Microphone permission checks
 * - HapticFeedbackManager: Tactile feedback on button press
 * - TTSManager: TTS announcements and stop before listening
 * 
 * @param voiceRecognitionManager Core voice recognition manager
 * @param permissionManager Permission state checks
 * @param hapticManager Haptic feedback
 * @param ttsManager TTS control for announcements and stopping before listening
 * @since Story 3.1
 */
@HiltViewModel
class VoiceRecognitionViewModel @Inject constructor(
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val permissionManager: PermissionManager,
    private val hapticManager: HapticFeedbackManager,
    private val ttsManager: TTSManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "VoiceRecognitionVM"
    }
    
    // State management (Task 7.3)
    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()
    
    // Permission state (Task 7.6)
    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()
    
    init {
        // Initialize permission state
        _isPermissionGranted.value = permissionManager.isMicrophonePermissionGranted()
        
        // Subscribe to VoiceRecognitionManager state changes
        setupVoiceRecognitionCallbacks()
        
        Log.d(TAG, "VoiceRecognitionViewModel initialized - permission granted: ${_isPermissionGranted.value}")
    }
    
    /**
     * Setup callbacks for VoiceRecognitionManager state changes.
     * Story 3.1 Task 8.1: Integrate with AccessibilityAnnouncementHelper
     */
    private fun setupVoiceRecognitionCallbacks() {
        // Listen to state changes from VoiceRecognitionManager
        viewModelScope.launch {
            voiceRecognitionManager.state.collect { newState ->
                _state.value = newState
                handleStateChange(newState)
            }
        }
        
        // Set state change callback for immediate feedback
        voiceRecognitionManager.setOnStateChangeCallback { newState ->
            handleStateChange(newState)
        }
        
        // Set recognized text callback (Story 3.2 integration point)
        voiceRecognitionManager.setOnRecognizedTextCallback { transcription ->
            Log.d(TAG, "Recognized text: \"$transcription\"")
            // Story 3.2 will implement command processing here
            // For now, just log the transcription
        }
    }
    
    /**
     * Handle voice recognition state changes with TTS announcements.
     * Story 3.1 Task 4.2, 8.1: Announce state changes via TTS
     * 
     * @param newState The new VoiceRecognitionState
     */
    private fun handleStateChange(newState: VoiceRecognitionState) {
        Log.d(TAG, "State changed: $newState")
        
        when (newState) {
            is VoiceRecognitionState.Listening -> {
                if (newState.isReady) {
                    // Announce listening state (AC: 5)
                    viewModelScope.launch {
                        ttsManager.announce("Listening for command")
                    }
                }
            }
            is VoiceRecognitionState.Processing -> {
                // Brief processing state - no announcement needed
                Log.d(TAG, "Processing transcription: ${newState.transcription}")
            }
            is VoiceRecognitionState.Error -> {
                // Announce error message (AC: 10, 11)
                viewModelScope.launch {
                    ttsManager.announce(newState.errorMessage)
                }
            }
            is VoiceRecognitionState.Idle -> {
                // Idle state - no announcement needed
            }
        }
    }
    
    /**
     * Start listening for voice commands.
     * Story 3.1 Task 7.4: Public method to start listening
     * 
     * Performs pre-listening actions:
     * 1. Stop any active TTS (avoid self-recognition)
     * 2. Provide haptic feedback (medium intensity)
     * 3. Start speech recognition
     * 
     * @throws IllegalStateException if microphone permission not granted
     */
    fun startListening() {
        Log.d(TAG, "startListening() called")
        
        // Check permission state (Task 7.6)
        if (!_isPermissionGranted.value) {
            Log.w(TAG, "Cannot start listening - microphone permission not granted")
            _state.value = VoiceRecognitionState.Error(
                errorCode = -1,
                errorMessage = "Microphone permission required for voice commands"
            )
            return
        }
        
        // Stop TTS before listening (avoid self-recognition, Story 2.4 learning)
        ttsManager.stop()
        Log.d(TAG, "Stopped TTS before listening")
        
        // Provide haptic feedback on button press (Task 4.6, Story 2.6 integration)
        viewModelScope.launch {
            hapticManager.trigger(HapticPattern.RecognitionStart)
        }
        
        // Start voice recognition
        voiceRecognitionManager.startListening()
    }
    
    /**
     * Stop listening and cancel recognition.
     * Story 3.1 Task 7.5: Public method to stop listening
     */
    fun stopListening() {
        Log.d(TAG, "stopListening() called")
        voiceRecognitionManager.stopListening()
    }
    
    /**
     * Update permission state.
     * Story 3.1 Task 7.6: Handle permission state changes
     * 
     * Called by Activity when microphone permission is granted or denied.
     * Updates UI state to disable/enable voice button.
     * 
     * @param granted True if permission granted, false otherwise
     */
    fun updatePermissionState(granted: Boolean) {
        Log.d(TAG, "updatePermissionState: granted = $granted")
        _isPermissionGranted.value = granted
        
        // If permission denied while listening, stop listening
        if (!granted && _state.value is VoiceRecognitionState.Listening) {
            stopListening()
        }
    }
    
    /**
     * Check if voice button should be enabled.
     * Story 3.1 Task 7.6: Button state based on permission
     * 
     * @return True if button should be enabled, false if disabled
     */
    fun isVoiceButtonEnabled(): Boolean {
        return _isPermissionGranted.value
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared() - cleaning up VoiceRecognitionManager")
        voiceRecognitionManager.destroy()
    }
}
