package com.visionfocus

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.visionfocus.recognition.service.ObjectRecognitionService
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import java.io.IOException

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
    
    /**
     * Hilt EntryPoint for manual dependency retrieval in Application.onCreate()
     * This allows us to get dependencies AFTER Hilt initialization completes
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InitializationEntryPoint {
        fun objectRecognitionService(): ObjectRecognitionService
        fun ttsManager(): TTSManager
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "VisionFocus application starting...")
        
        // Get EntryPoint to access Hilt dependencies manually
        // This works because it's called AFTER super.onCreate() when Hilt is ready
        val entryPoint = EntryPointAccessors.fromApplication(
            this,
            InitializationEntryPoint::class.java
        )
        
        // Verify TFLite model file exists before attempting initialization
        if (!verifyModelFileExists()) {
            val errorMsg = "FATAL: TFLite model file not found at assets/$TFLITE_MODEL_PATH"
            Log.e(TAG, errorMsg)
            Toast.makeText(this, "App initialization failed: Model file missing", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Recognition service initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
            // Don't crash - allow app to show error state
        } catch (e: Exception) {
            Log.e(TAG, "✗ Unexpected error during ObjectRecognitionService initialization", e)
            Toast.makeText(this, "Unexpected initialization error: ${e.message}", Toast.LENGTH_LONG).show()
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
        
        Log.d(TAG, "VisionFocus application startup complete")
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
    
    override fun onTerminate() {
        super.onTerminate()
        // Note: onTerminate() is never called in production, only in emulator
        // Actual cleanup happens when process is killed by Android OS
    }
}
