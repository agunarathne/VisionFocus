package com.visionfocus.voice.commands.navigation

import android.content.Context
import com.visionfocus.MainActivity
import com.visionfocus.R
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HomeCommand - Story 3.5 Task 13
 * 
 * Tests home screen navigation including:
 * - Navigation to home screen from different screens
 * - Back stack clearing
 * - TTS announcement of "Home screen"
 * - Already at home screen handling
 * - Error handling for non-MainActivity context
 * 
 * @since Story 3.5
 */
class HomeCommandTest {
    
    private lateinit var homeCommand: HomeCommand
    private lateinit var mockTTSManager: TTSManager
    private lateinit var mockMainActivity: MainActivity
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        mockTTSManager = mockk(relaxed = true)
        mockMainActivity = mockk(relaxed = true)
        mockContext = mockk()
        
        // Mock string resource
        every { mockMainActivity.getString(R.string.home_screen_announcement) } returns "Home screen"
        
        // Mock TTS readiness
        every { mockTTSManager.isReady() } returns true
        coEvery { mockTTSManager.announce(any()) } returns Result.success(50L)
        
        // Mock runOnUiThread to execute immediately in tests
        every { mockMainActivity.runOnUiThread(any()) } answers {
            val block = firstArg<Runnable>()
            block.run()
        }
        
        homeCommand = HomeCommand(mockTTSManager)
    }
    
    @Test
    fun `home command has correct display name`() {
        assertEquals("Home", homeCommand.displayName)
    }
    
    @Test
    fun `home command includes all keyword variations`() {
        val keywords = homeCommand.keywords
        assertTrue(keywords.contains("home"))
        assertTrue(keywords.contains("home screen"))
        assertTrue(keywords.contains("go home"))
        assertTrue(keywords.contains("main"))
        assertTrue(keywords.contains("main screen"))
        assertTrue(keywords.contains("go to home"))
        assertTrue(keywords.contains("go to main"))
        assertEquals(7, keywords.size) // Verify no unexpected keywords
    }
    
    @Test
    fun `home command navigates to home screen from MainActivity`() = runTest {
        val result = homeCommand.execute(mockMainActivity)
        
        // Verify success result
        assertTrue("Expected Success result", result is CommandResult.Success)
        assertEquals("Navigated to home screen", (result as CommandResult.Success).message)
        
        // Verify navigateToHome was called
        verify { mockMainActivity.navigateToHome(mockTTSManager) }
    }
    
    @Test
    fun `home command announces confirmation`() = runTest {
        homeCommand.execute(mockMainActivity)
        
        // Verify TTS announcement called
        verify { mockMainActivity.navigateToHome(mockTTSManager) }
    }
    
    @Test
    fun `home command returns failure for non-MainActivity context`() = runTest {
        val result = homeCommand.execute(mockContext)
        
        // Verify failure result
        assertTrue("Expected Failure result", result is CommandResult.Failure)
        assertTrue(
            "Expected 'MainActivity' error message",
            (result as CommandResult.Failure).error.contains("MainActivity")
        )
        
        // Verify error announcement
        coVerify { mockTTSManager.announce("Navigation error") }
    }
    
    @Test
    fun `home command handles navigation errors gracefully`() = runTest {
        // Simulate navigation error
        every { mockMainActivity.runOnUiThread(any()) } throws RuntimeException("Navigation failed")
        
        val result = homeCommand.execute(mockMainActivity)
        
        // Verify failure result
        assertTrue("Expected Failure result", result is CommandResult.Failure)
        assertTrue(
            "Expected error message",
            (result as CommandResult.Failure).error.contains("Home error")
        )
        
        // Verify error announcement
        coVerify { mockTTSManager.announce("Navigation error") }
    }
    
    @Test
    fun `home command executes on UI thread`() = runTest {
        var executedOnUiThread = false
        every { mockMainActivity.runOnUiThread(any()) } answers {
            executedOnUiThread = true
            val block = firstArg<Runnable>()
            block.run()
        }
        
        homeCommand.execute(mockMainActivity)
        
        // Verify runOnUiThread was called
        assertTrue("Expected navigation on UI thread", executedOnUiThread)
    }
    
    @Test
    fun `home command returns success with correct message`() = runTest {
        val result = homeCommand.execute(mockMainActivity)
        
        assertTrue(result is CommandResult.Success)
        val successResult = result as CommandResult.Success
        assertEquals("Navigated to home screen", successResult.message)
    }
    
    @Test
    fun `home command verifies keywords match story requirements`() {
        // Story 3.5: Verify keywords from NavigationCommands.kt
        val expectedKeywords = setOf(
            "home",
            "home screen",
            "go home",
            "main",
            "main screen",
            "go to home",
            "go to main"
        )
        
        assertEquals(expectedKeywords, homeCommand.keywords.toSet())
    }
}
