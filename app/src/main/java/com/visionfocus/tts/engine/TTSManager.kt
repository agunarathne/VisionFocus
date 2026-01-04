package com.visionfocus.tts.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.visionfocus.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 5.2: TTS voice option for UI selection
 * 
 * @property id Unique identifier (locale + gender): "en-US-female"
 * @property displayName User-facing name: "English US (Female)"
 * @property locale Locale string: "en-US"
 * @property gender Voice gender (if available): MALE, FEMALE, NEUTRAL
 * @property voice Android Voice object (internal use)
 */
data class VoiceOption(
    val id: String,
    val displayName: String,
    val locale: String,
    val gender: Int?,
    val voice: Voice
)

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
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
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
    
    // Story 5.1: Track current speech rate
    private var currentSpeechRate: Float = DEFAULT_SPEECH_RATE
    
    // Story 5.2: Track current voice locale
    private var currentVoiceLocale: String? = null
    
    // Story 6.3: Track current volume level
    private var currentVolume: Float = 1.0f
    
    // Story 5.1: Coroutine scope for observing settings changes
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Track actual TTS start time for latency measurement
    private var lastUtteranceStartTime: Long? = null
    private var lastUtteranceQueueTime: Long? = null
    
    /**
     * Initialize TextToSpeech engine
     * Should be called on Application.onCreate()
     * 
     * Story 5.1 FIX: Start observing speech rate changes BEFORE TTS initializes
     * This ensures we cache the user's saved rate and apply it immediately when TTS is ready
     * 
     * Story 5.2: Start observing voice locale changes BEFORE TTS initializes
     * 
     * Thread-safe: Can be called multiple times safely
     * Asynchronous: onInit() callback will be invoked when ready
     */
    @Synchronized
    fun initialize() {
        if (tts == null) {
            // FIX ISSUE #1: Start observing speech rate BEFORE TTS initializes
            // This caches the saved rate so it's ready when onInit() is called
            observeSpeechRateChanges()
            
            // Story 5.2: Start observing voice locale BEFORE TTS initializes
            observeVoiceLocaleChanges()
            
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
                    tts?.setSpeechRate(currentSpeechRate)
                    
                    // Story 5.2: Apply cached voice locale if available
                    if (currentVoiceLocale != null) {
                        scope.launch {
                            setVoice(currentVoiceLocale)
                        }
                    }
                    
                    setupUtteranceListener()
                    Log.d(TAG, "TTS initialized successfully with rate: $currentSpeechRate, voice: $currentVoiceLocale")
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
                lastUtteranceStartTime = System.currentTimeMillis()
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
     * Story 5.1: Observe speech rate changes from SettingsRepository
     * 
     * This coroutine observes the speech rate preference and updates TTSManager
     * whenever the user changes the setting.
     * 
     * CRITICAL FIX: Added null checks for isInitialized AND tts to prevent race condition
     * on app startup when DataStore emits before TTS engine is ready.
     */
    private fun observeSpeechRateChanges() {
        settingsRepository.getSpeechRate()
            .onEach { rate ->
                currentSpeechRate = rate
                // CRITICAL: Check both isInitialized AND tts != null before applying
                if (isInitialized && tts != null) {
                    tts?.setSpeechRate(rate)
                    Log.d(TAG, "Speech rate updated to: $rate")
                } else {
                    Log.d(TAG, "Speech rate cached ($rate) - TTS not ready yet")
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Announce text via TTS with latency tracking
     * 
     * Story 2.2 AC6: Latency requirement ≤200ms
     * Story 3.4 AC#3: Automatically stops any ongoing speech before new announcement
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
        
        // CRITICAL: ALWAYS stop before new announcement (Story 3.4 AC #3)
        // Android TTS isSpeaking() is unreliable, so we call stop() unconditionally
        // stop() is safe to call even if nothing is playing (no-op)
        tts?.stop()
        
        // Brief delay to ensure TTS engine fully stops previous speech
        // Only delay 50ms (reduced from 100ms for better responsiveness)
        kotlinx.coroutines.delay(50)
        
        val startTime = System.currentTimeMillis()
        lastUtteranceQueueTime = startTime
        
        // Generate unique utterance ID for tracking
        val utteranceId = "recognition_${System.currentTimeMillis()}"
        
        // Speak with QUEUE_FLUSH to immediately replace any pending announcements
        // Story 6.3: Apply volume from currentVolume (for navigation 10% increase)
        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, currentVolume)
        }
        
        val result = tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            utteranceId
        )
        
        val queueLatency = System.currentTimeMillis() - startTime
        
        if (result == TextToSpeech.SUCCESS) {
            // Only log in development (avoid production overhead)
            Result.success(queueLatency)
        } else {
            Log.e(TAG, "TTS speak failed with result: $result")
            Result.failure(Exception("TTS speak failed with result: $result"))
        }
    }
    
    /**
     * Get actual latency from queue time to speech start
     * Only available after onStart() callback is triggered
     * 
     * @return Latency in milliseconds, or null if not available
     */
    fun getLastActualLatency(): Long? {
        val queueTime = lastUtteranceQueueTime ?: return null
        val startTime = lastUtteranceStartTime ?: return null
        return startTime - queueTime
    }
    
    /**
     * Stop any ongoing speech
     * Useful for interrupting long announcements
     */
    fun stop() {
        if (tts?.isSpeaking == true) {
            Log.d(TAG, "Stopping ongoing TTS speech")
            tts?.stop()
        } else {
            Log.d(TAG, "No TTS speech to stop (already idle)")
        }
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
        
        currentSpeechRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
            Log.d(TAG, "Speech rate set to ${rate}x")
        }
    }
    
    /**
     * Story 6.3: Get current TTS volume level
     * 
     * @return Current volume (0.0-1.0)
     */
    fun getVolume(): Float {
        return currentVolume
    }
    
    /**
     * Story 6.3: Set TTS volume level
     * 
     * Used by NavigationAnnouncementManager to increase volume 10% for
     * safety-critical navigation announcements (AC #7).
     * 
     * Note: This affects the audio stream volume, not the device master volume.
     * 
     * @param volume Volume level (0.0-1.0, where 1.0 = 100%)
     */
    fun setVolume(volume: Float) {
        require(volume in 0f..1f) { "Volume must be in range [0.0, 1.0]" }
        
        currentVolume = volume
        Log.d(TAG, "TTS volume set to $volume")
        
        // Note: Android TTS doesn't have a setVolume() API
        // Volume is controlled via AudioManager audio stream or per-utterance params
        // For Story 6.3, we'll use Bundle params in speak() method
        // Story 8.1 will implement more sophisticated audio ducking/priority
    }
    
    /**
     * Story 5.2 Task 1: Query available TTS voices from Android engine
     * 
     * @return List of English voices available on device
     */
    fun getAvailableVoices(): List<VoiceOption> {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized, cannot query voices")
            return emptyList()
        }
        
        try {
            val voices = tts?.voices ?: return emptyList()
            
            // Filter to English voices only
            return voices
                .filter { voice ->
                    voice.locale.language == "en"  // English only
                }
                .map { voice ->
                    val locale = voice.locale
                    val gender = determineGenderFromVoiceName(voice.name)
                    
                    VoiceOption(
                        id = "${locale.language}-${locale.country}-${gender ?: "default"}",
                        displayName = formatVoiceDisplayName(locale, gender),
                        locale = "${locale.language}-${locale.country}",
                        gender = gender,
                        voice = voice
                    )
                }
                .distinctBy { it.locale }  // MEDIUM-4 FIX: Deduplicate by locale, not id (prevents multiple entries for same locale)
                .sortedBy { it.displayName }  // Alphabetical order
        } catch (e: Exception) {
            Log.e(TAG, "Error querying TTS voices", e)
            return emptyList()
        }
    }
    
    /**
     * Story 5.2 Task 1.4: Format voice display name for UI
     * 
     * @param locale Voice locale (e.g., Locale.US)
     * @param gender Voice gender constant or null
     * @return Formatted name: "English US (Female)"
     */
    private fun formatVoiceDisplayName(locale: Locale, gender: Int?): String {
        val languageName = locale.displayLanguage  // "English"
        val countryName = locale.displayCountry    // "United States"
        val genderName = when (gender) {
            300 -> "Male"      // Voice.GENDER_MALE
            200 -> "Female"    // Voice.GENDER_FEMALE
            400 -> "Neutral"   // Voice.GENDER_NEUTRAL
            else -> null
        }
        
        val countryShort = when (locale.country) {
            "US" -> "US"
            "GB" -> "GB"
            "AU" -> "Australia"
            "IN" -> "India"
            else -> countryName
        }
        
        return if (genderName != null) {
            "$languageName $countryShort ($genderName)"
        } else {
            "$languageName $countryShort"
        }
    }
    
    /**
     * Story 5.2 Task 1.4: Heuristic to determine gender from voice name
     * Android doesn't always provide gender metadata
     * 
     * @param voiceName Voice.name string from Android TTS engine
     * @return Best guess gender constant or null
     */
    private fun determineGenderFromVoiceName(voiceName: String): Int? {
        val nameLower = voiceName.lowercase()
        return when {
            nameLower.contains("female") || nameLower.contains("woman") -> 200  // Voice.GENDER_FEMALE
            nameLower.contains("male") || nameLower.contains("man") -> 300      // Voice.GENDER_MALE
            else -> null
        }
    }
    
    /**
     * Story 5.2 Task 3: Set TTS voice by locale string
     * 
     * @param localeString Locale identifier: "en-US", "en-GB", null for system default
     * @return true if voice set successfully, false if fallback to default
     */
    suspend fun setVoice(localeString: String?): Boolean = withContext(Dispatchers.Main) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized, cannot set voice")
            return@withContext false
        }
        
        try {
            if (localeString == null) {
                // Reset to system default voice
                tts?.voice = null  // Android uses default
                currentVoiceLocale = null
                Log.d(TAG, "Voice reset to system default")
                return@withContext true
            }
            
            val voices = tts?.voices ?: return@withContext false
            
            // Find matching voice by locale
            val matchedVoice = voices.firstOrNull { voice ->
                val voiceLocale = "${voice.locale.language}-${voice.locale.country}"
                voiceLocale.equals(localeString, ignoreCase = true)
            }
            
            if (matchedVoice != null) {
                tts?.voice = matchedVoice
                currentVoiceLocale = localeString
                Log.d(TAG, "Voice set to: ${matchedVoice.name}")
                return@withContext true
            } else {
                // Voice not found - fallback to system default
                Log.w(TAG, "Voice $localeString not found, falling back to system default")
                tts?.voice = null
                currentVoiceLocale = null
                
                // Announce fallback to user (Story 5.2 AC requirement)
                // Error handling: Don't crash if TTS engine not ready
                announce("Your preferred voice is unavailable. Using default voice.")
                    .onFailure { e ->
                        Log.e(TAG, "Failed to announce voice unavailability", e)
                    }
                
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting TTS voice", e)
            return@withContext false
        }
    }
    
    /**
     * Story 5.2 Task 3.6: Observe voice locale changes from SettingsRepository
     * 
     * Similar pattern to observeSpeechRateChanges() from Story 5.1
     */
    private fun observeVoiceLocaleChanges() {
        settingsRepository.getVoiceLocale()
            .onEach { locale ->
                currentVoiceLocale = locale
                if (isInitialized && tts != null) {
                    setVoice(locale)
                } else {
                    Log.d(TAG, "Voice locale cached ($locale) - TTS not ready yet")
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Shutdown TTS engine
     * Should be called on Application.onTerminate()
     * Releases system resources
     */
    fun shutdown() {
        scope.cancel()  // Story 5.1: Cancel coroutine scope
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d(TAG, "TTS shutdown complete")
    }
}
