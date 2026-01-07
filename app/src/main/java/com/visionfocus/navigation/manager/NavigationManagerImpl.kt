package com.visionfocus.navigation.manager

import android.content.Context
import android.widget.Toast
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of NavigationManager for Story 7.2.
 * 
 * Story 7.2: Provides basic TTS announcement for navigation start
 * Future (Epic 6): Full GPS-based turn-by-turn navigation with Google Maps Directions API
 * 
 * This stub allows SavedLocationsFragment to trigger "navigation" without
 * full Epic 6 implementation being complete.
 */
@Singleton
class NavigationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsManager: TTSManager
) : NavigationManager {
    
    override suspend fun startNavigation(
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationName: String
    ): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            // Story 7.2: Announce navigation start via TTS
            ttsManager.announce("Starting navigation to $destinationName")
            
            // Development verification: Show toast with coordinates
            Toast.makeText(
                context,
                "Navigation to $destinationName\nLat: $destinationLatitude, Lon: $destinationLongitude",
                Toast.LENGTH_LONG
            ).show()
            
            Timber.d("Navigation started to $destinationName ($destinationLatitude, $destinationLongitude)")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start navigation")
            Result.failure(e)
        }
    }
}
