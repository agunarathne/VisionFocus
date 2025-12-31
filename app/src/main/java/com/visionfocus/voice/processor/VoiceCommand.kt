package com.visionfocus.voice.processor

import android.content.Context

/**
 * Voice Command Interface
 * Story 3.2 Task 1.2: Command pattern for extensible command registry
 * 
 * Each voice command implements this interface to provide:
 * - Execution logic
 * - Display name for confirmations and help
 * - Keywords and variations that trigger this command
 * 
 * @since Story 3.2
 */
interface VoiceCommand {
    /**
     * Execute the command.
     * 
     * @param context Application context for accessing system services
     * @return CommandResult indicating success/failure with message
     */
    suspend fun execute(context: Context): CommandResult
    
    /**
     * Get display name for this command (used in Help and confirmations).
     * 
     * Example: "Recognize", "Settings", "High Contrast On"
     */
    val displayName: String
    
    /**
     * Get command keywords and variations that trigger this command.
     * 
     * Keywords should be lowercase for matching against normalized transcription.
     * Include synonyms and common misspellings.
     * 
     * Example: ["recognize", "recognition", "recognise", "identify"]
     * 
     * @return List of keywords including synonyms and common variations
     */
    val keywords: List<String>
}
