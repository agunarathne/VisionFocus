# Story 5.1: TTS Speech Rate Adjustment

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a visually impaired user,
I want to adjust how fast the app speaks,
So that I can match the speed to my comprehension preference.

## Acceptance Criteria

**Given** Settings screen is accessible
**When** I adjust speech rate setting
**Then** speech rate slider ranges from 0.5× (half speed) to 2.0× (double speed) with 0.1 increments
**And** default speech rate is 1.0× (normal)
**And** slider has TalkBack labels: "Speech rate, slider, currently 1.0 times normal speed"
**And** voice commands "Increase speed" increments by 0.25×, "Decrease speed" decrements by 0.25×
**And** speech rate change applies immediately to next TTS announcement
**And** sample announcement plays when slider changes: "This is how your speech rate sounds" (at selected rate)
**And** speech rate preference persists in DataStore across app restarts
**And** rate limits: attempting to go below 0.5× or above 2.0× announces: "Speech rate at minimum" or "Speech rate at maximum"
**And** Android TTS setSpeechRate() is called with selected multiplier

## Tasks / Subtasks

- [ ] Task 1: Create Settings screen UI with speech rate slider (AC: Slider, TalkBack labels, Sample announcement)
  - [ ] 1.1: Create fragment_settings.xml with ConstraintLayout
  - [ ] 1.2: Add speech rate SeekBar (range 0.5-2.0, step 0.1, default 1.0)
  - [ ] 1.3: Add current rate TextView (displays "1.0×")
  - [ ] 1.4: Add sample announcement Button ("Test speed")
  - [ ] 1.5: Ensure all touch targets meet 48×48 dp minimum (Story 2.7 pattern)
  - [ ] 1.6: Add TalkBack contentDescription for SeekBar: "Speech rate, slider, currently X times normal speed"
  - [ ] 1.7: Validate XML layout passes Accessibility Scanner (zero errors)

- [ ] Task 2: Implement SettingsFragment with speech rate control (AC: Slider interaction, Sample announcement)
  - [ ] 2.1: Create SettingsFragment.kt extending Fragment with View Binding
  - [ ] 2.2: Inject SettingsViewModel via Hilt (@AndroidEntryPoint)
  - [ ] 2.3: Observe speechRate Flow from ViewModel (update UI when rate changes)
  - [ ] 2.4: Implement SeekBar.OnSeekBarChangeListener (convert progress to 0.5-2.0 range)
  - [ ] 2.5: Update current rate TextView on slider change
  - [ ] 2.6: Call viewModel.setSpeechRate() on onStopTrackingTouch (not every onProgressChanged)
  - [ ] 2.7: Implement "Test speed" button: Play sample announcement at current rate
  - [ ] 2.8: Update SeekBar contentDescription on rate change: "Speech rate, slider, currently X.X times normal speed"

- [ ] Task 3: Create SettingsViewModel for speech rate management (AC: Persistence, Rate limits)
  - [ ] 3.1: Create SettingsViewModel.kt with @HiltViewModel annotation
  - [ ] 3.2: Inject SettingsRepository and TTSManager
  - [ ] 3.3: Expose speechRate Flow from SettingsRepository.getSpeechRate()
  - [ ] 3.4: Implement setSpeechRate(rate: Float) with validation (0.5-2.0 range)
  - [ ] 3.5: Announce rate limits via TTS: "Speech rate at minimum" or "Speech rate at maximum"
  - [ ] 3.6: Implement playSampleAnnouncement(rate: Float) for "Test speed" button
  - [ ] 3.7: Call TTSManager.setSpeechRate(rate) on rate change (applies to next announcement)

- [ ] Task 4: Extend TTSManager to support speech rate adjustment (AC: setSpeechRate(), Immediate application)
  - [ ] 4.1: Add private var currentSpeechRate: Float = 1.0f to TTSManager
  - [ ] 4.2: Implement setSpeechRate(rate: Float) method: Clamp rate (0.5-2.0), update currentSpeechRate
  - [ ] 4.3: Call tts.setSpeechRate(currentSpeechRate) before every announce() call
  - [ ] 4.4: Initialize currentSpeechRate from SettingsRepository on TTSManager init
  - [ ] 4.5: Add observeSpeechRateChanges() coroutine in init block (updates rate when preference changes)
  - [ ] 4.6: Test: Rate change applies immediately to next recognition announcement

- [ ] Task 5: Implement voice command support for speech rate adjustment (AC: "Increase/Decrease speed" commands)
  - [ ] 5.1: Add voice commands to VoiceCommandProcessor (Epic 3 integration point)
  - [ ] 5.2: "Increase speed" command: Increment rate by 0.25× (call viewModel.incrementSpeechRate())
  - [ ] 5.3: "Decrease speed" command: Decrement rate by 0.25× (call viewModel.decrementSpeechRate())
  - [ ] 5.4: Announce new rate after change: "Speech rate increased to 1.25 times"
  - [ ] 5.5: Handle edge cases: Max/min rate announcements
  - [ ] 5.6: Note: Epic 3 (Voice Commands) not yet implemented - mark as TODO/future integration

- [ ] Task 6: Add Settings navigation from RecognitionFragment (AC: Settings screen accessible)
  - [ ] 6.1: Add settings icon button to fragment_recognition.xml (top-right corner, 48×48 dp)
  - [ ] 6.2: Settings button contentDescription: "Settings, button. Open app settings."
  - [ ] 6.3: Navigate to SettingsFragment on button tap (use Navigation component if configured)
  - [ ] 6.4: If no Navigation component, use FragmentTransaction to replace fragment
  - [ ] 6.5: Verify TalkBack focus lands on first focusable element (SeekBar) in SettingsFragment

- [ ] Task 7: Test speech rate persistence across app restarts (AC: Preference persists)
  - [ ] 7.1: Launch app, navigate to Settings, change rate to 1.5×
  - [ ] 7.2: Close app (swipe from recent apps)
  - [ ] 7.3: Relaunch app, verify rate is still 1.5× (check SettingsFragment slider)
  - [ ] 7.4: Trigger recognition, verify TTS uses 1.5× rate
  - [ ] 7.5: Unit test: SettingsRepositoryTest.testSpeechRatePersistence()

- [ ] Task 8: Implement unit tests for speech rate logic (AC: All)
  - [ ] 8.1: Create SettingsViewModelTest.kt with JUnit 4 + Mockito
  - [ ] 8.2: Test: setSpeechRate() clamps values outside 0.5-2.0 range
  - [ ] 8.3: Test: setSpeechRate() persists to SettingsRepository
  - [ ] 8.4: Test: incrementSpeechRate() adds 0.25× and announces new rate
  - [ ] 8.5: Test: decrementSpeechRate() subtracts 0.25× and announces new rate
  - [ ] 8.6: Test: Rate at 2.0×, increment triggers "Speech rate at maximum"
  - [ ] 8.7: Test: Rate at 0.5×, decrement triggers "Speech rate at minimum"

- [ ] Task 9: Implement integration tests for Settings UI (AC: Slider, Sample announcement, TalkBack)
  - [ ] 9.1: Create SettingsFragmentTest.kt with AndroidX Test + Espresso
  - [ ] 9.2: Test: Launch SettingsFragment, verify SeekBar displays default 1.0×
  - [ ] 9.3: Test: Move slider, verify current rate TextView updates
  - [ ] 9.4: Test: Tap "Test speed" button, verify sample announcement plays
  - [ ] 9.5: Test: SeekBar has proper contentDescription for TalkBack
  - [ ] 9.6: Test: Settings screen passes Accessibility Scanner (zero errors)

- [ ] Task 10: Document Settings accessibility patterns for future Epic 5 stories (AC: All)
  - [ ] 10.1: Update docs/AccessibilityGuidelines.md with Settings screen patterns
  - [ ] 10.2: Document SeekBar accessibility: contentDescription, min/max announcements, TalkBack gestures
  - [ ] 10.3: Document settings navigation pattern (settings icon from any screen)
  - [ ] 10.4: Document sample announcement pattern (instant feedback for preference changes)
  - [ ] 10.5: Create checklist for future settings-related stories (5.2-5.5)

## Dev Notes

### Critical Story 5.1 Context and Dependencies

**Epic 5 Goal:** Users customize app experience for optimal comfort and usability across different contexts (home, work, transit).

From [epics.md#Epic 5: Personalization & Settings]:

**Story 5.1 (THIS STORY):** TTS Speech Rate Adjustment - First personalization feature
- **Purpose:** Enable users to adjust TTS speaking speed to match comprehension preference
- **Deliverable:** Settings screen with speech rate slider (0.5×-2.0×), sample announcement button, persistence via DataStore, immediate application to TTS

**Story 5.1 Dependencies on Epic 1 (Infrastructure):**

**From Story 1.3 (DataStore Preferences Infrastructure):**
- **CRITICAL:** DataStore setup with SettingsRepository interface ✅ ALREADY IMPLEMENTED
- **Available:** SettingsRepository.getSpeechRate() / setSpeechRate(rate) ✅
- **Available:** PreferenceKeys.SPEECH_RATE with float type ✅
- **Available:** Default value: 1.0f (normal speed) ✅
- **Available:** Speech rate clamping: MIN_SPEECH_RATE = 0.5f, MAX_SPEECH_RATE = 2.0f ✅
- **Available:** Thread-safe DataStore operations via Kotlin Flow ✅
- **Known:** Verbosity mode and high-contrast mode already in SettingsRepository (not yet used in UI)

**Story 5.1 Dependencies on Epic 2 (Recognition + TTS):**

**From Story 2.2 (TTS Announcement):**
- **CRITICAL:** TTSManager service for text-to-speech announcements ✅ IMPLEMENTED
- **Available:** announce() method for TTS playback
- **Limitation:** TTSManager does NOT yet support setSpeechRate() - STORY 5.1 ADDS THIS
- **Integration Point:** Story 5.1 extends TTSManager with speech rate control

**From Story 2.7 (TalkBack Navigation):**
- **CRITICAL:** Accessibility compliance baseline established ✅
- **Available:** Accessibility Scanner integration (zero-error requirement)
- **Available:** AccessibilityGuidelines.md with patterns for all UI elements
- **Requirement:** Story 5.1 Settings UI must maintain zero accessibility errors

**Story 5.1 Deliverables for Future Stories:**

**Story 5.2 (TTS Voice Selection):**
- Settings screen foundation (navigation pattern, layout structure)
- SettingsViewModel pattern (Flow observation, preference persistence)
- Sample announcement pattern (instant feedback for preference changes)

**Story 5.3 (Settings Screen with Persistent Preferences):**
- Settings screen already created in Story 5.1 ✅
- Story 5.3 adds additional preferences: Verbosity mode selector, High-contrast toggle, Haptic intensity
- Navigation pattern established (settings icon from any screen)

**Story 5.4 (Haptic Feedback Intensity):**
- Settings UI patterns (sliders, toggle switches, sample feedback)
- Accessibility patterns for settings controls

**Story 5.5 (Quick Settings Toggle via Voice Commands):**
- Voice command integration point (marked as TODO in Story 5.1)
- Rate adjustment announcements ("Speech rate increased to 1.25 times")

**Critical Design Principle:**
> Story 5.1 establishes the Settings screen foundation for Epic 5. This is the first user-configurable preference with immediate visual+audio feedback. All future settings stories build on this pattern.

### Technical Requirements from Architecture Document

From [architecture.md#Decision 1: Data Architecture - DataStore]:

**DataStore Configuration for Story 5.1:**

**SettingsRepository Already Implemented (Story 1.3):**
```kotlin
// SettingsRepository.kt (EXISTING)
interface SettingsRepository {
    fun getSpeechRate(): Flow<Float>  // ✅ Available
    suspend fun setSpeechRate(rate: Float)  // ✅ Available
    // Other methods: getVerbosity(), getHighContrastMode() (not used in Story 5.1)
}

// SettingsRepositoryImpl.kt (EXISTING)
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    companion object {
        private const val DEFAULT_SPEECH_RATE = 1.0f  // ✅ Matches AC requirement
        private const val MIN_SPEECH_RATE = 0.5f  // ✅ Matches AC requirement
        private const val MAX_SPEECH_RATE = 2.0f  // ✅ Matches AC requirement
    }
    
    override fun getSpeechRate(): Flow<Float> {
        return dataStore.data
            .catch { exception -> /* Handle IOException */ }
            .map { preferences ->
                preferences[PreferenceKeys.SPEECH_RATE] ?: DEFAULT_SPEECH_RATE
            }
    }
    
    override suspend fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)  // ✅ Clamping built-in
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SPEECH_RATE] = clampedRate
        }
    }
}
```

**Story 5.1 Implementation Notes:**
- ✅ NO DataStore changes needed - infrastructure already complete
- ✅ Speech rate clamping (0.5-2.0) handled in SettingsRepositoryImpl
- ✅ Default value (1.0×) correct per AC requirements
- ⚠️ TTSManager needs extension to apply speech rate (Task 4)
- ⚠️ Settings UI needs creation (fragment_settings.xml, SettingsFragment.kt, SettingsViewModel.kt)

### TTSManager Extension for Speech Rate Control

From [architecture.md] and [Story 2.2 implementation]:

**Current TTSManager Implementation:**
```kotlin
// TTSManager.kt (EXISTING - Story 2.2)
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
        }
    }
    
    fun announce(message: String) {
        if (!isInitialized) return
        
        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,  // Epic 8: Priority queue integration point
            null,
            null
        )
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
```

**Story 5.1 Extension (Task 4):**
```kotlin
// TTSManager.kt (STORY 5.1 ADDITIONS)
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository  // NEW: Inject settings
) : TextToSpeech.OnInitListener {
    
    // Existing fields...
    private var currentSpeechRate: Float = 1.0f  // NEW: Track current rate
    
    init {
        tts = TextToSpeech(context, this)
        observeSpeechRateChanges()  // NEW: Listen for rate changes
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
            // Apply current speech rate on init
            tts?.setSpeechRate(currentSpeechRate)
        }
    }
    
    // NEW: Observe speech rate changes from SettingsRepository
    private fun observeSpeechRateChanges() {
        // Use GlobalScope or pass CoroutineScope from Application
        // Alternatively: Inject CoroutineScope via Hilt
        settingsRepository.getSpeechRate()
            .onEach { rate ->
                currentSpeechRate = rate
                if (isInitialized) {
                    tts?.setSpeechRate(rate)  // Apply immediately
                }
            }
            .launchIn(applicationScope)  // Need to handle coroutine scope
    }
    
    // NEW: Public method for immediate rate change (called by ViewModel)
    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(currentSpeechRate)
        }
    }
    
    fun announce(message: String) {
        if (!isInitialized) return
        
        // Ensure rate is applied before speaking (defensive)
        tts?.setSpeechRate(currentSpeechRate)
        
        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
            null,
            null
        )
    }
    
    // Existing shutdown() method...
}
```

**Coroutine Scope Handling:**
- **Option 1 (Recommended):** Inject `@ApplicationScope CoroutineScope` via Hilt
- **Option 2:** Use `GlobalScope` (acceptable for app-lifetime services)
- **Option 3:** Accept CoroutineScope parameter in TTSManager constructor

**Hilt Module for ApplicationScope:**
```kotlin
// ApplicationModule.kt (NEW or extend existing AppModule)
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
```

### Settings UI Design and Accessibility

From [architecture.md#Decision 3: UI Architecture - XML Layouts]:

**Settings Screen Layout Pattern:**

```xml
<!-- fragment_settings.xml (NEW - Story 5.1 Task 1) -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">
    
    <!-- Settings Screen Title -->
    <TextView
        android:id="@+id/settingsTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="@string/settings_title"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="?attr/colorOnSurface"
        android:accessibilityHeading="true"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Speech Rate Section Label -->
    <TextView
        android:id="@+id/speechRateLabel"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="@string/speech_rate_label"
        android:textSize="18sp"
        android:textColor="?attr/colorOnSurface"
        android:layout_marginTop="24dp"
        app:layout_constraintTop_toBottomOf="@id/settingsTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Current Speech Rate Display -->
    <TextView
        android:id="@+id/currentRateTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/default_speech_rate"
        android:textSize="20sp"
        android:textStyle="bold"
        android:textColor="?attr/colorPrimary"
        android:layout_marginTop="8dp"
        android:importantForAccessibility="no"
        app:layout_constraintTop_toBottomOf="@id/speechRateLabel"
        app:layout_constraintStart_toStartOf="parent" />
    
    <!-- Speech Rate SeekBar (0.5x - 2.0x) -->
    <SeekBar
        android:id="@+id/speechRateSeekBar"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:minHeight="48dp"
        android:layout_marginTop="8dp"
        android:max="15"
        android:progress="5"
        android:contentDescription="@string/speech_rate_slider_description"
        app:layout_constraintTop_toBottomOf="@id/currentRateTextView"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Range Labels (0.5x and 2.0x) -->
    <TextView
        android:id="@+id/minRateLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/min_speech_rate"
        android:textSize="14sp"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:importantForAccessibility="no"
        app:layout_constraintTop_toBottomOf="@id/speechRateSeekBar"
        app:layout_constraintStart_toStartOf="@id/speechRateSeekBar" />
    
    <TextView
        android:id="@+id/maxRateLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/max_speech_rate"
        android:textSize="14sp"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:importantForAccessibility="no"
        app:layout_constraintTop_toBottomOf="@id/speechRateSeekBar"
        app:layout_constraintEnd_toEndOf="@id/speechRateSeekBar" />
    
    <!-- Test Speed Button -->
    <Button
        android:id="@+id/testSpeedButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/test_speed_button"
        android:contentDescription="@string/test_speed_button_description"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/minRateLabel"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Future Settings Sections (Story 5.2-5.5) -->
    <!-- TTS Voice Selection -->
    <!-- Verbosity Mode -->
    <!-- High-Contrast Toggle -->
    <!-- Haptic Intensity -->
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

**String Resources:**
```xml
<!-- strings.xml (ADD TO EXISTING) -->
<resources>
    <!-- Settings Screen -->
    <string name="settings_title">Settings</string>
    
    <!-- Speech Rate Section -->
    <string name="speech_rate_label">Speech Rate</string>
    <string name="default_speech_rate">1.0×</string>
    <string name="min_speech_rate">0.5×</string>
    <string name="max_speech_rate">2.0×</string>
    <string name="speech_rate_slider_description">Speech rate, slider, currently 1.0 times normal speed</string>
    <string name="test_speed_button">Test Speed</string>
    <string name="test_speed_button_description">Test speed, button. Play sample announcement at current rate.</string>
    <string name="sample_announcement_text">This is how your speech rate sounds.</string>
    
    <!-- Rate Limit Announcements -->
    <string name="speech_rate_at_minimum">Speech rate at minimum</string>
    <string name="speech_rate_at_maximum">Speech rate at maximum</string>
    <string name="speech_rate_changed">Speech rate changed to %1$.1f times normal speed</string>
    
    <!-- Voice Command Announcements -->
    <string name="speech_rate_increased">Speech rate increased to %1$.1f times</string>
    <string name="speech_rate_decreased">Speech rate decreased to %1$.1f times</string>
</resources>
```

**SeekBar Progress Calculation:**
```kotlin
// SettingsFragment.kt - SeekBar value mapping
// SeekBar max=15 (16 steps: 0-15)
// Map to range 0.5-2.0 with 0.1 increments

private fun progressToSpeechRate(progress: Int): Float {
    // progress 0 → 0.5×
    // progress 15 → 2.0×
    // Formula: 0.5 + (progress * 0.1)
    return 0.5f + (progress * 0.1f)
}

private fun speechRateToProgress(rate: Float): Int {
    // rate 0.5× → progress 0
    // rate 2.0× → progress 15
    // Formula: (rate - 0.5) / 0.1
    return ((rate - 0.5f) / 0.1f).roundToInt()
}
```

### Accessibility Compliance for Settings Screen

From [docs/AccessibilityGuidelines.md] (Story 2.7):

**SeekBar Accessibility Requirements:**

**1. Touch Target Size:**
- SeekBar minHeight="48dp" ✅ (exceeds 48×48 dp requirement)
- Test Speed Button minHeight="48dp" ✅

**2. TalkBack Labels:**
- SeekBar contentDescription: "Speech rate, slider, currently X.X times normal speed" ✅
- Update contentDescription dynamically as user moves slider (Task 2.8)
- Test Speed Button: "Test speed, button. Play sample announcement at current rate." ✅

**3. Focus Order:**
- Logical sequence: Title (heading) → Speech Rate Label → Current Rate Display (excluded from TalkBack) → SeekBar → Test Button
- Current Rate TextView: importantForAccessibility="no" (redundant with SeekBar contentDescription)
- Min/Max labels: importantForAccessibility="no" (decorative, SeekBar announces range)

**4. State Announcements:**
- SeekBar value change: Announce via accessibilityLiveRegion (Android handles automatically)
- Rate limits: Custom announcement when user tries to exceed 0.5× or 2.0×
- Sample announcement: TTS plays at selected rate (instant feedback)

**5. SeekBar TalkBack Gestures:**
- Swipe up/down: Increment/decrement by 1 step (Android default)
- Double-tap and hold: Enter continuous adjustment mode
- Story 5.1: Accept Android default SeekBar behavior (no custom gestures needed)

**Accessibility Testing Checklist (Task 9):**
- [ ] Settings screen passes Accessibility Scanner (zero errors)
- [ ] SeekBar announces current value on focus
- [ ] SeekBar contentDescription updates as slider moves
- [ ] Test Speed button plays announcement at correct rate
- [ ] Focus order: Title → SeekBar → Button (logical sequence)
- [ ] Rate limit announcements work at min/max values

### SettingsViewModel Implementation Pattern

From [architecture.md#Decision 2: State Management - StateFlow]:

```kotlin
// SettingsViewModel.kt (NEW - Story 5.1 Task 3)
package com.visionfocus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.tts.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * 
 * Manages speech rate preference with reactive UI updates via StateFlow.
 * Provides TTS sample announcements for instant user feedback.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TTSManager
) : ViewModel() {
    
    companion object {
        private const val MIN_SPEECH_RATE = 0.5f
        private const val MAX_SPEECH_RATE = 2.0f
        private const val SPEECH_RATE_INCREMENT = 0.25f
    }
    
    /**
     * Current speech rate from DataStore.
     * UI observes this Flow to update SeekBar position and display.
     */
    val speechRate: StateFlow<Float> = settingsRepository.getSpeechRate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )
    
    /**
     * One-time announcement events for TalkBack.
     * UI collects these to trigger accessibility announcements.
     */
    private val _announcements = MutableSharedFlow<String>()
    val announcements: SharedFlow<String> = _announcements.asSharedFlow()
    
    /**
     * Updates speech rate preference.
     * 
     * @param rate New speech rate (0.5x - 2.0x). Values outside range are clamped.
     */
    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            val clampedRate = rate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
            
            // Persist to DataStore
            settingsRepository.setSpeechRate(clampedRate)
            
            // Apply to TTSManager immediately
            ttsManager.setSpeechRate(clampedRate)
        }
    }
    
    /**
     * Increments speech rate by 0.25× (voice command integration).
     * 
     * Used by "Increase speed" voice command (Epic 3).
     */
    fun incrementSpeechRate() {
        viewModelScope.launch {
            val currentRate = speechRate.value
            val newRate = (currentRate + SPEECH_RATE_INCREMENT).coerceAtMost(MAX_SPEECH_RATE)
            
            if (newRate == MAX_SPEECH_RATE && currentRate == MAX_SPEECH_RATE) {
                // Already at maximum
                _announcements.emit("Speech rate at maximum")
            } else {
                setSpeechRate(newRate)
                _announcements.emit("Speech rate increased to %.2f times".format(newRate))
            }
        }
    }
    
    /**
     * Decrements speech rate by 0.25× (voice command integration).
     * 
     * Used by "Decrease speed" voice command (Epic 3).
     */
    fun decrementSpeechRate() {
        viewModelScope.launch {
            val currentRate = speechRate.value
            val newRate = (currentRate - SPEECH_RATE_INCREMENT).coerceAtLeast(MIN_SPEECH_RATE)
            
            if (newRate == MIN_SPEECH_RATE && currentRate == MIN_SPEECH_RATE) {
                // Already at minimum
                _announcements.emit("Speech rate at minimum")
            } else {
                setSpeechRate(newRate)
                _announcements.emit("Speech rate decreased to %.2f times".format(newRate))
            }
        }
    }
    
    /**
     * Plays sample announcement at current speech rate.
     * 
     * Triggered by "Test Speed" button for instant user feedback.
     * 
     * @param sampleText Optional custom text. Defaults to "This is how your speech rate sounds."
     */
    fun playSampleAnnouncement(sampleText: String = "This is how your speech rate sounds.") {
        viewModelScope.launch {
            // TTSManager automatically uses currentSpeechRate
            ttsManager.announce(sampleText)
        }
    }
}
```

### Settings Navigation Implementation

From [architecture.md#UI Architecture - Fragment Navigation]:

**Option 1: Simple FragmentTransaction (Recommended for Story 5.1):**
```kotlin
// RecognitionFragment.kt (MODIFY - Task 6)
// Add settings button to fragment_recognition.xml

private fun navigateToSettings() {
    parentFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SettingsFragment())
        .addToBackStack("settings")
        .commit()
}

// In onViewCreated():
binding.settingsButton.setOnClickListener {
    navigateToSettings()
}
```

**Option 2: Navigation Component (If Already Configured):**
```kotlin
// RecognitionFragment.kt
private fun navigateToSettings() {
    findNavController().navigate(R.id.action_recognition_to_settings)
}
```

**Settings Icon Button in RecognitionFragment:**
```xml
<!-- fragment_recognition.xml (MODIFY - Task 6.1) -->
<!-- Add to existing layout -->

<ImageButton
    android:id="@+id/settingsButton"
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_settings"
    android:contentDescription="@string/settings_button_description"
    android:background="?attr/selectableItemBackgroundBorderless"
    app:tint="?attr/colorOnSurface"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_margin="16dp" />
```

### Voice Command Integration (Epic 3 Placeholder)

From [epics.md#Epic 3: Voice Command System]:

**Story 5.1 Voice Command Hooks (Task 5):**

```kotlin
// VoiceCommandProcessor.kt (FUTURE - Epic 3)
// Story 5.1 prepares ViewModel methods for voice command integration

fun processCommand(command: String) {
    when (command.lowercase()) {
        "increase speed" -> {
            settingsViewModel.incrementSpeechRate()
            // Announcement handled by ViewModel
        }
        "decrease speed" -> {
            settingsViewModel.decrementSpeechRate()
            // Announcement handled by ViewModel
        }
        // Other commands...
    }
}
```

**Story 5.1 Implementation:**
- ✅ SettingsViewModel provides incrementSpeechRate() and decrementSpeechRate() methods
- ✅ Methods announce rate changes via announcements SharedFlow
- ⚠️ Actual voice command recognition not implemented (Epic 3 scope)
- 📝 Document integration points in code comments (Task 5.6)

### Performance Considerations

From [epics.md#Non-Functional Requirements - Performance]:

**Performance Budget for Story 5.1:**
- **SeekBar interaction latency:** ≤50ms (slider must feel responsive)
- **DataStore write latency:** ≤100ms (acceptable for settings changes)
- **TTS rate change latency:** ≤10ms (Android TTS setSpeechRate() is synchronous)
- **Sample announcement initiation:** ≤200ms (Story 2.2 TTS latency requirement)
- **Settings screen load time:** ≤300ms (standard fragment transition)

**Optimization Strategies:**
- ✅ SeekBar.onStopTrackingTouch: Write to DataStore only when user releases slider (not every onProgressChanged)
- ✅ TTSManager.setSpeechRate(): Lightweight operation, safe to call on every preference change
- ✅ StateFlow observation: Minimal overhead, efficient UI updates
- ✅ No network calls: All settings stored locally (zero latency)

### Testing Requirements

From [architecture.md#Decision 4: Testing Strategy]:

**Required Tests for Story 5.1:**

**1. Unit Tests (Task 8):**
```kotlin
// SettingsViewModelTest.kt (NEW)
@Test
fun `setSpeechRate clamps values outside valid range`() = runTest {
    // Test: rate < 0.5 → clamped to 0.5
    viewModel.setSpeechRate(0.3f)
    assertEquals(0.5f, viewModel.speechRate.value)
    
    // Test: rate > 2.0 → clamped to 2.0
    viewModel.setSpeechRate(3.0f)
    assertEquals(2.0f, viewModel.speechRate.value)
}

@Test
fun `incrementSpeechRate adds 0_25 and announces new rate`() = runTest {
    // Set initial rate to 1.0
    viewModel.setSpeechRate(1.0f)
    
    // Increment
    viewModel.incrementSpeechRate()
    
    // Verify new rate
    assertEquals(1.25f, viewModel.speechRate.value)
    
    // Verify announcement
    val announcement = viewModel.announcements.first()
    assertTrue(announcement.contains("1.25"))
}

@Test
fun `rate at maximum triggers max announcement`() = runTest {
    // Set to maximum
    viewModel.setSpeechRate(2.0f)
    
    // Try to increment
    viewModel.incrementSpeechRate()
    
    // Verify rate unchanged
    assertEquals(2.0f, viewModel.speechRate.value)
    
    // Verify "at maximum" announcement
    val announcement = viewModel.announcements.first()
    assertEquals("Speech rate at maximum", announcement)
}
```

**2. Integration Tests (Task 9):**
```kotlin
// SettingsFragmentTest.kt (NEW)
@Test
fun settingsFragment_displaysDefaultSpeechRate() {
    // Launch Settings fragment
    launchFragmentInHiltContainer<SettingsFragment>()
    
    // Verify SeekBar at default position (1.0× = progress 5)
    onView(withId(R.id.speechRateSeekBar))
        .check(matches(withProgress(5)))
    
    // Verify current rate display shows "1.0×"
    onView(withId(R.id.currentRateTextView))
        .check(matches(withText("1.0×")))
}

@Test
fun settingsFragment_updatesSpeechRateOnSliderChange() {
    launchFragmentInHiltContainer<SettingsFragment>()
    
    // Move slider to 1.5× (progress 10)
    onView(withId(R.id.speechRateSeekBar))
        .perform(setProgress(10))
    
    // Verify current rate display updates
    onView(withId(R.id.currentRateTextView))
        .check(matches(withText("1.5×")))
}

@Test
fun settingsFragment_testSpeedButtonPlaysSampleAnnouncement() {
    launchFragmentInHiltContainer<SettingsFragment>()
    
    // Tap "Test Speed" button
    onView(withId(R.id.testSpeedButton))
        .perform(click())
    
    // Verify TTSManager.announce() was called
    // (Mock TTSManager via Hilt test module)
    verify(mockTtsManager).announce("This is how your speech rate sounds.")
}
```

**3. Accessibility Tests (Task 9.6):**
```kotlin
// SettingsAccessibilityTest.kt (NEW)
@Test
fun settingsFragment_passesAccessibilityScanner() {
    AccessibilityChecks.enable()
        .setThrowExceptionForErrors(true)
    
    launchFragmentInHiltContainer<SettingsFragment>()
    
    // Verify SeekBar has contentDescription
    onView(withId(R.id.speechRateSeekBar))
        .check(matches(isDisplayed()))
        .check(matches(withContentDescription(containsString("Speech rate"))))
    
    // Scanner runs automatically on view interactions
    // Test will fail if any accessibility errors detected
}
```

### Security & Privacy Considerations

**Story 5.1 Privacy Impact: Minimal**

- Speech rate preference does not contain sensitive user data (just a float value)
- DataStore encryption not required for speech rate (non-sensitive preference)
- No network operations (all local storage via DataStore)
- No analytics or telemetry (user preference stays on device)

**Future Privacy Considerations:**
- Story 5.2 (TTS Voice Selection): Voice preference may reveal language/accent (still non-sensitive)
- Epic 7 (Saved Locations): Requires encryption (addresses, GPS coordinates)
- Epic 4 (Recognition History): Requires encryption (object detections may reveal context)

### Known Limitations and Future Work

**Story 5.1 Limitations:**

1. **Voice Command Integration:** Not implemented (Epic 3 scope)
   - SettingsViewModel provides incrementSpeechRate() / decrementSpeechRate() methods
   - Actual voice recognition happens in Epic 3

2. **Settings Screen Structure:** Minimal (only speech rate)
   - Story 5.2-5.5 add more preferences to same screen
   - Future: May need scrollable layout (ScrollView) when settings exceed screen height

3. **TTS Engine Compatibility:** Android TTS variations across OEMs
   - Samsung, Google, OnePlus TTS engines may have different speech rate curves
   - 1.0× may sound slightly different across devices (acceptable variance)

4. **SeekBar Granularity:** 0.1 increments (16 steps total)
   - Voice commands use 0.25× increments (coarser adjustment)
   - UI provides finer control than voice commands (by design)

5. **Sample Announcement Text:** Fixed string ("This is how your speech rate sounds.")
   - Future: Could test with recognition announcement template
   - Current: Sufficient for Story 5.1 acceptance criteria

### References

**Technical Details with Source Paths:**

1. **Story 5.1 Requirements:**
   - [Source: _bmad-output/project-planning-artifacts/epics.md#Epic 5: Story 5.1]
   - AC: Speech rate slider 0.5×-2.0× with 0.1 increments
   - AC: Voice commands "Increase/Decrease speed" (0.25× increments)
   - AC: Sample announcement at selected rate
   - AC: Preference persists in DataStore

2. **DataStore Infrastructure:**
   - [Source: app/src/main/java/com/visionfocus/data/repository/SettingsRepository.kt]
   - getSpeechRate() / setSpeechRate(rate) already implemented ✅
   - Default: 1.0f, Min: 0.5f, Max: 2.0f (matches AC requirements)

3. **TTSManager Foundation:**
   - [Source: Story 2.2 implementation]
   - announce() method for TTS playback
   - Story 5.1 extends with setSpeechRate() support

4. **Accessibility Guidelines:**
   - [Source: docs/AccessibilityGuidelines.md] (Story 2.7)
   - SeekBar accessibility: contentDescription, touch target size, TalkBack gestures
   - Settings screen must pass Accessibility Scanner (zero errors)

5. **Architecture Patterns:**
   - [Source: _bmad-output/architecture.md#Decision 1: DataStore]
   - DataStore + SettingsRepository pattern (already implemented)
   - [Source: _bmad-output/architecture.md#Decision 2: StateFlow]
   - ViewModel exposes StateFlow for reactive UI updates

6. **UI Architecture:**
   - [Source: _bmad-output/architecture.md#Decision 3: XML Layouts]
   - Traditional Android Views with View Binding
   - High-contrast theme support, accessibility-first design

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (December 30, 2025)

### Debug Log References

No debug logs required - implementation based on existing patterns from Stories 1.3, 2.2, 2.7

### Completion Notes List

*Story not yet implemented - awaiting developer execution*

**Story 5.1 Ready-for-Dev Summary:**

**Infrastructure Already Complete:**
- ✅ DataStore with SettingsRepository (Story 1.3)
- ✅ Speech rate preference methods (getSpeechRate, setSpeechRate)
- ✅ Clamping logic (0.5-2.0 range) built into SettingsRepositoryImpl
- ✅ TTSManager foundation (Story 2.2)
- ✅ Accessibility compliance baseline (Story 2.7)

**New Components Required:**
1. **Settings UI:** fragment_settings.xml with SeekBar, current rate display, test button
2. **SettingsFragment:** Fragment with View Binding, SeekBar listener, ViewModel observation
3. **SettingsViewModel:** HiltViewModel exposing speechRate StateFlow, increment/decrement methods
4. **TTSManager Extension:** Add setSpeechRate(), observe SettingsRepository changes
5. **Navigation:** Settings button in RecognitionFragment, FragmentTransaction to SettingsFragment

**Testing Requirements:**
- Unit tests: SettingsViewModel logic (clamping, increment/decrement, announcements)
- Integration tests: Settings UI interaction (slider, button, persistence)
- Accessibility tests: SeekBar contentDescription, zero Accessibility Scanner errors

**Voice Command Integration:**
- Prepared: incrementSpeechRate() / decrementSpeechRate() methods in ViewModel
- Deferred: Actual voice recognition (Epic 3 scope)
- Documented: Integration points in code comments

**Epic 5 Foundation:**
Story 5.1 establishes Settings screen structure for all Epic 5 stories (5.2-5.5). Future stories add preferences to this same screen.

### File List

**Files to Create:**
1. **app/src/main/res/layout/fragment_settings.xml**
   - Settings screen layout with speech rate SeekBar
   - Current rate display, range labels, test button
   - Accessibility: All touch targets ≥48dp, proper contentDescriptions

2. **app/src/main/java/com/visionfocus/ui/settings/SettingsFragment.kt**
   - Fragment with View Binding and Hilt injection
   - SeekBar.OnSeekBarChangeListener for rate changes
   - Observe speechRate Flow from ViewModel
   - Update contentDescription dynamically

3. **app/src/main/java/com/visionfocus/ui/settings/SettingsViewModel.kt**
   - HiltViewModel with SettingsRepository and TTSManager injection
   - Expose speechRate StateFlow (from repository)
   - Methods: setSpeechRate(), incrementSpeechRate(), decrementSpeechRate()
   - Sample announcement: playSampleAnnouncement()

4. **app/src/test/java/com/visionfocus/ui/settings/SettingsViewModelTest.kt**
   - Unit tests for speech rate logic
   - Test: Clamping, increment/decrement, rate limit announcements

5. **app/src/androidTest/java/com/visionfocus/ui/settings/SettingsFragmentTest.kt**
   - Integration tests for Settings UI
   - Test: Slider interaction, persistence, sample announcement

6. **app/src/androidTest/java/com/visionfocus/ui/settings/SettingsAccessibilityTest.kt**
   - Accessibility Scanner validation
   - Test: SeekBar contentDescription, zero errors

**Files to Modify:**
1. **app/src/main/java/com/visionfocus/tts/TTSManager.kt**
   - Add: Private field currentSpeechRate: Float = 1.0f
   - Add: Method setSpeechRate(rate: Float)
   - Add: Method observeSpeechRateChanges() (coroutine)
   - Modify: onInit() to apply currentSpeechRate
   - Modify: announce() to ensure rate applied before speaking

2. **app/src/main/res/layout/fragment_recognition.xml**
   - Add: Settings button (ImageButton, 48×48 dp, top-right corner)
   - contentDescription: "Settings, button. Open app settings."

3. **app/src/main/java/com/visionfocus/ui/recognition/RecognitionFragment.kt**
   - Add: navigateToSettings() method
   - Add: Settings button click listener in onViewCreated()

4. **app/src/main/res/values/strings.xml**
   - Add: Settings screen strings (title, labels, announcements)
   - Add: Speech rate strings (min, max, slider description, test button)

5. **app/src/main/java/com/visionfocus/di/ApplicationModule.kt** (if not exists, create)
   - Add: @ApplicationScope CoroutineScope provider for TTSManager

6. **docs/AccessibilityGuidelines.md**
   - Add: Settings screen accessibility patterns (SeekBar, sample feedback)
   - Add: SeekBar TalkBack usage documentation

**Technical Achievements (Once Implemented):**
- ✅ First user-configurable preference with UI (Settings screen foundation)
- ✅ DataStore persistence validated (survive app restart)
- ✅ TTSManager speech rate control (immediate application to announcements)
- ✅ Sample announcement pattern (instant user feedback)
- ✅ Accessibility compliance (SeekBar with TalkBack support)
- ✅ Voice command preparation (increment/decrement methods ready for Epic 3)

## Change Log

**Story 5.1: TTS Speech Rate Adjustment - Ready for Development**  
**Date:** December 30, 2025  
**Status:** ready-for-dev (comprehensive story context created)

**Story Context Analysis:**
- ✅ Epic 5 requirements analyzed from epics.md
- ✅ Architecture requirements confirmed (DataStore, TTSManager patterns)
- ✅ Previous story patterns reviewed (Stories 1.3, 2.2, 2.7)
- ✅ Existing infrastructure validated (SettingsRepository already implements speech rate)
- ✅ Implementation guidance documented with code examples

**Key Implementation Notes:**

1. **DataStore Foundation Already Complete (Story 1.3)**
   - SettingsRepository.getSpeechRate() / setSpeechRate(rate) available
   - Clamping logic (0.5-2.0) built into SettingsRepositoryImpl
   - Default value (1.0×) matches AC requirements
   - No DataStore changes needed

2. **TTSManager Extension Required (Task 4)**
   - Add currentSpeechRate field and setSpeechRate() method
   - Observe SettingsRepository changes via coroutine
   - Apply rate before every announce() call
   - Need @ApplicationScope CoroutineScope injection via Hilt

3. **Settings UI Creation (Tasks 1-2)**
   - fragment_settings.xml with SeekBar (0.5-2.0 range, 0.1 increments)
   - SeekBar progress mapping: 0-15 → 0.5-2.0 (16 steps)
   - Current rate TextView, test button, accessibility labels
   - All touch targets ≥48dp (Story 2.7 pattern)

4. **SettingsViewModel Pattern (Task 3)**
   - HiltViewModel exposing speechRate StateFlow
   - Methods: setSpeechRate(), incrementSpeechRate(), decrementSpeechRate()
   - Announcements SharedFlow for rate limit feedback
   - Sample announcement: playSampleAnnouncement()

5. **Voice Command Preparation (Task 5)**
   - ViewModel provides increment/decrement methods
   - Actual voice recognition deferred to Epic 3
   - Integration points documented

**Dependencies:**
- Story 1.3: DataStore infrastructure (foundation) ✅
- Story 2.2: TTSManager (will be extended) ✅
- Story 2.7: Accessibility guidelines (must follow) ✅
- Epic 3: Voice commands (future integration point) ⏸️

**Next Steps:**
- Developer implements SettingsFragment + SettingsViewModel
- Developer extends TTSManager with speech rate control
- Developer adds Settings navigation from RecognitionFragment
- Developer writes unit + integration + accessibility tests
- Code review validates AC compliance
