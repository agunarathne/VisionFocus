package com.visionfocus.navigation.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Maps Directions API.
 * 
 * Story 6.2: HTTP client for requesting turn-by-turn directions.
 * Endpoint: https://maps.googleapis.com/maps/api/directions/json
 */
interface DirectionsApi {
    @GET("json")
    suspend fun getDirections(
        @Query("origin") origin: String,        // "lat,lng" format
        @Query("destination") destination: String,  // "lat,lng" format
        @Query("mode") mode: String,             // "walking", "driving", etc.
        @Query("key") key: String                // API key from BuildConfig
    ): Response<DirectionsResponseDto>
}
