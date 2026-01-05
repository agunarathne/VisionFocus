package com.visionfocus.network.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.network.monitor.NetworkStateMonitor
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing network status UI and TTS announcements.
 * 
 * Transforms raw network state from NetworkStateMonitor into user-friendly NetworkStatus
 * with appropriate TTS announcements for state transitions.
 * 
 * Network Status Types:
 * - Online: Internet available, live directions possible
 * - Offline: No internet, saved maps required for navigation
 * - OfflineWithMaps: No internet, but offline maps available (Story 7.4)
 * 
 * TTS Announcement Strategy:
 * - Only announce transitions, NOT initial state (avoid noise on app launch)
 * - Debounce rapid transitions <2 seconds to prevent announcement spam
 * - Use TTSManager.Priority.MEDIUM for informational announcements
 * - Clear, actionable messages: "Internet connected" not "Network available"
 * 
 * Usage:
 * ```
 * // In Fragment
 * viewLifecycleOwner.lifecycleScope.launch {
 *     viewModel.networkStatus.collect { status ->
 *         updateUI(status)
 *     }
 * }
 * ```
 * 
 * @param networkStateMonitor Singleton network state monitor
 * @param ttsManager TTS manager for announcements
 */
@HiltViewModel
class NetworkStatusViewModel @Inject constructor(
    private val networkStateMonitor: NetworkStateMonitor,
    private val ttsManager: TTSManager
    // TODO: Inject OfflineMapRepository when Story 7.4 implemented
    // private val offlineMapRepository: OfflineMapRepository
) : ViewModel() {
    
    /**
     * Sealed class representing user-facing network status.
     * Maps raw network state to actionable user states.
     */
    sealed class NetworkStatus {
        /** Internet available - live directions work */
        object Online : NetworkStatus()
        
        /** No internet - navigation requires saved maps */
        object Offline : NetworkStatus()
        
        /** No internet but offline maps available (Story 7.4) */
        object OfflineWithMaps : NetworkStatus()
    }
    
    /**
     * Previous network state for transition detection.
     * Null on initial state (no announcement).
     * @Volatile ensures thread-safe reads/writes across coroutine contexts.
     */
    @Volatile
    private var previousNetworkState: NetworkStatus? = null
    
    /**
     * Timestamp of last transition for debouncing.
     * Prevents announcement spam on rapid network flickers.
     * @Volatile ensures thread-safe reads/writes across coroutine contexts.
     */
    @Volatile
    private var lastTransitionTime = 0L
    
    /**
     * Public network status StateFlow for UI observation.
     * 
     * Flow transformations:
     * 1. Debounce 2 seconds to ignore rapid flickers (WiFi <-> cellular)
     * 2. Map boolean network state to NetworkStatus enum
     * 3. Check offline map availability (Story 7.4 integration)
     * 4. Trigger TTS announcements on transitions
     * 5. Share state across collectors with 5-second replay
     * 
     * Initial value: Current network state (no announcement)
     */
    val networkStatus: StateFlow<NetworkStatus> = networkStateMonitor.isNetworkAvailable
        .debounce(NetworkStateMonitor.DEBOUNCE_MS) // Use constant for consistency
        .map { isAvailable ->
            when {
                isAvailable -> NetworkStatus.Online
                // TODO: Check offlineMapRepository.hasOfflineMaps() when Story 7.4 done
                // !isAvailable && hasOfflineMaps -> NetworkStatus.OfflineWithMaps
                else -> NetworkStatus.Offline
            }
        }
        .onEach { newStatus ->
            announceNetworkTransition(newStatus)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (networkStateMonitor.getCurrentNetworkState()) 
                NetworkStatus.Online else NetworkStatus.Offline
        )
    
    /**
     * Announces network state transitions via TTS with debouncing.
     * 
     * Announcement Strategy:
     * - Online → Offline: "Lost internet connection. Navigation will use saved maps or wait for reconnection."
     * - Offline → Online: "Internet connected. Updating route with live traffic."
     * - Initial state: No announcement (avoid noise on app launch)
     * - Rapid transitions: Ignore if <2 seconds since last announcement
     * 
     * Priority: MEDIUM (informational, not safety-critical)
     * 
     * @param newStatus New network status after transition
     */
    private fun announceNetworkTransition(newStatus: NetworkStatus) {
        val currentTime = System.currentTimeMillis()
        
        // Don't announce initial state or rapid transitions
        if (previousNetworkState == null) {
            previousNetworkState = newStatus
            lastTransitionTime = currentTime
            return
        }
        
        if (currentTime - lastTransitionTime < NetworkStateMonitor.DEBOUNCE_MS) {
            // Rapid transition - update state but skip announcement
            previousNetworkState = newStatus
            lastTransitionTime = currentTime
            return
        }
        
        // Announce meaningful transitions
        viewModelScope.launch {
            when {
                previousNetworkState is NetworkStatus.Online && newStatus is NetworkStatus.Offline -> {
                    ttsManager.announce(
                        "Lost internet connection. Navigation will use saved maps or wait for reconnection."
                    )
                }
                previousNetworkState is NetworkStatus.Offline && newStatus is NetworkStatus.Online -> {
                    ttsManager.announce(
                        "Internet connected. Updating route with live traffic."
                    )
                }
                previousNetworkState is NetworkStatus.Online && newStatus is NetworkStatus.OfflineWithMaps -> {
                    ttsManager.announce(
                        "Lost internet connection. Offline maps available for navigation."
                    )
                }
                previousNetworkState is NetworkStatus.OfflineWithMaps && newStatus is NetworkStatus.Online -> {
                    ttsManager.announce(
                        "Internet connected. Live directions available."
                    )
                }
            }
        }
        
        previousNetworkState = newStatus
        lastTransitionTime = currentTime
    }
}
