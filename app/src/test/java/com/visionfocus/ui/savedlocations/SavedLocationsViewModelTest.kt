package com.visionfocus.ui.savedlocations

import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.navigation.manager.NavigationManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SavedLocationsViewModel.
 * Story 7.2 Task 12.1: ViewModel tests for empty/loaded/error states
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedLocationsViewModelTest {
    
    private lateinit var repository: SavedLocationRepository
    private lateinit var navigationManager: NavigationManager
    private lateinit var viewModel: SavedLocationsViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        navigationManager = mockk(relaxed = true)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Test: ViewModel emits Empty state when repository returns empty list.
     * Story 7.2 AC: Empty state UI shown when no locations exist
     */
    @Test
    fun `loadLocations emits Empty state when no locations exist`() = runTest {
        // Given repository with no locations
        coEvery { repository.getAllLocationsSorted() } returns flowOf(emptyList())
        
        // When ViewModel initialized
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // Then Empty state emitted
        val state = viewModel.uiState.value
        assertTrue("Expected Empty state", state is SavedLocationsUiState.Empty)
    }
    
    /**
     * Test: ViewModel emits Success state with locations sorted by lastUsedAt DESC.
     * Story 7.2 AC1: Locations sorted by most recently used
     */
    @Test
    fun `loadLocations emits Success state with sorted locations`() = runTest {
        // Given repository with 3 locations (different lastUsedAt)
        val locations = listOf(
            SavedLocationEntity(1, "Home", 0.0, 0.0, 100, 300),    // Most recent
            SavedLocationEntity(2, "Work", 0.0, 0.0, 200, 200),
            SavedLocationEntity(3, "Gym", 0.0, 0.0, 50, 100)       // Oldest
        )
        coEvery { repository.getAllLocationsSorted() } returns flowOf(locations)
        
        // When ViewModel initialized
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // Then Success state with locations sorted by lastUsedAt DESC
        val state = viewModel.uiState.value as SavedLocationsUiState.Success
        assertEquals(3, state.locations.size)
        assertEquals("Home", state.locations[0].name)  // lastUsedAt: 300
        assertEquals("Work", state.locations[1].name)  // lastUsedAt: 200
        assertEquals("Gym", state.locations[2].name)   // lastUsedAt: 100
    }
    
    /**
     * Test: Update location name validates minimum length.
     * Story 7.2 AC7: Validation for edit dialog
     */
    @Test
    fun `updateLocationName rejects name shorter than 2 characters`() = runTest {
        // Given repository with one location
        val location = SavedLocationEntity(1, "Home", 0.0, 0.0, 100, 100)
        coEvery { repository.getAllLocationsSorted() } returns flowOf(listOf(location))
        
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // When updating with short name
        viewModel.updateLocationName(1, "H")
        advanceUntilIdle()
        
        // Then no repository update called (validation failed)
        coVerify(exactly = 0) { repository.updateLocation(any()) }
    }
    
    /**
     * Test: Update location name prevents duplicate names (case-insensitive).
     * Story 7.2 AC7: Duplicate name check
     */
    @Test
    fun `updateLocationName prevents duplicate names`() = runTest {
        // Given two locations exist
        val locations = listOf(
            SavedLocationEntity(1, "Home", 0.0, 0.0, 100, 100),
            SavedLocationEntity(2, "Work", 0.0, 0.0, 200, 200)
        )
        coEvery { repository.getAllLocationsSorted() } returns flowOf(locations)
        
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // When renaming location 2 to "Home" (duplicate)
        viewModel.updateLocationName(2, "Home")
        advanceUntilIdle()
        
        // Then no repository update called (duplicate check failed)
        coVerify(exactly = 0) { repository.updateLocation(any()) }
    }
    
    /**
     * Test: Update location name succeeds with valid input.
     * Story 7.2 AC7: Successful location name update
     */
    @Test
    fun `updateLocationName succeeds with valid name`() = runTest {
        // Given repository with one location
        val location = SavedLocationEntity(1, "Home", 0.0, 0.0, 100, 100)
        coEvery { repository.getAllLocationsSorted() } returns flowOf(listOf(location))
        coEvery { repository.updateLocation(any()) } returns Result.success(Unit)
        
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // When updating with valid name
        viewModel.updateLocationName(1, "Sweet Home")
        advanceUntilIdle()
        
        // Then repository update called with new name
        coVerify { repository.updateLocation(match { it.name == "Sweet Home" }) }
    }
    
    /**
     * Test: Delete location calls repository delete method.
     * Story 7.2 AC8: Delete location operation
     */
    @Test
    fun `deleteLocation calls repository delete`() = runTest {
        // Given repository with locations
        val location = SavedLocationEntity(1, "Home", 0.0, 0.0, 100, 100)
        coEvery { repository.getAllLocationsSorted() } returns flowOf(listOf(location))
        coEvery { repository.deleteLocation(any()) } returns Result.success(Unit)
        
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // When deleting location
        val uiModel = SavedLocationUiModel(1, "Home", 0.0, 0.0, null, 100, 100)
        viewModel.deleteLocation(uiModel)
        advanceUntilIdle()
        
        // Then repository delete called
        coVerify { repository.deleteLocation(any()) }
    }
    
    /**
     * Test: Navigate to location starts navigation and updates lastUsedAt.
     * Story 7.2 AC6: Navigation integration
     */
    @Test
    fun `navigateToLocation starts navigation and updates timestamp`() = runTest {
        // Given repository with location
        val location = SavedLocationEntity(1, "Home", 40.7128, -74.0060, 100, 100)
        coEvery { repository.getAllLocationsSorted() } returns flowOf(listOf(location))
        coEvery { repository.updateLocation(any()) } returns Result.success(Unit)
        coEvery { navigationManager.startNavigation(any(), any(), any()) } returns Result.success(Unit)
        
        viewModel = SavedLocationsViewModel(repository, navigationManager)
        advanceUntilIdle()
        
        // When navigating to location
        val uiModel = SavedLocationUiModel(1, "Home", 40.7128, -74.0060, null, 100, 100)
        viewModel.navigateToLocation(uiModel)
        advanceUntilIdle()
        
        // Then navigation manager called
        coVerify { 
            navigationManager.startNavigation(40.7128, -74.0060, "Home")
        }
        
        // And lastUsedAt updated
        coVerify { repository.updateLocation(any()) }
    }
}
