package com.visionfocus.navigation.consent

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Manages user consent for network access in navigation features.
 * 
 * Story 6.2: Network consent required before Google Maps Directions API calls.
 * Stores consent decision in DataStore to avoid repeated prompts.
 * 
 * @property settingsRepository Repository for persisting consent preference
 * @property ttsManager TTS manager for announcing consent decisions
 * @property context Application context for dialog display
 */
@Singleton
class NetworkConsentManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager,
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NetworkConsentManager"
    }
    
    /**
     * Checks if user has granted network consent for live directions.
     * 
     * @return true if consent granted, false otherwise
     */
    suspend fun hasConsent(): Boolean {
        val consent = settingsRepository.networkConsent.first()
        Timber.tag(TAG).d("Network consent status: $consent")
        return consent
    }
    
    /**
     * Stores user's network consent decision persistently.
     * 
     * @param granted true if user allowed network access, false if denied
     */
    suspend fun setConsent(granted: Boolean) {
        settingsRepository.updateNetworkConsent(granted)
        Timber.tag(TAG).d("Network consent updated: $granted")
    }
    
    /**
     * Shows network consent dialog to user and returns their decision.
     * 
     * Flow emits true if user allows network access, false if user denies.
     * Consent decision is stored persistently in DataStore.
     * 
     * @param fragmentManager FragmentManager for showing dialog
     * @return Flow<Boolean> emitting true on allow, false on deny
     */
    fun requestConsent(fragmentManager: FragmentManager): Flow<Boolean> = flow {
        Timber.tag(TAG).d("Requesting network consent from user")
        
        // Wait for user decision via suspendCoroutine
        val result = suspendCoroutine<Boolean> { continuation ->
            val dialog = NetworkConsentDialog()
            dialog.onConsentDecision = { granted ->
                Timber.tag(TAG).d("User consent decision: $granted")
                continuation.resume(granted)
            }
            dialog.show(fragmentManager, "network_consent")
        }
        
        // Store decision persistently
        setConsent(result)
        
        // Announce result via TTS
        if (result) {
            ttsManager.announce("Network access granted. Downloading directions.")
        } else {
            ttsManager.announce("Navigation cancelled. Enable internet to use live directions.")
        }
        
        emit(result)
    }
}
