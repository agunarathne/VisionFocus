package com.visionfocus

import android.app.Application
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for VisionFocus.
 * 
 * @HiltAndroidApp triggers Hilt code generation including:
 * - A base class for the application that serves as the application-level dependency container
 * - Access to Hilt's set of standard components
 * 
 * This class must be registered in AndroidManifest.xml
 * 
 * Story 2.2: TTSManager is injected and initialized on app launch to ensure
 * TTS is ready before first recognition request (AC5 requirement)
 */
@HiltAndroidApp
class VisionFocusApplication : Application() {
    
    // Inject TTSManager to force initialization on app startup
    @Inject
    lateinit var ttsManager: TTSManager
    
    override fun onCreate() {
        super.onCreate()
        // Hilt automatically initializes dependency graph here
        // TTSManager is now guaranteed to be initialized on app launch
        // Future initialization code (e.g., WorkManager, Crash reporting) goes here
    }
}
