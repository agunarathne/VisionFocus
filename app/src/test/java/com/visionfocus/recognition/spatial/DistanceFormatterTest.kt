package com.visionfocus.recognition.spatial

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for DistanceFormatter
 * 
 * Story 4.5: Tests natural language conversion for spatial distances
 * - All 3 distance levels produce human-readable descriptions
 * - Supports both with and without prepositions
 * - No robotic phrasing
 */
class DistanceFormatterTest {
    
    @Test
    fun `format close distance without preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.CLOSE, withPreposition = false)
        
        assertEquals("close by", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format close distance with preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.CLOSE, withPreposition = true)
        
        assertEquals("close by", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format medium distance without preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.MEDIUM, withPreposition = false)
        
        assertEquals("at medium distance", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format medium distance with preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.MEDIUM, withPreposition = true)
        
        assertEquals("at medium distance", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format far distance without preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.FAR, withPreposition = false)
        
        assertEquals("far away", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format far distance with preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.FAR, withPreposition = true)
        
        assertEquals("far away", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `default parameter uses without preposition`() {
        val description = DistanceFormatter.toNaturalLanguage(Distance.CLOSE)
        
        assertEquals("close by", description)
    }
    
    private fun assertNoRoboticPhrasing(description: String) {
        assertFalse(description.contains("%"), "Should not contain percentage")
        assertFalse(description.contains("meters"), "Should not contain absolute units")
        assertFalse(description.contains("cm"), "Should not contain absolute units")
        assertFalse(description.contains("feet"), "Should not contain absolute units")
        assertFalse(description.matches(Regex(".*\\d+.*")), "Should not contain raw numbers")
    }
}
