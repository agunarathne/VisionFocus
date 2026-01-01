package com.visionfocus.voice.commands.settings

import android.content.Context
import com.visionfocus.R
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.VoiceCommand
import com.visionfocus.voice.processor.CommandResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice command to set verbosity mode to BRIEF.
 * 
 * Story 4.1 AC6-AC7: Voice command support for verbosity modes
 * 
 * Keywords:
 * - "verbosity brief"
 * - "brief mode"
 * - "set brief"
 * - "simple announcements"
 * 
 * Effect:
 * - Sets verbosity mode to BRIEF in DataStore
 * - Announces confirmation: "Verbosity set to brief"
 * - Next recognition uses brief mode (category only)
 * 
 * Example:
 * User says: "Verbosity brief"
 * Response: "Verbosity set to brief" (TTS)
 * Next recognition: "Chair" (instead of "Chair with high confidence")
 */
@Singleton
class VerbosityBriefCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val displayName: String = "Verbosity Brief"
    
    override val keywords: List<String> = listOf(
        "verbosity brief",
        "brief mode",
        "set brief",
        "simple announcements",
        "short announcements"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            // Update verbosity mode in preferences
            settingsRepository.setVerbosity(VerbosityMode.BRIEF)
            
            // Announce confirmation
            val confirmationMessage = context.getString(R.string.verbosity_set_to_brief)
            ttsManager.announce(confirmationMessage)
            
            CommandResult.Success(confirmationMessage)
        } catch (e: Exception) {
            android.util.Log.e("VerbosityBriefCommand", "Failed to set verbosity mode to BRIEF", e)
            CommandResult.Failure("Failed to set verbosity mode: ${e.message}")
        }
    }
}

/**
 * Voice command to set verbosity mode to STANDARD.
 * 
 * Story 4.1 AC6-AC7: Voice command support for verbosity modes
 * 
 * Keywords:
 * - "verbosity standard"
 * - "standard mode"
 * - "set standard"
 * - "normal announcements"
 * 
 * Effect:
 * - Sets verbosity mode to STANDARD in DataStore
 * - Announces confirmation: "Verbosity set to standard"
 * - Next recognition uses standard mode (category + confidence)
 * 
 * Example:
 * User says: "Verbosity standard"
 * Response: "Verbosity set to standard" (TTS)
 * Next recognition: "Chair with high confidence"
 */
@Singleton
class VerbosityStandardCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val displayName: String = "Verbosity Standard"
    
    override val keywords: List<String> = listOf(
        "verbosity standard",
        "standard mode",
        "set standard",
        "normal announcements",
        "default announcements"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            // Update verbosity mode in preferences
            settingsRepository.setVerbosity(VerbosityMode.STANDARD)
            
            // Announce confirmation
            val confirmationMessage = context.getString(R.string.verbosity_set_to_standard)
            ttsManager.announce(confirmationMessage)
            
            CommandResult.Success(confirmationMessage)
        } catch (e: Exception) {
            android.util.Log.e("VerbosityStandardCommand", "Failed to set verbosity mode to STANDARD", e)
            CommandResult.Failure("Failed to set verbosity mode: ${e.message}")
        }
    }
}

/**
 * Voice command to set verbosity mode to DETAILED.
 * 
 * Story 4.1 AC6-AC7: Voice command support for verbosity modes
 * 
 * Keywords:
 * - "verbosity detailed"
 * - "detailed mode"
 * - "set detailed"
 * - "full announcements"
 * 
 * Effect:
 * - Sets verbosity mode to DETAILED in DataStore
 * - Announces confirmation: "Verbosity set to detailed"
 * - Next recognition uses detailed mode (category + confidence + position + count)
 * 
 * Example:
 * User says: "Verbosity detailed"
 * Response: "Verbosity set to detailed" (TTS)
 * Next recognition: "High confidence: chair in center of view. Two chairs detected."
 */
@Singleton
class VerbosityDetailedCommand @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val displayName: String = "Verbosity Detailed"
    
    override val keywords: List<String> = listOf(
        "verbosity detailed",
        "detailed mode",
        "set detailed",
        "full announcements",
        "complete announcements",
        "verbose announcements"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            // Update verbosity mode in preferences
            settingsRepository.setVerbosity(VerbosityMode.DETAILED)
            
            // Announce confirmation
            val confirmationMessage = context.getString(R.string.verbosity_set_to_detailed)
            ttsManager.announce(confirmationMessage)
            
            CommandResult.Success(confirmationMessage)
        } catch (e: Exception) {
            android.util.Log.e("VerbosityDetailedCommand", "Failed to set verbosity mode to DETAILED", e)
            CommandResult.Failure("Failed to set verbosity mode: ${e.message}")
        }
    }
}
