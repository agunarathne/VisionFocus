package com.visionfocus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

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
    
    override fun onCreate() {
        super.onCreate()
        // Hilt automatically initializes dependency graph here
        // Future initialization code (e.g., WorkManager, Crash reporting) goes here
    }
}
