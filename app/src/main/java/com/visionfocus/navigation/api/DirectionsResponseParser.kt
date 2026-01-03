package com.visionfocus.navigation.api

import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.Maneuver
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.RouteStep
import timber.log.Timber

/**
 * Parses Google Maps Directions API JSON response into NavigationRoute domain model.
 * 
 * Story 6.2: Converts DTOs to clean domain objects for navigation logic.
 * Handles HTML stripping for TTS announcements, distance/duration extraction,
 * and maneuver type mapping.
 */
object DirectionsResponseParser {
    
    private const val TAG = "DirectionsResponseParser"
    
    /**
     * Parses Google Maps Directions API JSON response into NavigationRoute domain model.
     * 
     * Extracts primary route (routes[0]), parses all legs and steps,
     * strips HTML from instructions, converts distances to meters, durations to seconds.
     * 
     * @param response Directions API JSON response DTO
     * @param origin Origin coordinates for route validation
     * @param destination Destination coordinates for route validation
     * @return NavigationRoute with all turn-by-turn steps
     * @throws DirectionsError.ApiError if response is invalid or missing data
     */
    fun parse(
        response: DirectionsResponseDto,
        origin: LatLng,
        destination: LatLng
    ): NavigationRoute {
        // Validate response status
        if (response.status != "OK") {
            Timber.tag(TAG).e("Directions API returned status: ${response.status}")
            throw DirectionsError.ApiError("Directions API returned status: ${response.status}")
        }
        
        // Extract primary route
        val route = response.routes.firstOrNull()
        if (route == null) {
            Timber.tag(TAG).e("No routes found in response")
            throw DirectionsError.ApiError("No routes found in response")
        }
        
        // Parse all legs (typically one leg for single origin/destination)
        val allSteps = mutableListOf<RouteStep>()
        var totalDistance = 0
        var totalDuration = 0
        
        route.legs.forEach { leg ->
            totalDistance += leg.distance.value
            totalDuration += leg.duration.value
            
            leg.steps.forEach { step ->
                allSteps.add(
                    RouteStep(
                        instruction = stripHtml(step.html_instructions ?: "Continue"),
                        distance = step.distance.value,
                        duration = step.duration.value,
                        maneuver = Maneuver.fromString(step.maneuver),
                        startLocation = LatLng(
                            step.start_location.lat,
                            step.start_location.lng
                        ),
                        endLocation = LatLng(
                            step.end_location.lat,
                            step.end_location.lng
                        ),
                        polyline = step.polyline?.points
                    )
                )
            }
        }
        
        Timber.tag(TAG).d("Parsed route: ${allSteps.size} steps, ${totalDistance}m, ${totalDuration}s")
        
        return NavigationRoute(
            origin = origin,
            destination = destination,
            steps = allSteps,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            polyline = route.overview_polyline.points,
            summary = route.summary ?: "Route to destination"
        )
    }
    
    /**
     * Strips HTML tags from Directions API instructions for TTS announcements.
     * 
     * Google returns instructions like: "Turn <b>left</b> onto <b>Main St</b>"
     * Converts to: "Turn left onto Main St"
     * 
     * @param html HTML-formatted instruction text
     * @return Plain text instruction suitable for TTS
     */
    private fun stripHtml(html: String): String {
        return html
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("<div.*?>".toRegex(), "")
            .replace("</div>", "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }
}
