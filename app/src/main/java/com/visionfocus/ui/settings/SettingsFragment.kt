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
import com.visionfocus.data.model.VerbosityMode
import com.visionfocus.databinding.FragmentSettingsBinding
import com.visionfocus.theme.ThemeManager
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.engine.VoiceOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.widget.SeekBar
import android.widget.RadioButton

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
    
    // Story 5.1: Inject TTSManager for speech rate control
    @Inject
    lateinit var ttsManager: TTSManager
    
    // Guard flag to prevent double theme application (HIGH-2 fix)
    private var isUpdatingFromObserver = false
    
    // Guard: Track last intensity to prevent duplicate sample vibrations (Samsung device fix)
    private var lastHapticIntensity: HapticIntensity? = null
    
    // Guard: Track last verbosity mode to prevent duplicate announcements (Story 4.1)
    private var lastVerbosityMode: VerbosityMode? = null
    
    // Story 5.2: Track last voice locale to prevent duplicate samples
    private var lastVoiceLocale: String? = null
    
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
        // Story 5.1: Observe speech rate preference
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.speechRate.collect { rate ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        
                        // Update SeekBar position (convert rate to progress: 0.5-2.0 → 0-15)
                        val progress = speechRateToProgress(rate)
                        binding.speechRateSeekBar.progress = progress
                        
                        // Update current rate display
                        binding.currentRateTextView.text = String.format("%.1f×", rate)
                        
                        // Update content description for TalkBack
                        // MEDIUM-3 FIX: Use format string instead of brittle string replacement
                        binding.speechRateSeekBar.contentDescription = String.format(
                            "Speech rate, slider, currently %.1f times normal speed",
                            rate
                        )
                        
                        isUpdatingFromObserver = false
                    }
                }
            }
        )
        
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
        
        // Observe verbosity mode preference (Story 4.1)
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.verbosityMode.collect { mode ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        
                        // Update radio button selection based on verbosity mode
                        when (mode) {
                            VerbosityMode.BRIEF -> binding.verbosityBriefRadio.isChecked = true
                            VerbosityMode.STANDARD -> binding.verbosityStandardRadio.isChecked = true
                            VerbosityMode.DETAILED -> binding.verbosityDetailedRadio.isChecked = true
                        }
                        
                        isUpdatingFromObserver = false
                    }
                }
            }
        )
        
        // Observe camera preview enabled preference (Testing/Development)
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.cameraPreviewEnabled.collect { enabled ->
                        // Set guard flag to prevent listener from triggering during update
                        isUpdatingFromObserver = true
                        binding.cameraPreviewSwitch.isChecked = enabled
                        isUpdatingFromObserver = false
                        
                        // Update content description for TalkBack
                        binding.cameraPreviewSwitch.contentDescription = getString(
                            if (enabled) R.string.camera_preview_description_on
                            else R.string.camera_preview_description_off
                        )
                    }
                }
            }
        )
        
        // Story 5.2: Observe available voices and voice locale
        // MEDIUM-6 FIX: Combine flows to prevent double population on startup
        observerJobs.add(
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    kotlinx.coroutines.flow.combine(
                        viewModel.availableVoices,
                        viewModel.voiceLocale
                    ) { voices, locale ->
                        Pair(voices, locale)
                    }.collect { (voices, locale) ->
                        populateVoiceSelector(voices, locale)
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
        // Story 5.1: Speech rate SeekBar listener
        binding.speechRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Guard: Ignore programmatic updates from observer
                if (isUpdatingFromObserver) return
                
                // Update current rate display
                val rate = progressToSpeechRate(progress)
                binding.currentRateTextView.text = String.format("%.1f×", rate)
                
                // Update content description for TalkBack
                // MEDIUM-3 FIX: Use format string instead of brittle string replacement
                binding.speechRateSeekBar.contentDescription = String.format(
                    "Speech rate, slider, currently %.1f times normal speed",
                    rate
                )
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // User started dragging slider
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Guard: Ignore programmatic updates from observer
                if (isUpdatingFromObserver) return
                
                // User released slider - save preference and apply to TTS
                val rate = progressToSpeechRate(seekBar?.progress ?: 5)
                android.util.Log.d("VisionFocus", "[Fragment] Speech rate changed to: $rate")
                
                // Announce rate change via TalkBack
                binding.root.announceForAccessibility(
                    getString(R.string.speech_rate_changed, rate)
                )
                
                // Save preference, apply to TTS, and play sample (AC: sample plays on slider change)
                // MEDIUM-2 FIX: Auto-play sample announcement when slider changes
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.setSpeechRate(rate)
                    ttsManager.setSpeechRate(rate)
                    // AC requirement: "sample announcement plays when slider changes"
                    viewModel.playSampleAnnouncement()
                }
            }
        })
        
        // Story 5.1: Test Speed button listener
        binding.testSpeedButton.setOnClickListener {
            val rate = viewModel.speechRate.value
            android.util.Log.d("VisionFocus", "[Fragment] Test speed button clicked, rate=$rate")
            
            // Play sample announcement at current rate
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.playSampleAnnouncement()
            }
        }
        
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
        
        binding.cameraPreviewSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Guard: Ignore programmatic updates from observer
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            // Save preference immediately (no recreation needed)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.setCameraPreviewEnabled(isChecked)
                // Announce change
                binding.root.announceForAccessibility(
                    "Camera preview ${if (isChecked) "enabled" else "disabled"}"
                )
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
        
        // Story 4.1: Verbosity mode RadioGroup listener
        binding.verbosityModeGroup.setOnCheckedChangeListener { _, checkedId ->
            // Guard: Ignore programmatic updates from observer
            if (isUpdatingFromObserver) return@setOnCheckedChangeListener
            
            val mode = when (checkedId) {
                R.id.verbosityBriefRadio -> VerbosityMode.BRIEF
                R.id.verbosityStandardRadio -> VerbosityMode.STANDARD
                R.id.verbosityDetailedRadio -> VerbosityMode.DETAILED
                else -> VerbosityMode.STANDARD // fallback
            }
            
            // Deduplicate to prevent multiple announcements
            if (mode == lastVerbosityMode) {
                android.util.Log.d("VisionFocus", "[Fragment] Duplicate verbosity trigger ignored: $mode")
                return@setOnCheckedChangeListener
            }
            lastVerbosityMode = mode
            
            android.util.Log.d("VisionFocus", "[Fragment] Verbosity mode changed: $mode")
            
            // Announce verbosity mode change via TalkBack
            val modeLabel = when (mode) {
                VerbosityMode.BRIEF -> getString(R.string.verbosity_set_to_brief)
                VerbosityMode.STANDARD -> getString(R.string.verbosity_set_to_standard)
                VerbosityMode.DETAILED -> getString(R.string.verbosity_set_to_detailed)
            }
            binding.root.announceForAccessibility(modeLabel)
            
            // Save preference
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.setVerbosityMode(mode)
            }
        }
    }
    
    /**
     * Story 5.1: Convert SeekBar progress (0-15) to speech rate (0.5-2.0)
     * 
     * Formula: rate = 0.5 + (progress * 0.1)
     * - progress 0 → 0.5×
     * - progress 5 → 1.0× (default)
     * - progress 15 → 2.0×
     */
    private fun progressToSpeechRate(progress: Int): Float {
        return 0.5f + (progress * 0.1f)
    }
    
    /**
     * Story 5.1: Convert speech rate (0.5-2.0) to SeekBar progress (0-15)
     * 
     * Formula: progress = (rate - 0.5) / 0.1
     * - rate 0.5× → progress 0
     * - rate 1.0× → progress 5
     * - rate 2.0× → progress 15
     */
    private fun speechRateToProgress(rate: Float): Int {
        return ((rate - 0.5f) / 0.1f).toInt().coerceIn(0, 15)
    }
    
    /**
     * Story 5.2 Task 5.2: Populate voice RadioGroup with available voices
     * 
     * Dynamically generates RadioButton items from available voices.
     * 
     * @param voices List of available voice options
     * @param currentVoiceLocale Currently selected voice locale (or null for default)
     */
    private fun populateVoiceSelector(voices: List<VoiceOption>, currentVoiceLocale: String?) {
        binding.voiceRadioGroup.removeAllViews()
        
        if (voices.isEmpty()) {
            // No additional voices - show system default only
            binding.noVoicesMessage.visibility = View.VISIBLE
            binding.voiceRadioGroup.visibility = View.GONE
            return
        }
        
        binding.noVoicesMessage.visibility = View.GONE
        binding.voiceRadioGroup.visibility = View.VISIBLE
        
        voices.forEach { voiceOption ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = voiceOption.displayName
                contentDescription = "${voiceOption.displayName} voice, radio button"
                minHeight = resources.getDimensionPixelSize(R.dimen.min_touch_target_size)  // 48dp
                setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                
                // Mark selected if matches current voice locale
                isChecked = (voiceOption.locale == currentVoiceLocale)
                
                // Set click listener for sample preview
                setOnClickListener {
                    onVoiceSelected(voiceOption)
                }
            }
            
            binding.voiceRadioGroup.addView(radioButton)
        }
    }
    
    /**
     * Story 5.2 Task 5.5: Update voice RadioGroup selection
     * 
     * Called when voiceLocale Flow emits new value.
     * 
     * @param locale Currently selected voice locale (or null for default)
     */
    private fun updateVoiceSelection(locale: String?) {
        isUpdatingFromObserver = true
        
        // Find RadioButton matching the locale and check it
        for (i in 0 until binding.voiceRadioGroup.childCount) {
            val radioButton = binding.voiceRadioGroup.getChildAt(i) as? RadioButton ?: continue
            val voiceOption = viewModel.availableVoices.value.getOrNull(i) ?: continue
            
            radioButton.isChecked = (voiceOption.locale == locale)
        }
        
        isUpdatingFromObserver = false
    }
    
    /**
     * Story 5.2 Task 5.5: Handle voice selection from RadioGroup
     * 
     * Persists selection and plays sample preview.
     * 
     * @param voiceOption Selected voice option
     */
    private fun onVoiceSelected(voiceOption: VoiceOption) {
        // Guard: Ignore programmatic updates
        if (isUpdatingFromObserver) return
        
        // Deduplicate to prevent multiple samples
        if (voiceOption.locale == lastVoiceLocale) {
            android.util.Log.d("VisionFocus", "[Fragment] Duplicate voice trigger ignored: ${voiceOption.locale}")
            return
        }
        lastVoiceLocale = voiceOption.locale
        
        android.util.Log.d("VisionFocus", "[Fragment] Voice selected: ${voiceOption.displayName}")
        
        // Persist selection
        viewModel.setVoiceLocale(voiceOption.locale)
        
        // Play sample announcement in selected voice
        viewModel.playSampleWithVoice(
            locale = voiceOption.locale,
            text = getString(R.string.voice_preview_text)
        )
        
        // Update TalkBack announcement
        binding.root.announceForAccessibility(
            getString(R.string.voice_changed, voiceOption.displayName)
        )
    }
    
    /**
     * Extension function to convert dp to pixels
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel all observer jobs to prevent memory leaks (MEDIUM-2 fix)
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
        _binding = null
    }
}
