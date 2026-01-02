package com.visionfocus.recognition.scanning

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks detected objects during continuous scanning to prevent duplicate announcements
 * 
 * Story 4.4 Task 3: DetectedObjectTracker for duplicate suppression
 * AC: Duplicate objects (already announced) are suppressed to avoid repetition
 * 
 * Implementation:
 * - Case-insensitive tracking (normalizes to lowercase)
 * - Thread-safe for concurrent access
 * - Reset capability for new scanning sessions
 * 
 * Usage:
 * ```
 * if (tracker.isNewObject("chair")) {
 *     tracker.addObject("chair")
 *     announceObject("chair")
 * }
 * ```
 */
@Singleton
class DetectedObjectTracker @Inject constructor() {
    
    /**
     * Set of detected object labels (lowercase normalized)
     * Thread-safe access through synchronized methods
     */
    private val detectedObjects = mutableSetOf<String>()
    
    /**
     * Check if object is new (not yet detected in current session)
     * 
     * @param label Object label to check
     * @return true if object has not been detected yet, false if already seen
     */
    @Synchronized
    fun isNewObject(label: String): Boolean {
        return !detectedObjects.contains(label.lowercase())
    }
    
    /**
     * Add object to detected set
     * 
     * @param label Object label to add (normalized to lowercase internally)
     */
    @Synchronized
    fun addObject(label: String) {
        detectedObjects.add(label.lowercase())
    }
    
    /**
     * Get all detected object labels
     * 
     * @return Immutable list of detected object labels
     */
    @Synchronized
    fun getAllDetectedObjects(): List<String> {
        return detectedObjects.toList()
    }
    
    /**
     * Get count of detected objects
     * 
     * @return Number of unique objects detected
     */
    @Synchronized
    fun count(): Int = detectedObjects.size
    
    /**
     * Reset tracker for new scanning session
     * Clears all detected objects
     */
    @Synchronized
    fun reset() {
        detectedObjects.clear()
    }
}
