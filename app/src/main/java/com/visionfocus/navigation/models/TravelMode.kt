package com.visionfocus.navigation.models

/**
 * Travel modes supported by Google Maps Directions API.
 * 
 * Story 6.2: Default to WALKING for accessibility use case.
 * Future stories may add mode selection UI.
 */
enum class TravelMode(val apiValue: String) {
    WALKING("walking"),
    DRIVING("driving"),
    BICYCLING("bicycling"),
    TRANSIT("transit")
}
