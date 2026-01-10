package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.BeaconDao
import com.visionfocus.data.local.entity.BeaconEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Bluetooth beacon data access.
 * 
 * Epic 10 Story 10.1: Beacon pairing and management.
 * Provides data layer abstraction between ViewModels and Room database.
 */
@Singleton
class BeaconRepository @Inject constructor(
    private val beaconDao: BeaconDao
) {
    
    /**
     * Get all paired beacons as reactive Flow.
     * Story 10.1: Used by beacon management UI.
     */
    fun getAllBeacons(): Flow<List<BeaconEntity>> = beaconDao.getAllBeacons()
    
    /**
     * Insert a new beacon after successful pairing.
     * Story 10.1: Called from BeaconPairingViewModel.
     */
    suspend fun insert(beacon: BeaconEntity): Long = beaconDao.insert(beacon)
    
    /**
     * Update existing beacon (rename, update timestamps).
     */
    suspend fun update(beacon: BeaconEntity) = beaconDao.update(beacon)
    
    /**
     * Delete a beacon.
     */
    suspend fun delete(beacon: BeaconEntity) = beaconDao.delete(beacon)
    
    /**
     * Find beacon by MAC address.
     * Story 10.2: Used during proximity navigation to match scanned BLE devices.
     */
    suspend fun findByMacAddress(macAddress: String): BeaconEntity? =
        beaconDao.findByMacAddress(macAddress)
    
    /**
     * Find beacon by name (case-insensitive).
     * Story 10.2: Used for voice command "Take me to [name]".
     */
    suspend fun findByName(name: String): BeaconEntity? = beaconDao.findByName(name)
    
    /**
     * Get all beacon names for fuzzy matching.
     * Story 10.2: Voice command disambiguation.
     */
    suspend fun getAllBeaconNames(): List<String> = beaconDao.getAllBeaconNames()
    
    /**
     * Update beacon's last seen timestamp and RSSI.
     * Story 10.2: Called during active proximity navigation.
     */
    suspend fun updateLastSeen(macAddress: String, timestamp: Long, rssi: Int) =
        beaconDao.updateLastSeen(macAddress, timestamp, rssi)
}
