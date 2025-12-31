package com.visionfocus.voice.commands

import android.content.Context
import com.visionfocus.R
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.commands.utility.HelpCommand
import com.visionfocus.voice.processor.CommandResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HelpCommand - Story 3.4 Task 9
 * 
 * Tests comprehensive help system with:
 * - Grouped command announcements (Recognition, Navigation, Settings, General)
 * - Speech rate preference integration (0.5×-2.0×)
 * - Help announcement interruptibility
 * - Command keyword variations
 * - String resource usage for internationalization
 * 
 * CRITICAL #2 FIX: Converted from Mockito to MockK for suspend function support
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
        // CRITICAL #2 FIX: Use MockK instead of Mockito for suspend functions
        mockContext = mockk()
        mockTTSManager = mockk()
        mockSettingsRepository = mockk()
        
        // Setup string resource mocks
        every { mockContext.getString(R.string.help_command_introduction) } returns testHelpIntroduction
        every { mockContext.getString(R.string.help_command_recognition_group) } returns testRecognitionGroup
        every { mockContext.getString(R.string.help_command_navigation_group) } returns testNavigationGroup
        every { mockContext.getString(R.string.help_command_settings_group) } returns testSettingsGroup
        every { mockContext.getString(R.string.help_command_general_group) } returns testGeneralGroup
        every { mockContext.getString(R.string.help_command_conclusion) } returns testConclusion
        every { mockContext.getString(R.string.help_command_error) } returns "Help system error. Please try again."
        
        // Default speech rate 1.0×
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(1.0f)
        
        // Mock TTS readiness and methods
        every { mockTTSManager.isReady() } returns true
        every { mockTTSManager.setSpeechRate(any()) } just Runs
        
        // CRITICAL #2 FIX: Use coEvery for suspend function announce()
        coEvery { mockTTSManager.announce(any()) } returns Result.success(50L)
        
        helpCommand = HelpCommand(mockTTSManager, mockSettingsRepository)
    }
    
    @Test
    fun `help announcement includes all 15 core commands`() = runTest {
        val result = helpCommand.execute(mockContext)
        assertTrue(result is CommandResult.Success)
        
        val slot = slot<String>()
        coVerify { mockTTSManager.announce(capture(slot)) }
        val announcement = slot.captured
        
        // Verify all 15 command keywords present
        assertTrue("Missing 'Recognize'", announcement.contains("Recognize", ignoreCase = true))
        assertTrue("Missing 'What do I see'", announcement.contains("What do I see", ignoreCase = true))
        assertTrue("Missing 'Repeat'", announcement.contains("Repeat", ignoreCase = true))
        assertTrue("Missing 'Navigate'", announcement.contains("Navigate", ignoreCase = true))
        assertTrue("Missing 'Where am I'", announcement.contains("Where am I", ignoreCase = true))
        assertTrue("Missing 'Cancel'", announcement.contains("Cancel", ignoreCase = true))
        assertTrue("Missing 'Settings'", announcement.contains("Settings", ignoreCase = true))
        assertTrue("Missing 'High contrast'", announcement.contains("High contrast", ignoreCase = true))
        assertTrue("Missing 'Increase speed'", announcement.contains("Increase", ignoreCase = true))
        assertTrue("Missing 'Decrease speed'", announcement.contains("Decrease", ignoreCase = true))
        assertTrue("Missing 'History'", announcement.contains("History", ignoreCase = true))
        assertTrue("Missing 'Save location'", announcement.contains("Save location", ignoreCase = true))
        assertTrue("Missing 'Back'", announcement.contains("Back", ignoreCase = true))
        assertTrue("Missing 'Home'", announcement.contains("Home", ignoreCase = true))
    }
    
    @Test
    fun `help announcement contains all four command groups`() = runTest {
        val result = helpCommand.execute(mockContext)
        assertTrue(result is CommandResult.Success)
        
        verify { mockContext.getString(R.string.help_command_introduction) }
        verify { mockContext.getString(R.string.help_command_recognition_group) }
        verify { mockContext.getString(R.string.help_command_navigation_group) }
        verify { mockContext.getString(R.string.help_command_settings_group) }
        verify { mockContext.getString(R.string.help_command_general_group) }
        verify { mockContext.getString(R.string.help_command_conclusion) }
    }
    
    @Test
    fun `help announcement groups commands logically`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        val slot = slot<String>()
        coVerify { mockTTSManager.announce(capture(slot)) }
        val announcement = slot.captured
        
        assertTrue(announcement.contains("For recognizing objects"))
        assertTrue(announcement.contains("For navigation"))
        assertTrue(announcement.contains("For adjusting settings"))
        assertTrue(announcement.contains("For general actions"))
    }
    
    @Test
    fun `help respects speech rate preference 0_5x`() = runTest {
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(0.5f)
        helpCommand.execute(mockContext)
        
        verify { mockTTSManager.setSpeechRate(0.5f) }
        coVerify { mockTTSManager.announce(any()) }
    }
    
    @Test
    fun `help respects speech rate preference 1_0x default`() = runTest {
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(1.0f)
        helpCommand.execute(mockContext)
        
        verify { mockTTSManager.setSpeechRate(1.0f) }
        coVerify { mockTTSManager.announce(any()) }
    }
    
    @Test
    fun `help respects speech rate preference 2_0x fast`() = runTest {
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(2.0f)
        helpCommand.execute(mockContext)
        
        verify { mockTTSManager.setSpeechRate(2.0f) }
        coVerify { mockTTSManager.announce(any()) }
    }
    
    @Test
    fun `help announcement concludes with prompt`() = runTest {
        val result = helpCommand.execute(mockContext)
        
        val slot = slot<String>()
        coVerify { mockTTSManager.announce(capture(slot)) }
        val announcement = slot.captured
        
        assertTrue(
            "Missing concluding prompt",
            announcement.contains("Say a command now, or tap the microphone button to exit voice mode")
        )
    }
    
    @Test
    fun `help announcement uses interruptible TTS announce method`() = runTest {
        helpCommand.execute(mockContext)
        coVerify { mockTTSManager.announce(any()) }
    }
    
    @Test
    fun `help retrieves speech rate from SettingsRepository`() = runTest {
        helpCommand.execute(mockContext)
        verify { mockSettingsRepository.getSpeechRate() }
    }
    
    @Test
    fun `help loads all announcement strings from resources`() = runTest {
        helpCommand.execute(mockContext)
        
        verify { mockContext.getString(R.string.help_command_introduction) }
        verify { mockContext.getString(R.string.help_command_recognition_group) }
        verify { mockContext.getString(R.string.help_command_navigation_group) }
        verify { mockContext.getString(R.string.help_command_settings_group) }
        verify { mockContext.getString(R.string.help_command_general_group) }
        verify { mockContext.getString(R.string.help_command_conclusion) }
    }
    
    @Test
    fun `help command supports all keyword variations`() {
        val keywords = helpCommand.keywords
        
        assertTrue(keywords.contains("help"))
        assertTrue(keywords.contains("commands"))
        assertTrue(keywords.contains("what can i say"))
        assertTrue(keywords.contains("how do i use this"))
        assertTrue(keywords.contains("command list"))
        assertTrue(keywords.contains("available commands"))
        assertTrue("Should have at least 6 keyword variations", keywords.size >= 6)
    }
    
    @Test
    fun `help command has correct display name`() {
        assertEquals("Help", helpCommand.displayName)
    }
    
    @Test
    fun `help handles TTS announcement failure gracefully`() = runTest {
        coEvery { mockTTSManager.announce(any()) } returns Result.failure(RuntimeException("TTS error"))
        
        val result = helpCommand.execute(mockContext)
        
        assertTrue(result is CommandResult.Failure)
        val failureMessage = (result as CommandResult.Failure).message
        assertTrue("Failure message should mention announcement", failureMessage.contains("announcement", ignoreCase = true))
    }
    
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
    
    @Test
    fun `help announcement follows correct order`() = runTest {
        helpCommand.execute(mockContext)
        
        val slot = slot<String>()
        coVerify { mockTTSManager.announce(capture(slot)) }
        val announcement = slot.captured
        
        val introIndex = announcement.indexOf("organized into four groups")
        val recognitionIndex = announcement.indexOf("For recognizing objects")
        assertTrue("Introduction should come before command groups", introIndex < recognitionIndex)
        
        val conclusionIndex = announcement.indexOf("Say a command now")
        assertTrue("Conclusion should come after command groups", conclusionIndex > recognitionIndex)
    }
    
    // CRITICAL #3: Test TTS readiness check
    @Test
    fun `help fails gracefully when TTS not ready`() = runTest {
        every { mockTTSManager.isReady() } returns false
        
        val result = helpCommand.execute(mockContext)
        
        assertTrue("Should fail when TTS not ready", result is CommandResult.Failure)
        val failureMessage = (result as CommandResult.Failure).message
        assertTrue("Should mention TTS not ready", failureMessage.contains("not ready", ignoreCase = true))
        
        coVerify(exactly = 0) { mockTTSManager.announce(any()) }
    }
    
    // CRITICAL #4: Test speech rate clamping
    @Test
    fun `help clamps invalid speech rate from corrupted DataStore`() = runTest {
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(5.0f)
        
        val result = helpCommand.execute(mockContext)
        
        assertTrue("Should succeed with clamped rate", result is CommandResult.Success)
        verify { mockTTSManager.setSpeechRate(2.0f) }
    }
    
    @Test
    fun `help clamps low speech rate to minimum`() = runTest {
        every { mockSettingsRepository.getSpeechRate() } returns flowOf(0.1f)
        
        helpCommand.execute(mockContext)
        
        verify { mockTTSManager.setSpeechRate(0.5f) }
    }
}
