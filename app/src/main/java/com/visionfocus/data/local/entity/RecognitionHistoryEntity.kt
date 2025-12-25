package com.visionfocus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for recognition history storage.
 * 
 * Foundation schema for Story 1.4. Full columns will be added in Story 4.2
 * when recognition history feature is implemented (Epic 4).
 * 
 * Future columns (Story 4.2):
 * - objectLabel: String (recognized object category)
 * - confidence: Float (recognition confidence score)
 * - timestamp: Long (recognition time in milliseconds)
 * - verbosityMode: String (brief/standard/detailed)
 */
@Entity(tableName = "recognition_history")
data class RecognitionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
    
    // Columns will be added in Story 4.2 (Epic 4: Recognition History)
)
