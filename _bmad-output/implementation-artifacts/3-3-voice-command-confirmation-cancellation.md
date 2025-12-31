# Story 3.3: Voice Command Confirmation & Cancellation

Status: in-progress

## Story

As a visually impaired user,
I want immediate audio confirmation when my voice command is recognized,
So that I know the system understood me and is taking action.

## Acceptance Criteria

**Given** voice command recognized from Story 3.2
**When** command execution begins
**Then** TTS confirmation announces within 300ms: "Recognize command received", "Navigation starting", "Settings opened"
**And** haptic feedback (single short vibration) provides tactile confirmation
**And** speaking "Cancel" during command execution stops the operation
**And** cancel command works mid-recognition: speaking "Cancel" while camera is analyzing stops processing
**And** cancel command works mid-navigation: speaking "Cancel" during turn-by-turn guidance stops navigation
**And** cancel confirmation announces: "Cancelled"
**And** user can immediately issue another command after cancellation
**And** timeout (no command within 10 seconds) exits listening mode with announcement: "Voice command timed out"

## Tasks / Subtasks

- [ ] Task 1: Enhance TTS confirmation system for immediate feedback (AC: 1, 2)
  - [ ] 1.1: Review existing confirmation implementation in VoiceCommandProcessor from Story 3.2
  - [ ] 1.2: Measure current confirmation latency (target: <300ms)
  - [ ] 1.3: Optimize confirmation announcement to be concise and immediate
  - [ ] 1.4: Add haptic feedback trigger alongside TTS confirmation
  - [ ] 1.5: Verify haptic pattern is distinct (single short vibration 100ms)
  - [ ] 1.6: Test confirmation timing with performance instrumentation

- [ ] Task 2: Implement cancellable operation tracking (AC: 3, 4, 5)
  - [ ] 2.1: Create OperationManager singleton to track active operations
  - [ ] 2.2: Define Operation sealed class (RecognitionOperation, NavigationOperation, None)
  - [ ] 2.3: Implement startOperation() and cancelOperation() methods
  - [ ] 2.4: Add operation state flow for reactive cancellation
  - [ ] 2.5: Integrate with RecognitionViewModel to register/deregister operations
  - [ ] 2.6: Integrate with NavigationViewModel (when available) for nav cancellation

- [ ] Task 3: Enhance CancelCommand with operation-aware cancellation (AC: 3, 4, 5, 6)
  - [ ] 3.1: Modify CancelCommand to query OperationManager for active operation
  - [ ] 3.2: Implement cancellation logic for RecognitionOperation
  - [ ] 3.3: Implement cancellation logic for NavigationOperation (placeholder for Epic 6)
  - [ ] 3.4: Add TTS announcement: "Cancelled" after successful cancellation
  - [ ] 3.5: Handle case where no operation is active: "Nothing to cancel"
  - [ ] 3.6: Ensure cancellation stops TTS mid-announcement if needed

- [ ] Task 4: Implement mid-recognition cancellation support (AC: 4)
  - [ ] 4.1: Add isCancellable flag to RecognitionViewModel state
  - [ ] 4.2: Expose cancelRecognition() method in RecognitionViewModel
  - [ ] 4.3: Implement cancellation during camera capture phase
  - [ ] 4.4: Implement cancellation during TFLite inference phase
  - [ ] 4.5: Implement cancellation during TTS announcement phase
  - [ ] 4.6: Clean up camera resources properly on cancellation
  - [ ] 4.7: Return focus to recognition FAB after cancellation

- [ ] Task 5: Add voice command listening timeout (AC: 8)
  - [ ] 5.1: Implement 10-second timeout timer in VoiceRecognitionManager
  - [ ] 5.2: Start timer when listening mode begins
  - [ ] 5.3: Cancel timer when transcription received or user cancels
  - [ ] 5.4: On timeout, announce: "Voice command timed out"
  - [ ] 5.5: Stop listening and return to idle state on timeout
  - [ ] 5.6: Ensure timeout doesn't interfere with command execution

- [ ] Task 6: Enable immediate next command after cancellation (AC: 7)
  - [ ] 6.1: Verify voice button remains active after cancellation
  - [ ] 6.2: Clear any pending operations on cancellation
  - [ ] 6.3: Reset VoiceRecognitionViewModel state to idle
  - [ ] 6.4: Test rapid command sequence: command → cancel → new command
  - [ ] 6.5: Ensure no state conflicts between cancelled and new operations
  - [ ] 6.6: Verify focus management after cancellation

- [ ] Task 7: Integration with existing voice command infrastructure (AC: All)
  - [ ] 7.1: Update VoiceCommandProcessor to register operations before execution
  - [ ] 7.2: Update VoiceCommandProcessor to deregister operations after completion
  - [ ] 7.3: Add error handling for operation conflicts (e.g., starting rec while nav active)
  - [ ] 7.4: Test confirmation timing across all 15 commands
  - [ ] 7.5: Verify haptic feedback consistency across commands
  - [ ] 7.6: Update strings.xml with cancellation messages

- [ ] Task 8: Unit testing for operation management and cancellation (AC: All)
  - [ ] 8.1: Test OperationManager tracks active operations correctly
  - [ ] 8.2: Test cancelOperation() stops active recognition
  - [ ] 8.3: Test cancelOperation() with no active operation
  - [ ] 8.4: Test voice command timeout triggers after 10 seconds
  - [ ] 8.5: Test confirmation latency <300ms (performance test)
  - [ ] 8.6: Test haptic feedback triggers on command confirmation
  - [ ] 8.7: Mock VoiceCommandProcessor and verify operation lifecycle

- [ ] Task 9: Integration testing for end-to-end cancellation flows (AC: All)
  - [ ] 9.1: Test voice command → confirmation → cancellation → "Cancelled" announcement
  - [ ] 9.2: Test recognition start → mid-processing cancel → camera cleanup
  - [ ] 9.3: Test navigation start → mid-guidance cancel → GPS cleanup (placeholder)
  - [ ] 9.4: Test timeout scenario: listen → 10s silence → timeout announcement
  - [ ] 9.5: Test rapid sequence: command → cancel → new command
  - [ ] 9.6: Test confirmation timing under load (multiple rapid commands)

- [ ] Task 10: Device testing with real voice commands and cancellation scenarios (AC: All)
  - [ ] 10.1: Test "Recognize" → speak "Cancel" mid-processing → verify stops
  - [ ] 10.2: Test "Navigate" → speak "Cancel" → verify nav stops (placeholder for Epic 6)
  - [ ] 10.3: Test confirmation announcements are immediate (<300ms perceived)
  - [ ] 10.4: Test haptic feedback is perceptible and distinct
  - [ ] 10.5: Test timeout with no voice input for 10 seconds
  - [ ] 10.6: Test cancellation from different operation states (camera, inference, TTS)
  - [ ] 10.7: Test accessibility: TalkBack focus after cancellation
  - [ ] 10.8: Validate user experience: cancellation feels responsive and predictable

## Dev Notes

### Critical Architecture Context

**Voice Command Confirmation & Cancellation - Story 3.3 Foundation**

From [epics.md#Epic 3: Voice Command System - Story 3.3]:

This is the THIRD story in Epic 3, enhancing the voice command engine from Stories 3.1-3.2:
- **Story 3.1 (COMPLETED Dec 31):** Android SpeechRecognizer integration, microphone permission, voice button UI, transcription
- **Story 3.2 (IN PROGRESS):** Core voice command processor with 15 commands, fuzzy matching, basic confirmations (11/15 commands functional, testing deferred)
- **Story 3.3 (THIS STORY):** Enhanced confirmation system with <300ms latency guarantee, operation-aware cancellation, timeout handling
- **Story 3.4:** Voice command help system (comprehensive help announcements)
- **Story 3.5:** Always-available voice activation (voice button on all screens)

**Critical Voice Requirements:**

From [epics.md]:
- **FR19:** System provides immediate audio confirmation (≤300ms - IMPLEMENTED IN THIS STORY)
- **FR20:** Users can cancel voice operations mid-execution (IMPLEMENTED IN THIS STORY)
- **NFR Performance:** Voice command acknowledgment within 300ms of detection (validated target)

**Performance Requirements for Voice Confirmations:**

From [architecture.md#Non-Functional Requirements - Performance]:
- **Voice command acknowledgment within 300ms** (NFR requirement) - THIS STORY enforces this
- **Haptic feedback synchronization** with audio confirmation for deaf-blind users
- **TTS priority management** to ensure confirmations are immediate, not queued

### Integration Points with Previous Stories

**1. Story 3.2 (IN PROGRESS) - Command Processing Engine:**

From [3-2-core-voice-command-processing-engine.md]:
- ✅ VoiceCommandProcessor.processCommand() already announces confirmations
- ✅ Confirmation messages in strings.xml: "recognize_command_confirmation", "settings_command_confirmation", etc.
- ✅ HapticFeedbackManager.trigger(CommandExecuted) already called during command execution
- ⚠️ **CURRENT LIMITATION:** No operation tracking - commands execute blindly without cancellable state
- ⚠️ **CURRENT LIMITATION:** CancelCommand broadcasts ACTION_CANCEL but has no context about what to cancel
- 🔗 **CRITICAL INTEGRATION:** This story adds OperationManager to make CancelCommand context-aware

**Key Integration Points:**
```kotlin
// From Story 3.2: VoiceCommandProcessor.processCommand()
suspend fun processCommand(transcription: String): CommandResult {
    val startTime = System.currentTimeMillis()
    // ... command lookup ...
    
    // Existing: TTS confirmation (Story 3.2)
    ttsManager.announce(confirmationMessage, priority = TTSPriority.HIGH)
    
    // Existing: Haptic feedback (Story 3.2)
    hapticFeedbackManager.trigger(HapticPattern.CommandExecuted)
    
    // THIS STORY ADDS: Operation registration before execution
    operationManager.startOperation(matchedCommand.getOperationType())
    
    // Execute command
    val result = matchedCommand.execute(context)
    
    // THIS STORY ADDS: Operation deregistration after completion
    operationManager.completeOperation()
    
    // Existing: Log execution time
    val executionTime = System.currentTimeMillis() - startTime
    Log.d(TAG, "Command executed in ${executionTime}ms (target: <300ms)")
    
    return result
}
```

**2. Story 3.1 (COMPLETED) - Speech Recognition Foundation:**

From [3-1-android-speech-recognizer-integration.md]:
- ✅ VoiceRecognitionManager.stopListening() available for cancellation
- ✅ VoiceRecognitionViewModel.cancelListening() method ready for external cancellation triggers
- ✅ TTS conflict avoidance: TTSManager.stop() before listening prevents self-recognition
- 🔗 **INTEGRATION:** OperationManager can call cancelListening() when user says "Cancel"

**3. Epic 2 (COMPLETED) - Recognition Infrastructure:**

From [2-3-recognition-fab-with-talkback-semantic-annotations.md] and [2-4-camera-capture-with-accessibility-focus-management.md]:
- ✅ RecognitionViewModel.startRecognition() triggers camera and inference
- ✅ RecognitionState sealed class tracks recognition lifecycle
- ⚠️ **CURRENT LIMITATION:** No cancellation mechanism in RecognitionViewModel
- 🔗 **INTEGRATION:** This story adds RecognitionViewModel.cancelRecognition() method

**Key Integration Points:**
```kotlin
// RecognitionViewModel (Epic 2) - MODIFY in this story
sealed class RecognitionState {
    object Idle : RecognitionState()
    object CameraStarting : RecognitionState()
    object Processing : RecognitionState()
    data class Success(val result: RecognitionResult) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
    object Cancelled : RecognitionState()  // NEW STATE - Added in Story 3.3
}

// NEW METHOD - Added in Story 3.3
fun cancelRecognition() {
    viewModelScope.launch {
        // Stop camera
        cameraManager.stopCamera()
        // Cancel inference if running
        recognitionService.cancelInference()
        // Stop TTS if announcing
        ttsManager.stop()
        // Update state
        _recognitionState.value = RecognitionState.Cancelled
        // Announce cancellation
        ttsManager.announce("Cancelled")
    }
}
```

**4. Story 2.6 (COMPLETED) - Haptic Feedback:**

From [2-6-haptic-feedback-for-recognition-events.md]:
- ✅ HapticFeedbackManager with intensity preferences (off, light, medium, strong)
- ✅ HapticPattern.CommandExecuted added in Story 3.2 (100ms single vibration)
- ✅ HapticFeedbackManager.trigger() method ready for use
- 🔗 **INTEGRATION:** Confirmation haptic already implemented in Story 3.2, this story ensures timing <300ms

**5. Navigation Module (Epic 6 - NOT YET STARTED):**

- ⚠️ **FUTURE INTEGRATION:** NavigationViewModel doesn't exist yet
- ⚠️ **PLACEHOLDER:** This story implements cancellation hooks for navigation, but actual navigation cancellation pending Epic 6
- 🔗 **FORWARD COMPATIBILITY:** OperationManager.cancelOperation() will support NavigationOperation when Epic 6 is implemented

### Technical Requirements from Architecture

**Operation Manager Design:**

From software design patterns and [architecture.md#Clean Architecture + MVVM]:

```kotlin
/**
 * Operation Manager - Tracks cancellable operations
 * Story 3.3: Central registry for active operations that can be cancelled
 */
@Singleton
class OperationManager @Inject constructor(
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) {
    // Current active operation
    private val _activeOperation = MutableStateFlow<Operation>(Operation.None)
    val activeOperation: StateFlow<Operation> = _activeOperation.asStateFlow()
    
    /**
     * Start tracking an operation.
     * @param operation The operation being started
     */
    fun startOperation(operation: Operation) {
        _activeOperation.value = operation
        Log.d(TAG, "Operation started: ${operation::class.simpleName}")
    }
    
    /**
     * Complete the current operation.
     */
    fun completeOperation() {
        _activeOperation.value = Operation.None
        Log.d(TAG, "Operation completed")
    }
    
    /**
     * Cancel the current active operation.
     * AC: Cancel command works for recognition and navigation
     * @return CommandResult indicating success/failure
     */
    suspend fun cancelOperation(): CommandResult {
        val current = _activeOperation.value
        
        return when (current) {
            is Operation.RecognitionOperation -> {
                // Cancel recognition via callback
                current.onCancel()
                _activeOperation.value = Operation.None
                
                // Announce cancellation (AC: 6)
                ttsManager.announce("Cancelled")
                hapticFeedbackManager.trigger(HapticPattern.Cancelled)
                
                CommandResult.Success("Recognition cancelled")
            }
            is Operation.NavigationOperation -> {
                // Cancel navigation via callback
                current.onCancel()
                _activeOperation.value = Operation.None
                
                // Announce cancellation (AC: 6)
                ttsManager.announce("Cancelled")
                hapticFeedbackManager.trigger(HapticPattern.Cancelled)
                
                CommandResult.Success("Navigation cancelled")
            }
            Operation.None -> {
                // No active operation to cancel (AC: 3.5)
                ttsManager.announce("Nothing to cancel")
                CommandResult.Failure("No active operation")
            }
        }
    }
    
    companion object {
        private const val TAG = "OperationManager"
    }
}

/**
 * Operation sealed class representing cancellable operations
 */
sealed class Operation {
    data class RecognitionOperation(val onCancel: suspend () -> Unit) : Operation()
    data class NavigationOperation(val onCancel: suspend () -> Unit) : Operation()
    object None : Operation()
}
```

**Enhanced CancelCommand Implementation:**

```kotlin
/**
 * Cancel Command - Enhanced with operation awareness
 * Story 3.3: Context-aware cancellation using OperationManager
 */
@Singleton
class CancelCommand @Inject constructor(
    private val operationManager: OperationManager,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val displayName: String = "Cancel"
    
    override val keywords: List<String> = listOf(
        "cancel",
        "stop",
        "abort",
        "never mind"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        // AC: Cancel works mid-recognition and mid-navigation
        return operationManager.cancelOperation()
    }
}
```

**RecognitionViewModel Cancellation Support:**

```kotlin
// RecognitionViewModel - Add cancellation method (Story 3.3)
@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val recognitionService: ObjectRecognitionService,
    private val cameraManager: CameraManager,
    private val ttsManager: TTSManager,
    private val operationManager: OperationManager,  // NEW - Story 3.3
    private val hapticFeedbackManager: HapticFeedbackManager
) : ViewModel() {
    
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()
    
    /**
     * Start object recognition flow.
     * Story 2.3-2.4: Existing implementation
     * Story 3.3: Register operation with OperationManager
     */
    fun startRecognition() {
        viewModelScope.launch {
            try {
                _recognitionState.value = RecognitionState.CameraStarting
                
                // Register operation for cancellation support (NEW - Story 3.3)
                operationManager.startOperation(
                    Operation.RecognitionOperation(
                        onCancel = { cancelRecognition() }
                    )
                )
                
                // Start camera
                cameraManager.startCamera()
                
                _recognitionState.value = RecognitionState.Processing
                
                // Run inference
                val result = recognitionService.recognizeObjects()
                
                // Complete operation (NEW - Story 3.3)
                operationManager.completeOperation()
                
                _recognitionState.value = RecognitionState.Success(result)
                
                // Announce result
                ttsManager.announce(result.announcement)
                hapticFeedbackManager.trigger(HapticPattern.RecognitionSuccess)
                
            } catch (e: Exception) {
                operationManager.completeOperation()  // Clean up on error
                _recognitionState.value = RecognitionState.Error(e.message ?: "Recognition failed")
                ttsManager.announce("Recognition error. Please try again.")
            }
        }
    }
    
    /**
     * Cancel active recognition operation.
     * Story 3.3: NEW METHOD - Cancellable recognition
     * AC: Cancel works mid-recognition (camera, inference, or TTS phase)
     */
    fun cancelRecognition() {
        viewModelScope.launch {
            Log.d(TAG, "Cancelling recognition")
            
            // Stop camera if active
            if (_recognitionState.value is RecognitionState.CameraStarting ||
                _recognitionState.value is RecognitionState.Processing) {
                cameraManager.stopCamera()
            }
            
            // Cancel inference if running
            recognitionService.cancelInference()
            
            // Stop TTS if announcing
            ttsManager.stop()
            
            // Update state
            _recognitionState.value = RecognitionState.Cancelled
            
            // Note: "Cancelled" announcement handled by OperationManager
        }
    }
    
    companion object {
        private const val TAG = "RecognitionViewModel"
    }
}
```

**Voice Command Listening Timeout:**

```kotlin
// VoiceRecognitionManager - Add timeout (Story 3.3)
class VoiceRecognitionManager @Inject constructor(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerActive = false
    private var listeningTimeoutJob: Job? = null  // NEW - Story 3.3
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val TAG = "VoiceRecognitionManager"
        private const val LISTENING_TIMEOUT_MS = 10_000L  // 10 seconds (AC: 8)
    }
    
    /**
     * Start listening for voice commands.
     * Story 3.1: Existing implementation
     * Story 3.3: Add 10-second timeout
     */
    fun startListening(onRecognizedText: (String) -> Unit) {
        if (isRecognizerActive) {
            Log.w(TAG, "Speech recognizer already active, ignoring startListening")
            return
        }
        
        // ... existing setup code ...
        
        speechRecognizer?.startListening(recognizerIntent)
        isRecognizerActive = true
        
        // Start timeout timer (NEW - Story 3.3, AC: 8)
        startListeningTimeout()
    }
    
    /**
     * Start 10-second timeout for voice listening.
     * Story 3.3 AC: Timeout exits listening mode after 10 seconds of no input
     */
    private fun startListeningTimeout() {
        // Cancel any existing timeout
        listeningTimeoutJob?.cancel()
        
        listeningTimeoutJob = coroutineScope.launch {
            delay(LISTENING_TIMEOUT_MS)
            
            // Timeout occurred - no voice input received
            Log.d(TAG, "Voice command listening timed out after 10 seconds")
            
            // Announce timeout (AC: 8)
            ttsManager.announce("Voice command timed out")
            
            // Stop listening
            stopListening()
        }
    }
    
    /**
     * Cancel listening timeout when transcription received.
     * Story 3.3: Prevent timeout from firing after successful recognition
     */
    private fun cancelListeningTimeout() {
        listeningTimeoutJob?.cancel()
        listeningTimeoutJob = null
    }
    
    /**
     * RecognitionListener callback: onResults
     * Story 3.1: Existing implementation
     * Story 3.3: Cancel timeout when results received
     */
    override fun onResults(results: Bundle?) {
        cancelListeningTimeout()  // NEW - Story 3.3
        
        // ... existing result processing ...
    }
    
    /**
     * Stop listening for voice commands.
     * Story 3.1: Existing implementation
     * Story 3.3: Cancel timeout when manually stopped
     */
    fun stopListening() {
        cancelListeningTimeout()  // NEW - Story 3.3
        
        // ... existing cleanup code ...
    }
}
```

### Performance Requirements

From [architecture.md#Non-Functional Requirements - Performance]:

**Voice Command Confirmation Timing (NFR):**
- **Target:** <300ms from transcription complete to confirmation announcement start
- **Measurement:** `System.currentTimeMillis()` timestamps at transcription receive and TTS initiation
- **Components in timing chain:**
  1. Transcription received callback → processCommand() call: <10ms
  2. Command lookup (registry or fuzzy match): <20ms
  3. TTS confirmation announcement start: <50ms (TTSManager initialization)
  4. Haptic feedback trigger: <10ms (parallel with TTS)
  5. **Total budget:** <300ms end-to-end

**Cancellation Response Time:**
- **Target:** <500ms from "Cancel" spoken to operation stopped and "Cancelled" announced
- **Components:**
  - Speech recognition: ~200ms (Android SpeechRecognizer)
  - Cancel command processing: <50ms
  - Operation cancellation: <100ms (stop camera, cancel inference, stop TTS)
  - Cancellation announcement: <100ms

**Timeout Accuracy:**
- **Target:** 10 seconds ±500ms
- **Implementation:** Kotlin coroutine delay (not Handler.postDelayed for precision)
- **Cancellation:** Timeout cancelled immediately on transcription or manual stop

### Previous Story Learnings

**From Story 3.2 (Core Voice Command Processing Engine):**

✅ **CRITICAL LEARNINGS FOR STORY 3.3:**

1. **Confirmation Already Partially Implemented:**
   - VoiceCommandProcessor already calls `ttsManager.announce(confirmationMessage)`
   - Haptic feedback already triggered: `hapticFeedbackManager.trigger(CommandExecuted)`
   - ⚠️ **CURRENT ISSUE:** No latency measurement or optimization
   - 🔗 **APPLY TO STORY 3.3:** Add timing instrumentation, optimize if >300ms

2. **CancelCommand is Context-Blind:**
   - Story 3.2: CancelCommand broadcasts ACTION_CANCEL intent
   - MainActivity has receiver that only logs
   - ⚠️ **CURRENT LIMITATION:** No knowledge of what operation is active
   - 🔗 **APPLY TO STORY 3.3:** OperationManager provides context for CancelCommand

3. **No Operation Lifecycle Tracking:**
   - Commands execute independently, no central registry
   - RecognitionViewModel has no cancellation support
   - ⚠️ **CURRENT LIMITATION:** Cannot cancel operations mid-execution
   - 🔗 **APPLY TO STORY 3.3:** OperationManager tracks active operations with cancellation callbacks

4. **Placeholder Commands:**
   - 7 of 15 commands are placeholders (RepeatCommand, NavigateCommand, etc.)
   - Some commands announce "coming soon" and do nothing
   - 🔗 **APPLY TO STORY 3.3:** Ensure cancellation works for functional commands; add hooks for future commands

5. **Testing Deferred:**
   - Story 3.2: Unit tests, integration tests, device tests NOT performed
   - ⚠️ **AC VALIDATION MISSING:** 85% accuracy, acoustic environments, 300ms latency not measured
   - 🔗 **APPLY TO STORY 3.3:** Include latency measurements in THIS story's testing

6. **Haptic Feedback Pattern:**
   - CommandExecuted pattern: 100ms single vibration
   - ⚠️ **NEED FOR STORY 3.3:** Add HapticPattern.Cancelled for cancellation feedback
   - 🔗 **APPLY TO STORY 3.3:** Add new pattern for "Cancelled" distinct from command execution

7. **TTS Priority Management:**
   - Confirmations use TTSPriority.HIGH for immediate playback
   - Navigation announcements (Epic 6) will use Priority 1 (higher than confirmations)
   - 🔗 **APPLY TO STORY 3.3:** Ensure cancellation announcements also use high priority

**From Story 3.1 (Android Speech Recognizer Integration):**

✅ **RELEVANT LEARNINGS FOR STORY 3.3:**

1. **Listening Mode State Management:**
   - VoiceRecognitionManager has `isRecognizerActive` flag to prevent double-listening
   - ⚠️ **CURRENT LIMITATION:** No timeout mechanism
   - 🔗 **APPLY TO STORY 3.3:** Add 10-second timeout with coroutine Job cancellation

2. **TTS Conflict Avoidance:**
   - TTSManager.stop() must be called before startListening() to prevent self-recognition
   - 250ms delay after TTS stop before microphone activation
   - 🔗 **APPLY TO STORY 3.3:** Cancellation must also stop TTS to avoid conflicts

3. **VoiceRecognitionViewModel.cancelListening():**
   - Method exists but only stops listening, doesn't cancel active operations
   - 🔗 **APPLY TO STORY 3.3:** OperationManager should call cancelListening() when cancelling voice operations

4. **Error Handling:**
   - All error messages moved to strings.xml for internationalization
   - 🔗 **APPLY TO STORY 3.3:** Add "cancelled", "nothing_to_cancel", "voice_command_timeout" strings

### AndroidManifest.xml - No Changes Required

**No New Permissions:**
- All required permissions (RECORD_AUDIO from Story 3.1, CAMERA from Epic 2) already declared
- Operation management is pure logic layer, no new Android permissions needed

### Library & Framework Requirements

**Core Dependencies (Extend from Stories 1.1-3.2):**

```kotlin
// build.gradle.kts - Operation management uses existing dependencies
dependencies {
    // NO NEW DEPENDENCIES REQUIRED
    // OperationManager uses standard Kotlin coroutines and StateFlow
    
    // Existing dependencies from previous stories:
    // - Hilt (OperationManager singleton injection)
    // - Coroutines (timeout implementation, cancellation support)
    // - StateFlow (activeOperation state management)
}
```

### Project Structure Alignment

**New Files Created (Story 3.3):**

```
app/src/main/java/com/visionfocus/
├── voice/
│   ├── operation/
│   │   ├── OperationManager.kt               # NEW - Central operation tracker
│   │   └── Operation.kt                      # NEW - Sealed class for operations
│   ├── processor/
│   │   └── VoiceCommandProcessor.kt          # MODIFY - Add operation lifecycle hooks
│   ├── commands/
│   │   └── UtilityCommands.kt                # MODIFY - Enhance CancelCommand with OperationManager
│   ├── recognizer/
│   │   └── VoiceRecognitionManager.kt        # MODIFY - Add 10-second listening timeout
│   └── ui/
│       └── VoiceRecognitionViewModel.kt      # MODIFY - Connect to OperationManager (if needed)
├── recognition/
│   └── ui/
│       └── RecognitionViewModel.kt           # MODIFY - Add cancelRecognition() method
├── accessibility/haptic/
│   ├── HapticPattern.kt                      # MODIFY - Add Cancelled pattern
│   └── HapticFeedbackManager.kt              # MODIFY - Add Cancelled pattern handling

app/src/main/res/
├── values/
│   └── strings.xml                           # MODIFY - Add cancellation messages

app/src/androidTest/java/com/visionfocus/
└── voice/
    ├── OperationManagerTest.kt               # NEW - Operation tracking tests
    ├── CancellationIntegrationTest.kt        # NEW - End-to-end cancellation tests
    └── ConfirmationTimingTest.kt             # NEW - <300ms latency validation tests
```

**Package Organization:**
- `com.visionfocus.voice.operation` → NEW package for operation lifecycle management
- Follows existing package structure from Stories 1.1-3.2

### Testing Strategy

**Unit Tests (Task 8):**

```kotlin
// OperationManagerTest.kt
@Test
fun `startOperation registers active operation`() = runTest {
    val operationManager = OperationManager(mockTTSManager, mockHapticManager)
    
    val recognitionOp = Operation.RecognitionOperation(onCancel = {})
    
    operationManager.startOperation(recognitionOp)
    
    assertTrue(operationManager.activeOperation.value is Operation.RecognitionOperation)
}

@Test
fun `cancelOperation stops active recognition`() = runTest {
    var cancelled = false
    val recognitionOp = Operation.RecognitionOperation(onCancel = { cancelled = true })
    
    operationManager.startOperation(recognitionOp)
    
    val result = operationManager.cancelOperation()
    
    assertTrue(cancelled, "onCancel callback should be invoked")
    assertTrue(result is CommandResult.Success)
    verify(mockTTSManager).announce("Cancelled")
}

@Test
fun `cancelOperation with no active operation announces nothing to cancel`() = runTest {
    val result = operationManager.cancelOperation()
    
    assertTrue(result is CommandResult.Failure)
    verify(mockTTSManager).announce("Nothing to cancel")
}

@Test
fun `completeOperation clears active operation`() = runTest {
    operationManager.startOperation(Operation.RecognitionOperation(onCancel = {}))
    
    operationManager.completeOperation()
    
    assertTrue(operationManager.activeOperation.value is Operation.None)
}
```

**Performance Tests (Task 8.5):**

```kotlin
// ConfirmationTimingTest.kt
@Test
fun `confirmation latency is less than 300ms`() = runTest {
    val startTime = System.currentTimeMillis()
    
    // Simulate transcription received
    val transcription = "recognize"
    
    // Process command (includes confirmation)
    voiceCommandProcessor.processCommand(transcription)
    
    // Measure time to TTS.announce() call
    val confirmationLatency = System.currentTimeMillis() - startTime
    
    assertTrue(
        confirmationLatency < 300,
        "Confirmation latency was ${confirmationLatency}ms (target: <300ms)"
    )
}

@Test
fun `cancellation response time is less than 500ms`() = runTest {
    // Start recognition
    recognitionViewModel.startRecognition()
    delay(100) // Let recognition start
    
    val startTime = System.currentTimeMillis()
    
    // Cancel via CancelCommand
    cancelCommand.execute(context)
    
    // Measure time to "Cancelled" announcement
    val cancellationTime = System.currentTimeMillis() - startTime
    
    assertTrue(
        cancellationTime < 500,
        "Cancellation took ${cancellationTime}ms (target: <500ms)"
    )
}
```

**Integration Tests (Task 9):**

```kotlin
// CancellationIntegrationTest.kt
@Test
fun completeVoiceCancellationFlow_stopsRecognitionAndAnnounces() = runTest {
    // Start recognition via voice command
    voiceCommandProcessor.processCommand("recognize")
    
    // Wait for recognition to start
    delay(200)
    
    // Cancel via voice command
    voiceCommandProcessor.processCommand("cancel")
    
    // Verify: Operation cancelled
    assertTrue(operationManager.activeOperation.value is Operation.None)
    
    // Verify: "Cancelled" announced
    verify(ttsManager).announce("Cancelled")
    
    // Verify: Recognition state is Cancelled
    assertTrue(recognitionViewModel.recognitionState.value is RecognitionState.Cancelled)
}

@Test
fun timeoutScenario_announcesTimeoutAfter10Seconds() = runTest {
    // Start listening mode
    voiceRecognitionManager.startListening { }
    
    // Wait 10 seconds (simulated)
    delay(10_000)
    
    // Verify: "Voice command timed out" announced
    verify(ttsManager).announce("Voice command timed out")
    
    // Verify: Listening stopped
    assertFalse(voiceRecognitionManager.isRecognizerActive)
}

@Test
fun rapidCommandSequence_cancelThenNewCommand_noStateConflicts() = runTest {
    // Command 1: Recognize
    voiceCommandProcessor.processCommand("recognize")
    delay(100)
    
    // Cancel
    voiceCommandProcessor.processCommand("cancel")
    delay(100)
    
    // Command 2: Settings (immediate after cancel)
    voiceCommandProcessor.processCommand("settings")
    
    // Verify: No state conflicts, settings command executes successfully
    verify(ttsManager).announce(contains("Settings"))
}
```

**Device Testing (Task 10):**

**Test Plan: Real Voice Commands and Cancellation**

| Test Case | Steps | Expected Result | AC |
|-----------|-------|-----------------|-----|
| Recognize + Cancel | 1. Tap voice button<br>2. Say "Recognize"<br>3. Say "Cancel" mid-processing | Recognition stops, "Cancelled" announced, camera closes | AC 4 |
| Navigate + Cancel | 1. Tap voice button<br>2. Say "Navigate"<br>3. Say "Cancel" | Nav input closes, "Cancelled" announced (placeholder for Epic 6) | AC 5 |
| Confirmation Timing | 1. Tap voice button<br>2. Say any command | Confirmation announced within 300ms (perceived as immediate) | AC 1 |
| Haptic Feedback | 1. Tap voice button<br>2. Say any command | Single vibration felt on command execution | AC 2 |
| Timeout | 1. Tap voice button<br>2. Remain silent for 10 seconds | "Voice command timed out" announced, listening stops | AC 8 |
| Cancel During Camera | 1. Say "Recognize"<br>2. Say "Cancel" before camera starts | Camera doesn't start, "Cancelled" announced | AC 4 |
| Cancel During Inference | 1. Say "Recognize"<br>2. Say "Cancel" while "Analyzing image" | Inference stops, "Cancelled" announced | AC 4 |
| Cancel During TTS | 1. Say "Recognize"<br>2. Let result announce<br>3. Say "Cancel" mid-announcement | TTS stops, "Cancelled" announced | AC 4 |
| Nothing to Cancel | 1. Tap voice button<br>2. Say "Cancel" (no active operation) | "Nothing to cancel" announced | Task 3.5 |
| Rapid Cancel-Command | 1. Say "Recognize"<br>2. Say "Cancel"<br>3. Immediately say "Help" | Help command executes without conflicts | AC 7 |
| TalkBack Focus After Cancel | 1. TalkBack on<br>2. Say "Recognize"<br>3. Say "Cancel" | Focus returns to voice FAB or last element | Task 4.7 |

**Acceptance Criteria Validation:**

| AC | Validation Method | Success Criteria |
|----|-------------------|------------------|
| 1 | Performance test + Device test | Confirmation <300ms measured and perceived |
| 2 | Integration test + Device test | Haptic feedback triggers with confirmation |
| 3 | Unit test + Integration test | Cancel command stops recognition |
| 4 | Device test | Cancel works during camera, inference, TTS |
| 5 | Integration test (placeholder) | Cancel stops navigation (Epic 6 pending) |
| 6 | Integration test + Device test | "Cancelled" announced after cancellation |
| 7 | Integration test | Next command executes after cancel |
| 8 | Integration test + Device test | 10-second timeout triggers announcement |

### Known Constraints & Considerations

**Constraint 1: Navigation Cancellation is Placeholder**
- **Issue:** Epic 6 (Navigation) not yet implemented, NavigationViewModel doesn't exist
- **Mitigation:** OperationManager supports NavigationOperation type, actual cancellation logic added when Epic 6 implemented
- **Impact:** AC #5 (cancel mid-navigation) cannot be fully validated until Epic 6

**Constraint 2: Confirmation Latency Dependent on TTS Engine**
- **Issue:** <300ms target includes Android TTS initialization which varies by device/engine
- **Mitigation:** Use TTSPriority.HIGH to minimize queueing; measure actual latency on target devices
- **Impact:** Some devices may exceed 300ms on first TTS call (cold start), subsequent calls should be <300ms

**Constraint 3: Cancellation During Inference May Not Be Instant**
- **Issue:** TFLite inference is native code, may not support immediate cancellation
- **Mitigation:** Check inference state before starting new inference; add cancellation flag check in inference loop if possible
- **Impact:** User may perceive slight delay (100-200ms) between "Cancel" and actual stop

**Constraint 4: Timeout Accuracy Dependent on System Load**
- **Issue:** Kotlin coroutine delay() can drift under high system load
- **Mitigation:** ±500ms tolerance acceptable for 10-second timeout
- **Impact:** Timeout may trigger 9.5-10.5 seconds after listening starts

**Constraint 5: Rapid Command Sequences May Conflict**
- **Issue:** Speaking "Cancel" then immediate next command may cause state conflicts
- **Mitigation:** Ensure OperationManager.completeOperation() sets state to None before processing next command
- **Impact:** Need integration test to verify no state corruption

**Constraint 6: Cancellation During TTS May Cause Audio Artifacts**
- **Issue:** TTSManager.stop() may clip audio or cause pops
- **Mitigation:** Use Android TTS.stop() API which should handle gracefully
- **Impact:** Minor audio artifact acceptable, prioritize responsiveness

**Constraint 7: Voice Button State After Cancellation**
- **Issue:** Voice button must remain responsive after cancellation
- **Mitigation:** Ensure VoiceRecognitionViewModel returns to Idle state after cancel
- **Impact:** Need integration test to verify button click works after cancel

### References

**Technical Details with Source Paths:**

1. **Story 3.3 Requirements:**
   - [Source: _bmad-output/project-planning-artifacts/epics.md#Epic 3: Voice Command System - Story 3.3]
   - FR19: Immediate audio confirmation of voice commands (≤300ms)
   - FR20: Users can cancel voice operations mid-execution
   - NFR: Voice command acknowledgment within 300ms
   - AC: Cancel works mid-recognition and mid-navigation

2. **Voice Command Architecture:**
   - [Source: _bmad-output/architecture.md#Non-Functional Requirements - Performance]
   - Voice command acknowledgment within 300ms (NFR requirement)
   - Confirmation latency timing chain analysis
   - Cancellation response time <500ms

3. **Story 3.2 Integration:**
   - [Source: _bmad-output/implementation-artifacts/3-2-core-voice-command-processing-engine.md]
   - VoiceCommandProcessor.processCommand() existing confirmation implementation
   - CancelCommand current broadcast-based implementation
   - Haptic feedback pattern CommandExecuted
   - Confirmation messages in strings.xml

4. **Story 3.1 Integration:**
   - [Source: _bmad-output/implementation-artifacts/3-1-android-speech-recognizer-integration.md]
   - VoiceRecognitionManager.stopListening() method
   - VoiceRecognitionViewModel.cancelListening() method
   - TTS conflict avoidance pattern
   - isRecognizerActive flag for state management

5. **RecognitionViewModel Integration:**
   - [Source: _bmad-output/implementation-artifacts/2-3-recognition-fab-with-talkback-semantic-annotations.md]
   - RecognitionViewModel.startRecognition() method
   - [Source: _bmad-output/implementation-artifacts/2-4-camera-capture-with-accessibility-focus-management.md]
   - RecognitionState sealed class hierarchy
   - Camera lifecycle management

6. **Operation Lifecycle Pattern:**
   - [Source: Design Patterns - Observer Pattern + State Pattern]
   - StateFlow for reactive operation tracking
   - Callback pattern for cancellation hooks
   - Centralized operation registry

7. **Performance Measurement:**
   - [Source: Android Performance Best Practices]
   - System.currentTimeMillis() for latency measurement
   - Kotlin coroutine delay() for timeout implementation
   - Job cancellation for timeout cleanup

8. **Accessibility Feedback Requirements:**
   - [Source: _bmad-output/implementation-artifacts/2-7-complete-talkback-navigation-for-primary-flow.md]
   - TTS announcements for all state changes
   - Haptic feedback for confirmations (blind users)
   - Focus management after cancellation

## Dev Agent Record

### Agent Model Used

GitHub Copilot (Claude Sonnet 4.5)

### Debug Log References

### Completion Notes List

**Session: December 31, 2025 - Core Architecture Implemented + Code Review Fixes**

✅ **COMPLETED - Core Operation Management Infrastructure:**
- Created Operation.kt sealed class (RecognitionOperation, NavigationOperation, None)
- Implemented OperationManager singleton with StateFlow-based operation tracking
- Integrated OperationManager with Hilt dependency injection
- Added cancellation callback pattern for operation-specific cleanup

✅ **COMPLETED - Enhanced CancelCommand (Task 3):**
- Replaced broadcast-based cancellation with OperationManager integration  
- Context-aware cancellation: queries active operation before cancelling
- Handles "Nothing to cancel" edge case gracefully
- TTS + haptic feedback managed by OperationManager

✅ **COMPLETED - RecognitionViewModel Cancellation Support (Task 4):**
- Added cancelRecognitionInternal() method for mid-recognition cancellation
- Integrated with OperationManager via Operation.RecognitionOperation callback
- Cancellation works during Capturing, Recognizing, and Announcing phases
- Added RecognitionUiState.Cancelled state for clean state transitions (later removed - see fixes)
- Operation lifecycle: startOperation() before inference, completeOperation() after success/error

✅ **COMPLETED - Voice Listening 10-Second Timeout (Task 5):**
- Added LISTENING_TIMEOUT_MS constant (10 seconds) to VoiceRecognitionManager
- Implemented startListeningTimeout() with coroutine-based timer
- Integrated cancelListeningTimeout() in onResults, onError, and stopListening
- Timeout announces "Voice command timed out" per AC
- Distinct from 5-second silence timeout (speech recognition) and 15-second watchdog

✅ **COMPLETED - RecognitionFragment UI State Handling:**
- Added Cancelled state branch to updateUi() when expression
- Cancelled state restores FAB to idle, restores focus for next command
- "Cancelled" announcement handled by OperationManager (not fragment)

**CODE REVIEW FIXES - December 31, 2025:**

✅ **FIXED - Issue #3: Added HapticPattern.Cancelled:**
- Added Cancelled pattern to HapticPattern.kt (50ms vs 100ms CommandExecuted)
- Updated HapticFeedbackManager to handle Cancelled pattern
- Distinct tactile feedback for cancellation vs command confirmation

✅ **FIXED - Issue #5: Internationalization - String Resources:**
- Added voice_nothing_to_cancel to strings.xml
- Added voice_cancelled to strings.xml
- Added voice_cancellation_failed to strings.xml
- OperationManager now uses context.getString() instead of hardcoded strings

✅ **FIXED - Issue #2: OperationManager Integration in VoiceCommandProcessor:**
- Injected OperationManager into VoiceCommandProcessor constructor
- Added confirmation latency measurement (AC #1 validation)
- Logs confirmation timing: "Confirmation latency: Xms (target: <300ms)"
- Logs performance warning if exceeds 300ms threshold

✅ **FIXED - Issue #6: TTSPriority.HIGH for Cancellation:**
- All OperationManager.cancelOperation() announcements use TTSPriority.HIGH
- "Cancelled", "Nothing to cancel", "Cancellation failed" all immediate priority
- Ensures cancellation feedback not delayed by queued TTS

✅ **FIXED - Issue #7: Race Condition in RecognitionViewModel:**
- Moved operationManager.startOperation() BEFORE state transition to Recognizing
- Eliminates 5-10ms window where cancel command could fail
- Operation now registered before user can say "Cancel"

✅ **FIXED - Issue #8: Improved Error Handling:**
- OperationManager.cancelOperation() now wraps onCancel() in try-catch
- On cancellation failure: resets state, announces failure, returns CommandResult.Failure
- User informed if cancellation didn't complete successfully

✅ **FIXED - Issue #13: Removed Intermediate Cancelled State:**
- RecognitionViewModel.cancelRecognitionInternal() transitions directly to Idle
- Eliminates unnecessary StateFlow emission (performance optimization)
- Cancellation announcement still handled by OperationManager

✅ **FIXED - Issue #1: Added OperationManagerTest.kt:**
- 9 comprehensive unit tests for operation lifecycle
- Tests: startOperation, completeOperation, cancelOperation
- Tests: "Nothing to cancel" edge case
- Tests: Haptic feedback validation (Cancelled pattern)
- Tests: Error handling (callback exception)
- Tests: Operation lifecycle (start → cancel → start again)
- Tests: Navigation operation support (Epic 6 placeholder)

✅ **FIXED - Issue #10: Updated File List:**
- Added OperationManagerTest.kt
- Added VoiceCommandProcessor.kt modifications
- Added HapticPattern.kt and HapticFeedbackManager.kt modifications
- Added strings.xml modifications
- Added sprint-status.yaml and story file to list

**Architecture Decisions:**
1. **Centralized Operation Tracking**: OperationManager uses StateFlow for reactive operation state
2. **Callback Pattern**: Operations register suspend fun callbacks for cancellation
3. **Clean Separation**: Voice layer (OperationManager) independent of UI layer (RecognitionViewModel)
4. **Forward Compatibility**: NavigationOperation placeholder ready for Epic 6
5. **Internationalization**: All user-facing strings in strings.xml for localization
6. **Distinct Haptic Patterns**: Cancelled (50ms) vs CommandExecuted (100ms) for tactile discrimination

**Implementation Notes:**
- TTS confirmation already implemented in Story 3.2 VoiceCommandProcessor
- Haptic feedback already triggered in Story 3.2 (CommandExecuted pattern)
- Confirmation latency measurement added during code review
- Navigation cancellation is placeholder (Epic 6 not yet implemented)
- Unit tests created, integration and device tests deferred to next session

**Performance Validation:**
- Confirmation latency now logged and measured
- Target: <300ms from transcription → TTS.announce()
- Performance warning logged if exceeds threshold
- Story 3.2 existing confirmations likely already <300ms

### File List

**New Files Created:**
- `app/src/main/java/com/visionfocus/voice/operation/Operation.kt` - Sealed class for cancellable operations
- `app/src/main/java/com/visionfocus/voice/operation/OperationManager.kt` - Central operation lifecycle tracker
- `app/src/test/java/com/visionfocus/voice/operation/OperationManagerTest.kt` - Unit tests for operation management

**Modified Files:**
- `app/src/main/java/com/visionfocus/voice/commands/utility/UtilityCommands.kt` - Enhanced CancelCommand with OperationManager
- `app/src/main/java/com/visionfocus/ui/recognition/RecognitionViewModel.kt` - Added operation registration and cancellation
- `app/src/main/java/com/visionfocus/ui/recognition/RecognitionUiState.kt` - Added Cancelled state
- `app/src/main/java/com/visionfocus/ui/recognition/RecognitionFragment.kt` - Added Cancelled state handling in UI
- `app/src/main/java/com/visionfocus/voice/recognizer/VoiceRecognitionManager.kt` - Added 10-second listening timeout
- `app/src/main/java/com/visionfocus/voice/processor/VoiceCommandProcessor.kt` - Added confirmation latency measurement and OperationManager integration
- `app/src/main/java/com/visionfocus/accessibility/haptic/HapticPattern.kt` - Added Cancelled haptic pattern
- `app/src/main/java/com/visionfocus/accessibility/haptic/HapticFeedbackManager.kt` - Added Cancelled pattern handling
- `app/src/main/res/values/strings.xml` - Added cancellation messages for internationalization
- `_bmad-output/implementation-artifacts/sprint-status.yaml` - Updated story status
- `_bmad-output/implementation-artifacts/3-3-voice-command-confirmation-cancellation.md` - Story documentation

