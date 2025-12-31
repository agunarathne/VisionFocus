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
 * Unit tests for BackCommand - Story 3.5 Task 13
 * 
 * Tests back stack navigation including:
 * - Back stack navigation from different screens
 * - "Already at home screen" handling (empty back stack)
 * - TTS announcement of "Going back"
 * - Error handling for non-MainActivity context
 * 
 * @since Story 3.5
 */
class BackCommandTest {
    
    private lateinit var backCommand: BackCommand
    private lateinit var mockTTSManager: TTSManager
    private lateinit var mockMainActivity: MainActivity
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        mockTTSManager = mockk(relaxed = true)
        mockMainActivity = mockk(relaxed = true)
        mockContext = mockk()
        
        // Mock string resources
        every { mockMainActivity.getString(R.string.going_back_announcement) } returns "Going back"
        every { mockMainActivity.getString(R.string.already_at_home_announcement) } returns "Already at home screen"
        
        // Mock TTS readiness
        every { mockTTSManager.isReady() } returns true
        coEvery { mockTTSManager.announce(any()) } returns Result.success(50L)
        
        // Mock runOnUiThread to execute immediately in tests
        every { mockMainActivity.runOnUiThread(any()) } answers {
            val block = firstArg<Runnable>()
            block.run()
        }
        
        backCommand = BackCommand(mockTTSManager)
    }
    
    @Test
    fun `back command has correct display name`() {
        assertEquals("Back", backCommand.displayName)
    }
    
    @Test
    fun `back command includes all keyword variations`() {
        val keywords = backCommand.keywords
        assertTrue(keywords.contains("back"))
        assertTrue(keywords.contains("go back"))
        assertTrue(keywords.contains("previous"))
        assertEquals(3, keywords.size) // Verify no unexpected keywords
    }
    
    @Test
    fun `back command navigates back from MainActivity`() = runTest {
        val result = backCommand.execute(mockMainActivity)
        
        // Verify success result
        assertTrue("Expected Success result", result is CommandResult.Success)
        assertEquals("Navigated back", (result as CommandResult.Success).message)
        
        // Verify navigateBack was called
        verify { mockMainActivity.navigateBack(mockTTSManager) }
    }
    
    @Test
    fun `back command announces confirmation`() = runTest {
        backCommand.execute(mockMainActivity)
        
        // Verify TTS announcement called via navigateBack
        verify { mockMainActivity.navigateBack(mockTTSManager) }
    }
    
    @Test
    fun `back command returns failure for non-MainActivity context`() = runTest {
        val result = backCommand.execute(mockContext)
        
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
    fun `back command handles navigation errors gracefully`() = runTest {
        // Simulate navigation error
        every { mockMainActivity.runOnUiThread(any()) } throws RuntimeException("Navigation failed")
        
        val result = backCommand.execute(mockMainActivity)
        
        // Verify failure result
        assertTrue("Expected Failure result", result is CommandResult.Failure)
        assertTrue(
            "Expected error message",
            (result as CommandResult.Failure).error.contains("Back error")
        )
        
        // Verify error announcement
        coVerify { mockTTSManager.announce("Navigation error") }
    }
    
    @Test
    fun `back command executes on UI thread`() = runTest {
        var executedOnUiThread = false
        every { mockMainActivity.runOnUiThread(any()) } answers {
            executedOnUiThread = true
            val block = firstArg<Runnable>()
            block.run()
        }
        
        backCommand.execute(mockMainActivity)
        
        // Verify runOnUiThread was called
        assertTrue("Expected navigation on UI thread", executedOnUiThread)
    }
    
    @Test
    fun `back command returns success with correct message`() = runTest {
        val result = backCommand.execute(mockMainActivity)
        
        assertTrue(result is CommandResult.Success)
        val successResult = result as CommandResult.Success
        assertEquals("Navigated back", successResult.message)
    }
    
    @Test
    fun `back command verifies keywords match story requirements`() {
        // Story 3.5: Verify keywords from NavigationCommands.kt
        val expectedKeywords = setOf(
            "back",
            "go back",
            "previous"
        )
        
        assertEquals(expectedKeywords, backCommand.keywords.toSet())
    }
}
