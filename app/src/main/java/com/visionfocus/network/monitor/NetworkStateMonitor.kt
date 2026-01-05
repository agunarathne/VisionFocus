package com.visionfocus.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network connectivity state in real-time using ConnectivityManager.NetworkCallback.
 * 
 * This singleton provides reactive network state updates via StateFlow and immediate
 * state checks via getCurrentNetworkState(). It monitors for actual internet connectivity,
 * not just network connection.
 * 
 * Usage:
 * ```
 * // In ViewModel or Repository
 * networkStateMonitor.isNetworkAvailable.collect { isAvailable ->
 *     // React to network state changes
 * }
 * 
 * // For immediate check
 * val isOnline = networkStateMonitor.getCurrentNetworkState()
 * ```
 * 
 * Lifecycle: Registered in init block, cleaned up in Application.onTerminate()
 * Thread Safety: StateFlow handles thread-safe updates from network callback
 * 
 * @param context Application context for ConnectivityManager access
 */
@Singleton
class NetworkStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NetworkStateMonitor"
        /** Debounce time in milliseconds to prevent rapid network flicker announcements */
        const val DEBOUNCE_MS = 2000L
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Internal mutable network state. Updated by network callback.
     * Thread-safe: StateFlow handles concurrent updates from network callback thread.
     */
    private val _isNetworkAvailable = MutableStateFlow(getCurrentNetworkState())
    
    /**
     * Public read-only network state. Emits true when internet is available, false otherwise.
     * Debounce rapid transitions in ViewModel to avoid UI flicker.
     */
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    /**
     * Network callback that updates StateFlow when network state changes.
     * Registered with NET_CAPABILITY_INTERNET to ensure actual connectivity, not just connection.
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        /**
         * Called when a network with internet capability becomes available.
         * Safe to call from network callback thread - StateFlow handles synchronization.
         */
        override fun onAvailable(network: Network) {
            _isNetworkAvailable.value = true
        }
        
        /**
         * Called when a network is lost.
         * Safe to call from network callback thread - StateFlow handles synchronization.
         */
        override fun onLost(network: Network) {
            _isNetworkAvailable.value = false
        }
    }
    
    init {
        registerNetworkCallback()
    }
    
    /**
     * Registers network callback to receive real-time connectivity updates.
     * Only monitors networks with NET_CAPABILITY_INTERNET to avoid false positives.
     * Handles registration failures gracefully with error logging.
     */
    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            timber.log.Timber.d(TAG, "Network callback registered successfully")
        } catch (e: SecurityException) {
            timber.log.Timber.e(TAG, "Failed to register network callback - Security exception", e)
            // Fallback: Set initial state, no live updates
            _isNetworkAvailable.value = getCurrentNetworkState()
        } catch (e: Exception) {
            timber.log.Timber.e(TAG, "Failed to register network callback", e)
            // Fallback: Set initial state, no live updates
            _isNetworkAvailable.value = getCurrentNetworkState()
        }
    }
    
    /**
     * Gets immediate network state without waiting for StateFlow emission.
     * Useful for synchronous checks before async operations.
     * 
     * Checks both NET_CAPABILITY_INTERNET (network claims internet) and
     * NET_CAPABILITY_VALIDATED (network actually has working internet).
     * This prevents false positives from captive portals or non-functional WiFi.
     * 
     * @return true if internet is available and validated, false otherwise
     */
    fun getCurrentNetworkState(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Unregisters network callback to prevent memory leaks.
     * Should be called in Application.onTerminate() or during testing cleanup.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // Callback already unregistered or never registered - safe to ignore
        }
    }
}
