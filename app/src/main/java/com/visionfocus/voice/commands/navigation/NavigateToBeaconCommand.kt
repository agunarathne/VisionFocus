package com.visionfocus.voice.commands.navigation

import android.content.Context
import android.util.Log
import com.visionfocus.beacon.ProximityNavigationService
import com.visionfocus.data.repository.BeaconRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.LevenshteinMatcher
import com.visionfocus.voice.processor.VoiceCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigate To Beacon Command
 * Epic 10 Story 10.2: Voice command for beacon-based indoor navigation
 * 
 * Directly starts proximity navigation to a paired beacon by name.
 * Uses RSSI-based guidance with audio + haptic feedback.
 * 
 * Command variations:
 * - "take me to Living Room"
 * - "go to Kitchen"
 * - "navigate to Bedroom"
 * 
 * Flow:
 * 1. Extract beacon name from transcription
 * 2. Try exact match (case-insensitive)
 * 3. Try fuzzy match if no exact match
 * 4. Start ProximityNavigationService with target beacon
 * 
 * @param beaconRepository Access to paired beacons database
 * @param proximityNav Proximity navigation service
 * @param ttsManager TTS engine for announcements
 * @since Epic 10 Story 10.2
 */
@Singleton
class NavigateToBeaconCommand @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val proximityNav: ProximityNavigationService,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "NavigateToBeaconCommand"
        private const val TRANSCRIPTION_PREF = "voice"
        private const val TRANSCRIPTION_KEY = "last_transcription"
        private const val FUZZY_MATCH_THRESHOLD = 2
    }
    
    // Dedicated scope for navigation lifecycle
    private val navigationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override val displayName: String = "Navigate To Beacon"
    
    override val keywords: List<String> = listOf(
        "find",
        "locate",
        "track",
        "search for",
        "where is"
    )
    
    /**
     * Epic 10 Story 10.2: Execute navigate to beacon command.
     * 
     * Flow:
     * 1. Extract beacon name from transcription
     * 2. Try exact match (case-insensitive)
     * 3. Try fuzzy match if no exact match (Levenshtein distance ≤ 2)
     * 4. Start proximity navigation if beacon found
     * 5. Announce error if beacon not paired
     */
    override suspend fun execute(context: Context): CommandResult {
        return try {
            // Get transcription from SharedPreferences
            val prefs = context.getSharedPreferences(TRANSCRIPTION_PREF, Context.MODE_PRIVATE)
            val transcription = prefs.getString(TRANSCRIPTION_KEY, "") ?: ""
            
            Log.d(TAG, "Processing beacon navigation command: $transcription")
            
            // Extract beacon name from transcription
            val beaconName = parseBeaconName(transcription)
            if (beaconName.isBlank()) {
                ttsManager.announce("Beacon name not recognized")
                return CommandResult.Failure("No beacon name found in transcription")
            }
            
            Log.d(TAG, "Parsed beacon name: $beaconName")
            
            // Try exact match (case-insensitive)
            val exactMatch = beaconRepository.findByName(beaconName)
            
            if (exactMatch != null) {
                Log.d(TAG, "Exact match found: ${exactMatch.name} (${exactMatch.macAddress})")
                startBeaconNavigation(exactMatch.macAddress, exactMatch.name)
                return CommandResult.Success("Navigating to ${exactMatch.name}")
            }
            
            // Try fuzzy match
            val allBeaconNames = beaconRepository.getAllBeaconNames()
            if (allBeaconNames.isEmpty()) {
                Log.d(TAG, "No beacons paired")
                ttsManager.announce("No beacons paired. Please pair a beacon first.")
                return CommandResult.Failure("No beacons paired")
            }
            
            val fuzzyMatches = allBeaconNames.mapNotNull { savedName ->
                val distance = LevenshteinMatcher.calculateDistance(
                    beaconName.lowercase(),
                    savedName.lowercase()
                )
                if (distance <= FUZZY_MATCH_THRESHOLD) {
                    savedName to distance
                } else null
            }.sortedBy { it.second }
            
            if (fuzzyMatches.isEmpty()) {
                Log.d(TAG, "No matches found for: $beaconName")
                ttsManager.announce("Beacon $beaconName not found")
                return CommandResult.Failure("No beacon match found")
            }
            
            // Use best fuzzy match
            val matchedName = fuzzyMatches.first().first
            val beacon = beaconRepository.findByName(matchedName)
            
            if (beacon != null) {
                Log.d(TAG, "Fuzzy match found: ${beacon.name} (${beacon.macAddress})")
                startBeaconNavigation(beacon.macAddress, beacon.name)
                return CommandResult.Success("Navigating to ${beacon.name}")
            }
            
            ttsManager.announce("Beacon not found")
            CommandResult.Failure("Beacon not found")
            
        } catch (e: Exception) {
            Log.e(TAG, "Navigate to beacon error", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Navigation error: ${e.message}")
        }
    }
    
    /**
     * Parse beacon name from transcription.
     * Removes trigger keywords ("take me to", "go to", etc.)
     */
    private fun parseBeaconName(transcription: String): String {
        val lowerTranscription = transcription.lowercase()
        
        // Remove trigger keywords
        val triggers = keywords
        var beaconName = lowerTranscription
        
        for (trigger in triggers) {
            if (lowerTranscription.contains(trigger)) {
                beaconName = lowerTranscription.substringAfter(trigger).trim()
                break
            }
        }
        
        return beaconName.trim()
    }
    
    /**
     * Start proximity navigation to beacon.
     */
    private fun startBeaconNavigation(macAddress: String, name: String) {
        Log.d(TAG, "Starting proximity navigation: $name ($macAddress)")
        proximityNav.startNavigation(macAddress, name, navigationScope)
    }
}
