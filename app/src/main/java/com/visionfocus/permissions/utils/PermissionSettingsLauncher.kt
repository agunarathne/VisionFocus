package com.visionfocus.permissions.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

/**
 * Utility for launching system settings to enable app permissions.
 * Story 6.5: GPS Location Permissions with Clear Explanations
 * 
 * Provides convenient method to open system app settings page where
 * users can manually enable denied permissions (location, camera, etc.).
 * 
 * Usage: Call openAppSettings(context) from onClick listener when
 * user taps "Open Settings" button in permission-denied UI.
 */
object PermissionSettingsLauncher {
    
    private const val TAG = "PermissionSettingsLauncher"
    
    /**
     * Open system app settings page for VisionFocus.
     * 
     * Launches Settings.ACTION_APPLICATION_DETAILS_SETTINGS intent with
     * app's package name. User can then navigate to Permissions section
     * to manually enable location, camera, or microphone access.
     * 
     * Story 6.5 AC #7: In-app settings link to system permission settings.
     * 
     * @param context Application or Activity context
     * @return true if settings intent launched successfully, false if unavailable
     */
    fun openAppSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Launched app settings for package: ${context.packageName}")
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Unable to open app settings - intent not available", e)
            false
        }
    }
}
