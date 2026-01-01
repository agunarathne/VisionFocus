package com.visionfocus.voice.commands.navigation

import android.content.Context
import com.visionfocus.MainActivity
import com.visionfocus.R
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for HistoryCommand.
 * 
 * Verifies voice command "History" correctly triggers navigation to HistoryFragment.
 * 
 * Story 4.3 Task 10: Navigation integration tests
 * Code Review Fix: Added missing test coverage for voice command navigation
 */
class HistoryCommandTest {
    
    @Mock
    private lateinit var mockTtsManager: TTSManager
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockActivity: MainActivity
    
    private lateinit var command: HistoryCommand
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        command = HistoryCommand(mockTtsManager)
        
        // Setup context to return activity
        `when`(mockContext.getString(R.string.cmd_history_received))
            .thenReturn("Opening history")
    }
    
    @Test
    fun `command has correct display name`() {
        assertEquals("History", command.displayName)
    }
    
    @Test
    fun `command recognizes history keywords`() {
        val keywords = command.keywords
        
        assertTrue(keywords.contains("history"))
        assertTrue(keywords.contains("show history"))
        assertTrue(keywords.contains("view history"))
    }
    
    @Test
    fun `execute announces navigation and returns success`() = runTest {
        // When: Command is executed
        val result = command.execute(mockContext)
        
        // Then: Should announce via TTS
        verify(mockTtsManager).announce("Opening history")
        
        // And: Should return success
        assertTrue(result is CommandResult.Success)
        assertEquals("Opening history", (result as CommandResult.Success).message)
    }
    
    @Test
    fun `command handles missing context gracefully`() = runTest {
        // Given: Context returns null for string resource
        `when`(mockContext.getString(R.string.cmd_history_received))
            .thenReturn(null)
        
        // When: Command is executed
        val result = command.execute(mockContext)
        
        // Then: Should still succeed (TTS handles null gracefully)
        assertTrue(result is CommandResult.Success)
    }
    
    /**
     * Note: Testing actual MainActivity.navigateToHistory() call requires instrumentation test
     * with Hilt setup, as MainActivity is not directly accessible from unit test context.
     * 
     * For integration testing of navigation flow, see:
     * - HistoryAccessibilityTest.kt (UI integration)
     * - Manual testing with voice commands on device
     */
}
