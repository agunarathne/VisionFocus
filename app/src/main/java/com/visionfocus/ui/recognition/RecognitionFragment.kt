package com.visionfocus.ui.recognition

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.visionfocus.R
import com.visionfocus.databinding.FragmentRecognitionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Recognition screen fragment with TalkBack accessibility support
 * 
 * Story 2.3: Recognition FAB with complete accessibility integration
 * - Task 2: Fragment with XML layout and View Binding
 * - Task 3: TalkBack semantic annotations
 * - Task 4: FAB click handler with haptic feedback
 * - Task 5: StateFlow observation and UI state updates
 * - Task 6: High-contrast mode theme support (applied via theme)
 * 
 * Architecture:
 * - MVVM pattern: Fragment observes ViewModel's StateFlow
 * - View Binding: Type-safe view access without findViewById()
 * - Lifecycle-aware: repeatOnLifecycle(STARTED) prevents leaks
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
    }
    
    // View Binding (Story 2.3 Task 2.7)
    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel with Hilt injection (Story 2.3 Task 2.8)
    private val viewModel: RecognitionViewModel by viewModels()
    
    // Haptic feedback vibrator
    private val vibrator: Vibrator? by lazy {
        context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
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
        
        // Story 2.3 Task 3: Setup TalkBack semantic annotations
        setupAccessibility()
        
        // Story 2.3 Task 4: Setup FAB click handler with haptic feedback
        setupFabClickListener()
        
        // Story 2.3 Task 5: Observe UI state and update views
        observeUiState()
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
     * 
     * Subtasks:
     * - 4.1: Set FAB onClickListener calling viewModel.recognizeObject()
     * - 4.2-4.3: Trigger medium-intensity haptic vibration (100ms)
     * - 4.4: Implement haptic pattern (single short vibration)
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
     */
    private fun updateUi(state: RecognitionUiState) {
        when (state) {
            is RecognitionUiState.Idle -> {
                // Task 5.3: Idle state - FAB enabled, default icon
                binding.recognizeFab.isEnabled = true
                binding.recognizeFab.setImageResource(R.drawable.ic_camera)
                announceForAccessibility(getString(R.string.state_idle))
            }
            
            is RecognitionUiState.Recognizing -> {
                // Task 5.4: Recognizing state - FAB disabled, analyzing icon
                binding.recognizeFab.isEnabled = false
                binding.recognizeFab.setImageResource(R.drawable.ic_camera_analyzing)
                announceForAccessibility(getString(R.string.state_recognizing))
            }
            
            is RecognitionUiState.Announcing -> {
                // Task 5.5: Announcing state - FAB disabled, TTS playing
                binding.recognizeFab.isEnabled = false
                announceForAccessibility(getString(R.string.state_announcing))
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
                announceForAccessibility(
                    getString(R.string.state_error, state.message)
                )
            }
        }
    }
    
    /**
     * Announce message for TalkBack users
     * 
     * Story 2.3 Task 5.8: State change announcements
     * 
     * Uses View.announceForAccessibility() which is handled by TalkBack automatically
     */
    private fun announceForAccessibility(message: String) {
        // Priority: Use TalkBack-specific announcement
        binding.root.announceForAccessibility(message)
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}
