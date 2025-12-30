package com.visionfocus.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.visionfocus.data.model.VerbosityMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals

/**
 * Unit tests for SettingsRepository DataStore implementation.
 * 
 * Uses in-memory DataStore for fast, isolated testing without device/emulator.
 * 
 * Note: Write tests are moved to instrumentation tests (androidTest) due to
 * Windows file locking issues with TemporaryFolder and DataStore.
 * DataStore works perfectly on actual Android devices.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    
    private lateinit var testFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository
    
    @Before
    fun setup() {
        // Create unique test file for each test
        testFile = tmpFolder.newFile("test_prefs_${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        
        repository = SettingsRepositoryImpl(dataStore)
    }
    
    @After
    fun teardown() {
        // Cancel the test scope to clean up coroutines
        testScope.cancel()
    }
    
    @Test
    fun `speech rate defaults to 1_0 when not set`() = runTest(testDispatcher) {
        val speechRate = repository.getSpeechRate().first()
        assertEquals(1.0f, speechRate)
    }
    
    @Test
    fun `verbosity mode defaults to STANDARD when not set`() = runTest(testDispatcher) {
        val verbosity = repository.getVerbosity().first()
        assertEquals(VerbosityMode.STANDARD, verbosity)
    }
    
    @Test
    fun `high contrast mode defaults to false when not set`() = runTest(testDispatcher) {
        val highContrast = repository.getHighContrastMode().first()
        assertEquals(false, highContrast)
    }
    
    @Test
    fun `large text mode defaults to false when not set`() = runTest(testDispatcher) {
        val largeTextMode = repository.getLargeTextMode().first()
        assertEquals(false, largeTextMode)
    }
}
