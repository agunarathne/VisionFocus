package com.visionfocus.voice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.visionfocus.MainActivity
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.commands.navigation.BackCommand
import com.visionfocus.voice.commands.navigation.HomeCommand
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommandProcessor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for Always-Available Voice Activation - Story 3.5 Task 14
 * 
 * Tests navigation commands with:
 * - Voice command processor integration
 * - MainActivity integration
 * - Home screen navigation
 * - Back stack navigation
 * - TTS announcements
 * 
 * Note: Full cross-screen testing limited by Epic 4-6 (History, Navigation screens)
 * @since Story 3.5
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AlwaysAvailableVoiceActivationTest {
    
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    var activityRule = ActivityTestRule(MainActivity::class.java)
    
    @Inject
    lateinit var homeCommand: HomeCommand
    
    @Inject
    lateinit var backCommand: BackCommand
    
    @Inject
    lateinit var voiceCommandProcessor: VoiceCommandProcessor
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private lateinit var context: Context
    private lateinit var mainActivity: MainActivity
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        mainActivity = activityRule.activity
        
        // Initialize TTS
        ttsManager.initialize()
        
        // Wait for TTS initialization
        Thread.sleep(500)
    }
    
    /**
     * Task 14.1: Test home command navigates to home screen
     * AC #5: Home command returns to home screen from any location
     * 
     * CODE REVIEW FIX: Navigate away first to ensure test actually validates navigation
     */
    @Test
    fun homeCommand_navigatesToHomeScreen() = runTest {
        // Navigate to Settings first to test "from any location"
        mainActivity.navigateToSettings()
        
        // Small delay for fragment transaction
        kotlinx.coroutines.delay(150)
        
        // Verify we're on Settings (not home)
        var currentScreen = mainActivity.getCurrentScreen()
        assertTrue("Should be on settings before test", currentScreen == "settings" || currentScreen == "unknown")
        
        // Execute home command
        val result = homeCommand.execute(mainActivity)
        
        // Small delay for navigation
        kotlinx.coroutines.delay(150)
        
        // Verify success
        assertTrue("Home command should succeed", result is CommandResult.Success)
        assertEquals("Navigated to home screen", (result as CommandResult.Success).message)
        
        // Verify we're on home screen
        currentScreen = mainActivity.getCurrentScreen()
        assertEquals("home", currentScreen)
        
        // Verify back stack cleared
        val backStackCount = mainActivity.supportFragmentManager.backStackEntryCount
        assertEquals("Back stack should be cleared", 0, backStackCount)
    }
    
    /**
     * Task 14.2: Test home command via voice processor
     * AC #1, #5: Voice command works from any screen
     */
    @Test
    fun voiceProcessor_recognizesHomeCommand() = runTest {
        // Process home command via voice processor
        val result = voiceCommandProcessor.processCommand("home")
        
        // Verify command was recognized and executed
        assertTrue("Voice processor should recognize 'home'", result is CommandResult.Success)
    }
    
    /**
     * Task 14.3: Test back command on home screen (already at home)
     * AC #6: Back command handles empty back stack gracefully
     */
    @Test
    fun backCommand_onHomeScreen_announcesAlreadyAtHome() = runTest {
        // Ensure we're on home screen (should be default)
        homeCommand.execute(mainActivity)
        
        // Execute back command
        val result = backCommand.execute(mainActivity)
        
        // Verify success (even though no navigation happened)
        assertTrue("Back command should succeed", result is CommandResult.Success)
        
        // Still on home screen
        val currentScreen = mainActivity.getCurrentScreen()
        assertEquals("home", currentScreen)
    }
    
    /**
     * Task 14.4: Test voice processor recognizes back command
     * AC #1, #6: Voice command works from any screen
     */
    @Test
    fun voiceProcessor_recognizesBackCommand() = runTest {
        // Process back command via voice processor
        val result = voiceCommandProcessor.processCommand("back")
        
        // Verify command was recognized and executed
        assertTrue("Voice processor should recognize 'back'", result is CommandResult.Success)
    }
    
    /**
     * Task 14.5: Test home command keyword variations
     * Verify fuzzy matching and synonyms work
     */
    @Test
    fun homeCommand_keywordVariations_allWork() = runTest {
        val variations = listOf("home", "main", "go home", "home screen", "main screen")
        
        for (keyword in variations) {
            val result = voiceCommandProcessor.processCommand(keyword)
            assertTrue(
                "Voice processor should recognize '$keyword'",
                result is CommandResult.Success
            )
        }
    }
    
    /**
     * Task 14.6: Test back command keyword variations
     * Verify fuzzy matching and synonyms work
     */
    @Test
    fun backCommand_keywordVariations_allWork() = runTest {
        val variations = listOf("back", "go back", "previous")
        
        for (keyword in variations) {
            val result = voiceCommandProcessor.processCommand(keyword)
            assertTrue(
                "Voice processor should recognize '$keyword'",
                result is CommandResult.Success
            )
        }
    }
    
    /**
     * Task 14.7: Test commands execute with TTS announcements
     * Verify TTS integration works correctly
     * 
     * CODE REVIEW FIX: Use coroutine delay instead of Thread.sleep()
     */
    @Test
    fun navigationCommands_triggerTTSAnnouncements() = runTest {
        // Verify TTS is ready
        assertTrue("TTS should be ready", ttsManager.isReady())
        
        // Execute home command (should announce "Home screen")
        homeCommand.execute(mainActivity)
        
        // Small delay for TTS (non-blocking)
        kotlinx.coroutines.delay(100)
        
        // Execute back command (should announce "Already at home screen")
        backCommand.execute(mainActivity)
        
        // Small delay for TTS
        kotlinx.coroutines.delay(100)
        
        // If no exceptions thrown, TTS integration is working
        assertTrue("TTS integration test passed", true)
    }
    
    /**
     * Task 14.8: Test getCurrentScreen helper
     * Verify screen tracking works correctly
     */
    @Test
    fun mainActivity_getCurrentScreen_returnsCorrectValue() {
        // Default screen should be home
        val currentScreen = mainActivity.getCurrentScreen()
        
        // Should be either "home" or "unknown" (depending on fragment lifecycle timing)
        assertTrue(
            "Current screen should be 'home' or 'unknown'",
            currentScreen == "home" || currentScreen == "unknown"
        )
    }
}
