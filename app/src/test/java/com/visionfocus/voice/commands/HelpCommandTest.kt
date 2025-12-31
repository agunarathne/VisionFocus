package com.visionfocus.voice.commands

import android.content.Context
import com.visionfocus.R
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.commands.utility.HelpCommand
import com.visionfocus.voice.processor.CommandResult
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for HelpCommand - Story 3.4 Task 9
 * 
 * Tests comprehensive help system with:
 * - Grouped command announcements (Recognition, Navigation, Settings, General)
 * - Speech rate preference integration (0.5×-2.0×)
 * - Help announcement interruptibility
 * - Command keyword variations
 * - String resource usage for internationalization
 */
class HelpCommandTest {
    
    private lateinit var helpCommand: HelpCommand
    private lateinit var mockContext: Context
    private lateinit var mockTTSManager: TTSManager
    private lateinit var mockSettingsRepository: SettingsRepository
    
    private val testHelpIntroduction = "Available voice commands. Commands are organized into four groups."
    private val testRecognitionGroup = "For recognizing objects, say: Recognize, What do I see, Repeat."
    private val testNavigationGroup = "For navigation, say: Navigate, Where am I, Cancel."
    private val testSettingsGroup = "For adjusting settings, say: Settings, High contrast on or off, Increase or Decrease speed."
    private val testGeneralGroup = "For general actions, say: History, Save location, Back, Home."
    private val testConclusion = "Say a command now, or tap the microphone button to exit voice mode."
    
    @Before
    fun setup() {
        mockContext = mock()
        mockTTSManager = mock()
        mockSettingsRepository = mock()
        
        // Setup string resource mocks
        whenever(mockContext.getString(R.string.help_command_introduction)).thenReturn(testHelpIntroduction)
        whenever(mockContext.getString(R.string.help_command_recognition_group)).thenReturn(testRecognitionGroup)
        whenever(mockContext.getString(R.string.help_command_navigation_group)).thenReturn(testNavigationGroup)
        whenever(mockContext.getString(R.string.help_command_settings_group)).thenReturn(testSettingsGroup)
        whenever(mockContext.getString(R.string.help_command_general_group)).thenReturn(testGeneralGroup)
        whenever(mockContext.getString(R.string.help_command_conclusion)).thenReturn(testConclusion)
        
        // Default speech rate 1.0×
        whenever(mockSettingsRepository.getSpeechRate()).thenReturn(flowOf(1.0f))
        
        // Mock TTS announce success
        whenever(mockTTSManager.announce(any())).thenReturn(Result.success(50L))
        
        helpCommand = HelpCommand(mockTTSManager, mockSettingsRepository)
    }
    
    /**
     * Task 9.1: Test help announcement generation includes all 15 commands
     * AC #1: All commands must be announced
     */
    @Test
    fun `help announcement includes all 15 core commands`() = runTest {
        // Execute help command
        val result = helpCommand.execute(mockContext)
        
        // Verify success
        assertTrue(result is CommandResult.Success)
        
        // Capture announcement text
        val captor = argumentCaptor<String>()
        verify(mockTTSManager).announce(captor.capture())
        
        val announcement = captor.firstValue
        
        // Verify all 15 command keywords are present in announcement
        // Recognition group (3)
        assertTrue("Missing 'Recognize'", announcement.contains("Recognize", ignoreCase = true))
        assertTrue("Missing 'What do I see'", announcement.contains("What do I see", ignoreCase = true))
        assertTrue("Missing 'Repeat'", announcement.contains("Repeat", ignoreCase = true))
        
        // Navigation group (3)
        assertTrue("Missing 'Navigate'", announcement.contains("Navigate", ignoreCase = true))
        assertTrue("Missing 'Where am I'", announcement.contains("Where am I", ignoreCase = true))
        assertTrue("Missing 'Cancel'", announcement.contains("Cancel", ignoreCase = true))
        
        // Settings group (5)
        assertTrue("Missing 'Settings'", announcement.contains("Settings", ignoreCase = true))
        assertTrue("Missing 'High contrast'", announcement.contains("High contrast", ignoreCase = true))
        assertTrue("Missing 'Increase speed'", announcement.contains("Increase", ignoreCase = true))
        assertTrue("Missing 'Decrease speed'", announcement.contains("Decrease", ignoreCase = true))
        
        // General group (4)
        assertTrue("Missing 'History'", announcement.contains("History", ignoreCase = true))
        assertTrue("Missing 'Save location'", announcement.contains("Save location", ignoreCase = true))
        assertTrue("Missing 'Back'", announcement.contains("Back", ignoreCase = true))
        assertTrue("Missing 'Home'", announcement.contains("Home", ignoreCase = true))
    }
    
    /**
     * Task 9.2: Test commands are grouped correctly
     * AC #1: Commands organized in logical groups
     */
    @Test
    fun `help announcement contains all four command groups`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        assertTrue(result is CommandResult.Success)
        
        // Verify all group strings were loaded from resources
        verify(mockContext).getString(R.string.help_command_introduction)
        verify(mockContext).getString(R.string.help_command_recognition_group)
        verify(mockContext).getString(R.string.help_command_navigation_group)
        verify(mockContext).getString(R.string.help_command_settings_group)
        verify(mockContext).getString(R.string.help_command_general_group)
        verify(mockContext).getString(R.string.help_command_conclusion)
    }
    
    /**
     * Task 9.2: Test command grouping structure
     */
    @Test
    fun `help announcement groups commands logically`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        val captor = argumentCaptor<String>()
        verify(mockTTSManager).announce(captor.capture())
        
        val announcement = captor.firstValue
        
        // Verify logical grouping phrases present
        assertTrue(announcement.contains("For recognizing objects"))
        assertTrue(announcement.contains("For navigation"))
        assertTrue(announcement.contains("For adjusting settings"))
        assertTrue(announcement.contains("For general actions"))
    }
    
    /**
     * Task 9.3: Test help announcement respects speech rate preference at 0.5×
     * AC #2: Speech rate preference integration
     */
    @Test
    fun `help respects speech rate preference 0_5x`() = runTest {
        // Mock speech rate 0.5×
        whenever(mockSettingsRepository.getSpeechRate()).thenReturn(flowOf(0.5f))
        
        helpCommand.execute(mockContext)
        
        // Verify speech rate set to 0.5×
        verify(mockTTSManager).setSpeechRate(0.5f)
        
        // Verify announcement made after rate set
        verify(mockTTSManager).announce(any())
    }
    
    /**
     * Task 9.3: Test help announcement respects speech rate preference at 1.0×
     */
    @Test
    fun `help respects speech rate preference 1_0x default`() = runTest {
        // Mock speech rate 1.0× (default)
        whenever(mockSettingsRepository.getSpeechRate()).thenReturn(flowOf(1.0f))
        
        helpCommand.execute(mockContext)
        
        // Verify speech rate set to 1.0×
        verify(mockTTSManager).setSpeechRate(1.0f)
        verify(mockTTSManager).announce(any())
    }
    
    /**
     * Task 9.3: Test help announcement respects speech rate preference at 2.0×
     */
    @Test
    fun `help respects speech rate preference 2_0x fast`() = runTest {
        // Mock speech rate 2.0×
        whenever(mockSettingsRepository.getSpeechRate()).thenReturn(flowOf(2.0f))
        
        helpCommand.execute(mockContext)
        
        // Verify speech rate set to 2.0×
        verify(mockTTSManager).setSpeechRate(2.0f)
        verify(mockTTSManager).announce(any())
    }
    
    /**
     * Task 9.4: Test help announcement concludes with prompt
     * AC #4: Concluding prompt to guide next action
     */
    @Test
    fun `help announcement concludes with prompt`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        val captor = argumentCaptor<String>()
        verify(mockTTSManager).announce(captor.capture())
        
        val announcement = captor.firstValue
        
        // Verify concluding prompt present
        assertTrue(
            "Missing concluding prompt",
            announcement.contains("Say a command now, or tap the microphone button to exit voice mode")
        )
    }
    
    /**
     * Task 9.5: Test help command can be interrupted by speaking another command
     * AC #3: Help interruptible via TTSManager.stop()
     * 
     * Note: Interruption happens externally via TTSManager.stop() when user speaks
     * This test verifies help uses announce() which is interruptible
     */
    @Test
    fun `help announcement uses interruptible TTS announce method`() = runTest {
        helpCommand.execute(mockContext)
        
        // Verify announce() called (not a blocking method)
        verify(mockTTSManager).announce(any())
        
        // TTSManager.stop() can interrupt announce() - verified by integration tests
        // This unit test confirms help uses the correct TTS method
    }
    
    /**
     * Task 9.7: Mock SettingsRepository for speech rate retrieval
     */
    @Test
    fun `help retrieves speech rate from SettingsRepository`() = runTest {
        helpCommand.execute(mockContext)
        
        // Verify speech rate retrieved from repository
        verify(mockSettingsRepository).getSpeechRate()
    }
    
    /**
     * Task 9.8: Verify help announcement strings loaded from resources
     * AC: All strings externalized for internationalization
     */
    @Test
    fun `help loads all announcement strings from resources`() = runTest {
        helpCommand.execute(mockContext)
        
        // Verify all 6 string resources loaded
        verify(mockContext).getString(R.string.help_command_introduction)
        verify(mockContext).getString(R.string.help_command_recognition_group)
        verify(mockContext).getString(R.string.help_command_navigation_group)
        verify(mockContext).getString(R.string.help_command_settings_group)
        verify(mockContext).getString(R.string.help_command_general_group)
        verify(mockContext).getString(R.string.help_command_conclusion)
    }
    
    /**
     * Task 8.5: Test help command has comprehensive keyword variations
     * AC: Help accessible via multiple phrases
     */
    @Test
    fun `help command supports all keyword variations`() {
        val keywords = helpCommand.keywords
        
        // Verify all expected variations present
        assertTrue(keywords.contains("help"))
        assertTrue(keywords.contains("commands"))
        assertTrue(keywords.contains("what can i say"))
        assertTrue(keywords.contains("how do i use this"))
        assertTrue(keywords.contains("command list"))
        assertTrue(keywords.contains("available commands"))
        
        // Verify minimum keyword count
        assertTrue("Should have at least 6 keyword variations", keywords.size >= 6)
    }
    
    /**
     * Test help command display name
     */
    @Test
    fun `help command has correct display name`() {
        assertEquals("Help", helpCommand.displayName)
    }
    
    /**
     * Test help command error handling
     */
    @Test
    fun `help handles TTS announcement failure gracefully`() = runTest {
        // Mock TTS announce failure
        whenever(mockTTSManager.announce(any())).thenThrow(RuntimeException("TTS error"))
        
        val result = helpCommand.execute(mockContext)
        
        // Verify failure result
        assertTrue(result is CommandResult.Failure)
        
        // Verify error announcement attempted
        verify(mockTTSManager).announce("Help system error. Please try again.")
    }
    
    /**
     * Test help command success result message
     */
    @Test
    fun `help command returns success with descriptive message`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        assertTrue(result is CommandResult.Success)
        val successMessage = (result as CommandResult.Success).message
        assertTrue(
            "Success message should mention command groups",
            successMessage.contains("command groups", ignoreCase = true)
        )
    }
    
    /**
     * Test help announcement order (introduction first, conclusion last)
     */
    @Test
    fun `help announcement follows correct order`() = runTest {
        helpCommand.execute(mockContext)
        
        val captor = argumentCaptor<String>()
        verify(mockTTSManager).announce(captor.capture())
        
        val announcement = captor.firstValue
        
        // Verify introduction comes before groups
        val introIndex = announcement.indexOf("organized into four groups")
        val recognitionIndex = announcement.indexOf("For recognizing objects")
        
        assertTrue("Introduction should come before command groups", introIndex < recognitionIndex)
        
        // Verify conclusion comes last
        val conclusionIndex = announcement.indexOf("Say a command now")
        assertTrue("Conclusion should come after command groups", conclusionIndex > recognitionIndex)
    }
}
