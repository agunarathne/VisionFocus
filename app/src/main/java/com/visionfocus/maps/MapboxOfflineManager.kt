package com.visionfocus.maps

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.OfflineRegion
import com.mapbox.maps.OfflineRegionGeometryDefinition
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.StylePackLoadOptions
import com.mapbox.maps.TileRegionLoadOptions
import com.mapbox.maps.TileStore
import com.visionfocus.BuildConfig
import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.local.entity.OfflineMapEntity
import com.visionfocus.navigation.offline.DownloadProgress
import com.visionfocus.util.NetworkConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.cos

/**
 * Story 7.4: MapboxOfflineManager
 * Real Mapbox SDK integration for offline map downloads
 * 
 * Responsibilities:
 * - Download map tiles using Mapbox Offline API
 * - Calculate bounding boxes for regions
 * - Estimate download sizes
 * - Track download progress
 * - Manage offline regions
 */
@Singleton
class MapboxOfflineManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val offlineMapDao: OfflineMapDao,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) {
    
    companion object {
        private const val MIN_ZOOM = 10.0  // City-level
        private const val MAX_ZOOM = 16.0  // Street-level
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val MB_PER_SQ_KM = 10.0  // Approximate for zoom 10-16
        
        // Mapbox style URL
        private const val STYLE_URL = "mapbox://styles/mapbox/streets-v12"
    }
    
    private val resourceOptions: ResourceOptions by lazy {
        ResourceOptions.Builder()
            .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
            .build()
    }
    
    private val tileStore: TileStore by lazy {
        TileStore.create()
    }
    
    /**
     * Download offline map for a saved location
     * 
     * @param locationId Saved location ID
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param locationName Name of location for display
     * @param radiusMeters Radius around location (default 2km)
     * @return Flow emitting download progress
     */
    suspend fun downloadOfflineMap(
        locationId: Long,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radiusMeters: Int = OfflineMapEntity.DEFAULT_RADIUS_METERS
    ): Flow<DownloadProgress> = callbackFlow {
        try {
            // Emit preparing state
            trySend(DownloadProgress.Preparing)
            
            // Check WiFi recommendation
            if (!networkConnectivityObserver.isWifiConnected()) {
                Timber.w("Downloading offline map without WiFi connection")
            }
            
            // Calculate bounding box
            val bounds = calculateBoundingBox(latitude, longitude, radiusMeters)
            val estimatedSize = estimateDownloadSize(bounds)
            
            Timber.d("Downloading offline map: lat=$latitude, lng=$longitude, radius=$radiusMeters")
            Timber.d("Bounding box: ${bounds.southwest} to ${bounds.northeast}")
            Timber.d("Estimated size: ${formatBytes(estimatedSize)}")
            
            // Create tile region definition
            val tileRegionId = "offline_${locationId}_${System.currentTimeMillis()}"
            val geometry = OfflineRegionGeometryDefinition.Builder()
                .geometry(bounds.toGeoJsonPolygon())
                .minZoom(MIN_ZOOM.toInt())
                .maxZoom(MAX_ZOOM.toInt())
                .pixelRatio(context.resources.displayMetrics.density)
                .build()
            
            // Start download
            val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
                .geometry(geometry.geometry)
                .descriptorsOptions(listOf(
                    com.mapbox.maps.TilesetDescriptorOptions.Builder()
                        .styleURI(STYLE_URL)
                        .minZoom(MIN_ZOOM.toByte())
                        .maxZoom(MAX_ZOOM.toByte())
                        .build()
                ))
                .acceptExpired(false)
                .networkRestriction(com.mapbox.maps.NetworkRestriction.NONE)
                .build()
            
            tileStore.loadTileRegion(
                tileRegionId,
                tileRegionLoadOptions,
                { progress ->
                    // Calculate progress
                    val completedResources = progress.completedResourceCount
                    val totalResources = progress.requiredResourceCount
                    val percent = if (totalResources > 0) {
                        ((completedResources.toFloat() / totalResources) * 100).toInt()
                    } else 0
                    
                    val bytesDownloaded = progress.completedResourceSize
                    
                    Timber.d("Download progress: $percent% ($completedResources/$totalResources resources)")
                    
                    trySend(DownloadProgress.Downloading(
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = estimatedSize,
                        percent = percent,
                        completedTiles = completedResources,
                        totalTiles = totalResources
                    ))
                },
                { result ->
                    result.fold(
                        onSuccess = { tileRegion ->
                            // Calculate actual size
                            val actualSize = tileRegion.completedResourceSize
                            
                            Timber.i("Offline map download completed: $tileRegionId, size=${formatBytes(actualSize)}")
                            
                            // Save to database
                            val expiresAt = System.currentTimeMillis() + 
                                TimeUnit.DAYS.toMillis(OfflineMapEntity.EXPIRATION_DAYS)
                            
                            val offlineMap = OfflineMapEntity(
                                locationId = locationId,
                                regionName = locationName,
                                centerLat = latitude,
                                centerLng = longitude,
                                radiusMeters = radiusMeters,
                                downloadedAt = System.currentTimeMillis(),
                                expiresAt = expiresAt,
                                sizeBytes = actualSize,
                                status = OfflineMapEntity.STATUS_AVAILABLE,
                                mapboxRegionId = tileRegionId.hashCode().toLong()
                            )
                            
                            kotlinx.coroutines.runBlocking {
                                offlineMapDao.insertOfflineMap(offlineMap)
                            }
                            
                            trySend(DownloadProgress.Complete(
                                sizeBytes = actualSize,
                                regionId = offlineMap.mapboxRegionId,
                                regionName = locationName
                            ))
                            close()
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to download offline map")
                            trySend(DownloadProgress.Error(
                                message = error.message ?: "Download failed",
                                isCancelled = false,
                                retryable = true
                            ))
                            close(error)
                        }
                    )
                }
            )
            
            awaitClose {
                Timber.d("Download flow closed for $tileRegionId")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to download offline map")
            trySend(DownloadProgress.Error(
                message = e.message ?: "Download failed",
                isCancelled = false,
                retryable = true
            ))
            close(e)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete offline region by Mapbox region ID
     */
    suspend fun deleteOfflineRegion(regionId: Long) = withContext(Dispatchers.IO) {
        try {
            val tileRegionId = "offline_${regionId}_*"
            // Remove tile region from Mapbox TileStore
            // Note: Mapbox v10+ uses TileStore.removeTileRegion()
            tileStore.removeTileRegion(tileRegionId)
            Timber.i("Deleted Mapbox offline region: $tileRegionId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete Mapbox offline region: $regionId")
            throw e
        }
    }
    
    /**
     * Calculate bounding box around a center point
     * 
     * @param centerLat Center latitude
     * @param centerLng Center longitude  
     * @param radiusMeters Radius in meters
     * @return Bounding box coordinates
     */
    private fun calculateBoundingBox(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int
    ): BoundingBox {
        // Convert radius to degrees (approximate)
        val latOffset = Math.toDegrees(radiusMeters / EARTH_RADIUS_METERS)
        val lngOffset = Math.toDegrees(radiusMeters / 
            (EARTH_RADIUS_METERS * cos(Math.toRadians(centerLat))))
        
        return BoundingBox(
            southwest = LatLng(centerLat - latOffset, centerLng - lngOffset),
            northeast = LatLng(centerLat + latOffset, centerLng + lngOffset)
        )
    }
    
    /**
     * Estimate download size based on bounding box area
     * 
     * @param bounds Bounding box
     * @return Estimated size in bytes
     */
    private fun estimateDownloadSize(bounds: BoundingBox): Long {
        // Calculate area in square kilometers
        val latDiff = bounds.northeast.latitude - bounds.southwest.latitude
        val lngDiff = bounds.northeast.longitude - bounds.southwest.longitude
        
        val avgLat = (bounds.northeast.latitude + bounds.southwest.latitude) / 2
        val areaSqKm = (latDiff * 111.0) * (lngDiff * 111.0 * cos(Math.toRadians(avgLat)))
        
        // Estimate: ~10 MB per square km for zoom levels 10-16
        return (areaSqKm * MB_PER_SQ_KM * 1024 * 1024).toLong()
    }
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.0f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes bytes"
        }
    }
    
    /**
     * Bounding box data class
     */
    data class BoundingBox(
        val southwest: LatLng,
        val northeast: LatLng
    ) {
        fun toGeoJsonPolygon(): String {
            // Create GeoJSON polygon from bounding box
            return """
                {
                    "type": "Polygon",
                    "coordinates": [[
                        [${southwest.longitude}, ${southwest.latitude}],
                        [${northeast.longitude}, ${southwest.latitude}],
                        [${northeast.longitude}, ${northeast.latitude}],
                        [${southwest.longitude}, ${northeast.latitude}],
                        [${southwest.longitude}, ${southwest.latitude}]
                    ]]
                }
            """.trimIndent()
        }
    }
    
    /**
     * LatLng data class
     */
    data class LatLng(
        val latitude: Double,
        val longitude: Double
    )
}
