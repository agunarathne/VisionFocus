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
import com.visionfocus.databinding.FragmentSettingsBinding
import com.visionfocus.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Settings screen for theme preferences.
 * 
 * Manages high-contrast mode and large text mode toggles,
 * applying theme changes via ThemeManager with activity recreation.
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
 * - TalkBack announcements for theme changes
 * - Explanation text marked importantForAccessibility="no" (redundant)
 * - Minimum 48×48 dp touch targets enforced
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    // Guard flag to prevent double theme application (HIGH-2 fix)
    private var isUpdatingFromObserver = false
    
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
    }
    
    /**
     * Sets up switch listeners for theme toggle actions.
     * 
     * Listener Pattern:
     * - setOnCheckedChangeListener triggers on user interaction
     * - Does NOT trigger when isChecked is set programmatically
     * - Calls ViewModel.toggle*() to update preference
     * - Announces theme change via TalkBack
     * - Applies theme immediately via ThemeManager.applyTheme()
     */
    private fun setupListeners() {
        binding.highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Guard: Ignore programmatic updates from observer (HIGH-2 fix)
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            android.util.Log.d("VisionFocus", "High-contrast mode toggled: $isChecked")
            viewModel.toggleHighContrastMode()
            
            // Announce theme change via TalkBack
            val modeLabel = getString(R.string.high_contrast_mode_label)
            val stateLabel = getString(
                if (isChecked) R.string.theme_enabled else R.string.theme_disabled
            )
            binding.root.announceForAccessibility(
                getString(R.string.theme_change_announcement, modeLabel, stateLabel)
            )
            
            // Apply theme immediately
            ThemeManager.applyTheme(
                requireContext(),
                isChecked,
                viewModel.largeTextMode.value
            )
        }
        
        binding.largeTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Guard: Ignore programmatic updates from observer (HIGH-2 fix)
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            android.util.Log.d("VisionFocus", "Large text mode toggled: $isChecked")
            viewModel.toggleLargeTextMode()
            
            // Announce theme change via TalkBack
            val modeLabel = getString(R.string.large_text_mode_label)
            val stateLabel = getString(
                if (isChecked) R.string.theme_enabled else R.string.theme_disabled
            )
            binding.root.announceForAccessibility(
                getString(R.string.theme_change_announcement, modeLabel, stateLabel)
            )
            
            // Apply theme immediately
            ThemeManager.applyTheme(
                requireContext(),
                viewModel.highContrastMode.value,
                isChecked
            )
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
