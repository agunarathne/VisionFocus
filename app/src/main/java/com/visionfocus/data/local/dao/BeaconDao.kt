package com.visionfocus.data.local.dao

import androidx.room.*
import com.visionfocus.data.local.entity.BeaconEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Bluetooth beacon database operations.
 * 
 * Epic 10 Story 10.1: Beacon pairing and management.
 * Provides CRUD operations for paired beacons.
 */
@Dao
interface BeaconDao {
    
    /**
     * Insert a new beacon.
     * Story 10.1: Called after successful BLE pairing.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beacon: BeaconEntity): Long
    
    /**
     * Update existing beacon (e.g., rename, update lastSeenAt).
     */
    @Update
    suspend fun update(beacon: BeaconEntity)
    
    /**
     * Delete a beacon.
     */
    @Delete
    suspend fun delete(beacon: BeaconEntity)
    
    /**
     * Get all paired beacons.
     * Returns Flow for reactive updates in UI.
     */
    @Query("SELECT * FROM beacons ORDER BY name ASC")
    fun getAllBeacons(): Flow<List<BeaconEntity>>
    
    /**
     * Find beacon by MAC address.
     * Story 10.2: Used during proximity navigation to match scanned devices.
     */
    @Query("SELECT * FROM beacons WHERE macAddress = :macAddress LIMIT 1")
    suspend fun findByMacAddress(macAddress: String): BeaconEntity?
    
    /**
     * Find beacon by name (case-insensitive).
     * Story 10.2: Used for voice command matching.
     */
    @Query("SELECT * FROM beacons WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): BeaconEntity?
    
    /**
     * Get all beacon names for fuzzy matching.
     * Story 10.2: Voice command disambiguation.
     */
    @Query("SELECT name FROM beacons ORDER BY name ASC")
    suspend fun getAllBeaconNames(): List<String>
    
    /**
     * Update beacon's last seen timestamp and RSSI.
     * Story 10.2: Called during active scanning.
     */
    @Query("UPDATE beacons SET lastSeenAt = :timestamp, lastRssi = :rssi WHERE macAddress = :macAddress")
    suspend fun updateLastSeen(macAddress: String, timestamp: Long, rssi: Int)
}
