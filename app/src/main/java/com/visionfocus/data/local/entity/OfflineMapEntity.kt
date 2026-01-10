package com.visionfocus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Story 7.4: Offline Map Pre-Caching
 * 
 * Entity representing offline map data for saved locations.
 * 
 * Architecture:
 * - 1:1 relationship with SavedLocationEntity
 * - CASCADE delete when parent location is deleted
 * - Tracks download status, expiration, and storage metadata
 * - Mapbox offline regions identified by mapboxRegionId
 * 
 * Privacy: Map tiles are NOT sensitive (public data), but locationId reference
 * links to encrypted SavedLocationEntity.
 */
@Entity(
    tableName = "offline_maps",
    foreignKeys = [
        ForeignKey(
            entity = SavedLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE  // Delete offline map when location deleted
        )
    ],
    indices = [
        Index("locationId"),
        Index("status"),
        Index("expiresAt")
    ]
)
data class OfflineMapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Foreign key to SavedLocationEntity */
    val locationId: Long,
    
    /** User-friendly name for the offline region (matches location name) */
    val regionName: String,
    
    /** Center latitude of the offline region */
    val centerLat: Double,
    
    /** Center longitude of the offline region */
    val centerLng: Double,
    
    /** Radius in meters of the offline region (default: 2000m = 2km) */
    val radiusMeters: Int,
    
    /** Timestamp when download completed (milliseconds since epoch) */
    val downloadedAt: Long,
    
    /** Timestamp when offline maps expire (30 days from download, Mapbox limit) */
    val expiresAt: Long,
    
    /** Size of downloaded map data in bytes */
    val sizeBytes: Long,
    
    /** Current status: NONE, DOWNLOADING, AVAILABLE, EXPIRED, ERROR */
    val status: String,
    
    /** Mapbox offline region ID for management operations */
    val mapboxRegionId: Long,
    
    /** Optional error message if status == ERROR */
    val errorMessage: String? = null
) {
    companion object {
        const val STATUS_NONE = "NONE"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_AVAILABLE = "AVAILABLE"
        const val STATUS_EXPIRED = "EXPIRED"
        const val STATUS_ERROR = "ERROR"
        
        /** Default radius for offline regions (2km = ~12.6 sq km = ~126 MB) */
        const val DEFAULT_RADIUS_METERS = 2000
        
        /** Mapbox offline regions expire after 30 days */
        const val EXPIRATION_DAYS = 30L
        
        /** Warn user when maps expire within this threshold */
        const val EXPIRATION_WARNING_DAYS = 5L
    }
    
    /**
     * Check if offline map is currently usable for navigation
     */
    fun isAvailable(): Boolean {
        return status == STATUS_AVAILABLE && expiresAt > System.currentTimeMillis()
    }
    
    /**
     * Check if offline map is expired
     */
    fun isExpired(): Boolean {
        return expiresAt <= System.currentTimeMillis()
    }
    
    /**
     * Check if offline map expires soon (within warning threshold)
     */
    fun expiresSoon(): Boolean {
        val warningThreshold = System.currentTimeMillis() + (EXPIRATION_WARNING_DAYS * 24 * 60 * 60 * 1000)
        return expiresAt in (System.currentTimeMillis() + 1)..warningThreshold
    }
    
    /**
     * Get human-readable expiration time remaining
     * Examples: "Expires in 5 days", "Expires tomorrow", "Expired 2 days ago"
     */
    fun getExpirationString(): String {
        val now = System.currentTimeMillis()
        val diff = expiresAt - now
        val days = diff / (24 * 60 * 60 * 1000)
        val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        
        return when {
            diff < 0 -> {
                val expiredDays = -days
                when (expiredDays) {
                    0L -> "Expired today"
                    1L -> "Expired yesterday"
                    else -> "Expired $expiredDays days ago"
                }
            }
            days == 0L -> {
                when {
                    hours == 0L -> "Expires in less than 1 hour"
                    hours == 1L -> "Expires in 1 hour"
                    else -> "Expires in $hours hours"
                }
            }
            days == 1L -> "Expires tomorrow"
            else -> "Expires in $days days"
        }
    }
    
    /**
     * Format size in bytes to human-readable string
     * Examples: "126 MB", "1.2 GB"
     */
    fun getFormattedSize(): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.0f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$sizeBytes bytes"
        }
    }
}
