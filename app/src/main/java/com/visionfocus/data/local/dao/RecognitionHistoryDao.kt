package com.visionfocus.data.local.dao

import androidx.room.Dao

/**
 * Data Access Object for recognition history.
 * 
 * Foundation DAO for Story 1.4. Query methods will be added in Story 4.2
 * when recognition history feature is implemented (Epic 4).
 * 
 * Future methods (Story 4.2):
 * - @Insert fun insert(entry: RecognitionHistoryEntity)
 * - @Query fun getLast50Results(): Flow<List<RecognitionHistoryEntity>>
 * - @Query fun pruneOldEntries()
 * - @Delete fun delete(entry: RecognitionHistoryEntity)
 */
@Dao
interface RecognitionHistoryDao {
    // Query methods will be added in Story 4.2 (Epic 4: Recognition History)
}
