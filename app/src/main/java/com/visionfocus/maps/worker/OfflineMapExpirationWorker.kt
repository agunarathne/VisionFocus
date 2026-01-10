package com.visionfocus.maps.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.tts.engine.TTSManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Story 7.4: OfflineMapExpirationWorker
 * Background worker to check for expired offline maps and clean them up
 * 
 * Runs daily to:
 * - Find maps older than 30 days
 * - Delete expired map files
 * - Remove expired entries from database
 * - Notify user via TalkBack announcement
 */
@HiltWorker
class OfflineMapExpirationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlineMapDao: OfflineMapDao,
    private val ttsManager: TTSManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "offline_map_expiration_check"
        private const val EXPIRATION_DAYS = 30
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting offline map expiration check")
            
            // Calculate expiration threshold (30 days ago)
            val expirationThreshold = System.currentTimeMillis() - (EXPIRATION_DAYS * MILLIS_PER_DAY)
            
            // Find all expired maps
            val allMaps = offlineMapDao.getAllOfflineMapsList()
            val expiredMaps = allMaps.filter { it.downloadedAt < expirationThreshold }
            
            if (expiredMaps.isEmpty()) {
                Timber.d("No expired offline maps found")
                return@withContext Result.success()
            }
            
            Timber.i("Found ${expiredMaps.size} expired offline maps")
            
            // Delete each expired map
            for (map in expiredMaps) {
                try {
                    // Delete map file if it exists
                    val mapFile = File(applicationContext.filesDir, map.regionName)
                    if (mapFile.exists()) {
                        mapFile.delete()
                        Timber.d("Deleted map file: ${map.regionName}")
                    }
                    
                    // Delete database entry
                    offlineMapDao.deleteOfflineMap(map.locationId)
                    Timber.d("Deleted database entry for location ${map.locationId}")
                    
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete expired map for location ${map.locationId}")
                }
            }
            
            // Announce expiration to user
            withContext(Dispatchers.Main) {
                announceExpiration(expiredMaps.size)
            }
            
            Timber.i("Successfully cleaned up ${expiredMaps.size} expired offline maps")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Offline map expiration check failed")
            Result.retry()
        }
    }
    
    /**
     * Announce offline map expiration to user via TalkBack
     */
    private suspend fun announceExpiration(count: Int) {
        val message = if (count == 1) {
            "1 offline map has expired and been removed"
        } else {
            "$count offline maps have expired and been removed"
        }
        ttsManager.announce(message)
        Timber.d("Announced: $message")
    }
}
