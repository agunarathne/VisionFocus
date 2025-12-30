# Story 4.3: Recognition History Review UI

Status: ready-for-dev

## Story

As a visually impaired user,
I want to navigate my recognition history using TalkBack,
So that I can hear past identifications without visual access.

## Acceptance Criteria

**Given** recognition history stored from Story 4.2
**When** I navigate to History screen (via voice command "History" or bottom navigation)
**Then** RecyclerView displays last 50 recognition results
**And** each list item includes: category, confidence level, timestamp, detail text
**And** each list item has TalkBack content description: "Chair, high confidence, December 24, 2025 at 3:45 PM"
**And** TalkBack swipe right/left navigates between history items
**And** double-tap on history item announces full details again via TTS
**And** empty history state announces: "No recognition history yet. Recognize an object to see results here."
**And** "Clear history" button (56×56 dp) with confirmation dialog allows deleting all history
**And** clear history confirmation dialog: "Are you sure you want to delete all recognition history? This cannot be undone."
**And** history cleared confirmation announces: "Recognition history cleared"

## Tasks / Subtasks

- [ ] Task 1: Create HistoryFragment with RecyclerView UI (AC: RecyclerView, empty state)
  - [ ] 1.1: Create fragment_history.xml layout with RecyclerView + empty state TextView
  - [ ] 1.2: Create HistoryFragment.kt extending Fragment with View Binding
  - [ ] 1.3: Configure RecyclerView with LinearLayoutManager
  - [ ] 1.4: Implement empty state visibility logic (show when history size == 0)
  - [ ] 1.5: Add TalkBack semantic annotation for empty state message

- [ ] Task 2: Create RecyclerView adapter for history items (AC: list items with data)
  - [ ] 2.1: Create item_recognition_history.xml layout for list item
  - [ ] 2.2: Add TextViews for category, confidence, timestamp, detail
  - [ ] 2.3: Create RecognitionHistoryAdapter extending ListAdapter with DiffUtil
  - [ ] 2.4: Implement ViewHolder binding recognition history data to views
  - [ ] 2.5: Format timestamp using DateTimeFormatter or SimpleDateFormat

- [ ] Task 3: Implement TalkBack content descriptions for list items (AC: TalkBack descriptions)
  - [ ] 3.1: Set contentDescription for each list item in ViewHolder.bind()
  - [ ] 3.2: Format description: "[category], [confidence level], [formatted timestamp]"
  - [ ] 3.3: Ensure list item is focusable for TalkBack navigation
  - [ ] 3.4: Test swipe right/left navigation between items with TalkBack enabled
  - [ ] 3.5: Validate focus order follows visual layout (top-to-bottom)

- [ ] Task 4: Implement list item click listener with TTS announcement (AC: double-tap announces)
  - [ ] 4.1: Add OnClickListener to list item in adapter
  - [ ] 4.2: Inject TTSManager via Hilt into HistoryFragment
  - [ ] 4.3: On item click, trigger TTS announcement with full detail text
  - [ ] 4.4: Use same TTSFormatter from Story 2.2 for consistent phrasing
  - [ ] 4.5: Test double-tap with TalkBack activates click listener

- [ ] Task 5: Create HistoryViewModel with StateFlow (AC: load history data)
  - [ ] 5.1: Create HistoryViewModel.kt with @HiltViewModel annotation
  - [ ] 5.2: Inject RecognitionHistoryRepository via constructor
  - [ ] 5.3: Create sealed class HistoryUiState (Loading, Success, Empty, Error)
  - [ ] 5.4: Expose StateFlow<HistoryUiState> for fragment observation
  - [ ] 5.5: Load history via repository.getLast50Results() in init block

- [ ] Task 6: Integrate HistoryViewModel with HistoryFragment (AC: display history)
  - [ ] 6.1: Obtain ViewModel using by viewModels() delegation
  - [ ] 6.2: Collect uiState Flow in lifecycleScope with repeatOnLifecycle(STARTED)
  - [ ] 6.3: Update RecyclerView adapter when uiState is Success
  - [ ] 6.4: Show empty state when uiState is Empty
  - [ ] 6.5: Handle Loading and Error states with appropriate UI feedback

- [ ] Task 7: Add "Clear History" button with 56×56 dp touch target (AC: clear button)
  - [ ] 7.1: Add FloatingActionButton for clear history in fragment_history.xml
  - [ ] 7.2: Set contentDescription: "Clear all recognition history, button"
  - [ ] 7.3: Position FAB consistently (bottom-right corner per Story 2.3 pattern)
  - [ ] 7.4: Set minimum touch target size to 56×56 dp (validated programmatically)
  - [ ] 7.5: Add icon (Material Symbols "delete" or "clear_all")

- [ ] Task 8: Implement clear history confirmation dialog (AC: confirmation dialog)
  - [ ] 8.1: Create confirmation dialog using MaterialAlertDialogBuilder
  - [ ] 8.2: Set dialog title and message with TalkBack-accessible text
  - [ ] 8.3: Add "Cancel" and "Delete" buttons with proper contentDescription
  - [ ] 8.4: Ensure dialog buttons have minimum 48×48 dp touch targets
  - [ ] 8.5: Test dialog announcement with TalkBack enabled

- [ ] Task 9: Implement clear history functionality (AC: history cleared)
  - [ ] 9.1: Add clearAllHistory() method to HistoryViewModel
  - [ ] 9.2: Call repository.clearHistory() in viewModelScope.launch
  - [ ] 9.3: Update uiState to Empty after successful deletion
  - [ ] 9.4: Trigger TalkBack announcement: "Recognition history cleared"
  - [ ] 9.5: Dismiss confirmation dialog after successful deletion

- [ ] Task 10: Add navigation to HistoryFragment (AC: voice command/bottom navigation)
  - [ ] 10.1: Add History destination to nav_graph.xml (if using Navigation Component)
  - [ ] 10.2: Add navigation action from RecognitionFragment to HistoryFragment
  - [ ] 10.3: Integrate with voice command system (Epic 3) - voice command "History" navigates
  - [ ] 10.4: Add bottom navigation bar item (if implementing bottom nav per UX spec)
  - [ ] 10.5: Ensure navigation preserves TalkBack focus properly

- [ ] Task 11: Create unit tests for HistoryViewModel (AC: all)
  - [ ] 11.1: Create HistoryViewModelTest.kt with JUnit 4 + Mockito
  - [ ] 11.2: Test initial state is Loading
  - [ ] 11.3: Test successful history load emits Success state with data
  - [ ] 11.4: Test empty history load emits Empty state
  - [ ] 11.5: Test clearAllHistory() calls repository and updates state

- [ ] Task 12: Create instrumentation tests for accessibility (AC: TalkBack compliance)
  - [ ] 12.1: Create HistoryAccessibilityTest.kt extending BaseAccessibilityTest (Story 1.5)
  - [ ] 12.2: Enable AccessibilityChecks.enable() in @Before setup
  - [ ] 12.3: Test RecyclerView items have proper contentDescription
  - [ ] 12.4: Test empty state has TalkBack announcement
  - [ ] 12.5: Test clear history button has 56×56 dp touch target

## Dev Notes

### ⚠️ CRITICAL: Story 4.2 Dependency

**Story 4.2 (Recognition History Storage with Room Database) MUST be completed before Story 4.3 can be implemented.**

Story 4.2 provides:
- RecognitionHistoryEntity schema with all required fields (category, confidence, timestamp, verbosityMode, detailText)
- RecognitionHistoryDao with getLast50Results(), clearHistory(), insert() methods
- RecognitionHistoryRepository implementation
- Room database integration with encryption (Android Keystore + SQLCipher)
- Recognition result persistence logic integrated into recognition flow (Story 2.1/2.2)

**IF Story 4.2 is NOT complete:**
1. **HALT development** - Do not proceed with Story 4.3
2. **Notify user:** "Story 4.2 must be completed first. Story 4.3 depends on RecognitionHistoryRepository and Room schema."
3. **Suggest:** Run `*create-story 4.2` to generate Story 4.2, then run `dev-story` to implement it

**IF Story 4.2 IS complete:**
- Verify RecognitionHistoryEntity exists in app/src/main/java/com/visionfocus/data/local/entity/
- Verify RecognitionHistoryDao exists in app/src/main/java/com/visionfocus/data/local/dao/
- Verify RecognitionHistoryRepository exists in app/src/main/java/com/visionfocus/data/repository/
- Proceed with Story 4.3 implementation using existing database infrastructure

### Technical Requirements from Architecture Document

**RecyclerView Pattern with TalkBack Support:**

From [architecture.md#Decision 3: UI Architecture Approach]:

VisionFocus uses **XML Layouts + View Binding** (NOT Jetpack Compose) for TalkBack maturity and research validation.

```kotlin
// HistoryFragment.kt pattern
class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistoryViewModel by viewModels()
    
    private lateinit var adapter: RecognitionHistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClearHistoryButton()
        observeUiState()
    }
    
    private fun setupRecyclerView() {
        adapter = RecognitionHistoryAdapter { historyItem ->
            // Handle item click - announce via TTS
            announceHistoryItem(historyItem)
        }
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
            // Accessibility: Ensure RecyclerView is traversable
            isFocusable = false // Let focus go to items, not container
        }
    }
    
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HistoryUiState.Loading -> showLoading()
                        is HistoryUiState.Success -> {
                            showHistory(state.items)
                        }
                        is HistoryUiState.Empty -> showEmptyState()
                        is HistoryUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**RecyclerView Adapter with DiffUtil and TalkBack:**

```kotlin
// RecognitionHistoryAdapter.kt
class RecognitionHistoryAdapter(
    private val onItemClick: (RecognitionHistoryEntity) -> Unit
) : ListAdapter<RecognitionHistoryEntity, RecognitionHistoryAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecognitionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemRecognitionHistoryBinding,
        private val onItemClick: (RecognitionHistoryEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: RecognitionHistoryEntity) {
            binding.categoryTextView.text = item.objectLabel
            binding.confidenceTextView.text = formatConfidence(item.confidence)
            binding.timestampTextView.text = formatTimestamp(item.timestamp)
            
            // CRITICAL: TalkBack content description for entire item
            binding.root.contentDescription = buildContentDescription(item)
            
            // Ensure item is focusable for TalkBack
            binding.root.isFocusable = true
            binding.root.isClickable = true
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
        
        private fun buildContentDescription(item: RecognitionHistoryEntity): String {
            val confidence = formatConfidence(item.confidence)
            val timestamp = formatTimestamp(item.timestamp)
            return "${item.objectLabel}, $confidence, $timestamp"
        }
        
        private fun formatConfidence(confidence: Float): String {
            return when {
                confidence >= 0.85 -> "high confidence"
                confidence >= 0.70 -> "medium confidence"
                else -> "low confidence"
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<RecognitionHistoryEntity>() {
        override fun areItemsTheSame(
            oldItem: RecognitionHistoryEntity,
            newItem: RecognitionHistoryEntity
        ): Boolean = oldItem.id == newItem.id
        
        override fun areContentsTheSame(
            oldItem: RecognitionHistoryEntity,
            newItem: RecognitionHistoryEntity
        ): Boolean = oldItem == newItem
    }
}
```

**HistoryViewModel Pattern:**

From [architecture.md#Decision 2: State Management Pattern]:

```kotlin
// HistoryViewModel.kt
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val items: List<RecognitionHistoryEntity>) : HistoryUiState()
    object Empty : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: RecognitionHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getLast50Results()
                .catch { error ->
                    _uiState.value = HistoryUiState.Error(
                        error.message ?: "Failed to load history"
                    )
                }
                .collect { items ->
                    _uiState.value = if (items.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(items)
                    }
                }
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearHistory()
                _uiState.value = HistoryUiState.Empty
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(
                    "Failed to clear history: ${e.message}"
                )
            }
        }
    }
}
```

### Room Database Schema (From Story 4.2)

**RecognitionHistoryEntity:**

From [architecture.md#Decision 1: Data Persistence Strategy]:

```kotlin
// app/src/main/java/com/visionfocus/data/local/entity/RecognitionHistoryEntity.kt
@Entity(tableName = "recognition_history")
data class RecognitionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "object_label") val objectLabel: String,
    @ColumnInfo(name = "confidence") val confidence: Float,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "verbosity_mode") val verbosityMode: String,
    @ColumnInfo(name = "detail_text") val detailText: String? = null
)
```

**RecognitionHistoryDao:**

```kotlin
// app/src/main/java/com/visionfocus/data/local/dao/RecognitionHistoryDao.kt
@Dao
interface RecognitionHistoryDao {
    @Query("SELECT * FROM recognition_history ORDER BY timestamp DESC LIMIT 50")
    fun getLast50Results(): Flow<List<RecognitionHistoryEntity>>
    
    @Insert
    suspend fun insert(entry: RecognitionHistoryEntity)
    
    @Query("DELETE FROM recognition_history")
    suspend fun clearAll()
    
    @Query("DELETE FROM recognition_history WHERE id NOT IN (SELECT id FROM recognition_history ORDER BY timestamp DESC LIMIT 50)")
    suspend fun pruneOldEntries()
}
```

**RecognitionHistoryRepository:**

```kotlin
// app/src/main/java/com/visionfocus/data/repository/RecognitionHistoryRepository.kt
interface RecognitionHistoryRepository {
    fun getLast50Results(): Flow<List<RecognitionHistoryEntity>>
    suspend fun saveRecognition(
        label: String,
        confidence: Float,
        verbosityMode: String,
        detailText: String?
    )
    suspend fun clearHistory()
}

class RecognitionHistoryRepositoryImpl @Inject constructor(
    private val dao: RecognitionHistoryDao
) : RecognitionHistoryRepository {
    
    override fun getLast50Results(): Flow<List<RecognitionHistoryEntity>> {
        return dao.getLast50Results()
    }
    
    override suspend fun saveRecognition(
        label: String,
        confidence: Float,
        verbosityMode: String,
        detailText: String?
    ) {
        val entity = RecognitionHistoryEntity(
            objectLabel = label,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            verbosityMode = verbosityMode,
            detailText = detailText
        )
        dao.insert(entity)
        dao.pruneOldEntries() // Maintain 50-item limit
    }
    
    override suspend fun clearHistory() {
        dao.clearAll()
    }
}
```

### Accessibility Requirements (CRITICAL)

**From Story 2.7 - Complete TalkBack Navigation Patterns:**

Story 2.7 established the VisionFocus accessibility baseline. **ALL patterns must be followed:**

1. **Content Descriptions:**
   - Every interactive element MUST have contentDescription
   - Format: "[object] [state/confidence] [timestamp]" (10-20 words)
   - Avoid redundancy: Don't say "button" - TalkBack announces element type
   - Example: "Chair, high confidence, December 24, 2025 at 3:45 PM"

2. **Focus Order:**
   - Follow visual layout: top-to-bottom, left-to-right
   - RecyclerView items: natural sequential order (newest first)
   - Test with TalkBack swipe right/left gestures
   - Empty state should be first focusable element when no items

3. **Touch Targets:**
   - Minimum: 48×48 dp (WCAG 2.1 AA requirement)
   - Preferred: 56×56 dp for primary actions (FAB pattern from Story 2.3)
   - Validate programmatically in accessibility tests

4. **Announcements:**
   - Use `view.announceForAccessibility(message)` for non-critical updates
   - Use `sendAccessibilityEvent(TYPE_ANNOUNCEMENT)` for critical state changes
   - Announcement when history cleared: "Recognition history cleared"
   - Empty state: Static text, not dynamic announcement

5. **Focus Restoration:**
   - Save focus state in onPause() when TalkBack enabled
   - Restore focus in onResume() to last-focused item or first item
   - Handle interruptions: phone calls, notifications, navigation

**Reference Document:**

See `docs/AccessibilityGuidelines.md` (created in Story 2.7) for:
- Complete TalkBack implementation checklist
- Testing procedures (automated + manual)
- Common pitfalls and solutions
- WCAG 2.1 AA compliance mapping

### UI Layout Specifications

**From Architecture Document - Material Design 3 Configuration:**

Story 1.1 established Material Design 3 theming with accessibility requirements:

**Colors:**
- Dark theme default: Background #121212
- High-contrast mode: 7:1 contrast ratio minimum (21:1 achieved in Story 2.3)
- Text colors: Primary (#FFFFFF), Secondary (#B0B0B0)

**Typography:**
- Font: Roboto
- Increased base sizes: Body 20sp (vs default 16sp)
- Line height: 1.5× for readability

**Touch Targets:**
- Interactive elements: 48×48 dp minimum, 56×56 dp preferred
- List items: Full-width touchable area, minimum 56 dp height
- FAB: 56×56 dp (established pattern from Story 2.3)

**Spacing:**
- Item padding: 16dp horizontal, 12dp vertical
- List item margin: 8dp between items
- Screen padding: 16dp edges

### Project File Structure

**Expected file locations (follow established patterns from Epic 1-2):**

```
app/src/main/java/com/visionfocus/
├── ui/
│   └── history/                           # NEW for Story 4.3
│       ├── HistoryFragment.kt
│       ├── HistoryViewModel.kt
│       └── adapter/
│           └── RecognitionHistoryAdapter.kt
│
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   └── RecognitionHistoryEntity.kt  # From Story 4.2
│   │   └── dao/
│   │       └── RecognitionHistoryDao.kt     # From Story 4.2
│   └── repository/
│       └── RecognitionHistoryRepository.kt  # From Story 4.2
│
└── di/
    └── modules/
        └── DataModule.kt                    # Already provides repository bindings

app/src/main/res/
├── layout/
│   ├── fragment_history.xml               # NEW
│   └── item_recognition_history.xml       # NEW
│
├── navigation/
│   └── nav_graph.xml                      # Add History destination
│
└── values/
    └── strings.xml                        # Add history-related strings

app/src/test/java/com/visionfocus/
└── ui/history/
    └── HistoryViewModelTest.kt           # NEW

app/src/androidTest/java/com/visionfocus/
└── accessibility/
    └── HistoryAccessibilityTest.kt       # NEW
```

### Integration with Existing Components

**TTSManager Integration (Story 2.2):**

When user double-taps history item, announce full details via TTS:

```kotlin
// HistoryFragment.kt
@Inject
lateinit var ttsManager: TTSManager

@Inject
lateinit var ttsFormatter: TTSPhraseFormatter  // From Story 2.2

private fun announceHistoryItem(item: RecognitionHistoryEntity) {
    val announcement = ttsFormatter.formatRecognitionResult(
        label = item.objectLabel,
        confidence = item.confidence,
        verbosityMode = item.verbosityMode,
        detailText = item.detailText
    )
    ttsManager.speak(announcement)
}
```

**Navigation Integration (Epic 3 - Voice Commands):**

Voice command "History" should navigate to HistoryFragment.

**IF Epic 3 (Voice Commands) is NOT yet complete:**
- Implement basic button navigation from RecognitionFragment for now
- Add voice command integration in Epic 3 Story 3.2 (Core Voice Command Processing Engine)

**IF Epic 3 IS complete:**
- Register "History" command in voice command processor
- Navigate using Navigation Component or FragmentManager

### Testing Requirements

**Unit Tests (JUnit 4 + Mockito):**

```kotlin
// HistoryViewModelTest.kt
@Test
fun `initial state is Loading`() {
    val viewModel = HistoryViewModel(mockRepository)
    assertEquals(HistoryUiState.Loading, viewModel.uiState.value)
}

@Test
fun `successful history load emits Success state with items`() = runTest {
    val mockItems = listOf(
        RecognitionHistoryEntity(id = 1, objectLabel = "Chair", confidence = 0.9f, timestamp = 123L, verbosityMode = "standard"),
        RecognitionHistoryEntity(id = 2, objectLabel = "Table", confidence = 0.85f, timestamp = 456L, verbosityMode = "brief")
    )
    
    whenever(mockRepository.getLast50Results()).thenReturn(flowOf(mockItems))
    
    val viewModel = HistoryViewModel(mockRepository)
    
    val states = mutableListOf<HistoryUiState>()
    val job = launch(UnconfinedTestDispatcher()) {
        viewModel.uiState.collect { states.add(it) }
    }
    
    advanceUntilIdle()
    
    assertTrue(states[0] is HistoryUiState.Loading)
    assertTrue(states[1] is HistoryUiState.Success)
    assertEquals(2, (states[1] as HistoryUiState.Success).items.size)
    
    job.cancel()
}

@Test
fun `empty history load emits Empty state`() = runTest {
    whenever(mockRepository.getLast50Results()).thenReturn(flowOf(emptyList()))
    
    val viewModel = HistoryViewModel(mockRepository)
    
    val states = mutableListOf<HistoryUiState>()
    val job = launch(UnconfinedTestDispatcher()) {
        viewModel.uiState.collect { states.add(it) }
    }
    
    advanceUntilIdle()
    
    assertTrue(states.last() is HistoryUiState.Empty)
    
    job.cancel()
}

@Test
fun `clearAllHistory calls repository and updates state to Empty`() = runTest {
    whenever(mockRepository.clearHistory()).thenReturn(Unit)
    
    val viewModel = HistoryViewModel(mockRepository)
    viewModel.clearAllHistory()
    
    advanceUntilIdle()
    
    verify(mockRepository).clearHistory()
    assertTrue(viewModel.uiState.value is HistoryUiState.Empty)
}
```

**Instrumentation Tests (Espresso + Accessibility Scanner):**

```kotlin
// HistoryAccessibilityTest.kt
@HiltAndroidTest
class HistoryAccessibilityTest : BaseAccessibilityTest() {
    
    @Before
    fun setup() {
        AccessibilityChecks.enable()
    }
    
    @Test
    fun `history items have proper contentDescription`() {
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // Verify first item has contentDescription
        onView(withId(R.id.historyRecyclerView))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        
        // Check contentDescription matches expected format
        onView(withText(containsString("confidence")))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun `empty state has TalkBack announcement`() {
        // Launch with empty history
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // Verify empty state message
        onView(withText(containsString("No recognition history yet")))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(containsString("No recognition history yet"))))
    }
    
    @Test
    fun `clear history button has 56x56 dp touch target`() {
        launchFragmentInHiltContainer<HistoryFragment>()
        
        onView(withId(R.id.clearHistoryFab))
            .check(matches(isDisplayed()))
            .check(matches(withMinimumTouchTargetSize(56))) // Custom matcher
    }
    
    @Test
    fun `RecyclerView items pass Accessibility Scanner`() {
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // AccessibilityChecks.enable() in @Before ensures zero errors
        onView(withId(R.id.historyRecyclerView))
            .check(matches(isDisplayed()))
    }
}
```

### Known Patterns from Previous Stories

**FAB Pattern (Story 2.3):**
- Size: 56×56 dp
- Position: Bottom-right corner with 16dp margin
- contentDescription: Action-oriented, no "button" suffix
- Haptic feedback: Medium intensity on tap

**Dialog Pattern (Story 2.7):**
- MaterialAlertDialogBuilder for consistency
- Title and message with TalkBack labels
- Button touch targets: 48×48 dp minimum
- Announcement after dismissal

**ViewModel Pattern (Story 2.3, 2.4, 5.1, 5.3):**
- @HiltViewModel with constructor injection
- StateFlow for UI state
- SharedFlow for one-time events
- viewModelScope for coroutines

**Testing Pattern (Story 1.5, 2.7):**
- BaseAccessibilityTest.kt for common accessibility test infrastructure
- AccessibilityChecks.enable() in @Before setup
- Zero tolerance for accessibility errors
- Manual TalkBack testing required alongside automated tests

### References

**Source Documents:**
- [epics.md#Epic 4 - Story 4.3: Recognition History Review UI] - User story and acceptance criteria
- [architecture.md#Decision 1: Data Persistence Strategy] - Room database schema and patterns
- [architecture.md#Decision 2: State Management Pattern] - StateFlow + ViewModel patterns
- [architecture.md#Decision 3: UI Architecture Approach] - XML layouts + View Binding
- [docs/AccessibilityGuidelines.md] - Complete TalkBack implementation guide (Story 2.7)

**Related Stories:**
- Story 1.4: Room Database Foundation - Database infrastructure
- Story 1.5: Camera Permissions & TalkBack Testing Framework - BaseAccessibilityTest
- Story 2.2: High-Confidence Detection Filtering & TTS Announcement - TTSManager and TTSFormatter
- Story 2.3: Recognition FAB with TalkBack Semantic Annotations - FAB pattern, haptic feedback
- Story 2.7: Complete TalkBack Navigation for Primary Flow - Accessibility baseline
- Story 4.2: Recognition History Storage with Room Database - **REQUIRED DEPENDENCY**

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (via GitHub Copilot)

### Debug Log References

### Completion Notes List

### File List

<!-- Will be populated during implementation -->
