package com.visionfocus.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.visionfocus.R
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.data.model.HapticIntensity
import com.visionfocus.databinding.FragmentSettingsBinding
import com.visionfocus.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings screen for theme preferences and haptic feedback.
 * 
 * Manages high-contrast mode, large text mode, and haptic intensity controls,
 * applying theme changes via ThemeManager with activity recreation.
 * 
 * Story 2.6 Extensions:
 * - Haptic intensity RadioGroup (OFF, LIGHT, MEDIUM, STRONG)
 * - Sample vibration trigger on intensity selection
 * - Dynamic content descriptions for TalkBack
 * 
 * StateFlow Observation Pattern:
 * Uses repeatOnLifecycle(STARTED) to:
 * - Start collection when Fragment is visible (onStart)
 * - Stop collection when Fragment is hidden (onStop)
 * - Prevents memory leaks and unnecessary work in background
 * 
 * Theme Change Flow:
 * 1. User toggles switch
 * 2. Switch listener calls ViewModel.toggle*()
 * 3. ViewModel updates DataStore preference
 * 4. DataStore emits new value through Flow
 * 5. Observer in Fragment receives update
 * 6. ThemeManager.applyTheme() recreates activity
 * 7. MainActivity.onCreate() applies theme on restart
 * 
 * Accessibility:
 * - Switch content descriptions update dynamically ("currently on/off")
 * - RadioButton content descriptions for each haptic intensity
 * - TalkBack announcements for theme changes and haptic intensity changes
 * - Explanation text marked importantForAccessibility="no" (redundant)
 * - Minimum 48×48 dp touch targets enforced
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    // Story 2.6: Inject HapticFeedbackManager for sample vibrations
    @Inject
    lateinit var hapticFeedbackManager: HapticFeedbackManager
    
    // Guard flag to prevent double theme application (HIGH-2 fix)
    private var isUpdatingFromObserver = false
    
    // Guard: Track last intensity to prevent duplicate sample vibrations (Samsung device fix)
    private var lastHapticIntensity: HapticIntensity? = null
    
    // Job tracking for memory leak prevention (MEDIUM-2 fix)
    private val observerJobs = mutableListOf<kotlinx.coroutines.Job>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    /**
     * Sets up StateFlow observers for theme preferences.
     * 
     * repeatOnLifecycle(STARTED) ensures:
     * - Collection starts when Fragment is visible
     * - Collection stops when Fragment is hidden
     * - No memory leaks from active coroutines
     */
    private fun setupObservers() {
        // Observe high-contrast mode preference
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.highContrastMode.collect { enabled ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        binding.highContrastSwitch.isChecked = enabled
                        isUpdatingFromObserver = false
                        
                        // Update content description for TalkBack
                        binding.highContrastSwitch.contentDescription = getString(
                            if (enabled) R.string.high_contrast_mode_description_on
                            else R.string.high_contrast_mode_description_off
                        )
                    }
                }
            }
        )
        
        // Observe large text mode preference
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.largeTextMode.collect { enabled ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        binding.largeTextSwitch.isChecked = enabled
                        isUpdatingFromObserver = false
                        
                        // Update content description for TalkBack
                        binding.largeTextSwitch.contentDescription = getString(
                            if (enabled) R.string.large_text_mode_description_on
                            else R.string.large_text_mode_description_off
                        )
                    }
                }
            }
        )
        
        // Observe haptic intensity preference (Story 2.6)
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.hapticIntensity.collect { intensity ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        
                        // Update radio button selection based on intensity
                        when (intensity) {
                            HapticIntensity.OFF -> binding.hapticOff.isChecked = true
                            HapticIntensity.LIGHT -> binding.hapticLight.isChecked = true
                            HapticIntensity.MEDIUM -> binding.hapticMedium.isChecked = true
                            HapticIntensity.STRONG -> binding.hapticStrong.isChecked = true
                        }
                        
                        isUpdatingFromObserver = false
                    }
                }
            }
        )
    }
    
    /**
     * Sets up switch listeners for theme toggle actions and haptic intensity selection.
     * 
     * Listener Pattern:
     * - setOnCheckedChangeListener triggers on user interaction
     * - Does NOT trigger when isChecked is set programmatically
     * - Calls ViewModel methods to update preferences
     * - Announces changes via TalkBack
     * - Applies theme changes immediately via activity recreation
     * 
     * Story 2.6: Added haptic intensity RadioGroup listener with sample vibration
     * 
     * KNOWN ISSUE (Dec 30, 2024):
     * Toggles only work ONCE per app launch. After first toggle (e.g., HC ON),
     * subsequent toggles (HC OFF) don't work until app restart. Root cause unclear -
     * possibly related to recreate() lifecycle, observer state, or listener registration.
     * TODO: Debug why toggle works once but not repeatedly after recreate().
     */
    private fun setupListeners() {
        binding.highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Guard: Ignore programmatic updates from observer (HIGH-2 fix)
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            android.util.Log.d("VisionFocus", "[Fragment] High-contrast toggle: isChecked=$isChecked, guard=$isUpdatingFromObserver")
            
            // Announce theme change via TalkBack
            val modeLabel = getString(R.string.high_contrast_mode_label)
            val stateLabel = getString(
                if (isChecked) R.string.theme_enabled else R.string.theme_disabled
            )
            binding.root.announceForAccessibility(
                getString(R.string.theme_change_announcement, modeLabel, stateLabel)
            )
            
            // Save preference and recreate activity (wait for DataStore write to complete)
            viewLifecycleOwner.lifecycleScope.launch {
                android.util.Log.d("VisionFocus", "[Fragment] Calling setHighContrastMode($isChecked)")
                viewModel.setHighContrastMode(isChecked)
                android.util.Log.d("VisionFocus", "[Fragment] Calling requireActivity().recreate()")
                requireActivity().recreate()
                android.util.Log.d("VisionFocus", "[Fragment] recreate() returned (should not see this)")
            }
        }
        
        binding.largeTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Guard: Ignore programmatic updates from observer (HIGH-2 fix)
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            android.util.Log.d("VisionFocus", "[Fragment] Large text toggle: isChecked=$isChecked, guard=$isUpdatingFromObserver")
            
            // Announce theme change via TalkBack
            val modeLabel = getString(R.string.large_text_mode_label)
            val stateLabel = getString(
                if (isChecked) R.string.theme_enabled else R.string.theme_disabled
            )
            binding.root.announceForAccessibility(
                getString(R.string.theme_change_announcement, modeLabel, stateLabel)
            )
            
            // Save preference and recreate activity (wait for DataStore write to complete)
            viewLifecycleOwner.lifecycleScope.launch {
                android.util.Log.d("VisionFocus", "[Fragment] Calling setLargeTextMode($isChecked)")
                viewModel.setLargeTextMode(isChecked)
                android.util.Log.d("VisionFocus", "[Fragment] Calling requireActivity().recreate()")
                requireActivity().recreate()
                android.util.Log.d("VisionFocus", "[Fragment] recreate() returned (should not see this)")
            }
        }
        
        // Story 2.6: Haptic intensity RadioGroup listener
        binding.hapticIntensityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Guard: Ignore programmatic updates from observer
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            val intensity = when (checkedId) {
                R.id.hapticOff -> HapticIntensity.OFF
                R.id.hapticLight -> HapticIntensity.LIGHT
                R.id.hapticMedium -> HapticIntensity.MEDIUM
                R.id.hapticStrong -> HapticIntensity.STRONG
                else -> HapticIntensity.MEDIUM // fallback
            }
            
            // CRITICAL FIX: Samsung devices retrigger listener on every touch - deduplicate
            if (intensity == lastHapticIntensity) {
                android.util.Log.d("VisionFocus", "[Fragment] Duplicate haptic trigger ignored: $intensity")
                return@setOnCheckedChangeListener
            }
            lastHapticIntensity = intensity
            
            android.util.Log.d("VisionFocus", "[Fragment] Haptic intensity changed: $intensity")
            
            // Announce haptic intensity change via TalkBack
            val intensityLabel = when (intensity) {
                HapticIntensity.OFF -> getString(R.string.haptic_intensity_off)
                HapticIntensity.LIGHT -> getString(R.string.haptic_intensity_light)
                HapticIntensity.MEDIUM -> getString(R.string.haptic_intensity_medium)
                HapticIntensity.STRONG -> getString(R.string.haptic_intensity_strong)
            }
            binding.root.announceForAccessibility(
                getString(R.string.haptic_sample_triggered, intensityLabel)
            )
            
            // HIGH-8 FIX: Save preference BEFORE triggering sample to prevent race condition
            // This ensures rapid intensity changes don't cause state desync
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.setHapticIntensity(intensity)  // Await DataStore write
                
                // CRITICAL FIX: Don't trigger sample for OFF intensity (amplitude=0 crashes on Android API 26+)
                if (intensity != HapticIntensity.OFF) {
                    hapticFeedbackManager.triggerSample(intensity)  // Then trigger sample
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel all observer jobs to prevent memory leaks (MEDIUM-2 fix)
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
        _binding = null
    }
}
