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
    }
    
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
        
        setupAccessibility()
        setupFabClickListener()
        setupBackButtonHandler()
        observeUiState()
        
        // Story 2.4 Task 1.2: Initialize CameraX if permission granted
        if (hasCameraPermission()) {
            startCamera()
        } else {
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
     */
    private fun setupFabClickListener() {
        binding.recognizeFab.setOnClickListener {
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
            
            // Story 2.4 Task 1.5: Bind use cases to lifecycle
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            viewModel.onCameraReady()
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
                    
                    // Story 2.4 Task 2.5: Pass to recognition pipeline
                    viewModel.performRecognition(bitmap)
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
                        // Small delay to ensure state is stable before camera restart
                        delay(100)
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
                    startCamera()
                    // Wait for camera to be ready before capturing
                    viewLifecycleOwner.lifecycleScope.launch {
                        // Wait for camera to bind (max 2 seconds)
                        var attempts = 0
                        while (imageCapture == null && attempts < 20) {
                            delay(100)
                            attempts++
                        }
                        if (imageCapture != null) {
                            captureFrame()
                        } else {
                            handleCameraError("Camera failed to initialize")
                        }
                    }
                } else if (!hasCameraPermission()) {
                    handleCameraError("Camera permission not granted")
                } else {
                    // Camera already initialized - capture immediately after stabilization
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
     * Fix: Use ACCESSIBILITY_LIVE_REGION_POLITE for more reliable announcements
     * 
     * Uses View.announceForAccessibility() which is handled by TalkBack automatically
     */
    private fun announceForAccessibility(message: String) {
        // Use post() to ensure announcement happens on UI thread after state updates
        view?.post {
            // Check if binding is still valid (Fragment not destroyed)
            if (!isAdded || _binding == null) {
                android.util.Log.w("RecognitionFragment", "Skipping announcement - Fragment destroyed")
                return@post
            }
            
            // Set live region for more reliable announcements
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                binding.root.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            }
            
            // Make announcement
            binding.root.announceForAccessibility(message)
            
            // Log for debugging
            android.util.Log.d("RecognitionFragment", "TalkBack announcement: $message")
        }
    }
    
    /**
     * Check if accessibility service (TalkBack) is enabled
     * 
     * @return true if TalkBack or similar accessibility service is active
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return accessibilityManager?.isEnabled == true
    }
    
    /**
     * Story 2.4 HIGH-3: Implement camera lifecycle pause (Task 1.7)
     * Release camera when fragment paused to prevent battery drain
     */
    override fun onPause() {
        super.onPause()
        cameraProvider?.unbindAll()
    }
    
    /**
     * Story 2.4 HIGH-9: Re-check permission and re-bind camera on resume (Task 7.6)
     * User may have granted permission from Settings
     */
    override fun onResume() {
        super.onResume()
        
        // Re-check permission and start camera if granted and not already bound
        if (hasCameraPermission() && imageCapture == null) {
            startCamera()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Story 2.4 Task 1.7: Unbind camera use cases when view destroyed
        cameraProvider?.unbindAll()
        
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}
