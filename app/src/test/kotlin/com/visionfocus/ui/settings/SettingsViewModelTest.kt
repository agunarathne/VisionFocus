package com.visionfocus.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.visionfocus.data.repository.SettingsRepository
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
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var mockRepository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel
    
    private val highContrastFlow = MutableStateFlow(false)
    private val largeTextFlow = MutableStateFlow(false)
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockRepository = mock()
        
        // Setup mock repository flows
        whenever(mockRepository.getHighContrastMode()).thenReturn(highContrastFlow)
        whenever(mockRepository.getLargeTextMode()).thenReturn(largeTextFlow)
        
        viewModel = SettingsViewModel(mockRepository)
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
    fun `toggleHighContrastMode calls repository with negated value when false`() = runTest(testDispatcher) {
        // Arrange: Repository returns false
        highContrastFlow.value = false
        
        // Act
        viewModel.toggleHighContrastMode()
        advanceUntilIdle()
        
        // Assert: Should call repository with true (negated value)
        verify(mockRepository).setHighContrastMode(true)
    }
    
    @Test
    fun `toggleLargeTextMode calls repository with negated value when false`() = runTest(testDispatcher) {
        // Arrange: Repository returns false
        largeTextFlow.value = false
        
        // Act
        viewModel.toggleLargeTextMode()
        advanceUntilIdle()
        
        // Assert: Should call repository with true (negated value)
        verify(mockRepository).setLargeTextMode(true)
    }
    
    @Test
    fun `toggleHighContrastMode calls repository with false when current value is true`() = runTest(testDispatcher) {
        // Arrange: Repository returns true, wait for ViewModel to initialize StateFlow
        highContrastFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify StateFlow initialized with true
        assertEquals(true, viewModel.highContrastMode.value)
        
        // Act
        viewModel.toggleHighContrastMode()
        advanceUntilIdle()
        
        // Assert: Should call repository with false (negated value)
        verify(mockRepository).setHighContrastMode(false)
    }
    
    @Test
    fun `toggleLargeTextMode calls repository with false when current value is true`() = runTest(testDispatcher) {
        // Arrange: Repository returns true, wait for ViewModel to initialize StateFlow
        largeTextFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify StateFlow initialized with true
        assertEquals(true, viewModel.largeTextMode.value)
        
        // Act
        viewModel.toggleLargeTextMode()
        advanceUntilIdle()
        
        // Assert: Should call repository , wait for initialization
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
    fun `multiple rapid toggles handled correctly without race conditions`() = runTest(testDispatcher) {
        // Arrange: Start with false
        highContrastFlow.value = false
        
        // Act: Toggle multiple times rapidly
        viewModel.toggleHighContrastMode()
        viewModel.toggleHighContrastMode()
        viewModel.toggleHighContrastMode()
        advanceUntilIdle()
        
        // Assert: All three calls made (no race condition blocking)
        verify(mockRepository, org.mockito.kotlin.times(3)).setHighContrastMode(org.mockito.kotlin.any())
    }
}

