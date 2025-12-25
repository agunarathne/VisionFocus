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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
 * Comprehensive integration tests for SettingsRepository.
 * 
 * These tests verify:
 * - Write and read cycles for all preferences
 * - Speech rate clamping
 * - Persistence across DataStore instances
 * - Thread-safety under concurrent access
 * - Flow emission on preference changes
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryIntegrationTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var testDataStoreFile: java.io.File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository
    
    @Before
    fun setup() {
        // Use unique file for each test
        testDataStoreFile = context.preferencesDataStoreFile("test_settings_${System.nanoTime()}")
        
        // Clean up any existing test file
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
        
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testDataStoreFile }
        )
        
        repository = SettingsRepositoryImpl(dataStore)
    }
    
    @After
    fun teardown() {
        // Clean up test file
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
    }
    
    @Test
    fun speech_rate_write_and_read_cycle_works_correctly() = runTest(testDispatcher) {
        // Write speech rate
        repository.setSpeechRate(1.5f)
        
        // Read speech rate
        val speechRate = repository.getSpeechRate().first()
        assertEquals(1.5f, speechRate)
    }
    
    @Test
    fun speech_rate_is_clamped_to_valid_range() = runTest(testDispatcher) {
        // Try to set rate below minimum (0.5x)
        repository.setSpeechRate(0.3f)
        assertEquals(0.5f, repository.getSpeechRate().first(), "Rate below 0.5 should be clamped")
        
        // Try to set rate above maximum (2.0x)
        repository.setSpeechRate(3.0f)
        assertEquals(2.0f, repository.getSpeechRate().first(), "Rate above 2.0 should be clamped")
    }
    
    @Test
    fun verbosity_mode_write_and_read_cycle_works_correctly() = runTest(testDispatcher) {
        // Test all verbosity modes
        for (mode in VerbosityMode.values()) {
            repository.setVerbosity(mode)
            val readMode = repository.getVerbosity().first()
            assertEquals(mode, readMode, "Failed for mode: $mode")
        }
    }
    
    @Test
    fun high_contrast_mode_write_and_read_cycle_works_correctly() = runTest(testDispatcher) {
        // Enable high-contrast mode
        repository.setHighContrastMode(true)
        assertTrue(repository.getHighContrastMode().first())
        
        // Disable high-contrast mode
        repository.setHighContrastMode(false)
        assertEquals(false, repository.getHighContrastMode().first())
    }
    
    @Test
    fun flow_emits_updated_values_when_preferences_change() = runTest(testDispatcher) {
        // Initial value
        val initialRate = repository.getSpeechRate().first()
        assertEquals(1.0f, initialRate)
        
        // Update preference
        repository.setSpeechRate(1.75f)
        
        // Flow should emit new value
        val updatedRate = repository.getSpeechRate().first()
        assertEquals(1.75f, updatedRate)
    }
    
    @Test
    fun preferences_persist_across_datastore_instances() = runTest(testDispatcher) {
        // Write preferences with first repository instance
        repository.setSpeechRate(1.75f)
        repository.setVerbosity(VerbosityMode.DETAILED)
        repository.setHighContrastMode(true)
        
        // Create new DataStore instance reading from same file (simulates app restart)
        val dataStore2 = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testDataStoreFile }
        )
        val repository2 = SettingsRepositoryImpl(dataStore2)
        
        // Verify preferences persisted
        assertEquals(1.75f, repository2.getSpeechRate().first(), "Speech rate should persist")
        assertEquals(VerbosityMode.DETAILED, repository2.getVerbosity().first(), "Verbosity should persist")
        assertTrue(repository2.getHighContrastMode().first(), "High contrast should persist")
    }
    
    @Test
    fun concurrent_writes_do_not_corrupt_data() = runTest(testDispatcher) {
        val iterations = 50
        
        // Launch multiple coroutines writing simultaneously
        val jobs = (1..iterations).map { iteration ->
            async {
                repository.setSpeechRate(0.5f + (iteration * 0.01f))
            }
        }
        
        // Wait for all writes to complete
        jobs.awaitAll()
        
        // Verify DataStore didn't crash or corrupt data
        val finalRate = repository.getSpeechRate().first()
        
        // Final value should be one of the written values (in valid range)
        assertTrue(finalRate in 0.5f..2.0f, "Final rate $finalRate should be in valid range")
    }
    
    @Test
    fun concurrent_reads_during_writes_return_consistent_values() = runTest(testDispatcher) {
        // Start with a known value
        repository.setSpeechRate(1.0f)
        
        // Launch concurrent reads and writes
        val writeJobs = (1..25).map { iteration ->
            async {
                repository.setSpeechRate(0.5f + (iteration * 0.01f))
            }
        }
        
        val readJobs = (1..25).map {
            async {
                repository.getSpeechRate().first()
            }
        }
        
        // Wait for all operations
        writeJobs.awaitAll()
        val readResults = readJobs.awaitAll()
        
        // All reads should return valid values (no corruption)
        readResults.forEach { rate ->
            assertTrue(rate in 0.5f..2.0f, "Rate $rate should be in valid range")
        }
    }
}
