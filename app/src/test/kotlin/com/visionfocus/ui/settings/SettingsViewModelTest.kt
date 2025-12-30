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
}

