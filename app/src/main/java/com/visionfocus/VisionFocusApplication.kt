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
 */
@HiltAndroidApp
class VisionFocusApplication : Application() {
    
    /**
     * Story 2.2: TTSManager injected and initialized on app startup
     * Ensures TTS is ready before any recognition operations
     */
    @Inject
    lateinit var ttsManager: TTSManager
    
    override fun onCreate() {
        super.onCreate()
        // Hilt automatically initializes dependency graph here
        // TTSManager.initialize() is called in TTSModule.provideTTSManager()
        // Future initialization code (e.g., WorkManager, Crash reporting) goes here
    }
    
    /**
     * Story 2.2: Cleanup TTS resources on app termination
     * Note: onTerminate() is not always called on actual devices
     * but provides cleanup for emulator and testing scenarios
     */
    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
    }
}
