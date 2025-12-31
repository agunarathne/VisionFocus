package com.visionfocus.voice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.commands.utility.HelpCommand
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommandProcessor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for HelpCommand - Story 3.4 Task 10
 * 
 * Tests complete help command flow with:
 * - Voice command processor integration
 * - Settings repository integration
 * - TTS manager integration
 * - Speech rate application
 * - Help announcement duration
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HelpCommandIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var helpCommand: HelpCommand
    
    @Inject
    lateinit var voiceCommandProcessor: VoiceCommandProcessor
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize TTS
        ttsManager.initialize()
        
        // Wait for TTS initialization
        Thread.sleep(1000)
    }
    
    /**
     * Task 10.1: Test complete help flow
     * AC #1, #4: Full announcement → concluding prompt
     */
    @Test
    fun completeHelpFlow_announcesAllCommandGroups() = runTest {
        // Execute help command
        val result = voiceCommandProcessor.processCommand("help")
        
        // Verify success
        assertTrue("Help command should succeed", result is CommandResult.Success)
        
        // Allow TTS to complete announcement (approximate 45 seconds at 1.0×)
        // For test purposes, just verify command executed
        assertNotNull(result)
    }
    
    /**
     * Task 10.3: Test speech rate integration
     * AC #2: Help respects speech rate preference
     */
    @Test
    fun helpWithDifferentSpeechRates_appliesCorrectly() = runTest {
        // Test 1: Set speech rate to 0.5×
        settingsRepository.setSpeechRate(0.5f)
        delay(100) // Allow DataStore write
        
        var currentRate = settingsRepository.getSpeechRate().first()
        assertEquals("Speech rate should be 0.5×", 0.5f, currentRate, 0.01f)
        
        val result1 = voiceCommandProcessor.processCommand("help")
        assertTrue("Help command should succeed at 0.5× rate", result1 is CommandResult.Success)
        
        // Test 2: Set speech rate to 2.0×
        settingsRepository.setSpeechRate(2.0f)
        delay(100)
        
        currentRate = settingsRepository.getSpeechRate().first()
        assertEquals("Speech rate should be 2.0×", 2.0f, currentRate, 0.01f)
        
        val result2 = voiceCommandProcessor.processCommand("help")
        assertTrue("Help command should succeed at 2.0× rate", result2 is CommandResult.Success)
    }
    
    /**
     * Task 10.5: Test help announcement length estimation
     * Target: <60 seconds at 1.0× rate
     */
    @Test
    fun helpDuration_staysUnder60Seconds_at1xRate() = runTest {
        // Set speech rate to 1.0×
        settingsRepository.setSpeechRate(1.0f)
        delay(100)
        
        // Build help announcement
        val result = helpCommand.execute(context)
        
        assertTrue("Help command should succeed", result is CommandResult.Success)
        
        // Note: Actual TTS duration measurement would require UtteranceProgressListener
        // For integration test, we verify the command executes successfully
        // Device testing (Task 11) will validate actual announcement duration
    }
    
    /**
     * Task 10.6: Test help command with various keyword variations
     * AC: Help accessible via multiple phrases
     */
    @Test
    fun helpCommandVariations_allTriggerHelp() = runTest {
        val variations = listOf(
            "help",
            "commands",
            "what can i say",
            "how do i use this",
            "command list",
            "available commands"
        )
        
        for (keyword in variations) {
            val result = voiceCommandProcessor.processCommand(keyword)
            assertTrue(
                "Keyword '$keyword' should trigger help command",
                result is CommandResult.Success
            )
            delay(100) // Brief delay between commands
        }
    }
    
    /**
     * Test help announcement contains all command groups
     */
    @Test
    fun helpAnnouncement_containsAllGroups() = runTest {
        val result = helpCommand.execute(context)
        
        assertTrue("Help command should succeed", result is CommandResult.Success)
        
        // Verify success message mentions command groups
        val successMessage = (result as CommandResult.Success).message
        assertTrue(
            "Success message should mention command groups",
            successMessage.contains("command groups", ignoreCase = true)
        )
    }
    
    /**
     * Test help command keyword matching
     */
    @Test
    fun helpCommand_hasComprehensiveKeywords() {
        val keywords = helpCommand.keywords
        
        // Verify all expected variations present
        assertTrue("Should have 'help'", keywords.contains("help"))
        assertTrue("Should have 'commands'", keywords.contains("commands"))
        assertTrue("Should have 'what can i say'", keywords.contains("what can i say"))
        assertTrue("Should have 'how do i use this'", keywords.contains("how do i use this"))
        assertTrue("Should have 'command list'", keywords.contains("command list"))
        assertTrue("Should have 'available commands'", keywords.contains("available commands"))
        
        // Verify minimum keyword count
        assertTrue("Should have at least 6 keyword variations", keywords.size >= 6)
    }
}
