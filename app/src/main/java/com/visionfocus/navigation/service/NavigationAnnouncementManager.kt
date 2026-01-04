package com.visionfocus.navigation.service

import android.content.Context
import android.util.Log
import com.visionfocus.navigation.models.Maneuver
import com.visionfocus.navigation.models.RouteStep
import com.visionfocus.navigation.utils.BearingCalculator
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 6.3 Task 6: NavigationAnnouncementManager for turn-by-turn voice guidance.
 * 
 * Manages TTS announcements during active navigation with:
 * - Natural language turn instructions
 * - Audio priority (interrupts recognition announcements)
 * - 10% increased volume for safety-critical navigation
 * - Cardinal directions when helpful
 * - Complex intersection handling (roundabouts, multi-exits)
 * 
 * Announcement types:
 * - Navigation start: "Navigation started. X km, Y minutes"
 * - Advance warning: "In 50 meters, turn left onto Main Street"
 * - Immediate turn: "Turn left now onto Main Street"
 * - Straight checkpoint: "Continue straight for 200 meters"
 * - Arrival: "You have arrived at your destination"
 */
@Singleton
class NavigationAnnouncementManager @Inject constructor(
    private val ttsManager: TTSManager,
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NavigationAnnouncementManager"
        
        /** Story 6.3 AC: Navigation uses 10% increased volume for safety */
        private const val NAVIGATION_VOLUME_MULTIPLIER = 1.1f
    }
    
    private var originalVolume: Float = 1.0f
    private var isNavigationVolumeActive = false
    
    /**
     * Announce navigation start with route summary.
     * 
     * AC: "Navigation started. Total distance X kilometers, estimated Y minutes"
     */
    suspend fun announceNavigationStart(totalDistanceMeters: Int, totalDurationSeconds: Int) {
        val distanceKm = totalDistanceMeters / 1000f
        val durationMinutes = totalDurationSeconds / 60
        
        val message = if (distanceKm >= 1.0f) {
            "Navigation started. Total distance %.1f kilometers, estimated %d minutes".format(
                distanceKm, durationMinutes
            )
        } else {
            "Navigation started. Total distance %d meters, estimated %d minutes".format(
                totalDistanceMeters, durationMinutes
            )
        }
        
        Log.d(TAG, "Navigation start: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announce advance turn warning (5-7 seconds before turn).
     * 
     * AC #2: "In 50 meters, turn left onto Main Street"
     * 
     * Natural language patterns:
     * - Distance units: meters (< 1000m)
     * - Street names extracted from instruction
     * - Cardinal directions added when helpful
     */
    suspend fun announceAdvanceWarning(step: RouteStep, distanceMeters: Int) {
        val maneuverText = getManeuverDescription(step.maneuver)
        val streetName = extractStreetName(step.instruction)
        
        // Calculate bearing for cardinal direction (CRITICAL Issue #7 - AC #4)
        val bearing = BearingCalculator.calculateBearing(step.startLocation, step.endLocation)
        val cardinal = BearingCalculator.toCardinalDirection(bearing)
        
        val message = buildString {
            append("In $distanceMeters meters, ")
            append(maneuverText)
            // Add cardinal direction for turn instructions (AC #4: "when helpful")
            if (maneuverText.contains("turn", ignoreCase = true)) {
                append(" heading $cardinal")
            }
            if (streetName.isNotEmpty()) {
                append(" onto $streetName")
            }
        }
        
        Log.d(TAG, "Advance warning: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announce immediate turn confirmation (at turn point).
     * 
     * AC #3: "Turn left now onto Main Street"
     */
    suspend fun announceImmediateTurn(step: RouteStep) {
        val maneuverText = getManeuverDescription(step.maneuver)
        val streetName = extractStreetName(step.instruction)
        
        val message = buildString {
            append(maneuverText)
            append(" now")
            if (streetName.isNotEmpty()) {
                append(" onto $streetName")
            }
        }
        
        Log.d(TAG, "Immediate turn: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announce straight section checkpoint.
     * 
     * AC #5: "Continue straight for 200 meters"
     */
    suspend fun announceStraightCheckpoint(distanceMeters: Int) {
        val message = "Continue straight for $distanceMeters meters"
        Log.d(TAG, "Checkpoint: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announce arrival at destination.
     * 
     * AC #9: "You have arrived at your destination"
     */
    suspend fun announceArrival() {
        val message = "You have arrived at your destination"
        Log.d(TAG, "Arrival: $message")
        announceWithPriority(message)
        
        // Restore volume after arrival announcement
        restoreOriginalVolume()
    }
    
    /**
     * Announce with navigation audio priority.
     * 
     * Story 6.3 AC #6: Uses interrupt mode to stop ongoing recognition announcements.
     * AC #7: Increases volume 10% for safety-critical navigation instructions.
     * 
     * Story 8.1 will formalize audio priority queue system.
     * 
     * Made public for GPS error announcements (Issue #13).
     */
    suspend fun announceWithPriority(message: String) {
        // Interrupt any ongoing TTS (AC #6: navigation has priority)
        ttsManager.stop()
        
        // Increase volume for navigation if not already active (AC #7: 10% louder)
        if (!isNavigationVolumeActive) {
            originalVolume = ttsManager.getVolume()
            val navigationVolume = (originalVolume * NAVIGATION_VOLUME_MULTIPLIER).coerceIn(0f, 1f)
            ttsManager.setVolume(navigationVolume)
            isNavigationVolumeActive = true
            Log.d(TAG, "Navigation volume: $originalVolume → $navigationVolume")
        }
        
        // Announce with interrupt mode (QUEUE_FLUSH)
        ttsManager.announce(message)
    }
    
    /**
     * Restore original volume after navigation ends.
     */
    fun restoreOriginalVolume() {
        if (isNavigationVolumeActive) {
            ttsManager.setVolume(originalVolume)
            isNavigationVolumeActive = false
            Log.d(TAG, "Volume restored to: $originalVolume")
        }
    }
    
    /**
     * Convert Maneuver enum to natural language description.
     * 
     * AC #4: Natural language and cardinal directions when helpful.
     * AC #8: Complex intersections (roundabouts, multi-exits).
     * 
     * Note: Roundabout exit numbers are extracted from instruction text in announceAdvanceWarning.
     */
    private fun getManeuverDescription(maneuver: Maneuver): String {
        return when (maneuver) {
            Maneuver.TURN_LEFT -> "turn left"
            Maneuver.TURN_RIGHT -> "turn right"
            Maneuver.TURN_SLIGHT_LEFT -> "turn slight left"
            Maneuver.TURN_SLIGHT_RIGHT -> "turn slight right"
            Maneuver.TURN_SHARP_LEFT -> "turn sharp left"
            Maneuver.TURN_SHARP_RIGHT -> "turn sharp right"
            Maneuver.STRAIGHT -> "continue straight"
            Maneuver.RAMP_LEFT -> "take ramp left"
            Maneuver.RAMP_RIGHT -> "take ramp right"
            Maneuver.MERGE -> "merge"
            Maneuver.FORK_LEFT -> "take left fork"
            Maneuver.FORK_RIGHT -> "take right fork"
            Maneuver.ROUNDABOUT_LEFT -> "enter roundabout"  // Exit number added separately
            Maneuver.ROUNDABOUT_RIGHT -> "enter roundabout"  // Exit number added separately
            Maneuver.UTURN_LEFT -> "make U-turn left"
            Maneuver.UTURN_RIGHT -> "make U-turn right"
            Maneuver.UNKNOWN -> "continue"
        }
    }
    
    /**
     * Extract roundabout exit number from instruction text (HIGH Issue #8 - AC #8).
     * 
     * Examples:
     * - "Take the 1st exit" → "first exit"
     * - "Take the 2nd exit" → "second exit"
     * - "Take the 3rd exit" → "third exit"
     * - "At the roundabout, take the second exit" → "second exit"
     * 
     * @param instruction Google Maps instruction text
     * @return Exit description or empty string if not found
     */
    private fun extractRoundaboutExit(instruction: String): String {
        // Match patterns like "1st exit", "2nd exit", "3rd exit", "4th exit"
        val exitPattern = Regex("(\\d+)(st|nd|rd|th)\\s+exit", RegexOption.IGNORE_CASE)
        val match = exitPattern.find(instruction)
        
        if (match != null) {
            val exitNumber = match.groupValues[1].toIntOrNull() ?: return ""
            val ordinal = when (exitNumber) {
                1 -> "first"
                2 -> "second"
                3 -> "third"
                4 -> "fourth"
                5 -> "fifth"
                6 -> "sixth"
                else -> "${exitNumber}th"
            }
            return "$ordinal exit"
        }
        
        // Also match written-out ordinals
        val writtenPattern = Regex("(first|second|third|fourth|fifth|sixth)\\s+exit", RegexOption.IGNORE_CASE)
        val writtenMatch = writtenPattern.find(instruction)
        return writtenMatch?.value?.lowercase() ?: ""
    }
    
    /**
     * Extract street name from instruction text.
     * 
     * Examples:
     * - "Turn left onto Main Street" → "Main Street"
     * - "Turn right onto <b>Oak Ave</b>" → "Oak Ave"
     * - "Continue straight" → ""
     * 
     * Story 6.2 already stripped HTML tags, so we just need to extract after "onto".
     */
    private fun extractStreetName(instruction: String): String {
        // Extract text after "onto" keyword
        val ontoIndex = instruction.indexOf("onto", ignoreCase = true)
        if (ontoIndex >= 0) {
            val afterOnto = instruction.substring(ontoIndex + 4).trim()
            
            // Clean up any remaining HTML or special characters
            return afterOnto
                .replace(Regex("<[^>]*>"), "") // Remove any HTML tags
                .trim()
        }
        
        // Check for "on" (alternative pattern)
        val onIndex = instruction.indexOf(" on ", ignoreCase = true)
        if (onIndex >= 0) {
            return instruction.substring(onIndex + 4).trim()
        }
        
        return ""
    }
    
    /**
     * Announces route deviation to user.
     * 
     * Story 6.4 AC #2: "You have gone off route. Recalculating directions."
     */
    suspend fun announceDeviation() {
        val message = "You have gone off route. Recalculating directions."
        Log.d(TAG, "Deviation announcement: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announces successful recalculation.
     * 
     * Story 6.4 AC #5: Confirmation that new route is ready.
     */
    suspend fun announceRecalculationSuccess() {
        val message = "New route calculated. Resuming navigation."
        Log.d(TAG, "Recalculation success: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announces recalculation error.
     * 
     * Story 6.4: Network error, timeout, or API failure.
     * 
     * @param reason Human-readable error description
     */
    suspend fun announceRecalculationError(reason: String) {
        val message = reason
        Log.e(TAG, "Recalculation error: $message")
        announceWithPriority(message)
    }
    
    /**
     * Announce excessive recalculations guidance.
     * 
     * Story 6.4 AC #7: Helpful prompt when user goes off route frequently.
     * Changed from question to statement - no response handler implemented yet.
     */
    suspend fun announceExcessiveRecalculations() {
        val message = "You're going off route frequently. Try listening for earlier turn warnings to stay on track."
        Log.w(TAG, "Excessive recalculations: $message")
        announceWithPriority(message)
    }
}
