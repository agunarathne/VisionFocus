package com.visionfocus.voice.commands.navigation

import android.content.Context
import android.util.Log
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigate Command
 * Story 3.2 Task 2.2: Open navigation destination input
 * Story 6.1: Updated to launch DestinationInputFragment
 * 
 * Opens navigation screen for destination input (Epic 6).
 * User can enter destination via voice or text input.
 * 
 * Command variations:
 * - "navigate"
 * - "navigation"
 * - "directions"
 * - "take me to"
 * - "go to"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2, implemented in Story 6.1
 */
@Singleton
class NavigateCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "NavigateCommand"
    }
    
    override val displayName: String = "Navigate"
    
    override val keywords: List<String> = listOf(
        "navigate",
        "navigation",
        "directions",
        "take me to",
        "go to"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Navigate command")
            
            // Story 6.1: Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate to destination input screen using MainActivity helper
                context.runOnUiThread {
                    context.navigateToDestinationInput()
                }
                
                // Announce navigation - fragment will announce "Where would you like to go?"
                ttsManager.announce("Navigation")
                
                Log.d(TAG, "Navigate command executed - opened destination input")
                CommandResult.Success("Opened destination input")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open navigation", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Navigate error: ${e.message}")
        }
    }
}

/**
 * Where Am I Command
 * Story 3.2 Task 2.14: Announce current GPS location
 * Story 7.1+ Enhancement: Implement actual location announcement using LocationManager
 * 
 * Announces current GPS coordinates and approximate address.
 * 
 * Command variations:
 * - "where am i"
 * - "what is my location"
 * - "current location"
 * 
 * @param ttsManager TTS engine for announcements
 * @param locationManager GPS location service
 * @since Story 3.2, enhanced Story 7.1+
 */
@Singleton
class WhereAmICommand @Inject constructor(
    private val ttsManager: TTSManager,
    private val locationManager: com.visionfocus.navigation.location.LocationManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "WhereAmICommand"
    }
    
    override val displayName: String = "Where Am I"
    
    override val keywords: List<String> = listOf(
        "where am i",
        "what is my location",
        "current location"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Where Am I command")
            
            // Announce getting location
            ttsManager.announce("Getting your location")
            
            // Get current location (returns Result<LatLng>)
            val locationResult = locationManager.getCurrentLocation()
            
            if (locationResult.isSuccess) {
                val location = locationResult.getOrNull()!!
                
                // Format coordinates to 4 decimal places
                val lat = String.format("%.4f", location.latitude)
                val lng = String.format("%.4f", location.longitude)
                
                val announcement = "Your location: latitude $lat, longitude $lng"
                
                ttsManager.announce(announcement)
                Log.d(TAG, "Where Am I command executed: $announcement")
                CommandResult.Success("Location announced")
            } else {
                val error = locationResult.exceptionOrNull()
                ttsManager.announce("Could not get your location. Please check GPS and permissions.")
                Log.w(TAG, "Location unavailable: ${error?.message}")
                CommandResult.Failure("Location unavailable: ${error?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location", e)
            ttsManager.announce("Location error. Please try again.")
            CommandResult.Failure("Location error: ${e.message}")
        }
    }
}

/**
 * Back Command
 * Story 3.2 Task 2.12 & 8.1: Navigate to previous screen
 * Story 3.5 Task 9: Complete BackCommand implementation with back stack navigation
 * 
 * Navigates back in the navigation stack. If already on home screen (back stack empty),
 * announces "Already at home screen".
 * 
 * Command variations:
 * - "back"
 * - "go back"
 * - "previous"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2, completed in Story 3.5
 */
@Singleton
class BackCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "BackCommand"
    }
    
    override val displayName: String = "Back"
    
    override val keywords: List<String> = listOf(
        "back",
        "go back",
        "previous"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Back command")
            
            // Story 3.5: Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate back using MainActivity helper
                context.runOnUiThread {
                    context.navigateBack(ttsManager)
                }
                
                Log.d(TAG, "Back command executed - navigated back")
                CommandResult.Success("Navigated back")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate back", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Back error: ${e.message}")
        }
    }
}

/**
 * Settings Command
 * Story 3.5 Task 4.5: Navigate to Settings screen
 * 
 * Opens Settings screen from any location.
 * Integrates with MainActivity.navigateToSettings().
 * 
 * Command variations:
 * - "settings"
 * - "preferences"
 * - "options"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.5
 */
@Singleton
class SettingsCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "SettingsCommand"
    }
    
    override val displayName: String = "Settings"
    
    override val keywords: List<String> = listOf(
        "settings",
        "preferences",
        "options",
        "open settings"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Settings command")
            
            // Story 3.5: Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate to settings using MainActivity helper
                context.runOnUiThread {
                    context.navigateToSettings()
                }
                
                // Announce navigation
                ttsManager.announce("Settings")
                
                Log.d(TAG, "Settings command executed - navigated to settings")
                CommandResult.Success("Navigated to settings")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to settings", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Settings error: ${e.message}")
        }
    }
}

/**
 * Home Command
 * Story 3.2 Task 2.13 & 8.2: Return to home screen
 * Story 3.5 Task 8: Complete HomeCommand implementation with navigation
 * 
 * Navigates to MainActivity home fragment (RecognitionFragment).
 * Clears back stack and returns to home screen from any location.
 * 
 * Command variations:
 * - "home"
 * - "home screen"
 * - "go home"
 * - "main"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 3.2, completed in Story 3.5
 */
@Singleton
class HomeCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HomeCommand"
    }
    
    override val displayName: String = "Home"
    
    override val keywords: List<String> = listOf(
        "home",
        "home screen",
        "go home",
        "main",
        "main screen",
        "go to home",
        "go to main"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Home command")
            
            // Story 3.5: Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate to home screen using MainActivity helper
                context.runOnUiThread {
                    context.navigateToHome(ttsManager)
                }
                
                Log.d(TAG, "Home command executed - navigated to home screen")
                CommandResult.Success("Navigated to home screen")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate home", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Home error: ${e.message}")
        }
    }
}

/**
 * History Command
 * Story 4.3 Task 10.3: Voice command "History" navigates to HistoryFragment
 * 
 * Opens History screen showing last 50 recognition results.
 * Integrates with MainActivity.navigateToHistory().
 * 
 * Command variations:
 * - "history"
 * - "recognition history"
 * - "past recognitions"
 * - "show history"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 4.3
 */
@Singleton
class HistoryCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "HistoryCommand"
    }
    
    override val displayName: String = "History"
    
    override val keywords: List<String> = listOf(
        "history",
        "recognition history",
        "past recognitions",
        "show history"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing History command")
            
            // Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate to history using MainActivity helper
                context.runOnUiThread {
                    context.navigateToHistory()
                }
                
                // Announce navigation
                ttsManager.announce("History")
                
                Log.d(TAG, "History command executed - navigated to history")
                CommandResult.Success("Navigated to history")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to history", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("History error: ${e.message}")
        }
    }
}

/**
 * Saved Locations Command
 * Story 7.2 Task 10: Open saved locations management screen
 * 
 * Opens saved locations screen to view, edit, and delete saved locations.
 * User can navigate to saved locations or manage location list.
 * 
 * Command variations:
 * - "saved locations"
 * - "show saved locations"
 * - "saved places"
 * - "my locations"
 * 
 * @param ttsManager TTS engine for announcements
 * @since Story 7.2
 */
@Singleton
class SavedLocationsCommand @Inject constructor(
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    companion object {
        private const val TAG = "SavedLocationsCommand"
    }
    
    override val displayName: String = "Saved Locations"
    
    override val keywords: List<String> = listOf(
        "saved locations",
        "show saved locations",
        "saved places",
        "show saved places",
        "my locations"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Saved Locations command")
            
            // Story 7.2: Cast context to MainActivity for navigation
            if (context is com.visionfocus.MainActivity) {
                // Navigate to saved locations screen using MainActivity helper
                context.runOnUiThread {
                    context.navigateToSavedLocations()
                }
                
                // Announce navigation
                ttsManager.announce("Opening saved locations")
                
                Log.d(TAG, "Saved Locations command executed - navigated to saved locations")
                CommandResult.Success("Navigated to saved locations")
            } else {
                Log.e(TAG, "Context is not MainActivity - cannot navigate")
                ttsManager.announce("Navigation error")
                CommandResult.Failure("Context is not MainActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to saved locations", e)
            ttsManager.announce("Navigation error")
            CommandResult.Failure("Saved locations error: ${e.message}")
        }
    }
}

