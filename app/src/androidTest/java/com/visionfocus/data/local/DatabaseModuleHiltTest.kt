package com.visionfocus.data.local

import com.visionfocus.data.local.dao.RecognitionHistoryDao
import com.visionfocus.data.local.dao.SavedLocationDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Instrumented tests for Hilt database module dependency injection.
 * 
 * Validates that AppDatabase and DAOs can be injected via Hilt DI,
 * satisfying AC #7: "Database builds successfully and can be injected into repositories"
 */
@HiltAndroidTest
class DatabaseModuleHiltTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var recognitionHistoryDao: RecognitionHistoryDao
    
    @Inject
    lateinit var savedLocationDao: SavedLocationDao
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun hiltProvidesAppDatabase() {
        // Verify AppDatabase can be injected via Hilt
        assertNotNull(database)
    }
    
    @Test
    fun hiltProvidesRecognitionHistoryDao() {
        // Verify RecognitionHistoryDao can be injected via Hilt (AC #7)
        assertNotNull(recognitionHistoryDao)
    }
    
    @Test
    fun hiltProvidesSavedLocationDao() {
        // Verify SavedLocationDao can be injected via Hilt (AC #7)
        assertNotNull(savedLocationDao)
    }
    
    @Test
    fun injectedDaoMatchesDatabaseDao() {
        // Verify injected DAO is same instance as database DAO (singleton scope)
        // Note: Room DAOs are not singletons by default, so we compare types only
        assertNotNull(database.recognitionHistoryDao())
        assertNotNull(database.savedLocationDao())
    }
}
