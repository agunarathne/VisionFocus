package com.visionfocus.maps.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Story 7.4: OfflineMapWorkScheduler
 * Schedules periodic background work for offline map management
 * 
 * Responsibilities:
 * - Schedule daily expiration checks
 * - Ensure work runs even after app restart
 * - Handle work policy (KEEP vs REPLACE)
 */
@Singleton
class OfflineMapWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule daily offline map expiration checks
     * Runs once per day to check for and clean up expired maps
     */
    fun scheduleDailyExpirationCheck() {
        val workRequest = PeriodicWorkRequestBuilder<OfflineMapExpirationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .addTag("offline_maps")
            .build()
        
        // Use KEEP policy to avoid rescheduling if already scheduled
        workManager.enqueueUniquePeriodicWork(
            OfflineMapExpirationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Timber.i("Scheduled daily offline map expiration check")
    }
    
    /**
     * Cancel all offline map-related work
     * Used for testing or when feature is disabled
     */
    fun cancelAllWork() {
        workManager.cancelUniqueWork(OfflineMapExpirationWorker.WORK_NAME)
        Timber.i("Cancelled offline map expiration check")
    }
}
