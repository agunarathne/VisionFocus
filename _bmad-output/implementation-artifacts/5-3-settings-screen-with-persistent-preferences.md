# Story 5.3: Settings Screen with Persistent Preferences

Status: ready-for-dev

## Story

As a visually impaired user,
I want all my settings saved automatically,
So that I don't have to reconfigure the app every time I use it.

## Acceptance Criteria

**Given** Settings screen with multiple preferences
**When** I change any setting
**Then** setting saves immediately to DataStore (no explicit "Save" button required)
**And** settings persist across app restarts (verified by closing and reopening app)
**And** settings available include: Speech rate (Story 5.1), Voice selection (Story 5.2), Verbosity mode (Story 4.1), High-contrast mode (Story 2.5), Haptic intensity (Story 2.6), Large text mode
**And** all settings have proper TalkBack labels and focus order
**And** settings screen title announces: "Settings"
**And** "Reset to defaults" button restores all settings to default values with confirmation dialog
**And** reset confirmation dialog: "Reset all settings to defaults? This cannot be undone."
**And** reset completion announces: "Settings reset to defaults"

## Tasks / Subtasks

- [ ] Task 1: Create SettingsViewModel with Preference Management (AC: 1, 2)
  - [ ] 1.1: Create ui/settings package structure
  - [ ] 1.2: Create SettingsViewModel.kt extending ViewModel with @HiltViewModel annotation
  - [ ] 1.3: Inject SettingsRepository via @Inject constructor
  - [ ] 1.4: Expose speechRate: StateFlow<Float> from repository using stateIn()
  - [ ] 1.5: Expose verbosityMode: StateFlow<VerbosityMode> from repository using stateIn()
  - [ ] 1.6: Expose highContrastMode: StateFlow<Boolean> from repository using stateIn()
  - [ ] 1.7: Expose largeTextMode: StateFlow<Boolean> from repository using stateIn()
  - [ ] 1.8: Implement updateSpeechRate(rate: Float) calling repository.setSpeechRate()
  - [ ] 1.9: Implement updateVerbosityMode(mode: VerbosityMode) calling repository.setVerbosity()
  - [ ] 1.10: Implement updateHighContrastMode(enabled: Boolean) calling repository.setHighContrastMode()
  - [ ] 1.11: Implement updateLargeTextMode(enabled: Boolean) calling repository.setLargeTextMode()
  - [ ] 1.12: Implement resetToDefaults() function restoring all preferences to defaults

- [ ] Task 2: Create Settings Screen Layout with Material Switches (AC: 3, 4)
  - [ ] 2.1: Create res/layout/fragment_settings.xml with ScrollView root
  - [ ] 2.2: Add vertical LinearLayout with proper padding (16dp)
  - [ ] 2.3: Add Settings title TextView with TalkBack announcement "Settings"
  - [ ] 2.4: Create "Speech Rate" section with label and SeekBar (0.5x - 2.0x range)
  - [ ] 2.5: Set SeekBar contentDescription: "Speech rate, slider, currently %1$s times normal speed"
  - [ ] 2.6: Create "Verbosity Mode" section with RadioGroup
  - [ ] 2.7: Add RadioButton for BRIEF with contentDescription: "Brief mode, radio button"
  - [ ] 2.8: Add RadioButton for STANDARD with contentDescription: "Standard mode, radio button, selected"
  - [ ] 2.9: Add RadioButton for DETAILED with contentDescription: "Detailed mode, radio button"
  - [ ] 2.10: Create "High Contrast Mode" section with MaterialSwitch
  - [ ] 2.11: Set switch contentDescription: "High contrast mode, switch, currently off"
  - [ ] 2.12: Create "Large Text Mode" section with MaterialSwitch
  - [ ] 2.13: Set switch contentDescription: "Large text mode, switch, currently off"
  - [ ] 2.14: Add "Reset to Defaults" button with contentDescription: "Reset all settings to defaults"
  - [ ] 2.15: Ensure all interactive elements minimum 48×48 dp touch targets
  - [ ] 2.16: Use theme attributes for colors (?attr/colorOnSurface, ?attr/colorPrimary)

- [ ] Task 3: Create SettingsFragment with View Binding (AC: 1, 4)
  - [ ] 3.1: Create SettingsFragment.kt extending Fragment
  - [ ] 3.2: Setup View Binding with FragmentSettingsBinding
  - [ ] 3.3: Inject SettingsViewModel using `by viewModels()` delegate
  - [ ] 3.4: Implement onCreateView() inflating binding
  - [ ] 3.5: Implement onViewCreated() calling setupListeners() and observePreferences()
  - [ ] 3.6: Implement onDestroyView() cleaning up _binding reference
  - [ ] 3.7: Add fragment lifecycle logging for debugging

- [ ] Task 4: Implement Settings Listeners and UI Updates (AC: 1, 4)
  - [ ] 4.1: Setup SeekBar onProgressChanged listener calling viewModel.updateSpeechRate()
  - [ ] 4.2: Setup RadioGroup onCheckedChanged listener calling viewModel.updateVerbosityMode()
  - [ ] 4.3: Setup highContrastSwitch onCheckedChanged listener calling viewModel.updateHighContrastMode()
  - [ ] 4.4: Setup largeTextSwitch onCheckedChanged listener calling viewModel.updateLargeTextMode()
  - [ ] 4.5: Setup resetButton onClick listener showing confirmation dialog
  - [ ] 4.6: Add flag to prevent listener loops during UI updates from StateFlow
  - [ ] 4.7: Announce state changes via TalkBack using announceForAccessibility()

- [ ] Task 5: Observe ViewModel StateFlows and Update UI (AC: 2, 4)
  - [ ] 5.1: Collect speechRate StateFlow in viewLifecycleOwner.lifecycleScope
  - [ ] 5.2: Update SeekBar progress when speechRate changes
  - [ ] 5.3: Update speech rate display TextView: "1.5× speed"
  - [ ] 5.4: Collect verbosityMode StateFlow
  - [ ] 5.5: Update RadioGroup checked state based on verbosityMode
  - [ ] 5.6: Collect highContrastMode StateFlow
  - [ ] 5.7: Update highContrastSwitch checked state and contentDescription
  - [ ] 5.8: Collect largeTextMode StateFlow
  - [ ] 5.9: Update largeTextSwitch checked state and contentDescription
  - [ ] 5.10: Use repeatOnLifecycle(Lifecycle.State.STARTED) for lifecycle-aware collection

- [ ] Task 6: Implement Reset to Defaults with Confirmation (AC: 6, 7, 8)
  - [ ] 6.1: Create MaterialAlertDialog when reset button clicked
  - [ ] 6.2: Set dialog title: "Reset all settings to defaults?"
  - [ ] 6.3: Set dialog message: "This will restore all preferences to their default values. This cannot be undone."
  - [ ] 6.4: Add positive button "Reset" calling viewModel.resetToDefaults()
  - [ ] 6.5: Add negative button "Cancel" dismissing dialog
  - [ ] 6.6: Announce confirmation via TalkBack: "Settings reset to defaults"
  - [ ] 6.7: Add proper TalkBack labels for all dialog buttons
  - [ ] 6.8: Test dialog with TalkBack navigation

- [ ] Task 7: Add Settings Menu Item to MainActivity (AC: 4)
  - [ ] 7.1: Create res/menu/main_menu.xml with "Settings" item
  - [ ] 7.2: Set menu item contentDescription: "Settings, opens app settings screen"
  - [ ] 7.3: Override onCreateOptionsMenu() in MainActivity inflating menu
  - [ ] 7.4: Override onOptionsItemSelected() handling Settings menu item click
  - [ ] 7.5: Implement fragment transaction: supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, SettingsFragment()).addToBackStack(null).commit()
  - [ ] 7.6: Test back button navigation from Settings to RecognitionFragment
  - [ ] 7.7: Verify Settings menu accessible via TalkBack "More options" button

- [ ] Task 8: Add String Resources for Settings Screen (AC: 3, 4)
  - [ ] 8.1: Add settings_title: "Settings"
  - [ ] 8.2: Add speech_rate_label: "Speech Rate"
  - [ ] 8.3: Add speech_rate_description: "Adjust text-to-speech speed. Current: %1$s times normal speed."
  - [ ] 8.4: Add speech_rate_sample_announcement: "This is how your speech rate sounds"
  - [ ] 8.5: Add verbosity_mode_label: "Verbosity Mode"
  - [ ] 8.6: Add verbosity_brief: "Brief"
  - [ ] 8.7: Add verbosity_standard: "Standard"
  - [ ] 8.8: Add verbosity_detailed: "Detailed"
  - [ ] 8.9: Add high_contrast_label: "High Contrast Mode"
  - [ ] 8.10: Add large_text_label: "Large Text Mode"
  - [ ] 8.11: Add reset_defaults_button: "Reset to Defaults"
  - [ ] 8.12: Add reset_confirmation_title: "Reset all settings to defaults?"
  - [ ] 8.13: Add reset_confirmation_message: "This will restore all preferences to their default values. This cannot be undone."
  - [ ] 8.14: Add reset_success_announcement: "Settings reset to defaults"
  - [ ] 8.15: Add switch_on: "on"
  - [ ] 8.16: Add switch_off: "off"

- [ ] Task 9: Create Unit Tests for SettingsViewModel (AC: 1, 2)
  - [ ] 9.1: Create SettingsViewModelTest.kt in test/kotlin/com/visionfocus/ui/settings/
  - [ ] 9.2: Setup test with mock SettingsRepository using Mockito
  - [ ] 9.3: Test speechRate StateFlow defaults to 1.0f
  - [ ] 9.4: Test updateSpeechRate() calls repository.setSpeechRate()
  - [ ] 9.5: Test verbosityMode StateFlow defaults to VerbosityMode.STANDARD
  - [ ] 9.6: Test updateVerbosityMode() calls repository.setVerbosity()
  - [ ] 9.7: Test highContrastMode StateFlow defaults to false
  - [ ] 9.8: Test updateHighContrastMode() calls repository.setHighContrastMode()
  - [ ] 9.9: Test largeTextMode StateFlow defaults to false
  - [ ] 9.10: Test updateLargeTextMode() calls repository.setLargeTextMode()
  - [ ] 9.11: Test resetToDefaults() calls all repository setters with default values
  - [ ] 9.12: Use StandardTestDispatcher for coroutine testing
  - [ ] 9.13: Verify all tests pass: ./gradlew test

- [ ] Task 10: Create Integration Tests for Settings UI (AC: 2, 3, 4)
  - [ ] 10.1: Create SettingsScreenIntegrationTest.kt in androidTest
  - [ ] 10.2: Use @HiltAndroidTest annotation
  - [ ] 10.3: Test: Launch MainActivity → navigate to Settings → verify Settings screen displays
  - [ ] 10.4: Test: Change speech rate → verify SeekBar updates
  - [ ] 10.5: Test: Change verbosity mode → verify RadioButton selection
  - [ ] 10.6: Test: Toggle high contrast mode → verify switch state
  - [ ] 10.7: Test: Toggle large text mode → verify switch state
  - [ ] 10.8: Test: Click reset button → verify confirmation dialog appears
  - [ ] 10.9: Test: Confirm reset → verify all settings restored to defaults
  - [ ] 10.10: Test: Settings persist across activity recreation (simulate app restart)
  - [ ] 10.11: Verify all tests pass: ./gradlew connectedAndroidTest

- [ ] Task 11: Create Accessibility Tests for Settings Screen (AC: 4)
  - [ ] 11.1: Create SettingsAccessibilityTest.kt in androidTest/accessibility/
  - [ ] 11.2: Enable AccessibilityChecks.enable() for automated scanning
  - [ ] 11.3: Test: Settings screen passes Accessibility Scanner (zero errors)
  - [ ] 11.4: Test: All switches have proper contentDescription
  - [ ] 11.5: Test: Speech rate SeekBar has proper contentDescription
  - [ ] 11.6: Test: RadioButtons have proper contentDescription
  - [ ] 11.7: Test: Reset button has proper contentDescription
  - [ ] 11.8: Test: All interactive elements meet 48×48 dp touch target minimum
  - [ ] 11.9: Test: Focus order is logical (top to bottom, left to right)
  - [ ] 11.10: Test: TalkBack announces state changes (record announcements)
  - [ ] 11.11: Verify all tests pass with zero Accessibility Scanner violations

- [ ] Task 12: Manual TalkBack Testing (AC: 4, 5)
  - [ ] 12.1: Enable TalkBack on test device
  - [ ] 12.2: Navigate to Settings screen from MainActivity menu
  - [ ] 12.3: Verify Settings title announces correctly
  - [ ] 12.4: Swipe through all settings controls verifying proper announcements
  - [ ] 12.5: Test SeekBar adjustment with TalkBack double-tap and swipe
  - [ ] 12.6: Test RadioButton selection with TalkBack
  - [ ] 12.7: Test MaterialSwitch toggle with TalkBack
  - [ ] 12.8: Test Reset button with confirmation dialog navigation
  - [ ] 12.9: Verify back button returns to RecognitionFragment
  - [ ] 12.10: Document any accessibility issues for refinement

## Dev Notes

### Critical Epic 5 Context and Story Dependencies

**Epic 5 Goal:** Users customize app experience for optimal comfort and usability.

From [epics.md#Epic 5: Personalization & Settings]:

**Story 5.3 (THIS STORY):** Settings Screen with Persistent Preferences - Central settings UI with automatic persistence
- **Purpose:** Create comprehensive settings screen exposing all user preferences (speech rate, verbosity, visual modes, haptic intensity) with immediate DataStore persistence and TalkBack accessibility
- **Deliverable:** SettingsFragment with MaterialSwitch/SeekBar controls, SettingsViewModel orchestrating repository calls, MainActivity menu integration, reset-to-defaults functionality

**Story 5.3 Dependencies on Stories 1.3, 2.5, 4.1, 5.1, 5.2:**

**From Story 1.3 (DataStore Preferences - COMPLETED December 24, 2025):**
- **CRITICAL:** SettingsRepository interface and SettingsRepositoryImpl already implemented ✅
- **CRITICAL:** PreferenceKeys.kt with all preference keys defined ✅
  - SPEECH_RATE: Float (0.5-2.0 range, default 1.0)
  - VERBOSITY_MODE: String enum (BRIEF/STANDARD/DETAILED, default STANDARD)
  - HIGH_CONTRAST_MODE: Boolean (default false)
  - LARGE_TEXT_MODE: Boolean (default false)
- **CRITICAL:** DataStore persistence with thread-safe access validated ✅
- **Available:** Unit tests for all preferences passing ✅
- **Pattern Established:** Flow-based reactive preferences with stateIn() for ViewModel integration

**From Story 2.5 (High-Contrast Mode & Large Text - IN-PROGRESS):**
- **Status:** Story 2.5 marked `in-progress` but SettingsViewModel/SettingsFragment NOT yet created
- **CRITICAL:** Story 5.3 WILL create SettingsViewModel and SettingsFragment (Story 2.5 planned them but hasn't implemented)
- **Coordination:** Story 2.5 will extend SettingsFragment with theme switching logic when completed
- **Theme Management:** ThemeManager singleton will be called from Settings screen for high-contrast/large-text toggle

**From Story 4.1 (Verbosity Mode Selection - BACKLOG):**
- **Status:** Story 4.1 not started, but verbosity preference infrastructure exists (Story 1.3)
- **CRITICAL:** VerbosityMode enum already defined: BRIEF, STANDARD (default), DETAILED ✅
- **Integration:** Story 5.3 Settings screen will expose verbosity mode selection via RadioGroup
- **Implementation:** Story 4.1 will consume verbosity preference from SettingsRepository when implementing recognition announcement logic

**From Story 5.1 (TTS Speech Rate Adjustment - BACKLOG):**
- **Status:** Story 5.1 not started, but speech rate preference infrastructure exists (Story 1.3)
- **CRITICAL:** Speech rate preference (0.5-2.0 range) already validated in SettingsRepositoryImpl ✅
- **Integration:** Story 5.3 Settings screen will expose speech rate adjustment via SeekBar
- **Implementation:** Story 5.1 will consume speech rate preference from SettingsRepository when integrating with TTSManager

**From Story 5.2 (TTS Voice Selection - BACKLOG):**
- **Status:** Story 5.2 not started, voice selection preference NOT in SettingsRepository
- **CRITICAL:** Story 5.3 should NOT implement voice selection UI (out of scope)
- **Future:** Story 5.2 will add voice selection preference and extend Settings screen

**Story 5.3 Infrastructure Status:**
- ✅ SettingsRepository: Fully implemented (Story 1.3)
- ✅ PreferenceKeys: All 4 preferences defined (Story 1.3)
- ✅ DataStore persistence: Validated and working (Story 1.3)
- ❌ SettingsViewModel: Does NOT exist (Story 5.3 will create)
- ❌ SettingsFragment: Does NOT exist (Story 5.3 will create)
- ❌ fragment_settings.xml: Does NOT exist (Story 5.3 will create)
- ❌ MainActivity menu: Does NOT exist (Story 5.3 will add)

### Technical Requirements from Architecture Document

From [architecture.md#Clean Architecture Layers]:

**Layer Separation for Story 5.3:**
- **Data Layer:** SettingsRepository (already exists in Story 1.3) ✅
- **Presentation Layer:** SettingsViewModel orchestrating repository calls (NEW)
- **UI Layer:** SettingsFragment with View Binding observing ViewModel StateFlows (NEW)

**SettingsViewModel Pattern:**
```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    // Expose repository Flows as StateFlows for UI observation
    val speechRate: StateFlow<Float> = settingsRepository
        .getSpeechRate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,  // Eagerly to show current value immediately
            initialValue = 1.0f
        )
    
    val verbosityMode: StateFlow<VerbosityMode> = settingsRepository
        .getVerbosity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = VerbosityMode.STANDARD
        )
    
    val highContrastMode: StateFlow<Boolean> = settingsRepository
        .getHighContrastMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )
    
    val largeTextMode: StateFlow<Boolean> = settingsRepository
        .getLargeTextMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )
    
    // Update functions launched in viewModelScope
    fun updateSpeechRate(rate: Float) {
        viewModelScope.launch {
            settingsRepository.setSpeechRate(rate)
        }
    }
    
    fun updateVerbosityMode(mode: VerbosityMode) {
        viewModelScope.launch {
            settingsRepository.setVerbosity(mode)
        }
    }
    
    fun updateHighContrastMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHighContrastMode(enabled)
        }
    }
    
    fun updateLargeTextMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLargeTextMode(enabled)
        }
    }
    
    // Reset all preferences to defaults
    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.setSpeechRate(1.0f)                      // Default: 1.0× speed
            settingsRepository.setVerbosity(VerbosityMode.STANDARD)     // Default: STANDARD
            settingsRepository.setHighContrastMode(false)                // Default: off
            settingsRepository.setLargeTextMode(false)                   // Default: off
        }
    }
}
```

From [architecture.md#Decision 2: State Management Pattern]:

**SettingsFragment StateFlow Observation:**
```kotlin
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    private var isUpdatingUI = false  // Flag to prevent listener loops
    
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
        
        setupListeners()
        observePreferences()
    }
    
    private fun setupListeners() {
        // Speech rate SeekBar listener
        binding.speechRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isUpdatingUI) {
                    val rate = 0.5f + (progress / 100f) * 1.5f  // Map 0-100 to 0.5-2.0
                    viewModel.updateSpeechRate(rate)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Verbosity RadioGroup listener
        binding.verbosityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isUpdatingUI) {
                val mode = when (checkedId) {
                    R.id.verbosityBriefRadioButton -> VerbosityMode.BRIEF
                    R.id.verbosityDetailedRadioButton -> VerbosityMode.DETAILED
                    else -> VerbosityMode.STANDARD
                }
                viewModel.updateVerbosityMode(mode)
            }
        }
        
        // High contrast switch listener
        binding.highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) {
                viewModel.updateHighContrastMode(isChecked)
                
                // Announce state change
                val announcement = if (isChecked) "High contrast mode enabled" else "High contrast mode disabled"
                binding.root.announceForAccessibility(announcement)
            }
        }
        
        // Large text switch listener
        binding.largeTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) {
                viewModel.updateLargeTextMode(isChecked)
                
                val announcement = if (isChecked) "Large text mode enabled" else "Large text mode disabled"
                binding.root.announceForAccessibility(announcement)
            }
        }
        
        // Reset button listener
        binding.resetDefaultsButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun observePreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Launch all StateFlow collectors in parallel
                launch {
                    viewModel.speechRate.collect { rate ->
                        updateSpeechRateUI(rate)
                    }
                }
                
                launch {
                    viewModel.verbosityMode.collect { mode ->
                        updateVerbosityModeUI(mode)
                    }
                }
                
                launch {
                    viewModel.highContrastMode.collect { enabled ->
                        updateHighContrastUI(enabled)
                    }
                }
                
                launch {
                    viewModel.largeTextMode.collect { enabled ->
                        updateLargeTextUI(enabled)
                    }
                }
            }
        }
    }
    
    private fun updateSpeechRateUI(rate: Float) {
        isUpdatingUI = true
        
        // Update SeekBar progress
        val progress = ((rate - 0.5f) / 1.5f * 100f).toInt()
        binding.speechRateSeekBar.progress = progress
        
        // Update display text
        binding.speechRateValueText.text = getString(R.string.speech_rate_value, rate)
        
        // Update contentDescription for TalkBack
        binding.speechRateSeekBar.contentDescription = 
            "Speech rate, slider, currently $rate times normal speed"
        
        isUpdatingUI = false
    }
    
    private fun updateVerbosityModeUI(mode: VerbosityMode) {
        isUpdatingUI = true
        
        val checkedId = when (mode) {
            VerbosityMode.BRIEF -> R.id.verbosityBriefRadioButton
            VerbosityMode.DETAILED -> R.id.verbosityDetailedRadioButton
            VerbosityMode.STANDARD -> R.id.verbosityStandardRadioButton
        }
        
        binding.verbosityRadioGroup.check(checkedId)
        
        isUpdatingUI = false
    }
    
    private fun updateHighContrastUI(enabled: Boolean) {
        isUpdatingUI = true
        
        binding.highContrastSwitch.isChecked = enabled
        binding.highContrastSwitch.contentDescription = 
            "High contrast mode, switch, currently ${if (enabled) "on" else "off"}"
        
        isUpdatingUI = false
    }
    
    private fun updateLargeTextUI(enabled: Boolean) {
        isUpdatingUI = true
        
        binding.largeTextSwitch.isChecked = enabled
        binding.largeTextSwitch.contentDescription = 
            "Large text mode, switch, currently ${if (enabled) "on" else "off"}"
        
        isUpdatingUI = false
    }
    
    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_confirmation_title)
            .setMessage(R.string.reset_confirmation_message)
            .setPositiveButton(R.string.reset) { _, _ ->
                viewModel.resetToDefaults()
                
                // Announce reset completion
                binding.root.announceForAccessibility(
                    getString(R.string.reset_success_announcement)
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### Architecture Compliance Requirements

**Hilt Dependency Injection:**
- SettingsRepository already provided in RepositoryModule (Story 1.3) ✅
- SettingsViewModel uses @HiltViewModel annotation for automatic injection
- SettingsFragment uses `by viewModels()` delegate for ViewModel retrieval
- No additional Hilt modules needed

**Testing Strategy:**
- **Unit Tests:** SettingsViewModelTest.kt mocking SettingsRepository with Mockito
- **Integration Tests:** SettingsScreenIntegrationTest.kt with @HiltAndroidTest validating full UI flow
- **Accessibility Tests:** SettingsAccessibilityTest.kt with Accessibility Scanner integration

**View Binding Pattern (Story 1.1):**
- ViewBinding enabled in build.gradle.kts ✅
- FragmentSettingsBinding generated from fragment_settings.xml
- Standard lifecycle pattern: inflate in onCreateView(), clean up in onDestroyView()

### Library & Framework Requirements

**Material Design 3 Components:**
- **Version:** com.google.android.material:material:1.11.0 (already configured)
- **MaterialSwitch:** For high-contrast and large-text toggles (com.google.android.material.switchmaterial.SwitchMaterial)
- **RadioGroup/RadioButton:** For verbosity mode selection
- **SeekBar:** For speech rate adjustment (0.5x - 2.0x range)
- **MaterialAlertDialog:** For reset confirmation dialog

**AndroidX Lifecycle:**
- **Version:** androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0 (already configured)
- **StateFlow:** Convert repository Flows to StateFlows using stateIn()
- **repeatOnLifecycle:** Lifecycle-aware StateFlow collection in Fragment
- **viewModelScope:** Coroutine scope for repository update calls

**DataStore Preferences (Story 1.3):**
- **Version:** androidx.datastore:datastore-preferences:1.0.0 (already configured)
- **No new dependencies needed** - all infrastructure exists

### File Structure Requirements

**New Files to Create:**
```
app/src/main/
├── java/com/visionfocus/
│   └── ui/
│       └── settings/
│           ├── SettingsViewModel.kt         # NEW: ViewModel with preference StateFlows
│           └── SettingsFragment.kt          # NEW: Settings UI with Material switches
│
├── res/
│   ├── layout/
│   │   └── fragment_settings.xml            # NEW: Settings screen layout
│   └── menu/
│       └── main_menu.xml                    # NEW: Overflow menu with Settings item
│
└── test/java/com/visionfocus/
    └── ui/settings/
        └── SettingsViewModelTest.kt         # NEW: Unit tests for ViewModel
```

**Files to Modify:**
```
app/src/main/java/com/visionfocus/
└── MainActivity.kt                          # MODIFY: Add menu inflation and Settings navigation

app/src/main/res/values/
└── strings.xml                              # MODIFY: Add Settings screen string resources

app/src/androidTest/java/com/visionfocus/
├── ui/settings/
│   └── SettingsScreenIntegrationTest.kt    # NEW: Integration tests
└── accessibility/
    └── SettingsAccessibilityTest.kt         # NEW: Accessibility validation
```

### Testing Requirements

From [architecture.md#Testing Strategy]:

**Unit Testing (SettingsViewModelTest.kt):**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    private lateinit var mockRepository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        mockRepository = mock()
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock behaviors
        whenever(mockRepository.getSpeechRate()).thenReturn(flowOf(1.0f))
        whenever(mockRepository.getVerbosity()).thenReturn(flowOf(VerbosityMode.STANDARD))
        whenever(mockRepository.getHighContrastMode()).thenReturn(flowOf(false))
        whenever(mockRepository.getLargeTextMode()).thenReturn(flowOf(false))
        
        viewModel = SettingsViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `speechRate defaults to 1_0f`() = runTest {
        assertEquals(1.0f, viewModel.speechRate.value)
    }
    
    @Test
    fun `updateSpeechRate calls repository setSpeechRate`() = runTest {
        viewModel.updateSpeechRate(1.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockRepository).setSpeechRate(1.5f)
    }
    
    @Test
    fun `verbosityMode defaults to STANDARD`() = runTest {
        assertEquals(VerbosityMode.STANDARD, viewModel.verbosityMode.value)
    }
    
    @Test
    fun `updateVerbosityMode calls repository setVerbosity`() = runTest {
        viewModel.updateVerbosityMode(VerbosityMode.DETAILED)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockRepository).setVerbosity(VerbosityMode.DETAILED)
    }
    
    @Test
    fun `resetToDefaults calls all repository setters with default values`() = runTest {
        viewModel.resetToDefaults()
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockRepository).setSpeechRate(1.0f)
        verify(mockRepository).setVerbosity(VerbosityMode.STANDARD)
        verify(mockRepository).setHighContrastMode(false)
        verify(mockRepository).setLargeTextMode(false)
    }
}
```

**Integration Testing (SettingsScreenIntegrationTest.kt):**
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenIntegrationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun settingsScreenDisplaysAllPreferences() {
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify all settings visible
        onView(withId(R.id.speechRateSeekBar)).check(matches(isDisplayed()))
        onView(withId(R.id.verbosityRadioGroup)).check(matches(isDisplayed()))
        onView(withId(R.id.highContrastSwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.largeTextSwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.resetDefaultsButton)).check(matches(isDisplayed()))
    }
    
    @Test
    fun toggleHighContrastMode_persistsAcrossActivityRecreation() {
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Enable high contrast mode
        onView(withId(R.id.highContrastSwitch)).perform(click())
        
        // Verify switch checked
        onView(withId(R.id.highContrastSwitch)).check(matches(isChecked()))
        
        // Recreate activity (simulates app restart)
        activityRule.scenario.recreate()
        
        // Navigate to Settings again
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify switch still checked
        onView(withId(R.id.highContrastSwitch)).check(matches(isChecked()))
    }
    
    @Test
    fun resetToDefaults_restoresAllSettings() {
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Change multiple settings
        onView(withId(R.id.highContrastSwitch)).perform(click())
        onView(withId(R.id.verbosityDetailedRadioButton)).perform(click())
        
        // Click reset button
        onView(withId(R.id.resetDefaultsButton)).perform(click())
        
        // Confirm reset dialog
        onView(withText(R.string.reset)).perform(click())
        
        // Verify settings restored to defaults
        onView(withId(R.id.highContrastSwitch)).check(matches(isNotChecked()))
        onView(withId(R.id.verbosityStandardRadioButton)).check(matches(isChecked()))
    }
}
```

**Accessibility Testing (SettingsAccessibilityTest.kt):**
```kotlin
@RunWith(AndroidJUnit4::class)
class SettingsAccessibilityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        AccessibilityChecks.enable()
            .setRunChecksFromRootView(true)
    }
    
    @Test
    fun settingsScreen_passesAccessibilityScanner() {
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Accessibility Scanner runs automatically
        // Test fails if any violations found
    }
    
    @Test
    fun allSwitches_havePrContent Descriptions() {
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify high contrast switch content description
        onView(withId(R.id.highContrastSwitch)).check { view, _ ->
            val switch = view as MaterialSwitch
            assertNotNull("High contrast switch missing contentDescription", switch.contentDescription)
            assertTrue(
                "High contrast switch contentDescription should mention current state",
                switch.contentDescription.toString().contains("currently")
            )
        }
        
        // Similar checks for large text switch
    }
    
    @Test
    fun allInteractiveElements_meetMinimumTouchTarget() {
        onView(withId(R.id.action_settings)).perform(click())
        
        val minSize = 48.dpToPx()
        
        // Verify switches meet minimum size
        onView(withId(R.id.highContrastSwitch)).check { view, _ ->
            assertTrue("Switch height < 48dp", view.height >= minSize)
            assertTrue("Switch width < 48dp", view.width >= minSize)
        }
        
        // Similar checks for all interactive elements
    }
    
    private fun Int.dpToPx(): Int {
        val displayMetrics = InstrumentationRegistry.getInstrumentation()
            .targetContext.resources.displayMetrics
        return (this * displayMetrics.density).toInt()
    }
}
```

### Previous Story Intelligence

**From Story 2.7 (TalkBack Navigation - COMPLETED December 30, 2025):**

**Learnings Applied:**
- **Comprehensive Testing:** Story 2.7 created 162 lines of accessibility tests (RecognitionAccessibilityTest.kt, FocusOrderTest.kt) - Story 5.3 follows same rigor
- **TalkBack Announcements:** `announceForAccessibility()` pattern for state changes validated - Story 5.3 uses for switch toggles
- **Content Descriptions:** Dynamic content descriptions with current state ("currently on/off") - Story 5.3 applies to all switches
- **Accessibility Scanner Integration:** Zero errors enforcement pattern - Story 5.3 uses AccessibilityChecks.enable()
- **Focus Management:** Focus order validation tests - Story 5.3 validates Settings screen focus order

**Implementation Patterns to Reuse:**
- **StateFlow Observation:** `viewLifecycleOwner.repeatOnLifecycle(STARTED)` pattern from RecognitionFragment
- **TalkBack Announcements:** `binding.root.announceForAccessibility()` for state changes
- **Accessibility Testing:** AccessibilityChecks.enable() with zero-errors enforcement
- **Content Descriptions:** Dynamic descriptions reflecting current state

**From Story 2.5 (High-Contrast Mode - IN-PROGRESS):**

**Status Check:**
- **Story 2.5 marked `in-progress` in sprint-status.yaml**
- **CRITICAL FINDING:** SettingsViewModel and SettingsFragment do NOT exist yet (verified via codebase research)
- **Resolution:** Story 5.3 will create SettingsViewModel and SettingsFragment, Story 2.5 will extend them for theme switching when completed
- **Coordination:** No conflict - Settings infrastructure (Story 5.3) is foundational, theme logic (Story 2.5) extends it

**From Story 1.3 (DataStore Preferences - COMPLETED December 24, 2025):**

**Infrastructure Validated:**
- ✅ SettingsRepository with getSpeechRate(), getVerbosity(), getHighContrastMode(), getLargeTextMode()
- ✅ SettingsRepositoryImpl with DataStore persistence and error handling
- ✅ PreferenceKeys.kt with all 4 preference keys defined
- ✅ Unit tests validating preference persistence (11 total tests)
- ✅ Thread-safe DataStore access (50 concurrent write test passing)

**Testing Patterns from Story 1.3:**
- DataStore instrumentation tests in androidTest/ directory
- Mock SettingsRepository using Mockito for ViewModel tests
- Preference persistence validation across repository recreations

### Git Intelligence Summary

**Recent Commit Analysis (Last 5 Commits from Git Log):**

**Commit Pattern: Comprehensive Commit Messages**
- Story 2.7 commit: 100+ line message documenting AC status, files modified, testing status, known issues
- **Story 5.3 Application:** Use comprehensive commit template with:
  - All 9 AC statuses
  - Files created/modified lists
  - Testing status (unit, integration, accessibility)
  - Next steps for reviewer

**Commit Pattern: Status Transitions**
- Recent commits document sprint-status.yaml updates: `backlog → ready-for-dev → in-progress → review → done`
- **Story 5.3 Application:** Commit message must document: `sprint-status.yaml (backlog → ready-for-dev)` when story file created

**Commit Pattern: Test Coverage Documentation**
- Story 2.7 documented: "162 lines of accessibility tests created"
- Story 2.5 documented: "11 tests (3 unit + 8 integration)"
- **Story 5.3 Application:** Document test counts in commit message

### Latest Technical Information

**Material Design 3 Components (2025 Update):**

**MaterialSwitch Best Practices:**
- Use `com.google.android.material.switchmaterial.SwitchMaterial` (NOT deprecated android.widget.Switch)
- Set `android:minWidth="48dp"` and `android:minHeight="48dp"` for touch targets
- Dynamic contentDescription reflecting current state: "Switch name, switch, currently on/off"
- Use `setOnCheckedChangeListener` with fromUser parameter check to prevent loops

**SeekBar Accessibility (2025):**
- Set `android:contentDescription` with current value announcement
- Use `setOnSeekBarChangeListener` with fromUser parameter to distinguish user vs. programmatic changes
- Announce value changes via TalkBack using `announceForAccessibility()`
- Consider Material Slider component (MaterialSlider) for better accessibility (optional enhancement)

**RadioGroup Best Practices:**
- Each RadioButton must have individual contentDescription
- Group must have `android:checkedButton` attribute for default selection
- Use `setOnCheckedChangeListener` to detect user selection
- Announce selection changes via TalkBack

**Fragment Navigation (2025):**

From Android Developer documentation:
- Manual fragment transactions remain supported and valid (NO requirement to use NavComponent)
- Use `supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()`
- Back button handled automatically when using addToBackStack()
- NavComponent is optional for simple apps like VisionFocus

### Project Context Reference

From [architecture.md#Project Context Analysis]:

**VisionFocus Mission:** Assist blind and low vision users in object identification and GPS navigation using TalkBack-first accessibility design with on-device AI inference preserving privacy.

**Story 5.3 User Value:**
Visually impaired users can customize VisionFocus to match their preferences without manual reconfiguration on each app launch. Settings persist automatically using DataStore, ensuring speech rate, verbosity mode, high-contrast mode, and large text mode remain configured across app restarts. This reduces friction and enables users to focus on navigation tasks rather than app configuration.

**Research Validation:**
From Chapter 8: Testing & Evaluation:
- **Usability Metric:** SUS score target ≥75 (validated 78.5) - Settings screen must maintain this with clear organization
- **Task Success Rate:** ≥85% target (validated 91.3%) - Settings must be discoverable and intuitive
- **Accessibility Compliance:** Zero Accessibility Scanner errors enforced - Settings screen must pass scanner

### Story Completion Status

**Status:** ready-for-dev

**Ultimate Context Engine Analysis Completed:**

✅ **Context Gathered:**
- Epic 5 objectives and Story 5.3 requirements extracted from epics.md
- Story 5.3 dependencies analyzed: 70% infrastructure complete from Story 1.3
- SettingsRepository, PreferenceKeys, DataStore persistence validated and working
- SettingsViewModel/Fragment do NOT exist yet (Story 5.3 will create)
- RecognitionFragment patterns analyzed for StateFlow observation reuse
- Git commit intelligence: comprehensive message patterns, test coverage documentation

✅ **Developer Guardrails Established:**
- SettingsViewModel pattern specified with StateFlow conversion using stateIn()
- SettingsFragment pattern specified with lifecycle-aware StateFlow collection
- MaterialSwitch, RadioGroup, SeekBar accessibility patterns documented
- Reset-to-defaults confirmation dialog pattern specified
- 12 tasks with 90+ subtasks providing step-by-step implementation guide
- Testing requirements: unit tests (SettingsViewModel), integration tests (full UI flow), accessibility tests (zero Scanner errors)

✅ **Risk Mitigation:**
- Story 2.5 coordination clarified (no conflict, Settings infrastructure is foundational)
- SettingsRepository already validated in Story 1.3 (low risk)
- RecognitionFragment patterns reusable for SettingsFragment (proven approach)
- isUpdatingUI flag documented to prevent listener loops
- Manual fragment navigation matches existing patterns (no NavComponent needed)

✅ **Clear Success Criteria:**
- All 9 acceptance criteria validated in comprehensive testing
- Settings persist across app restarts (integration test validation)
- Zero Accessibility Scanner errors enforced
- All switches with proper TalkBack labels
- Reset-to-defaults with confirmation dialog

**Ready for Dev Agent Implementation:** Story 5.3 provides comprehensive context preventing common LLM developer mistakes (reinventing SettingsRepository, wrong navigation patterns, missing accessibility labels, listener loops). Developer has everything needed for flawless implementation.

## Dev Agent Record

### Agent Model Used

(To be filled during implementation)

### Debug Log References

(To be filled during implementation)

### Completion Notes List

(To be filled during implementation)

### File List

**Files to be Created:**
1. `app/src/main/java/com/visionfocus/ui/settings/SettingsViewModel.kt` - ViewModel with preference StateFlows
2. `app/src/main/java/com/visionfocus/ui/settings/SettingsFragment.kt` - Settings UI with Material switches
3. `app/src/main/res/layout/fragment_settings.xml` - Settings screen layout
4. `app/src/main/res/menu/main_menu.xml` - Overflow menu with Settings item
5. `app/src/test/java/com/visionfocus/ui/settings/SettingsViewModelTest.kt` - Unit tests for ViewModel
6. `app/src/androidTest/java/com/visionfocus/ui/settings/SettingsScreenIntegrationTest.kt` - Integration tests
7. `app/src/androidTest/java/com/visionfocus/accessibility/SettingsAccessibilityTest.kt` - Accessibility validation

**Files to be Modified:**
1. `app/src/main/java/com/visionfocus/MainActivity.kt` - Add menu inflation and Settings navigation
2. `app/src/main/res/values/strings.xml` - Add Settings screen string resources
3. `_bmad-output/implementation-artifacts/sprint-status.yaml` - Update Story 5.3 status from backlog to ready-for-dev

**Existing Files Referenced (No Changes Needed):**
- `app/src/main/java/com/visionfocus/data/repository/SettingsRepository.kt` - Already has all needed methods
- `app/src/main/java/com/visionfocus/data/repository/SettingsRepositoryImpl.kt` - DataStore persistence already working
- `app/src/main/java/com/visionfocus/data/preferences/PreferenceKeys.kt` - All 4 preference keys defined
- `app/src/main/java/com/visionfocus/data/model/VerbosityMode.kt` - Enum already defined
