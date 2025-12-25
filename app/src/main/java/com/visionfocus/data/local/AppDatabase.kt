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
 * Database version: 1 (foundation schema)
 * Migration strategy: fallbackToDestructiveMigration() for development
 * 
 * Future enhancements:
 * - Story 4.2: Add full schema for RecognitionHistoryEntity + encryption with SQLCipher
 * - Story 7.1: Add full schema for SavedLocationEntity + encryption with SQLCipher
 * - Production: Replace fallbackToDestructiveMigration with proper Migration objects
 */
@Database(
    entities = [
        RecognitionHistoryEntity::class,
        SavedLocationEntity::class
    ],
    version = 1,
    exportSchema = true  // Export schema for version tracking and migrations
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Provides access to recognition history data access object.
     * Methods will be added in Story 4.2.
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
