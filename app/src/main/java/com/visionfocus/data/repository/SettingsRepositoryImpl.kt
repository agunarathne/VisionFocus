package com.visionfocus.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.data.preferences.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of SettingsRepository.
 * 
 * Thread-safe by design: DataStore handles concurrent access internally.
 * All operations are async via Kotlin Flow and suspend functions.
 * 
 * Error handling: IOException from DataStore operations is caught
 * and emits default values to prevent app crashes.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    
    companion object {
        // Default values matching requirements
        private const val DEFAULT_SPEECH_RATE = 1.0f
        private val DEFAULT_VERBOSITY_MODE = VerbosityMode.STANDARD
        private const val DEFAULT_HIGH_CONTRAST = false
        private const val DEFAULT_LARGE_TEXT = false
        private val DEFAULT_HAPTIC_INTENSITY = HapticIntensity.MEDIUM
        private const val DEFAULT_CAMERA_PREVIEW = false // Production default: invisible for blind users
        
        // Speech rate constraints (FR30, FR46)
        private const val MIN_SPEECH_RATE = 0.5f
        private const val MAX_SPEECH_RATE = 2.0f
    }
    
    override fun getSpeechRate(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                // Handle DataStore read errors (e.g., corrupted file)
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferenceKeys.SPEECH_RATE] ?: DEFAULT_SPEECH_RATE
            }
    }
    
    override suspend fun setSpeechRate(rate: Float) {
        // Clamp rate to valid range (0.5x - 2.0x)
        val clampedRate = rate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SPEECH_RATE] = clampedRate
        }
    }
    
    override fun getVerbosity(): Flow<VerbosityMode> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val modeString = preferences[PreferenceKeys.VERBOSITY_MODE]
                modeString?.let { VerbosityMode.fromString(it) } ?: DEFAULT_VERBOSITY_MODE
            }
    }
    
    override suspend fun setVerbosity(mode: VerbosityMode) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.VERBOSITY_MODE] = mode.name
        }
    }
    
    override fun getHighContrastMode(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferenceKeys.HIGH_CONTRAST_MODE] ?: DEFAULT_HIGH_CONTRAST
            }
    }
    
    override suspend fun setHighContrastMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HIGH_CONTRAST_MODE] = enabled
        }
    }
    
    override fun getLargeTextMode(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferenceKeys.LARGE_TEXT_MODE] ?: DEFAULT_LARGE_TEXT
            }
    }
    
    override suspend fun setLargeTextMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.LARGE_TEXT_MODE] = enabled
        }
    }
    
    override fun getHapticIntensity(): Flow<HapticIntensity> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val intensityString = preferences[PreferenceKeys.HAPTIC_INTENSITY]
                intensityString?.let { HapticIntensity.fromString(it) } ?: DEFAULT_HAPTIC_INTENSITY
            }
    }
    
    override suspend fun setHapticIntensity(intensity: HapticIntensity) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HAPTIC_INTENSITY] = intensity.name
        }
    }
    
    override fun getCameraPreviewEnabled(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferenceKeys.CAMERA_PREVIEW_ENABLED] ?: DEFAULT_CAMERA_PREVIEW
            }
    }
    
    override suspend fun setCameraPreviewEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CAMERA_PREVIEW_ENABLED] = enabled
        }
    }
}
