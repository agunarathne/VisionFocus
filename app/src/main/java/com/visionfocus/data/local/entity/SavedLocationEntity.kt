package com.visionfocus.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Room entity for saved locations storage.
 * 
 * Story 7.1: Complete schema with all columns for saved locations feature.
 * Story 7.3: Made Parcelable for disambiguation dialog arguments.
 * Encrypted at rest with SQLCipher (configured in Story 4.2).
 * 
 * Schema fields:
 * - id: Auto-generated primary key
 * - name: User-provided location name (e.g., "Home", "Work")
 * - latitude: GPS latitude coordinate
 * - longitude: GPS longitude coordinate
 * - createdAt: Timestamp when location was saved (milliseconds)
 * - lastUsedAt: Timestamp when location was last used for navigation (Story 7.2)
 * - address: Reverse geocoded address (optional, future enhancement)
 * 
 * Indexes:
 * - name: For duplicate check query performance (findByName)
 * - lastUsedAt: For getAllLocationsSorted() query performance
 */
@Parcelize
@Entity(
    tableName = "saved_locations",
    indices = [
        Index(value = ["name"]),
        Index(value = ["lastUsedAt"])
    ]
)
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    
    @ColumnInfo(name = "lastUsedAt")
    val lastUsedAt: Long = createdAt,
    
    @ColumnInfo(name = "address")
    val address: String? = null
) : Parcelable
