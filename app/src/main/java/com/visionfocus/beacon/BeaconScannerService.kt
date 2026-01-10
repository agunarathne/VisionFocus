package com.visionfocus.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BLE scanner service for beacon detection.
 * 
 * Epic 10 Story 10.1: Scans for nearby Bluetooth beacons during pairing.
 * Epic 10 Story 10.2: Scans for specific beacon during proximity navigation.
 * 
 * Uses Android's BluetoothLeScanner for low-energy beacon detection.
 * Supports iSearching beacons and standard BLE devices.
 */
@Singleton
class BeaconScannerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "BeaconScannerService"
    }
    
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }
    
    /**
     * Scan for nearby BLE devices.
     * 
     * Story 10.1: Returns Flow of discovered devices with name, MAC address, and RSSI.
     * Filters out devices without names (non-beacon BLE devices).
     * 
     * @return Flow<ScannedBeacon> emitting each discovered beacon
     */
    fun scanForBeacons(): Flow<ScannedBeacon> = callbackFlow {
        val scanner = bluetoothLeScanner
        
        if (scanner == null || bluetoothAdapter?.isEnabled != true) {
            Log.e(TAG, "Bluetooth not available or not enabled")
            close()
            return@callbackFlow
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)  // Fast scanning for pairing
            .build()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val rssi = result.rssi
                val name = device.name
                
                // Filter devices with names only (beacons typically advertise names)
                if (!name.isNullOrBlank()) {
                    val scannedBeacon = ScannedBeacon(
                        name = name,
                        macAddress = device.address,
                        rssi = rssi
                    )
                    Log.d(TAG, "Discovered beacon: $name (${device.address}) RSSI: $rssi dBm")
                    trySend(scannedBeacon)
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Beacon scan failed with error: $errorCode")
                close()
            }
        }
        
        try {
            Log.d(TAG, "Starting BLE scan for beacons...")
            scanner.startScan(null, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied", e)
            close()
        }
        
        awaitClose {
            try {
                Log.d(TAG, "Stopping BLE scan")
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan", e)
            }
        }
    }
    
    /**
     * Scan for a specific beacon by MAC address.
     * 
     * Story 10.2: Used during proximity navigation to find target beacon.
     * Returns RSSI updates for distance estimation.
     * 
     * @param targetMacAddress MAC address of beacon to find
     * @return Flow<Int> emitting RSSI values when beacon is detected
     */
    fun scanForSpecificBeacon(targetMacAddress: String): Flow<Int> = callbackFlow {
        val scanner = bluetoothLeScanner
        
        if (scanner == null || bluetoothAdapter?.isEnabled != true) {
            Log.e(TAG, "Bluetooth not available or not enabled")
            close()
            return@callbackFlow
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)  // Fast updates for navigation
            .build()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                
                // Only emit RSSI for target beacon
                if (device.address.equals(targetMacAddress, ignoreCase = true)) {
                    val rssi = result.rssi
                    Log.d(TAG, "Target beacon detected: $targetMacAddress RSSI: $rssi dBm")
                    trySend(rssi)
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Specific beacon scan failed with error: $errorCode")
                close()
            }
        }
        
        try {
            Log.d(TAG, "Starting BLE scan for beacon: $targetMacAddress")
            scanner.startScan(null, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied", e)
            close()
        }
        
        awaitClose {
            try {
                Log.d(TAG, "Stopping specific beacon scan")
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan", e)
            }
        }
    }
    
    /**
     * Check if Bluetooth is available and enabled.
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}

/**
 * Data class representing a discovered BLE beacon.
 * 
 * @param name Beacon's advertised name
 * @param macAddress Unique Bluetooth MAC address
 * @param rssi Signal strength in dBm (typically -100 to -30)
 */
data class ScannedBeacon(
    val name: String,
    val macAddress: String,
    val rssi: Int
)
