package com.visionfocus.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.visionfocus.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for database migration from v3 to v4.
 * 
 * Story 7.1 Task 12: Migration instrumentation tests
 * 
 * Tests MIGRATION_3_4 which adds SavedLocationEntity full schema:
 * - name: String
 * - latitude: Double
 * - longitude: Double
 * - createdAt: Long
 * - lastUsedAt: Long
 * - address: String (nullable)
 * 
 * Verifies:
 * - Migration executes successfully
 * - All new columns exist with correct types
 * - Default values applied correctly
 * - Can insert and query SavedLocationEntity after migration
 * - Database version is 4 after migration
 */
@RunWith(AndroidJUnit4::class)
class SavedLocationMigrationTest {
    
    private val TEST_DB_NAME = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_addsSavedLocationColumns() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            // Insert test data into recognition_history (existing table)
            execSQL(
                """
                INSERT INTO recognition_history (id, category, confidence, timestamp, verbosityMode, detailText, positionText, distanceText)
                VALUES (1, 'person', 0.95, 1234567890, 'standard', 'Person detected', 'center', 'close by')
                """.trimIndent()
            )
            close()
        }
        
        // Run migration to version 4
        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            4,
            true
            // Migration is auto-discovered from DatabaseModule
        )
        
        // Verify saved_locations table has new columns
        val cursor = db.query("SELECT * FROM saved_locations")
        
        val columnNames = cursor.columnNames.toList()
        
        // Verify all columns exist
        assert(columnNames.contains("id")) { "Missing 'id' column" }
        assert(columnNames.contains("name")) { "Missing 'name' column" }
        assert(columnNames.contains("latitude")) { "Missing 'latitude' column" }
        assert(columnNames.contains("longitude")) { "Missing 'longitude' column" }
        assert(columnNames.contains("createdAt")) { "Missing 'createdAt' column" }
        assert(columnNames.contains("lastUsedAt")) { "Missing 'lastUsedAt' column" }
        assert(columnNames.contains("address")) { "Missing 'address' column" }
        
        cursor.close()
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_canInsertAndQuerySavedLocation() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            close()
        }
        
        // Run migration to version 4
        helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true)
        
        // Get a Room database instance to test high-level operations
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DB_NAME
        ).build()
        
        // Insert a saved location using DAO
        runBlocking {
            val location = SavedLocationEntity(
                name = "Home",
                latitude = 51.5074,
                longitude = -0.1278,
                createdAt = 1000L,
                lastUsedAt = 2000L,
                address = null
            )
            
            val insertedId = db.savedLocationDao().insert(location)
            assert(insertedId > 0) { "Failed to insert location" }
            
            // Query back the location
            val retrieved = db.savedLocationDao().findByName("Home")
            
            assertNotNull("Location not found after insert", retrieved)
            assertEquals("Home", retrieved?.name)
            assertEquals(51.5074, retrieved?.latitude, 0.0001)
            assertEquals(-0.1278, retrieved?.longitude, 0.0001)
            assertEquals(1000L, retrieved?.createdAt)
            assertEquals(2000L, retrieved?.lastUsedAt)
            assertEquals(null, retrieved?.address)
        }
        
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_defaultValuesApplied() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            // Insert a saved location with only id (pre-migration stub)
            execSQL("INSERT INTO saved_locations (id) VALUES (1)")
            close()
        }
        
        // Run migration to version 4
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true)
        
        // Query the migrated row
        val cursor = db.query("SELECT * FROM saved_locations WHERE id = 1")
        
        assert(cursor.moveToFirst()) { "No rows found" }
        
        // Verify default values
        val nameIndex = cursor.getColumnIndex("name")
        val latitudeIndex = cursor.getColumnIndex("latitude")
        val longitudeIndex = cursor.getColumnIndex("longitude")
        val createdAtIndex = cursor.getColumnIndex("createdAt")
        val lastUsedAtIndex = cursor.getColumnIndex("lastUsedAt")
        val addressIndex = cursor.getColumnIndex("address")
        
        assertEquals("Default name should be empty string", "", cursor.getString(nameIndex))
        assertEquals("Default latitude should be 0.0", 0.0, cursor.getDouble(latitudeIndex), 0.0001)
        assertEquals("Default longitude should be 0.0", 0.0, cursor.getDouble(longitudeIndex), 0.0001)
        assertEquals("Default createdAt should be 0", 0L, cursor.getLong(createdAtIndex))
        assertEquals("Default lastUsedAt should be 0", 0L, cursor.getLong(lastUsedAtIndex))
        assertEquals("Default address should be null", null, cursor.getString(addressIndex))
        
        cursor.close()
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_databaseVersionIs4() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            close()
        }
        
        // Run migration to version 4
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true)
        
        // Verify database version
        assertEquals("Database version should be 4", 4, db.version)
        
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_indexesCreated() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            close()
        }
        
        // Run migration to version 4
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true)
        
        // Verify indexes exist
        val cursor = db.query(
            """
            SELECT name FROM sqlite_master 
            WHERE type='index' AND tbl_name='saved_locations'
            """.trimIndent()
        )
        
        val indexNames = mutableListOf<String>()
        while (cursor.moveToNext()) {
            indexNames.add(cursor.getString(0))
        }
        
        // Should have idx_saved_location_name and idx_saved_location_last_used
        assert(indexNames.any { it.contains("name") }) { "Missing index on name column" }
        assert(indexNames.any { it.contains("last_used") }) { "Missing index on lastUsedAt column" }
        
        cursor.close()
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4_recognitionHistoryTableUnaffected() {
        // Create database at version 3
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            execSQL(
                """
                INSERT INTO recognition_history (id, category, confidence, timestamp, verbosityMode, detailText, positionText, distanceText)
                VALUES (1, 'person', 0.95, 1234567890, 'standard', 'Person detected', 'center', 'close by')
                """.trimIndent()
            )
            close()
        }
        
        // Run migration to version 4
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true)
        
        // Verify recognition_history data is preserved
        val cursor = db.query("SELECT * FROM recognition_history WHERE id = 1")
        
        assert(cursor.moveToFirst()) { "Recognition history row not found" }
        
        val categoryIndex = cursor.getColumnIndex("category")
        val confidenceIndex = cursor.getColumnIndex("confidence")
        
        assertEquals("person", cursor.getString(categoryIndex))
        assertEquals(0.95f, cursor.getFloat(confidenceIndex), 0.001f)
        
        cursor.close()
        db.close()
    }
}
