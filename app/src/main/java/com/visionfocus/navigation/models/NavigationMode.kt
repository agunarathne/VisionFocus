package com.visionfocus.navigation.models

/**
 * Story 7.5: Navigation mode enumeration for online/offline routing strategy.
 * 
 * Determines routing behavior and capabilities:
 * - ONLINE: Google Maps API with live traffic, route recalculation
 * - OFFLINE: Mapbox offline routing with cached tiles, static routes
 * - UNAVAILABLE: No navigation possible (no network, no offline maps)
 * 
 * Mode transitions:
 * - Online → Offline: Network lost + offline maps available
 * - Offline → Online: Network restored + automatic route recalculation
 * - * → Unavailable: No network + no offline maps
 */
enum class NavigationMode {
    /**
     * Online navigation using Google Maps Directions API.
     * 
     * Features:
     * - Live traffic data
     * - Route recalculation on deviation (Story 6.4)
     * - Real-time ETA updates
     * - Dynamic routing
     * 
     * Requirements:
     * - Active internet connection
     * - Network consent granted (Story 6.2)
     */
    ONLINE,
    
    /**
     * Offline navigation using Mapbox cached tiles.
     * 
     * Features:
     * - Turn-by-turn guidance from cached route
     * - No network required
     * - Privacy-enhanced (no API calls)
     * 
     * Limitations:
     * - Static routes (no live traffic)
     * - No route recalculation on deviation
     * - Requires pre-downloaded offline maps (Story 7.4)
     * 
     * Requirements:
     * - Offline maps downloaded for destination area
     * - Maps not expired (30-day expiration)
     */
    OFFLINE,
    
    /**
     * Navigation unavailable - no routing capability.
     * 
     * Occurs when:
     * - No internet connection
     * - No offline maps for destination area
     * - Offline maps expired
     * 
     * Fallback: Basic guidance (Story 7.5 Task 12)
     * - Straight-line distance and bearing
     * - Compass-style directions
     * - No turn-by-turn capability
     */
    UNAVAILABLE
}
