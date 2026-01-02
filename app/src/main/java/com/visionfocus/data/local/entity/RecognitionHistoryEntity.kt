package com.visionfocus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for recognition history storage.
 * 
 * Stores the last 50 object recognitions for review and verification.
 * Automatically pruned to maintain storage limit.
 * 
 * Code Review Fix: Added index on timestamp for ORDER BY query performance.
 * Story 4.5: Added spatial information fields (positionText, distanceText)
 * 
 * @property id Unique identifier for the recognition entry (auto-generated)
 * @property category Recognized object category (e.g., "person", "car", "chair")
 * @property confidence Recognition confidence score (0.0 to 1.0)
 * @property timestamp Unix epoch time in milliseconds when recognition occurred
 * @property verbosityMode Active verbosity mode during recognition (brief/standard/detailed)
 * @property detailText Full text announcement provided to user via TTS
 * @property positionText Human-readable position description (e.g., "on the left", "in center of view") - Story 4.5
 * @property distanceText Human-readable distance description (e.g., "close by", "at medium distance") - Story 4.5
 */
@Entity(
    tableName = "recognition_history",
    indices = [androidx.room.Index(value = ["timestamp"], name = "idx_recognition_timestamp")]
)
data class RecognitionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "confidence")
    val confidence: Float,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "verbosityMode")
    val verbosityMode: String,
    
    @ColumnInfo(name = "detailText")
    val detailText: String,
    
    @ColumnInfo(name = "positionText", defaultValue = "")
    val positionText: String? = null,
    
    @ColumnInfo(name = "distanceText", defaultValue = "")
    val distanceText: String? = null
)
