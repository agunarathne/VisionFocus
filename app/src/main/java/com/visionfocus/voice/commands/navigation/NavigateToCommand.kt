package com.visionfocus.voice.commands.navigation

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.visionfocus.beacon.ProximityNavigationService
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.data.repository.BeaconRepository
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.repository.NavigationRepository
import com.visionfocus.navigation.service.NavigationService
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.LevenshteinMatcher
import com.visionfocus.voice.processor.VoiceCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigate To Command
 * Story 7.3 Task 4: Voice command for quick navigation to saved locations
 * Epic 10: Supports fallback to Beacon navigation if no location found
 * 
 * Directly starts navigation to a saved location by name without intermediate screens.
 * Supports fuzzy matching with Levenshtein distance ≤2 for typos and speech recognition errors.
 * 
 * Command variations:
 * - "navigate to [location name]"
 * - "go to [location name]"
 * - "take me to [location name]"
 * - "directions to [location name]"
 * 
 * Examples:
 * - "navigate to home" → Starts navigation to saved location "Home"
 * - "go to work" → Starts navigation to saved location "Work"
 * - "take me to gym" → Starts navigation to saved location "Gym"
 * 
 * Fuzzy matching examples:
 * - "navigate to hme" → Matches "Home" (distance: 1)
 * - "go to wrk" → Matches "Work" (distance: 1)
 * 
 * @param repository SavedLocationRepository for location lookup
 * @param navigationManager NavigationManager to start turn-by-turn guidance
 * @param ttsManager TTSManager for voice announcements
 * @since Story 7.3
 */
@Singleton
class NavigateToCommand @Inject constructor(
    private val repository: SavedLocationRepository,
    private val beaconRepository: BeaconRepository,
    private val navigationRepository: NavigationRepository,
    private val proximityNav: ProximityNavigationService,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "NavigateToCommand"
        private const val TRANSCRIPTION_PREF = "voice"
        private const val TRANSCRIPTION_KEY = "last_transcription"
        private const val FUZZY_MATCH_THRESHOLD = 2
    }
    
    // Dedicated scope for proximity navigation lifecycle
    private val navigationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override val displayName: String = "Navigate To"
    
    override val keywords: List<String> = listOf(
        "navigate to",
        "go to",
        "take me to",
        "directions to"
    )
    
    /**
     * Story 7.3 Task 4: Execute navigate to saved location command.
     * 
     * Flow:
     * 1. Extract location name from transcription
     * 2. Try exact match (case-insensitive) for Saved Location
     * 3. Try fuzzy match for Saved Location
     * 4. If no Saved Location found, try specific/exact match for Beacon
     * 5. Start navigation (Turn-by-turn OR Proximity)
     */
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Timber.d("Executing Navigate To command")
            
            // Story 7.3 Task 7: Extract location name from transcription
            val transcription = context.getSharedPreferences(TRANSCRIPTION_PREF, Context.MODE_PRIVATE)
                .getString(TRANSCRIPTION_KEY, "") ?: ""
            
            val locationName = parseLocationName(transcription)
            
            if (locationName.isBlank()) {
                ttsManager.announce("Please specify a location name")
                return CommandResult.Failure("No location name specified")
            }
            
            Timber.d("Extracted location name: \"$locationName\" from transcription: \"$transcription\"")
            
            // Story 7.3 Task 4.6: Try exact match first
            val exactMatch = repository.findLocationByName(locationName.lowercase())
            
            if (exactMatch != null) {
                Timber.d("Exact match found: ${exactMatch.name}")
                startNavigationToLocation(context, exactMatch)
                return CommandResult.Success("Navigation to ${exactMatch.name} started")
            }
            
            // Story 7.3 Task 4.8: Try fuzzy matching
            val allLocations = repository.getAllLocationsSorted().firstOrNull() ?: emptyList()
            val fuzzyMatches = findFuzzyLocationMatches(locationName, allLocations)
            
            if (fuzzyMatches.isNotEmpty()) {
                if (fuzzyMatches.size == 1) {
                    val match = fuzzyMatches.first()
                    ttsManager.announce(context.getString(com.visionfocus.R.string.did_you_mean, match.name))
                    startNavigationToLocation(context, match)
                    return CommandResult.Success("Navigation to ${match.name} started")
                } else {
                    handleDisambiguation(context, fuzzyMatches)
                    return CommandResult.Success("Disambiguation dialog shown")
                }
            }

            // Fallback: Check for Beacon if no location found
            // Epic 10: Seamless beacon support
            Timber.d("No saved location found, checking beacons for: $locationName")
            val beaconMatch = beaconRepository.findByName(locationName)
            
            if (beaconMatch != null) {
                Timber.d("Beacon match found: ${beaconMatch.name}")
                ttsManager.announce("Found beacon ${beaconMatch.name}. Starting proximity tracking.")
                proximityNav.startNavigation(beaconMatch.macAddress, beaconMatch.name, navigationScope)
                return CommandResult.Success("Tracking beacon ${beaconMatch.name}")
            }
            
            // No matches at all
            val message = context.getString(com.visionfocus.R.string.no_location_found, locationName)
            ttsManager.announce(message)
            Timber.w("No saved location or beacon found named: $locationName")
            return CommandResult.Failure("Location not found")
            
        } catch (e: Exception) {
            Timber.e(e, "Navigate To command execution failed")
            ttsManager.announce("Navigation failed. Please try again.")
            CommandResult.Failure("Execution error: ${e.message}")
        }
    }
    
    /**
     * Story 7.3 Task 7: Extract location name from voice transcription.
     * Removes command keywords and trims whitespace.
     * 
     * @param transcription Full voice transcription
     * @return Location name without command keywords
     * 
     * Examples:
     * - "navigate to home" → "home"
     * - "go to work" → "work"
     * - "take me to office building" → "office building"
     */
    private fun parseLocationName(transcription: String): String {
        var parsed = transcription.lowercase().trim()
        
        // Story 7.3 Task 7.2: Remove command keywords
        keywords.forEach { keyword ->
            if (parsed.startsWith(keyword)) {
                parsed = parsed.removePrefix(keyword).trim()
            }
        }
        
        return parsed
    }
    
    /**
     * Story 7.3 Task 5: Find fuzzy location name matches.
     * Uses Levenshtein distance ≤2 for matching with typos.
     * 
     * @param input Location name from transcription
     * @param locations All saved locations to search
     * @return List of matching locations sorted by distance (closest first)
     */
    private fun findFuzzyLocationMatches(
        input: String,
        locations: List<SavedLocationEntity>
    ): List<SavedLocationEntity> {
        return locations.filter { location ->
            val distance = LevenshteinMatcher.calculateDistance(
                input.lowercase(),
                location.name.lowercase()
            )
            distance <= FUZZY_MATCH_THRESHOLD
        }.sortedBy { location ->
            LevenshteinMatcher.calculateDistance(
                input.lowercase(),
                location.name.lowercase()
            )
        }
    }
    
    /**
     * Story 7.3 Task 8: Start navigation to location using NavigationRepository.
     * Updates lastUsedAt timestamp, calculates route, and starts NavigationService.
     * 
     * @param context Application context for starting service
     * @param location SavedLocationEntity to navigate to
     */
    private suspend fun startNavigationToLocation(context: Context, location: SavedLocationEntity) = withContext(Dispatchers.IO) {
        try {
            // Story 7.3 Task 9: Update lastUsedAt timestamp
            val updated = location.copy(lastUsedAt = System.currentTimeMillis())
            repository.updateLocation(updated)
            Timber.d("Updated lastUsedAt for location: ${location.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update lastUsedAt for location: ${location.name}")
            // Story 7.3 Task 9.5: Continue navigation even if timestamp update fails
        }
        
        // Story 7.3 Task 8: Calculate route and start NavigationService
        try {
            // Create Destination from saved location
            val destination = Destination(
                query = location.name,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                formattedAddress = location.address ?: location.name
            )
            
            // Calculate route using NavigationRepository
            val routeResult = navigationRepository.getRoute(destination)
            
            if (routeResult.isSuccess) {
                val route = routeResult.getOrThrow()
                Timber.d("Route calculated: ${route.steps.size} steps, ${route.totalDistance}m to ${location.name}")
                
                // Start NavigationService with route
                withContext(Dispatchers.Main) {
                    val serviceIntent = Intent(context, NavigationService::class.java).apply {
                        action = NavigationService.ACTION_START_NAVIGATION
                        putExtra(NavigationService.EXTRA_ROUTE, route)
                        putExtra(NavigationService.EXTRA_DESTINATION, destination)
                    }
                    context.startForegroundService(serviceIntent)
                    
                    // Launch MainActivity with navigation to NavigationActiveFragment
                    val activityIntent = Intent(context, com.visionfocus.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("navigate_to_fragment", "navigationActive")
                        putExtra("route", route)
                        putExtra("destinationName", location.name)
                    }
                    context.startActivity(activityIntent)
                    
                    ttsManager.announce("Starting navigation to ${location.name}")
                    Timber.d("NavigationService started and UI launched for ${location.name}")
                }
            } else {
                val error = routeResult.exceptionOrNull()
                Timber.e(error, "Failed to calculate route to ${location.name}")
                withContext(Dispatchers.Main) {
                    ttsManager.announce("Navigation failed. Could not calculate route.")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to start navigation for location: ${location.name}")
            withContext(Dispatchers.Main) {
                ttsManager.announce("Navigation failed. Please try again.")
            }
        }
    }
    
    /**
     * Story 7.3 Task 6: Handle disambiguation for multiple fuzzy matches.
     * Shows dialog on UI thread if MainActivity context available.
     * Falls back to TTS announcement if UI not accessible.
     * 
     * @param context Application or MainActivity context
     * @param matches List of matching SavedLocationEntity objects (up to 5)
     */
    private suspend fun handleDisambiguation(
        context: Context,
        matches: List<SavedLocationEntity>
    ) {
        // Story 7.3 Task 6.7: Check if MainActivity context available for dialog
        if (context is com.visionfocus.MainActivity) {
            withContext(Dispatchers.Main) {
                // Story 7.3 Task 6.3: Show disambiguation dialog
                val dialog = com.visionfocus.navigation.ui.LocationDisambiguationDialog.newInstance(
                    locations = matches.take(5),  // Max 5 options
                    onLocationSelected = { selectedLocation ->
                        // Story 7.3 Task 6.6: Start navigation to chosen location
                        // Launch in viewModelScope from the Activity's lifecycle
                        (context as? com.visionfocus.MainActivity)?.lifecycleScope?.launch {
                            startNavigationToLocation(context, selectedLocation)
                        }
                    }
                )
                
                dialog.show(
                    context.supportFragmentManager,
                    com.visionfocus.navigation.ui.LocationDisambiguationDialog.TAG
                )
                
                Timber.d("Disambiguation dialog shown with ${matches.size} options")
            }
        } else {
            // Story 7.3 Task 6.7: Fallback - Announce options via TTS
            val options = matches.take(5).joinToString(", ") { it.name }
            val announcement = context.getString(
                com.visionfocus.R.string.multiple_locations_announcement,
                options
            )
            ttsManager.announce(announcement)
            Timber.d("TTS-only disambiguation: $options")
        }
    }
}
