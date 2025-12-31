package com.visionfocus.data.model

/**
 * Haptic feedback intensity levels (Story 2.6)
 * 
 * Provides accessibility options for deaf-blind users who rely on
 * tactile feedback as their primary non-visual communication channel.
 * 
 * Intensity mapping (API 26+):
 * - OFF: No vibrations (0% amplitude)
 * - LIGHT: Subtle vibrations (50% amplitude)
 * - MEDIUM: Moderate vibrations (75% amplitude) [DEFAULT]
 * - STRONG: Maximum vibrations (100% amplitude)
 * 
 * Pre-API 26 fallback uses duration scaling instead of amplitude.
 * 
 * @see com.visionfocus.accessibility.haptic.HapticFeedbackManager
 */
enum class HapticIntensity {
    /** No haptic feedback - zero battery impact */
    OFF,
    
    /** Light vibrations - gentle tactile feedback (50% amplitude) */
    LIGHT,
    
    /** Medium vibrations - default balanced setting (75% amplitude) */
    MEDIUM,
    
    /** Strong vibrations - maximum tactile feedback (100% amplitude) */
    STRONG;
    
    companion object {
        /**
         * Parse string to HapticIntensity enum
         * 
         * @param value String representation (e.g., "MEDIUM")
         * @return Corresponding enum value, or MEDIUM if invalid
         */
        fun fromString(value: String): HapticIntensity {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("HapticIntensity", "Invalid value '$value', defaulting to MEDIUM", e)
                MEDIUM // Default fallback
            }
        }
    }
}
