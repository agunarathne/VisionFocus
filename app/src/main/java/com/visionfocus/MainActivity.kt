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
import com.visionfocus.voice.processor.VoiceCommandProcessor
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
    
    // Story 3.5: Voice command processor for navigation context
    @Inject
    lateinit var voiceCommandProcessor: VoiceCommandProcessor
    
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    
    // Story 3.1: Microphone permission launcher
    private lateinit var microphonePermissionLauncher: ActivityResultLauncher<String>
    
    // Story 3.1 Task 4.3: Pulsing animation for listening state
    private var pulsingAnimator: AnimatorSet? = null
    
    // Story 3.5 AC #3: Track origin screen for context preservation
    private var originScreenBeforeCommand: String? = null
    
    // Story 3.2: Broadcast receiver for voice commands
    private val voiceCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_RECOGNIZE -> {
                    // RecognizeCommand: Trigger object recognition
                    // Story 3.5 AC #3: Save current screen before navigating
                    originScreenBeforeCommand = getCurrentScreen()
                    android.util.Log.d("VisionFocus", "RecognizeCommand received from screen: $originScreenBeforeCommand")
                    
                    // If not on home screen, navigate to home for recognition
                    if (originScreenBeforeCommand != "home") {
                        android.util.Log.d("VisionFocus", "Navigating to home for recognition")
                        navigateToHomeForRecognition()
                    } else {
                        android.util.Log.d("VisionFocus", "Already on home screen - starting recognition")
                        // Trigger recognition on current screen
                        // Note: Actual recognition trigger will be implemented when Epic 2 integration is ready
                    }
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
        
        // CRITICAL FIX: Set BottomNavigationView colors programmatically to support theme switching
        applyBottomNavigationColors()
        
        // Story 2.5: Set up toolbar with menu
        setSupportActionBar(binding.toolbar)
        
        // Story 6.1 Task 11: Setup bottom navigation
        setupBottomNavigation()
        
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
        
        // Story 3.5: Set MainActivity context for navigation commands
        voiceCommandProcessor.activityContext = this
        
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
     * FIX: Keep button enabled but change visual appearance for permission state
     */
    private fun updateVoiceButtonState(granted: Boolean) {
        // Keep button always enabled so user can tap to request permission
        binding.voiceFab.isEnabled = true
        
        if (granted) {
            // Enable button with normal appearance
            binding.voiceFab.contentDescription = getString(R.string.voice_commands_button)
            binding.voiceFab.alpha = 1.0f
        } else {
            // Button is clickable but visually indicates permission needed
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
     * CRITICAL FIX: Apply BottomNavigationView colors programmatically.
     * 
     * Resolves colors from the active theme at runtime to support dynamic theme
     * switching without recreating the activity. This prevents crashes when
     * switching to high contrast mode.
     * 
     * Uses MaterialColors.getColor() to safely resolve theme attributes
     * (?attr/colorPrimary, ?attr/colorOnSurface) from the current theme context.
     */
    private fun applyBottomNavigationColors() {
        val colorPrimary = com.google.android.material.color.MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary,
            android.graphics.Color.MAGENTA // Fallback color (should never be used)
        )
        
        val colorOnSurface = com.google.android.material.color.MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSurface,
            android.graphics.Color.BLACK // Fallback color
        )
        
        // Create ColorStateList for icons (checked = primary, unchecked = onSurface with 60% alpha)
        val iconColors = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf() // default state
            ),
            intArrayOf(
                colorPrimary,
                android.graphics.Color.argb(
                    (255 * 0.6).toInt(),
                    android.graphics.Color.red(colorOnSurface),
                    android.graphics.Color.green(colorOnSurface),
                    android.graphics.Color.blue(colorOnSurface)
                )
            )
        )
        
        // Create ColorStateList for text (same as icons)
        val textColors = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf() // default state
            ),
            intArrayOf(
                colorPrimary,
                android.graphics.Color.argb(
                    (255 * 0.6).toInt(),
                    android.graphics.Color.red(colorOnSurface),
                    android.graphics.Color.green(colorOnSurface),
                    android.graphics.Color.blue(colorOnSurface)
                )
            )
        )
        
        binding.bottomNavigation.itemIconTintList = iconColors
        binding.bottomNavigation.itemTextColor = textColors
        
        android.util.Log.d("VisionFocus", "[MainActivity] Applied BottomNavigation colors programmatically")
    }
    
    /**
     * Navigates to SettingsFragment.
     * Story 3.5: Made public for SettingsCommand voice navigation access
     * 
     * Replaces current fragment with SettingsFragment and adds to back stack
     * for proper back navigation.
     */
    fun navigateToSettings() {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        navController.navigate(R.id.settingsFragment)
    }
    
    /**
     * Navigates to HistoryFragment.
     * Story 4.3 Task 10.2 & 10.3: Voice command "History" navigation
     * 
     * Replaces current fragment with HistoryFragment and adds to back stack
     * for proper back navigation.
     */
    fun navigateToHistory() {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        navController.navigate(R.id.historyFragment)
    }
    
    /**
     * Navigate to SavedLocationsFragment.
     * Story 7.2 Task 10: Voice command "Saved locations" navigation
     * 
     * Navigates to saved locations management screen for viewing,
     * editing, and deleting saved locations.
     */
    fun navigateToSavedLocations() {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        navController.navigate(R.id.savedLocationsFragment)
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
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        
        // Check if already on home screen
        val alreadyAtHome = navController.currentDestination?.id == R.id.recognitionFragment
        
        if (alreadyAtHome) {
            // Already at home - announce once and return
            android.util.Log.d("VisionFocus", "Already on home screen")
            ttsManager?.let {
                lifecycleScope.launch {
                    it.announce(getString(R.string.home_screen_announcement))
                }
            }
            return
        }
        
        // Navigate to home (Navigation Component handles back stack)
        navController.navigate(R.id.recognitionFragment)
        
        // Announce AFTER navigation complete
        android.util.Log.d("VisionFocus", "Navigated to home screen")
        lifecycleScope.launch {
            kotlinx.coroutines.delay(100)  // Wait for fragment transaction
            ttsManager?.announce(getString(R.string.home_screen_announcement))
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
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        
        if (navController.previousBackStackEntry != null) {
            // Back stack has entries - pop it
            navController.popBackStack()
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
     * Navigate to destination input screen for navigation feature.
     * Story 6.1: Destination Input via Voice and Text
     * CODE REVIEW FIX: Update bottom navigation selected item when navigating programmatically
     * 
     * Replaces current fragment with DestinationInputFragment and adds to back stack
     * for proper back navigation.
     */
    fun navigateToDestinationInput() {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        navController.navigate(R.id.destinationInputFragment)
        
        // CODE REVIEW FIX: Update bottom navigation selected item to highlight Navigate tab
        binding.bottomNavigation.selectedItemId = R.id.navigation_destination
        android.util.Log.d("VisionFocus", "Navigate tab highlighted after programmatic navigation")
    }
    
    /**
     * Setup bottom navigation menu.
     * Story 6.1 Task 11: Bottom navigation with 3 tabs (Recognition, Navigate, Settings)
     * 
     * Handles navigation between main app sections without adding to back stack.
     */
    private fun setupBottomNavigation() {
        // Story 6.1: Setup with Navigation Component
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        
        // Setup bottom navigation with nav controller
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_recognition -> {
                    navController.navigate(R.id.recognitionFragment)
                    android.util.Log.d("VisionFocus", "Bottom nav: Recognition selected")
                    true
                }
                R.id.navigation_destination -> {
                    navController.navigate(R.id.destinationInputFragment)
                    android.util.Log.d("VisionFocus", "Bottom nav: Navigate selected")
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    android.util.Log.d("VisionFocus", "Bottom nav: Settings selected")
                    true
                }
                else -> false
            }
        }
        
        // Set default selected item (Recognition)
        binding.bottomNavigation.selectedItemId = R.id.navigation_recognition
    }
    
    /**
     * Get current screen identifier for context tracking.
     * Story 3.5 Task 7: Screen context preservation
     * Story 6.1: Updated to use Navigation Component
     * 
     * @return String identifier: "home", "settings", "navigate", or "unknown"
     */
    fun getCurrentScreen(): String {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        return when (navController.currentDestination?.id) {
            R.id.recognitionFragment -> "home"
            R.id.settingsFragment -> "settings"
            R.id.destinationInputFragment -> "navigate"
            R.id.navigationActiveFragment -> "navigation_active"
            else -> "unknown"
        }
    }
    
    /**
     * Navigate to home screen for recognition without clearing back stack.
     * Story 3.5 AC #3: Preserve origin screen for return navigation.
     * Used when RecognizeCommand is issued from Settings/other screens.
     */
    private fun navigateToHomeForRecognition() {
        val navController = binding.navHostFragment.getFragment<androidx.navigation.fragment.NavHostFragment>().navController
        if (navController.currentDestination?.id != R.id.recognitionFragment) {
            navController.navigate(R.id.recognitionFragment)
            android.util.Log.d("VisionFocus", "Navigated to home for recognition (origin: $originScreenBeforeCommand)")
        }
    }
    
    /**
     * Return to origin screen after command execution.
     * Story 3.5 AC #3: Context preservation after command completes.
     * Call this after recognition/operation finishes to return user to origin screen.
     */
    fun returnToOriginScreen() {
        originScreenBeforeCommand?.let { origin ->
            android.util.Log.d("VisionFocus", "Returning to origin screen: $origin")
            when (origin) {
                "settings" -> navigateToSettings()
                "home" -> {
                    // Already home or should be home - no action needed
                    android.util.Log.d("VisionFocus", "Origin was home - staying on home")
                }
                else -> {
                    // Unknown origin - try popping back stack
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                        android.util.Log.d("VisionFocus", "Popped back stack to return to origin")
                    }
                }
            }
            // Clear origin after returning
            originScreenBeforeCommand = null
        } ?: run {
            android.util.Log.d("VisionFocus", "No origin screen to return to")
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
        
        // CRITICAL FIX: Stop NavigationService when MainActivity is destroyed
        // This ensures voice instructions stop when app is killed
        stopNavigationService()
        
        // Note: ObjectRecognitionService and TTSManager are Application-scoped singletons
        // They are NOT cleaned up when Activity is destroyed - they live for app lifetime
        // Cleanup happens when Android OS kills the app process
        // VoiceRecognitionManager is cleaned up by VoiceRecognitionViewModel.onCleared()
    }
    
    /**
     * Stop NavigationService if running.
     * Called when MainActivity is destroyed to ensure foreground service cleanup.
     */
    private fun stopNavigationService() {
        try {
            val intent = Intent(this, com.visionfocus.navigation.service.NavigationService::class.java).apply {
                action = com.visionfocus.navigation.service.NavigationService.ACTION_STOP_NAVIGATION
            }
            startService(intent)
            android.util.Log.d("VisionFocus", "NavigationService stop requested from MainActivity.onDestroy()")
        } catch (e: Exception) {
            android.util.Log.e("VisionFocus", "Failed to stop NavigationService", e)
        }
    }
    
    companion object {
        // Story 3.2: Voice command broadcast actions
        const val ACTION_RECOGNIZE = "com.visionfocus.ACTION_RECOGNIZE"
        const val ACTION_CANCEL = "com.visionfocus.ACTION_CANCEL"
    }
}
