package com.visionfocus

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.databinding.ActivityMainBinding
import com.visionfocus.permissions.manager.AccessibilityAnnouncementHelper
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.recognition.service.ObjectRecognitionService
import com.visionfocus.theme.ThemeManager
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.settings.SettingsFragment
import com.visionfocus.ui.viewmodels.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity for VisionFocus.
 * 
 * Story 2.3 Task 7: MainActivity integration with RecognitionFragment
 * Story 2.5: Theme preferences applied on startup before setContentView
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
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Story 2.5: Apply theme preferences BEFORE setContentView to prevent flicker
        // Note: Theme application happens synchronously via runBlocking
        // Safe in onCreate as preferences load is fast (<5ms from DataStore)
        kotlinx.coroutines.runBlocking {
            val highContrast = settingsRepository.getHighContrastMode().first()
            val largeText = settingsRepository.getLargeTextMode().first()
            ThemeManager.setThemeWithoutRecreate(this@MainActivity, highContrast, largeText)
        }
        
        super.onCreate(savedInstanceState)
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Fix Issue #4: Enable TalkBack announcements on root view
        binding.root.importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        // REMOVED: ObjectRecognitionService and TTSManager initialization
        // Now handled in VisionFocusApplication.onCreate() before any Activity starts
        // This fixes race condition where Fragment loaded before initialization completed
        
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
    
    /**
     * Creates options menu with Settings item.
     * 
     * Story 2.5 Task 10: Settings menu for navigation to SettingsFragment
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    /**
     * Handles menu item selections.
     * 
     * Story 2.5 Task 10: Navigate to SettingsFragment on Settings menu tap
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                navigateToSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Navigates to SettingsFragment.
     * 
     * Replaces current fragment with SettingsFragment and adds to back stack
     * for proper back navigation.
     */
    private fun navigateToSettings() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Note: ObjectRecognitionService and TTSManager are Application-scoped singletons
        // They are NOT cleaned up when Activity is destroyed - they live for app lifetime
        // Cleanup happens when Android OS kills the app process
    }
}
