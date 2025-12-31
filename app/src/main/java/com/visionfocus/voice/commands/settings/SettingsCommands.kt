package com.visionfocus.voice.commands.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings Command
 * Story 3.2 Task 2.5 & 7.1: Open settings screen
 * 
 * Navigates to SettingsActivity/Fragment.
 * 
 * Command variations:
 * - "settings"
 * - "preferences"
 * - "options"
 * - "config"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class SettingsCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "SettingsCommand"
    }
    
    override val displayName: String = "Settings"
    
    override val keywords: List<String> = listOf(
        "settings",
        "preferences",
        "options",
        "config"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Settings command")
            
            // Story 3.5: Navigate to settings using MainActivity
            if (context is com.visionfocus.MainActivity) {
                context.runOnUiThread {
                    context.navigateToSettings()
                }
                
                ttsManager.announce("Settings")
                
                Log.d(TAG, "Settings command executed - navigated to settings")
                CommandResult.Success("Navigated to settings")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Settings error: ${e.message}")
        }
    }
}

/**
 * High Contrast On Command
 * Story 3.2 Task 2.7 & 7.2: Enable high contrast mode
 * 
 * Sets high contrast preference to true and announces change.
 * 
 * Command variations:
 * - "high contrast on"
 * - "enable high contrast"
 * - "turn on high contrast"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class HighContrastOnCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HighContrastOnCommand"
    }
    
    override val displayName: String = "High Contrast On"
    
    override val keywords: List<String> = listOf(
        "high contrast on",
        "enable high contrast",
        "turn on high contrast",
        "contrast on",
        "dark mode on",
        "enable dark mode",
        "hi contrast on"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing High Contrast On command")
            
            // Enable high contrast mode
            settingsRepository.setHighContrastMode(true)
            
            // Note: VoiceCommandProcessor already announced "High contrast on"
            // No need for duplicate TTS announcement here
            
            Log.d(TAG, "High contrast enabled")
            CommandResult.Success("High contrast enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable high contrast", e)
            ttsManager.announce("Unable to change high contrast setting")
            CommandResult.Failure("High contrast error: ${e.message}")
        }
    }
}

/**
 * High Contrast Off Command
 * Story 3.2 Task 2.7 & 7.3: Disable high contrast mode
 * 
 * Sets high contrast preference to false and announces change.
 * 
 * Command variations:
 * - "high contrast off"
 * - "disable high contrast"
 * - "turn off high contrast"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class HighContrastOffCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HighContrastOffCommand"
    }
    
    override val displayName: String = "High Contrast Off"
    
    override val keywords: List<String> = listOf(
        "high contrast off",
        "disable high contrast",
        "turn off high contrast",
        "contrast off",
        "dark mode off",
        "disable dark mode",
        "hi contrast off"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing High Contrast Off command")
            
            // Disable high contrast mode
            settingsRepository.setHighContrastMode(false)
            
            // Note: VoiceCommandProcessor already announced "High contrast off"
            // No need for duplicate TTS announcement here
            
            Log.d(TAG, "High contrast disabled")
            CommandResult.Success("High contrast disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable high contrast", e)
            ttsManager.announce("Unable to change high contrast setting")
            CommandResult.Failure("High contrast error: ${e.message}")
        }
    }
}

/**
 * Increase Speed Command
 * Story 3.2 Task 2.8 & 7.4: Increment TTS speech rate
 * 
 * Increases TTS rate by 0.25× and announces new rate.
 * Handles maximum rate limit (2.0×).
 * 
 * Command variations:
 * - "increase speed"
 * - "faster"
 * - "speed up"
 * - "talk faster"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class IncreaseSpeedCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "IncreaseSpeedCommand"
        private const val SPEED_INCREMENT = 0.25f
        private const val MAX_SPEED = 2.0f
    }
    
    override val displayName: String = "Increase Speed"
    
    override val keywords: List<String> = listOf(
        "increase speed",
        "faster",
        "speed up",
        "talk faster"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Increase Speed command")
            
            // Get current rate
            val currentRate = settingsRepository.getSpeechRate().first()
            
            // Calculate new rate with limit
            val newRate = (currentRate + SPEED_INCREMENT).coerceAtMost(MAX_SPEED)
            
            if (newRate == currentRate) {
                // Already at maximum
                ttsManager.announce("Speech rate already at maximum")
                return CommandResult.Success("Already at max rate")
            }
            
            // Save new rate
            settingsRepository.setSpeechRate(newRate)
            
            // Announce new rate (specific feedback, processor already said "Increasing speech speed")
            ttsManager.announce("Now at ${newRate} times normal speed")
            
            Log.d(TAG, "Speech rate increased to $newRate")
            CommandResult.Success("Speech rate: $newRate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increase speech rate", e)
            ttsManager.announce("Unable to change speech rate")
            CommandResult.Failure("Speed error: ${e.message}")
        }
    }
}

/**
 * Decrease Speed Command
 * Story 3.2 Task 2.9 & 7.5: Decrement TTS speech rate
 * 
 * Decreases TTS rate by 0.25× and announces new rate.
 * Handles minimum rate limit (0.5×).
 * 
 * Command variations:
 * - "decrease speed"
 * - "slower"
 * - "slow down"
 * - "talk slower"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class DecreaseSpeedCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "DecreaseSpeedCommand"
        private const val SPEED_DECREMENT = 0.25f
        private const val MIN_SPEED = 0.5f
    }
    
    override val displayName: String = "Decrease Speed"
    
    override val keywords: List<String> = listOf(
        "decrease speed",
        "slower",
        "slow down",
        "talk slower"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Decrease Speed command")
            
            // Get current rate
            val currentRate = settingsRepository.getSpeechRate().first()
            
            // Calculate new rate with limit
            val newRate = (currentRate - SPEED_DECREMENT).coerceAtLeast(MIN_SPEED)
            
            if (newRate == currentRate) {
                // Already at minimum
                ttsManager.announce("Speech rate already at minimum")
                return CommandResult.Success("Already at min rate")
            }
            
            // Save new rate
            settingsRepository.setSpeechRate(newRate)
            
            // Announce new rate (specific feedback, processor already said "Decreasing speech speed")
            ttsManager.announce("Now at ${newRate} times normal speed")
            
            Log.d(TAG, "Speech rate decreased to $newRate")
            CommandResult.Success("Speech rate: $newRate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrease speech rate", e)
            ttsManager.announce("Unable to change speech rate")
            CommandResult.Failure("Speed error: ${e.message}")
        }
    }
}
