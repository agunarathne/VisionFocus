package com.visionfocus.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.visionfocus.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for saved locations.
 * 
 * Story 7.1: Complete DAO methods for saved locations feature.
 * All operations are suspend functions for coroutine support.
 * 
 * Query methods:
 * - insert: Add new location, returns generated ID
 * - getAllLocationsSorted: Get all locations ordered by most recently used
 * - update: Update existing location
 * - delete: Remove location
 * - findByName: Find location by exact name match (duplicate check)
 * - updateLastUsedAt: Update last used timestamp (Story 7.2/7.3)
 */
@Dao
interface SavedLocationDao {
    
    /**
     * Insert a new saved location.
     * @return The ID of the newly inserted location
     */
    @Insert
    suspend fun insert(location: SavedLocationEntity): Long
    
    /**
     * Get all saved locations sorted by most recently used.
     * Returns a Flow for reactive updates when data changes.
     */
    @Query("SELECT * FROM saved_locations ORDER BY lastUsedAt DESC")
    fun getAllLocationsSorted(): Flow<List<SavedLocationEntity>>
    
    /**
     * Update an existing saved location.
     */
    @Update
    suspend fun update(location: SavedLocationEntity)
    
    /**
     * Delete a saved location.
     */
    @Delete
    suspend fun delete(location: SavedLocationEntity)
    
    /**
     * Find a saved location by exact name match.
     * Used for duplicate name detection.
     * @return The location entity if found, null otherwise
     */
    @Query("SELECT * FROM saved_locations WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): SavedLocationEntity?
    
    /**
     * Update the last used timestamp for a location.
     * Called when user navigates to this location (Story 7.2/7.3).
     */
    @Query("UPDATE saved_locations SET lastUsedAt = :timestamp WHERE id = :locationId")
    suspend fun updateLastUsedAt(locationId: Long, timestamp: Long)
}
