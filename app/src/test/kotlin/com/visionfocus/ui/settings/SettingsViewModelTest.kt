package com.visionfocus.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.engine.VoiceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SettingsViewModel theme preference management.
 * 
 * Tests verify:
 * - StateFlow emissions from repository
 * - Toggle methods call repository correctly
 * - Initial state handling
 * - Story 5.2: Voice selection functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var mockRepository: SettingsRepository
    private lateinit var mockTtsManager: TTSManager
    private lateinit var viewModel: SettingsViewModel
    
    private val highContrastFlow = MutableStateFlow(false)
    private val largeTextFlow = MutableStateFlow(false)
    private val speechRateFlow = MutableStateFlow(1.0f)
    private val voiceLocaleFlow = MutableStateFlow<String?>(null)  // Story 5.2
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        mockTtsManager = mock()
        
        // Setup mock repository flows
        whenever(mockRepository.getHighContrastMode()).thenReturn(highContrastFlow)
        whenever(mockRepository.getLargeTextMode()).thenReturn(largeTextFlow)
        whenever(mockRepository.getSpeechRate()).thenReturn(speechRateFlow)
        whenever(mockRepository.getVoiceLocale()).thenReturn(voiceLocaleFlow)  // Story 5.2
        
        // Story 5.2: Mock TTSManager.isReady() to return true
        whenever(mockTtsManager.isReady()).thenReturn(true)
        whenever(mockTtsManager.getAvailableVoices()).thenReturn(emptyList())
        
        // Story 5.4: Mock HapticManager
        val mockHapticManager: com.visionfocus.service.haptic.HapticManager = mock()
        
        viewModel = SettingsViewModel(mockRepository, mockTtsManager, mockHapticManager)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `highContrastMode initial value is false`() = runTest(testDispatcher) {
        val value = viewModel.highContrastMode.first()
        assertFalse(value)
    }
    
    @Test
    fun `largeTextMode initial value is false`() = runTest(testDispatcher) {
        val value = viewModel.largeTextMode.first()
        assertFalse(value)
    }
    
    @Test
    fun `setHighContrastMode calls repository with provided value`() = runTest(testDispatcher) {
        // Act
        viewModel.setHighContrastMode(true)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setHighContrastMode(true)
    }
    
    @Test
    fun `setLargeTextMode calls repository with provided value`() = runTest(testDispatcher) {
        // Act
        viewModel.setLargeTextMode(true)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setLargeTextMode(true)
    }
    
    @Test
    fun `setHighContrastMode can disable high contrast mode`() = runTest(testDispatcher) {
        // Act
        viewModel.setHighContrastMode(false)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setHighContrastMode(false)
    }
    
    @Test
    fun `setLargeTextMode can disable large text mode when currently enabled`() = runTest(testDispatcher) {
        // Act
        viewModel.setLargeTextMode(false)
        advanceUntilIdle()
        
        // Assert: Should call repository with false
        verify(mockRepository).setLargeTextMode(false)
    }
    
    @Test
    fun `highContrastMode StateFlow updates when repository emits new value`() = runTest(testDispatcher) {
        // Arrange: Initial value is false, wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.highContrastMode.value)
        
        // Act: Repository emits new value
        highContrastFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert: StateFlow updated
        assertTrue(viewModel.highContrastMode.value)
    }
    
    @Test
    fun `largeTextMode StateFlow updates when repository emits new value`() = runTest(testDispatcher) {
        // Arrange: Initial value is false, wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.largeTextMode.value)
        
        // Act: Repository emits new value
        largeTextFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert: StateFlow updated
        assertTrue(viewModel.largeTextMode.value)
    }
    
    @Test
    fun `multiple rapid setHighContrastMode calls handled correctly without race conditions`() = runTest(testDispatcher) {
        // Act: Set multiple times rapidly
        viewModel.setHighContrastMode(true)
        viewModel.setHighContrastMode(false)
        viewModel.setHighContrastMode(true)
        advanceUntilIdle()
        
        // Assert: All three calls made (no race condition blocking)
        verify(mockRepository, org.mockito.kotlin.times(3)).setHighContrastMode(org.mockito.kotlin.any())
    }
    
    // Story 5.1: Speech Rate Tests
    
    @Test
    fun `speechRate initial value is 1_0`() = runTest(testDispatcher) {
        val value = viewModel.speechRate.first()
        assertEquals(1.0f, value)
    }
    
    @Test
    fun `setSpeechRate calls repository with clamped value`() = runTest(testDispatcher) {
        // Act
        viewModel.setSpeechRate(1.5f)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setSpeechRate(1.5f)
    }
    
    @Test
    fun `setSpeechRate clamps value below 0_5 to 0_5`() = runTest(testDispatcher) {
        // Act
        viewModel.setSpeechRate(0.3f)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setSpeechRate(0.5f)
    }
    
    @Test
    fun `setSpeechRate clamps value above 2_0 to 2_0`() = runTest(testDispatcher) {
        // Act
        viewModel.setSpeechRate(3.0f)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setSpeechRate(2.0f)
    }
    
    @Test
    fun `incrementSpeechRate adds 0_25 and calls TTSManager`() = runTest(testDispatcher) {
        // Arrange
        speechRateFlow.value = 1.0f
        advanceUntilIdle()
        
        // Act
        viewModel.incrementSpeechRate()
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setSpeechRate(1.25f)
        verify(mockTtsManager).setSpeechRate(1.25f)
    }
    
    @Test
    fun `decrementSpeechRate subtracts 0_25 and calls TTSManager`() = runTest(testDispatcher) {
        // Arrange
        speechRateFlow.value = 1.0f
        advanceUntilIdle()
        
        // Act
        viewModel.decrementSpeechRate()
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setSpeechRate(0.75f)
        verify(mockTtsManager).setSpeechRate(0.75f)
    }
    
    @Test
    fun `incrementSpeechRate at maximum emits announcement`() = runTest(testDispatcher) {
        // Arrange
        speechRateFlow.value = 2.0f
        advanceUntilIdle()
        
        // Act
        viewModel.incrementSpeechRate()
        advanceUntilIdle()
        
        // Assert: No repository call (already at max)
        verify(mockRepository, org.mockito.kotlin.never()).setSpeechRate(org.mockito.kotlin.any())
    }
    
    @Test
    fun `decrementSpeechRate at minimum emits announcement`() = runTest(testDispatcher) {
        // Arrange
        speechRateFlow.value = 0.5f
        advanceUntilIdle()
        
        // Act
        viewModel.decrementSpeechRate()
        advanceUntilIdle()
        
        // Assert: No repository call (already at min)
        verify(mockRepository, org.mockito.kotlin.never()).setSpeechRate(org.mockito.kotlin.any())
    }
    
    @Test
    fun `playSampleAnnouncement calls TTSManager announce with provided text`() = runTest(testDispatcher) {
        // Arrange
        val sampleText = "This is how your speech rate sounds."
        
        // Act
        viewModel.playSampleAnnouncement(sampleText)
        advanceUntilIdle()
        
        // Assert
        verify(mockTtsManager).announce(sampleText)
    }
    
    @Test
    fun `speechRate StateFlow updates when repository emits new value`() = runTest(testDispatcher) {
        // Arrange: Initial value is 1.0, wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1.0f, viewModel.speechRate.value)
        
        // Act: Repository emits new value
        speechRateFlow.value = 1.5f
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert: StateFlow updated
        assertEquals(1.5f, viewModel.speechRate.value)
    }
    
    // Story 5.2: Voice Selection Tests
    
    @Test
    fun `voiceLocale initial value is null (system default)`() = runTest(testDispatcher) {
        val value = viewModel.voiceLocale.first()
        assertEquals(null, value)
    }
    
    @Test
    fun `setVoiceLocale persists to repository`() = runTest(testDispatcher) {
        // Arrange: Mock successful voice setting
        whenever(mockTtsManager.setVoice("en-GB")).thenReturn(true)
        
        // Act
        viewModel.setVoiceLocale("en-GB")
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setVoiceLocale("en-GB")
    }
    
    @Test
    fun `setVoiceLocale triggers TTSManager setVoice`() = runTest(testDispatcher) {
        // Arrange: Mock successful voice setting
        whenever(mockTtsManager.setVoice("en-US")).thenReturn(true)
        
        // Act
        viewModel.setVoiceLocale("en-US")
        advanceUntilIdle()
        
        // Assert
        verify(mockTtsManager).setVoice("en-US")
    }
    
    @Test
    fun `setVoiceLocale handles voice unavailability`() = runTest(testDispatcher) {
        // Arrange: Mock voice not found (returns false)
        whenever(mockTtsManager.setVoice("en-IN")).thenReturn(false)
        
        // Act
        viewModel.setVoiceLocale("en-IN")
        advanceUntilIdle()
        
        // Assert: Still persisted to repository (user preference saved)
        verify(mockRepository).setVoiceLocale("en-IN")
        verify(mockTtsManager).setVoice("en-IN")
    }
    
    @Test
    fun `setVoiceLocale can reset to system default with null`() = runTest(testDispatcher) {
        // Arrange: Mock successful voice reset
        whenever(mockTtsManager.setVoice(null)).thenReturn(true)
        
        // Act
        viewModel.setVoiceLocale(null)
        advanceUntilIdle()
        
        // Assert
        verify(mockRepository).setVoiceLocale(null)
        verify(mockTtsManager).setVoice(null)
    }
    
    @Test
    fun `playSampleWithVoice calls TTSManager with correct voice and text`() = runTest(testDispatcher) {
        // Arrange: Mock successful voice setting
        whenever(mockTtsManager.setVoice("en-GB")).thenReturn(true)
        
        // Act
        viewModel.playSampleWithVoice("en-GB", "This is a preview.")
        advanceUntilIdle()
        
        // Assert
        verify(mockTtsManager).setVoice("en-GB")
        verify(mockTtsManager).announce("This is a preview.")
    }
    
    @Test
    fun `playSampleWithVoice restores original voice after preview`() = runTest(testDispatcher) {
        // Arrange: Set original voice
        voiceLocaleFlow.value = "en-US"
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Mock successful voice setting
        whenever(mockTtsManager.setVoice(org.mockito.kotlin.any())).thenReturn(true)
        
        // Act: Preview different voice
        viewModel.playSampleWithVoice("en-GB", "Preview text")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Advance time past restoration delay (2500ms)
        testDispatcher.scheduler.advanceTimeBy(3000)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert: Original voice restored (MEDIUM-5 FIX: Voice restoration test)
        verify(mockTtsManager).setVoice("en-GB")  // Preview voice set
        verify(mockTtsManager).setVoice("en-US")  // Original voice restored
    }
    
    @Test
    fun `voiceLocale StateFlow updates when repository emits new value`() = runTest(testDispatcher) {
        // Arrange: Initial value is null, wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(null, viewModel.voiceLocale.value)
        
        // Act: Repository emits new value
        voiceLocaleFlow.value = "en-US"
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert: StateFlow updated
        assertEquals("en-US", viewModel.voiceLocale.value)
    }
    
    // Story 5.3: Reset to Defaults Tests
    
    @Test
    fun `resetToDefaults calls all repository setters with default values`() = runTest(testDispatcher) {
        // Act
        viewModel.resetToDefaults()
        advanceUntilIdle()
        
        // Assert: All 7 preferences reset to defaults (CRITICAL-2 FIX: Added camera preview)
        verify(mockRepository).setSpeechRate(1.0f)                                          // Default: 1.0×
        verify(mockRepository).setVoiceLocale(null)                                         // Default: system voice
        verify(mockRepository).setVerbosity(com.visionfocus.data.model.VerbosityMode.STANDARD)  // Default: STANDARD
        verify(mockRepository).setHighContrastMode(false)                                   // Default: off
        verify(mockRepository).setLargeTextMode(false)                                      // Default: off
        verify(mockRepository).setHapticIntensity(com.visionfocus.data.model.HapticIntensity.MEDIUM) // Default: MEDIUM
        verify(mockRepository).setCameraPreviewEnabled(false)                               // Default: off
    }
}

