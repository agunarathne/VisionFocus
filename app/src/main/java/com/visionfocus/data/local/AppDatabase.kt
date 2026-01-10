package com.visionfocus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import com.visionfocus.data.local.entity.SavedLocationEntity

/**
 * Room database for VisionFocus local data storage.
 * 
 * Contains three entities:
 * 1. RecognitionHistoryEntity - Stores last 50 recognition results (Epic 4)
 * 2. SavedLocationEntity - Stores user's frequently visited locations (Epic 7)
 * 3. OfflineMapEntity - Stores offline map metadata for saved locations (Story 7.4)
 * 
 * Database version: 6 (Story 7.4 - added OfflineMapEntity)
 * Migration strategy: MIGRATION_5_6 for production data preservation
 * 
 * Version history:
 * - v1: Foundation schema (Story 1.4) - empty entities with id field only
 * - v2: Recognition history columns (Story 4.2) - category, confidence, timestamp, verbosityMode, detailText
 * - v3: Spatial information (Story 4.5) - positionText, distanceText
 * - v4: Saved locations schema (Story 7.1) - name, latitude, longitude, createdAt, lastUsedAt, address
 * - v5: Database migration bug fix (Story 7.2)
 * - v6: Offline maps table (Story 7.4) - locationId FK, download metadata, expiration tracking
 * 
 * Future enhancements:
 * - Story 7.5: Automatic offline/GPS mode switching
 * - Story 7.6: Destination arrival confirmation
 */
@Database(
    entities = [
        RecognitionHistoryEntity::class,
        SavedLocationEntity::class,
        OfflineMapEntity::class
    ],
    version = 6,
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
     * Methods added in Story 7.1.
     */
    abstract fun savedLocationDao(): SavedLocationDao
    
    /**
     * Provides access to offline maps data access object.
     * Methods added in Story 7.4.
     */
    abstract fun offlineMapDao(): OfflineMapDao
    
    companion object {
        const val DATABASE_NAME = "visionfocus_database"
    }
}
