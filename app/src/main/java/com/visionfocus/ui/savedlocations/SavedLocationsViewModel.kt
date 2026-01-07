package com.visionfocus.ui.savedlocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.navigation.repository.NavigationRepository
import com.visionfocus.navigation.models.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for SavedLocationsFragment.
 * 
 * Story 7.2: State management with MVVM pattern
 * 
 * Responsibilities:
 * - Load saved locations from repository (sorted by most recently used)
 * - Handle CRUD operations: Navigate, Edit, Delete
 * - Emit UI state changes (Loading, Success, Empty, Error)
 * - Emit one-time events for TalkBack announcements
 * 
 * @param repository SavedLocationRepository for data operations
 * @param navigationRepository NavigationRepository for calculating routes to saved locations
 */
@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    private val repository: SavedLocationRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SavedLocationsUiState>(SavedLocationsUiState.Loading)
    val uiState: StateFlow<SavedLocationsUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<SavedLocationsEvent>()
    val events: SharedFlow<SavedLocationsEvent> = _events.asSharedFlow()
    
    init {
        loadLocations()
    }
    
    /**
     * Load locations sorted by most recently used.
     * Story 7.2 AC1: RecyclerView displays locations ordered by lastUsedAt DESC
     */
    fun loadLocations() {
        viewModelScope.launch {
            _uiState.value = SavedLocationsUiState.Loading
            
            repository.getAllLocationsSorted()
                .catch { error ->
                    Timber.e(error, "Failed to load locations")
                    _uiState.value = SavedLocationsUiState.Error(
                        error.message ?: "Failed to load saved locations"
                    )
                }
                .collect { entities ->
                    if (entities.isEmpty()) {
                        _uiState.value = SavedLocationsUiState.Empty
                        Timber.d("No saved locations found")
                    } else {
                        val uiModels = entities.map { it.toUiModel() }
                        _uiState.value = SavedLocationsUiState.Success(uiModels)
                        Timber.d("Loaded ${entities.size} saved locations")
                    }
                }
        }
    }
    
    /**
     * Navigate to selected location.
     * Story 7.2 AC6: Calculate route and start turn-by-turn guidance
     */
    fun navigateToLocation(location: SavedLocationUiModel) {
        viewModelScope.launch {
            try {
                // Update lastUsedAt timestamp
                val updated = location.toEntity().copy(lastUsedAt = System.currentTimeMillis())
                repository.updateLocation(updated)
                    .onFailure { error ->
                        Timber.w(error, "Failed to update lastUsedAt for ${location.name}")
                    }
                
                // Announce navigation start
                _events.emit(SavedLocationsEvent.NavigationStarted(location.name))
                
                // Calculate route using NavigationRepository (same as DestinationInputViewModel)
                val destination = Destination(
                    query = location.name,
                    name = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    formattedAddress = location.name
                )
                
                val routeResult = navigationRepository.getRoute(destination)
                
                if (routeResult.isSuccess) {
                    val route = routeResult.getOrThrow()
                    Timber.d("Route calculated: ${route.steps.size} steps, ${route.totalDistance}m")
                    
                    // Emit navigation ready event with route
                    _events.emit(SavedLocationsEvent.NavigationReady(route, location.name))
                } else {
                    val error = routeResult.exceptionOrNull()
                    Timber.e(error, "Failed to calculate route to ${location.name}")
                    _events.emit(SavedLocationsEvent.Error("Failed to calculate route: ${error?.message}"))
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to start navigation")
                _events.emit(SavedLocationsEvent.Error("Failed to start navigation: ${e.message}"))
            }
        }
    }
    
    /**
     * Update location name.
     * Story 7.2 AC7: Edit location with validation and duplicate check
     */
    fun updateLocationName(locationId: Long, newName: String) {
        viewModelScope.launch {
            try {
                // Validate name length
                val trimmedName = newName.trim()
                if (trimmedName.isBlank() || trimmedName.length < 2) {
                    _events.emit(SavedLocationsEvent.Error("Location name must be at least 2 characters"))
                    return@launch
                }
                
                // Check for duplicate names (case-insensitive)
                val currentState = _uiState.value
                if (currentState is SavedLocationsUiState.Success) {
                    val duplicate = currentState.locations.any { 
                        it.name.equals(trimmedName, ignoreCase = true) && it.id != locationId 
                    }
                    if (duplicate) {
                        _events.emit(SavedLocationsEvent.Error("A location named '$trimmedName' already exists"))
                        return@launch
                    }
                }
                
                // Find and update location
                val entity = findEntityById(locationId)
                if (entity == null) {
                    _events.emit(SavedLocationsEvent.Error("Location not found"))
                    return@launch
                }
                
                val updated = entity.copy(name = trimmedName)
                repository.updateLocation(updated)
                    .onSuccess {
                        _events.emit(SavedLocationsEvent.LocationUpdated(trimmedName))
                        Timber.d("Updated location name to: $trimmedName")
                    }
                    .onFailure { error ->
                        _events.emit(SavedLocationsEvent.Error(error.message ?: "Failed to update location"))
                        Timber.e(error, "Failed to update location name")
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error updating location name")
                _events.emit(SavedLocationsEvent.Error("Failed to update location: ${e.message}"))
            }
        }
    }
    
    /**
     * Delete location.
     * Story 7.2 AC8: Delete with confirmation
     */
    fun deleteLocation(location: SavedLocationUiModel) {
        viewModelScope.launch {
            try {
                repository.deleteLocation(location.toEntity())
                    .onSuccess {
                        _events.emit(SavedLocationsEvent.LocationDeleted(location.name))
                        Timber.d("Deleted location: ${location.name}")
                    }
                    .onFailure { error ->
                        _events.emit(SavedLocationsEvent.Error(error.message ?: "Failed to delete location"))
                        Timber.e(error, "Failed to delete location")
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting location")
                _events.emit(SavedLocationsEvent.Error("Failed to delete location: ${e.message}"))
            }
        }
    }
    
    /**
     * Find entity by ID from repository.
     * Helper method for update operations.
     * 
     * Code Review Fix: Query repository directly instead of relying on potentially
     * stale _uiState.value to avoid race conditions during loading states.
     */
    private suspend fun findEntityById(locationId: Long): SavedLocationEntity? {
        return try {
            repository.getAllLocationsSorted().first().find { it.id == locationId }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find location by ID: $locationId")
            null
        }
    }
    
    // Mapper extensions
    
    private fun SavedLocationEntity.toUiModel() = SavedLocationUiModel(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        address = address,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
    
    private fun SavedLocationUiModel.toEntity() = SavedLocationEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        address = address
    )
}
