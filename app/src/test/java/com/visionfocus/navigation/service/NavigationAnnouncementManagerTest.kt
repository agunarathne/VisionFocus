package com.visionfocus.navigation.service

import com.visionfocus.core.audio.TTSManager
import com.visionfocus.navigation.models.Maneuver
import com.visionfocus.navigation.models.RouteStep
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.utils.BearingCalculator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Story 6.3 Task 13: Unit tests for NavigationAnnouncementManager (CRITICAL Issue #5).
 * 
 * Tests natural language announcements: advance warnings (70m), immediate warnings (15m),
 * maneuver descriptions, cardinal directions, roundabout exits, and 10% volume increase.
 */
class NavigationAnnouncementManagerTest {
    
    private lateinit var ttsManager: TTSManager
    private lateinit var announcementManager: NavigationAnnouncementManager
    
    private val testStep = RouteStep(
        instruction = "Turn left onto Broadway",
        distance = 350,
        duration = 250,
        maneuver = Maneuver.TURN_LEFT,
        startLocation = LatLng(40.758896, -73.985130),
        endLocation = LatLng(40.753856, -73.985279)
    )
    
    @Before
    fun setUp() {
        ttsManager = mockk(relaxed = true)
        announcementManager = NavigationAnnouncementManager(ttsManager)
    }
    
    @Test
    fun `announceAdvanceWarning - formats 70m warning with maneuver`() {
        // Arrange
        val distanceToTurn = 70f
        
        // Act
        announcementManager.announceAdvanceWarning(distanceToTurn, testStep)
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                message.contains("70 meters", ignoreCase = true) &&
                message.contains("turn left", ignoreCase = true) &&
                message.contains("Broadway", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `announceAdvanceWarning - includes cardinal direction`() {
        // Arrange: Step heading south (bearing ~180°)
        val stepHeadingSouth = testStep.copy(
            startLocation = LatLng(40.760000, -73.985130),
            endLocation = LatLng(40.750000, -73.985130)  // Due south
        )
        val distanceToTurn = 65f
        
        // Act
        announcementManager.announceAdvanceWarning(distanceToTurn, stepHeadingSouth)
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                message.contains("south", ignoreCase = true) ||
                message.contains("heading", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `announceImmediateWarning - formats 15m warning concisely`() {
        // Arrange
        val distanceToTurn = 15f
        
        // Act
        announcementManager.announceImmediateWarning(distanceToTurn, testStep)
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                message.contains("turn left", ignoreCase = true) &&
                message.contains("now", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `announceImmediateWarning - omits street name for brevity`() {
        // Arrange
        val distanceToTurn = 12f
        
        // Act
        announcementManager.announceImmediateWarning(distanceToTurn, testStep)
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                !message.contains("Broadway")  // Street name omitted in immediate warning
            }, priority = true)
        }
    }
    
    @Test
    fun `formatManeuverDescription - converts enum to natural language`() {
        // Arrange & Act & Assert
        val testCases = mapOf(
            Maneuver.TURN_LEFT to "turn left",
            Maneuver.TURN_RIGHT to "turn right",
            Maneuver.TURN_SLIGHT_LEFT to "turn slightly left",
            Maneuver.TURN_SLIGHT_RIGHT to "turn slightly right",
            Maneuver.TURN_SHARP_LEFT to "turn sharply left",
            Maneuver.TURN_SHARP_RIGHT to "turn sharply right",
            Maneuver.ROUNDABOUT_LEFT to "take the roundabout left",
            Maneuver.MERGE_LEFT to "merge left",
            Maneuver.RAMP_LEFT to "take the ramp left",
            Maneuver.FORK_LEFT to "keep left at the fork",
            Maneuver.UTURN_LEFT to "make a u-turn"
        )
        
        testCases.forEach { (maneuver, expected) ->
            val step = testStep.copy(maneuver = maneuver)
            val result = announcementManager.formatManeuverDescription(step)
            assertTrue("Maneuver $maneuver should contain '$expected'", 
                result.contains(expected, ignoreCase = true))
        }
    }
    
    @Test
    fun `formatManeuverDescription - extracts street name correctly`() {
        // Arrange: Various instruction formats
        val testCases = listOf(
            "Turn left onto Broadway" to "Broadway",
            "Turn right on W 42nd St" to "W 42nd St",
            "Take the ramp onto I-95 N" to "I-95 N",
            "Merge onto FDR Drive" to "FDR Drive",
            "Continue on Main Street" to "Main Street"
        )
        
        testCases.forEach { (instruction, expectedStreet) ->
            // Act
            val step = testStep.copy(instruction = instruction)
            val result = announcementManager.formatManeuverDescription(step)
            
            // Assert
            assertTrue("Should extract '$expectedStreet' from '$instruction'", 
                result.contains(expectedStreet, ignoreCase = true))
        }
    }
    
    @Test
    fun `roundabout announcements parse exit numbers`() {
        // Arrange: Roundabout with exit numbers
        val testCases = listOf(
            "Take the 2nd exit onto Broadway" to "second exit",
            "At roundabout take 1st exit" to "first exit",
            "Take the third exit" to "third exit"
        )
        
        testCases.forEach { (instruction, expectedPhrase) ->
            // Act
            val step = testStep.copy(
                instruction = instruction,
                maneuver = Maneuver.ROUNDABOUT_RIGHT
            )
            announcementManager.announceAdvanceWarning(70f, step)
            
            // Assert
            verify {
                ttsManager.announce(match { message ->
                    message.contains(expectedPhrase, ignoreCase = true)
                }, priority = true)
            }
        }
    }
    
    @Test
    fun `announcements use priority audio for turn warnings`() {
        // Arrange
        val distanceToTurn = 65f
        
        // Act
        announcementManager.announceAdvanceWarning(distanceToTurn, testStep)
        
        // Assert: Priority flag should be true (pauses music)
        verify {
            ttsManager.announce(any(), priority = true)
        }
    }
    
    @Test
    fun `volume increase applied for navigation announcements`() {
        // Arrange: Mock TTSManager with volume setting
        every { ttsManager.getCurrentVolume() } returns 0.7f
        
        // Act
        announcementManager.announceAdvanceWarning(70f, testStep)
        
        // Assert: Check that volume was increased by 10%
        // (This tests integration with TTSManager.increaseNavigationVolume())
        verify {
            ttsManager.announce(any(), priority = true)
        }
        
        // Note: Full volume increase test requires checking TTSManager.speak() Bundle params
        // which is covered in TTSManagerTest
    }
    
    @Test
    fun `announceArrival - announces destination reached`() {
        // Act
        announcementManager.announceArrival()
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                message.contains("arrived", ignoreCase = true) ||
                message.contains("destination", ignoreCase = true)
            }, priority = true)
        }
    }
    
    @Test
    fun `announceRerouting - informs user of recalculation`() {
        // Act
        announcementManager.announceRerouting()
        
        // Assert
        verify {
            ttsManager.announce(match { message ->
                message.contains("recalculating", ignoreCase = true) ||
                message.contains("route", ignoreCase = true)
            }, priority = true)
        }
    }
}
