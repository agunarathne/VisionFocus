package com.visionfocus.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.data.model.VerbosityMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test verifying DataStore preferences persist across app restarts.
 * 
 * Simulates app restart by creating new DataStore instances that read
 * from the same underlying file.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DataStorePersistenceTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDataStoreFile = context.preferencesDataStoreFile("test_persistence")
    
    @Before
    fun setup() {
        // Clean up any existing test file
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
    }
    
    @After
    fun teardown() {
        // Clean up test file
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
    }
    
    @Test
    fun preferences_persist_across_datastore_instances() = runTest(testDispatcher) {
        // Create first DataStore instance and write preferences
        val dataStore1: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testDataStoreFile }
        )
        val repository1 = SettingsRepositoryImpl(dataStore1)
        
        // Write preferences
        repository1.setSpeechRate(1.75f)
        repository1.setVerbosity(VerbosityMode.DETAILED)
        repository1.setHighContrastMode(true)
        
        // Simulate app restart by creating new DataStore instance
        // This reads from the same underlying file
        val dataStore2: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testDataStoreFile }
        )
        val repository2 = SettingsRepositoryImpl(dataStore2)
        
        // Verify preferences persisted
        assertEquals(1.75f, repository2.getSpeechRate().first(), "Speech rate should persist")
        assertEquals(VerbosityMode.DETAILED, repository2.getVerbosity().first(), "Verbosity should persist")
        assertTrue(repository2.getHighContrastMode().first(), "High contrast should persist")
    }
}
