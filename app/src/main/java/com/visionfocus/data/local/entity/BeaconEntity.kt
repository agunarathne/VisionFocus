package com.visionfocus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for Bluetooth beacon storage.
 * 
 * Epic 10 Story 10.1: Beacon pairing and management for indoor navigation.
 * Stores paired iSearching beacons with user-defined names and locations.
 * 
 * Schema fields:
 * - id: Auto-generated primary key
 * - name: User-provided beacon name (e.g., "Living Room", "Kitchen")
 * - macAddress: Bluetooth MAC address (unique identifier)
 * - lastRssi: Last recorded signal strength (for debugging)
 * - createdAt: Timestamp when beacon was paired (milliseconds)
 * - lastSeenAt: Timestamp when beacon was last detected
 * 
 * Indexes:
 * - macAddress: For quick lookup during beacon scanning
 * - name: For voice command matching
 */
@Entity(
    tableName = "beacons",
    indices = [
        Index(value = ["macAddress"], unique = true),
        Index(value = ["name"])
    ]
)
data class BeaconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "macAddress")
    val macAddress: String,
    
    @ColumnInfo(name = "lastRssi")
    val lastRssi: Int = -100,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    
    @ColumnInfo(name = "lastSeenAt")
    val lastSeenAt: Long = createdAt
)
