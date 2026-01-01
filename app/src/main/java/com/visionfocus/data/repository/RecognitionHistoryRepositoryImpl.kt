package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of RecognitionHistoryRepository.
 * 
 * Manages recognition history storage with automatic pruning to maintain 50-entry limit.
 * All database operations are performed on IO dispatcher for thread safety.
 * 
 * Error handling strategy:
 * - Database errors are logged and wrapped in RecognitionHistoryException
 * - Callers should handle exceptions to prevent blocking main recognition flow
 * 
 * @property dao Data access object for recognition history database operations
 */
class RecognitionHistoryRepositoryImpl @Inject constructor(
    private val dao: RecognitionHistoryDao
) : RecognitionHistoryRepository {
    
    override suspend fun saveRecognition(
        category: String,
        confidence: Float,
        verbosityMode: String,
        detailText: String
    ) {
        // Code Review Fix: Input validation to prevent corrupt data
        require(category.isNotBlank()) { "Category cannot be blank" }
        require(confidence in 0f..1f) { "Confidence must be between 0 and 1, got $confidence" }
        require(verbosityMode.lowercase() in listOf("brief", "standard", "detailed")) { 
            "Invalid verbosity mode: $verbosityMode" 
        }
        require(detailText.length <= 500) { 
            "Detail text exceeds 500 character limit: ${detailText.length}" 
        }
        
        withContext(Dispatchers.IO) {
            try {
                val entity = RecognitionHistoryEntity(
                    category = category,
                    confidence = confidence,
                    timestamp = System.currentTimeMillis(),
                    verbosityMode = verbosityMode.lowercase(),
                    detailText = detailText
                )
                
                // Code Review Fix: Use efficient pruning that only runs when needed
                dao.insertAndPruneIfNeeded(entity, 50)
                
                Timber.d("Saved recognition: $category (confidence: ${confidence * 100}%%)")
            } catch (e: Exception) {
                Timber.e(e, "Failed to save recognition history")
                throw RecognitionHistoryException("Database save failed", e)
            }
        }
    }
    
    override fun getRecentHistory(): Flow<List<RecognitionHistoryEntity>> {
        return dao.getRecentRecognitions(50)
    }
    
    override suspend fun clearAllHistory() {
        withContext(Dispatchers.IO) {
            try {
                dao.clearHistory()
                Timber.i("Cleared all recognition history")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear recognition history")
                throw RecognitionHistoryException("Clear history failed", e)
            }
        }
    }
}
