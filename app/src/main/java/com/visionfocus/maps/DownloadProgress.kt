package com.visionfocus.maps

/**
 * Story 7.4: DownloadProgress sealed class
 * Represents the various states of offline map download progress
 */
sealed class DownloadProgress {
    /**
     * Download not started yet
     */
    object Idle : DownloadProgress()
    
    /**
     * Download in progress
     * @param percent Download completion percentage (0-100)
     * @param bytesDownloaded Number of bytes downloaded so far
     * @param totalBytes Total size of the download in bytes
     */
    data class Downloading(
        val percent: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadProgress()
    
    /**
     * Download completed successfully
     */
    object Completed : DownloadProgress()
    
    /**
     * Download failed
     * @param error Error message describing the failure
     */
    data class Failed(
        val error: String
    ) : DownloadProgress()
}
