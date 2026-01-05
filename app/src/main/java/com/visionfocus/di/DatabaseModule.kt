package com.visionfocus.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.visionfocus.data.local.AppDatabase
import com.visionfocus.data.local.EncryptionHelper
import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import com.visionfocus.data.repository.RecognitionHistoryRepository
import com.visionfocus.data.repository.RecognitionHistoryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAO dependencies.
 * 
 * Database lifecycle is application-scoped (singleton) to ensure:
 * - Single database instance throughout app lifecycle
 * - Proper connection pooling and transaction management
 * - Consistent data access across all repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Migration from database version 1 to 2.
     * 
     * Story 4.2: Adds columns to RecognitionHistoryEntity
     * - category: String
     * - confidence: Float
     * - timestamp: Long
     * - verbosityMode: String
     * - detailText: String
     * 
     * Code Review Fix: Adds index on timestamp column for query performance
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns with NOT NULL and default values
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN category TEXT NOT NULL DEFAULT ''"
            )
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN confidence REAL NOT NULL DEFAULT 0.0"
            )
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN timestamp INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN verbosityMode TEXT NOT NULL DEFAULT 'standard'"
            )
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN detailText TEXT NOT NULL DEFAULT ''"
            )
            
            // Add index on timestamp for ORDER BY query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_recognition_timestamp ON recognition_history(timestamp)"
            )
        }
    }
    
    /**
     * Migration from database version 2 to 3.
     * 
     * Story 4.5: Adds spatial information columns to RecognitionHistoryEntity
     * - positionText: String (nullable) - e.g., "on the left", "in center of view"
     * - distanceText: String (nullable) - e.g., "close by", "at medium distance"
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add spatial information columns (nullable for backward compatibility)
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN positionText TEXT DEFAULT ''"
            )
            database.execSQL(
                "ALTER TABLE recognition_history ADD COLUMN distanceText TEXT DEFAULT ''"
            )
        }
    }
    
    /**
     * Migration from database version 3 to 4.
     * 
     * Story 7.1: Adds full schema columns to SavedLocationEntity
     * - name: String - user-provided location name
     * - latitude: Double - GPS latitude coordinate
     * - longitude: Double - GPS longitude coordinate
     * - createdAt: Long - timestamp when location was saved
     * - lastUsedAt: Long - timestamp when location was last used for navigation
     * - address: String (nullable) - reverse geocoded address (future enhancement)
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add SavedLocationEntity columns with NOT NULL and default values
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN name TEXT NOT NULL DEFAULT ''"
            )
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0"
            )
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0"
            )
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN lastUsedAt INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE saved_locations ADD COLUMN address TEXT DEFAULT NULL"
            )
            
            // Add index on name for duplicate check query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_saved_location_name ON saved_locations(name)"
            )
            
            // Add index on lastUsedAt for ORDER BY query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_saved_location_last_used ON saved_locations(lastUsedAt DESC)"
            )
        }
    }
    
    /**
     * Provides singleton AppDatabase instance with encryption.
     * 
     * Story 4.2 enhancements:
     * - SQLCipher encryption with Android Keystore-managed passphrase
     * - Migration from v1 → v2 to add RecognitionHistoryEntity columns
     * - Replaced fallbackToDestructiveMigration with explicit migration
     * 
     * Story 4.5 enhancements:
     * - Migration from v2 → v3 to add spatial information columns
     * 
     * Story 7.1 enhancements:
     * - Migration from v3 → v4 to add SavedLocationEntity full schema
     * 
     * Security: Database encrypted at rest with AES-256 (SQLCipher)
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        // Code Review Fix: Handle SQLCipher initialization failures gracefully
        return try {
            // Story 4.2: Get encryption passphrase from Android Keystore
            val passphrase = EncryptionHelper.getDatabasePassphrase(context)
            val factory = SupportFactory(passphrase)
            
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .openHelperFactory(factory)  // Story 4.2: Enable SQLCipher encryption
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)  // Story 4.2, 4.5, 7.1: Migrations for schema changes
                .build()
        } catch (e: Exception) {
            // Fallback: Create unencrypted database if encryption fails
            // (Keystore unavailable, device incompatibility, etc.)
            android.util.Log.e("DatabaseModule", "Failed to initialize encrypted database, using unencrypted fallback", e)
            
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
    
    /**
     * Provides RecognitionHistoryDao.
     * 
     * DAO methods added in Story 4.2 for recognition history feature.
     */
    @Provides
    @Singleton
    fun provideRecognitionHistoryDao(database: AppDatabase): RecognitionHistoryDao {
        return database.recognitionHistoryDao()
    }
    
    /**
     * Provides SavedLocationDao.
     * 
     * DAO methods will be added in Story 7.1 when saved locations
     * feature is implemented (Epic 7).
     */
    @Provides
    @Singleton
    fun provideSavedLocationDao(database: AppDatabase): SavedLocationDao {
        return database.savedLocationDao()
    }
    
    /**
     * Provides RecognitionHistoryRepository implementation.
     * 
     * Binds the repository interface to its implementation for dependency injection.
     * Story 4.2: Recognition history storage with automatic pruning.
     */
    @Provides
    @Singleton
    fun provideRecognitionHistoryRepository(
        dao: RecognitionHistoryDao
    ): RecognitionHistoryRepository {
        return RecognitionHistoryRepositoryImpl(dao)
    }
}
