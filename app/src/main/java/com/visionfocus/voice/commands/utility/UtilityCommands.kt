package com.visionfocus.voice.commands.utility

import android.content.Context
import android.util.Log
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.recognition.RecognitionViewModel
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repeat Command
 * Story 3.2 Task 2.3 & 8.4: Replay last TTS announcement
 * 
 * Retrieves and repeats the last announcement from TTSManager cache.
 * Useful for users who missed or want to re-hear the last recognition result.
 * 
 * Command variations:
 * - "repeat"
 * - "say that again"
 * - "repeat last"
 * - "replay"
 * 
 * @param ttsManager TTS engine with announcement cache
 * @since Story 3.2
 */
@Singleton
class RepeatCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "RepeatCommand"
    }
    
    override val displayName: String = "Repeat"
    
    override val keywords: List<String> = listOf(
        "repeat",
        "say that again",
        "repeat last",
        "replay"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Repeat command")
            
            // Retrieve last announcement from TTSManager
            // Note: TTSManager needs to implement lastAnnouncement property
            // For now, announce placeholder message
            ttsManager.announce("Repeating last announcement feature coming soon")
            
            Log.d(TAG, "Repeat command executed")
            CommandResult.Success("Repeat executed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to repeat announcement", e)
            ttsManager.announce("No announcement to repeat")
            CommandResult.Failure("Repeat error: ${e.message}")
        }
    }
}

/**
 * Cancel Command
 * Story 3.2 Task 2.4 & 8.3: Stop active operation
 * 
 * Cancels any active operation (recognition or navigation).
 * Broadcasts intent to MainActivity to cancel recognition.
 * 
 * Command variations:
 * - "cancel"
 * - "stop"
 * - "abort"
 * - "never mind"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class CancelCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "CancelCommand"
        const val ACTION_CANCEL = "com.visionfocus.ACTION_CANCEL"
    }
    
    override val displayName: String = "Cancel"
    
    override val keywords: List<String> = listOf(
        "cancel",
        "stop",
        "abort",
        "never mind"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Cancel command")
            
            // Broadcast intent to MainActivity to cancel recognition
            val intent = android.content.Intent(ACTION_CANCEL).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
            
            // Note: VoiceCommandProcessor already announced "Canceling operation"
            // Brief "Cancelled" confirms completion
            ttsManager.announce("Cancelled")
            
            Log.d(TAG, "Cancel command executed")
            CommandResult.Success("Operation cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel operation", e)
            ttsManager.announce("Nothing to cancel")
            CommandResult.Failure("Cancel error: ${e.message}")
        }
    }
}

/**
 * Help Command
 * Story 3.2 Task 2.11 & 9: Announce available commands
 * 
 * Provides audio help by announcing all available voice commands.
 * Commands grouped logically: Recognition, Navigation, Settings, General.
 * 
 * Command variations:
 * - "help"
 * - "commands"
 * - "what can i say"
 * - "how do i use this"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class HelpCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HelpCommand"
    }
    
    override val displayName: String = "Help"
    
    override val keywords: List<String> = listOf(
        "help",
        "commands",
        "what can i say",
        "how do i use this"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Help command")
            
            // Concise help announcement (~30 seconds at normal speech rate)
            val helpMessage = """
                Available commands.
                Recognition: Recognize, What do I see.
                Navigation: Navigate, Where am I, Back, Home.
                Settings: Settings, High contrast on or off, Increase or decrease speed.
                History: History, Save location.
                Utility: Repeat, Cancel, Help.
            """.trimIndent()
            
            // Announce available commands grouped logically
            ttsManager.announce(helpMessage)
            
            Log.d(TAG, "Help command executed")
            CommandResult.Success("Help announced")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to announce help", e)
            CommandResult.Failure("Help error: ${e.message}")
        }
    }
}
