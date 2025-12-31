package com.visionfocus.voice.processor

import android.content.Context
import android.util.Log
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice Command Processor
 * Story 3.2 Task 1: Central command dispatcher with fuzzy matching
 * 
 * Processes voice transcriptions and executes matching commands.
 * Provides:
 * - Command registry for 15 core commands
 * - Case-insensitive exact matching
 * - Fuzzy matching with Levenshtein distance ≤2
 * - TTS confirmation within 300ms
 * - Haptic feedback on command execution
 * - Helpful error messages for unrecognized commands
 * 
 * Integration Points:
 * - VoiceRecognitionManager provides transcriptions via callback (Story 3.1)
 * - Individual VoiceCommand implementations execute actions
 * - TTSManager announces confirmations and errors
 * - HapticFeedbackManager provides tactile feedback
 * 
 * @param context Application context for command execution
 * @param ttsManager TTS engine for confirmations and errors
 * @param hapticFeedbackManager Haptic feedback for command execution
 * @since Story 3.2
 */
@Singleton
class VoiceCommandProcessor @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) {
    
    // Activity context for navigation commands (Story 3.5)
    // Must be set by MainActivity on creation
    var activityContext: Context? = null
    
    companion object {
        private const val TAG = "VoiceCommandProcessor"
        
        /**
         * Maximum command execution latency (AC: Command execution within 300ms)
         */
        private const val MAX_EXECUTION_TIME_MS = 300
    }
    
    // Command registry: Map<normalized_keyword, VoiceCommand>
    // Lazily initialized to inject all command implementations
    private val commandRegistry: MutableMap<String, VoiceCommand> = mutableMapOf()
    
    // Confirmation messages for each command
    private val confirmationMessages: Map<String, String> = mapOf(
        "Recognize" to "Recognize command received",
        "Navigate" to "Opening navigation",
        "Repeat" to "Repeating last announcement",
        "Cancel" to "Canceling operation",
        "Settings" to "Opening settings",
        "Save Location" to "Saving current location",
        "High Contrast On" to "High contrast on",
        "High Contrast Off" to "High contrast off",
        "Increase Speed" to "Increasing speech speed",
        "Decrease Speed" to "Decreasing speech speed",
        "History" to "Opening history",
        "Help" to "Available commands",
        "Back" to "Going back",
        "Home" to "Going home",
        "Where Am I" to "Getting your location",
        "What Do I See" to "Repeating last recognition"
    )
    
    /**
     * Register a voice command in the registry.
     * Story 3.2 Task 1.3: Command registry building
     * 
     * @param command VoiceCommand implementation to register
     */
    fun registerCommand(command: VoiceCommand) {
        command.keywords.forEach { keyword ->
            val normalizedKeyword = keyword.lowercase().trim()
            commandRegistry[normalizedKeyword] = command
            Log.d(TAG, "Registered command: ${command.displayName} with keyword \"$normalizedKeyword\"")
        }
    }
    
    /**
     * Process voice transcription and execute matching command.
     * Story 3.2 Task 3.3: Command lookup and execution
     * 
     * AC: Command execution within 300ms of recognition
     * 
     * Flow:
     * 1. Normalize transcription (already lowercase from Story 3.1)
     * 2. Exact match lookup in command registry
     * 3. Fuzzy match if no exact match (Levenshtein distance ≤2)
     * 4. Announce TTS confirmation (<300ms target)
     * 5. Trigger haptic feedback
     * 6. Execute command
     * 7. Handle unrecognized commands with helpful message
     * 
     * @param transcription Recognized text from SpeechRecognizer (already lowercase)
     * @return CommandResult indicating success/failure
     */
    suspend fun processCommand(transcription: String): CommandResult = withContext(Dispatchers.Main) {
        val startTime = System.currentTimeMillis()
        
        // 1. Normalize transcription (trim whitespace, already lowercase from Story 3.1)
        val normalized = transcription.trim()
        Log.d(TAG, "Processing command: \"$normalized\"")
        
        // 2. Exact match lookup
        var command = commandRegistry[normalized]
        var matchDistance = 0
        
        // 3. Fuzzy match if no exact match (AC: Task 5)
        if (command == null) {
            val fuzzyMatch = findFuzzyMatch(normalized)
            if (fuzzyMatch != null) {
                command = fuzzyMatch.first
                matchDistance = fuzzyMatch.second
                Log.d(TAG, "Fuzzy match found: \"$normalized\" → \"${command.displayName}\" (distance: $matchDistance)")
            }
        }
        
        // 4. Handle unrecognized command (AC: 5)
        if (command == null) {
            val errorMessage = "Command not recognized. Say 'Help' for available commands."
            // Note: announce() now automatically stops ongoing TTS (Story 3.4 AC #3)
            ttsManager.announce(errorMessage)
            Log.w(TAG, "Unrecognized command: \"$transcription\"")
            return@withContext CommandResult.Failure("Unrecognized command: $transcription")
        }
        
        // 5. Announce confirmation (AC: 4, <300ms target - Story 3.3)
        // Note: announce() automatically stops any ongoing TTS before speaking (Story 3.4 AC #3)
        val confirmationStartTime = System.currentTimeMillis()
        val confirmationMessage = getConfirmationMessage(command, matchDistance)
        ttsManager.announce(confirmationMessage)
        
        // Measure confirmation latency (Story 3.3 AC#1)
        val confirmationLatency = System.currentTimeMillis() - confirmationStartTime
        Log.d(TAG, "Confirmation latency: ${confirmationLatency}ms (target: <300ms)")
        if (confirmationLatency > 300) {
            Log.w(TAG, "PERFORMANCE: Confirmation exceeded 300ms target: ${confirmationLatency}ms")
        }
        
        // 6. Haptic feedback (AC: 4)
        try {
            // Note: trigger() is a suspend function, launch in background scope
            CoroutineScope(Dispatchers.IO).launch {
                hapticFeedbackManager.trigger(HapticPattern.CommandExecuted)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic feedback failed", e)
            // Non-fatal - continue with command execution
        }
        
        // 7. Register operation for cancellation support (Story 3.3 Task 7.1)
        // Note: Some commands (like RecognizeCommand) register their own operations
        // This provides fallback registration for commands that don't self-register
        val operationType = when (command.displayName) {
            "Recognize", "What do I see" -> null // RecognitionViewModel registers
            "Navigate", "Where am I" -> null // NavigationViewModel will register (Epic 6)
            else -> null // Most commands complete immediately, no cancellation needed
        }
        
        // 8. Execute command with appropriate context
        // Story 3.5: Use activityContext (MainActivity) if available for navigation commands
        // Fall back to applicationContext for non-navigation commands
        val contextToUse = activityContext ?: applicationContext
        val result = try {
            command.execute(contextToUse)
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: ${command.displayName}", e)
            ttsManager.announce("Command failed. Please try again.")
            CommandResult.Failure("Execution error: ${e.message}")
        }
        
        // 9. Log execution time for NFR validation (AC: 3)
        val executionTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Command \"${command.displayName}\" executed in ${executionTime}ms (target: <${MAX_EXECUTION_TIME_MS}ms)")
        
        if (executionTime > MAX_EXECUTION_TIME_MS) {
            Log.w(TAG, "Command execution exceeded ${MAX_EXECUTION_TIME_MS}ms target: ${executionTime}ms")
        }
        
        // 10. Analytics hook (Task 3.6 - opt-in analytics)
        // TODO: Emit analytics event when user opts in to analytics
        // AnalyticsManager.logVoiceCommand(
        //     commandName = command.displayName,
        //     success = result is CommandResult.Success,
        //     executionTimeMs = executionTime,
        //     fuzzyMatch = matchDistance > 0
        // )
        
        return@withContext result
    }
    
    /**
     * Find best fuzzy match using Levenshtein distance ≤2.
     * Story 3.2 Task 5.2: Fuzzy matching algorithm
     * 
     * AC: Tolerates minor variations ("recgonize" → "recognize")
     * 
     * IMPORTANT: Special handling for "high contrast of" vs "on"/"off"
     * When last word distances are equal, prefer longer keyword match (off > on)
     * 
     * @param input Transcribed voice input
     * @return Pair of (matching command, distance) or null if no match within threshold
     */
    private fun findFuzzyMatch(input: String): Pair<VoiceCommand, Int>? {
        data class Match(val command: VoiceCommand, val keyword: String, val distance: Int, val lastWordDistance: Int, val keywordLength: Int)
        
        val inputWords = input.split(" ")
        val inputLastWord = if (inputWords.isNotEmpty()) inputWords.last() else ""
        
        val candidates = mutableListOf<Match>()
        
        // Build list of all matching candidates
        commandRegistry.forEach { (keyword, command) ->
            val distance = LevenshteinMatcher.calculateDistance(input, keyword)
            if (LevenshteinMatcher.isMatch(input, keyword)) {
                val keywordWords = keyword.split(" ")
                val keywordLastWord = if (keywordWords.isNotEmpty()) keywordWords.last() else ""
                val lastWordDistance = LevenshteinMatcher.calculateDistance(inputLastWord, keywordLastWord)
                
                candidates.add(Match(command, keyword, distance, lastWordDistance, keywordLastWord.length))
            }
        }
        
        if (candidates.isEmpty()) {
            return null
        }
        
        // Sort by: 1) Last word distance, 2) Keyword last word length (longer = better match for "of" → "off" vs "on")
        // 3) Overall distance
        // This ensures "high contrast of" matches "high contrast off" (3 chars) instead of "high contrast on" (2 chars)
        val bestMatch = candidates.minWithOrNull(compareBy({ it.lastWordDistance }, { -it.keywordLength }, { it.distance }))
        
        if (bestMatch != null) {
            Log.d(TAG, "Best fuzzy match: \"$input\" → \"${bestMatch.keyword}\" (overall: ${bestMatch.distance}, last word: ${bestMatch.lastWordDistance}, length: ${bestMatch.keywordLength})")
            return Pair(bestMatch.command, bestMatch.distance)
        }
        
        return null
    }
    
    /**
     * Get confirmation message for command.
     * Story 3.2 Task 4.2: Confirmation message registry
     * 
     * @param command Command to get confirmation for
     * @param matchDistance Edit distance (0 = exact, >0 = fuzzy)
     * @return Confirmation message string
     */
    private fun getConfirmationMessage(command: VoiceCommand, matchDistance: Int): String {
        val baseMessage = confirmationMessages[command.displayName] 
            ?: "${command.displayName} command received"
        
        // AC: 5.5: Announce fuzzy match for user awareness
        return if (matchDistance > 0) {
            "Did you mean '${command.displayName}'? $baseMessage"
        } else {
            baseMessage
        }
    }
    
    /**
     * Get count of registered commands.
     * Useful for validation and testing.
     * 
     * @return Number of registered commands
     */
    fun getRegisteredCommandCount(): Int {
        val uniqueCommands = commandRegistry.values.toSet()
        return uniqueCommands.size
    }
    
    /**
     * Get all registered command names.
     * Used by HelpCommand to list available commands.
     * 
     * @return List of command display names
     */
    fun getRegisteredCommandNames(): List<String> {
        return commandRegistry.values
            .map { it.displayName }
            .toSet()
            .sorted()
    }
}
