package com.visionfocus.maps

import android.content.Context
import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.util.NetworkConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 7.4: OfflineMapManager
 * Manages offline map downloads using Mapbox SDK
 * 
 * Responsibilities:
 * - Download map regions for saved locations
 * - Store map data locally
 * - Track download progress
 * - Manage map expiration (30 days)
 * - Check WiFi connectivity before downloads
 * - Integrate with Room database for persistence
 */
@Singleton
class OfflineMapManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val offlineMapDao: OfflineMapDao,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) {
    
    companion object {
        private const val OFFLINE_MAP_RADIUS_METERS = 1000.0 // 1km radius
        private const val MAPBOX_TILE_STORE_PATH = "mapbox_offline_tiles"
    }
    
    /**
     * Download offline map for a saved location
     * 
     * @param locationId Saved location ID
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param locationName Name of location for display
     * @return Flow emitting download progress
     */
    suspend fun downloadOfflineMap(
        locationId: Long,
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Flow<DownloadProgress> = flow {
        try {
            emit(DownloadProgress.Idle)
            
            // Check if WiFi is connected (recommended but not required)
            if (!networkConnectivityObserver.isWifiConnected()) {
                Timber.w("Downloading offline map without WiFi connection")
            }
            
            // Check if map already exists
            val existing = offlineMapDao.getOfflineMapByLocationId(locationId)
            if (existing != null) {
                Timber.w("Offline map already exists for location $locationId")
                emit(DownloadProgress.Completed)
                return@flow
            }
            
            // Generate region name based on location
            val regionName = generateRegionName(locationId, locationName)
            
            // Simulate download progress (Replace with actual Mapbox SDK integration)
            // In production, this would use: OfflineManager.createOfflineRegion()
            emit(DownloadProgress.Downloading(0f, 0L, 50_000_000L))
            
            // TODO: Integrate actual Mapbox SDK offline download
            // Example (pseudo-code):
            // val styleUrl = Style.MAPBOX_STREETS
            // val bounds = calculateBounds(latitude, longitude, OFFLINE_MAP_RADIUS_METERS)
            // val definition = OfflineTilePyramidRegionDefinition(
            //     styleUrl, bounds, minZoom, maxZoom, pixelRatio
            // )
            // offlineManager.createOfflineRegion(definition, metadata) { region ->
            //     region.setDownloadState(OfflineRegion.STATE_ACTIVE)
            //     region.setObserver { status ->
            //         val progress = (status.completedResourceCount.toFloat() / 
            //                        status.requiredResourceCount) * 100f
            //         emit(DownloadProgress.Downloading(progress, 
            //              status.completedResourceSize, 
            //              status.completedResourceSize + status.requiredResourceSize))
            //     }
            // }
            
            // Simulate download progress for now
            for (percent in 10..100 step 10) {
                kotlinx.coroutines.delay(200) // Simulate download time
                val bytesDownloaded = (50_000_000L * percent) / 100
                emit(DownloadProgress.Downloading(
                    percent.toFloat(), 
                    bytesDownloaded, 
                    50_000_000L
                ))
            }
            
            // Save to database
            val expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
            val offlineMap = OfflineMapEntity(
                locationId = locationId,
                regionName = regionName,
                centerLat = latitude,
                centerLng = longitude,
                radiusMeters = OFFLINE_MAP_RADIUS_METERS.toInt(),
                downloadedAt = System.currentTimeMillis(),
                expiresAt = expiresAt,
                sizeBytes = 50_000_000L, // Approximate size
                status = OfflineMapEntity.STATUS_AVAILABLE,
                mapboxRegionId = 0L // Placeholder - in production would be from Mapbox SDK
            )
            
            offlineMapDao.insertOfflineMap(offlineMap)
            Timber.i("Successfully downloaded offline map for location $locationId")
            
            emit(DownloadProgress.Completed)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to download offline map")
            emit(DownloadProgress.Failed(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check if offline map exists for a location
     */
    suspend fun hasOfflineMap(locationId: Long): Boolean = withContext(Dispatchers.IO) {
        return@withContext offlineMapDao.getOfflineMapByLocationId(locationId) != null
    }
    
    /**
     * Get offline map for a location
     */
    suspend fun getOfflineMap(locationId: Long): OfflineMapEntity? = withContext(Dispatchers.IO) {
        return@withContext offlineMapDao.getOfflineMapByLocationId(locationId)
    }
    
    /**
     * Delete offline map for a location
     */
    suspend fun deleteOfflineMap(locationId: Long) = withContext(Dispatchers.IO) {
        val offlineMap = offlineMapDao.getOfflineMapByLocationId(locationId)
        if (offlineMap != null) {
            // Delete map file if exists
            val mapFile = File(context.filesDir, offlineMap.regionName)
            if (mapFile.exists()) {
                mapFile.delete()
                Timber.d("Deleted map file: ${offlineMap.regionName}")
            }
            
            // Delete from database
            offlineMapDao.deleteOfflineMap(locationId)
            Timber.i("Deleted offline map for location $locationId")
        } else {
            Timber.w("No offline map found for location $locationId")
        }
    }
    
    /**
     * Check if offline map is expired (older than 30 days)
     */
    suspend fun isMapExpired(locationId: Long): Boolean = withContext(Dispatchers.IO) {
        val offlineMap = offlineMapDao.getOfflineMapByLocationId(locationId) ?: return@withContext false
        val expirationThreshold = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        return@withContext offlineMap.downloadedAt < expirationThreshold
    }
    
    /**
     * Get all offline maps
     */
    suspend fun getAllOfflineMaps(): List<OfflineMapEntity> = withContext(Dispatchers.IO) {
        return@withContext offlineMapDao.getAllOfflineMapsList()
    }
    
    /**
     * Calculate total storage used by offline maps
     */
    suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        val allMaps = offlineMapDao.getAllOfflineMapsList()
        return@withContext allMaps.sumOf { it.sizeBytes }
    }
    
    /**
     * Generate region name for Mapbox offline region
     */
    private fun generateRegionName(locationId: Long, locationName: String): String {
        val sanitizedName = locationName.replace(Regex("[^A-Za-z0-9]"), "_")
        return "offline_map_${locationId}_${sanitizedName}_${System.currentTimeMillis()}"
    }
}
