package com.visionfocus

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.visionfocus.databinding.ActivityMainBinding
import com.visionfocus.permissions.manager.AccessibilityAnnouncementHelper
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.ui.viewmodels.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for VisionFocus.
 * 
 * Story 2.3 Task 7: MainActivity integration with RecognitionFragment
 * 
 * @AndroidEntryPoint enables Hilt dependency injection in this Activity.
 * Required for injecting ViewModels and other dependencies.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    @Inject
    lateinit var accessibilityHelper: AccessibilityAnnouncementHelper
    
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Fix Issue #4: Enable TalkBack announcements on root view
        binding.root.importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        // Story 2.3 Task 7.3: RecognitionFragment auto-loaded via FragmentContainerView
        // No manual fragment transaction needed - android:name attribute handles it
        
        // Setup permission launcher and check camera permission (Story 1.5)
        setupPermissionLauncher()
        checkCameraPermission()
    }
    
    private fun setupPermissionLauncher() {
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleCameraPermissionResult(isGranted)
        }
    }
    
    private fun checkCameraPermission() {
        when {
            permissionManager.isCameraPermissionGranted() -> {
                // Permission already granted - no action needed for Story 1.5
                // Epic 2 will enable camera capture here
            }
            permissionManager.shouldShowCameraRationale(this) -> {
                showCameraRationale()
            }
            else -> {
                requestCameraPermission()
            }
        }
    }
    
    private fun showCameraRationale() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_permission_rationale, null)
        
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            .apply {
                dialogView.findViewById<View>(R.id.rationaleAllowButton).setOnClickListener {
                    dismiss()
                    requestCameraPermission()
                }
                
                dialogView.findViewById<View>(R.id.rationaleDeclineButton).setOnClickListener {
                    dismiss()
                    handleCameraPermissionResult(false)
                }
                
                show()
            }
    }
    
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
    
    private fun handleCameraPermissionResult(isGranted: Boolean) {
        val announcement = if (isGranted) {
            getString(R.string.camera_permission_granted)
        } else {
            getString(R.string.camera_permission_denied)
        }
        
        accessibilityHelper.announce(binding.root, announcement)
        
        if (!isGranted) {
            // Epic 9 will implement graceful degradation UI here
            // For Story 1.5, just log the denial
            android.util.Log.w("VisionFocus", "Camera permission denied")
        }
    }
}
