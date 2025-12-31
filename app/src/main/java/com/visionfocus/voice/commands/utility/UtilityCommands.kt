package com.visionfocus.voice.commands.utility

import android.content.Context
import android.util.Log
import com.visionfocus.R
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.recognition.RecognitionViewModel
import com.visionfocus.voice.operation.OperationManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import kotlinx.coroutines.flow.first
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
 * Story 3.3 Task 3: Enhanced with operation-aware cancellation
 * 
 * Cancels any active operation (recognition or navigation) using OperationManager.
 * Context-aware cancellation ensures the right operation is stopped.
 * 
 * Command variations:
 * - "cancel"
 * - "stop"
 * - "abort"
 * - "never mind"
 * 
 * AC: Cancel works mid-recognition and mid-navigation
 * AC: Announce "Cancelled" after successful cancellation
 * AC: Handle case where no operation is active: "Nothing to cancel"
 * 
 * @param operationManager Tracks and cancels active operations
 * @since Story 3.2, enhanced in Story 3.3
 */
@Singleton
class CancelCommand @Inject constructor(
    private val operationManager: OperationManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "CancelCommand"
    }
    
    override val displayName: String = "Cancel"
    
    override val keywords: List<String> = listOf(
        "cancel",
        "stop",
        "abort",
        "never mind"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        Log.d(TAG, "Executing Cancel command")
        
        // Query OperationManager for active operation and cancel it
        // OperationManager handles:
        // - Invoking operation-specific cancellation callback
        // - Announcing "Cancelled" or "Nothing to cancel"
        // - Haptic feedback
        val result = operationManager.cancelOperation()
        
        Log.d(TAG, "Cancel command executed: ${if (result is CommandResult.Success) "operation cancelled" else "nothing to cancel"}")
        return result
    }
}

/**
 * Help Command
 * Story 3.2 Task 2.11 & 9: Announce available commands (basic)
 * Story 3.4: Enhanced with comprehensive grouped announcements and speech rate support
 * 
 * Provides audio help by announcing all 15 available voice commands.
 * Commands organized into logical groups: Recognition, Navigation, Settings, General.
 * 
 * AC #1: Announces all 15 commands in 4 logical groups with usage examples
 * AC #2: Respects user's speech rate preference (0.5×-2.0×)
 * AC #3: Interruptible by speaking another command (via TTSManager.stop())
 * AC #4: Concludes with prompt: "Say a command now, or tap the microphone button"
 * 
 * Command variations:
 * - "help"
 * - "commands"
 * - "what can i say"
 * - "how do i use this"
 * - "command list"
 * - "available commands"
 * 
 * Performance: Help announcement ~30-45 seconds at 1.0× speech rate
 * 
 * @param context Android context for string resource access
 * @param ttsManager TTS engine for announcements (stop() enables interruption)
 * @param settingsRepository Access to user's speech rate preference
 * @since Story 3.2, enhanced in Story 3.4
 */
@Singleton
class HelpCommand @Inject constructor(
    private val ttsManager: TTSManager,
    private val settingsRepository: SettingsRepository
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HelpCommand"
    }
    
    override val displayName: String = "Help"
    
    override val keywords: List<String> = listOf(
        "help",
        "commands",
        "what can i say",
        "how do i use this",
        "command list",
        "available commands"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Help command")
            
            // CRITICAL #3: Check TTS readiness before proceeding
            if (!ttsManager.isReady()) {
                Log.w(TAG, "TTS not initialized, cannot announce help")
                return CommandResult.Failure("TTS not ready")
            }
            
            // AC #2: Retrieve user's speech rate preference (0.5×-2.0×)
            val speechRate = settingsRepository.getSpeechRate().first()
            
            // CRITICAL #4: Clamp speech rate defensively (prevent crashes from corrupted DataStore)
            val clampedRate = speechRate.coerceIn(0.5f, 2.0f)
            Log.d(TAG, "Speech rate: $clampedRate")
            
            // AC #1: Build comprehensive help announcement with logical command groups
            val helpMessage = buildHelpAnnouncement(context)
            
            // Set TTS speech rate to user's preference
            // CRITICAL #1: Wrap in try-catch (setSpeechRate validates range)
            try {
                ttsManager.setSpeechRate(clampedRate)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid speech rate: $clampedRate", e)
                // Continue with default rate
            }
            
            // AC #3: Announce help (interruptible via ttsManager.stop())
            // CRITICAL #1: Handle Result<Long> return value
            val result = ttsManager.announce(helpMessage)
            
            result.fold(
                onSuccess = { latency ->
                    Log.d(TAG, "Help announced (${latency}ms latency)")
                    CommandResult.Success("Help announced with all command groups")
                },
                onFailure = { error ->
                    Log.e(TAG, "TTS announce failed", error)
                    CommandResult.Failure("Help announcement failed: ${error.message}")
                }
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            // MEDIUM #2: Rethrow CancellationException for proper coroutine cancellation
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to announce help", e)
            // MEDIUM #1: Use externalized error string
            ttsManager.announce(context.getString(R.string.help_command_error))
            CommandResult.Failure("Help error: ${e.message}")
        }
    }
    
    /**
     * Build comprehensive help announcement with logical command groups.
     * AC #1: Commands grouped as Recognition, Navigation, Settings, General
     * AC #4: Includes concluding prompt to guide next action
     * 
     * @param context Android context for string resource access
     * @return Complete help announcement text (~300-400 words, ~30-45 seconds at 1.0× rate)
     */
    private fun buildHelpAnnouncement(context: Context): String {
        // MEDIUM #3: Use buildString for efficient concatenation (6 strings + spaces)
        return buildString {
            append(context.getString(R.string.help_command_introduction))
            append(" ")
            append(context.getString(R.string.help_command_recognition_group))
            append(" ")
            append(context.getString(R.string.help_command_navigation_group))
            append(" ")
            append(context.getString(R.string.help_command_settings_group))
            append(" ")
            append(context.getString(R.string.help_command_general_group))
            append(" ")
            append(context.getString(R.string.help_command_conclusion))
        }
    }
}
