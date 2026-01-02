package com.visionfocus.recognition.scanning

/**
 * Sealed class representing the state of continuous scanning mode
 * 
 * Story 4.4: Continuous Scanning Mode for Environment Mapping
 * Task 1.1: ScanningState sealed class for state management
 * 
 * States:
 * - Idle: No scanning active
 * - Scanning: Actively capturing frames and announcing objects
 * - Stopping: Transitioning to idle with summary announcement
 */
sealed class ScanningState {
    /**
     * Idle state - no scanning active
     * User can start scanning from this state
     */
    object Idle : ScanningState()
    
    /**
     * Scanning state - actively capturing frames and processing detections
     * 
     * @property startTime Unix timestamp when scanning started (milliseconds)
     * @property objectsDetected Count of unique objects detected so far
     */
    data class Scanning(
        val startTime: Long,
        val objectsDetected: Int = 0
    ) : ScanningState()
    
    /**
     * Stopping state - transitioning to idle with summary announcement
     * 
     * @property summary Final summary message to announce
     */
    data class Stopping(
        val summary: String
    ) : ScanningState()
}
