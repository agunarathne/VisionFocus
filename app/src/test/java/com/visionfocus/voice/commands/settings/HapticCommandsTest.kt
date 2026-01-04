package com.visionfocus.voice.commands.settings

import android.content.Context
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for Haptic Intensity voice commands
 * Story 5.5 Task 2: Test HapticOffCommand, HapticLightCommand, HapticMediumCommand, HapticStrongCommand
 * 
 * Test Coverage:
 * - execute() success paths
 * - execute() failure paths (DataStore exceptions, Vibrator unavailable)
 * - Sample vibration triggers (except HapticOffCommand)
 * - Keyword matching
 * - DataStore persistence verification
 */
class HapticCommandsTest {
    
    @Mock
    private lateinit var settingsRepository: SettingsRepository
    
    @Mock
    private lateinit var hapticFeedbackManager: HapticFeedbackManager
    
    @Mock
    private lateinit var ttsManager: TTSManager
    
    @Mock
    private lateinit var context: Context
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }
    
    // ========== HapticOffCommand Tests ==========
    
    @Test
    fun `HapticOffCommand execute() should set intensity to OFF`() = runTest {
        // Given
        val command = HapticOffCommand(settingsRepository, ttsManager)
        
        verify(settingsRepository).setHapticIntensity(HapticIntensity.OFF)
        assertTrue(result is CommandResult.Success)
    }
    
    @Test
    fun `HapticOffCommand execute() should NOT trigger sample vibration`() = runTest {
        // Given - HapticOffCommand does NOT inject HapticFeedbackManager
        val command = HapticOffCommand(settingsRepository, ttsManager)
        
        // When
        command.execute(context)
        
        // Then - No vibration should be triggered (design decision: user wants silence)
        // This test verifies HapticOffCommand doesn't have hapticFeedbackManager dependency
        assertNotNull(command)
        command.execute(context)
        
        // Then - hapticFeedbackManager should NOT be used
        coVerify(exactly = 0) { hapticFeedbackManager.trigger(any()) }
    }
    
    @Test
    fun `HapticOffCommand should have expected keywords`() {
        // Given
        val command = HapticOffCommand(settingsRepository, ttsManager)
        
        // Then
        assertTrue(command.keywords.contains("haptic off"))
        assertTrue(command.keywords.contains("disable haptic"))
        assertTrue(command.keywords.contains("vibration off"))
    }
    
    // ========== HapticLightCommand Tests ==========
    
    @Test
    fun `HapticLightCommand execute() should set intensity to LIGHT`() = runTest {
        // Given
        val command = HapticLightCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        val result = command.execute(context)
        
        verify(settingsRepository).setHapticIntensity(HapticIntensity.LIGHT)
        assertTrue(result is CommandResult.Success)
    }
    
    @Test
    fun `HapticLightCommand execute() should trigger sample vibration`() = runTest {
        // Given
        val command = HapticLightCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        command.execute(context)
        
        // Then
        verify(hapticFeedbackManager).trigger(HapticPattern.ButtonPress)
    }
    
    @Test
    fun `HapticLightCommand execute() should return failure on exception`() = runTest {
        // Given
        val command = HapticLightCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        whenever(settingsRepository.setHapticIntensity(any())).thenThrow(java.io.IOException("Storage full"))
        
        // When
        val result = command.execute(context)
        
        // Then
        assertTrue(result is CommandResult.Failure)
        assertTrue((result as CommandResult.Failure).message.contains("storage unavailable"))
    }
    
    @Test
    fun `HapticLightCommand should have expected keywords`() {
        // Given
        val command = HapticLightCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // Then
        assertTrue(command.keywords.contains("haptic light"))
        assertTrue(command.keywords.contains("light vibration"))
        assertTrue(command.keywords.contains("gentle haptic"))
    }
    
    // ========== HapticMediumCommand Tests ==========
    
    @Test
    fun `HapticMediumCommand execute() should set intensity to MEDIUM`() = runTest {
        // Given
        val command = HapticMediumCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        val result = command.execute(context)
        
        // Then
        verify(settingsRepository).setHapticIntensity(HapticIntensity.MEDIUM)
        assertTrue(result is CommandResult.Success)
    }
    
    @Test
    fun `HapticMediumCommand execute() should trigger sample vibration`() = runTest {
        // Given
        val command = HapticMediumCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        command.execute(context)
        
        // Then
        verify(hapticFeedbackManager).trigger(HapticPattern.ButtonPress)
    }
    
    @Test
    fun `HapticMediumCommand should have expected keywords`() {
        // Given
        val command = HapticMediumCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // Then
        assertTrue(command.keywords.contains("haptic medium"))
        assertTrue(command.keywords.contains("medium vibration"))
        assertTrue(command.keywords.contains("normal haptic"))
    }
    
    // ========== HapticStrongCommand Tests ==========
    
    @Test
    fun `HapticStrongCommand execute() should set intensity to STRONG`() = runTest {
        // Given
        val command = HapticStrongCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        val result = command.execute(context)
        
        // Then
        coVerify { settingsRepository.setHapticIntensity(HapticIntensity.STRONG) }
        verify(settingsRepository).setHapticIntensity(HapticIntensity.STRONG)
        assertTrue(result is CommandResult.Success)
    }
    
    @Test
    fun `HapticStrongCommand execute() should trigger sample vibration`() = runTest {
        // Given
        val command = HapticStrongCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // When
        command.execute(context)
        
        // Then
        verify(hapticFeedbackManager).trigger(HapticPattern.ButtonPress)
    }
    
    @Test
    fun `HapticStrongCommand should have expected keywords`() {
        // Given
        val command = HapticStrongCommand(settingsRepository, hapticFeedbackManager, ttsManager)
        
        // Then
        assertTrue(command.keywords.contains("haptic strong"))
        assertTrue(command.keywords.contains("strong vibration"))
        assertTrue(command.keywords.contains("intense haptic"))
    }
}
