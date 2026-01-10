package com.visionfocus.navigation.offline.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.visionfocus.R
import com.visionfocus.data.repository.OfflineMapRepository
import com.visionfocus.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Story 7.4: ExpirationCheckWorker
 * Periodic worker to check for expiring offline maps
 * 
 * Runs daily to check if maps are expiring within 5 days
 * Sends notifications to remind users to update maps
 */
@HiltWorker
class ExpirationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlineMapRepository: OfflineMapRepository
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "offline_map_expiration_check"
        private const val NOTIFICATION_CHANNEL_ID = "offline_map_expiration"
        private const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("Running offline map expiration check")
            
            // Get maps expiring soon
            val expiringSoon = offlineMapRepository.getMapsExpiringSoon().first()
            
            if (expiringSoon.isNotEmpty()) {
                Timber.i("Found ${expiringSoon.size} maps expiring soon")
                
                // Send notification
                sendExpirationNotification(expiringSoon.size, expiringSoon.first().regionName)
            } else {
                Timber.d("No maps expiring soon")
            }
            
            // Check for expired maps
            val expired = offlineMapRepository.getExpiredMaps().first()
            if (expired.isNotEmpty()) {
                Timber.w("Found ${expired.size} expired maps")
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check offline map expiration")
            Result.retry()
        }
    }
    
    private fun sendExpirationNotification(count: Int, locationName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        
        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Offline Map Expiration",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for expiring offline maps"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val message = if (count == 1) {
            "Offline maps for $locationName will expire soon. Update now?"
        } else {
            "Offline maps for $count locations will expire soon. Update now?"
        }
        
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_offline_map)
            .setContentTitle("Offline Maps Expiring")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Timber.i("Sent offline map expiration notification")
    }
}
