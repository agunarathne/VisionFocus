package com.visionfocus.data.repository

import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.navigation.offline.DownloadProgress
import kotlinx.coroutines.flow.Flow

/**
 * Story 7.4: Offline Map Pre-Caching
 * 
 * Repository interface for offline map operations.
 * Abstracts data layer from business logic.
 */
interface OfflineMapRepository {
    
    /**
     * Download offline map for a location
     * @return Flow emitting download progress
     */
    suspend fun downloadOfflineMap(
        locationId: Long,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radiusMeters: Int = OfflineMapEntity.DEFAULT_RADIUS_METERS
    ): Flow<DownloadProgress>
    
    /**
     * Get offline map for a specific location (reactive)
     */
    fun getOfflineMapForLocation(locationId: Long): Flow<OfflineMapEntity?>
    
    /**
     * Get offline map for a specific location (suspend)
     */
    suspend fun getOfflineMapByLocationId(locationId: Long): OfflineMapEntity?
    
    /**
     * Get all offline maps (reactive)
     */
    fun getAllOfflineMaps(): Flow<List<OfflineMapEntity>>
    
    /**
     * Get expired offline maps
     */
    fun getExpiredMaps(): Flow<List<OfflineMapEntity>>
    
    /**
     * Get maps expiring soon (within warning threshold)
     */
    fun getMapsExpiringSoon(): Flow<List<OfflineMapEntity>>
    
    /**
     * Check if offline map is available for location
     */
    suspend fun isOfflineMapAvailable(locationId: Long): Boolean
    
    /**
     * Delete offline map for a location
     */
    suspend fun deleteOfflineMap(locationId: Long): Result<Unit>
    
    /**
     * Get total storage used by all offline maps
     */
    suspend fun getTotalStorageUsed(): Long
    
    /**
     * Update offline map status
     */
    suspend fun updateOfflineMapStatus(id: Long, status: String)
    
    /**
     * Insert or update offline map
     */
    suspend fun upsertOfflineMap(map: OfflineMapEntity): Long
}
