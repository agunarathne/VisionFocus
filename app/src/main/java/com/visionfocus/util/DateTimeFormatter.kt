package com.visionfocus.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility for formatting timestamps in recognition history.
 * 
 * Provides consistent date-time formatting across the application.
 * Uses device's local timezone for display.
 * 
 * Story 4.2: Recognition history timestamp formatting (AC6)
 * Code Review Fix: Thread-safe implementation (SimpleDateFormat not shared)
 */
object DateTimeFormatter {
    
    /**
     * Formats Unix timestamp (milliseconds) to human-readable string.
     * 
     * Thread-safe: Creates new SimpleDateFormat instance per call to avoid
     * concurrent access issues with shared mutable state.
     * 
     * Example: 1703438700000L → "December 24, 2025 at 3:45 PM"
     * 
     * @param timestampMillis Unix epoch time in milliseconds
     * @return Formatted date-time string in user's local timezone
     */
    fun formatTimestamp(timestampMillis: Long): String {
        val formatter = SimpleDateFormat(
            "MMMM d, yyyy 'at' h:mm a",
            Locale.US
        )
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(Date(timestampMillis))
    }
}
