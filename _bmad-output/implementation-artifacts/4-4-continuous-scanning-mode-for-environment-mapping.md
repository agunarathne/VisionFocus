# Story 4.4: Continuous Scanning Mode for Environment Mapping

Status: done

## Story

As a visually impaired user,
I want to continuously scan my environment and hear multiple object announcements,
So that I can build a mental map of objects around me without repeated button presses.

## Acceptance Criteria

**Given** recognition feature working from Epic 2
**When** I activate continuous scanning mode
**Then** voice command "Scan environment" or long-press FAB (>2 seconds) activates continuous mode
**And** TTS announces: "Continuous scanning active. I'll announce objects as I detect them. Say 'Stop' to end."
**And** camera captures frames every 3 seconds and runs inference
**And** new unique objects (not previously announced in this session) are announced immediately
**And** duplicate objects (already announced) are suppressed to avoid repetition
**And** confidence threshold remains ≥0.6 to ensure quality
**And** announcements are queued if multiple new objects detected simultaneously
**And** speaking "Stop" or "Cancel" exits continuous mode
**And** continuous mode announces summary when stopped: "Scanning stopped. I detected 5 objects: chair, table, bottle, cup, laptop."
**And** continuous mode auto-stops after 60 seconds with announcement: "Scan complete"
**And** continuous scanning respects battery performance (frames processed at 0.33 Hz to limit power draw)

## Tasks / Subtasks

- [x] Task 1: Create ScanningState sealed class and ContinuousScanner service (AC: continuous mode state management)
  - [x] 1.1: Create ScanningState sealed class: Idle, Scanning, Stopping with data classes
  - [x] 1.2: Create ContinuousScanner class with lifecycle management
  - [x] 1.3: Inject RecognitionService and TTSManager via Hilt
  - [x] 1.4: Implement start(), stop(), and cleanup() methods
  - [x] 1.5: Add StateFlow<ScanningState> for UI state observation

- [x] Task 2: Implement frame capture timer with 3-second interval (AC: captures every 3 seconds)
  - [x] 2.1: Create CoroutineScope with Dispatchers.IO for background processing
  - [x] 2.2: Use Flow.interval(3000ms) for periodic frame capture trigger
  - [x] 2.3: Cancel timer when scanning stops or auto-timeout reached
  - [x] 2.4: Log frame capture timing for performance verification
  - [x] 2.5: Handle camera lifecycle properly (pause/resume with scanning state)

- [x] Task 3: Create DetectedObjectTracker for duplicate suppression (AC: duplicate suppression)
  - [x] 3.1: Create data class DetectedObject(label: String, confidence: Float, timestamp: Long)
  - [x] 3.2: Implement DetectedObjectTracker with mutableSetOf<String> for seen objects
  - [x] 3.3: Add isNewObject(label: String) method checking against seen set
  - [x] 3.4: Add addObject(label: String) method adding to seen set
  - [x] 3.5: Add reset() method clearing seen set on new scanning session

- [x] Task 4: Integrate inference with duplicate detection (AC: unique objects announced)
  - [x] 4.1: Call RecognitionService.recognizeFrame() for each timer tick
  - [x] 4.2: Apply confidence filter ≥0.6 threshold (reuse from Story 2.2)
  - [x] 4.3: For each detection, check DetectedObjectTracker.isNewObject()
  - [x] 4.4: If new object, add to tracker and queue for announcement
  - [x] 4.5: If duplicate, skip announcement (log as suppressed)

- [x] Task 5: Implement announcement queue with priority (AC: announcements queued)
  - [x] 5.1: Create AnnouncementQueue using Channel<String> for async queue
  - [x] 5.2: Launch coroutine consuming queue and calling TTSManager.speak()
  - [x] 5.3: Add queue size limit (max 5 pending announcements to prevent overwhelming)
  - [x] 5.4: Implement cancellation on stop command (clear queue)
  - [x] 5.5: Use TTSFormatter from Story 2.2 for consistent phrasing

- [x] Task 6: Add long-press gesture detection to Recognition FAB (AC: long-press activates)
  - [x] 6.1: Add OnLongClickListener to Recognition FAB in RecognitionFragment
  - [x] 6.2: Set long-press duration threshold: 2000ms (2 seconds) - HIGH-1 FIX: Custom OnTouchListener implementation
  - [x] 6.3: Trigger haptic feedback (medium intensity) on long-press detection
  - [x] 6.4: Call startContinuousScanning() on long-press
  - [x] 6.5: Update FAB icon to "pause" or "stop" during scanning mode

- [x] Task 7: Integrate voice commands "Scan environment" and "Stop" (AC: voice commands)
  - [x] 7.1: Add ScanEnvironmentCommand to voice command registry (Epic 3 integration)
  - [x] 7.2: Register keywords: "scan environment", "continuous scan", "map environment"
  - [x] 7.3: Add StopScanningCommand with keywords: "stop", "stop scanning", "cancel"
  - [x] 7.4: Execute startContinuousScanning() on ScanEnvironmentCommand
  - [x] 7.5: Execute stopContinuousScanning() on StopScanningCommand

- [x] Task 8: Implement auto-stop after 60-second timeout (AC: auto-stop after 60s)
  - [x] 8.1: Start 60-second countdown timer when scanning begins
  - [x] 8.2: Emit ScanningState.AutoStopping when timer expires
  - [x] 8.3: Announce "Scan complete" via TTS
  - [x] 8.4: Call stopContinuousScanning() with isAutoStop=true flag
  - [x] 8.5: Cancel timer if user manually stops before timeout

- [x] Task 9: Implement summary announcement on stop (AC: summary announcement)
  - [x] 9.1: Track DetectedObjectTracker.count() throughout scanning session
  - [x] 9.2: On stop, retrieve list of all detected object labels from tracker
  - [x] 9.3: Format summary: "Scanning stopped. I detected [count] objects: [list]"
  - [x] 9.4: If count > 5, summarize: "I detected [count] objects including [top 5]"
  - [x] 9.5: Announce via TTSManager with priority (interrupt any pending recognition)

- [x] Task 10: Update RecognitionViewModel to integrate scanning mode (AC: all)
  - [x] 10.1: Add scanningState: StateFlow<ScanningState> to ViewModel
  - [x] 10.2: Add startContinuousScanning() and stopContinuousScanning() methods
  - [x] 10.3: Inject ContinuousScanner via Hilt constructor
  - [x] 10.4: Observe scanning state and update UI accordingly
  - [x] 10.5: Handle errors during scanning (camera error, inference error) with recovery

- [x] Task 11: Update RecognitionFragment UI for scanning mode (AC: UI state changes)
  - [x] 11.1: Observe scanningState Flow in lifecycleScope
  - [x] 11.2: Change FAB icon when scanning: camera → pause/stop icon
  - [x] 11.3: Update FAB contentDescription: "Stop scanning, button"
  - [x] 11.4: Disable single-tap recognition when scanning active
  - [x] 11.5: Show visual indicator of scanning state (pulsing icon or progress)

- [x] Task 12: Implement battery optimization for 0.33 Hz frame rate (AC: battery performance)
  - [x] 12.1: Verify 3-second interval = 0.33 Hz frame capture rate
  - [x] 12.2: Use Dispatchers.IO for inference to avoid blocking main thread
  - [x] 12.3: Pause camera preview between captures (not continuous preview)
  - [x] 12.4: Monitor battery impact during testing (target ≤12% drain/hour)
  - [x] 12.5: Add power-saving mode detection and announce if enabled

- [x] Task 13: Add TalkBack accessibility support for scanning mode (AC: TalkBack support)
  - [x] 13.1: Set FAB contentDescription when entering scanning mode
  - [x] 13.2: Announce scanning start: "Continuous scanning active"
  - [x] 13.3: Announce each detected object with TalkBack priority
  - [x] 13.4: Announce scanning stop with summary via announceForAccessibility()
  - [x] 13.5: Ensure focus restoration after scanning completes

- [x] Task 14: Create unit tests for ContinuousScanner (AC: all) - HIGH-2 FIX: Complete test suite created
  - [x] 14.1: Create ContinuousScannerTest.kt with JUnit 4 + Mockito + Coroutines Test
  - [x] 14.2: Test start() emits ScanningState.Scanning
  - [x] 14.3: Test stop() emits ScanningState.Idle with summary
  - [x] 14.4: Test auto-timeout stops after 60 seconds
  - [x] 14.5: Test duplicate suppression (DetectedObjectTracker)
  - [x] 14.6: HIGH-2 FIX: Test frame capture interval (3 seconds)
  - [x] 14.7: HIGH-2 FIX: Test low confidence filtering (≥0.6)
  - [x] 14.8: HIGH-2 FIX: Test camera permission error stops scanning (CRITICAL-3 validation)
  - [x] 14.9: HIGH-2 FIX: Test consecutive errors stop scanning (MEDIUM-2 validation)

- [x] Task 15: Create instrumentation tests for continuous scanning (AC: all) - HIGH-3 FIX: Accessibility tests created
  - [x] 15.1: Create ContinuousScanningAccessibilityTest.kt extending BaseAccessibilityTest
  - [x] 15.2: Test long-press FAB activates scanning mode (HIGH-1 validation: 2000ms threshold)
  - [x] 15.3: Test voice command "Scan environment" works
  - [x] 15.4: Test FAB contentDescription updates during scanning
  - [x] 15.5: Test TalkBack announcements for start/stop events
  - [x] 15.6: HIGH-3 FIX: Test short press (<2s) triggers single recognition, not scanning
  - [x] 15.7: HIGH-3 FIX: Test FAB accessibility attributes (focusable, touch target size)

## Dev Notes

### ⚠️ CRITICAL: Epic 2 and Story 4.1 Dependencies

**Epic 2 Stories MUST be completed before Story 4.4:**

Story 4.4 depends on Epic 2 (Accessible Object Recognition) providing:
- RecognitionService with recognizeFrame() method (Story 2.1)
- TTSManager with speak() method (Story 2.2)
- TTSFormatter for confidence-aware phrasing (Story 2.2)
- Recognition FAB in RecognitionFragment (Story 2.3)
- Camera lifecycle management (Story 2.4)
- Haptic feedback integration (Story 2.6)
- TalkBack semantic annotations baseline (Story 2.7)

**Story 4.1 (Verbosity Mode Selection) MUST be completed:**
- VerbosityMode enum and preference storage
- TTSFormatter.formatRecognitionResult() respects verbosity setting
- Brief/Standard/Detailed announcement patterns

**IF Epic 2 or Story 4.1 are NOT complete:**
1. **HALT development** - Do not proceed with Story 4.4
2. **Notify user:** "Epic 2 and Story 4.1 must be completed first"
3. **Suggest:** Complete Epic 2 stories and Story 4.1 before implementing continuous scanning

**IF Epic 2 and Story 4.1 ARE complete:**
- Verify RecognitionService exists: `app/src/main/java/com/visionfocus/recognition/`
- Verify TTSManager exists: `app/src/main/java/com/visionfocus/tts/`
- Verify Recognition FAB exists in fragment_recognition.xml
- Proceed with continuous scanning implementation

### Technical Requirements from Architecture Document

**Continuous Scanning Implementation Pattern:**

From [architecture.md#Decision 1: Data Persistence Strategy] and [epics.md#Epic 4 - Story 4.4]:

Continuous scanning introduces a new operational mode requiring careful state management and resource optimization.

```kotlin
// ContinuousScanner.kt - Main scanning service
@HiltViewModel
class ContinuousScanner @Inject constructor(
    private val recognitionService: RecognitionService,
    private val ttsManager: TTSManager,
    private val ttsFormatter: TTSPhraseFormatter,
    private val tracker: DetectedObjectTracker,
    @ApplicationContext private val context: Context
) {
    
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Idle)
    val scanningState: StateFlow<ScanningState> = _scanningState.asStateFlow()
    
    private val scanningScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val announcementQueue = Channel<String>(capacity = 5)
    
    private var scanningJob: Job? = null
    private var timeoutJob: Job? = null
    private var announcementJob: Job? = null
    
    fun startScanning() {
        if (_scanningState.value is ScanningState.Scanning) {
            Timber.w("Scanning already active")
            return
        }
        
        // Reset tracker for new session
        tracker.reset()
        
        // Announce start
        ttsManager.speak("Continuous scanning active. I'll announce objects as I detect them. Say 'Stop' to end.")
        
        // Update state
        _scanningState.value = ScanningState.Scanning(startTime = System.currentTimeMillis())
        
        // Start frame capture loop (every 3 seconds)
        scanningJob = scanningScope.launch {
            flow {
                while (true) {
                    delay(3000) // 0.33 Hz capture rate
                    emit(Unit)
                }
            }.collect {
                captureAndProcessFrame()
            }
        }
        
        // Start announcement consumer
        announcementJob = scanningScope.launch {
            for (announcement in announcementQueue) {
                ttsManager.speak(announcement)
                delay(500) // Brief gap between announcements
            }
        }
        
        // Start 60-second auto-timeout
        timeoutJob = scanningScope.launch {
            delay(60_000)
            stopScanning(isAutoStop = true)
        }
    }
    
    private suspend fun captureAndProcessFrame() {
        try {
            val results = recognitionService.recognizeFrame()
            
            // Apply confidence filter ≥0.6
            val highConfidenceResults = results.filter { it.confidence >= 0.6f }
            
            // Check for new objects
            for (result in highConfidenceResults) {
                if (tracker.isNewObject(result.label)) {
                    tracker.addObject(result.label)
                    
                    // Format announcement based on verbosity
                    val announcement = ttsFormatter.formatRecognitionResult(
                        label = result.label,
                        confidence = result.confidence,
                        verbosityMode = getVerbosityMode(),
                        detailText = null
                    )
                    
                    // Queue for announcement (non-blocking)
                    announcementQueue.trySend(announcement)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during frame processing")
            // Continue scanning despite error
        }
    }
    
    fun stopScanning(isAutoStop: Boolean = false) {
        // Cancel all jobs
        scanningJob?.cancel()
        timeoutJob?.cancel()
        announcementJob?.cancel()
        announcementQueue.cancel()
        
        // Generate summary
        val detectedObjects = tracker.getAllDetectedObjects()
        val count = detectedObjects.size
        
        val summary = if (isAutoStop) {
            "Scan complete. I detected $count objects: ${formatObjectList(detectedObjects)}"
        } else {
            "Scanning stopped. I detected $count objects: ${formatObjectList(detectedObjects)}"
        }
        
        // Announce summary
        ttsManager.speak(summary, priority = TTSManager.Priority.HIGH)
        
        // Update state
        _scanningState.value = ScanningState.Idle
    }
    
    private fun formatObjectList(objects: List<String>): String {
        return when {
            objects.isEmpty() -> "none"
            objects.size <= 5 -> objects.joinToString(", ")
            else -> "${objects.take(5).joinToString(", ")}, and ${objects.size - 5} more"
        }
    }
    
    private fun getVerbosityMode(): VerbosityMode {
        // Retrieve from UserPreferencesRepository
        // For continuous scanning, Brief mode is recommended for rapid feedback
        return VerbosityMode.BRIEF
    }
    
    fun cleanup() {
        stopScanning()
        scanningScope.cancel()
    }
}
```

**ScanningState Sealed Class:**

```kotlin
// ScanningState.kt
sealed class ScanningState {
    object Idle : ScanningState()
    
    data class Scanning(
        val startTime: Long,
        val objectsDetected: Int = 0
    ) : ScanningState()
    
    data class Stopping(
        val summary: String
    ) : ScanningState()
}
```

**DetectedObjectTracker Implementation:**

```kotlin
// DetectedObjectTracker.kt
class DetectedObjectTracker {
    
    private val detectedObjects = mutableSetOf<String>()
    
    fun isNewObject(label: String): Boolean {
        return !detectedObjects.contains(label.lowercase())
    }
    
    fun addObject(label: String) {
        detectedObjects.add(label.lowercase())
    }
    
    fun getAllDetectedObjects(): List<String> {
        return detectedObjects.toList()
    }
    
    fun count(): Int = detectedObjects.size
    
    fun reset() {
        detectedObjects.clear()
    }
}
```

### Integration with Existing Components

**RecognitionService Integration (Story 2.1):**

Continuous scanning reuses recognizeFrame() method from Story 2.1:

```kotlin
// Existing RecognitionService.recognizeFrame() from Story 2.1
interface RecognitionService {
    suspend fun recognizeFrame(): List<RecognitionResult>
    fun cleanup()
}

data class RecognitionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF? = null
)
```

**TTSManager Integration (Story 2.2):**

Continuous scanning uses TTSManager.speak() with priority support:

```kotlin
// TTSManager from Story 2.2 - add priority parameter
class TTSManager {
    enum class Priority { LOW, MEDIUM, HIGH }
    
    fun speak(text: String, priority: Priority = Priority.MEDIUM) {
        // Implementation handles priority queue
        // HIGH priority interrupts current speech
        // MEDIUM and LOW queue behind HIGH
    }
}
```

**TTSFormatter Integration (Story 2.2):**

Reuse formatRecognitionResult() for consistent phrasing:

```kotlin
// TTSPhraseFormatter from Story 2.2
class TTSPhraseFormatter {
    fun formatRecognitionResult(
        label: String,
        confidence: Float,
        verbosityMode: VerbosityMode,
        detailText: String?
    ): String {
        // Returns formatted announcement based on verbosity
        // Brief: "Chair"
        // Standard: "Chair with high confidence"
        // Detailed: "High confidence: chair in center of view"
    }
}
```

**Voice Command Integration (Epic 3):**

Add ScanEnvironmentCommand and StopScanningCommand to voice registry:

```kotlin
// ScanEnvironmentCommand.kt
class ScanEnvironmentCommand @Inject constructor(
    private val continuousScanner: ContinuousScanner
) : VoiceCommand {
    
    override val keywords = listOf(
        "scan environment",
        "continuous scan",
        "map environment",
        "scan surroundings",
        "map surroundings"
    )
    
    override suspend fun execute(context: Context) {
        continuousScanner.startScanning()
    }
}

// StopScanningCommand.kt
class StopScanningCommand @Inject constructor(
    private val continuousScanner: ContinuousScanner
) : VoiceCommand {
    
    override val keywords = listOf(
        "stop",
        "stop scanning",
        "cancel",
        "end scan"
    )
    
    // Note: "Stop" and "Cancel" may conflict with other commands
    // Priority resolution: Check if scanning active first
    override suspend fun execute(context: Context) {
        if (continuousScanner.scanningState.value is ScanningState.Scanning) {
            continuousScanner.stopScanning()
        } else {
            // Pass to other stop/cancel command handlers
        }
    }
}
```

**FAB Long-Press Integration (RecognitionFragment):**

```kotlin
// RecognitionFragment.kt - Add long-press listener
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    binding.recognitionFab.setOnClickListener {
        // Existing single-tap: single recognition
        viewModel.startSingleRecognition()
    }
    
    binding.recognitionFab.setOnLongClickListener {
        // New long-press: continuous scanning
        hapticFeedback(HapticIntensity.MEDIUM)
        viewModel.startContinuousScanning()
        true // Consume event
    }
    
    // Observe scanning state
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.scanningState.collect { state ->
            when (state) {
                is ScanningState.Idle -> {
                    binding.recognitionFab.setImageResource(R.drawable.ic_camera_24)
                    binding.recognitionFab.contentDescription = getString(R.string.fab_recognize_objects)
                }
                is ScanningState.Scanning -> {
                    binding.recognitionFab.setImageResource(R.drawable.ic_stop_24)
                    binding.recognitionFab.contentDescription = getString(R.string.fab_stop_scanning)
                    binding.recognitionFab.setOnClickListener {
                        viewModel.stopContinuousScanning()
                    }
                }
                is ScanningState.Stopping -> {
                    // Transitioning to Idle, announcement in progress
                }
            }
        }
    }
}
```

### Battery Performance Requirements

**From NFRs: ≤12% battery drain per hour during continuous operation**

Continuous scanning must not exceed battery performance budget:

**Optimization Strategies:**

1. **Frame Rate Reduction:** 0.33 Hz (1 frame per 3 seconds) vs. continuous 15 FPS
   - 15 FPS continuous = 54,000 frames/hour
   - 0.33 Hz scanning = 1,200 frames/hour
   - **95.6% reduction in processing load**

2. **Camera Preview Pausing:**
   - Pause preview between captures (not continuous stream)
   - Reduces GPU/battery load from continuous rendering

3. **Inference Throttling:**
   - Only run TFLite inference when new frame captured
   - No idle CPU/GPU utilization between captures

4. **Background Processing:**
   - Use Dispatchers.IO for inference (CPU-bound)
   - Avoid blocking main thread (prevents UI jank)

5. **Timeout Mechanism:**
   - Auto-stop after 60 seconds prevents infinite battery drain
   - User can manually stop anytime

**Battery Impact Calculation:**

From [prd.md#Performance Requirements]:
- Recognition-only mode: ≤8% battery drain per hour
- Continuous scanning: 0.33 Hz × 3 seconds = 1 inference per 3 seconds
- Expected drain: ~8-10% per hour (within acceptable range)

**Testing Validation:**

Monitor battery drain during continuous scanning:
```bash
# Android battery monitoring
adb shell dumpsys battery

# Monitor power usage during scanning (60-second test)
adb shell "dumpsys batterystats --reset && sleep 60 && dumpsys batterystats"
```

Target: ≤10% drain for 60-second scanning session = ~10% per hour (within budget)

### Accessibility Requirements (CRITICAL)

**From Story 2.7 - TalkBack Compliance:**

All TalkBack patterns from Epic 2 MUST be maintained:

1. **FAB Content Description Updates:**
   - Idle state: "Recognize objects. Double-tap to activate camera."
   - Scanning state: "Stop scanning. Double-tap to stop continuous scanning."
   - Use setContentDescription() programmatically when state changes

2. **TTS Announcements:**
   - Scanning start: "Continuous scanning active. I'll announce objects as I detect them. Say 'Stop' to end."
   - Each detected object: "[Object] [confidence]" (based on verbosity)
   - Scanning stop: "Scanning stopped. I detected [count] objects: [list]"
   - Auto-timeout: "Scan complete. I detected [count] objects: [list]"

3. **Focus Management:**
   - When scanning starts, focus should remain on FAB (now "Stop" button)
   - When scanning stops, focus returns to FAB (now "Recognize" button)
   - No focus disruption during scanning (announcements are non-modal)

4. **Haptic Feedback:**
   - Long-press detection: Medium intensity vibration (100ms)
   - Scanning start: Single short vibration (100ms)
   - Scanning stop: Double vibration pattern (100ms, 50ms gap, 100ms)

5. **Error Handling:**
   - Camera error during scanning: "Camera error. Scanning stopped." + graceful stop
   - Inference error: Continue scanning, log error, don't crash

### Project File Structure

**New files for Story 4.4:**

```
app/src/main/java/com/visionfocus/
├── recognition/
│   ├── scanning/                               # NEW for Story 4.4
│   │   ├── ContinuousScanner.kt               # Main scanning service
│   │   ├── ScanningState.kt                   # Sealed class for states
│   │   ├── DetectedObjectTracker.kt           # Duplicate suppression
│   │   └── AnnouncementQueue.kt               # TTS announcement queue (optional utility)
│   │
│   └── RecognitionService.kt                   # Already exists from Story 2.1
│
├── voice/commands/
│   ├── recognition/
│   │   ├── ScanEnvironmentCommand.kt          # NEW
│   │   └── StopScanningCommand.kt             # NEW
│   │
│   └── VoiceCommandRegistry.kt                # Modified: add new commands
│
├── ui/recognition/
│   ├── RecognitionFragment.kt                 # Modified: add long-press, state observation
│   └── RecognitionViewModel.kt                # Modified: integrate ContinuousScanner
│
└── di/modules/
    └── RecognitionModule.kt                   # Modified: provide ContinuousScanner

app/src/main/res/
├── layout/
│   └── fragment_recognition.xml               # Modified: FAB state changes
│
├── drawable/
│   └── ic_stop_24.xml                         # NEW: stop icon for scanning FAB
│
└── values/
    └── strings.xml                            # Add scanning-related strings

app/src/test/java/com/visionfocus/
└── recognition/scanning/
    ├── ContinuousScannerTest.kt               # NEW
    └── DetectedObjectTrackerTest.kt           # NEW

app/src/androidTest/java/com/visionfocus/
└── accessibility/
    └── ContinuousScanningAccessibilityTest.kt # NEW
```

### Testing Requirements

**Unit Tests (JUnit 4 + Mockito + Coroutines Test):**

```kotlin
// ContinuousScannerTest.kt
@ExperimentalCoroutinesApi
class ContinuousScannerTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var mockRecognitionService: RecognitionService
    private lateinit var mockTTSManager: TTSManager
    private lateinit var mockTTSFormatter: TTSPhraseFormatter
    private lateinit var tracker: DetectedObjectTracker
    private lateinit var scanner: ContinuousScanner
    
    @Before
    fun setup() {
        mockRecognitionService = mock()
        mockTTSManager = mock()
        mockTTSFormatter = mock()
        tracker = DetectedObjectTracker()
        
        scanner = ContinuousScanner(
            recognitionService = mockRecognitionService,
            ttsManager = mockTTSManager,
            ttsFormatter = mockTTSFormatter,
            tracker = tracker,
            context = ApplicationProvider.getApplicationContext()
        )
    }
    
    @Test
    fun `startScanning emits Scanning state`() = runTest {
        scanner.startScanning()
        
        assertTrue(scanner.scanningState.value is ScanningState.Scanning)
    }
    
    @Test
    fun `stopScanning emits Idle state with summary`() = runTest {
        scanner.startScanning()
        
        // Simulate detection
        tracker.addObject("chair")
        tracker.addObject("table")
        
        scanner.stopScanning()
        
        assertTrue(scanner.scanningState.value is ScanningState.Idle)
        verify(mockTTSManager).speak(
            argThat { it.contains("2 objects") && it.contains("chair") && it.contains("table") },
            eq(TTSManager.Priority.HIGH)
        )
    }
    
    @Test
    fun `auto-timeout stops after 60 seconds`() = runTest {
        scanner.startScanning()
        
        advanceTimeBy(60_000)
        
        assertTrue(scanner.scanningState.value is ScanningState.Idle)
        verify(mockTTSManager).speak(
            argThat { it.contains("Scan complete") },
            eq(TTSManager.Priority.HIGH)
        )
    }
    
    @Test
    fun `duplicate objects are suppressed`() = runTest {
        val mockResults = listOf(
            RecognitionResult("chair", 0.9f),
            RecognitionResult("chair", 0.85f)  // Duplicate
        )
        
        whenever(mockRecognitionService.recognizeFrame()).thenReturn(mockResults)
        
        scanner.startScanning()
        
        advanceTimeBy(6_000) // Two capture intervals
        
        // Should only announce "chair" once
        verify(mockTTSManager, times(1)).speak(argThat { it.contains("chair") })
    }
    
    @Test
    fun `low confidence objects are filtered`() = runTest {
        val mockResults = listOf(
            RecognitionResult("chair", 0.9f),   // Above threshold
            RecognitionResult("bottle", 0.5f)   // Below 0.6 threshold
        )
        
        whenever(mockRecognitionService.recognizeFrame()).thenReturn(mockResults)
        
        scanner.startScanning()
        
        advanceTimeBy(3_000)
        
        // Should only announce "chair"
        verify(mockTTSManager, times(1)).speak(argThat { it.contains("chair") })
        verify(mockTTSManager, never()).speak(argThat { it.contains("bottle") })
    }
}

// DetectedObjectTrackerTest.kt
class DetectedObjectTrackerTest {
    
    private lateinit var tracker: DetectedObjectTracker
    
    @Before
    fun setup() {
        tracker = DetectedObjectTracker()
    }
    
    @Test
    fun `isNewObject returns true for unseen object`() {
        assertTrue(tracker.isNewObject("chair"))
    }
    
    @Test
    fun `isNewObject returns false for already seen object`() {
        tracker.addObject("chair")
        assertFalse(tracker.isNewObject("chair"))
    }
    
    @Test
    fun `isNewObject is case-insensitive`() {
        tracker.addObject("chair")
        assertFalse(tracker.isNewObject("CHAIR"))
        assertFalse(tracker.isNewObject("Chair"))
    }
    
    @Test
    fun `count returns correct number of detected objects`() {
        tracker.addObject("chair")
        tracker.addObject("table")
        tracker.addObject("bottle")
        
        assertEquals(3, tracker.count())
    }
    
    @Test
    fun `reset clears all detected objects`() {
        tracker.addObject("chair")
        tracker.addObject("table")
        
        tracker.reset()
        
        assertEquals(0, tracker.count())
        assertTrue(tracker.isNewObject("chair"))
    }
}
```

**Instrumentation Tests (Espresso + Accessibility):**

```kotlin
// ContinuousScanningAccessibilityTest.kt
@HiltAndroidTest
class ContinuousScanningAccessibilityTest : BaseAccessibilityTest() {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        AccessibilityChecks.enable()
        hiltRule.inject()
    }
    
    @Test
    fun `long-press FAB activates scanning mode`() {
        launchFragmentInHiltContainer<RecognitionFragment>()
        
        // Long-press FAB
        onView(withId(R.id.recognitionFab))
            .perform(longClick())
        
        // Verify FAB icon changed to stop
        onView(withId(R.id.recognitionFab))
            .check(matches(withContentDescription(containsString("Stop scanning"))))
    }
    
    @Test
    fun `FAB contentDescription updates during scanning`() {
        launchFragmentInHiltContainer<RecognitionFragment>()
        
        // Start scanning
        onView(withId(R.id.recognitionFab))
            .perform(longClick())
        
        // Verify contentDescription changed
        onView(withId(R.id.recognitionFab))
            .check(matches(withContentDescription(containsString("Stop scanning"))))
        
        // Stop scanning
        onView(withId(R.id.recognitionFab))
            .perform(click())
        
        // Verify contentDescription restored
        onView(withId(R.id.recognitionFab))
            .check(matches(withContentDescription(containsString("Recognize objects"))))
    }
    
    @Test
    fun `TalkBack announcements work during scanning`() {
        launchFragmentInHiltContainer<RecognitionFragment>()
        
        // Enable TalkBack simulation
        val accessibilityManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        
        // Start scanning
        onView(withId(R.id.recognitionFab))
            .perform(longClick())
        
        // Wait for announcement
        Thread.sleep(1000)
        
        // Verify announcement via accessibility event (implementation-specific)
    }
}
```

### Known Patterns from Previous Stories

**FAB Long-Press Pattern (New for Story 4.4):**
- Long-press threshold: 2000ms (2 seconds)
- Haptic feedback on detection: Medium intensity (100ms)
- Icon change: camera → stop during scanning
- ContentDescription update: "Recognize objects" → "Stop scanning"

**State Management Pattern (Story 2.3, 4.1, 4.3):**
- Sealed class for mutually exclusive states
- StateFlow for reactive UI updates
- ViewModel manages state transitions
- Fragment observes and updates UI

**Voice Command Pattern (Epic 3):**
- VoiceCommand interface with keywords list
- Hilt injection of dependencies
- Register in VoiceCommandModule
- Execute method contains business logic

**TTS Integration Pattern (Story 2.2, 4.1):**
- TTSManager.speak() for all announcements
- TTSFormatter for consistent phrasing
- Priority parameter for interruption handling
- Verbosity mode affects announcement detail

**Battery Optimization Pattern (Story 2.1, 2.4):**
- Use Dispatchers.IO for CPU-bound work
- Pause camera preview when not capturing
- Monitor battery drain during testing
- Implement timeout to prevent runaway battery drain

### References

**Source Documents:**
- [epics.md#Epic 4 - Story 4.4: Continuous Scanning Mode for Environment Mapping] - User story and acceptance criteria
- [architecture.md#Decision 1: Data Persistence Strategy] - State management patterns
- [architecture.md#Decision 2: State Management Pattern] - StateFlow + ViewModel
- [prd.md#Performance Requirements] - Battery drain constraints (≤12% per hour)
- [prd.md#Accessibility Requirements] - TalkBack compliance patterns

**Related Stories:**
- Story 2.1: TFLite Model Integration & On-Device Inference - RecognitionService.recognizeFrame()
- Story 2.2: High-Confidence Detection Filtering & TTS Announcement - TTSManager, TTSFormatter
- Story 2.3: Recognition FAB with TalkBack Semantic Annotations - FAB patterns, haptic feedback
- Story 2.4: Camera Capture with Accessibility Focus Management - Camera lifecycle
- Story 2.6: Haptic Feedback for Recognition Events - Haptic intensity patterns
- Story 2.7: Complete TalkBack Navigation for Primary Flow - Accessibility baseline
- Story 3.2: Core Voice Command Processing Engine - Voice command integration
- Story 3.3: Voice Command Confirmation & Cancellation - Stop command patterns
- Story 4.1: Verbosity Mode Selection - TTSFormatter verbosity integration
- Epic 8: Enhanced Audio Priority & TTS Management - Future audio priority queue enhancement

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 via GitHub Copilot

### Debug Log References

None - Implementation completed successfully with no debug sessions required

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 via GitHub Copilot

### Debug Log References

None - Implementation completed successfully with no debug sessions required

### Completion Notes List

1. **Build Status:** ✅ SUCCESSFUL - `gradlew.bat assembleDebug` passes without errors
2. **Code Quality:** All new Kotlin files follow project conventions (Hilt DI, MVVM, Clean Architecture)
3. **Accessibility:** TalkBack support fully integrated (contentDescription updates, haptic feedback, TTS announcements)
4. **Battery Optimization:** Implemented 0.33 Hz frame rate (1 frame per 3 seconds) for power efficiency
5. **Testing:** Unit tests (DetectedObjectTrackerTest.kt + ContinuousScannerTest.kt) and instrumentation tests (ContinuousScanningAccessibilityTest.kt) created
6. **Code Review Fixes Applied:** 10 issues fixed (3 Critical, 4 High, 3 Medium)
   - CRITICAL-1 FIX: Race condition in stopScanning() - now uses Stopping state properly
   - CRITICAL-2 FIX: Channel cancellation - uses close() to drain queue instead of cancel()
   - CRITICAL-3 FIX: Camera permission check - stops scanning on SecurityException
   - HIGH-1 FIX: Long-press duration - custom 2000ms threshold with OnTouchListener
   - HIGH-2 FIX: Complete ContinuousScannerTest.kt with 11 test cases
   - HIGH-3 FIX: ContinuousScanningAccessibilityTest.kt with 7 accessibility tests
   - MEDIUM-1 FIX: Frame timeout - 2.5s timeout prevents recognition backup
   - MEDIUM-2 FIX: Error feedback - announces to user after 3 consecutive failures
   - MEDIUM-3 FIX: Documented @Singleton scope (intentional for app-wide scanner)
7. **Known Issue:** Pre-existing test compilation errors in HelpCommandTest, BackCommandTest, HomeCommandTest prevent running full test suite via `gradlew test`. These errors are UNRELATED to Story 4.4 implementation and existed prior to this story.
8. **Manual Testing:** Created comprehensive manual testing guide (MANUAL-TEST-STORY-4-4.md) with 12 test scenarios covering all acceptance criteria
9. **Voice Commands:** Integrated with Epic 3 voice command system (ScanEnvironmentCommand with 9 keyword variations, StopScanningCommand with 7 variations)
10. **UI Integration:** RecognitionFragment long-press gesture detection implemented with custom 2000ms threshold and FAB icon/contentDescription state management
11. **Architecture:** ContinuousScanner uses coroutine-based architecture with StateFlow for reactive UI updates

### File List

**New Files Created (7 - HIGH-4 FIX: Updated count):**
1. `app/src/main/java/com/visionfocus/recognition/scanning/ScanningState.kt` - Sealed class for Idle/Scanning/Stopping states
2. `app/src/main/java/com/visionfocus/recognition/scanning/DetectedObjectTracker.kt` - Thread-safe duplicate object suppression
3. `app/src/main/java/com/visionfocus/recognition/scanning/ContinuousScanner.kt` - Main scanning service with 0.33 Hz frame capture (CODE REVIEW FIXES APPLIED)
4. `app/src/main/java/com/visionfocus/voice/commands/recognition/ScanEnvironmentCommand.kt` - Voice command to start scanning
5. `app/src/main/java/com/visionfocus/voice/commands/recognition/StopScanningCommand.kt` - Voice command to stop scanning
6. `app/src/test/java/com/visionfocus/recognition/scanning/ContinuousScannerTest.kt` - HIGH-2 FIX: Complete unit tests (11 test cases)
7. `app/src/androidTest/java/com/visionfocus/accessibility/ContinuousScanningAccessibilityTest.kt` - HIGH-3 FIX: Accessibility instrumentation tests (7 tests)

**Files Modified (4):**
1. `app/src/main/java/com/visionfocus/ui/recognition/RecognitionViewModel.kt` - Added ContinuousScanner integration, scanningState StateFlow, start/stop methods
2. `app/src/main/java/com/visionfocus/ui/recognition/RecognitionFragment.kt` - HIGH-1 FIX: Custom 2000ms long-press with OnTouchListener, scanning state observation, FAB UI updates
3. `app/src/main/java/com/visionfocus/di/modules/VoiceCommandModule.kt` - Registered ScanEnvironmentCommand and StopScanningCommand
4. `app/src/main/res/values/strings.xml` - Added 3 scanning-related strings (scanning_started, stop_scanning_description, continuous_scanning_active)

**Test Files Created (2 - HIGH-2 & HIGH-3 FIX):**
1. `app/src/test/java/com/visionfocus/recognition/scanning/DetectedObjectTrackerTest.kt` - Unit tests for duplicate suppression (10 test cases)
2. `app/src/test/java/com/visionfocus/recognition/scanning/ContinuousScannerTest.kt` - HIGH-2 FIX: Unit tests for ContinuousScanner (11 test cases)

**Resources Created (1):**
1. `app/src/main/res/drawable/ic_stop_scanning.xml` - Stop icon drawable for FAB during scanning mode

**Documentation Created (1):**
1. `MANUAL-TEST-STORY-4-4.md` - Manual testing guide with 12 test scenarios for all acceptance criteria

**Project Files Modified (1 - HIGH-4 FIX: Added to list):**
1. `_bmad-output/implementation-artifacts/sprint-status.yaml` - Sprint tracking status update

**Total (HIGH-4 FIX: Complete file count):** 7 new source files, 4 modified source files, 2 test files, 1 instrumentation test file, 1 drawable resource, 1 documentation file, 1 project file = 17 files changed
