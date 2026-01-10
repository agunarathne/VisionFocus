package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.maps.MapboxOfflineManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Story 7.4: Unit tests for OfflineMapRepository
 * Tests repository pattern implementation and data layer coordination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineMapRepositoryTest {
    
    private lateinit var repository: OfflineMapRepository
    private lateinit var offlineMapDao: OfflineMapDao
    private lateinit var mapboxOfflineManager: MapboxOfflineManager
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        offlineMapDao = mockk(relaxed = true)
        mapboxOfflineManager = mockk(relaxed = true)
        
        repository = OfflineMapRepositoryImpl(
            offlineMapDao = offlineMapDao,
            mapboxOfflineManager = mapboxOfflineManager,
            ioDispatcher = testDispatcher
        )
    }
    
    @Test
    fun `getOfflineMapByLocationId returns map when exists`() = runTest(testDispatcher) {
        // Given
        val locationId = 1L
        val expectedMap = createTestOfflineMap(locationId = locationId)
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns expectedMap
        
        // When
        val result = repository.getOfflineMapByLocationId(locationId)
        
        // Then
        assertNotNull(result)
        assertEquals(locationId, result?.locationId)
        assertEquals(OfflineMapEntity.STATUS_AVAILABLE, result?.status)
    }
    
    @Test
    fun `getOfflineMapByLocationId returns null when not exists`() = runTest(testDispatcher) {
        // Given
        val locationId = 99L
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns null
        
        // When
        val result = repository.getOfflineMapByLocationId(locationId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `isOfflineMapAvailable returns true for valid available map`() = runTest(testDispatcher) {
        // Given
        val locationId = 1L
        val futureExpiry = System.currentTimeMillis() + 86400000L // +1 day
        val availableMap = createTestOfflineMap(
            locationId = locationId,
            status = OfflineMapEntity.STATUS_AVAILABLE,
            expiresAt = futureExpiry
        )
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns availableMap
        
        // When
        val result = repository.isOfflineMapAvailable(locationId)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isOfflineMapAvailable returns false for expired map`() = runTest(testDispatcher) {
        // Given
        val locationId = 1L
        val pastExpiry = System.currentTimeMillis() - 86400000L // -1 day (expired)
        val expiredMap = createTestOfflineMap(
            locationId = locationId,
            status = OfflineMapEntity.STATUS_AVAILABLE,
            expiresAt = pastExpiry
        )
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns expiredMap
        
        // When
        val result = repository.isOfflineMapAvailable(locationId)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isOfflineMapAvailable returns false when map downloading`() = runTest(testDispatcher) {
        // Given
        val locationId = 1L
        val downloadingMap = createTestOfflineMap(
            locationId = locationId,
            status = OfflineMapEntity.STATUS_DOWNLOADING
        )
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns downloadingMap
        
        // When
        val result = repository.isOfflineMapAvailable(locationId)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `deleteOfflineMap removes map and deletes Mapbox region`() = runTest(testDispatcher) {
        // Given
        val locationId = 1L
        val mapboxRegionId = 12345L
        val existingMap = createTestOfflineMap(
            locationId = locationId,
            mapboxRegionId = mapboxRegionId
        )
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns existingMap
        coEvery { mapboxOfflineManager.deleteOfflineRegion(mapboxRegionId) } returns Unit
        coEvery { offlineMapDao.deleteOfflineMap(locationId) } returns Unit
        
        // When
        val result = repository.deleteOfflineMap(locationId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { mapboxOfflineManager.deleteOfflineRegion(mapboxRegionId) }
        coVerify { offlineMapDao.deleteOfflineMap(locationId) }
    }
    
    @Test
    fun `deleteOfflineMap returns failure when map not found`() = runTest(testDispatcher) {
        // Given
        val locationId = 99L
        coEvery { offlineMapDao.getOfflineMapByLocationId(locationId) } returns null
        
        // When
        val result = repository.deleteOfflineMap(locationId)
        
        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { mapboxOfflineManager.deleteOfflineRegion(any()) }
    }
    
    @Test
    fun `getTotalStorageUsed returns sum of all map sizes`() = runTest(testDispatcher) {
        // Given
        val totalSize = 500_000_000L // 500 MB
        coEvery { offlineMapDao.getTotalStorageUsed() } returns totalSize
        
        // When
        val result = repository.getTotalStorageUsed()
        
        // Then
        assertEquals(totalSize, result)
    }
    
    @Test
    fun `getTotalStorageUsed returns zero when no maps exist`() = runTest(testDispatcher) {
        // Given
        coEvery { offlineMapDao.getTotalStorageUsed() } returns null
        
        // When
        val result = repository.getTotalStorageUsed()
        
        // Then
        assertEquals(0L, result)
    }
    
    @Test
    fun `getExpiredMaps returns Flow of expired maps`() = runTest(testDispatcher) {
        // Given
        val pastExpiry = System.currentTimeMillis() - 86400000L
        val expiredMaps = listOf(
            createTestOfflineMap(locationId = 1L, expiresAt = pastExpiry),
            createTestOfflineMap(locationId = 2L, expiresAt = pastExpiry)
        )
        coEvery { offlineMapDao.getExpiredMaps() } returns flowOf(expiredMaps)
        
        // When
        val result = repository.getExpiredMaps()
        
        // Then
        result.collect { maps ->
            assertEquals(2, maps.size)
            assertTrue(maps.all { it.isExpired() })
        }
    }
    
    private fun createTestOfflineMap(
        id: Long = 1L,
        locationId: Long = 1L,
        regionName: String = "Test Location",
        centerLat: Double = 51.5,
        centerLng: Double = -0.1,
        radiusMeters: Int = 2000,
        downloadedAt: Long = System.currentTimeMillis(),
        expiresAt: Long = System.currentTimeMillis() + 2592000000L, // +30 days
        sizeBytes: Long = 126_000_000L, // ~126 MB
        status: String = OfflineMapEntity.STATUS_AVAILABLE,
        mapboxRegionId: Long = 12345L
    ): OfflineMapEntity {
        return OfflineMapEntity(
            id = id,
            locationId = locationId,
            regionName = regionName,
            centerLat = centerLat,
            centerLng = centerLng,
            radiusMeters = radiusMeters,
            downloadedAt = downloadedAt,
            expiresAt = expiresAt,
            sizeBytes = sizeBytes,
            status = status,
            mapboxRegionId = mapboxRegionId
        )
    }
}
