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
     * Check if microphone permission is currently granted.
     * Story 3.1 Task 2.1: Required for voice command functionality (FR55)
     */
    fun isMicrophonePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Story 6.2: Check if fine location permission is currently granted.
     * Required for GPS navigation and Google Maps Directions API (FR56)
     */
    fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Story 6.5: Determine if rationale should be shown for location permission.
     * Returns true if user previously denied permission.
     * 
     * Note: Only ACCESS_FINE_LOCATION is requested (foreground-only navigation).
     * ACCESS_BACKGROUND_LOCATION is NOT used per Story 6.5 AC #8.
     */
    fun shouldShowLocationRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    /**
     * Story 6.5: Request location permission for GPS navigation.
     * 
     * @param launcher ActivityResultLauncher from Activity
     */
    fun requestLocationPermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
    
    /**
     * Story 6.5: Register location permission result launcher in Activity.
     * Must be called during Activity initialization (before onCreate completes).
     * 
     * @param activity Host activity
     * @param onResult Callback with isGranted boolean result
     * @return ActivityResultLauncher configured for location permission
     */
    fun registerLocationPermissionLauncher(
        activity: Activity,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return (activity as androidx.activity.ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
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
     * Determine if rationale should be shown for microphone permission.
     * Returns true if user previously denied permission.
     * Story 3.1 Task 2.4: Rationale dialog support
     */
    fun shouldShowMicrophoneRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            android.Manifest.permission.RECORD_AUDIO
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
     * Request microphone permission for voice commands.
     * Story 3.1 Task 2.2: Microphone permission request
     * 
     * @param launcher ActivityResultLauncher from Activity
     */
    fun requestMicrophonePermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(android.Manifest.permission.RECORD_AUDIO)
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
    
    /**
     * Register microphone permission result launcher in Activity.
     * Must be called during Activity initialization (before onCreate completes).
     * Story 3.1 Task 2.2: Microphone permission launcher registration
     */
    fun registerMicrophonePermissionLauncher(
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
