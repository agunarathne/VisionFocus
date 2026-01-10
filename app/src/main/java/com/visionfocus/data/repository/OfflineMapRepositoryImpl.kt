package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.maps.MapboxOfflineManager
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
    private val mapboxOfflineManager: MapboxOfflineManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OfflineMapRepository {
    
    override suspend fun downloadOfflineMap(
        locationId: Long,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radiusMeters: Int
    ): Flow<DownloadProgress> {
        return mapboxOfflineManager.downloadOfflineMap(
            locationId = locationId,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            radiusMeters = radiusMeters
        )
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
                val map = offlineMapDao.getOfflineMapByLocationId(locationId)
                if (map != null) {
                    // Delete from Mapbox offline manager
                    mapboxOfflineManager.deleteOfflineRegion(map.mapboxRegionId)
                    
                    // Delete from database
                    offlineMapDao.deleteOfflineMap(locationId)
                    
                    Timber.i("Successfully deleted offline map for location $locationId")
                    Result.success(Unit)
                } else {
                    Timber.w("No offline map found for location $locationId")
                    Result.failure(Exception("Offline map not found"))
                }
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
