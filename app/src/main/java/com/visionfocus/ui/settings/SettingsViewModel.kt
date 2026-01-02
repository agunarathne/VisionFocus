package com.visionfocus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen managing theme preferences and haptic feedback.
 * 
 * Orchestrates user interactions with SettingsRepository,
 * exposing reactive StateFlows for UI observation.
 * 
 * Theme Preferences:
 * - High-Contrast Mode: 7:1 contrast ratio (pure black/white)
 * - Large Text Mode: 150% text scaling (20sp → 30sp)
 * 
 * Haptic Preferences (Story 2.6):
 * - Haptic Intensity: OFF, LIGHT, MEDIUM, STRONG
 * 
 * Verbosity Preferences (Story 4.1):
 * - Verbosity Mode: BRIEF, STANDARD, DETAILED
 * 
 * StateFlow Usage:
 * StateFlow conversion with stateIn() provides:
 * - Hot flow that survives configuration changes
 * - Immediate access to current value via .value property
 * - Lifecycle-aware collection with WhileSubscribed(5000)
 * 
 * Toggle Pattern:
 * toggle*() methods read current StateFlow value, negate it,
 * and persist to repository. Repository emits new value through
 * Flow, updating StateFlow reactively.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    companion object {
        // LOW-2 fix: Keep StateFlow active 5 seconds after last collector disconnects
        // This prevents unnecessary DataStore re-reads during configuration changes
        private const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5000L
    }
    
    /**
     * High-contrast mode enabled state.
     * 
     * When true, app uses pure black (#000000) background with
     * pure white (#FFFFFF) foreground for 21:1 contrast ratio.
     * 
     * Default: false
     */
    val highContrastMode: StateFlow<Boolean> = settingsRepository
        .getHighContrastMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = false
        )
    
    /**
     * Large text mode enabled state.
     * 
     * When true, all text sizes increase by 150%:
     * - Body text: 20sp → 30sp
     * - Headlines: 24sp → 36sp
     * - Captions: 12sp → 18sp
     * 
     * Default: false
     */
    val largeTextMode: StateFlow<Boolean> = settingsRepository
        .getLargeTextMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = false
        )
    
    /**
     * Haptic intensity preference (Story 2.6).
     * 
     * Controls vibration strength for recognition events:
     * - OFF: No vibration
     * - LIGHT: 50% amplitude
     * - MEDIUM: 75% amplitude (default)
     * - STRONG: 100% amplitude
     * 
     * Default: MEDIUM
     */
    val hapticIntensity: StateFlow<HapticIntensity> = settingsRepository
        .getHapticIntensity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = HapticIntensity.MEDIUM
        )
    
    /**
     * Verbosity mode preference (Story 4.1).
     * 
     * Controls announcement detail level for recognized objects:
     * - BRIEF: Category only ("Chair")
     * - STANDARD: Category + confidence ("Chair with high confidence")
     * - DETAILED: Category + confidence + position + count 
     *   ("High confidence: chair in center of view. Two chairs detected.")
     * 
     * Default: STANDARD
     */
    val verbosityMode: StateFlow<VerbosityMode> = settingsRepository
        .getVerbosity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = VerbosityMode.STANDARD
        )
    
    /**
     * Camera preview enabled state (Testing/Development).
     * 
     * Controls whether camera preview is visible on recognition screen:
     * - false: 1x1px invisible preview (production - for blind users)
     * - true: Full-screen preview (testing/development - for manual testing)
     * 
     * Default: false
     */
    val cameraPreviewEnabled: StateFlow<Boolean> = settingsRepository
        .getCameraPreviewEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = false
        )
    
    /**
     * Sets high-contrast mode preference.
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * Note: Theme change requires activity recreation
     * (handled in Fragment/ThemeManager).
     * 
     * This is a suspend function to ensure DataStore write completes
     * before activity recreation.
     * 
     * @param enabled The desired high-contrast mode state
     */
    suspend fun setHighContrastMode(enabled: Boolean) {
        android.util.Log.d("VisionFocus", "[ViewModel] setHighContrastMode called with: $enabled")
        settingsRepository.setHighContrastMode(enabled)
        android.util.Log.d("VisionFocus", "[ViewModel] setHighContrastMode DataStore write completed")
    }
    
    /**
     * Sets large text mode preference.
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * Note: Theme change requires activity recreation
     * (handled in Fragment/ThemeManager).
     * 
     * This is a suspend function to ensure DataStore write completes
     * before activity recreation.
     * 
     * @param enabled The desired large text mode state
     */
    suspend fun setLargeTextMode(enabled: Boolean) {
        android.util.Log.d("VisionFocus", "[ViewModel] setLargeTextMode called with: $enabled")
        settingsRepository.setLargeTextMode(enabled)
        android.util.Log.d("VisionFocus", "[ViewModel] setLargeTextMode DataStore write completed")
    }
    
    /**
     * Sets haptic intensity preference (Story 2.6).
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * @param intensity The desired haptic intensity level
     */
    suspend fun setHapticIntensity(intensity: HapticIntensity) {
        android.util.Log.d("VisionFocus", "[ViewModel] setHapticIntensity called with: $intensity")
        settingsRepository.setHapticIntensity(intensity)
        android.util.Log.d("VisionFocus", "[ViewModel] setHapticIntensity DataStore write completed")
    }
    
    /**
     * Sets verbosity mode preference (Story 4.1).
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * @param mode The desired verbosity mode level
     */
    suspend fun setVerbosityMode(mode: VerbosityMode) {
        android.util.Log.d("VisionFocus", "[ViewModel] setVerbosityMode called with: $mode")
        settingsRepository.setVerbosity(mode)
        android.util.Log.d("VisionFocus", "[ViewModel] setVerbosityMode DataStore write completed")
    }
    
    /**
     * Sets camera preview enabled preference (Testing/Development).
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * @param enabled The desired camera preview state
     */
    suspend fun setCameraPreviewEnabled(enabled: Boolean) {
        android.util.Log.d("VisionFocus", "[ViewModel] setCameraPreviewEnabled called with: $enabled")
        settingsRepository.setCameraPreviewEnabled(enabled)
        android.util.Log.d("VisionFocus", "[ViewModel] setCameraPreviewEnabled DataStore write completed")
    }
}
