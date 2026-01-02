package com.visionfocus.recognition.spatial

/**
 * Distance enum for relative distance estimation based on bounding box size
 * 
 * Story 4.5 AC: Distance estimation based on box area percentage
 * 
 * Calibrated for typical object sizes:
 * - Chair at 1m: ~40% screen area = CLOSE
 * - Chair at 2m: ~25% screen area = MEDIUM
 * - Chair at 4m: ~10% screen area = FAR
 * 
 * Thresholds:
 * - CLOSE: Bounding box >40% of screen (within arm's reach ~0.5-1.5m)
 * - MEDIUM: Bounding box 20-40% of screen (a few steps away ~1.5-3.0m)
 * - FAR: Bounding box <20% of screen (across room >3.0m)
 */
enum class Distance {
    /**
     * Object is close by - box area >40% of screen
     * Typically within arm's reach (0.5-1.5m)
     */
    CLOSE,
    
    /**
     * Object is at medium distance - box area 20-40% of screen
     * User needs to take a few steps to reach (1.5-3.0m)
     */
    MEDIUM,
    
    /**
     * Object is far away - box area <20% of screen
     * User needs to walk across room (>3.0m)
     */
    FAR;
    
    companion object {
        /**
         * Threshold for CLOSE classification
         * Box area >40% of screen
         */
        const val CLOSE_THRESHOLD = 0.40f
        
        /**
         * Threshold for MEDIUM classification
         * Box area 20-40% of screen
         */
        const val MEDIUM_THRESHOLD = 0.20f
    }
}
