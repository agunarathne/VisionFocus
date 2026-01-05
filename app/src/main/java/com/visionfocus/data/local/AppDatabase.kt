package com.visionfocus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import com.visionfocus.data.local.entity.SavedLocationEntity

/**
 * Room database for VisionFocus local data storage.
 * 
 * Contains two entities:
 * 1. RecognitionHistoryEntity - Stores last 50 recognition results (Epic 4)
 * 2. SavedLocationEntity - Stores user's frequently visited locations (Epic 7)
 * 
 * Database version: 4 (Story 7.1 - added SavedLocationEntity full schema)
 * Migration strategy: MIGRATION_3_4 for production data preservation
 * 
 * Version history:
 * - v1: Foundation schema (Story 1.4) - empty entities with id field only
 * - v2: Recognition history columns (Story 4.2) - category, confidence, timestamp, verbosityMode, detailText
 * - v3: Spatial information (Story 4.5) - positionText, distanceText
 * - v4: Saved locations schema (Story 7.1) - name, latitude, longitude, createdAt, lastUsedAt, address
 * 
 * Future enhancements:
 * - Story 7.2: Saved locations management UI
 * - Story 7.3: Quick navigation to saved locations
 */
@Database(
    entities = [
        RecognitionHistoryEntity::class,
        SavedLocationEntity::class
    ],
    version = 4,
    exportSchema = true  // Export schema for version tracking and migrations
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Provides access to recognition history data access object.
     * Methods added in Story 4.2.
     */
    abstract fun recognitionHistoryDao(): RecognitionHistoryDao
    
    /**
     * Provides access to saved locations data access object.
     * Methods will be added in Story 7.1.
     */
    abstract fun savedLocationDao(): SavedLocationDao
    
    companion object {
        const val DATABASE_NAME = "visionfocus_database"
    }
}
