package com.visionfocus.navigation.service

import com.visionfocus.navigation.models.Maneuver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Story 6.3 Task 12: Unit tests for TurnWarningCalculator (CRITICAL Issue #4).
 * 
 * Tests turn warning thresholds: advance (70m), immediate (15m), reroute (10m),
 * and intersection announcements (200m).
 */
class TurnWarningCalculatorTest {
    
    private lateinit var calculator: TurnWarningCalculator
    
    @Before
    fun setUp() {
        calculator = TurnWarningCalculator()
    }
    
    @Test
    fun `shouldGiveAdvanceWarning - triggers at 70 meters`() {
        // Arrange: Distance is exactly at threshold
        val distanceToTurn = 70f
        val hasGivenWarning = false
        val maneuver = Maneuver.TURN_LEFT
        
        // Act
        val result = calculator.shouldGiveAdvanceWarning(distanceToTurn, hasGivenWarning, maneuver)
        
        // Assert
        assertTrue("Should trigger advance warning at 70m", result)
    }
    
    @Test
    fun `shouldGiveAdvanceWarning - does not trigger if already given`() {
        // Arrange: Warning already given
        val distanceToTurn = 65f
        val hasGivenWarning = true
        val maneuver = Maneuver.TURN_RIGHT
        
        // Act
        val result = calculator.shouldGiveAdvanceWarning(distanceToTurn, hasGivenWarning, maneuver)
        
        // Assert
        assertFalse("Should not re-trigger advance warning", result)
    }
    
    @Test
    fun `shouldGiveAdvanceWarning - does not trigger for straight maneuvers`() {
        // Arrange: Going straight ahead
        val distanceToTurn = 60f
        val hasGivenWarning = false
        val maneuver = Maneuver.STRAIGHT
        
        // Act
        val result = calculator.shouldGiveAdvanceWarning(distanceToTurn, hasGivenWarning, maneuver)
        
        // Assert
        assertFalse("Should not warn for straight maneuvers", result)
    }
    
    @Test
    fun `shouldGiveAdvanceWarning - triggers within range 50-70m`() {
        // Arrange: Test multiple distances in range
        val maneuver = Maneuver.TURN_LEFT
        
        // Act & Assert
        assertTrue("50m should trigger", calculator.shouldGiveAdvanceWarning(50f, false, maneuver))
        assertTrue("60m should trigger", calculator.shouldGiveAdvanceWarning(60f, false, maneuver))
        assertTrue("70m should trigger", calculator.shouldGiveAdvanceWarning(70f, false, maneuver))
        assertFalse("75m too far", calculator.shouldGiveAdvanceWarning(75f, false, maneuver))
        assertFalse("40m too close", calculator.shouldGiveAdvanceWarning(40f, false, maneuver))
    }
    
    @Test
    fun `shouldGiveImmediateWarning - triggers at 15 meters`() {
        // Arrange: Distance at immediate threshold
        val distanceToTurn = 15f
        val hasGivenWarning = false
        val maneuver = Maneuver.ROUNDABOUT_LEFT
        
        // Act
        val result = calculator.shouldGiveImmediateWarning(distanceToTurn, hasGivenWarning, maneuver)
        
        // Assert
        assertTrue("Should trigger immediate warning at 15m", result)
    }
    
    @Test
    fun `shouldGiveImmediateWarning - does not trigger if already given`() {
        // Arrange: Warning already given
        val distanceToTurn = 12f
        val hasGivenWarning = true
        val maneuver = Maneuver.MERGE_LEFT
        
        // Act
        val result = calculator.shouldGiveImmediateWarning(distanceToTurn, hasGivenWarning, maneuver)
        
        // Assert
        assertFalse("Should not re-trigger immediate warning", result)
    }
    
    @Test
    fun `shouldGiveImmediateWarning - triggers within range 10-20m`() {
        // Arrange: Test multiple distances
        val maneuver = Maneuver.TURN_RIGHT
        
        // Act & Assert
        assertTrue("10m should trigger", calculator.shouldGiveImmediateWarning(10f, false, maneuver))
        assertTrue("15m should trigger", calculator.shouldGiveImmediateWarning(15f, false, maneuver))
        assertTrue("20m should trigger", calculator.shouldGiveImmediateWarning(20f, false, maneuver))
        assertFalse("25m too far", calculator.shouldGiveImmediateWarning(25f, false, maneuver))
        assertFalse("5m too close", calculator.shouldGiveImmediateWarning(5f, false, maneuver))
    }
    
    @Test
    fun `shouldReroute - triggers at 10 meters past turn`() {
        // Arrange: User missed turn by 10m
        val distanceFromIntendedTurn = 10f
        
        // Act
        val result = calculator.shouldReroute(distanceFromIntendedTurn)
        
        // Assert
        assertTrue("Should reroute at 10m past turn", result)
    }
    
    @Test
    fun `shouldReroute - does not trigger before 10 meters`() {
        // Arrange: User only 5m past turn (might turn around)
        val distanceFromIntendedTurn = 5f
        
        // Act
        val result = calculator.shouldReroute(distanceFromIntendedTurn)
        
        // Assert
        assertFalse("Should not reroute before 10m threshold", result)
    }
    
    @Test
    fun `shouldAnnounceIntersection - triggers at 200 meters`() {
        // Arrange: Approaching complex intersection
        val distanceToIntersection = 200f
        val hasAnnounced = false
        
        // Act
        val result = calculator.shouldAnnounceIntersection(distanceToIntersection, hasAnnounced)
        
        // Assert
        assertTrue("Should announce intersection at 200m", result)
    }
    
    @Test
    fun `shouldAnnounceIntersection - does not trigger if already announced`() {
        // Arrange: Intersection already announced
        val distanceToIntersection = 180f
        val hasAnnounced = true
        
        // Act
        val result = calculator.shouldAnnounceIntersection(distanceToIntersection, hasAnnounced)
        
        // Assert
        assertFalse("Should not re-announce intersection", result)
    }
    
    @Test
    fun `shouldAnnounceIntersection - triggers within range 150-250m`() {
        // Arrange & Act & Assert
        assertTrue("150m should trigger", calculator.shouldAnnounceIntersection(150f, false))
        assertTrue("200m should trigger", calculator.shouldAnnounceIntersection(200f, false))
        assertTrue("250m should trigger", calculator.shouldAnnounceIntersection(250f, false))
        assertFalse("300m too far", calculator.shouldAnnounceIntersection(300f, false))
        assertFalse("100m too close", calculator.shouldAnnounceIntersection(100f, false))
    }
    
    @Test
    fun `roundabout maneuvers trigger warnings correctly`() {
        // Arrange: Roundabout maneuvers should trigger warnings
        val distanceAdvance = 65f
        val distanceImmediate = 15f
        
        // Act & Assert
        assertTrue("Roundabout left should trigger advance", 
            calculator.shouldGiveAdvanceWarning(distanceAdvance, false, Maneuver.ROUNDABOUT_LEFT))
        assertTrue("Roundabout right should trigger advance", 
            calculator.shouldGiveAdvanceWarning(distanceAdvance, false, Maneuver.ROUNDABOUT_RIGHT))
        assertTrue("Roundabout straight should trigger immediate", 
            calculator.shouldGiveImmediateWarning(distanceImmediate, false, Maneuver.ROUNDABOUT_STRAIGHT))
    }
}
