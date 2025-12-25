package com.visionfocus.permissions.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permission manager for runtime permission requests.
 * 
 * Handles camera permission (Story 1.5), with extension points for:
 * - Microphone permission (Story 3.1)
 * - Location permissions (Story 6.5)
 * - Bluetooth permission (Story 8.3)
 * 
 * Features:
 * - TalkBack announcements for grant/deny events
 * - Rationale dialog for previously denied permissions
 * - Graceful degradation logic hooks (Epic 9)
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Check if camera permission is currently granted.
     */
    fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Determine if rationale should be shown for camera permission.
     * Returns true if user previously denied permission.
     */
    fun shouldShowCameraRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            android.Manifest.permission.CAMERA
        )
    }
    
    /**
     * Request camera permission with TalkBack announcements.
     * 
     * @param launcher ActivityResultLauncher from Activity
     */
    fun requestCameraPermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }
    
    /**
     * Register permission result launcher in Activity.
     * Must be called during Activity initialization (before onCreate completes).
     */
    fun registerCameraPermissionLauncher(
        activity: Activity,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return (activity as androidx.activity.ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }
}
