package com.visionfocus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.visionfocus.data.local.entity.OfflineMapEntity
import kotlinx.coroutines.flow.Flow

/**
 * Story 7.4: Offline Map Pre-Caching
 * 
 * DAO for offline map database operations.
 * 
 * Design:
 * - Reactive queries using Flow for UI observation
 * - REPLACE strategy for upsert operations (update or insert)
 * - Expiration queries for periodic cleanup and warnings
 */
@Dao
interface OfflineMapDao {
    
    /**
     * Get offline map for a specific saved location.
     * Returns Flow for reactive UI updates.
     */
    @Query("SELECT * FROM offline_maps WHERE locationId = :locationId LIMIT 1")
    fun getOfflineMapForLocation(locationId: Long): Flow<OfflineMapEntity?>
    
    /**
     * Get offline map for a specific saved location (suspend version)
     */
    @Query("SELECT * FROM offline_maps WHERE locationId = :locationId LIMIT 1")
    suspend fun getOfflineMapByLocationId(locationId: Long): OfflineMapEntity?
    
    /**
     * Get offline map by ID (for direct lookups)
     */
    @Query("SELECT * FROM offline_maps WHERE id = :id LIMIT 1")
    suspend fun getOfflineMapById(id: Long): OfflineMapEntity?
    
    /**
     * Get offline map by Mapbox region ID
     */
    @Query("SELECT * FROM offline_maps WHERE mapboxRegionId = :mapboxRegionId LIMIT 1")
    suspend fun getOfflineMapByRegionId(mapboxRegionId: Long): OfflineMapEntity?
    
    /**
     * Story 7.5: Get offline map by coordinates
     * Checks if coordinates fall within any cached offline map region
     * Uses simple bounding box check (centerLat ± radius, centerLng ± radius)
     */
    @Query("""
        SELECT * FROM offline_maps 
        WHERE status = :availableStatus
        AND expiresAt > :currentTime
        AND :latitude BETWEEN (centerLat - (radiusMeters / 111000.0)) 
                         AND (centerLat + (radiusMeters / 111000.0))
        AND :longitude BETWEEN (centerLng - (radiusMeters / (111000.0 * 0.8))) 
                          AND (centerLng + (radiusMeters / (111000.0 * 0.8)))
        LIMIT 1
    """)
    suspend fun getOfflineMapByCoordinates(
        latitude: Double,
        longitude: Double,
        availableStatus: String = OfflineMapEntity.STATUS_AVAILABLE,
        currentTime: Long = System.currentTimeMillis()
    ): OfflineMapEntity?
    
    /**
     * Get all offline maps
     */
    @Query("SELECT * FROM offline_maps ORDER BY downloadedAt DESC")
    fun getAllOfflineMaps(): Flow<List<OfflineMapEntity>>
    
    /**
     * Get all offline maps (suspend version for direct access)
     */
    @Query("SELECT * FROM offline_maps ORDER BY downloadedAt DESC")
    suspend fun getAllOfflineMapsList(): List<OfflineMapEntity>
    
    /**
     * Get all available (not expired) offline maps
     */
    @Query("""
        SELECT * FROM offline_maps 
        WHERE status = :availableStatus 
        AND expiresAt > :currentTime
        ORDER BY downloadedAt DESC
    """)
    fun getAvailableOfflineMaps(
        availableStatus: String = OfflineMapEntity.STATUS_AVAILABLE,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<OfflineMapEntity>>
    
    /**
     * Get expired offline maps for cleanup or update prompts
     */
    @Query("""
        SELECT * FROM offline_maps 
        WHERE expiresAt <= :currentTime
        ORDER BY expiresAt ASC
    """)
    fun getExpiredMaps(
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<OfflineMapEntity>>
    
    /**
     * Get offline maps expiring soon (within warning threshold)
     */
    @Query("""
        SELECT * FROM offline_maps 
        WHERE status = :availableStatus
        AND expiresAt > :currentTime 
        AND expiresAt <= :warningThreshold
        ORDER BY expiresAt ASC
    """)
    fun getMapsExpiringSoon(
        availableStatus: String = OfflineMapEntity.STATUS_AVAILABLE,
        currentTime: Long = System.currentTimeMillis(),
        warningThreshold: Long = currentTime + (OfflineMapEntity.EXPIRATION_WARNING_DAYS * 24 * 60 * 60 * 1000)
    ): Flow<List<OfflineMapEntity>>
    
    /**
     * Get offline maps by status (for filtering in UI)
     */
    @Query("SELECT * FROM offline_maps WHERE status = :status ORDER BY downloadedAt DESC")
    fun getOfflineMapsByStatus(status: String): Flow<List<OfflineMapEntity>>
    
    /**
     * Insert new offline map entry.
     * Use REPLACE strategy for upsert behavior.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineMap(map: OfflineMapEntity): Long
    
    /**
     * Update existing offline map entry
     */
    @Update
    suspend fun updateOfflineMap(map: OfflineMapEntity)
    
    /**
     * Update offline map status (for progress tracking)
     */
    @Query("UPDATE offline_maps SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
    
    /**
     * Update offline map status with error message
     */
    @Query("UPDATE offline_maps SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatusWithError(id: Long, status: String, errorMessage: String)
    
    /**
     * Update download progress metadata
     */
    @Query("""
        UPDATE offline_maps 
        SET sizeBytes = :sizeBytes, 
            status = :status 
        WHERE id = :id
    """)
    suspend fun updateDownloadProgress(id: Long, sizeBytes: Long, status: String)
    
    /**
     * Delete offline map for specific location
     */
    @Query("DELETE FROM offline_maps WHERE locationId = :locationId")
    suspend fun deleteOfflineMap(locationId: Long)
    
    /**
     * Delete offline map by ID
     */
    @Query("DELETE FROM offline_maps WHERE id = :id")
    suspend fun deleteOfflineMapById(id: Long)
    
    /**
     * Delete offline map by Mapbox region ID
     */
    @Query("DELETE FROM offline_maps WHERE mapboxRegionId = :mapboxRegionId")
    suspend fun deleteOfflineMapByRegionId(mapboxRegionId: Long)
    
    /**
     * Delete all expired offline maps (for bulk cleanup)
     */
    @Query("DELETE FROM offline_maps WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredMaps(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Get total storage used by offline maps
     */
    @Query("SELECT SUM(sizeBytes) FROM offline_maps WHERE status = :availableStatus")
    suspend fun getTotalStorageUsed(availableStatus: String = OfflineMapEntity.STATUS_AVAILABLE): Long?
    
    /**
     * Count total offline maps
     */
    @Query("SELECT COUNT(*) FROM offline_maps")
    suspend fun getOfflineMapCount(): Int
    
    /**
     * Count available (not expired) offline maps
     */
    @Query("""
        SELECT COUNT(*) FROM offline_maps 
        WHERE status = :availableStatus 
        AND expiresAt > :currentTime
    """)
    suspend fun getAvailableOfflineMapCount(
        availableStatus: String = OfflineMapEntity.STATUS_AVAILABLE,
        currentTime: Long = System.currentTimeMillis()
    ): Int
}
