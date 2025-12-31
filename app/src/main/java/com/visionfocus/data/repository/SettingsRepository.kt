package com.visionfocus.data.repository

import com.visionfocus.data.model.VerbosityMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences storage.
 * 
 * Provides reactive access to user settings via Kotlin Flow,
 * enabling ViewModels to observe preference changes in real-time.
 * 
 * Clean Architecture: This interface defines the data access contract
 * without exposing implementation details (DataStore).
 */
interface SettingsRepository {
    
    /**
     * Observes TTS speech rate preference.
     * 
     * @return Flow emitting speech rate multiplier (0.5x - 2.0x).
     *         Default: 1.0x if not set.
     */
    fun getSpeechRate(): Flow<Float>
    
    /**
     * Updates TTS speech rate preference.
     * 
     * @param rate Speech rate multiplier (0.5x - 2.0x).
     *             Values outside range are clamped.
     */
    suspend fun setSpeechRate(rate: Float)
    
    /**
     * Observes verbosity mode preference.
     * 
     * @return Flow emitting current VerbosityMode.
     *         Default: VerbosityMode.STANDARD if not set.
     */
    fun getVerbosity(): Flow<VerbosityMode>
    
    /**
     * Updates verbosity mode preference.
     * 
     * @param mode New verbosity mode for recognition announcements.
     */
    suspend fun setVerbosity(mode: VerbosityMode)
    
    /**
     * Observes high-contrast mode preference.
     * 
     * @return Flow emitting boolean state.
     *         Default: false if not set.
     */
    fun getHighContrastMode(): Flow<Boolean>
    
    /**
     * Updates high-contrast mode preference.
     * 
     * @param enabled True to enable high-contrast theme (7:1 ratio).
     */
    suspend fun setHighContrastMode(enabled: Boolean)
    
    /**
     * Observes large text mode preference.
     * 
     * @return Flow emitting boolean state.
     *         Default: false if not set.
     */
    fun getLargeTextMode(): Flow<Boolean>
    
    /**
     * Updates large text mode preference.
     * 
     * @param enabled True to enable large text (150% scaling).
     */
    suspend fun setLargeTextMode(enabled: Boolean)
    
    /**
     * Observes haptic feedback intensity preference (Story 2.6).
     * 
     * @return Flow emitting current HapticIntensity.
     *         Default: HapticIntensity.MEDIUM if not set.
     */
    fun getHapticIntensity(): Flow<com.visionfocus.data.model.HapticIntensity>
    
    /**
     * Updates haptic feedback intensity preference (Story 2.6).
     * 
     * @param intensity New haptic intensity for recognition events.
     */
    suspend fun setHapticIntensity(intensity: com.visionfocus.data.model.HapticIntensity)
}
