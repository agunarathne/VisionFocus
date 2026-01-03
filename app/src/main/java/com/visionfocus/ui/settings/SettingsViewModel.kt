package com.visionfocus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.engine.VoiceOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
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
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : ViewModel() {
    
    companion object {
        // LOW-2 fix: Keep StateFlow active 5 seconds after last collector disconnects
        // This prevents unnecessary DataStore re-reads during configuration changes
        private const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5000L
        
        // Story 5.1: Speech rate constraints
        private const val MIN_SPEECH_RATE = 0.5f
        private const val MAX_SPEECH_RATE = 2.0f
        private const val SPEECH_RATE_INCREMENT = 0.25f
    }
    
    /**
     * Story 5.1: Speech rate preference.
     * 
     * Controls TTS speaking speed multiplier:
     * - 0.5×: Half speed (slow)
     * - 1.0×: Normal speed (default)
     * - 2.0×: Double speed (fast)
     * 
     * Default: 1.0×
     */
    val speechRate: StateFlow<Float> = settingsRepository
        .getSpeechRate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = 1.0f
        )
    
    /**
     * Story 5.2: Current voice locale from DataStore
     * null = system default voice
     */
    val voiceLocale: StateFlow<String?> = settingsRepository.getVoiceLocale()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = null
        )
    
    /**
     * Story 5.2: Available TTS voices from Android engine
     * Loaded once on ViewModel creation
     */
    val availableVoices: StateFlow<List<VoiceOption>> = flow {
        // Wait for TTSManager to initialize
        while (!ttsManager.isReady()) {
            delay(100)
        }
        emit(ttsManager.getAvailableVoices())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,  // Load immediately
        initialValue = emptyList()
    )
    
    /**
     * Story 5.1: One-time announcement events for TalkBack.
     * UI collects these to trigger accessibility announcements.
     */
    private val _announcements = MutableSharedFlow<String>()
    val announcements: SharedFlow<String> = _announcements.asSharedFlow()
    
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
    
    /**
     * Story 5.1: Sets speech rate preference.
     * 
     * Persists to DataStore.
     * UI automatically updates via StateFlow observation.
     * 
     * @param rate New speech rate (0.5× - 2.0×). Values outside range are clamped.
     */
    suspend fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        android.util.Log.d("VisionFocus", "[ViewModel] setSpeechRate called with: $clampedRate")
        settingsRepository.setSpeechRate(clampedRate)
        android.util.Log.d("VisionFocus", "[ViewModel] setSpeechRate DataStore write completed")
    }
    
    /**
     * Story 5.1: Increments speech rate by 0.25× (voice command integration).
     * 
     * Used by "Increase speed" voice command (Epic 3).
     */
    suspend fun incrementSpeechRate() {
        val currentRate = speechRate.value
        val newRate = (currentRate + SPEECH_RATE_INCREMENT).coerceAtMost(MAX_SPEECH_RATE)
        
        if (newRate == MAX_SPEECH_RATE && currentRate == MAX_SPEECH_RATE) {
            // Already at maximum
            _announcements.emit("Speech rate at maximum")
        } else {
            setSpeechRate(newRate)
            ttsManager.setSpeechRate(newRate)
            _announcements.emit(String.format("Speech rate increased to %.2f times", newRate))
        }
    }
    
    /**
     * Story 5.1: Decrements speech rate by 0.25× (voice command integration).
     * 
     * Used by "Decrease speed" voice command (Epic 3).
     */
    suspend fun decrementSpeechRate() {
        val currentRate = speechRate.value
        val newRate = (currentRate - SPEECH_RATE_INCREMENT).coerceAtLeast(MIN_SPEECH_RATE)
        
        if (newRate == MIN_SPEECH_RATE && currentRate == MIN_SPEECH_RATE) {
            // Already at minimum
            _announcements.emit("Speech rate at minimum")
        } else {
            setSpeechRate(newRate)
            ttsManager.setSpeechRate(newRate)
            _announcements.emit(String.format("Speech rate decreased to %.2f times", newRate))
        }
    }
    
    /**
     * Story 5.1: Plays sample announcement at current speech rate.
     * 
     * Triggered by "Test Speed" button for instant user feedback.
     * 
     * CRITICAL-1 FIX: Use string resource via text parameter for i18n support
     * 
     * @param text Sample announcement text from string resource
     */
    suspend fun playSampleAnnouncement(text: String) {
        ttsManager.announce(text)
    }
    
    /**
     * Story 5.2 Task 6.3: Set voice locale preference
     * 
     * @param locale Voice locale string: "en-US", "en-GB", null for system default
     */
    fun setVoiceLocale(locale: String?) {
        viewModelScope.launch {
            // Persist to DataStore
            settingsRepository.setVoiceLocale(locale)
            
            // Apply to TTSManager immediately
            val success = ttsManager.setVoice(locale)
            
            if (!success) {
                // Voice unavailable - TTSManager already announced fallback
                android.util.Log.w("VisionFocus", "Voice $locale unavailable, fell back to default")
            }
        }
    }
    
    /**
     * Story 5.2 Task 6.4: Play sample announcement in specific voice
     * 
     * Used for voice preview when user selects different voice option.
     * Temporarily sets voice, plays sample, then restores saved voice.
     * 
     * @param locale Voice locale to preview
     * @param text Sample text to announce
     */
    fun playSampleWithVoice(locale: String, text: String) {
        viewModelScope.launch {
            // MEDIUM-5 FIX: Save original voice to restore after preview
            val originalLocale = voiceLocale.value
            
            // Temporarily set voice for preview
            ttsManager.setVoice(locale)
            
            // Play sample
            ttsManager.announce(text)
            
            // Restore original voice after sample completes
            // Wait for TTS announcement to start (typical latency ~200ms + sample duration ~2s)
            delay(2500)
            ttsManager.setVoice(originalLocale)
        }
    }
    
    /**
     * Story 5.3 Task 1.12: Reset all preferences to default values.
     * 
     * Restores all user preferences to their default states:
     * - Speech rate: 1.0×
     * - Voice locale: null (system default)
     * - Verbosity: STANDARD
     * - High-contrast: false
     * - Large text: false
     * - Haptic intensity: MEDIUM
     * 
     * Used by "Reset to Defaults" button in Settings screen with confirmation dialog.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            android.util.Log.d("VisionFocus", "[ViewModel] Resetting all preferences to defaults")
            settingsRepository.setSpeechRate(1.0f)                      // Default: 1.0× speed
            settingsRepository.setVoiceLocale(null)                     // Default: system voice
            settingsRepository.setVerbosity(VerbosityMode.STANDARD)     // Default: STANDARD
            settingsRepository.setHighContrastMode(false)                // Default: off
            settingsRepository.setLargeTextMode(false)                   // Default: off
            settingsRepository.setHapticIntensity(HapticIntensity.MEDIUM) // Default: MEDIUM
            settingsRepository.setCameraPreviewEnabled(false)            // Default: off (CRITICAL-2 FIX)
            android.util.Log.d("VisionFocus", "[ViewModel] All preferences reset completed")
        }
    }
}
