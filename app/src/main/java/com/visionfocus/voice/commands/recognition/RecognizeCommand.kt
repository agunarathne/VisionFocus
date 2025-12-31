package com.visionfocus.voice.commands.recognition

import android.content.Context
import android.content.Intent
import android.util.Log
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recognize Command
 * Story 3.2 Task 2.1 & 6: Trigger object recognition via voice
 * 
 * Executes the same recognition flow as tapping the FAB button.
 * Broadcasts intent to MainActivity to trigger recognition.
 * 
 * Command variations:
 * - "recognize"
 * - "recognition"
 * - "recognise" (British spelling)
 * - "identify"
 * - "what is this"
 * - "what is that"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2
 */
@Singleton
class RecognizeCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "RecognizeCommand"
        const val ACTION_RECOGNIZE = "com.visionfocus.ACTION_RECOGNIZE"
    }
    
    override val displayName: String = "Recognize"
    
    override val keywords: List<String> = listOf(
        "recognize",     // Primary keyword
        "recognition",   // Noun variation
        "recognise",     // British spelling
        "identify",      // Synonym
        "what is this",  // Natural language variation
        "what is that",
        "what do i see"  // Overlaps with WhatDoISeeCommand but acceptable
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Recognize command")
            
            // Note: VoiceCommandProcessor already announced "Recognize command received"
            // Only announce camera start when Epic 2 implementation is ready
            
            // Broadcast intent to MainActivity to trigger recognition
            val intent = Intent(ACTION_RECOGNIZE).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Recognition broadcast sent")
            CommandResult.Success("Recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recognition", e)
            ttsManager.announce("Failed to start recognition. Please try again.")
            CommandResult.Failure("Recognition error: ${e.message}")
        }
    }
}
