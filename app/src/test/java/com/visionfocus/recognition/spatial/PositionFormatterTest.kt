package com.visionfocus.recognition.spatial

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for PositionFormatter
 * 
 * Story 4.5: Tests natural language conversion for spatial positions
 * - All 9 position zones produce human-readable descriptions
 * - No robotic "X: 150px" style output
 */
class PositionFormatterTest {
    
    @Test
    fun `format top left position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.TOP_LEFT)
        
        assertEquals("on the left side, near the top", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format top center position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.TOP_CENTER)
        
        assertEquals("in the center, near the top", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format top right position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.TOP_RIGHT)
        
        assertEquals("on the right side, near the top", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format center left position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.CENTER_LEFT)
        
        assertEquals("on the left side", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format center center position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.CENTER_CENTER)
        
        assertEquals("in the center of view", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format center right position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.CENTER_RIGHT)
        
        assertEquals("on the right side", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format bottom left position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.BOTTOM_LEFT)
        
        assertEquals("on the left side, near the bottom", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format bottom center position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.BOTTOM_CENTER)
        
        assertEquals("in the center, near the bottom", description)
        assertNoRoboticPhrasing(description)
    }
    
    @Test
    fun `format bottom right position`() {
        val description = PositionFormatter.toNaturalLanguage(Position.BOTTOM_RIGHT)
        
        assertEquals("on the right side, near the bottom", description)
        assertNoRoboticPhrasing(description)
    }
    
    private fun assertNoRoboticPhrasing(description: String) {
        assertFalse(description.contains("X:"), "Should not contain X coordinates")
        assertFalse(description.contains("Y:"), "Should not contain Y coordinates")
        assertFalse(description.contains("px"), "Should not contain pixel units")
        assertFalse(description.contains("("), "Should not contain parentheses")
        assertFalse(description.matches(Regex(".*\\d+.*")), "Should not contain raw numbers")
    }
}
