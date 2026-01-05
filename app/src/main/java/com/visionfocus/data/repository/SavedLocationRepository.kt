package com.visionfocus.data.repository

import com.visionfocus.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for saved locations feature.
 * 
 * Story 7.1: Provides methods for saving, retrieving, updating, and deleting
 * user's saved locations with GPS coordinates.
 * 
 * All operations use Result<T> for error handling, allowing callers to handle
 * failures gracefully without catching exceptions.
 * 
 * Data storage:
 * - Locations stored in Room database encrypted with SQLCipher
 * - Sorted by most recently used for quick access (Story 7.2/7.3)
 * - Duplicate name validation prevents overwriting without confirmation
 */
interface SavedLocationRepository {
    
    /**
     * Save a new location with GPS coordinates.
     * 
     * Validation rules:
     * - Name must be at least 2 characters
     * - Duplicate names are rejected (caller should handle with confirmation dialog)
     * 
     * @param name User-provided location name (e.g., "Home", "Work")
     * @param latitude GPS latitude coordinate
     * @param longitude GPS longitude coordinate
     * @return Result.success(locationId) if saved, Result.failure(exception) otherwise
     */
    suspend fun saveLocation(
        name: String,
        latitude: Double,
        longitude: Double
    ): Result<Long>
    
    /**
     * Get all saved locations sorted by most recently used.
     * 
     * @return Flow emitting list of locations (reactive updates on data change)
     */
    fun getAllLocationsSorted(): Flow<List<SavedLocationEntity>>
    
    /**
     * Delete a saved location.
     * 
     * @param location The location entity to delete
     * @return Result.success(Unit) if deleted, Result.failure(exception) otherwise
     */
    suspend fun deleteLocation(location: SavedLocationEntity): Result<Unit>
    
    /**
     * Update an existing saved location.
     * 
     * Used for:
     * - Renaming locations (Story 7.2)
     * - Updating lastUsedAt timestamp (Story 7.3)
     * 
     * @param location The location entity with updated fields
     * @return Result.success(Unit) if updated, Result.failure(exception) otherwise
     */
    suspend fun updateLocation(location: SavedLocationEntity): Result<Unit>
    
    /**
     * Find a location by exact name match.
     * 
     * Used for duplicate name detection.
     * 
     * @param name The location name to search for
     * @return The location entity if found, null otherwise
     */
    suspend fun findLocationByName(name: String): SavedLocationEntity?
}

/**
 * Exception thrown when saved location operations fail.
 */
class SavedLocationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when attempting to save a location with duplicate name.
 */
class DuplicateLocationException(val locationName: String) : Exception("Location '$locationName' already exists")
