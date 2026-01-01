package com.visionfocus.data.repository

import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recognition history operations.
 * 
 * Provides abstraction layer between data sources and domain/presentation layers.
 * Manages recognition history storage, retrieval, and cleanup operations.
 */
interface RecognitionHistoryRepository {
    
    /**
     * Saves a new recognition result to history.
     * Automatically prunes old entries to maintain 50-entry limit.
     * 
     * @param category Recognized object category
     * @param confidence Recognition confidence score (0.0 to 1.0)
     * @param verbosityMode Active verbosity mode (brief/standard/detailed)
     * @param detailText Full text announcement provided to user
     * @throws RecognitionHistoryException if database operation fails
     * @throws IllegalArgumentException if input validation fails
     */
    suspend fun saveRecognition(
        category: String,
        confidence: Float,
        verbosityMode: String,
        detailText: String
    )
    
    /**
     * Retrieves recent recognition history as reactive stream.
     * Automatically updates when new recognitions are saved.
     * 
     * @return Flow emitting list of up to 50 most recent entries, ordered by timestamp descending
     */
    fun getRecentHistory(): Flow<List<RecognitionHistoryEntity>>
    
    /**
     * Deletes all recognition history entries.
     * Used when user clears their history from settings.
     */
    suspend fun clearAllHistory()
}

/**
 * Exception thrown when recognition history operations fail.
 */
class RecognitionHistoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
