package com.visionfocus.data.local.dao

import androidx.room.Dao

/**
 * Data Access Object for saved locations.
 * 
 * Foundation DAO for Story 1.4. Query methods will be added in Story 7.1
 * when saved locations feature is implemented (Epic 7).
 * 
 * Future methods (Story 7.1):
 * - @Insert fun insert(location: SavedLocationEntity)
 * - @Query fun getAllLocations(): Flow<List<SavedLocationEntity>>
 * - @Update fun update(location: SavedLocationEntity)
 * - @Delete fun delete(location: SavedLocationEntity)
 */
@Dao
interface SavedLocationDao {
    // Query methods will be added in Story 7.1 (Epic 7: Saved Locations)
}
