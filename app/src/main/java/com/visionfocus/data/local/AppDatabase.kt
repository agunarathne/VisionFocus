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
 * Database version: 2 (Story 4.2 - added RecognitionHistoryEntity columns)
 * Migration strategy: MIGRATION_1_2 for production data preservation
 * 
 * Version history:
 * - v1: Foundation schema (Story 1.4) - empty entities with id field only
 * - v2: Recognition history columns (Story 4.2) - category, confidence, timestamp, verbosityMode, detailText
 * 
 * Future enhancements:
 * - Story 7.1: Add full schema for SavedLocationEntity + encryption with SQLCipher
 */
@Database(
    entities = [
        RecognitionHistoryEntity::class,
        SavedLocationEntity::class
    ],
    version = 2,
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
