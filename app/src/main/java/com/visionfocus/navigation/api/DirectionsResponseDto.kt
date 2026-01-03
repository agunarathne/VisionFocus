package com.visionfocus.navigation.api

/**
 * Data Transfer Objects matching Google Maps Directions API JSON schema.
 * 
 * Story 6.2: Response structure from https://maps.googleapis.com/maps/api/directions/json
 * Reference: https://developers.google.com/maps/documentation/directions/get-directions
 * 
 * These DTOs map 1:1 to the API response JSON for automatic Gson deserialization.
 * DirectionsResponseParser converts DTOs to domain models (NavigationRoute, RouteStep).
 */

data class DirectionsResponseDto(
    val status: String,
    val routes: List<RouteDto>
)

data class RouteDto(
    val summary: String?,
    val legs: List<LegDto>,
    val overview_polyline: PolylineDto
)

data class LegDto(
    val distance: DistanceDto,
    val duration: DurationDto,
    val start_address: String,
    val end_address: String,
    val steps: List<StepDto>
)

data class StepDto(
    val html_instructions: String,
    val distance: DistanceDto,
    val duration: DurationDto,
    val maneuver: String?,
    val start_location: LocationDto,
    val end_location: LocationDto,
    val polyline: PolylineDto?
)

data class DistanceDto(
    val value: Int,   // meters
    val text: String  // "1.2 km"
)

data class DurationDto(
    val value: Int,   // seconds
    val text: String  // "5 mins"
)

data class LocationDto(
    val lat: Double,
    val lng: Double
)

data class PolylineDto(
    val points: String  // Encoded polyline string
)
