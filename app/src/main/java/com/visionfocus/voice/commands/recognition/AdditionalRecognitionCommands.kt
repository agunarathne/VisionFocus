package com.visionfocus.voice.commands.recognition

import android.content.Context
import android.util.Log
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What Do I See Command
 * Story 3.2 Task 2.15: Replay last recognition result
 * 
 * Announces the last object recognition result.
 * Useful if user wants to hear results again without re-recognizing.
 * 
 * Command variations:
 * - "what do i see"
 * - "last recognition"
 * - "what did you see"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class WhatDoISeeCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "WhatDoISeeCommand"
    }
    
    override val displayName: String = "What Do I See"
    
    override val keywords: List<String> = listOf(
        "what do i see",
        "last recognition",
        "what did you see"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing What Do I See command")
            
            // Placeholder: Retrieve last recognition result
            // Story 4.2 will implement recognition history
            ttsManager.announce("Last recognition result feature coming soon")
            
            Log.d(TAG, "What Do I See command executed")
            CommandResult.Success("What Do I See placeholder")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last recognition", e)
            ttsManager.announce("No recognition result available")
            CommandResult.Failure("What Do I See error: ${e.message}")
        }
    }
}

/**
 * NOTE: HistoryCommand has been MOVED to navigation/NavigationCommands.kt (Story 4.3)
 * with full navigation implementation that opens HistoryFragment.
 * Old placeholder removed to avoid duplicate class conflict.
 */

/**
 * Save Location Command
 * Story 3.2 Task 2.6: Save current GPS location
 * 
 * Saves current GPS location with custom label (Epic 7).
 * Placeholder implementation for Story 3.2.
 * 
 * Command variations:
 * - "save location"
 * - "save here"
 * - "bookmark location"
 * - "remember place"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class SaveLocationCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "SaveLocationCommand"
    }
    
    override val displayName: String = "Save Location"
    
    override val keywords: List<String> = listOf(
        "save location",
        "save here",
        "bookmark location",
        "remember place"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Save Location command")
            
            // Story 7.1: Show SaveLocationDialogFragment
            val activity = context as? androidx.fragment.app.FragmentActivity
            
            if (activity == null) {
                Log.e(TAG, "Context is not FragmentActivity, cannot show dialog")
                ttsManager.announce("Save location unavailable")
                return CommandResult.Failure("Context is not FragmentActivity")
            }
            
            // Show save location dialog (direct instantiation for Hilt injection)
            val dialog = com.visionfocus.ui.savedlocations.SaveLocationDialogFragment()
            dialog.show(activity.supportFragmentManager, "SaveLocationDialog")
            
            Log.d(TAG, "Save Location dialog shown")
            CommandResult.Success("Save location dialog shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show save location dialog", e)
            ttsManager.announce("Save location error")
            CommandResult.Failure("Save Location error: ${e.message}")
        }
    }
}
