package com.visionfocus.ui.recognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.visionfocus.R
import com.visionfocus.databinding.FragmentRecognitionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ExecutionException

/**
 * Recognition screen fragment with CameraX lifecycle and TalkBack accessibility
 * 
 * Story 2.3: Recognition FAB with complete accessibility integration
 * Story 2.4: Camera capture with accessibility focus management
 * 
 * Features:
 * - CameraX lifecycle binding (Story 2.4 Task 1)
 * - Frame capture with 1s stabilization (Story 2.4 Task 2)
 * - TalkBack state announcements (Story 2.4 Task 3)
 * - Focus restoration to FAB (Story 2.4 Task 4)
 * - Back button cancellation (Story 2.4 Task 5)
 * - Camera permission handling (Story 2.4 Task 7)
 * 
 * Architecture:
 * - MVVM pattern: Fragment observes ViewModel's StateFlow
 * - View Binding: Type-safe view access
 * - Lifecycle-aware: repeatOnLifecycle(STARTED) prevents leaks
 * - CameraX: Modern camera API with lifecycle binding
 */
@AndroidEntryPoint
class RecognitionFragment : Fragment() {
    
    companion object {
        private const val TAG = "RecognitionFragment"
        
        /**
         * Medium intensity haptic feedback duration
         * Story 2.3 AC7: Haptic feedback on FAB tap
         */
        private const val HAPTIC_DURATION_MS = 100L
        
        /**
         * Haptic feedback amplitude (0-255)
         * Medium intensity = 75% of maximum
         */
        private const val HAPTIC_AMPLITUDE = 191 // 75% of 255
        
        /**
         * Camera stabilization delay before capture
         * Story 2.4 AC3: 1 second delay for camera focus/exposure adjustment
         */
        private const val STABILIZATION_DELAY_MS = 1000L
        
        /**
         * Camera restart delay after capture completion
         * LOW-1 FIX: Named constant for camera restart timing
         */
        private const val CAMERA_RESTART_DELAY_MS = 100L
        
        /**
         * HIGH-1 FIX: Long-press threshold for continuous scanning
         * Story 4.4 AC: "long-press FAB (>2 seconds)" requires 2000ms
         */
        private const val LONG_PRESS_THRESHOLD_MS = 2000L
    }
    
    // HIGH-1 FIX: Track long-press state for custom 2-second threshold
    private var longPressStartTime: Long = 0L
    private var isLongPressActive = false
    
    // View Binding
    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel with Hilt injection
    private val viewModel: RecognitionViewModel by viewModels()
    
    // Haptic feedback vibrator
    private val vibrator: Vibrator? by lazy {
        context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    // Story 2.4: CameraX components
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    
    // Story 2.4 Task 4: Focus restoration flag
    private var shouldRestoreFocus = false
    
    // Story 2.7 Task 3: Focus restoration after interruptions (phone call, notification)
    private var lastFocusedViewId: Int? = null
    private var accessibilityManager: AccessibilityManager? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Story 2.7 Task 3.1: Initialize AccessibilityManager for TalkBack state tracking
        accessibilityManager = context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        
        setupAccessibility()
        setupFabClickListener()
        setupBackButtonHandler()
        observeUiState()
        observeScanningState()  // Story 4.4: Observe continuous scanning state
        
        // Story 2.4 Task 1.2: Initialize CameraX if permission granted
        checkPermissionAndStartCamera()
    }
    
    /**
     * Check camera permission and start camera if granted, otherwise show permission UI
     */
    private fun checkPermissionAndStartCamera() {
        if (hasCameraPermission()) {
            // Permission granted - start camera if not already running
            if (imageCapture == null) {
                startCamera()
            }
            // Enable FAB
            binding.recognizeFab.isEnabled = true
        } else {
            // Permission denied - show permission required UI
            handleCameraPermissionDenied()
        }
    }
    
    /**
     * Story 2.3 Task 3: Implement TalkBack semantic annotations
     * 
     * Subtasks:
     * - 3.1: Set contentDescription (already in XML)
     * - 3.2: FAB is focusable with proper accessibility attributes
     * - 3.3: Configure focus order (already in XML)
     * - 3.4-3.6: TalkBack announces correctly (validated in tests)
     * 
     * Implementation note: Using XML attributes for accessibility (contentDescription,
     * android:focusable, accessibilityTraversalAfter) rather than custom delegate.
     * This approach is simpler and equally effective for TalkBack support.
     */
    private fun setupAccessibility() {
        // Ensure FAB is accessible
        binding.recognizeFab.apply {
            isFocusable = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }
    
    /**
     * Story 2.3 Task 4: Implement FAB click handler with haptic feedback
     * Story 4.4 Task 6: Add long-press support for continuous scanning
     * 
     * Click behavior (Story 2.3):
     * - Single tap: Trigger single recognition
     * - Haptic feedback on tap
     * 
     * Long-press behavior (Story 4.4):
     * - Hold for >2 seconds: Activate continuous scanning mode
     * - Haptic feedback on long-press detection
     * 
     * HIGH-1 FIX: Implement custom 2000ms long-press with OnTouchListener
     * Android's setOnLongClickListener uses ~500ms default, not 2000ms as required
     * 
     * HIGH-8 FIX: Stop TTS/announcements before starting new recognition (AC9)
     */
    private fun setupFabClickListener() {
        // HIGH-1 FIX: Use OnTouchListener for custom 2-second long-press
        binding.recognizeFab.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Start tracking long-press
                    longPressStartTime = System.currentTimeMillis()
                    isLongPressActive = true
                    false // Don't consume - allow click listener to work
                }
                android.view.MotionEvent.ACTION_UP -> {
                    // Check if this was a long-press (>2 seconds)
                    val pressDuration = System.currentTimeMillis() - longPressStartTime
                    android.util.Log.e("RECOGNITION_FRAGMENT", "ACTION_UP: duration=${pressDuration}ms, threshold=$LONG_PRESS_THRESHOLD_MS")
                    
                    if (isLongPressActive && pressDuration >= LONG_PRESS_THRESHOLD_MS) {
                        android.util.Log.e("RECOGNITION_FRAGMENT", "LONG PRESS DETECTED - checking camera readiness")
                        // Long-press detected - start continuous scanning
                        // CRITICAL FIX: Check if camera is ready before starting scanning
                        if (imageCapture != null) {
                            android.util.Log.e("RECOGNITION_FRAGMENT", "Camera ready - starting continuous scanning")
                            performHapticFeedback()
                            viewModel.startContinuousScanning()
                            view.announceForAccessibility(getString(R.string.scanning_started))
                        } else {
                            // Camera not ready - announce error
                            android.util.Log.e("RECOGNITION_FRAGMENT", "Camera NOT ready - cannot start scanning")
                            view.announceForAccessibility("Camera not ready. Please wait.")
                            Timber.w("Long-press detected but camera not initialized")
                        }
                        isLongPressActive = false
                        true // Consume event
                    } else {
                        isLongPressActive = false
                        false // Let click listener handle
                    }
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    isLongPressActive = false
                    false
                }
                else -> false
            }
        }
        
        // Single click listener (Story 2.3)
        binding.recognizeFab.setOnClickListener {
            // Check if scanning is active - tap stops scanning
            if (viewModel.scanningState.value is com.visionfocus.recognition.scanning.ScanningState.Scanning) {
                performHapticFeedback()
                viewModel.stopContinuousScanning()
                return@setOnClickListener
            }
            
            // AC9: Stop TalkBack announcements when user activates FAB
            view?.announceForAccessibility("")  // Interrupts current announcement
            
            // Task 4.2-4.3: Trigger haptic feedback on tap
            performHapticFeedback()
            
            // Task 4.1: Trigger recognition pipeline
            viewModel.recognizeObject()
        }
    }
    
    /**
     * Story 2.4 Task 5: Back button cancellation during recognition
     */
    private fun setupBackButtonHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentState = viewModel.uiState.value
                    
                    if (currentState is RecognitionUiState.Recognizing || 
                        currentState is RecognitionUiState.Announcing) {
                        // Cancel recognition if in progress
                        viewModel.cancelRecognition()
                        announceForAccessibility(getString(R.string.recognition_cancelled))
                    } else {
                        // Default back button behavior
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }
    
    /**
     * Story 2.4 Task 1.4: Initialize ProcessCameraProvider
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: ExecutionException) {
                handleCameraError("Camera initialization failed: ${e.message}")
            } catch (e: InterruptedException) {
                handleCameraError("Camera initialization interrupted: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    /**
     * Story 2.4 Task 1.5: Bind CameraX Preview + ImageCapture use cases
     * Fixed: Added delay after unbindAll() to prevent buffer errors
     */
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        // Story 2.4 Task 1.6: Camera selector (back camera)
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        
        // Preview use case (invisible to blind users, but needed for camera warm-up)
        preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
        
        // Story 2.4 Task 2.3: Image capture use case (single frame)
        // MEDIUM-6: Use MAXIMIZE_QUALITY for better recognition (blind users need accuracy)
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(binding.previewView.display?.rotation ?: android.view.Surface.ROTATION_0)
            .build()
        
        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()
            
            // FIX: Add 300ms delay to allow CameraX to fully release resources
            // Without this, rapid FAB taps cause buffer errors (errorCode=3,4,5)
            // 100ms was insufficient - camera still crashed intermittently
            viewLifecycleOwner.lifecycleScope.launch {
                delay(300)
                
                // Story 2.4 Task 1.5: Bind use cases to lifecycle
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                viewModel.onCameraReady()
                
                // Story 4.4 FIX: Initialize recognition camera for continuous scanning
                // This starts the CameraManager instance used by ObjectRecognitionService
                // Must be called after preview camera is bound to avoid conflicts
                Timber.d("Fragment: Initializing recognition camera for continuous scanning")
                viewModel.initializeRecognitionCamera(viewLifecycleOwner)
            }
        } catch (e: Exception) {
            handleCameraError("Camera binding failed: ${e.message}")
        }
    }
    
    /**
     * Story 2.4 Task 2: Capture frame with stabilization delay
     * Fixed: Added capture timeout and camera pause after capture (AC5)
     */
    private fun captureFrame() {
        val imageCapture = imageCapture ?: run {
            handleCameraError("Camera not initialized")
            return
        }
        
        // Story 2.4 MEDIUM-3: Add capture timeout (5 seconds)
        var captureCompleted = false
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)
            if (!captureCompleted && viewModel.uiState.value is RecognitionUiState.Capturing) {
                handleCameraError("Camera capture timeout")
            }
        }
        
        // Story 2.4 Task 2.3: Capture image
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    captureCompleted = true
                    
                    // Story 2.4 Task 2.4: Convert ImageProxy to Bitmap
                    val bitmap = imageProxyToBitmap(image)
                    image.close()
                    
                    // Story 2.4 HIGH-5: Pause camera after capture (AC5 - prevent battery drain)
                    cameraProvider?.unbindAll()
                    this@RecognitionFragment.imageCapture = null  // Clear reference so camera rebinds on next recognition
                    
                    // Story 4.5: Capture screen dimensions for spatial analysis
                    val screenWidth = binding.previewView.width
                    val screenHeight = binding.previewView.height
                    
                    // Edge case: Preview view not measured yet (width/height = 0)
                    if (screenWidth == 0 || screenHeight == 0) {
                        Timber.w("Preview view not measured, spatial analysis disabled")
                        viewModel.performRecognition(bitmap, 0, 0)
                    } else {
                        // Story 2.4 Task 2.5: Pass to recognition pipeline with screen size (Story 4.5)
                        viewModel.performRecognition(bitmap, screenWidth, screenHeight)
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    captureCompleted = true
                    handleCameraError("Image capture failed: ${exception.message}")
                }
            }
        )
    }
    
    /**
     * Story 2.4 Task 2.4: Convert ImageProxy to Bitmap
     * Fixed: Handle both JPEG and YUV formats from CameraX
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        // Check image format - camera might output JPEG or YUV
        return when (image.format) {
            android.graphics.ImageFormat.JPEG -> {
                // Direct JPEG conversion
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            android.graphics.ImageFormat.YUV_420_888 -> {
                // YUV conversion (3 planes required)
                if (image.planes.size < 3) {
                    throw IllegalArgumentException("YUV format requires 3 planes, got ${image.planes.size}")
                }
                
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer
                
                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()
                
                val nv21 = ByteArray(ySize + uSize + vSize)
                
                // Convert YUV planes to NV21 format
                yBuffer.get(nv21, 0, ySize)
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)
                
                // Convert NV21 to JPEG then to Bitmap
                val yuvImage = android.graphics.YuvImage(
                    nv21, 
                    android.graphics.ImageFormat.NV21,
                    image.width, 
                    image.height, 
                    null
                )
                val out = java.io.ByteArrayOutputStream()
                yuvImage.compressToJpeg(
                    android.graphics.Rect(0, 0, image.width, image.height), 
                    100, 
                    out
                )
                val imageBytes = out.toByteArray()
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
            else -> {
                // Unsupported format - log and throw error
                android.util.Log.e("RecognitionFragment", "Unsupported image format: ${image.format}")
                throw IllegalArgumentException("Unsupported camera image format: ${image.format}")
            }
        }
    }
    
    /**
     * Story 2.4 Task 8: Handle camera errors
     */
    private fun handleCameraError(message: String) {
        viewModel.onCameraError(message)
        announceForAccessibility(getString(R.string.camera_error_message))
    }
    
    /**
     * Story 2.4 Task 7: Handle camera permission denial gracefully
     * HIGH-7: Added permission rationale dialog (Task 7.2)
     * HIGH-8: Added settings navigation button (Task 7.5)
     */
    private fun handleCameraPermissionDenied() {
        binding.recognizeFab.isEnabled = false
        binding.recognizeFab.contentDescription = getString(R.string.camera_permission_required)
        announceForAccessibility(getString(R.string.camera_permission_denied_message))
        
        // HIGH-7: Show rationale dialog
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.camera_permission_rationale_title))
            .setMessage(getString(R.string.camera_permission_rationale_message))
            .setPositiveButton(getString(R.string.permission_allow)) { _, _ ->
                // HIGH-8: Navigate to app settings
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.permission_deny), null)
            .show()
    }
    
    /**
     * Story 2.4 Task 7.1: Check camera permission
     */
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Perform medium-intensity haptic feedback
     * 
     * Story 2.3 Task 4.3: Medium intensity (100ms, 75% amplitude)
     * 
     * Implementation note: Inline vibrator implementation for Story 2.3.
     * Future enhancement (Story 5.4): Extract to HapticFeedbackManager with
     * user preference support for intensity levels (Off/Light/Medium/Strong).
     */
    private fun performHapticFeedback() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Modern API: VibrationEffect with amplitude control
                vib.vibrate(
                    VibrationEffect.createOneShot(HAPTIC_DURATION_MS, HAPTIC_AMPLITUDE)
                )
            } else {
                // Legacy API: Simple vibration
                @Suppress("DEPRECATION")
                vib.vibrate(HAPTIC_DURATION_MS)
            }
        }
    }
    
    /**
     * Story 2.3 Task 5: Implement StateFlow observation and UI state updates
     * 
     * Subtasks:
     * - 5.1: Collect viewModel.uiState in viewLifecycleOwner.lifecycleScope
     * - 5.2: Use repeatOnLifecycle(STARTED) for lifecycle-aware collection
     * - 5.3-5.7: Handle all UI states (Idle, Recognizing, Announcing, Success, Error)
     * - 5.8: Ensure TalkBack announces state changes
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }
    
    /**
     * Observe continuous scanning state and update UI
     * Story 4.4 Task 11: Update RecognitionFragment UI for scanning mode
     * 
     * State changes:
     * - Idle: FAB shows camera icon, enables single-tap recognition
     * - Scanning: FAB shows stop icon, tap stops scanning, disables single recognition
     * - Stopping: Brief transitional state before returning to Idle
     * 
     * Accessibility:
     * - Updates FAB contentDescription for TalkBack
     * - Announces state changes via TalkBack
     * - Visual indicator (icon change) for sighted users
     */
    private fun observeScanningState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scanningState.collect { state ->
                    updateScanningUi(state)
                }
            }
        }
    }
    
    /**
     * Update UI based on current scanning state
     * Story 4.4 Task 11: Handle scanning state transitions
     */
    private fun updateScanningUi(state: com.visionfocus.recognition.scanning.ScanningState) {
        when (state) {
            is com.visionfocus.recognition.scanning.ScanningState.Idle -> {
                // Restore normal FAB state
                binding.recognizeFab.setImageResource(R.drawable.ic_camera)
                binding.recognizeFab.contentDescription = getString(R.string.recognize_fab_description)
                binding.recognizeFab.isEnabled = true
                // Note: Single-tap behavior already handles single recognition
            }
            
            is com.visionfocus.recognition.scanning.ScanningState.Scanning -> {
                // Change FAB to stop icon during scanning
                binding.recognizeFab.setImageResource(R.drawable.ic_stop_scanning)
                binding.recognizeFab.contentDescription = getString(R.string.stop_scanning_description)
                binding.recognizeFab.isEnabled = true
                // Note: Click listener already handles stopping scanning when state is Scanning
                
                // Announce scanning active via TalkBack
                announceForAccessibility(getString(R.string.continuous_scanning_active))
            }
            
            is com.visionfocus.recognition.scanning.ScanningState.Stopping -> {
                // Brief transitional state - no UI change needed
                // Summary announcement handled by ContinuousScanner
            }
        }
    }
    
    /**
     * Update UI based on current recognition state
     * 
     * Story 2.3 Task 5: Handle all RecognitionUiState cases
     * Story 2.4: Extended with Capturing and CameraError states
     */
    private fun updateUi(state: RecognitionUiState) {
        when (state) {
            is RecognitionUiState.Idle -> {
                // Task 5.3: Idle state - FAB enabled, default icon
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera)
                
                // Story 2.4 Task 4.2: Restore focus to FAB after recognition
                if (shouldRestoreFocus) {
                    restoreFocusToFab()
                    shouldRestoreFocus = false
                }
                
                // Restart camera if unbound after capture (AC5 requirement)
                if (hasCameraPermission() && imageCapture == null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        // LOW-1 FIX: Named constant for camera restart delay
                        delay(CAMERA_RESTART_DELAY_MS)
                        startCamera()
                    }
                }
            }
            
            is RecognitionUiState.Capturing -> {
                binding.recognizeFab.isEnabled = false
                binding.recognizeFab.setImageResource(R.drawable.ic_camera_analyzing)
                // Story 2.4 Task 3.2: Announce camera capture starting
                announceForAccessibility(getString(R.string.starting_recognition))
                shouldRestoreFocus = true
                
                // Check if camera is initialized, start it if needed
                if (imageCapture == null && hasCameraPermission()) {
                    android.util.Log.d("RecognitionFragment", "Camera not started - initializing now")
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            startCamera()
                            // Wait for camera to be ready before capturing (max 2 seconds)
                            var attempts = 0
                            while (imageCapture == null && attempts < 20) {
                                delay(100)
                                attempts++
                                android.util.Log.d(TAG, "Waiting for camera... attempt $attempts")
                            }
                            if (imageCapture != null) {
                                android.util.Log.d(TAG, "Camera ready, capturing frame")
                                captureFrame()
                            } else {
                                android.util.Log.e(TAG, "Camera initialization timeout")
                                handleCameraError("Camera failed to initialize")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Camera start failed", e)
                            handleCameraError("Camera error: ${e.message}")
                        }
                    }
                } else if (!hasCameraPermission()) {
                    android.util.Log.e(TAG, "Camera permission not granted")
                    handleCameraError("Camera permission not granted")
                } else {
                    // Camera already initialized - capture immediately after stabilization
                    android.util.Log.d(TAG, "Camera already initialized, capturing after delay")
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(STABILIZATION_DELAY_MS)
                        captureFrame()
                    }
                }
            }
            
            is RecognitionUiState.Recognizing -> {
                // Task 5.4: Recognizing state - FAB disabled, analyzing icon
                binding.recognizeFab.isEnabled = false
                binding.recognizeFab.setImageResource(R.drawable.ic_camera_analyzing)
                // Story 2.4 Task 3.3: Announce analyzing
                announceForAccessibility(getString(R.string.analyzing_image))
            }
            
            is RecognitionUiState.Announcing -> {
                // Task 5.5: Announcing state - FAB disabled, TTS playing
                binding.recognizeFab.isEnabled = false
            }
            
            is RecognitionUiState.Success -> {
                // Task 5.6: Success state - FAB re-enabled, default icon
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera)
                // TTS announcement already handled by TTSManager
                // Silent success state (results already announced)
            }
            
            is RecognitionUiState.Error -> {
                // Task 5.7: Error state - FAB re-enabled, error icon
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera_error)
                // Story 2.4 Task 3.4: Announce error
                announceForAccessibility(state.message)
            }
            
            is RecognitionUiState.CameraError -> {
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera_error)
                announceForAccessibility(getString(R.string.camera_error_message))
            }
            
            is RecognitionUiState.Cancelled -> {
                // Story 3.3: Cancelled state - restore to idle immediately
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera)
                // Note: "Cancelled" announcement handled by OperationManager
                // Restore focus to FAB for next command
                if (shouldRestoreFocus) {
                    restoreFocusToFab()
                    shouldRestoreFocus = false
                }
            }
        }
    }
    
    /**
     * Story 2.4 Task 4.3: Restore focus to FAB
     */
    private fun restoreFocusToFab() {
        binding.recognizeFab.post {
            binding.recognizeFab.requestFocus()
            binding.recognizeFab.sendAccessibilityEvent(
                android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED
            )
        }
    }
    
    /**
     * Announce message for TalkBack users
     * 
     * Story 2.3 Task 5.8: State change announcements
     * Story 2.7 Task 9: Enhanced announcement implementation with queueing prevention
     * 
     * Implementation Strategy:
     * - Uses View.announceForAccessibility() for non-interrupting announcements
     * - Sets ACCESSIBILITY_LIVE_REGION_POLITE on FAB (announces when TalkBack idle)
     * - Posts to main thread to ensure view is ready
     * - Checks fragment lifecycle to prevent crashes after destruction
     * 
     * AC6: TalkBack announces state changes: "Loading", "Recognition complete", "Error occurred"
     * AC9: TalkBack reading stops when user double-taps to activate action
     * 
     * Announcement Timing:
     * - State machine prevents overlaps: 1s stabilization + 320ms inference + TTS duration
     * - No explicit queueing needed - announcements naturally spaced by state transitions
     * 
     * @param message The message to announce to TalkBack users (concise, actionable)
     */
    private fun announceForAccessibility(message: String) {
        // Check if binding is still valid (Fragment not destroyed)
        if (!isAdded || _binding == null) {
            android.util.Log.w(TAG, "Story 2.7: Skipping announcement - Fragment destroyed")
            return
        }
        
        // Story 2.4: Set live region polite (non-interrupting)
        // TalkBack announces message when current speech finishes
        binding.recognizeFab.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        
        // Post to main thread to ensure view is ready
        // AC9: TalkBack stops reading when user double-taps (handled automatically by Android)
        binding.recognizeFab.post {
            binding.recognizeFab.announceForAccessibility(message)
            android.util.Log.d(TAG, "Story 2.7: TalkBack announcement: $message")
        }
    }
    
    /**
     * Check if accessibility service (TalkBack) is enabled
     * 
     * @return true if TalkBack or similar accessibility service is active
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        return accessibilityManager?.isEnabled == true && accessibilityManager?.isTouchExplorationEnabled == true
    }
    
    /**
     * Story 2.7 Task 3.2: Save focus state before fragment pauses
     * Handles interruptions: phone calls, notifications, split-screen mode changes
     * 
     * HIGH-3 FIX: Always save focus state (onResume decides whether to restore)
     */
    override fun onPause() {
        super.onPause()
        
        // Always save focus state - restoration logic checks TalkBack state
        val focusedView = view?.findFocus()
        lastFocusedViewId = focusedView?.id
        android.util.Log.d(TAG, "Story 2.7: Saved focus on view ID: $lastFocusedViewId")
        
        // Story 2.4 HIGH-3: Release camera when fragment paused to prevent battery drain
        cameraProvider?.unbindAll()
    }
    
    /**
     * Story 2.7 Task 3.3: Restore focus when fragment resumes
     * Story 2.4 HIGH-9: Re-check permission and re-bind camera on resume
     * 
     * HIGH-1 FIX: Restore camera first, then restore focus after views stabilize
     */
    override fun onResume() {
        super.onResume()
        
        // Story 2.4: Re-check permission when returning from Settings (must come first)
        checkPermissionAndStartCamera()
        
        // Story 2.7 Task 3.3: Restore focus if TalkBack is active (after camera binding)
        if (isAccessibilityServiceEnabled() && lastFocusedViewId != null) {
            view?.post {  // Post to ensure camera binding completes
                val viewToFocus = view?.findViewById<View>(lastFocusedViewId!!)
                    ?: binding.recognizeFab  // Default to FAB if saved view not found
                
                viewToFocus.requestFocus()
                viewToFocus.sendAccessibilityEvent(
                    android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED
                )
                android.util.Log.d(TAG, "Story 2.7: Restored focus to view ID: ${viewToFocus.id}")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Story 2.4 Task 1.7: Unbind camera use cases when view destroyed
        cameraProvider?.unbindAll()
        
        // HIGH-2 FIX: Clear system service reference to prevent memory leak
        accessibilityManager = null
        
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}
