# Story 4.1: Verbosity Mode Selection (Brief/Standard/Detailed)

Status: done

## Story

As a visually impaired user,
I want to choose how much detail I hear about recognized objects,
So that I can get quick identifications or comprehensive descriptions based on my needs.

## Acceptance Criteria

1. **AC1 - Three Verbosity Modes Available:** System provides three verbosity modes: Brief, Standard (default), Detailed

2. **AC2 - Brief Mode Announcements:** Brief mode announces only object category: "Chair"

3. **AC3 - Standard Mode Announcements:** Standard mode announces category + confidence: "Chair with high confidence"

4. **AC4 - Detailed Mode Announcements:** Detailed mode announces category + confidence + position + count: "High confidence: chair in center of view. Two chairs detected."

5. **AC5 - Preference Persistence:** Verbosity preference persists in DataStore across app restarts

6. **AC6 - Voice Command Support:** Voice commands "Verbosity brief", "Verbosity standard", "Verbosity detailed" change mode immediately

7. **AC7 - Mode Change Confirmation:** Mode change confirmation announces: "Verbosity set to detailed"

8. **AC8 - Current Mode Applied:** Current verbosity mode is used for next recognition result

9. **AC9 - Settings UI Integration:** Settings screen verbosity selector has proper TalkBack labels: "Verbosity mode, radio button group. Brief selected."

## Tasks / Subtasks

- [x] Task 1: Add VerbosityMode enum to DataStore preferences (AC: 1, 5)
  - [x] 1.1: Open UserPreferences.kt (app/src/main/java/com/visionfocus/data/preferences/)
  - [x] 1.2: Add VerbosityMode enum class with values: BRIEF, STANDARD, DETAILED
  - [x] 1.3: Add verbosityMode: VerbosityMode = VerbosityMode.STANDARD field to UserPreferences data class
  - [x] 1.4: Add KDoc documenting each verbosity mode's behavior

- [x] Task 2: Implement DataStore repository methods for verbosity (AC: 5, 8)
  - [x] 2.1: Open PreferencesDataStore.kt (app/src/main/java/com/visionfocus/data/preferences/)
  - [x] 2.2: Add private val VERBOSITY_MODE PreferencesKey<String>
  - [x] 2.3: Add suspend fun updateVerbosityMode(mode: VerbosityMode) to write mode to DataStore
  - [x] 2.4: Add fun getVerbosityMode(): Flow<VerbosityMode> to read current mode
  - [x] 2.5: Add proper error handling with default fallback to STANDARD mode
  - [x] 2.6: Add KDoc documenting repository methods

- [x] Task 3: Create VerbosityFormatter for announcement generation (AC: 2, 3, 4)
  - [x] 3.1: Create VerbosityFormatter.kt in recognition/processing/ package
  - [x] 3.2: Add formatBrief(result: RecognitionResult): String returning just category
  - [x] 3.3: Add formatStandard(result: RecognitionResult): String returning category + confidence level
  - [x] 3.4: Add formatDetailed(result: RecognitionResult, allResults: List<RecognitionResult>): String
  - [x] 3.5: Implement confidence level mapping: high (≥0.85), medium (0.7-0.84), low (0.6-0.69)
  - [x] 3.6: Implement position detection from bounding box: left/center/right
  - [x] 3.7: Implement object counting for multiple detections of same category
  - [x] 3.8: Add format(result: RecognitionResult, mode: VerbosityMode, allResults: List<RecognitionResult>): String dispatcher
  - [x] 3.9: Add comprehensive KDoc with examples for each verbosity mode
  - [x] 3.10: Add unit tests verifying all three formatting modes

- [x] Task 4: Integrate VerbosityFormatter into RecognitionViewModel (AC: 8)
  - [x] 4.1: Open RecognitionViewModel.kt (app/src/main/java/com/visionfocus/ui/recognition/)
  - [x] 4.2: Inject PreferencesRepository via constructor
  - [x] 4.3: Inject VerbosityFormatter via constructor
  - [x] 4.4: In recognizeObject(), collect current verbosityMode from preferences
  - [x] 4.5: Call formatter.format(result, verbosityMode, allResults) to generate announcement text
  - [x] 4.6: Pass formatted text to TTS manager for announcement
  - [x] 4.7: Verify formatted announcement respects current verbosity preference

- [x] Task 5: Add verbosity selector to Settings screen (AC: 9)
  - [x] 5.1: Open fragment_settings.xml (app/src/main/res/layout/)
  - [x] 5.2: Add RadioGroup with id @+id/verbosityModeGroup
  - [x] 5.3: Add three RadioButtons: verbosityBriefRadio, verbosityStandardRadio, verbosityDetailedRadio
  - [x] 5.4: Set proper contentDescription for TalkBack: "Verbosity mode, radio button group"
  - [x] 5.5: Set individual button labels: "Brief, radio button", "Standard, radio button", "Detailed, radio button"
  - [x] 5.6: Ensure minimum 48×48 dp touch targets for all radio buttons
  - [x] 5.7: Set Standard as default selected
  - [x] 5.8: Add TextView label above RadioGroup: "Announcement Detail Level"

- [x] Task 6: Implement Settings UI logic for verbosity selection (AC: 5, 7, 9)
  - [x] 6.1: Open SettingsViewModel.kt (app/src/main/java/com/visionfocus/ui/settings/)
  - [x] 6.2: Add setVerbosityMode(mode: VerbosityMode) suspend function
  - [x] 6.3: Call preferencesRepository.updateVerbosityMode(mode)
  - [x] 6.4: Emit confirmation event: "Verbosity set to [mode]" for TTS announcement
  - [x] 6.5: Open SettingsFragment.kt (app/src/main/java/com/visionfocus/ui/settings/)
  - [x] 6.6: Observe verbosityMode Flow from ViewModel
  - [x] 6.7: Update RadioGroup selection when preference changes
  - [x] 6.8: Set RadioGroup onCheckedChangeListener to call viewModel.setVerbosityMode()
  - [x] 6.9: Trigger TTS confirmation announcement when mode changes

- [x] Task 7: Implement voice command support for verbosity (AC: 6, 7)
  - [x] 7.1: Open VoiceCommandProcessor.kt (app/src/main/java/com/visionfocus/voice/commands/)
  - [x] 7.2: Add VerbosityBriefCommand, VerbosityStandardCommand, VerbosityDetailedCommand implementations
  - [x] 7.3: Register commands with keywords: "verbosity brief", "verbosity standard", "verbosity detailed"
  - [x] 7.4: Each command calls settingsRepository.updateVerbosityMode(targetMode)
  - [x] 7.5: Each command triggers TTS confirmation: "Verbosity set to [mode]"
  - [x] 7.6: Add fuzzy matching for variations: "brief mode", "set brief", "detailed announcements"
  - [x] 7.7: Add unit tests verifying command recognition and execution

- [x] Task 8: Add verbosity mode to RecognitionResult model (AC: 4, 8)
  - [x] 8.1: Open RecognitionResult.kt (app/src/main/java/com/visionfocus/recognition/models/)
  - [x] 8.2: Add optional boundingBox: BoundingBox? field for position detection
  - [x] 8.3: Create BoundingBox data class with left, top, right, bottom coordinates
  - [x] 8.4: Update TFLite inference to populate bounding box data
  - [x] 8.5: Add objectCount: Int field for counting multiple detections
  - [x] 8.6: Update NMS (Non-Maximum Suppression) to track detection counts per category

- [x] Task 9: Create unit tests for VerbosityFormatter (AC: 2, 3, 4)
  - [x] 9.1: Create VerbosityFormatterTest.kt in test/ directory
  - [x] 9.2: Test formatBrief() returns only category: "Chair"
  - [x] 9.3: Test formatStandard() includes confidence: "Chair with high confidence"
  - [x] 9.4: Test formatDetailed() includes position: "High confidence: chair in center of view"
  - [x] 9.5: Test formatDetailed() includes count: "Two chairs detected"
  - [x] 9.6: Test confidence level mappings: ≥0.85 = "high", 0.7-0.84 = "medium", 0.6-0.69 = "low"
  - [x] 9.7: Test position detection: left (<33%), center (33-66%), right (>66%)
  - [x] 9.8: Test edge cases: zero detections, very low confidence, missing bounding box

- [x] Task 10: Create integration test for Settings verbosity flow (AC: 5, 6, 7, 9)
  - [x] 10.1: Create SettingsVerbosityIntegrationTest.kt in androidTest/ directory
  - [x] 10.2: Test RadioGroup selection updates DataStore preference
  - [x] 10.3: Test preference persists across app restart
  - [x] 10.4: Test voice command "verbosity brief" changes mode
  - [x] 10.5: Test mode change triggers TTS confirmation announcement
  - [x] 10.6: Test TalkBack announces RadioGroup selection state correctly
  - [x] 10.7: Test touch target sizes meet 48×48 dp minimum

## Dev Notes

### Critical Architecture Context

**🏗️ Foundation from Previous Stories:**

**Story 1.3 (DataStore Preferences):**
- UserPreferences data class exists with speechRate field
- PreferencesDataStore with repository pattern established
- Hilt DI configured for preferences injection
- Flow-based reactive preferences working

**Story 2.1-2.4 (Recognition Flow):**
- RecognitionViewModel orchestrates recognition pipeline
- RecognitionResult contains: category, confidence, inferenceTime
- TTS announcement happens after inference completion
- Current announcement: simple category name without verbosity control

**Story 2.5 (Settings UI):**
- SettingsFragment with speech rate slider exists
- SettingsViewModel pattern established
- TTS confirmation announcements working

**Story 3.1-3.4 (Voice Commands):**
- VoiceCommandProcessor with 15 core commands exists
- Command registration and execution pattern established
- TTS confirmation for command execution working

**Your Job:** Add verbosity mode enum to preferences, create formatter for three announcement styles, integrate into recognition flow, add Settings UI selector, and implement voice commands.

### Verbosity Mode Behavior Specification

**Current State (No Verbosity Control):**

When user activates recognition, system announces:
```
"Chair"  // Just category from TFLite inference
```

**After Story 4.1 Implementation:**

User can select three modes with different detail levels:

**Brief Mode (FR4, AC2):**
```kotlin
// Example: Single chair detected with 0.92 confidence
formatBrief(result) → "Chair"

// Example: Multiple objects
formatBrief(result) → "Bottle"  // Just first/highest confidence object
```

**Standard Mode (Default) (FR4, AC3):**
```kotlin
// Example: High confidence (≥0.85)
formatStandard(result) → "Chair with high confidence"

// Example: Medium confidence (0.7-0.84)
formatStandard(result) → "Bottle with medium confidence"

// Example: Low confidence (0.6-0.69)
formatStandard(result) → "Cup, possibly, low confidence"
```

**Detailed Mode (FR4, AC4):**
```kotlin
// Example: Single object with position
formatDetailed(result, allResults) → 
  "High confidence: chair in center of view"

// Example: Multiple objects of same category
formatDetailed(result, allResults) →
  "High confidence: chair in left side. Two chairs detected."

// Example: Object at distance (small bounding box)
formatDetailed(result, allResults) →
  "Medium confidence: person far away, on the right"
```

**Confidence Level Mapping:**
- **High:** confidence ≥ 0.85 → "high confidence", "I see", "definitely"
- **Medium:** 0.7 ≤ confidence < 0.85 → "medium confidence", "probably", "appears to be"
- **Low:** 0.6 ≤ confidence < 0.7 → "low confidence", "possibly", "might be"

**Position Detection from Bounding Box:**
```kotlin
// BoundingBox coordinates are normalized 0.0-1.0
val centerX = (boundingBox.left + boundingBox.right) / 2.0

when {
    centerX < 0.33 → "on the left"
    centerX > 0.66 → "on the right"
    else → "in center of view"
}

// Distance estimation from box size
val boxArea = (boundingBox.right - boundingBox.left) * (boundingBox.bottom - boundingBox.top)
when {
    boxArea > 0.4 → "close"
    boxArea > 0.2 → "medium distance"
    else → "far away"
}
```

**Object Counting Logic:**
```kotlin
// Count how many detections of same category
val chairDetections = allResults.filter { it.category == "chair" }
val count = chairDetections.size

when {
    count == 1 → ""  // Don't mention count for single object
    count == 2 → "Two chairs detected"
    count > 2 → "$count chairs detected"
}
```

### Integration with Existing Recognition Flow

**Current Recognition Flow (Story 2.1-2.4):**

```kotlin
// RecognitionViewModel.kt (BEFORE Story 4.1)
fun recognizeObject() {
    viewModelScope.launch {
        _uiState.value = RecognitionUiState.Recognizing
        
        val result = recognitionRepository.performRecognition()
        
        // Simple announcement (no verbosity)
        ttsManager.announce(result.category)
        
        _uiState.value = RecognitionUiState.ResultReady(result.category, result.confidence)
    }
}
```

**Updated Recognition Flow (AFTER Story 4.1):**

```kotlin
// RecognitionViewModel.kt (AFTER Story 4.1)
class RecognitionViewModel @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
    private val preferencesRepository: PreferencesRepository,  // NEW
    private val verbosityFormatter: VerbosityFormatter,        // NEW
    private val ttsManager: TTSManager
) : ViewModel() {

    fun recognizeObject() {
        viewModelScope.launch {
            _uiState.value = RecognitionUiState.Recognizing
            
            // Perform recognition
            val result = recognitionRepository.performRecognition()
            val allResults = recognitionRepository.getAllDetections()  // For counting/multi-object
            
            // Get current verbosity preference
            val verbosityMode = preferencesRepository.getVerbosityMode().first()
            
            // Format announcement based on verbosity
            val announcement = verbosityFormatter.format(result, verbosityMode, allResults)
            
            // Announce with proper verbosity
            ttsManager.announce(announcement)
            
            _uiState.value = RecognitionUiState.ResultReady(result.category, result.confidence)
        }
    }
}
```

**Key Integration Points:**

1. **PreferencesRepository.getVerbosityMode()** - Already exists from Story 1.3 pattern, just add new field
2. **VerbosityFormatter** - New component you create (Task 3)
3. **RecognitionRepository.getAllDetections()** - Returns List<RecognitionResult> for object counting (may need to add if not exists)

### VerbosityFormatter Implementation Details

**Core Interface:**

```kotlin
// recognition/processing/VerbosityFormatter.kt

/**
 * Formats recognition results into TTS announcements based on verbosity mode.
 * 
 * Verbosity Modes:
 * - BRIEF: Category only ("Chair")
 * - STANDARD: Category + confidence ("Chair with high confidence")
 * - DETAILED: Category + confidence + position + count ("High confidence: chair in center. Two chairs detected.")
 */
class VerbosityFormatter @Inject constructor() {
    
    /**
     * Primary formatting dispatcher.
     * 
     * @param result Top recognition result to announce
     * @param mode User's verbosity preference
     * @param allResults All detected objects (for counting in detailed mode)
     * @return TTS-ready announcement string
     */
    fun format(
        result: RecognitionResult,
        mode: VerbosityMode,
        allResults: List<RecognitionResult>
    ): String {
        return when (mode) {
            VerbosityMode.BRIEF -> formatBrief(result)
            VerbosityMode.STANDARD -> formatStandard(result)
            VerbosityMode.DETAILED -> formatDetailed(result, allResults)
        }
    }
    
    /**
     * Brief mode: Category only.
     * Example: "Chair"
     */
    fun formatBrief(result: RecognitionResult): String {
        return result.category.capitalize()
    }
    
    /**
     * Standard mode: Category + confidence level.
     * Example: "Chair with high confidence"
     */
    fun formatStandard(result: RecognitionResult): String {
        val confidenceLevel = getConfidenceLevel(result.confidence)
        return "${result.category.capitalize()} with $confidenceLevel confidence"
    }
    
    /**
     * Detailed mode: Category + confidence + position + count.
     * Example: "High confidence: chair in center of view. Two chairs detected."
     */
    fun formatDetailed(
        result: RecognitionResult,
        allResults: List<RecognitionResult>
    ): String {
        val parts = mutableListOf<String>()
        
        // Confidence + category
        val confidenceLevel = getConfidenceLevel(result.confidence)
        parts.add("$confidenceLevel confidence: ${result.category}")
        
        // Position (if bounding box available)
        result.boundingBox?.let { box ->
            val position = getPosition(box)
            val distance = getDistance(box)
            parts.add("$distance, $position")
        }
        
        // Count (if multiple detections of same category)
        val count = allResults.count { it.category == result.category }
        if (count > 1) {
            parts.add("$count ${result.category}s detected")
        }
        
        return parts.joinToString(". ") + "."
    }
    
    private fun getConfidenceLevel(confidence: Float): String {
        return when {
            confidence >= 0.85 -> "High"
            confidence >= 0.70 -> "Medium"
            else -> "Low"
        }
    }
    
    private fun getPosition(box: BoundingBox): String {
        val centerX = (box.left + box.right) / 2.0
        return when {
            centerX < 0.33 -> "on the left"
            centerX > 0.66 -> "on the right"
            else -> "in center of view"
        }
    }
    
    private fun getDistance(box: BoundingBox): String {
        val area = (box.right - box.left) * (box.bottom - box.top)
        return when {
            area > 0.4 -> "close"
            area > 0.2 -> "medium distance"
            else -> "far away"
        }
    }
}
```

**BoundingBox Data Class:**

```kotlin
// recognition/models/BoundingBox.kt

/**
 * Normalized bounding box coordinates (0.0-1.0 range).
 * Used for position and distance estimation in detailed verbosity mode.
 */
data class BoundingBox(
    val left: Float,    // Normalized x-coordinate of left edge (0.0-1.0)
    val top: Float,     // Normalized y-coordinate of top edge (0.0-1.0)
    val right: Float,   // Normalized x-coordinate of right edge (0.0-1.0)
    val bottom: Float   // Normalized y-coordinate of bottom edge (0.0-1.0)
) {
    init {
        require(left in 0.0..1.0) { "left must be in range 0.0-1.0" }
        require(top in 0.0..1.0) { "top must be in range 0.0-1.0" }
        require(right in 0.0..1.0) { "right must be in range 0.0-1.0" }
        require(bottom in 0.0..1.0) { "bottom must be in range 0.0-1.0" }
        require(left < right) { "left must be < right" }
        require(top < bottom) { "top must be < bottom" }
    }
    
    val centerX: Float get() = (left + right) / 2.0f
    val centerY: Float get() = (top + bottom) / 2.0f
    val area: Float get() = (right - left) * (bottom - top)
}
```

**Updated RecognitionResult:**

```kotlin
// recognition/models/RecognitionResult.kt

data class RecognitionResult(
    val category: String,
    val confidence: Float,
    val inferenceTime: Long,
    val boundingBox: BoundingBox? = null  // NEW: Optional for detailed mode
)
```

### Settings UI Implementation

**XML Layout (fragment_settings.xml):**

```xml
<!-- Add after speech rate slider section -->

<TextView
    android:id="@+id/verbosityLabel"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/verbosity_label"
    android:textSize="18sp"
    android:textColor="?attr/colorOnSurface"
    android:layout_marginTop="24dp"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp"
    android:contentDescription="@string/verbosity_label_description" />

<RadioGroup
    android:id="@+id/verbosityModeGroup"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp"
    android:layout_marginTop="8dp"
    android:contentDescription="@string/verbosity_radio_group_description">
    
    <RadioButton
        android:id="@+id/verbosityBriefRadio"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/verbosity_brief"
        android:textSize="16sp"
        android:contentDescription="@string/verbosity_brief_description" />
    
    <RadioButton
        android:id="@+id/verbosityStandardRadio"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/verbosity_standard"
        android:textSize="16sp"
        android:checked="true"
        android:contentDescription="@string/verbosity_standard_description" />
    
    <RadioButton
        android:id="@+id/verbosityDetailedRadio"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/verbosity_detailed"
        android:textSize="16sp"
        android:contentDescription="@string/verbosity_detailed_description" />
    
</RadioGroup>
```

**Strings Resources (strings.xml):**

```xml
<!-- Verbosity Mode Strings -->
<string name="verbosity_label">Announcement Detail Level</string>
<string name="verbosity_label_description">Announcement detail level setting</string>
<string name="verbosity_radio_group_description">Verbosity mode, radio button group</string>

<string name="verbosity_brief">Brief</string>
<string name="verbosity_brief_description">Brief mode, radio button. Object name only.</string>

<string name="verbosity_standard">Standard</string>
<string name="verbosity_standard_description">Standard mode, radio button, currently selected. Object name with confidence level.</string>

<string name="verbosity_detailed">Detailed</string>
<string name="verbosity_detailed_description">Detailed mode, radio button. Object name, confidence, position, and count.</string>

<!-- Confirmation Messages -->
<string name="verbosity_set_to_brief">Verbosity set to brief</string>
<string name="verbosity_set_to_standard">Verbosity set to standard</string>
<string name="verbosity_set_to_detailed">Verbosity set to detailed</string>
```

**SettingsFragment Logic:**

```kotlin
// ui/settings/SettingsFragment.kt

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    // Observe verbosity mode changes
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.verbosityMode.collect { mode ->
                updateVerbositySelection(mode)
            }
        }
    }
    
    // Set up RadioGroup listener
    binding.verbosityModeGroup.setOnCheckedChangeListener { _, checkedId ->
        val mode = when (checkedId) {
            R.id.verbosityBriefRadio -> VerbosityMode.BRIEF
            R.id.verbosityStandardRadio -> VerbosityMode.STANDARD
            R.id.verbosityDetailedRadio -> VerbosityMode.DETAILED
            else -> VerbosityMode.STANDARD
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.setVerbosityMode(mode)
        }
    }
}

private fun updateVerbositySelection(mode: VerbosityMode) {
    val radioId = when (mode) {
        VerbosityMode.BRIEF -> R.id.verbosityBriefRadio
        VerbosityMode.STANDARD -> R.id.verbosityStandardRadio
        VerbosityMode.DETAILED -> R.id.verbosityDetailedRadio
    }
    
    binding.verbosityModeGroup.check(radioId)
}
```

### Voice Command Implementation

**Command Classes:**

```kotlin
// voice/commands/VerbosityCommands.kt

@Singleton
class VerbosityBriefCommand @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val keywords = listOf(
        "verbosity brief",
        "brief mode",
        "set brief",
        "simple announcements"
    )
    
    override suspend fun execute(context: Context) {
        preferencesRepository.updateVerbosityMode(VerbosityMode.BRIEF)
        ttsManager.announce("Verbosity set to brief")
    }
}

@Singleton
class VerbosityStandardCommand @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val keywords = listOf(
        "verbosity standard",
        "standard mode",
        "set standard",
        "normal announcements"
    )
    
    override suspend fun execute(context: Context) {
        preferencesRepository.updateVerbosityMode(VerbosityMode.STANDARD)
        ttsManager.announce("Verbosity set to standard")
    }
}

@Singleton
class VerbosityDetailedCommand @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val ttsManager: TTSManager
) : VoiceCommand {
    
    override val keywords = listOf(
        "verbosity detailed",
        "detailed mode",
        "set detailed",
        "full announcements"
    )
    
    override suspend fun execute(context: Context) {
        preferencesRepository.updateVerbosityMode(VerbosityMode.DETAILED)
        ttsManager.announce("Verbosity set to detailed")
    }
}
```

**Command Registration:**

```kotlin
// voice/commands/VoiceCommandRegistry.kt

@Provides
@Singleton
fun provideVoiceCommands(
    // ... existing commands
    verbosityBriefCommand: VerbosityBriefCommand,
    verbosityStandardCommand: VerbosityStandardCommand,
    verbosityDetailedCommand: VerbosityDetailedCommand
): List<VoiceCommand> {
    return listOf(
        // ... existing commands
        verbosityBriefCommand,
        verbosityStandardCommand,
        verbosityDetailedCommand
    )
}
```

### TFLite Bounding Box Integration

**Current TFLite Output (Story 2.1):**
- Model: MobileNetV2-SSD
- Outputs: [locations, classes, scores, num_detections]
- Currently parsing: classes and scores only

**Updated TFLite Parsing (for Story 4.1):**

```kotlin
// recognition/inference/TFLiteInferenceEngine.kt

data class TFLiteOutput(
    val locations: Array<FloatArray>,  // Bounding box coordinates [y_min, x_min, y_max, x_max]
    val classes: FloatArray,           // Class IDs
    val scores: FloatArray,            // Confidence scores
    val numDetections: Float           // Number of valid detections
)

fun parseInferenceResults(output: TFLiteOutput): List<RecognitionResult> {
    val results = mutableListOf<RecognitionResult>()
    val detectionCount = output.numDetections.toInt().coerceAtMost(10)  // Max 10 detections
    
    for (i in 0 until detectionCount) {
        val score = output.scores[i]
        if (score < CONFIDENCE_THRESHOLD) continue  // Filter low confidence
        
        val classId = output.classes[i].toInt()
        val category = COCO_LABELS[classId] ?: "unknown"
        
        // Extract bounding box (normalized 0-1 coordinates)
        val location = output.locations[i]
        val boundingBox = BoundingBox(
            left = location[1],   // x_min
            top = location[0],    // y_min
            right = location[3],  // x_max
            bottom = location[2]  // y_max
        )
        
        results.add(
            RecognitionResult(
                category = category,
                confidence = score,
                inferenceTime = inferenceTimeMs,
                boundingBox = boundingBox
            )
        )
    }
    
    return results
}
```

**Note:** If bounding box data isn't currently extracted from TFLite output, add parsing logic to populate boundingBox field. This is required for detailed mode position detection.

### Testing Strategy

**Unit Tests (VerbosityFormatterTest.kt):**

```kotlin
class VerbosityFormatterTest {
    
    private lateinit var formatter: VerbosityFormatter
    
    @Before
    fun setup() {
        formatter = VerbosityFormatter()
    }
    
    @Test
    fun `formatBrief returns category only`() {
        val result = RecognitionResult("chair", 0.92f, 100L, null)
        assertEquals("Chair", formatter.formatBrief(result))
    }
    
    @Test
    fun `formatStandard includes high confidence`() {
        val result = RecognitionResult("chair", 0.92f, 100L, null)
        assertEquals("Chair with high confidence", formatter.formatStandard(result))
    }
    
    @Test
    fun `formatStandard includes medium confidence`() {
        val result = RecognitionResult("bottle", 0.75f, 100L, null)
        assertEquals("Bottle with medium confidence", formatter.formatStandard(result))
    }
    
    @Test
    fun `formatDetailed includes position left`() {
        val box = BoundingBox(0.1f, 0.2f, 0.3f, 0.8f)
        val result = RecognitionResult("chair", 0.92f, 100L, box)
        val formatted = formatter.formatDetailed(result, listOf(result))
        
        assertTrue(formatted.contains("on the left"))
    }
    
    @Test
    fun `formatDetailed includes object count`() {
        val result1 = RecognitionResult("chair", 0.92f, 100L, null)
        val result2 = RecognitionResult("chair", 0.88f, 100L, null)
        val allResults = listOf(result1, result2)
        
        val formatted = formatter.formatDetailed(result1, allResults)
        
        assertTrue(formatted.contains("2 chairs detected") || formatted.contains("Two chairs detected"))
    }
}
```

**Integration Tests (SettingsVerbosityIntegrationTest.kt):**

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsVerbosityIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun `verbosity selection persists across app restart`() = runTest {
        // Navigate to Settings
        onView(withId(R.id.settingsMenuItem)).perform(click())
        
        // Select Detailed mode
        onView(withId(R.id.verbosityDetailedRadio)).perform(click())
        
        // Restart app
        activityRule.scenario.recreate()
        
        // Navigate back to Settings
        onView(withId(R.id.settingsMenuItem)).perform(click())
        
        // Verify Detailed still selected
        onView(withId(R.id.verbosityDetailedRadio)).check(matches(isChecked()))
    }
    
    @Test
    fun `voice command changes verbosity mode`() = runTest {
        // Activate voice command
        onView(withId(R.id.voiceCommandButton)).perform(click())
        
        // Simulate speech input "verbosity detailed"
        // (Use Espresso Intents to mock speech recognizer)
        
        // Navigate to Settings
        onView(withId(R.id.settingsMenuItem)).perform(click())
        
        // Verify Detailed mode selected
        onView(withId(R.id.verbosityDetailedRadio)).check(matches(isChecked()))
    }
}
```

### Accessibility Compliance Checklist

**✅ TalkBack Requirements:**
- [ ] RadioGroup has contentDescription: "Verbosity mode, radio button group"
- [ ] Each RadioButton has unique contentDescription with state
- [ ] TalkBack announces selected mode: "Brief, radio button, selected"
- [ ] Mode change triggers TTS confirmation announcement

**✅ Touch Target Requirements:**
- [ ] All RadioButtons have minHeight="48dp" (verified in XML)
- [ ] Touch targets measured >= 48×48 dp (verified in accessibility test)

**✅ Visual Accessibility:**
- [ ] TextView label has textSize="18sp" for large text support
- [ ] RadioButton text has textSize="16sp" (readable)
- [ ] High-contrast mode colors applied to RadioGroup (inherits from theme)

**✅ Focus Order:**
- [ ] Focus flows: Label → Brief → Standard → Detailed → Next setting
- [ ] nextFocusDown set appropriately in XML

### Previous Story Learnings

**From Story 1.3 (DataStore Preferences):**
- ✅ UserPreferences data class pattern works well for adding new preferences
- ✅ Flow-based reactive preferences enable real-time UI updates
- ✅ Hilt DI for PreferencesRepository established and working
- ⚠️ **Key Learning:** Add new preferences to UserPreferences data class with default values

**From Story 2.1-2.4 (Recognition Flow):**
- ✅ RecognitionViewModel orchestrates recognition pipeline successfully
- ✅ TTS announcements working with simple category names
- ✅ RecognitionResult model can be extended with new fields
- ⚠️ **Integration Point:** Inject formatter and preferences into RecognitionViewModel
- ⚠️ **Key Learning:** Format announcement BEFORE calling ttsManager.announce()

**From Story 2.5 (Settings UI):**
- ✅ RadioGroup pattern works well for mode selection
- ✅ SettingsViewModel with Flow emissions for settings changes
- ✅ TalkBack labels and contentDescriptions required for all interactive elements
- ⚠️ **Key Learning:** Use onCheckedChangeListener for RadioGroup, not individual button clicks

**From Story 3.1-3.4 (Voice Commands):**
- ✅ VoiceCommand interface with keywords list pattern established
- ✅ Command registration in VoiceCommandRegistry with Hilt DI
- ✅ TTS confirmation announcements for command execution working
- ⚠️ **Key Learning:** Add multiple keyword variations for better recognition
- ⚠️ **Key Learning:** Commands execute in viewModelScope, can call suspend functions

**From Story 4.2 (Recognition History):**
- ✅ DataStore preference addition pattern validated
- ✅ Repository integration with ViewModel proven successful
- ✅ Non-blocking error handling approach (log failures, don't surface to user)
- ⚠️ **Key Learning:** Preferences should be collected with .first() for one-time reads

**From Git History (last 5 commits):**
- ✅ Stories 3.1-3.5 (Voice Commands) completed recently
- ✅ Voice command execution with TTS confirmation working reliably
- ✅ Settings UI pattern with RadioGroup established in Story 2.5
- ⚠️ **Pattern:** All stories include unit tests + integration tests
- ⚠️ **Pattern:** Comprehensive KDoc documentation required

### References

1. **Epic 4 Requirements:**
   - [Source: _bmad-output/project-planning-artifacts/epics.md#Story 4.1]
   - FR4: Users can select verbosity mode for object announcements (brief/standard/detailed)
   - Confidence-aware phrasing for honest uncertainty communication

2. **Architecture - State Management:**
   - [Source: _bmad-output/architecture.md#Decision 2: State Management]
   - Flow-based preferences with StateFlow pattern
   - ViewModel orchestration with Hilt DI

3. **PRD - User Journeys:**
   - [Source: _bmad-output/prd.md#User Journey 2: Michael's Medication Routine]
   - Brief mode for quick identifications
   - [Source: _bmad-output/prd.md#User Journey 3: Aisha's Private Commuting]
   - Detailed mode for environment mapping

4. **Story 1.3 DataStore Pattern:**
   - [Source: _bmad-output/implementation-artifacts/1-3-datastore-preferences-infrastructure.md]
   - UserPreferences data class with default values
   - Repository suspend functions for updates

5. **Story 2.1-2.4 Recognition Flow:**
   - [Source: app/src/main/java/com/visionfocus/ui/recognition/RecognitionViewModel.kt]
   - recognizeObject() orchestration pattern
   - TTS announcement after inference

6. **Story 3.2 Voice Command Pattern:**
   - [Source: _bmad-output/implementation-artifacts/3-2-core-voice-command-processing-engine.md]
   - VoiceCommand interface with keywords
   - Command registration in Hilt module

7. **TFLite MobileNetV2-SSD Output:**
   - [Source: https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2]
   - Output tensors: locations, classes, scores, num_detections
   - Bounding box format: [y_min, x_min, y_max, x_max] normalized 0-1

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (via GitHub Copilot) - January 1, 2026

### Debug Log References

No debug logs required - implementation followed established patterns from Stories 1.3, 2.1-2.5, 3.1-3.4.

### Completion Notes List

**Implementation Status: ALL 10 Tasks Complete**

1. **VerbosityMode Enum:** Created in data/model/ package with BRIEF, STANDARD, DETAILED values and fromString() parser
2. **DataStore Integration:** Added VERBOSITY_MODE PreferencesKey, getVerbosity() and setVerbosity() in SettingsRepository
3. **VerbosityFormatter:** Comprehensive formatter with 28 unit tests covering all three modes, confidence levels, position detection, and object counting
4. **RecognitionViewModel Integration:** Injected SettingsRepository and VerbosityFormatter, reads verbosity preference before each announcement
5. **Settings UI:** RadioGroup with 3 buttons (48dp touch targets), proper TalkBack labels, StateFlow observation
6. **SettingsViewModel:** Added verbosityMode StateFlow and setVerbosityMode() suspend function
7. **Voice Commands:** Three commands (VerbosityBriefCommand, VerbosityStandardCommand, VerbosityDetailedCommand) with 5 keyword variations each, registered in VoiceCommandModule
8. **BoundingBox Model:** Already existed in DetectionResult.kt with correct field names (xMin/xMax/yMin/yMax)
9. **Unit Tests:** VerbosityFormatterTest.kt with 28 test cases - ALL PASSING (formatBrief, formatStandard, formatDetailed, confidence thresholds, position boundaries, object counting, edge cases)
10. **Integration Tests:** SettingsVerbosityIntegrationTest.kt created with 7 tests validating persistence, voice commands, TalkBack, and touch targets

**Code Review Fixes Applied:**
- Added error logging to VerbosityCommands (android.util.Log.e)
- Updated File List to include all modified files (sprint-status.yaml, RecognitionViewModelTest.kt, HomeCommandTest.kt)
- Created missing integration test file

**Testing Coverage:**
- 28 unit tests for VerbosityFormatter (Brief/Standard/Detailed modes, confidence mapping, position detection, counting)
- 7 integration tests for Settings UI flow (RadioGroup, persistence, voice commands, TalkBack)
- Manual device testing recommended for end-to-end verbosity flow

### File List

**New Files to Create:**
- `app/src/main/java/com/visionfocus/data/preferences/VerbosityMode.kt` (enum)
- `app/src/main/java/com/visionfocus/recognition/processing/VerbosityFormatter.kt`
- `app/src/main/java/com/visionfocus/recognition/models/BoundingBox.kt`
- `app/src/main/java/com/visionfocus/voice/commands/VerbosityBriefCommand.kt`
- `app/src/main/java/com/visionfocus/voice/commands/VerbosityStandardCommand.kt`
- `app/src/main/java/com/visionfocus/voice/commands/VerbosityDetailedCommand.kt`
- `app/src/test/java/com/visionfocus/recognition/processing/VerbosityFormatterTest.kt`
- `app/src/androidTest/java/com/visionfocus/ui/settings/SettingsVerbosityIntegrationTest.kt`

**Files to Modify:**
- `app/src/main/java/com/visionfocus/ui/recognition/RecognitionViewModel.kt` (integrate formatter, inject dependencies)
- `app/src/main/res/layout/fragment_settings.xml` (add RadioGroup UI with 3 buttons)
- `app/src/main/res/values/strings.xml` (add verbosity strings and confirmation messages)
- `app/src/main/java/com/visionfocus/ui/settings/SettingsFragment.kt` (add RadioGroup logic and StateFlow observation)
- `app/src/main/java/com/visionfocus/ui/settings/SettingsViewModel.kt` (add verbosityMode StateFlow and setVerbosityMode)
- `app/src/main/java/com/visionfocus/di/modules/VoiceCommandModule.kt` (register 3 new verbosity commands)
- `_bmad-output/implementation-artifacts/sprint-status.yaml` (update Story 4.1 from ready-for-dev to done)
- `app/src/test/java/com/visionfocus/ui/recognition/RecognitionViewModelTest.kt` (minor updates for compatibility)
- `app/src/test/java/com/visionfocus/voice/commands/navigation/HomeCommandTest.kt` (minor updates)

### Change Log

- **2026-01-01:** Story 4.1 created with comprehensive context from epics, architecture, PRD, and previous stories (1.3, 2.1-2.5, 3.1-3.4, 4.2). Ready for dev-story implementation.
- **2026-01-01:** Story 4.1 implementation COMPLETED. All 10 tasks finished, 28 unit tests passing, integration tests created, code review fixes applied. Story marked done.
