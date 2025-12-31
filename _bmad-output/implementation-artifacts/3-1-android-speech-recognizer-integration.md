# Story 3.1: Android Speech Recognizer Integration

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a visually impaired user,
I want to control the app using voice commands,
So that I can operate it hands-free without touching the screen.

## Acceptance Criteria

**Given** microphone permission granted
**When** I activate voice command mode
**Then** Android SpeechRecognizer service initializes successfully
**And** microphone permission (android.permission.RECORD_AUDIO) is declared in manifest
**And** microphone icon button (56×56 dp) appears in top-right corner with TalkBack label: "Voice commands, button"
**And** tapping microphone button activates listening mode
**And** listening state announces via TTS: "Listening for command"
**And** visual indicator shows listening state (pulsing microphone icon)
**And** speech audio is captured and sent to on-device speech recognition
**And** recognition timeout occurs after 5 seconds of silence
**And** recognized text is converted to lowercase for command matching
**And** recognition errors announce clearly: "Didn't catch that. Please try again."

## Tasks / Subtasks

- [x] Task 1: Add microphone permission to AndroidManifest and create VoiceRecognitionManager (AC: 1, 2)
  - [x] 1.1: Add android.permission.RECORD_AUDIO to AndroidManifest.xml
  - [x] 1.2: Create VoiceRecognitionManager.kt in voice/recognizer package with Hilt @Singleton
  - [x] 1.3: Initialize SpeechRecognizer in VoiceRecognitionManager constructor
  - [x] 1.4: Implement RecognitionListener interface for speech callbacks
  - [x] 1.5: Configure recognition parameters (language, on-device recognition)
  
- [x] Task 2: Extend PermissionManager for microphone permission (AC: 1)
  - [x] 2.1: Add isMicrophonePermissionGranted() method to PermissionManager
  - [x] 2.2: Add requestMicrophonePermission() method using ActivityResultContracts
  - [x] 2.3: Create microphone permission rationale strings in strings.xml
  - [x] 2.4: Implement rationale dialog for previously denied microphone permission
  - [x] 2.5: Add TalkBack announcements for microphone grant/deny events
  
- [x] Task 3: Create voice command button UI component (AC: 3)
  - [x] 3.1: Design microphone FAB (56×56 dp) with Material Design 3 icon
  - [x] 3.2: Position button consistently in top-right corner (16dp margins)
  - [x] 3.3: Add contentDescription: "Voice commands, button"
  - [x] 3.4: Implement touch target validation (minimum 56×56 dp)
  - [x] 3.5: Add high-contrast mode support for voice button
  - [x] 3.6: Inject voice button into MainActivity layout
  
- [x] Task 4: Implement listening mode activation and visual feedback (AC: 4, 5, 6)
  - [x] 4.1: Handle voice button click to activate SpeechRecognizer.startListening()
  - [x] 4.2: Announce via TTS: "Listening for command" when listening starts
  - [x] 4.3: Create pulsing animation for microphone icon (Material Design motion)
  - [x] 4.4: Show visual indicator: animated mic icon (scale + alpha pulse loop)
  - [x] 4.5: Ensure TTS stops before listening starts (avoid self-recognition)
  - [x] 4.6: Add haptic feedback on voice button press (medium intensity)
  
- [x] Task 5: Implement speech capture and recognition callbacks (AC: 7, 8)
  - [x] 5.1: Configure RecognitionListener.onReadyForSpeech() callback
  - [x] 5.2: Implement RecognitionListener.onResults() to capture transcription
  - [x] 5.3: Convert recognized text to lowercase for command matching
  - [x] 5.4: Configure 5-second silence timeout in recognition intent extras
  - [x] 5.5: Implement onEndOfSpeech() to handle recognition completion
  - [x] 5.6: Pass recognized text to command processor (Story 3.2 integration point)
  
- [x] Task 6: Implement error handling and timeout management (AC: 9, 10, 11)
  - [x] 6.1: Implement RecognitionListener.onError() with error code handling
  - [x] 6.2: Handle ERROR_NO_MATCH: announce "Didn't catch that. Please try again."
  - [x] 6.3: Handle ERROR_SPEECH_TIMEOUT: announce "Voice command timed out"
  - [x] 6.4: Handle ERROR_NETWORK: fallback to on-device recognition or announce unavailable
  - [x] 6.5: Handle ERROR_AUDIO: announce "Microphone error. Please check device settings."
  - [x] 6.6: Stop pulsing animation and reset button state on error
  - [x] 6.7: Clear TTS queue before error announcements
  
- [x] Task 7: Create VoiceRecognitionViewModel for state management (AC: All)
  - [x] 7.1: Create VoiceRecognitionViewModel with Hilt @HiltViewModel
  - [x] 7.2: Define VoiceRecognitionState sealed class (Idle, Listening, Processing, Error)
  - [x] 7.3: Expose StateFlow<VoiceRecognitionState> for UI observation
  - [x] 7.4: Implement startListening() public method
  - [x] 7.5: Implement stopListening() public method
  - [x] 7.6: Handle permission state changes (disable button if denied)
  
- [x] Task 8: Integrate with existing accessibility infrastructure (AC: 3, 5)
  - [x] 8.1: Use TTSManager for TTS announcements
  - [x] 8.2: Ensure voice button focus order is logical (after recognition FAB)
  - [x] 8.3: Test voice button TalkBack navigation with swipe gestures
  - [x] 8.4: Verify contentDescription announces correctly with TalkBack
  - [x] 8.5: Test button state changes announce appropriately (idle → listening)
  
- [x] Task 9: Unit testing for VoiceRecognitionManager (AC: All)
  - [x] 9.1: Mock SpeechRecognizer API for unit tests
  - [x] 9.2: Test startListening() initializes recognition successfully
  - [x] 9.3: Test onResults() processes transcription correctly (lowercase conversion)
  - [x] 9.4: Test onError() handles all error codes with appropriate announcements
  - [x] 9.5: Test 5-second timeout configuration
  - [x] 9.6: Test recognition cancellation (stopListening())
  
- [x] Task 10: Integration testing with TalkBack and permissions (AC: 1, 2, 3)
  - [x] 10.1: Test microphone permission request flow with TalkBack
  - [x] 10.2: Test permission denial shows rationale dialog
  - [x] 10.3: Test voice button disabled when permission denied
  - [x] 10.4: Test TalkBack announces listening state changes
  - [x] 10.5: Test voice button with TalkBack double-tap gesture
  - [x] 10.6: Validate 56×56 dp touch target programmatically
  
- [ ] Task 11: Device testing across acoustic environments (AC: 7, 11)
  - [ ] 11.1: Test speech recognition in quiet room (baseline)
  - [ ] 11.2: Test with background noise (music, conversation)
  - [ ] 11.3: Test in outdoor environment (street noise, wind)
  - [ ] 11.4: Verify on-device recognition works offline (airplane mode)
  - [ ] 11.5: Test with different voices (male, female, accents)
  - [ ] 11.6: Document recognition quality across environments

## Dev Notes

### Critical Architecture Context

**Voice Command Foundation for Epic 3**

From [epics.md#Epic 3: Voice Command System]:

This is the FIRST story in Epic 3, establishing the foundation for all voice command functionality:
- **Story 3.1 (THIS STORY):** Android SpeechRecognizer integration, microphone permission, voice button UI
- **Story 3.2:** Core voice command processor (15 commands: Recognize, Navigate, Settings, etc.)
- **Story 3.3:** Voice command confirmation & cancellation ("Cancel" stops operations)
- **Story 3.4:** Voice command help system ("Help" announces available commands)
- **Story 3.5:** Always-available voice activation (button on all screens)

**Critical Voice Requirements:**
- **FR17:** Users can control app via 15 core voice commands (implemented across Stories 3.1-3.2)
- **FR18:** System recognizes voice commands in various acoustic environments (tested in Task 11)
- **FR19:** System provides immediate audio confirmation (Story 3.3 implements confirmations)
- **FR20:** Users can cancel operations mid-execution via voice command (Story 3.3)
- **FR55:** System requests and manages microphone permission (implemented in this story)

**Performance Requirements for Voice:**
From [architecture.md#Non-Functional Requirements - Performance]:
- **Voice command acknowledgment within 300ms** of detection (NFR requirement)
- **≥85% voice command recognition accuracy** threshold, validated 92.1% (NFR requirement)
- Commands work across different acoustic environments (quiet, outdoor, transit)

**Integration Points with Existing Systems:**

1. **Epic 1 (Completed) - Foundation Ready:**
   - ✅ Hilt DI for VoiceRecognitionManager injection (Story 1.2)
   - ✅ PermissionManager pattern established (Story 1.5) - extend for microphone
   - ✅ AccessibilityAnnouncementHelper for TTS (Story 1.5) - reuse for voice feedback
   - ✅ Material Design 3 theming (Story 1.1) - voice button styling

2. **Epic 2 (Completed) - Recognition Integration:**
   - ✅ TTSManager for announcements (Story 2.2) - announce listening state
   - ✅ HapticFeedbackManager (Story 2.6) - provide tactile feedback on voice button
   - ✅ RecognitionViewModel (Stories 2.3-2.4) - trigger recognition via voice command (Story 3.2)
   - 🔗 RecognitionViewModel.kt:252 has placeholder: "Voice command cancellation (placeholder for Epic 3)"

3. **Future Epic Integration:**
   - ⏳ Story 3.2 will use recognized text from this story to dispatch commands
   - ⏳ Epic 4 (History): "History" voice command triggers history UI
   - ⏳ Epic 5 (Settings): "Settings", "Increase speed", "High contrast on" commands
   - ⏳ Epic 6 (Navigation): "Navigate to [destination]" voice input

### Technical Requirements from Architecture

**Android SpeechRecognizer API:**

From Android documentation and [architecture.md#Technical Stack]:

```kotlin
// Voice recognition configuration
val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    // Language model for voice commands (optimized for short phrases)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    
    // Prefer on-device recognition (API 33+) for privacy and offline support
    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    
    // English (US) for command recognition
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
    
    // Single result for command processing
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    
    // 5-second silence timeout per AC
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
    
    // Partial results disabled for simplicity (Story 3.1)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
}
```

**RecognitionListener Callbacks:**

```kotlin
interface RecognitionListener {
    // Called when recognizer ready to listen
    fun onReadyForSpeech(params: Bundle?)
    
    // Called when user starts speaking
    fun onBeginningOfSpeech()
    
    // Called when user stops speaking
    fun onEndOfSpeech()
    
    // Recognition results (AC: 7)
    fun onResults(results: Bundle?)  // Extract transcription here
    
    // Error handling (AC: 10, 11)
    fun onError(error: Int)  // Handle ERROR_NO_MATCH, ERROR_SPEECH_TIMEOUT, etc.
    
    // Unused but required
    fun onRmsChanged(rmsdB: Float) {}
    fun onPartialResults(partialResults: Bundle?) {}
    fun onEvent(eventType: Int, params: Bundle?) {}
    fun onBufferReceived(buffer: ByteArray?) {}
}
```

**Error Codes to Handle:**

From Android SpeechRecognizer documentation:
- `ERROR_NO_MATCH (7)`: No recognition results → "Didn't catch that. Please try again."
- `ERROR_SPEECH_TIMEOUT (6)`: No speech detected → "Voice command timed out"
- `ERROR_AUDIO (3)`: Microphone error → "Microphone error. Please check device settings."
- `ERROR_NETWORK (2)`: Network unavailable → Fallback to on-device or announce unavailable
- `ERROR_CLIENT (5)`: Client-side error → Log error, reset state
- `ERROR_INSUFFICIENT_PERMISSIONS (9)`: Permission denied → Show permission rationale

**Microphone Permission Integration:**

Extend PermissionManager.kt pattern from Story 1.5:

```kotlin
// permissions/manager/PermissionManager.kt - ADD to existing class

/**
 * Check if microphone permission is currently granted.
 * Story 3.1: Required for voice command functionality (FR55)
 */
fun isMicrophonePermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Request microphone permission for voice commands.
 * Shows rationale if user previously denied.
 * Story 3.1 Task 2
 */
fun requestMicrophonePermission(activity: Activity) {
    // Implementation similar to camera permission flow from Story 1.5
}
```

### Voice Button UI Specifications

**Material Design 3 Voice FAB:**

From [epics.md#UX Design Document - Primary Components]:

```xml
<!-- Voice command FAB positioned top-right (consistent across all screens) -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/voice_fab"
    android:layout_width="56dp"
    android:layout_height="56dp"
    android:layout_marginTop="16dp"
    android:layout_marginEnd="16dp"
    android:contentDescription="@string/voice_commands_button"
    app:fabSize="normal"
    app:srcCompat="@drawable/ic_mic"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:tint="?attr/colorOnPrimary"
    app:backgroundTint="?attr/colorPrimary" />
```

**Voice Button States:**

1. **Idle State:**
   - Microphone icon (Material Symbol `mic`)
   - Normal color (colorPrimary background)
   - ContentDescription: "Voice commands, button"

2. **Listening State:**
   - Animated microphone icon (pulsing scale + alpha)
   - Accent color (colorSecondary background)
   - ContentDescription: "Listening for command, button"
   - Animation: Scale 1.0 → 1.1 → 1.0 (600ms loop), Alpha 1.0 → 0.7 → 1.0

3. **Disabled State (Permission Denied):**
   - Grayed out microphone icon
   - Disabled color (colorSurfaceVariant background)
   - ContentDescription: "Voice commands unavailable. Microphone permission required, button"

**Pulsing Animation Implementation:**

```kotlin
// Pulsing animation for listening state (AC: 6)
private fun startPulsingAnimation() {
    val scaleAnimation = ObjectAnimator.ofFloat(voiceFab, "scaleX", 1.0f, 1.1f, 1.0f).apply {
        duration = 600
        repeatCount = ObjectAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
    }
    
    val alphaAnimation = ObjectAnimator.ofFloat(voiceFab, "alpha", 1.0f, 0.7f, 1.0f).apply {
        duration = 600
        repeatCount = ObjectAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
    }
    
    AnimatorSet().apply {
        playTogether(scaleAnimation, alphaAnimation)
        start()
    }
}
```

### Previous Story Learnings from Epic 2

**From Story 2.6 (Haptic Feedback):**
- ✅ Samsung device compatibility requires amplitude values: LIGHT=127, MEDIUM=191, STRONG=255
- ✅ Use VibrationEffect.createOneShot(duration, amplitude) for API 26+
- ✅ Check Vibrator.hasVibrator() before showing haptic settings
- 🔗 **APPLY TO STORY 3.1:** Add haptic feedback on voice button press (medium intensity, 100ms)

**From Story 2.7 (TalkBack Navigation):**
- ✅ Use AccessibilityAnnouncementHelper.announce() for state changes
- ✅ Clear previous announcements with announceForAccessibility("") before new announcement
- ✅ Test focus order with TalkBack swipe gestures
- ✅ ContentDescription format: "[action], [type]" e.g., "Voice commands, button"
- 🔗 **APPLY TO STORY 3.1:** Announce "Listening for command" when voice button pressed

**From Story 2.4 (Camera Capture & Focus Management):**
- ✅ Use ViewModel StateFlow for UI state management (clean separation)
- ✅ Stop TTS before starting listening (avoid self-recognition)
- ✅ Return focus to triggering button after operation completes
- 🔗 **APPLY TO STORY 3.1:** VoiceRecognitionViewModel with StateFlow<VoiceRecognitionState>

**From Story 1.5 (Camera Permissions):**
- ✅ Permission rationale dialog with title + message + Allow/Deny buttons
- ✅ TalkBack announcements for permission grant/deny events
- ✅ PermissionManager singleton pattern with Hilt injection
- ✅ ActivityResultContracts for permission requests
- 🔗 **APPLY TO STORY 3.1:** Extend PermissionManager with microphone methods (Task 2)

### AndroidManifest.xml - Microphone Permission

**Add Permission:**
```xml
<!-- AndroidManifest.xml - Story 3.1 addition -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Existing permissions from Stories 1.1-2.7 -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <!-- NEW: Story 3.1 microphone permission -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    
    <application ... >
        <!-- App content -->
    </application>
</manifest>
```

**Permission Justification:**
- **RECORD_AUDIO:** Required for voice command recognition (FR55, Story 3.1)
- **Privacy Impact:** Audio processed on-device only, no recording or transmission
- **Permission Type:** Dangerous permission (requires runtime request)
- **Graceful Degradation:** App functions without microphone (touch-only mode)

### Library & Framework Requirements

**Core Dependencies (Extend from Stories 1.1-2.7):**

```kotlin
// build.gradle.kts - Voice recognition uses built-in Android APIs
dependencies {
    // NO NEW DEPENDENCIES REQUIRED
    // Android SpeechRecognizer is part of android.speech package (API 26+)
    
    // Existing dependencies from previous stories:
    // - Hilt (VoiceRecognitionManager injection)
    // - Coroutines (StateFlow for ViewModel)
    // - Material Design 3 (FAB styling)
    // - Espresso Accessibility (testing)
}
```

**Important:** Android SpeechRecognizer is built into Android SDK, no external dependency needed.

### Project Structure Alignment

**New Files Created (Story 3.1):**

```
app/src/main/java/com/visionfocus/
├── voice/
│   ├── recognizer/
│   │   ├── VoiceRecognitionManager.kt         # NEW - SpeechRecognizer wrapper
│   │   └── VoiceRecognitionState.kt           # NEW - Sealed class for states
│   └── ui/
│       └── VoiceRecognitionViewModel.kt       # NEW - State management
├── permissions/
│   └── manager/
│       └── PermissionManager.kt               # MODIFY - Add microphone methods
└── ui/
    └── MainActivity.kt                        # MODIFY - Add voice button

app/src/main/res/
├── layout/
│   └── activity_main.xml                      # MODIFY - Add voice FAB
├── values/
│   └── strings.xml                            # MODIFY - Add voice command strings
└── drawable/
    └── ic_mic.xml                             # NEW - Microphone icon vector

app/src/androidTest/java/com/visionfocus/
└── voice/
    ├── VoiceRecognitionManagerTest.kt         # NEW - Unit tests
    └── VoiceButtonAccessibilityTest.kt        # NEW - TalkBack tests
```

**Package Organization:**
- `com.visionfocus.voice.recognizer` → Core voice recognition logic
- `com.visionfocus.voice.ui` → Voice UI components and ViewModels
- Follows existing package structure from Epic 1 (recognition, permissions, ui)

### Testing Strategy

**Unit Tests (Task 9):**
```kotlin
// VoiceRecognitionManagerTest.kt
@Test
fun `startListening initializes SpeechRecognizer successfully`() {
    // Mock SpeechRecognizer
    // Call startListening()
    // Verify startListening() called on recognizer
}

@Test
fun `onResults converts transcription to lowercase`() {
    // Mock results bundle with "RECOGNIZE" text
    // Trigger onResults()
    // Assert output is "recognize" (lowercase)
}

@Test
fun `onError ERROR_NO_MATCH announces retry message`() {
    // Trigger onError(ERROR_NO_MATCH)
    // Verify TTS announces "Didn't catch that. Please try again."
}
```

**Integration Tests (Task 10):**
```kotlin
// VoiceButtonAccessibilityTest.kt (extends BaseAccessibilityTest from Story 1.5)
@Test
fun voiceButton_hasCorrectContentDescription() {
    onView(withId(R.id.voice_fab))
        .check(matches(withContentDescription("Voice commands, button")))
}

@Test
fun voiceButton_meetsTouchTargetSize() {
    onView(withId(R.id.voice_fab))
        .check(matches(withMinimumSize(56, 56))) // 56dp minimum
}

@Test
fun voiceButton_disabledWhenPermissionDenied() {
    // Deny microphone permission
    // Check voice button is disabled
    // Verify contentDescription indicates unavailable
}
```

**Device Testing (Task 11):**
- **Quiet room:** Baseline recognition accuracy (expect ~94%)
- **Background noise:** Music/conversation (expect ~85-88%)
- **Outdoor:** Street noise, wind (expect ~78-85%)
- **Offline mode:** Airplane mode with on-device recognition (API 33+)
- **Different voices:** Male/female, native/non-native English
- **TalkBack mode:** All voice button interactions with screen reader

**Acceptance Criteria Validation:**
| AC | Validation Method | Success Criteria |
|----|-------------------|------------------|
| 1 | Manifest inspection | RECORD_AUDIO permission declared |
| 2 | Unit test | SpeechRecognizer.createSpeechRecognizer() succeeds |
| 3 | Accessibility test | Voice button 56×56dp with correct contentDescription |
| 4 | Device test (TalkBack) | Tap button activates listening mode |
| 5 | Device test (TalkBack) | TTS announces "Listening for command" |
| 6 | Manual test | Pulsing animation plays during listening |
| 7 | Unit test | Speech audio captured and passed to callbacks |
| 8 | Unit test | 5-second timeout configured correctly |
| 9 | Unit test | Recognized text converted to lowercase |
| 10 | Device test | Timeout announces "Voice command timed out" |
| 11 | Device test | Errors announce "Didn't catch that. Please try again." |

### Known Constraints & Considerations

**Constraint 1: On-Device vs Network Recognition**
- **API 33+:** EXTRA_PREFER_OFFLINE enables on-device recognition (privacy-first)
- **API 26-32:** May fall back to network recognition (requires internet)
- **Mitigation:** Test offline mode, announce network requirement if needed

**Constraint 2: Background Noise Handling**
- **Issue:** Street noise, wind, transit can degrade accuracy below 85% target
- **Mitigation:** Configure noise cancellation in SpeechRecognizer, provide visual feedback during listening, allow manual retry

**Constraint 3: TTS/Voice Conflict**
- **Issue:** SpeechRecognizer may recognize TTS announcements as commands
- **Mitigation:** Stop all TTS playback before startListening() (clearTTSQueue())

**Constraint 4: Permission Denial**
- **Issue:** User may deny microphone permission permanently
- **Mitigation:** Graceful degradation (touch-only mode), settings link to re-enable, clear messaging

**Constraint 5: Continuous Listening Not Supported**
- **Scope Limitation:** Story 3.1 implements single-shot recognition (tap button → listen → result)
- **Future Enhancement:** Story 3.5 will add always-available voice activation
- **Note:** Continuous "hotword" listening requires separate framework (not in Story 3.1)

### References

**Technical Details with Source Paths:**

1. **Story 3.1 Requirements:**
   - [Source: _bmad-output/project-planning-artifacts/epics.md#Epic 3: Voice Command System - Story 3.1]
   - FR17-FR20: Voice command requirements
   - FR55: Microphone permission management
   - NFR: ≥85% voice command accuracy, <300ms confirmation latency

2. **Voice Command Architecture:**
   - [Source: _bmad-output/architecture.md#Non-Functional Requirements - Performance]
   - Voice command acknowledgment within 300ms
   - ≥85% recognition accuracy (validated 92.1% in testing)
   - Commands work across acoustic environments

3. **Permission Pattern:**
   - [Source: _bmad-output/implementation-artifacts/1-5-camera-permissions-talkback-testing-framework.md]
   - PermissionManager.kt pattern established in Story 1.5
   - Runtime permission request flow with TalkBack announcements
   - Graceful degradation when permissions denied

4. **Accessibility Requirements:**
   - [Source: _bmad-output/implementation-artifacts/2-7-complete-talkback-navigation-for-primary-flow.md]
   - AccessibilityAnnouncementHelper usage pattern
   - ContentDescription format: "[action], [type]"
   - Focus order and TalkBack gesture testing

5. **Haptic Feedback Integration:**
   - [Source: _bmad-output/implementation-artifacts/2-6-haptic-feedback-for-recognition-events.md]
   - Samsung device amplitude values (LIGHT=127, MEDIUM=191, STRONG=255)
   - VibrationEffect.createOneShot() API usage
   - Apply medium intensity (100ms) on voice button press

6. **Android SpeechRecognizer API:**
   - [Source: Android Developer Documentation - android.speech.SpeechRecognizer]
   - RecognizerIntent configuration options
   - RecognitionListener callback interface
   - Error code handling (ERROR_NO_MATCH, ERROR_SPEECH_TIMEOUT, etc.)

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5

### Debug Log References

_No critical debugging sessions required_

### Completion Notes List

**Implementation Summary:**

Successfully implemented Android Speech Recognizer integration with microphone permission management, voice button UI, and comprehensive accessibility support.

**Key Implementation Decisions:**

1. **VoiceRecognitionManager Architecture:**
   - Singleton pattern with Hilt @Inject for dependency injection
   - Wraps Android SpeechRecognizer API with clean StateFlow-based state management
   - Configures on-device recognition (API 33+) with EXTRA_PREFER_OFFLINE for privacy
   - 5-second silence timeout configured via EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS
   - Lowercase transcription conversion for command matching (Story 3.2 integration)

2. **Permission Management:**
   - Extended existing PermissionManager pattern from Story 1.5
   - Added isMicrophonePermissionGranted(), requestMicrophonePermission(), shouldShowMicrophoneRationale()
   - Rationale dialog follows camera permission pattern with Material Design 3 styling
   - TalkBack announcements for grant/deny events via strings.xml resources

3. **Voice Button UI:**
   - Material Design 3 FloatingActionButton (56×56 dp) positioned top-right
   - Microphone icon from Material Symbols (ic_mic.xml)
   - Three button states: Idle, Listening (pulsing animation), Disabled (permission denied)
   - Pulsing animation: ScaleX/Y 1.0→1.1→1.0 + Alpha 1.0→0.7→1.0 (600ms loop)
   - contentDescription changes with state for TalkBack accessibility

4. **State Management:**
   - VoiceRecognitionState sealed class: Idle, Listening, Processing, Error
   - VoiceRecognitionViewModel exposes StateFlow for reactive UI updates
   - ViewModel handles permission state, TTS coordination, haptic feedback
   - Used TTSManager.announce() for state announcements (not AccessibilityAnnouncementHelper which requires View)
   - Haptic feedback using HapticPattern.RecognitionStart on button press

5. **TTS Conflict Avoidance:**
   - TTSManager.stop() called before startListening() to prevent self-recognition
   - Critical learning from Story 2.4 applied: stop TTS before audio input operations

6. **Error Handling:**
   - Comprehensive error code mapping to user-facing messages
   - ERROR_NO_MATCH: "Didn't catch that. Please try again."
   - ERROR_SPEECH_TIMEOUT: "Voice command timed out"
   - ERROR_AUDIO: "Microphone error. Please check device settings."
   - ERROR_NETWORK: "Network unavailable. Voice recognition requires internet."
   - Pulsing animation stops on error with button reset to idle state

7. **Testing Strategy:**
   - Unit tests (VoiceRecognitionManagerTest.kt): State changes, error handling, configuration validation
   - Integration tests (VoiceButtonAccessibilityTest.kt): Touch target size, contentDescription, TalkBack navigation
   - Manual device testing (Task 11) documented for acoustic environment validation
   - Test framework leverages existing BaseAccessibilityTest patterns from Story 1.5

**Integration with Existing Systems:**

- ✅ Hilt DI: VoiceRecognitionManager @Singleton, VoiceRecognitionViewModel @HiltViewModel
- ✅ PermissionManager: Extended with microphone permission methods
- ✅ TTSManager: Used for state announcements, stopped before listening
- ✅ HapticFeedbackManager: RecognitionStart pattern for button press feedback
- ✅ Material Design 3: FAB styling consistent with recognition button

**Story 3.2 Integration Point:**

VoiceRecognitionManager.setOnRecognizedTextCallback() provides lowercase transcription to command processor. Story 3.2 will implement command dispatcher that receives this callback.

**Deviations from Plan:**

- Task 8.1: Used TTSManager.announce() instead of AccessibilityAnnouncementHelper.announce() because AccessibilityAnnouncementHelper requires View parameter which ViewModel doesn't have access to. TTSManager provides same TTS functionality without View dependency.
- Task 11: Device testing across acoustic environments deferred to manual testing phase as it requires physical device in various real-world environments (quiet room, outdoor, transit, etc.)

**Performance Notes:**

- SpeechRecognizer initialization is lazy (created on first startListening() call)
- StateFlow observers use lifecycleScope in MainActivity for proper lifecycle management
- Pulsing animation uses ObjectAnimator for efficient rendering
- Haptic feedback is suspend function, launched in viewModelScope

**Accessibility Validation:**

- Voice button meets 56×56 dp minimum touch target (WCAG AAA)
- contentDescription follows established pattern: "[action], [type]"
- Focus order verified: Toolbar → Recognition FAB → Voice FAB → Fragment content
- TalkBack state announcements tested: "Listening for command", error messages
- High-contrast mode support via Material Design 3 theme system

### File List

**Files Modified:**
- `app/src/main/AndroidManifest.xml` - Added RECORD_AUDIO permission
- `app/src/main/java/com/visionfocus/permissions/manager/PermissionManager.kt` - Added microphone permission methods
- `app/src/main/res/values/strings.xml` - Added microphone permission and voice command strings
- `app/src/main/res/layout/activity_main.xml` - Added voice FAB (56×56 dp)
- `app/src/main/java/com/visionfocus/MainActivity.kt` - Integrated voice button, permission flow, pulsing animation

**Files Created:**
- `app/src/main/java/com/visionfocus/voice/recognizer/VoiceRecognitionState.kt` - State sealed class
- `app/src/main/java/com/visionfocus/voice/recognizer/VoiceRecognitionManager.kt` - SpeechRecognizer wrapper @Singleton
- `app/src/main/java/com/visionfocus/voice/ui/VoiceRecognitionViewModel.kt` - State management @HiltViewModel
- `app/src/main/res/drawable/ic_mic.xml` - Microphone icon vector drawable
- `app/src/androidTest/java/com/visionfocus/voice/VoiceRecognitionManagerTest.kt` - Unit tests
- `app/src/androidTest/java/com/visionfocus/voice/VoiceButtonAccessibilityTest.kt` - Integration tests

**Files Deleted:**
_None_
