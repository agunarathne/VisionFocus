package com.visionfocus.navigation.offline

/**
 * Story 7.4: Offline Map Pre-Caching
 * 
 * Sealed class representing the progress of an offline map download.
 * 
 * States:
 * - Idle: Initial state before download starts
 * - Preparing: Calculating bounding box, estimating size, creating Mapbox region
 * - Downloading: Active download with progress tracking
 * - Complete: Download finished successfully
 * - Error: Download failed or was cancelled
 * 
 * Used by OfflineMapManager.downloadOfflineMap() Flow emissions.
 */
sealed class DownloadProgress {
    
    /**
     * Initial idle state
     */
    object Idle : DownloadProgress()
    
    /**
     * Preparing download (calculating bounds, creating region definition)
     */
    object Preparing : DownloadProgress()
    
    /**
     * Active download state with progress tracking
     * 
     * @param bytesDownloaded Number of bytes downloaded so far
     * @param totalBytes Estimated total bytes to download
     * @param percent Progress percentage (0-100)
     * @param completedTiles Number of map tiles downloaded
     * @param totalTiles Estimated total tiles to download
     */
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percent: Int,
        val completedTiles: Long = 0,
        val totalTiles: Long = 0
    ) : DownloadProgress() {
        
        /**
         * Format progress as human-readable string
         * Example: "45.2 MB / 126.0 MB (35%)"
         */
        fun getFormattedProgress(): String {
            val downloadedMB = bytesDownloaded / (1024.0 * 1024.0)
            val totalMB = totalBytes / (1024.0 * 1024.0)
            return String.format("%.1f MB / %.1f MB (%d%%)", downloadedMB, totalMB, percent)
        }
        
        /**
         * Check if progress milestone reached (for TTS announcements)
         * Milestones: 25%, 50%, 75%, 100%
         */
        fun isMilestone(): Boolean {
            return percent in listOf(25, 50, 75, 100)
        }
    }
    
    /**
     * Download completed successfully
     * 
     * @param sizeBytes Final size of downloaded map data
     * @param regionId Mapbox offline region ID
     * @param regionName Name of the offline region
     */
    data class Complete(
        val sizeBytes: Long,
        val regionId: Long,
        val regionName: String
    ) : DownloadProgress() {
        
        /**
         * Format size as human-readable string
         * Example: "126 MB"
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
    
    /**
     * Download failed or was cancelled
     * 
     * @param message Error description
     * @param isCancelled True if user cancelled, false if error
     * @param retryable True if download can be retried
     */
    data class Error(
        val message: String,
        val isCancelled: Boolean = false,
        val retryable: Boolean = true
    ) : DownloadProgress()
}
