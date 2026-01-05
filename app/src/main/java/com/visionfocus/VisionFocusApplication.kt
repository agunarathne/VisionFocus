package com.visionfocus

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.visionfocus.network.monitor.NetworkStateMonitor
import com.visionfocus.recognition.service.ObjectRecognitionService
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Application class for VisionFocus.
 * 
 * @HiltAndroidApp triggers Hilt code generation including:
 * - A base class for the application that serves as the application-level dependency container
 * - Access to Hilt's set of standard components
 * 
 * This class must be registered in AndroidManifest.xml
 * 
 * CRITICAL FIX: Uses Hilt EntryPoint to manually retrieve and initialize services
 * AFTER super.onCreate() completes (when Hilt injection is ready).
 * This prevents race conditions and ensures proper initialization timing.
 */
@HiltAndroidApp
class VisionFocusApplication : Application() {
    
    companion object {
        private const val TAG = "VisionFocusApp"
        private const val TFLITE_MODEL_PATH = "models/ssd_mobilenet_v1_quantized.tflite"
    }
    
    // Application-scoped coroutine scope for async initialization
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Hilt EntryPoint for manual dependency retrieval in Application.onCreate()
     * This allows us to get dependencies AFTER Hilt initialization completes
     * 
     * Story 6.6: Added NetworkStateMonitor for cleanup in onTerminate()
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InitializationEntryPoint {
        fun objectRecognitionService(): ObjectRecognitionService
        fun ttsManager(): TTSManager
        fun networkStateMonitor(): NetworkStateMonitor
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Code Review Fix (Story 4.2): Initialize Timber for proper logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("VisionFocus application started (DEBUG mode)")
        }
        
        Log.d(TAG, "VisionFocus application starting...")
        
        // Initialize services in background thread to avoid blocking UI
        applicationScope.launch {
            initializeServices()
        }
        
        Log.d(TAG, "VisionFocus application startup complete (services initializing in background)")
    }
    
    /**
     * Initialize heavy services (TFLite, TTS) on background thread
     * This prevents blocking the UI thread during app startup
     */
    private suspend fun initializeServices() {
        // Get EntryPoint to access Hilt dependencies manually
        // This works because it's called AFTER super.onCreate() when Hilt is ready
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            InitializationEntryPoint::class.java
        )
        
        // Verify TFLite model file exists before attempting initialization
        if (!verifyModelFileExists()) {
            val errorMsg = "FATAL: TFLite model file not found at assets/$TFLITE_MODEL_PATH"
            Log.e(TAG, errorMsg)
            // Don't crash - allow app to show error UI
            return
        }
        
        // Initialize ObjectRecognitionService (TFLite model loading)
        // Using EntryPoint ensures we get the SAME singleton instance that will be injected elsewhere
        try {
            val startTime = System.currentTimeMillis()
            val objectRecognitionService = entryPoint.objectRecognitionService()
            objectRecognitionService.initialize()
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "✓ ObjectRecognitionService initialized successfully (${duration}ms)")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "✗ ObjectRecognitionService initialization failed", e)
            // Don't crash - allow app to show error state
        } catch (e: Exception) {
            Log.e(TAG, "✗ Unexpected error during ObjectRecognitionService initialization", e)
        }
        
        // Initialize TTSManager (Text-to-Speech engine)
        try {
            val ttsManager = entryPoint.ttsManager()
            ttsManager.initialize()
            Log.d(TAG, "✓ TTSManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ TTSManager initialization failed", e)
            // Non-fatal - TTS will be unavailable but app can still function
        }
    }
    
    /**
     * Verify TFLite model file exists in assets before attempting to load
     * Provides clear diagnostic if model file is missing
     */
    private fun verifyModelFileExists(): Boolean {
        return try {
            assets.open(TFLITE_MODEL_PATH).use { stream ->
                val fileSize = stream.available()
                Log.d(TAG, "TFLite model file found: $TFLITE_MODEL_PATH (${fileSize / 1024}KB)")
                fileSize > 0
            }
        } catch (e: IOException) {
            Log.e(TAG, "TFLite model file not found: $TFLITE_MODEL_PATH", e)
            false
        }
    }
    
    /**
     * Called when application is terminated (process killed).
     * 
     * Story 6.6 FIX (CRITICAL #1): Cleanup NetworkStateMonitor to prevent memory leak.
     * Unregisters network callback to release ConnectivityManager reference.
     * 
     * NOTE: onTerminate() is NOT called in production (only in emulator).
     * For production cleanup, consider using ProcessLifecycleOwner.
     */
    override fun onTerminate() {
        super.onTerminate()
        
        // Cleanup NetworkStateMonitor (Story 6.6)
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                InitializationEntryPoint::class.java
            )
            entryPoint.networkStateMonitor().unregister()
            Timber.d(TAG, "NetworkStateMonitor cleaned up successfully")
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to cleanup NetworkStateMonitor", e)
        }
    }
}
