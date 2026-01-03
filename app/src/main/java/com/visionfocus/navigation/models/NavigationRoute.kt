package com.visionfocus.navigation.models

/**
 * Complete navigation route from origin to destination.
 * 
 * Story 6.2: Parsed from Google Maps Directions API response.
 * Contains turn-by-turn steps for voice guidance (Story 6.3).
 * 
 * @property origin Starting location coordinates
 * @property destination Ending location coordinates
 * @property steps List of turn-by-turn navigation steps
 * @property totalDistance Total route distance in meters
 * @property totalDuration Estimated duration in seconds
 * @property polyline Encoded polyline for route visualization (Story 6.3)
 * @property summary Human-readable route summary (e.g., "via Main St and Oak Ave")
 */
data class NavigationRoute(
    val origin: LatLng,
    val destination: LatLng,
    val steps: List<RouteStep>,
    val totalDistance: Int,        // meters
    val totalDuration: Int,        // seconds
    val polyline: String,
    val summary: String
)

/**
 * Single step in navigation route.
 * 
 * @property instruction Human-readable instruction for TTS (HTML stripped)
 * @property distance Distance to this step in meters
 * @property duration Estimated duration to reach this step in seconds
 * @property maneuver Type of maneuver (turn-left, turn-right, straight, etc.)
 * @property startLocation GPS coordinates where step begins
 * @property endLocation GPS coordinates where step ends
 * @property polyline Encoded polyline for this step (optional, for map display)
 */
data class RouteStep(
    val instruction: String,       // "Turn left onto Main Street"
    val distance: Int,              // meters to this step
    val duration: Int,              // seconds to this step
    val maneuver: Maneuver,
    val startLocation: LatLng,
    val endLocation: LatLng,
    val polyline: String? = null
)

/**
 * Maneuver types from Google Maps Directions API.
 * Mapped from API string values to enum for type safety.
 */
enum class Maneuver {
    TURN_LEFT,
    TURN_RIGHT,
    TURN_SLIGHT_LEFT,
    TURN_SLIGHT_RIGHT,
    TURN_SHARP_LEFT,
    TURN_SHARP_RIGHT,
    STRAIGHT,
    RAMP_LEFT,
    RAMP_RIGHT,
    MERGE,
    FORK_LEFT,
    FORK_RIGHT,
    ROUNDABOUT_LEFT,
    ROUNDABOUT_RIGHT,
    UTURN_LEFT,
    UTURN_RIGHT,
    UNKNOWN;
    
    companion object {
        fun fromString(value: String?): Maneuver {
            return when (value) {
                "turn-left" -> TURN_LEFT
                "turn-right" -> TURN_RIGHT
                "turn-slight-left" -> TURN_SLIGHT_LEFT
                "turn-slight-right" -> TURN_SLIGHT_RIGHT
                "turn-sharp-left" -> TURN_SHARP_LEFT
                "turn-sharp-right" -> TURN_SHARP_RIGHT
                "straight" -> STRAIGHT
                "ramp-left" -> RAMP_LEFT
                "ramp-right" -> RAMP_RIGHT
                "merge" -> MERGE
                "fork-left" -> FORK_LEFT
                "fork-right" -> FORK_RIGHT
                "roundabout-left" -> ROUNDABOUT_LEFT
                "roundabout-right" -> ROUNDABOUT_RIGHT
                "uturn-left" -> UTURN_LEFT
                "uturn-right" -> UTURN_RIGHT
                else -> UNKNOWN
            }
        }
    }
}
