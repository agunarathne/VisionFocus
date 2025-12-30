package com.visionfocus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen managing theme preferences.
 * 
 * Orchestrates user interactions with SettingsRepository,
 * exposing reactive StateFlows for UI observation.
 * 
 * Theme Preferences:
 * - High-Contrast Mode: 7:1 contrast ratio (pure black/white)
 * - Large Text Mode: 150% text scaling (20sp → 30sp)
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
     * Toggles high-contrast mode preference.
     * 
     * Negates current value and persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * Note: Theme change requires activity recreation
     * (handled in Fragment/ThemeManager).
     */
    fun toggleHighContrastMode() {
        viewModelScope.launch {
            val newValue = !highContrastMode.value
            settingsRepository.setHighContrastMode(newValue)
        }
    }
    
    /**
     * Toggles large text mode preference.
     * 
     * Negates current value and persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * Note: Theme change requires activity recreation
     * (handled in Fragment/ThemeManager).
     */
    fun toggleLargeTextMode() {
        viewModelScope.launch {
            val newValue = !largeTextMode.value
            settingsRepository.setLargeTextMode(newValue)
        }
    }
}
