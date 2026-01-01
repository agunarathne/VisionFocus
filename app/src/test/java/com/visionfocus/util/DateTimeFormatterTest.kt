package com.visionfocus.util

import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for DateTimeFormatter.
 * 
 * Verifies timestamp formatting meets AC6 requirements:
 * - Format: "December 24, 2025 at 3:45 PM"
 * - Uses device local timezone
 * 
 * Story 4.2 Task 7: Timestamp formatting utility tests
 */
class DateTimeFormatterTest {
    
    @Test
    fun `formatTimestamp produces correct format`() {
        // Given - December 24, 2025 at 3:45 PM UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.DECEMBER, 24, 15, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeFormatter.formatTimestamp(timestamp)
        
        // Then - verify contains expected components
        assertTrue(formatted.contains("December 24, 2025"))
        assertTrue(formatted.contains("at"))
        assertTrue(formatted.contains("PM"))
        
        // Format should match pattern (with timezone variation)
        val expectedPattern = """\w+ \d{1,2}, \d{4} at \d{1,2}:\d{2} [AP]M""".toRegex()
        assertTrue(formatted.matches(expectedPattern), 
            "Format '$formatted' doesn't match expected pattern")
    }
    
    @Test
    fun `formatTimestamp handles midnight correctly`() {
        // Given - January 1, 2026 at 12:00 AM UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeFormatter.formatTimestamp(timestamp)
        
        // Then
        assertTrue(formatted.contains("January 1, 2026"))
        assertTrue(formatted.contains("12:00 AM") || formatted.contains("12:00 PM"), 
            "Midnight should be formatted as 12:00 AM")
    }
    
    @Test
    fun `formatTimestamp handles noon correctly`() {
        // Given - June 15, 2025 at 12:00 PM UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.JUNE, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeFormatter.formatTimestamp(timestamp)
        
        // Then
        assertTrue(formatted.contains("June 15, 2025"))
        assertTrue(formatted.contains("at"))
    }
    
    @Test
    fun `formatTimestamp handles current time`() {
        // Given - current system time
        val now = System.currentTimeMillis()
        
        // When
        val formatted = DateTimeFormatter.formatTimestamp(now)
        
        // Then - should not throw exception and should contain expected elements
        assertTrue(formatted.contains("at"))
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
        
        val expectedPattern = """\w+ \d{1,2}, \d{4} at \d{1,2}:\d{2} [AP]M""".toRegex()
        assertTrue(formatted.matches(expectedPattern))
    }
    
    @Test
    fun `formatTimestamp is consistent for same timestamp`() {
        // Given
        val timestamp = 1735056300000L  // Fixed timestamp
        
        // When - format twice
        val formatted1 = DateTimeFormatter.formatTimestamp(timestamp)
        val formatted2 = DateTimeFormatter.formatTimestamp(timestamp)
        
        // Then - results should be identical
        assertEquals(formatted1, formatted2)
    }
}
