package com.visionfocus

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.visionfocus.voice.recognizer.VoiceRecognitionState
import com.visionfocus.voice.ui.VoiceRecognitionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity for VisionFocus.
 * 
 * Story 2.3 Task 7: MainActivity integration with RecognitionFragment
 * Story 2.5: Theme preferences applied on startup before setContentView
 * Story 3.1: Voice command button integration with microphone permission
 * 
 * @AndroidEntryPoint enables Hilt dependency injection in this Activity.
 * Required for injecting ViewModels and other dependencies.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Story 3.1: Voice recognition ViewModel
    private val voiceViewModel: VoiceRecognitionViewModel by viewModels()
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    @Inject
    lateinit var accessibilityHelper: AccessibilityAnnouncementHelper
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    
    // Story 3.1: Microphone permission launcher
    private lateinit var microphonePermissionLauncher: ActivityResultLauncher<String>
    
    // Story 3.1 Task 4.3: Pulsing animation for listening state
    private var pulsingAnimator: AnimatorSet? = null
    
    // Story 3.2: Broadcast receiver for voice commands
    private val voiceCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_RECOGNIZE -> {
                    // RecognizeCommand: Trigger object recognition
                    // TODO Epic 2: Start camera capture and recognition
                    android.util.Log.d("VisionFocus", "RecognizeCommand received - camera recognition will be implemented in Epic 2")
                }
                ACTION_CANCEL -> {
                    // CancelCommand: Cancel voice recognition
                    voiceViewModel.cancelListening()
                    android.util.Log.d("VisionFocus", "CancelCommand received - voice recognition cancelled")
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Story 2.5: Apply theme preferences BEFORE setContentView() to prevent flicker
        // CRITICAL: super.onCreate() MUST be called first for Hilt dependency injection
        // After super.onCreate(), settingsRepository is injected and ready to use
        android.util.Log.d("VisionFocus", "[MainActivity] onCreate started")
        super.onCreate(savedInstanceState)
        
        // Load and apply theme preferences (runBlocking is acceptable here - fast DataStore read)
        kotlinx.coroutines.runBlocking {
            try {
                val highContrast = settingsRepository.getHighContrastMode().first()
                val largeText = settingsRepository.getLargeTextMode().first()
                android.util.Log.d("VisionFocus", "[MainActivity] Theme preferences: highContrast=$highContrast, largeText=$largeText")
                ThemeManager.setThemeWithoutRecreate(this@MainActivity, highContrast, largeText)
                android.util.Log.d("VisionFocus", "[MainActivity] Theme applied successfully")
            } catch (e: Exception) {
                android.util.Log.e("VisionFocus", "[MainActivity] Failed to load theme preferences", e)
                // Fallback to default theme on error
                ThemeManager.setThemeWithoutRecreate(this@MainActivity, false, false)
            }
        }
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Story 2.5: Set up toolbar with menu
        setSupportActionBar(binding.toolbar)
        
        // Fix Issue #4: Enable TalkBack announcements on root view
        binding.root.importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        // REMOVED: ObjectRecognitionService and TTSManager initialization
        // Now handled in VisionFocusApplication.onCreate() before any Activity starts
        // This fixes race condition where Fragment loaded before initialization completed
        
        // Story 2.3 Task 7.3: RecognitionFragment auto-loaded via FragmentContainerView
        // No manual fragment transaction needed - android:name attribute handles it
        
        // Setup permission launchers and check permissions (Story 1.5, Story 3.1)
        setupPermissionLaunchers()
        checkCameraPermission()
        checkMicrophonePermission()
        
        // Story 3.1: Setup voice button and observe voice recognition state
        setupVoiceButton()
        observeVoiceRecognitionState()
        
        // Story 3.2: Register broadcast receiver for voice commands
        registerVoiceCommandReceiver()
    }
    
    /**
     * Register broadcast receiver for voice commands.
     * Story 3.2 Task 6: RecognizeCommand and CancelCommand integration
     */
    private fun registerVoiceCommandReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_RECOGNIZE)
            addAction(ACTION_CANCEL)
        }
        registerReceiver(voiceCommandReceiver, filter, RECEIVER_NOT_EXPORTED)
        android.util.Log.d("VisionFocus", "Voice command receiver registered")
    }
    
    private fun setupPermissionLaunchers() {
        // Camera permission launcher (Story 1.5)
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleCameraPermissionResult(isGranted)
        }
        
        // Microphone permission launcher (Story 3.1 Task 2.2)
        microphonePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleMicrophonePermissionResult(isGranted)
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
     * Check microphone permission for voice commands.
     * Story 3.1 Task 2: Microphone permission flow
     * HIGH-4 FIX: Only check permission state, don't auto-request or show rationale
     */
    private fun checkMicrophonePermission() {
        // HIGH-4: Just check state and update ViewModel - don't show dialogs
        val isGranted = permissionManager.isMicrophonePermissionGranted()
        voiceViewModel.updatePermissionState(isGranted)
        
        if (isGranted) {
            android.util.Log.d("VisionFocus", "Microphone permission already granted")
        } else {
            android.util.Log.d("VisionFocus", "Microphone permission not granted - button will be disabled")
        }
    }
    
    /**
     * Show rationale dialog for microphone permission.
     * Story 3.1 Task 2.4: Rationale dialog for previously denied permission
     */
    private fun showMicrophoneRationale() {
        AlertDialog.Builder(this)
            .setTitle(R.string.microphone_permission_rationale_title)
            .setMessage(R.string.microphone_permission_rationale_message)
            .setPositiveButton(R.string.permission_allow) { dialog, _ ->
                dialog.dismiss()
                requestMicrophonePermission()
            }
            .setNegativeButton(R.string.permission_deny) { dialog, _ ->
                dialog.dismiss()
                handleMicrophonePermissionResult(false)
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Request microphone permission.
     * Story 3.1 Task 2.2: Request microphone permission via launcher
     */
    private fun requestMicrophonePermission() {
        microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }
    
    /**
     * Handle microphone permission result.
     * Story 3.1 Task 2.5: TalkBack announcements for grant/deny events
     */
    private fun handleMicrophonePermissionResult(isGranted: Boolean) {
        val announcement = if (isGranted) {
            getString(R.string.microphone_permission_granted)
        } else {
            getString(R.string.microphone_permission_denied)
        }
        
        accessibilityHelper.announce(binding.root, announcement)
        voiceViewModel.updatePermissionState(isGranted)
        
        if (isGranted) {
            // Permission granted - can now start listening
            android.util.Log.d("VisionFocus", "Microphone permission granted")
        } else {
            // Permission denied - voice button will remain disabled
            android.util.Log.w("VisionFocus", "Microphone permission denied")
        }
    }
    
    /**
     * Setup voice button click listener and permission handling.
     * Story 3.1 Task 4: Voice button activation and permission check
     * HIGH-8 FIX: Added debouncing to prevent rapid clicks
     */
    private fun setupVoiceButton() {
        // HIGH-8: Track last click time for debouncing
        var lastClickTime = 0L
        val debounceDelayMs = 500L
        
        binding.voiceFab.setOnClickListener {
            // HIGH-8: Debounce - ignore clicks within 500ms
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < debounceDelayMs) {
                android.util.Log.d("VisionFocus", "Voice button click debounced")
                return@setOnClickListener
            }
            lastClickTime = currentTime
            
            if (permissionManager.isMicrophonePermissionGranted()) {
                // Permission granted - start listening (Task 4.1)
                voiceViewModel.startListening()
            } else {
                // Permission not granted - request it (Task 2.2)
                if (permissionManager.shouldShowMicrophoneRationale(this)) {
                    showMicrophoneRationale()
                } else {
                    requestMicrophonePermission()
                }
            }
        }
        
        // Update button state based on permission (Task 7.6)
        lifecycleScope.launch {
            voiceViewModel.isPermissionGranted.collect { granted ->
                updateVoiceButtonState(granted)
            }
        }
    }
    
    /**
     * Update voice button appearance based on permission state.
     * Story 3.1 Task 7.6: Button state based on permission
     */
    private fun updateVoiceButtonState(granted: Boolean) {
        binding.voiceFab.isEnabled = granted
        
        if (granted) {
            // Enable button with normal appearance
            binding.voiceFab.contentDescription = getString(R.string.voice_commands_button)
            binding.voiceFab.alpha = 1.0f
        } else {
            // Disable button with visual indication
            binding.voiceFab.contentDescription = getString(R.string.voice_commands_unavailable)
            binding.voiceFab.alpha = 0.5f
        }
    }
    
    /**
     * Observe voice recognition state changes for visual feedback.
     * Story 3.1 Task 4.3, 4.4: Pulsing animation for listening state
     */
    private fun observeVoiceRecognitionState() {
        lifecycleScope.launch {
            voiceViewModel.state.collect { state ->
                when (state) {
                    is VoiceRecognitionState.Idle -> {
                        stopPulsingAnimation()
                        binding.voiceFab.contentDescription = getString(R.string.voice_commands_button)
                    }
                    is VoiceRecognitionState.Listening -> {
                        if (state.isReady) {
                            startPulsingAnimation()
                            binding.voiceFab.contentDescription = getString(R.string.voice_commands_listening)
                        }
                    }
                    is VoiceRecognitionState.Processing -> {
                        // Brief processing state - keep animation
                    }
                    is VoiceRecognitionState.Error -> {
                        stopPulsingAnimation()
                        
                        // MEDIUM-2: Check if error is permission-related
                        if (state.errorCode == android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                            // Permission was revoked - update state and prompt re-request
                            voiceViewModel.updatePermissionState(false)
                            android.util.Log.w("VisionFocus", "Microphone permission revoked during recognition")
                        }
                        
                        // MEDIUM-4: Error-specific content description
                        binding.voiceFab.contentDescription = getString(R.string.voice_commands_error)
                    }
                }
            }
        }
    }
    
    /**
     * Start pulsing animation for listening state.
     * Story 3.1 Task 4.3: Pulsing microphone icon animation
     * 
     * Animation: Scale 1.0 → 1.1 → 1.0 (600ms loop), Alpha 1.0 → 0.7 → 1.0
     */
    private fun startPulsingAnimation() {
        // Stop any existing animation
        stopPulsingAnimation()
        
        val scaleX = ObjectAnimator.ofFloat(binding.voiceFab, "scaleX", 1.0f, 1.1f, 1.0f).apply {
            duration = 600
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val scaleY = ObjectAnimator.ofFloat(binding.voiceFab, "scaleY", 1.0f, 1.1f, 1.0f).apply {
            duration = 600
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val alpha = ObjectAnimator.ofFloat(binding.voiceFab, "alpha", 1.0f, 0.7f, 1.0f).apply {
            duration = 600
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        pulsingAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
        
        android.util.Log.d("VisionFocus", "Started pulsing animation")
    }
    
    /**
     * Stop pulsing animation.
     * Story 3.1 Task 6.6: Stop animation on error or completion
     */
    private fun stopPulsingAnimation() {
        pulsingAnimator?.cancel()
        pulsingAnimator = null
        
        // Reset button to normal state
        binding.voiceFab.scaleX = 1.0f
        binding.voiceFab.scaleY = 1.0f
        binding.voiceFab.alpha = if (voiceViewModel.isVoiceButtonEnabled()) 1.0f else 0.5f
        
        android.util.Log.d("VisionFocus", "Stopped pulsing animation")
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
    
    /**
     * Navigate to home screen (RecognitionFragment).
     * Story 3.5 Task 8: HomeCommand implementation
     * 
     * Clears back stack and shows RecognitionFragment.
     * If already on home screen, announces "Home screen".
     * 
     * @param ttsManager Optional TTSManager for announcement (injected from command)
     */
    fun navigateToHome(ttsManager: TTSManager? = null) {
        // Check if already on home screen
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is com.visionfocus.ui.recognition.RecognitionFragment && 
            supportFragmentManager.backStackEntryCount == 0) {
            // Already at home
            android.util.Log.d("VisionFocus", "Already on home screen")
            ttsManager?.let {
                lifecycleScope.launch {
                    it.announce(getString(R.string.home_screen_announcement))
                }
            }
            return
        }
        
        // Clear back stack
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        
        // Navigate to home if not already there
        if (currentFragment !is com.visionfocus.ui.recognition.RecognitionFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, com.visionfocus.ui.recognition.RecognitionFragment())
                .commit()
        }
        
        android.util.Log.d("VisionFocus", "Navigated to home screen")
        ttsManager?.let {
            lifecycleScope.launch {
                it.announce(getString(R.string.home_screen_announcement))
            }
        }
    }
    
    /**
     * Navigate back in the fragment stack.
     * Story 3.5 Task 9: BackCommand implementation
     * 
     * Pops the back stack. If already on home screen (back stack empty),
     * announces "Already at home screen".
     * 
     * @param ttsManager Optional TTSManager for announcement (injected from command)
     */
    fun navigateBack(ttsManager: TTSManager? = null) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Back stack has entries - pop it
            supportFragmentManager.popBackStack()
            android.util.Log.d("VisionFocus", "Navigated back")
            ttsManager?.let {
                lifecycleScope.launch {
                    it.announce(getString(R.string.going_back_announcement))
                }
            }
        } else {
            // Already at home screen - no back stack to pop
            android.util.Log.d("VisionFocus", "Already at home screen - back stack empty")
            ttsManager?.let {
                lifecycleScope.launch {
                    it.announce(getString(R.string.already_at_home_announcement))
                }
            }
        }
    }
    
    /**
     * Get current screen identifier for context tracking.
     * Story 3.5 Task 7: Screen context preservation
     * 
     * @return String identifier: "home", "settings", or "unknown"
     */
    fun getCurrentScreen(): String {
        return when (supportFragmentManager.findFragmentById(R.id.fragmentContainer)) {
            is com.visionfocus.ui.recognition.RecognitionFragment -> "home"
            is SettingsFragment -> "settings"
            else -> "unknown"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Story 3.1: Stop pulsing animation if active
        stopPulsingAnimation()
        
        // Story 3.2: Unregister broadcast receiver
        try {
            unregisterReceiver(voiceCommandReceiver)
            android.util.Log.d("VisionFocus", "Voice command receiver unregistered")
        } catch (e: IllegalArgumentException) {
            // Receiver not registered - safe to ignore
            android.util.Log.d("VisionFocus", "Voice command receiver was not registered")
        }
        
        // Note: ObjectRecognitionService and TTSManager are Application-scoped singletons
        // They are NOT cleaned up when Activity is destroyed - they live for app lifetime
        // Cleanup happens when Android OS kills the app process
        // VoiceRecognitionManager is cleaned up by VoiceRecognitionViewModel.onCleared()
    }
    
    companion object {
        // Story 3.2: Voice command broadcast actions
        const val ACTION_RECOGNIZE = "com.visionfocus.ACTION_RECOGNIZE"
        const val ACTION_CANCEL = "com.visionfocus.ACTION_CANCEL"
    }
}
