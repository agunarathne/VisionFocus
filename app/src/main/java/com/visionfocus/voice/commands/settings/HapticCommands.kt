package com.visionfocus.voice.commands.settings

import android.content.Context
import android.util.Log
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic Off Command
 * Story 5.5 Task 2: Disable haptic feedback
 * 
 * Sets haptic intensity preference to OFF and triggers sample vibration.
 * No tactile feedback will be provided for any actions (zero battery impact).
 * 
 * Command variations:
 * - "haptic off"
 * - "disable haptic"
 * - "vibration off"
 * - "no vibration"
 * - "turn off haptic"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class HapticOffCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HapticOffCommand"
    }
    
    override val displayName: String = "Haptic Off"
    
    override val keywords: List<String> = listOf(
        "haptic off",
        "disable haptic",
        "vibration off",
        "no vibration",
        "turn off haptic"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Haptic Off command")
            
            // Update DataStore preference
            settingsRepository.setHapticIntensity(HapticIntensity.OFF)
            
            // Note: No sample vibration for OFF setting (user wants silence)
            // VoiceCommandProcessor already announced "Haptic feedback off"
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Haptic feedback disabled")
            CommandResult.Success("Haptic feedback disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable haptic feedback", e)
            ttsManager.announce("Unable to change haptic setting")
            CommandResult.Failure("Haptic error: ${e.message}")
        }
    }
}

/**
 * Haptic Light Command
 * Story 5.5 Task 2: Set haptic feedback to light intensity
 * 
 * Sets haptic intensity preference to LIGHT (50% amplitude) and triggers
 * sample vibration at that intensity for immediate tactile confirmation.
 * 
 * Command variations:
 * - "haptic light"
 * - "light vibration"
 * - "gentle haptic"
 * - "soft haptic"
 * - "subtle vibration"
 * 
 * @param settingsRepository Settings persistence layer
 * @param hapticFeedbackManager Haptic feedback controller
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class HapticLightCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HapticLightCommand"
    }
    
    override val displayName: String = "Haptic Light"
    
    override val keywords: List<String> = listOf(
        "haptic light",
        "light vibration",
        "gentle haptic",
        "soft haptic",
        "subtle vibration"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Haptic Light command")
            
            // Update DataStore preference
            settingsRepository.setHapticIntensity(HapticIntensity.LIGHT)
            
            // Trigger sample vibration at new intensity (ButtonPress = 50ms)
            // CRITICAL: HapticFeedbackManager.trigger() must run on Main dispatcher
            // HapticFeedbackManager observes SettingsRepository, will use new LIGHT intensity
            withContext(Dispatchers.Main) {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
            }
            
            // Note: VoiceCommandProcessor already announced "Haptic feedback light"
            // No duplicate TTS announcement needed (learned from Story 3.2)
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Haptic feedback set to light")
            CommandResult.Success("Haptic feedback set to light")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set haptic to light", e)
            ttsManager.announce("Unable to change haptic setting")
            CommandResult.Failure("Haptic error: ${e.message}")
        }
    }
}

/**
 * Haptic Medium Command
 * Story 5.5 Task 2: Set haptic feedback to medium intensity
 * 
 * Sets haptic intensity preference to MEDIUM (75% amplitude, default balanced setting)
 * and triggers sample vibration at that intensity.
 * 
 * Command variations:
 * - "haptic medium"
 * - "medium vibration"
 * - "normal haptic"
 * - "default haptic"
 * - "standard vibration"
 * 
 * @param settingsRepository Settings persistence layer
 * @param hapticFeedbackManager Haptic feedback controller
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class HapticMediumCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HapticMediumCommand"
    }
    
    override val displayName: String = "Haptic Medium"
    
    override val keywords: List<String> = listOf(
        "haptic medium",
        "medium vibration",
        "normal haptic",
        "default haptic",
        "standard vibration"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Haptic Medium command")
            
            // Update DataStore preference
            settingsRepository.setHapticIntensity(HapticIntensity.MEDIUM)
            
            // Trigger sample vibration at new intensity (ButtonPress = 50ms)
            // CRITICAL: HapticFeedbackManager.trigger() must run on Main dispatcher
            // HapticFeedbackManager observes SettingsRepository, will use new MEDIUM intensity
            withContext(Dispatchers.Main) {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
            }
            
            // Note: VoiceCommandProcessor already announced "Haptic feedback medium"
            // No duplicate TTS announcement needed (learned from Story 3.2)
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Haptic feedback set to medium")
            CommandResult.Success("Haptic feedback set to medium")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set haptic to medium", e)
            ttsManager.announce("Unable to change haptic setting")
            CommandResult.Failure("Haptic error: ${e.message}")
        }
    }
}

/**
 * Haptic Strong Command
 * Story 5.5 Task 2: Set haptic feedback to strong intensity
 * 
 * Sets haptic intensity preference to STRONG (100% amplitude, maximum tactile feedback)
 * and triggers sample vibration at that intensity. Critical for deaf-blind users.
 * 
 * Command variations:
 * - "haptic strong"
 * - "strong vibration"
 * - "intense haptic"
 * - "powerful vibration"
 * - "maximum haptic"
 * 
 * @param settingsRepository Settings persistence layer
 * @param hapticFeedbackManager Haptic feedback controller
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class HapticStrongCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HapticStrongCommand"
    }
    
    override val displayName: String = "Haptic Strong"
    
    override val keywords: List<String> = listOf(
        "haptic strong",
        "strong vibration",
        "intense haptic",
        "powerful vibration",
        "maximum haptic"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Haptic Strong command")
            
            // Update DataStore preference
            settingsRepository.setHapticIntensity(HapticIntensity.STRONG)
            
            // Trigger sample vibration at new intensity (ButtonPress = 50ms)
            // CRITICAL: HapticFeedbackManager.trigger() must run on Main dispatcher
            // HapticFeedbackManager observes SettingsRepository, will use new STRONG intensity
            withContext(Dispatchers.Main) {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
            }
            
            // Note: VoiceCommandProcessor already announced "Haptic feedback strong"
            // No duplicate TTS announcement needed (learned from Story 3.2)
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Haptic feedback set to strong")
            CommandResult.Success("Haptic feedback set to strong")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set haptic to strong", e)
            ttsManager.announce("Unable to change haptic setting")
            CommandResult.Failure("Haptic error: ${e.message}")
        }
    }
}
