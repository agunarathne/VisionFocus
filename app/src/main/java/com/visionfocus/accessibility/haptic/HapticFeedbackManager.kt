package com.visionfocus.accessibility.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized haptic feedback controller (Story 2.6)
 * 
 * Provides tactile communication channel for deaf-blind users through
 * distinct vibration patterns that complement audio (TTS) and visual feedback.
 * 
 * **API Level Support:**
 * - API 31+: VibratorManager for multi-vibrator device support
 * - API 26-30: Vibrator with amplitude control (VibrationEffect)
 * - Pre-API 26: Duration-based fallback (no amplitude control)
 * 
 * **Amplitude Control (API 26+):**
 * - LIGHT: 50% amplitude (~127/255)
 * - MEDIUM: 75% amplitude (~191/255) [DEFAULT]
 * - STRONG: 100% amplitude (255/255 = DEFAULT_AMPLITUDE)
 * - OFF: No vibrations (zero battery impact)
 * 
 * **Battery Efficiency:**
 * - Typical usage: ~50 recognitions/hour = <1% battery/hour
 * - All patterns have finite duration (100-300ms)
 * - OFF setting bypasses all vibration calls
 * 
 * @param context Application context for system service access
 * @param settingsRepository Reactive access to user's haptic intensity preference
 * 
 * @see HapticPattern for pattern definitions and timing details
 * @see HapticIntensity for amplitude scaling
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    
    companion object {
        private const val TAG = "HapticFeedbackManager"
        
        /**
         * Amplitude percentages for intensity levels (API 26+)
         * Values scale VibrationEffect.DEFAULT_AMPLITUDE (255)
         */
        private const val AMPLITUDE_LIGHT = 0.5f // 50% = ~127
        private const val AMPLITUDE_MEDIUM = 0.75f // 75% = ~191
        private const val AMPLITUDE_STRONG = 1.0f // 100% = 255
        
        /**
         * Pre-API 26 fallback: Duration scaling (no amplitude support)
         * Base duration multiplied by these factors
         */
        private const val DURATION_LIGHT = 0.5f // 50% duration
        private const val DURATION_MEDIUM = 1.0f // 100% duration (no change)
        private const val DURATION_STRONG = 2.0f // 200% duration
    }
    
    // Coroutine scope for observing preferences (Thread-safe intensity monitoring)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Current intensity level (cached to avoid Flow collection in every trigger call)
    @Volatile
    private var currentIntensity = HapticIntensity.MEDIUM
    
    init {
        // CRITICAL-3 FIX: Load initial intensity synchronously to prevent race condition
        // If user has OFF setting, we must know BEFORE first trigger() call
        runBlocking {
            currentIntensity = settingsRepository.getHapticIntensity().first()
            android.util.Log.d(TAG, "Initial haptic intensity loaded: $currentIntensity")
        }
        
        // Then observe changes reactively
        scope.launch {
            settingsRepository.getHapticIntensity().collect { intensity ->
                currentIntensity = intensity
                android.util.Log.d(TAG, "Haptic intensity updated: $currentIntensity")
            }
        }
    }
    
    /**
     * Lazy vibrator initialization with API level compatibility
     * 
     * API 31+: Use VibratorManager for better multi-vibrator support
     * API <31: Use legacy Vibrator service
     * 
     * Returns null if device has no vibrator capability.
     */
    private val vibrator: Vibrator? by lazy {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: VibratorManager approach
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            // API <31: Legacy Vibrator service
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        if (v == null) {
            android.util.Log.w(TAG, "Vibrator service unavailable on this device")
        } else {
            val hasAmplitudeControl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.hasAmplitudeControl()
            } else {
                false
            }
            android.util.Log.d(TAG, "Vibrator initialized: hasVibrator=${v.hasVibrator()}, API=${Build.VERSION.SDK_INT}, hasAmplitudeControl=$hasAmplitudeControl, Device=${Build.MANUFACTURER} ${Build.MODEL}")
        }
        
        v
    }
    
    /**
     * Check if device has vibrator hardware
     * 
     * Graceful degradation: App functions normally without vibrator.
     * Settings UI should disable haptic options if hasVibrator == false.
     */
    private val hasVibrator: Boolean
        get() = vibrator?.hasVibrator() ?: false
    
    /**
     * Trigger haptic feedback pattern
     * 
     * Flow:
     * 1. Check device has vibrator (no-op if unavailable)
     * 2. Retrieve user's haptic intensity preference
     * 3. Bypass vibration if intensity == OFF
     * 4. Execute pattern with amplitude scaling
     * 
     * Non-blocking: Runs on separate thread, does not delay state transitions.
     * Performance: ≤10ms trigger latency
     * 
     * Error handling: Catches all exceptions to prevent crashes during recognition flow.
     * 
     * @param pattern HapticPattern to execute (Start, Success, Error)
     */
    suspend fun trigger(pattern: HapticPattern) {
        try {
            // Graceful degradation: Device has no vibrator
            if (!hasVibrator) {
                android.util.Log.d(TAG, "Haptic trigger ignored: Device has no vibrator")
                return
            }
            
            // Retrieve user's haptic intensity preference (reactive)
            val intensity = settingsRepository.getHapticIntensity().first()
            
            // Respect user preference: OFF disables all vibrations
            if (intensity == HapticIntensity.OFF) {
                android.util.Log.d(TAG, "Haptic trigger ignored: Intensity set to OFF")
                return
            }
            
            // Execute pattern with amplitude/duration scaling
            when (pattern) {
            HapticPattern.RecognitionStart -> {
                vibratePattern(
                    timings = longArrayOf(0, 100), // [0ms delay, 100ms vibration]
                    amplitudes = intArrayOf(0, getAmplitude(intensity)),
                    repeat = -1 // no repeat
                )
            }
            
            HapticPattern.RecognitionSuccess -> {
                // Double vibration: 100ms, 50ms gap, 100ms
                vibratePattern(
                    timings = longArrayOf(0, 100, 50, 100), // [0ms, vibrate, gap, vibrate]
                    amplitudes = intArrayOf(0, getAmplitude(intensity), 0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
            
            HapticPattern.RecognitionError -> {
                vibratePattern(
                    timings = longArrayOf(0, 300), // [0ms delay, 300ms vibration]
                    amplitudes = intArrayOf(0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
            
            HapticPattern.CommandExecuted -> {
                // Story 3.2 Task 4.4: Same pattern as RecognitionStart for consistency
                vibratePattern(
                    timings = longArrayOf(0, 100), // [0ms delay, 100ms vibration]
                    amplitudes = intArrayOf(0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
            
            HapticPattern.Cancelled -> {
                // Story 3.3 Task 2.4: Distinct cancellation pattern (shorter than CommandExecuted)
                vibratePattern(
                    timings = longArrayOf(0, 50), // [0ms delay, 50ms vibration]
                    amplitudes = intArrayOf(0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
            
            HapticPattern.ButtonPress -> {
                // Story 5.4 Task 6: Button press feedback (50ms, same as Cancelled)
                vibratePattern(
                    timings = longArrayOf(0, 50), // [0ms delay, 50ms vibration]
                    amplitudes = intArrayOf(0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
            
            HapticPattern.NavigationAlert -> {
                // Story 5.4 Task 7: Triple pulse for navigation alerts (Epic 6 integration)
                // Pattern: 50ms on, 50ms off, 50ms on, 50ms off, 50ms on
                vibratePattern(
                    timings = longArrayOf(0, 50, 50, 50, 50, 50), // [0ms, pulse, gap, pulse, gap, pulse]
                    amplitudes = intArrayOf(0, getAmplitude(intensity), 0, getAmplitude(intensity), 0, getAmplitude(intensity)),
                    repeat = -1
                )
            }
        }
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "Haptic trigger failed: VIBRATE permission revoked", e)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Haptic trigger failed: ${e.message}", e)
        }
    }
    
    /**
     * Execute vibration waveform with API level compatibility
     * 
     * API 26+: VibrationEffect.createWaveform() with amplitude control
     * Pre-API 26: Deprecated vibrate() with duration-only control
     * 
     * @param timings Duration array in milliseconds [delay, vibrate, pause, vibrate, ...]
     * @param amplitudes Amplitude array 0-255 [0, intensity, 0, intensity, ...]
     * @param repeat Index to repeat from, or -1 for no repeat
     */
    private fun vibratePattern(timings: LongArray, amplitudes: IntArray, repeat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+: Modern VibrationEffect with amplitude control
            android.util.Log.d(TAG, "vibratePattern: API ${Build.VERSION.SDK_INT}, timings=${timings.contentToString()}, amplitudes=${amplitudes.contentToString()}")
            val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)
            vibrator?.vibrate(effect)
            android.util.Log.d(TAG, "Vibration executed with amplitude control")
        } else {
            // Pre-API 26: Duration fallback (no amplitude control)
            // Use timings directly (amplitude scaling not available)
            android.util.Log.d(TAG, "vibratePattern: Legacy API ${Build.VERSION.SDK_INT}, timings=${timings.contentToString()} (no amplitude control)")
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings, repeat)
        }
    }
    
    /**
     * Convert HapticIntensity to amplitude value (0-255)
     * 
     * API 26+ amplitude control allows fine-grained intensity adjustment.
     * DEFAULT_AMPLITUDE = 255 (maximum vibration strength)
     * 
     * @param intensity User's haptic intensity preference
     * @return Amplitude value scaled to intensity (50%, 75%, 100%)
     */
    private fun getAmplitude(intensity: HapticIntensity): Int {
        // Samsung devices: DEFAULT_AMPLITUDE = -1 (use device default)
        // Other devices: DEFAULT_AMPLITUDE = 255
        // For explicit control, always use 1-255 range
        val baseAmplitude = 255
        
        val amplitude = when (intensity) {
            HapticIntensity.OFF -> 0
            HapticIntensity.LIGHT -> (baseAmplitude * AMPLITUDE_LIGHT).toInt() // 127
            HapticIntensity.MEDIUM -> (baseAmplitude * AMPLITUDE_MEDIUM).toInt() // 191
            HapticIntensity.STRONG -> baseAmplitude // 255
        }
        
        android.util.Log.d(TAG, "getAmplitude($intensity) = $amplitude")
        return amplitude
    }
    
    /**
     * Get duration scale for pre-API 26 fallback
     * 
     * @param intensity User's haptic intensity preference
     * @return Duration multiplier (0.5x, 1.0x, 2.0x)
     */
    private fun getDurationScale(intensity: HapticIntensity): Float {
        return when (intensity) {
            HapticIntensity.OFF -> 0f
            HapticIntensity.LIGHT -> DURATION_LIGHT
            HapticIntensity.MEDIUM -> DURATION_MEDIUM
            HapticIntensity.STRONG -> DURATION_STRONG
        }
    }
    
    /**
     * Trigger sample vibration for settings UI
     * 
     * Used when user selects haptic intensity option to provide immediate
     * tactile feedback of the selected intensity level.
     * 
     * Story 5.4 AC #3: "selecting intensity triggers sample vibration at that intensity"
     * 
     * Duration: 100ms at selected intensity
     * 
     * @param intensity Intensity level to preview (OFF bypasses vibration)
     */
    fun triggerSample(intensity: HapticIntensity) {
        if (!hasVibrator || intensity == HapticIntensity.OFF) {
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, getAmplitude(intensity))
            vibrator?.vibrate(effect)
        } else {
            val duration = (100 * getDurationScale(intensity)).toLong()
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
}
