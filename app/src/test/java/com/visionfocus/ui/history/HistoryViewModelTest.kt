package com.visionfocus.ui.history

import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import com.visionfocus.data.repository.RecognitionHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for HistoryViewModel.
 * 
 * Tests repository logic with mocked DAO to verify:
 * - Initial state is Loading
 * - Successful history load emits Success state
 * - Empty history load emits Empty state
 * - Clear history calls repository and updates state
 * 
 * Story 4.3 Task 11: Create unit tests for HistoryViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    
    @Mock
    private lateinit var mockRepository: RecognitionHistoryRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Test initial state is Loading.
     * Story 4.3 Task 11.2: Test initial state is Loading
     */
    @Test
    fun `initial state is Loading`() {
        // Given: Repository returns empty flow (will never emit)
        `when`(mockRepository.getRecentHistory()).thenReturn(flowOf())
        
        // When: ViewModel is created
        val viewModel = HistoryViewModel(mockRepository)
        
        // Then: Initial state should be Loading
        assertTrue(viewModel.uiState.value is HistoryUiState.Loading)
    }
    
    /**
     * Test successful history load emits Success state with data.
     * Story 4.3 Task 11.3: Test successful history load emits Success state
     */
    @Test
    fun `successful history load emits Success state with items`() = runTest {
        // Given: Repository returns list of history items
        val mockItems = listOf(
            RecognitionHistoryEntity(
                id = 1,
                category = "Chair",
                confidence = 0.9f,
                timestamp = 1234567890L,
                verbosityMode = "standard",
                detailText = "Chair, high confidence"
            ),
            RecognitionHistoryEntity(
                id = 2,
                category = "Table",
                confidence = 0.85f,
                timestamp = 1234567900L,
                verbosityMode = "brief",
                detailText = "Table"
            )
        )
        
        `when`(mockRepository.getRecentHistory()).thenReturn(flowOf(mockItems))
        
        // When: ViewModel is created and coroutines complete
        val viewModel = HistoryViewModel(mockRepository)
        advanceUntilIdle()
        
        // Then: State should be Success with 2 items
        val state = viewModel.uiState.value
        assertTrue(state is HistoryUiState.Success)
        assertEquals(2, (state as HistoryUiState.Success).items.size)
        assertEquals("Chair", state.items[0].category)
        assertEquals("Table", state.items[1].category)
    }
    
    /**
     * Test empty history load emits Empty state.
     * Story 4.3 Task 11.4: Test empty history load emits Empty state
     */
    @Test
    fun `empty history load emits Empty state`() = runTest {
        // Given: Repository returns empty list
        `when`(mockRepository.getRecentHistory()).thenReturn(flowOf(emptyList()))
        
        // When: ViewModel is created and coroutines complete
        val viewModel = HistoryViewModel(mockRepository)
        advanceUntilIdle()
        
        // Then: State should be Empty
        val state = viewModel.uiState.value
        assertTrue(state is HistoryUiState.Empty)
    }
    
    /**
     * Test clearAllHistory calls repository and updates state to Empty.
     * Story 4.3 Task 11.5: Test clearAllHistory() calls repository
     */
    @Test
    fun `clearAllHistory calls repository and updates state to Empty`() = runTest {
        // Given: Repository returns successful clear operation
        `when`(mockRepository.getRecentHistory()).thenReturn(flowOf(emptyList()))
        `when`(mockRepository.clearAllHistory()).thenReturn(Unit)
        
        // When: ViewModel calls clearAllHistory()
        val viewModel = HistoryViewModel(mockRepository)
        advanceUntilIdle()
        
        viewModel.clearAllHistory()
        advanceUntilIdle()
        
        // Then: Repository should be called and state should be Empty
        verify(mockRepository).clearAllHistory()
        assertTrue(viewModel.uiState.value is HistoryUiState.Empty)
    }
    
    /**
     * Test error handling when repository throws exception.
     * Story 4.3 Task 11 (bonus): Test error handling
     */
    @Test
    fun `repository error emits Error state`() = runTest {
        // Given: Repository throws exception
        val errorMessage = "Database error"
        `when`(mockRepository.getRecentHistory()).thenReturn(
            kotlinx.coroutines.flow.flow { throw Exception(errorMessage) }
        )
        
        // When: ViewModel is created and coroutines complete
        val viewModel = HistoryViewModel(mockRepository)
        advanceUntilIdle()
        
        // Then: State should be Error
        val state = viewModel.uiState.value
        assertTrue(state is HistoryUiState.Error)
        assertTrue((state as HistoryUiState.Error).message.contains("Database error"))
    }
    
    /**
     * Test clearAllHistory error handling.
     * Story 4.3 Task 11 (bonus): Test clear history error handling
     */
    @Test
    fun `clearAllHistory error emits Error state`() = runTest {
        // Given: Repository returns empty list initially, then throws on clear
        `when`(mockRepository.getRecentHistory()).thenReturn(flowOf(emptyList()))
        `when`(mockRepository.clearAllHistory()).thenThrow(RuntimeException("Clear failed"))
        
        // When: ViewModel calls clearAllHistory()
        val viewModel = HistoryViewModel(mockRepository)
        advanceUntilIdle()
        
        viewModel.clearAllHistory()
        advanceUntilIdle()
        
        // Then: State should be Error
        val state = viewModel.uiState.value
        assertTrue(state is HistoryUiState.Error)
        assertTrue((state as HistoryUiState.Error).message.contains("Failed to clear history"))
    }
}
