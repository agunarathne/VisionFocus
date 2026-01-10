package com.visionfocus.ui.beacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.beacon.BeaconScannerService
import com.visionfocus.beacon.ScannedBeacon
import com.visionfocus.data.local.entity.BeaconEntity
import com.visionfocus.data.repository.BeaconRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Beacon Management Screen.
 * 
 * Epic 10 Story 10.1: Managing paired beacons.
 * 
 * Capabilities:
 * - List paired beacons (BeaconRepository)
 * - Scan for new beacons (BeaconScannerService)
 * - Pair/Unpair beacons
 */
@HiltViewModel
class BeaconManagementViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val beaconScanner: BeaconScannerService
) : ViewModel() {

    // List of saved/paired beacons
    val savedBeacons: StateFlow<List<BeaconEntity>> = beaconRepository.getAllBeacons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Scanning state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // List of scanned (discovered) beacons during pairing
    private val _scannedBeacons = MutableStateFlow<List<ScannedBeacon>>(emptyList())
    val scannedBeacons: StateFlow<List<ScannedBeacon>> = _scannedBeacons.asStateFlow()

    private var scanJob: Job? = null

    /**
     * Start scanning for new beacons.
     * Clears previous results and updates _scannedBeacons flow.
     * Auto-stops after 15 seconds to save battery.
     */
    fun startScanning() {
        if (_isScanning.value) return

        _isScanning.value = true
        _scannedBeacons.value = emptyList()

        scanJob = viewModelScope.launch {
            beaconScanner.scanForBeacons()
                .catch { e ->
                    // Handle scan errors (e.g. permission denied)
                    _isScanning.value = false
                }
                .collect { beacon ->
                    // Update list with new unique device (avoid duplicates)
                    val currentList = _scannedBeacons.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.macAddress == beacon.macAddress }
                    
                    if (existingIndex != -1) {
                        // Update existing (e.g. RSSI change)
                        currentList[existingIndex] = beacon
                    } else {
                        // Add new
                        currentList.add(beacon)
                    }
                    // Sort by RSSI (strongest first)
                    currentList.sortByDescending { it.rssi }
                    _scannedBeacons.value = currentList
                }
        }

        // Auto-stop after 15 seconds
        viewModelScope.launch {
            delay(15000)
            stopScanning()
        }
    }

    /**
     * Stop scanning.
     */
    fun stopScanning() {
        scanJob?.cancel()
        _isScanning.value = false
    }

    /**
     * Pair a scanned beacon.
     * Saves it to the database with a user-friendly name.
     */
    fun pairBeacon(beacon: ScannedBeacon, name: String) {
        viewModelScope.launch {
            val entity = BeaconEntity(
                macAddress = beacon.macAddress,
                name = name,
                createdAt = System.currentTimeMillis()
            )
            beaconRepository.insert(entity)
        }
    }

    /**
     * Delete/Unpair a beacon.
     */
    fun deleteBeacon(beacon: BeaconEntity) {
        viewModelScope.launch {
            beaconRepository.delete(beacon)
        }
    }

    /**
     * Rename a beacon.
     */
    fun renameBeacon(beacon: BeaconEntity, newName: String) {
        viewModelScope.launch {
            beaconRepository.update(beacon.copy(name = newName))
        }
    }
}
