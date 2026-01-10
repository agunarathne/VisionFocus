package com.visionfocus.beacon

import android.content.Context
import android.util.Log
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proximity-based navigation service using Bluetooth beacon RSSI.
 * 
 * Epic 10 Story 10.2: Guides user to beacon location using signal strength.
 * 
 * Navigation strategy:
 * - RSSI thresholds determine proximity zones (far/medium/close/arrived)
 * - Audio announcements guide user ("Getting closer", "Very close", etc.)
 * - Haptic pulse frequency increases as user approaches beacon
 * - Kalman filter smooths noisy RSSI readings
 * 
 * RSSI-to-distance is unreliable indoors, so we use zone-based approach
 * instead of precise positioning.
 */
@Singleton
class ProximityNavigationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val beaconScanner: BeaconScannerService,
    private val ttsManager: TTSManager,
    private val hapticManager: HapticFeedbackManager
) {
    
    companion object {
        private const val TAG = "ProximityNavigationService"
        
        // RSSI threshold zones (dBm) - Tuned for ~2m intervals
        // Based on log-distance path loss model approximation
        private const val RSSI_ARRIVED = -55     // < 1 meter ("Reached")
        private const val RSSI_VERY_CLOSE = -68  // 1-3 meters ("Very closer")
        private const val RSSI_CLOSE = -78       // 3-5 meters ("Closer")
        private const val RSSI_MEDIUM = -88      // 5-8 meters ("Signal found / So far")
        // RSSI_FAR = anything below -88
        
        // Haptic pulse intervals (milliseconds)
        private const val HAPTIC_ARRIVED = 100L
        private const val HAPTIC_VERY_CLOSE = 250L
        private const val HAPTIC_CLOSE = 500L
        private const val HAPTIC_MEDIUM = 1000L
        private const val HAPTIC_FAR = 2000L
        
        // Announcement throttling
        private const val ANNOUNCEMENT_COOLDOWN_MS = 2500L
    }
    
    private var navigationJob: Job? = null
    private var lastAnnouncementTime = 0L
    private var lastProximityZone: ProximityZone? = null
    private val rssiFilter = KalmanRssiFilter()
    
    /**
     * Start proximity-based navigation to beacon.
     * 
     * Story 10.2: Voice command triggers this method with target beacon MAC.
     * Continuously scans for beacon and provides audio + haptic guidance.
     * 
     * @param targetMacAddress MAC address of target beacon
     * @param beaconName User-friendly name for announcements
     * @param scope CoroutineScope for navigation lifecycle
     */
    fun startNavigation(
        targetMacAddress: String,
        beaconName: String,
        scope: CoroutineScope
    ) {
        // Cancel any existing navigation
        stopNavigation()
        
        Log.d(TAG, "Starting proximity navigation to: $beaconName ($targetMacAddress)")
        
        rssiFilter.reset()
        lastProximityZone = null
        
        navigationJob = scope.launch {
            // Announce start
            ttsManager.announce("Searching for $beaconName beacon")
            
            beaconScanner.scanForSpecificBeacon(targetMacAddress)
                .onEach { rssi ->
                    handleRssiUpdate(rssi, beaconName, this)
                }
                .catch { e ->
                    Log.e(TAG, "Navigation error", e)
                    ttsManager.announce("Navigation error")
                    stopNavigation()
                }
                .collect()
        }
    }
    
    /**
     * Stop active navigation.
     */
    fun stopNavigation() {
        navigationJob?.cancel()
        navigationJob = null
        rssiFilter.reset()
        lastProximityZone = null
        Log.d(TAG, "Proximity navigation stopped")
    }
    
    /**
     * Handle RSSI update from beacon scanner.
     * Applies Kalman filter, determines proximity zone, and triggers guidance.
     */
    private suspend fun handleRssiUpdate(rawRssi: Int, beaconName: String, scope: CoroutineScope) {
        // Apply Kalman filter to smooth noisy RSSI
        val filteredRssi = rssiFilter.update(rawRssi.toFloat()).toInt()
        
        // Determine proximity zone
        val zone = when {
            filteredRssi >= RSSI_ARRIVED -> ProximityZone.ARRIVED
            filteredRssi >= RSSI_VERY_CLOSE -> ProximityZone.VERY_CLOSE
            filteredRssi >= RSSI_CLOSE -> ProximityZone.CLOSE
            filteredRssi >= RSSI_MEDIUM -> ProximityZone.MEDIUM
            else -> ProximityZone.FAR
        }
        
        Log.d(TAG, "RSSI: $rawRssi → $filteredRssi dBm | Zone: $zone")
        
        // Only announce if zone changed
        if (zone != lastProximityZone) {
            announceProximity(zone, beaconName)
            lastProximityZone = zone
            
            // Stop navigation on arrival
            if (zone == ProximityZone.ARRIVED) {
                stopNavigation()
            }
        }
        
        // Continuous haptic feedback based on zone
        provideHapticFeedback(zone, scope)
    }
    
    /**
     * Announce proximity zone via TTS.
     * Throttled to avoid announcement spam.
     */
    private suspend fun announceProximity(zone: ProximityZone, beaconName: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnnouncementTime < ANNOUNCEMENT_COOLDOWN_MS &&
            zone != ProximityZone.ARRIVED) {
            return  // Skip announcement if cooldown active
        }
        
        val message = when (zone) {
            ProximityZone.ARRIVED -> "You have reached $beaconName"
            ProximityZone.VERY_CLOSE -> "Very close"
            ProximityZone.CLOSE -> "Getting closer"
            ProximityZone.MEDIUM -> "$beaconName is far, but signal detected"
            ProximityZone.FAR -> "Signal weak, search around"
        }
        
        ttsManager.announce(message)
        lastAnnouncementTime = currentTime
    }
    
    /**
     * Provide haptic pulse feedback based on proximity.
     * Pulse frequency increases as user gets closer (warmer/colder game).
     */
    private fun provideHapticFeedback(zone: ProximityZone, scope: CoroutineScope) {
        val pattern = when (zone) {
            ProximityZone.ARRIVED -> HapticPattern.NavigationArrived  // Triple pulse
            ProximityZone.VERY_CLOSE -> HapticPattern.ProximityVeryClose  // Fast pulse
            ProximityZone.CLOSE -> HapticPattern.ProximityClose
            ProximityZone.MEDIUM -> HapticPattern.ProximityFar
            ProximityZone.FAR -> HapticPattern.ProximityFar  // Slow pulse
        }
        
        scope.launch {
            hapticManager.trigger(pattern)
        }
    }
}

/**
 * Proximity zones based on RSSI thresholds.
 */
enum class ProximityZone {
    FAR,        // > 10 meters
    MEDIUM,     // ~10 meters
    CLOSE,      // ~5 meters
    VERY_CLOSE, // ~2-3 meters
    ARRIVED     // < 1 meter
}

/**
 * Kalman filter for RSSI smoothing.
 * 
 * RSSI values bounce wildly due to interference, reflections, and obstacles.
 * This filter smooths readings over ~5 samples to prevent "getting closer...
 * farther... closer" chaos.
 * 
 * Based on simple 1D Kalman filter algorithm.
 */
class KalmanRssiFilter {
    companion object {
        private const val PROCESS_NOISE = 0.5f
        private const val MEASUREMENT_NOISE = 4.0f
    }
    
    private var estimate: Float = -100f
    private var errorEstimate: Float = 1f
    
    fun update(measurement: Float): Float {
        // Prediction step
        val predictedEstimate = estimate
        val predictedError = errorEstimate + PROCESS_NOISE
        
        // Update step
        val kalmanGain = predictedError / (predictedError + MEASUREMENT_NOISE)
        estimate = predictedEstimate + kalmanGain * (measurement - predictedEstimate)
        errorEstimate = (1 - kalmanGain) * predictedError
        
        return estimate
    }
    
    fun reset() {
        estimate = -100f
        errorEstimate = 1f
    }
}
