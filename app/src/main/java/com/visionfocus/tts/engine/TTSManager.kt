package com.visionfocus.tts.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android TextToSpeech service manager
 * 
 * Story 2.2 AC5: Initialize TextToSpeech on app startup
 * Story 2.2 AC6: TTS announcement latency ≤200ms
 * 
 * Lifecycle:
 * - Initialize on Application.onCreate()
 * - Shutdown on Application.onTerminate()
 * 
 * Usage:
 * ```
 * ttsManager.initialize()
 * ttsManager.announce("I see a chair").onSuccess { latency ->
 *     Log.d(TAG, "Announced in ${latency}ms")
 * }
 * ```
 */
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    companion object {
        private const val TAG = "TTSManager"
        
        /**
         * Default speech rate: 1.0x (normal speed)
         * Epic 5 Story 5.1 will add user-adjustable rate (0.5×-2.0×)
         */
        private const val DEFAULT_SPEECH_RATE = 1.0f
        
        /**
         * Target latency: ≤200ms from recognition completion to TTS initiation
         * Story 2.2 AC6 performance requirement
         */
        const val TARGET_LATENCY_MS = 200L
    }
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    /**
     * Initialize TextToSpeech engine
     * Should be called on Application.onCreate()
     * 
     * Asynchronous: onInit() callback will be invoked when ready
     */
    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }
    
    /**
     * TextToSpeech initialization callback
     * Called asynchronously after initialize()
     * 
     * @param status SUCCESS or ERROR
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            
            when (result) {
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.e(TAG, "TTS language data missing")
                    isInitialized = false
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e(TAG, "TTS language not supported")
                    isInitialized = false
                }
                else -> {
                    isInitialized = true
                    tts?.setSpeechRate(DEFAULT_SPEECH_RATE)
                    setupUtteranceListener()
                    Log.d(TAG, "TTS initialized successfully")
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            isInitialized = false
        }
    }
    
    /**
     * Setup utterance progress listener for tracking TTS lifecycle
     * Enables monitoring of speech start, completion, and errors
     */
    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS started: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS completed: $utteranceId")
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "TTS error: $utteranceId, code: $errorCode")
            }
        })
    }
    
    /**
     * Announce text via TTS with latency tracking
     * 
     * Story 2.2 AC6: Latency requirement ≤200ms
     * 
     * @param text Natural language announcement string
     * @return Result with latency in milliseconds, or failure exception
     * @throws IllegalStateException if TTS not initialized
     */
    suspend fun announce(text: String): Result<Long> = withContext(Dispatchers.Main) {
        if (!isInitialized || tts == null) {
            return@withContext Result.failure(
                IllegalStateException("TTS not initialized")
            )
        }
        
        val startTime = System.currentTimeMillis()
        
        // Generate unique utterance ID for tracking
        val utteranceId = "recognition_${System.currentTimeMillis()}"
        
        // Speak with QUEUE_FLUSH to immediately replace any pending announcements
        val result = tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
        
        val latency = System.currentTimeMillis() - startTime
        
        if (result == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS announcement queued in ${latency}ms: $text")
            Result.success(latency)
        } else {
            Log.e(TAG, "TTS speak failed with result: $result")
            Result.failure(Exception("TTS speak failed with result: $result"))
        }
    }
    
    /**
     * Stop any ongoing speech
     * Useful for interrupting long announcements
     */
    fun stop() {
        tts?.stop()
    }
    
    /**
     * Check if TTS is currently initialized and ready
     * 
     * @return true if ready to announce, false otherwise
     */
    fun isReady(): Boolean = isInitialized && tts != null
    
    /**
     * Update speech rate
     * Epic 5 Story 5.1: User-adjustable TTS speech rate
     * 
     * @param rate Speech rate multiplier (0.5×-2.0× range)
     *             1.0 = normal speed
     *             < 1.0 = slower
     *             > 1.0 = faster
     */
    fun setSpeechRate(rate: Float) {
        require(rate in 0.5f..2.0f) { "Speech rate must be in range [0.5, 2.0]" }
        
        tts?.setSpeechRate(rate)
        Log.d(TAG, "Speech rate set to ${rate}x")
    }
    
    /**
     * Shutdown TTS engine
     * Should be called on Application.onTerminate()
     * Releases system resources
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d(TAG, "TTS shutdown complete")
    }
}
