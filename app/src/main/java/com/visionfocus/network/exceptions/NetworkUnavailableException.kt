package com.visionfocus.network.exceptions

/**
 * Exception thrown when network connectivity is required but unavailable.
 * 
 * This exception provides context about offline map availability to help
 * the UI determine appropriate recovery actions.
 * 
 * Usage Scenarios:
 * - NavigationRepository: Thrown when attempting live directions without internet
 * - DirectionsApiService: Thrown when API call fails due to no connectivity
 * - NetworkConsentManager: Thrown when network required but consent not given
 * 
 * Recovery Actions:
 * - If offlineMapsAvailable=true: Offer "Use Offline Maps" option
 * - If offlineMapsAvailable=false: Offer "Wait for Connection" only
 * 
 * Example:
 * ```
 * if (!networkStateMonitor.getCurrentNetworkState()) {
 *     throw NetworkUnavailableException.noConnection(hasOfflineMaps = false)
 * }
 * ```
 * 
 * @param message User-friendly error message explaining the issue
 * @param offlineMapsAvailable True if offline maps available for this destination
 */
class NetworkUnavailableException(
    override val message: String,
    val offlineMapsAvailable: Boolean
) : Exception(message) {
    
    companion object {
        /**
         * Factory method for standard no-connection scenario.
         * 
         * @param hasOfflineMaps True if offline maps available for destination
         * @return NetworkUnavailableException with appropriate message
         */
        fun noConnection(hasOfflineMaps: Boolean): NetworkUnavailableException {
            val message = if (hasOfflineMaps) {
                "No internet connection. You can navigate using saved offline maps or wait for connectivity."
            } else {
                "No internet connection. Please connect to internet to use navigation."
            }
            return NetworkUnavailableException(message, hasOfflineMaps)
        }
        
        /**
         * Factory method for scenario where network required but consent not given.
         * 
         * @return NetworkUnavailableException with consent message
         */
        fun consentRequired(): NetworkUnavailableException {
            return NetworkUnavailableException(
                message = "Network access required. Please grant permission to use live directions.",
                offlineMapsAvailable = false
            )
        }
        
        /**
         * Factory method for scenario where API call failed due to network timeout.
         * 
         * @return NetworkUnavailableException with timeout message
         */
        fun timeout(): NetworkUnavailableException {
            return NetworkUnavailableException(
                message = "Network request timed out. Please check your internet connection.",
                offlineMapsAvailable = false
            )
        }
    }
}
