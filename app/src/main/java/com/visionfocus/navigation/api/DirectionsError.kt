package com.visionfocus.navigation.api

/**
 * Sealed class for Directions API errors.
 * 
 * Story 6.2: Type-safe error handling for Google Maps API integration.
 * Each error type maps to specific user-facing TTS announcements.
 */
sealed class DirectionsError(message: String) : Exception(message) {
    /**
     * User has not granted network consent yet.
     * Action: Show network consent dialog.
     */
    class ConsentRequired(message: String) : DirectionsError(message)
    
    /**
     * Network unavailable (airplane mode, no WiFi/data).
     * TTS: "Cannot download directions. Check internet connection."
     */
    class NetworkUnavailable(message: String) : DirectionsError(message)
    
    /**
     * Invalid API key (HTTP 403).
     * TTS: "Navigation service unavailable. Please try again later."
     */
    class InvalidApiKey(message: String) : DirectionsError(message)
    
    /**
     * API quota exceeded (HTTP 429).
     * TTS: "Navigation service unavailable. Please try again later."
     */
    class QuotaExceeded(message: String) : DirectionsError(message)
    
    /**
     * Invalid request (HTTP 400) - malformed origin/destination.
     * TTS: "Invalid destination. Please try again."
     */
    class InvalidRequest(message: String) : DirectionsError(message)
    
    /**
     * Request timeout (30s read timeout exceeded).
     * TTS: "Request timed out. Please try again."
     */
    class Timeout(message: String) : DirectionsError(message)
    
    /**
     * Generic API error (non-2xx status code).
     * TTS: "Navigation service unavailable. Please try again later."
     */
    class ApiError(message: String) : DirectionsError(message)
    
    /**
     * Unexpected error (parsing failure, unknown exception).
     * TTS: "Unexpected error occurred. Please try again."
     */
    class Unknown(message: String) : DirectionsError(message)
}
