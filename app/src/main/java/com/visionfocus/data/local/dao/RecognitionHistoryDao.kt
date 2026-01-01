package com.visionfocus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recognition history operations.
 * 
 * Provides methods to insert, query, and manage recognition history entries.
 * History is automatically pruned to maintain the last 50 entries.
 */
@Dao
interface RecognitionHistoryDao {
    
    /**
     * Inserts a new recognition history entry.
     * If entry with same ID exists, it will be replaced.
     * 
     * @param entry The recognition history entry to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecognition(entry: RecognitionHistoryEntity)
    
    /**
     * Retrieves the most recent recognition history entries.
     * Results are ordered by timestamp in descending order (newest first).
     * 
     * @param limit Maximum number of entries to retrieve (default: 50)
     * @return Flow emitting list of recognition history entries
     */
    @Query("SELECT * FROM recognition_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecognitions(limit: Int = 50): Flow<List<RecognitionHistoryEntity>>
    
    /**
     * Prunes old recognition history entries beyond the specified limit.
     * Deletes all entries NOT in the latest N entries by timestamp.
     * 
     * @param limit Number of most recent entries to keep (default: 50)
     */
    @Query("""
        DELETE FROM recognition_history 
        WHERE id NOT IN (
            SELECT id FROM recognition_history 
            ORDER BY timestamp DESC 
            LIMIT :limit
        )
    """)
    suspend fun pruneOldEntries(limit: Int = 50)
    
    /**
     * Deletes all recognition history entries.
     * Used when user clears their history.
     */
    @Query("DELETE FROM recognition_history")
    suspend fun clearHistory()
    
    /**
     * Returns the total number of recognition history entries stored.
     * 
     * @return Total count of stored entries
     */
    @Query("SELECT COUNT(*) FROM recognition_history")
    suspend fun getRecognitionCount(): Int
    
    /**
     * Inserts recognition and prunes only if count exceeds limit.
     * Code Review Fix: More efficient than pruning after every insert.
     * 
     * @param entry The recognition history entry to insert
     * @param limit Maximum number of entries to keep
     */
    suspend fun insertAndPruneIfNeeded(entry: RecognitionHistoryEntity, limit: Int = 50) {
        insertRecognition(entry)
        val count = getRecognitionCount()
        if (count > limit) {
            pruneOldEntries(limit)
        }
    }
}
