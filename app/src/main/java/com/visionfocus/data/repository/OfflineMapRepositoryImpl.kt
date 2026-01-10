package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.di.IODispatcher
import com.visionfocus.maps.GoogleMapsOfflineManager
import com.visionfocus.navigation.offline.DownloadProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 7.4: Offline Map Pre-Caching
 * 
 * Repository implementation for offline map operations.
 * Coordinates between DAO and Mapbox SDK.
 */
@Singleton
class OfflineMapRepositoryImpl @Inject constructor(
    private val offlineMapDao: OfflineMapDao,
    private val googleMapsOfflineManager: GoogleMapsOfflineManager,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : OfflineMapRepository {
    
    override suspend fun downloadOfflineMap(
        locationId: Long,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radiusMeters: Int
    ): Flow<DownloadProgress> {
        // Create a temporary SavedLocationEntity for the manager
        val tempLocation = SavedLocationEntity(
            id = locationId,
            name = locationName,
            latitude = latitude,
            longitude = longitude,
            address = null, // Not needed for offline map download
            createdAt = System.currentTimeMillis()
        )
        return googleMapsOfflineManager.downloadOfflineMap(tempLocation, radiusMeters)
    }
    
    override fun getOfflineMapForLocation(locationId: Long): Flow<OfflineMapEntity?> {
        return offlineMapDao.getOfflineMapForLocation(locationId)
    }
    
    override suspend fun getOfflineMapByLocationId(locationId: Long): OfflineMapEntity? =
        withContext(ioDispatcher) {
            try {
                offlineMapDao.getOfflineMapByLocationId(locationId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get offline map for location $locationId")
                null
            }
        }
    
    override fun getAllOfflineMaps(): Flow<List<OfflineMapEntity>> {
        return offlineMapDao.getAllOfflineMaps()
    }
    
    override fun getExpiredMaps(): Flow<List<OfflineMapEntity>> {
        return offlineMapDao.getExpiredMaps()
    }
    
    override fun getMapsExpiringSoon(): Flow<List<OfflineMapEntity>> {
        return offlineMapDao.getMapsExpiringSoon()
    }
    
    override suspend fun isOfflineMapAvailable(locationId: Long): Boolean =
        withContext(ioDispatcher) {
            try {
                val map = offlineMapDao.getOfflineMapByLocationId(locationId)
                map?.isAvailable() == true
            } catch (e: Exception) {
                Timber.e(e, "Failed to check offline map availability for location $locationId")
                false
            }
        }
    
    override suspend fun deleteOfflineMap(locationId: Long): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                // Use GoogleMapsOfflineManager to handle deletion
                googleMapsOfflineManager.deleteOfflineMap(locationId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete offline map for location $locationId")
                Result.failure(e)
            }
        }
    
    override suspend fun getTotalStorageUsed(): Long =
        withContext(ioDispatcher) {
            try {
                offlineMapDao.getTotalStorageUsed() ?: 0L
            } catch (e: Exception) {
                Timber.e(e, "Failed to calculate total storage used")
                0L
            }
        }
    
    override suspend fun updateOfflineMapStatus(id: Long, status: String) {
        withContext(ioDispatcher) {
            try {
                offlineMapDao.updateStatus(id, status)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update offline map status")
            }
        }
    }
    
    override suspend fun upsertOfflineMap(map: OfflineMapEntity): Long =
        withContext(ioDispatcher) {
            try {
                offlineMapDao.insertOfflineMap(map)
            } catch (e: Exception) {
                Timber.e(e, "Failed to upsert offline map")
                0L
            }
        }
}
