package com.visionfocus.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Type-safe keys for DataStore preferences.
 * 
 * Using preferences keys provides compile-time safety and prevents
 * typos in key names across the codebase.
 */
object PreferenceKeys {
    /**
     * TTS speech rate multiplier (0.5x - 2.0x range).
     * Default: 1.0x (normal speed)
     */
    val SPEECH_RATE = floatPreferencesKey("speech_rate")
    
    /**
     * Story 5.2: TTS voice locale preference.
     * Values: "en-US", "en-GB", "en-AU", etc.
     * Default: null (system default voice)
     */
    val VOICE_LOCALE = stringPreferencesKey("voice_locale")
    
    /**
     * Verbosity mode for recognition announcements.
     * Values: "BRIEF", "STANDARD", "DETAILED"
     * Default: "STANDARD"
     */
    val VERBOSITY_MODE = stringPreferencesKey("verbosity_mode")
    
    /**
     * High-contrast visual mode enabled state.
     * Default: false
     */
    val HIGH_CONTRAST_MODE = booleanPreferencesKey("high_contrast_mode")
    
    /**
     * Large text mode enabled state (150% text scaling).
     * Default: false
     */
    val LARGE_TEXT_MODE = booleanPreferencesKey("large_text_mode")
    
    /**
     * Haptic feedback intensity level (Story 2.6).
     * Values: "OFF", "LIGHT", "MEDIUM", "STRONG"
     * Default: "MEDIUM"
     */
    val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
    
    /**
     * Camera preview visibility enabled state (Testing/Development).
     * Default: false (invisible 1x1px for blind users)
     */
    val CAMERA_PREVIEW_ENABLED = booleanPreferencesKey("camera_preview_enabled")
    
    /**
     * Story 6.2: Network consent for Google Maps Directions API.
     * User must explicitly grant permission before any API calls.
     * Default: false (no consent yet)
     */
    val NETWORK_CONSENT = booleanPreferencesKey("network_consent")
}
