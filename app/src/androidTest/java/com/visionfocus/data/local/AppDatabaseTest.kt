package com.visionfocus.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Room database setup.
 * 
 * Validates database creation, DAO injection, and schema version.
 * Entity column tests will be added in Stories 4.2 and 7.1.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var recognitionHistoryDao: RecognitionHistoryDao
    private lateinit var savedLocationDao: SavedLocationDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Create in-memory database for testing (data cleared after each test)
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()  // OK for testing
            .build()
        
        recognitionHistoryDao = database.recognitionHistoryDao()
        savedLocationDao = database.savedLocationDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun databaseCreatesSuccessfully() {
        // Verify database instance is created
        assertNotNull(database)
    }
    
    @Test
    fun databaseVersionIs1() {
        // Verify schema version matches Story 1.4 foundation
        assertEquals(1, database.openHelper.readableDatabase.version)
    }
    
    @Test
    fun recognitionHistoryDaoCanBeRetrieved() {
        // Verify Hilt can inject RecognitionHistoryDao
        assertNotNull(recognitionHistoryDao)
    }
    
    @Test
    fun savedLocationDaoCanBeRetrieved() {
        // Verify Hilt can inject SavedLocationDao
        assertNotNull(savedLocationDao)
    }
    
    @Test
    fun databaseContainsRecognitionHistoryTable() {
        // Verify entity table creation
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='recognition_history'"
        )
        
        assertEquals("recognition_history table should exist", 1, cursor.count)
        cursor.close()
    }
    
    @Test
    fun databaseContainsSavedLocationsTable() {
        // Verify entity table creation
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='saved_locations'"
        )
        
        assertEquals("saved_locations table should exist", 1, cursor.count)
        cursor.close()
    }
}
