package com.visionfocus.voice.commands.navigation

import android.content.Context
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.navigation.manager.NavigationManager
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NavigateToCommand.
 * 
 * Story 7.3 Task 11: Test voice command "navigate to [location]" with fuzzy matching
 * 
 * Tests cover:
 * - Exact name match
 * - Case-insensitive matching
 * - Fuzzy matching with Levenshtein distance ≤2
 * - No match scenarios
 * - Multiple fuzzy matches (disambiguation)
 * - lastUsedAt timestamp updates
 * - Navigation failure handling
 */
class NavigateToCommandTest {
    
    private lateinit var mockRepository: SavedLocationRepository
    private lateinit var mockNavigationManager: NavigationManager
    private lateinit var mockTtsManager: TTSManager
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: android.content.SharedPreferences
    
    private lateinit var command: NavigateToCommand
    
    private val homeLocation = SavedLocationEntity(
        id = 1,
        name = "Home",
        latitude = 40.7128,
        longitude = -74.0060,
        address = "123 Main St, New York, NY",
        createdAt = System.currentTimeMillis() - 10000,
        lastUsedAt = System.currentTimeMillis() - 5000
    )
    
    private val workLocation = SavedLocationEntity(
        id = 2,
        name = "Work",
        latitude = 40.7580,
        longitude = -73.9855,
        address = "456 Office Ave, New York, NY",
        createdAt = System.currentTimeMillis() - 8000,
        lastUsedAt = System.currentTimeMillis() - 3000
    )
    
    private val gymLocation = SavedLocationEntity(
        id = 3,
        name = "Gym",
        latitude = 40.7489,
        longitude = -73.9680,
        address = "789 Fitness Blvd, New York, NY",
        createdAt = System.currentTimeMillis() - 6000,
        lastUsedAt = System.currentTimeMillis() - 1000
    )
    
    @Before
    fun setup() {
        mockRepository = mockk()
        mockNavigationManager = mockk()
        mockTtsManager = mockk()
        mockContext = mockk()
        mockSharedPreferences = mockk()
        
        // Default TTS behavior
        every { mockTtsManager.announce(any()) } just Runs
        
        // Mock SharedPreferences chain for transcription storage
        every { mockContext.getSharedPreferences("voice", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to home"
        
        // Mock getString for error messages
        every { mockContext.getString(any()) } returns "Mock string"
        every { mockContext.getString(any(), any()) } returns "Mock string with param"
        
        command = NavigateToCommand(
            repository = mockRepository,
            navigationManager = mockNavigationManager,
            ttsManager = mockTtsManager
        )
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    /**
     * Test exact name match starts navigation.
     * AC: 4, 5 - Voice command with exact location name
     */
    @Test
    fun `execute with exact match navigates to location`() = runTest {
        // Given saved locations
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to Home"
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation, workLocation, gymLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing command with exact match
        val result = command.execute(mockContext)
        
        // Then navigation starts to Home location
        assertTrue(result is CommandResult.Success)
        assertEquals("Starting navigation to Home", (result as CommandResult.Success).message)
        
        // Verify navigation called with correct coordinates
        coVerify {
            mockNavigationManager.startNavigation(
                latitude = homeLocation.latitude,
                longitude = homeLocation.longitude,
                destinationName = homeLocation.name
            )
        }
        
        // Verify TTS announces navigation
        verify { mockTtsManager.announce("Starting navigation to Home") }
        
        // Verify timestamp updated
        coVerify { mockRepository.updateLocation(match { it.id == homeLocation.id && it.lastUsedAt > homeLocation.lastUsedAt }) }
    }
    
    /**
     * Test case-insensitive matching.
     * AC: 5 - Fuzzy matching includes case-insensitivity
     */
    @Test
    fun `execute with different case matches location`() = runTest {
        // Given saved locations
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to HOME"
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation, workLocation, gymLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing with uppercase input
        val result = command.execute(mockContext)
        
        // Then matches "Home" location
        assertTrue(result is CommandResult.Success)
        
        // Verify navigation to Home
        coVerify {
            mockNavigationManager.startNavigation(
                latitude = homeLocation.latitude,
                longitude = homeLocation.longitude,
                destinationName = homeLocation.name
            )
        }
    }
    
    /**
     * Test fuzzy match with typo (Levenshtein distance = 1).
     * AC: 5, 6 - Fuzzy matching with distance ≤2
     */
    @Test
    fun `execute with fuzzy match distance 1 navigates to location`() = runTest {
        // Given saved locations
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to hme"
        coEvery { mockRepository.findLocationByName("hme") } returns null
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation, workLocation, gymLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing with typo (distance: 1)
        val result = command.execute(mockContext)
        
        // Then matches "Home" with fuzzy matching
        assertTrue(result is CommandResult.Success)
        
        // Verify navigation to Home
        coVerify {
            mockNavigationManager.startNavigation(
                latitude = homeLocation.latitude,
                longitude = homeLocation.longitude,
                destinationName = homeLocation.name
            )
        }
    }
    
    /**
     * Test fuzzy match with distance = 2 still matches.
     * AC: 5 - Levenshtein distance ≤2
     */
    @Test
    fun `execute with fuzzy match distance 2 navigates to location`() = runTest {
        // Given saved locations
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to wrk"
        coEvery { mockRepository.findLocationByName("wrk") } returns null
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(workLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing with 2-char typo: "wrk" → "Work" (distance: 2 - missing 'o' and changed position)
        val result = command.execute(mockContext)
        
        // Then matches "Work"
        assertTrue(result is CommandResult.Success)
        
        // Verify navigation to Work
        coVerify {
            mockNavigationManager.startNavigation(
                latitude = workLocation.latitude,
                longitude = workLocation.longitude,
                destinationName = workLocation.name
            )
        }
    }
    
    /**
     * Test no match returns failure.
     * AC: 5, 6 - No location found message
     */
    @Test
    fun `execute with no match returns failure`() = runTest {
        // Given saved locations
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to xyz"
        coEvery { mockRepository.findLocationByName("xyz") } returns null
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation, workLocation, gymLocation))
        
        // When executing with non-existent location
        val result = command.execute(mockContext)
        
        // Then returns failure
        assertTrue(result is CommandResult.Failure)
        assertEquals("No saved location found matching: xyz", (result as CommandResult.Failure).errorMessage)
        
        // Verify TTS announces error
        verify { mockTtsManager.announce("No saved location found matching: xyz") }
        
        // Verify navigation not called
        coVerify(exactly = 0) { mockNavigationManager.startNavigation(any(), any(), any()) }
    }
    
    /**
     * Test multiple fuzzy matches triggers disambiguation.
     * AC: 6 - Multiple matches show disambiguation dialog
     * 
     * Note: Dialog behavior tested separately, here we just verify correct matches returned
     */
    @Test
    fun `execute with multiple fuzzy matches returns success with message`() = runTest {
        // Given two similar locations
        val homeDepot = SavedLocationEntity(
            id = 4,
            name = "Home Depot",
            latitude = 40.7200,
            longitude = -74.0100,
            address = "321 Shopping Rd, New York, NY",
            createdAt = System.currentTimeMillis() - 4000,
            lastUsedAt = System.currentTimeMillis() - 2000
        )
        
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to hom"
        coEvery { mockRepository.findLocationByName("hom") } returns null
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation, homeDepot))
        
        // When executing fuzzy match that matches both
        // "hom" matches both "Home" (distance: 1) and "Home Depot" (distance: 1 for first word)
        val result = command.execute(mockContext)
        
        // Then returns success (disambiguation handled internally)
        assertTrue(result is CommandResult.Success)
        
        // Verify TTS announces multiple matches
        verify { mockTtsManager.announce(match { it.contains("Multiple locations found") }) }
    }
    
    /**
     * Test lastUsedAt timestamp updated before navigation.
     * AC: Story 7.2 implicit - timestamp for sorting
     */
    @Test
    fun `execute updates lastUsedAt timestamp before navigation`() = runTest {
        // Given saved location with old timestamp
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to Home"
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing command
        val startTime = System.currentTimeMillis()
        command.execute(mockContext)
        val endTime = System.currentTimeMillis()
        
        // Then timestamp updated
        coVerify {
            mockRepository.updateLocation(
                match {
                    it.id == homeLocation.id &&
                    it.lastUsedAt >= startTime &&
                    it.lastUsedAt <= endTime
                }
            )
        }
        
        // Verify update happens before navigation
        coVerifyOrder {
            mockRepository.updateLocation(any())
            mockNavigationManager.startNavigation(any(), any(), any())
        }
    }
    
    /**
     * Test navigation failure handled gracefully.
     * AC: 8 - Handle navigation errors
     */
    @Test
    fun `execute handles navigation failure gracefully`() = runTest {
        // Given saved location but navigation fails
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to Home"
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.failure(Exception("GPS unavailable"))
        
        // When executing command
        val result = command.execute(mockContext)
        
        // Then returns failure
        assertTrue(result is CommandResult.Failure)
        assertTrue((result as CommandResult.Failure).errorMessage.contains("navigation failed"))
        
        // Verify TTS announces error
        verify { mockTtsManager.announce(match { it.contains("navigation failed") }) }
    }
    
    /**
     * Test command keywords removed correctly.
     * AC: 4 - Support multiple command variations
     */
    @Test
    fun `execute handles different command keywords`() = runTest {
        // Given saved locations
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation))
        coEvery { mockRepository.updateLocation(any()) } just Runs
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // Test all keyword variations
        val commands = listOf(
            "navigate to Home",
            "go to Home",
            "take me to Home",
            "directions to Home"
        )
        
        commands.forEach { cmd ->
            // Mock transcription for this iteration
            every { mockSharedPreferences.getString("last_transcription", any()) } returns cmd
            
            // When executing with different keywords
            val result = command.execute(mockContext)
            
            // Then all match "Home"
            assertTrue("Command '$cmd' should succeed", result is CommandResult.Success)
        }
        
        // Verify navigation called for each command
        coVerify(exactly = commands.size) {
            mockNavigationManager.startNavigation(
                latitude = homeLocation.latitude,
                longitude = homeLocation.longitude,
                destinationName = homeLocation.name
            )
        }
    }
    
    /**
     * Test empty location name after keyword removal.
     * AC: 4 - Handle malformed input
     */
    @Test
    fun `execute with command keyword only returns failure`() = runTest {
        // When executing with keyword only (no location name)
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to"
        coEvery { mockRepository.findLocationByName("") } returns null
        
        val result = command.execute(mockContext)
        
        // Then returns failure
        assertTrue(result is CommandResult.Failure)
        
        // Verify TTS announces error
        verify { mockTtsManager.announce(match { it.contains("location name") || it.contains("No saved location") }) }
    }
    
    /**
     * Test timestamp update failure doesn't block navigation.
     * AC: Task 9.5 - Timestamp failure shouldn't prevent navigation
     */
    @Test
    fun `execute continues navigation even if timestamp update fails`() = runTest {
        // Given saved location but timestamp update fails
        every { mockSharedPreferences.getString("last_transcription", any()) } returns "navigate to Home"
        coEvery { mockRepository.findLocationByName("home") } returns homeLocation
        coEvery { mockRepository.getAllLocationsSorted() } returns flowOf(listOf(homeLocation))
        coEvery { mockRepository.updateLocation(any()) } throws Exception("Database write error")
        coEvery { mockNavigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        // When executing command
        val result = command.execute(mockContext)
        
        // Then navigation still succeeds
        assertTrue(result is CommandResult.Success)
        
        // Verify navigation was called despite timestamp failure
        coVerify {
            mockNavigationManager.startNavigation(
                latitude = homeLocation.latitude,
                longitude = homeLocation.longitude,
                destinationName = homeLocation.name
            )
        }
    }
}
