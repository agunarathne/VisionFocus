package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.SavedLocationDao
import com.visionfocus.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SavedLocationRepositoryImpl.
 * 
 * Story 7.1 Task 11: Repository unit tests
 * 
 * Tests repository logic with mocked DAO to verify:
 * - Input validation (name length, GPS coordinates)
 * - Duplicate name detection
 * - Correct delegation to DAO methods
 * - Error handling and Result types
 * - Timestamp generation during save
 */
class SavedLocationRepositoryTest {
    
    @Mock
    private lateinit var mockDao: SavedLocationDao
    
    private lateinit var repository: SavedLocationRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SavedLocationRepositoryImpl(mockDao)
    }
    
    // ========== saveLocation() tests ==========
    
    @Test
    fun `saveLocation with valid input returns success`() = runTest {
        // Given
        val name = "Home"
        val latitude = 51.5074
        val longitude = -0.1278
        val expectedId = 42L
        
        `when`(mockDao.findByName(name)).thenReturn(null)
        `when`(mockDao.insert(any())).thenReturn(expectedId)
        
        // When
        val result = repository.saveLocation(name, latitude, longitude)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        
        val captor = argumentCaptor<SavedLocationEntity>()
        verify(mockDao).insert(captor.capture())
        
        val entity = captor.firstValue
        assertEquals(name, entity.name)
        assertEquals(latitude, entity.latitude)
        assertEquals(longitude, entity.longitude)
        assertTrue(entity.createdAt > 0)
        assertEquals(entity.createdAt, entity.lastUsedAt)
    }
    
    @Test
    fun `saveLocation trims whitespace from name`() = runTest {
        // Given
        val nameWithSpaces = "  Work  "
        val trimmedName = "Work"
        val latitude = 51.5074
        val longitude = -0.1278
        
        `when`(mockDao.findByName(trimmedName)).thenReturn(null)
        `when`(mockDao.insert(any())).thenReturn(1L)
        
        // When
        val result = repository.saveLocation(nameWithSpaces, latitude, longitude)
        
        // Then
        assertTrue(result.isSuccess)
        
        val captor = argumentCaptor<SavedLocationEntity>()
        verify(mockDao).insert(captor.capture())
        assertEquals(trimmedName, captor.firstValue.name)
    }
    
    @Test
    fun `saveLocation with empty name returns failure`() = runTest {
        // Given
        val name = ""
        val latitude = 51.5074
        val longitude = -0.1278
        
        // When
        val result = repository.saveLocation(name, latitude, longitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        
        // Verify DAO not called
        verify(mockDao, never()).insert(any())
    }
    
    @Test
    fun `saveLocation with 1 character name returns failure`() = runTest {
        // Given
        val name = "A"
        val latitude = 51.5074
        val longitude = -0.1278
        
        // When
        val result = repository.saveLocation(name, latitude, longitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull()?.message?.contains("at least 2") == true)
        
        verify(mockDao, never()).insert(any())
    }
    
    @Test
    fun `saveLocation with duplicate name returns failure`() = runTest {
        // Given
        val name = "Home"
        val latitude = 51.5074
        val longitude = -0.1278
        
        val existingLocation = SavedLocationEntity(
            id = 1L,
            name = name,
            latitude = 52.0,
            longitude = -1.0,
            createdAt = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis()
        )
        
        `when`(mockDao.findByName(name)).thenReturn(existingLocation)
        
        // When
        val result = repository.saveLocation(name, latitude, longitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<DuplicateLocationException>(result.exceptionOrNull())
        assertEquals(name, (result.exceptionOrNull() as DuplicateLocationException).locationName)
        
        verify(mockDao, never()).insert(any())
    }
    
    @Test
    fun `saveLocation with invalid latitude returns failure`() = runTest {
        // Given
        val name = "Home"
        val invalidLatitude = 91.0  // Valid range: -90 to 90
        val longitude = -0.1278
        
        // When
        val result = repository.saveLocation(name, invalidLatitude, longitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull()?.message?.contains("Latitude") == true)
        
        verify(mockDao, never()).insert(any())
    }
    
    @Test
    fun `saveLocation with invalid longitude returns failure`() = runTest {
        // Given
        val name = "Home"
        val latitude = 51.5074
        val invalidLongitude = 181.0  // Valid range: -180 to 180
        
        // When
        val result = repository.saveLocation(name, latitude, invalidLongitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull()?.message?.contains("Longitude") == true)
        
        verify(mockDao, never()).insert(any())
    }
    
    @Test
    fun `saveLocation with DAO exception returns failure`() = runTest {
        // Given
        val name = "Home"
        val latitude = 51.5074
        val longitude = -0.1278
        
        `when`(mockDao.findByName(name)).thenReturn(null)
        `when`(mockDao.insert(any())).thenThrow(RuntimeException("Database error"))
        
        // When
        val result = repository.saveLocation(name, latitude, longitude)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<SavedLocationException>(result.exceptionOrNull())
    }
    
    // ========== getAllLocationsSorted() tests ==========
    
    @Test
    fun `getAllLocationsSorted returns Flow from DAO`() = runTest {
        // Given
        val location1 = SavedLocationEntity(
            id = 1L,
            name = "Home",
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        val location2 = SavedLocationEntity(
            id = 2L,
            name = "Work",
            latitude = 51.5155,
            longitude = -0.1426,
            createdAt = 1500L,
            lastUsedAt = 2500L
        )
        
        val mockFlow = flowOf(listOf(location1, location2))
        `when`(mockDao.getAllLocationsSorted()).thenReturn(mockFlow)
        
        // When
        val result = repository.getAllLocationsSorted()
        
        // Then
        assertEquals(mockFlow, result)
        verify(mockDao).getAllLocationsSorted()
    }
    
    // ========== deleteLocation() tests ==========
    
    @Test
    fun `deleteLocation with valid location returns success`() = runTest {
        // Given
        val location = SavedLocationEntity(
            id = 1L,
            name = "Home",
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        
        // When
        val result = repository.deleteLocation(location)
        
        // Then
        assertTrue(result.isSuccess)
        verify(mockDao).delete(location)
    }
    
    @Test
    fun `deleteLocation with DAO exception returns failure`() = runTest {
        // Given
        val location = SavedLocationEntity(
            id = 1L,
            name = "Home",
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        
        `when`(mockDao.delete(location)).thenThrow(RuntimeException("Database error"))
        
        // When
        val result = repository.deleteLocation(location)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<SavedLocationException>(result.exceptionOrNull())
    }
    
    // ========== updateLocation() tests ==========
    
    @Test
    fun `updateLocation with valid location returns success`() = runTest {
        // Given
        val location = SavedLocationEntity(
            id = 1L,
            name = "Updated Home",
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 3000L
        )
        
        // When
        val result = repository.updateLocation(location)
        
        // Then
        assertTrue(result.isSuccess)
        verify(mockDao).update(location)
    }
    
    @Test
    fun `updateLocation with short name returns failure`() = runTest {
        // Given
        val location = SavedLocationEntity(
            id = 1L,
            name = "A",  // Too short
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        
        // When
        val result = repository.updateLocation(location)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        
        verify(mockDao, never()).update(any())
    }
    
    @Test
    fun `updateLocation with invalid coordinates returns failure`() = runTest {
        // Given
        val location = SavedLocationEntity(
            id = 1L,
            name = "Home",
            latitude = 200.0,  // Invalid
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        
        // When
        val result = repository.updateLocation(location)
        
        // Then
        assertFalse(result.isSuccess)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        
        verify(mockDao, never()).update(any())
    }
    
    // ========== findLocationByName() tests ==========
    
    @Test
    fun `findLocationByName with existing location returns entity`() = runTest {
        // Given
        val name = "Home"
        val expectedLocation = SavedLocationEntity(
            id = 1L,
            name = name,
            latitude = 51.5074,
            longitude = -0.1278,
            createdAt = 1000L,
            lastUsedAt = 2000L
        )
        
        `when`(mockDao.findByName(name)).thenReturn(expectedLocation)
        
        // When
        val result = repository.findLocationByName(name)
        
        // Then
        assertNotNull(result)
        assertEquals(expectedLocation, result)
        verify(mockDao).findByName(name)
    }
    
    @Test
    fun `findLocationByName with non-existent location returns null`() = runTest {
        // Given
        val name = "NonExistent"
        
        `when`(mockDao.findByName(name)).thenReturn(null)
        
        // When
        val result = repository.findLocationByName(name)
        
        // Then
        assertNull(result)
        verify(mockDao).findByName(name)
    }
    
    @Test
    fun `findLocationByName with DAO exception returns null`() = runTest {
        // Given
        val name = "Home"
        
        `when`(mockDao.findByName(name)).thenThrow(RuntimeException("Database error"))
        
        // When
        val result = repository.findLocationByName(name)
        
        // Then
        assertNull(result)
    }
}
