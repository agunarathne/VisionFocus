package com.visionfocus.voice.commands.settings

import android.content.Context
import android.util.Log
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Large Text On Command
 * Story 5.5 Task 1: Enable large text mode
 * 
 * Sets large text preference to true and announces change.
 * Large text mode scales text size across the app for better readability.
 * 
 * Command variations:
 * - "large text on"
 * - "enable large text"
 * - "big text on"
 * - "increase text size"
 * - "bigger text"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class LargeTextOnCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "LargeTextOnCommand"
    }
    
    override val displayName: String = "Large Text On"
    
    override val keywords: List<String> = listOf(
        "large text on",
        "enable large text",
        "big text on",
        "increase text size",
        "bigger text"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Large Text On command")
            
            // Update DataStore preference
            settingsRepository.setLargeTextMode(true)
            
            // Note: VoiceCommandProcessor already announced "Large text on"
            // No duplicate TTS announcement needed (learned from Story 3.2)
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Large text enabled")
            CommandResult.Success("Large text enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable large text", e)
            // Only announce error, not success (VoiceCommandProcessor handles success)
            ttsManager.announce("Unable to change large text setting")
            CommandResult.Failure("Large text error: ${e.message}")
        }
    }
}

/**
 * Large Text Off Command
 * Story 5.5 Task 1: Disable large text mode
 * 
 * Sets large text preference to false and announces change.
 * Restores normal text size across the app.
 * 
 * Command variations:
 * - "large text off"
 * - "disable large text"
 * - "big text off"
 * - "decrease text size"
 * - "normal text"
 * - "smaller text"
 * 
 * @param settingsRepository Settings persistence layer
 * @param ttsManager TTS engine for error announcements
 * @since Story 5.5
 */
@Singleton
class LargeTextOffCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "LargeTextOffCommand"
    }
    
    override val displayName: String = "Large Text Off"
    
    override val keywords: List<String> = listOf(
        "large text off",
        "disable large text",
        "big text off",
        "decrease text size",
        "normal text",
        "smaller text"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Large Text Off command")
            
            // Update DataStore preference
            settingsRepository.setLargeTextMode(false)
            
            // Note: VoiceCommandProcessor already announced "Large text off"
            // No duplicate TTS announcement needed (learned from Story 3.2)
            // UI will update reactively via SettingsViewModel StateFlow observation
            
            Log.d(TAG, "Large text disabled")
            CommandResult.Success("Large text disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable large text", e)
            ttsManager.announce("Unable to change large text setting")
            CommandResult.Failure("Large text error: ${e.message}")
        }
    }
}
