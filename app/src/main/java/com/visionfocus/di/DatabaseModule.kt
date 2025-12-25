package com.visionfocus.di

import android.content.Context
import androidx.room.Room
import com.visionfocus.data.local.AppDatabase
import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
     * Provides singleton AppDatabase instance.
     * 
     * Migration strategy: fallbackToDestructiveMigration() for development.
     * This is acceptable for Stories 1.4-7.1 because:
     * - Database schema is evolving (columns added incrementally)
     * - User data loss acceptable during development/testing
     * - No production users yet
     * 
     * IMPORTANT: Before production release, replace with proper Migration objects
     * to preserve user data (recognition history, saved locations) across updates.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // Dev only - replace for production
            .build()
    }
    
    /**
     * Provides RecognitionHistoryDao.
     * 
     * DAO methods will be added in Story 4.2 when recognition history
     * feature is implemented (Epic 4).
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
}
