---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/prd.md
  - _bmad-output/analysis/brainstorming-session-2025-12-24.md
  - _bmad-output/project-planning-artifacts/product-brief-VisionFocus-2025-12-24.md
  - docs/Chapter_01_FrontMatter_Abstract_Glossary.md
  - docs/Chapter_01_Introduction.md
  - docs/Chapter_02_Literature_Review.md
  - docs/Chapter_03_Technology.md
  - docs/Chapter_04_Design.md
  - docs/Chapter_05_Methodology.md
  - docs/Chapter_06_Requirements_Analysis.md
  - docs/Chapter_07_Implementation.md
  - docs/Chapter_08_Testing_Evaluation.md
  - docs/Chapter_09_Ethics_Legal_Professional.md
  - docs/Chapter_10_Results_Analysis.md
  - docs/Chapter_11_Discussion.md
  - docs/Chapter_12_Conclusion.md
  - docs/Chapter_13_References.md
  - docs/Chapter_14_Appendices.md
  - docs/COMPLETE_TOC_SAMPLE_1.md
  - docs/List_of_Figures.md
  - docs/List_of_Tables.md
documentCounts:
  prd: 1
  epics: 0
  ux: 0
  research: 0
  brainstorming: 1
  productBrief: 1
  projectDocs: 17
hasProjectContext: false
workflowType: 'architecture'
project_name: 'VisionFocus'
user_name: 'Allan'
date: '2025-12-24'
starterApproach: 'minimal-android-research-validated'
developmentIDE: 'VS Code'
---

# Architecture Decision Document - VisionFocus

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

VisionFocus defines **62 functional requirements** organized into 10 distinct categories:

1. **Object Recognition Capabilities (FR1-FR8):** Real-time camera-based object identification with 80+ COCO categories, confidence-aware announcements (high/medium/low), multiple verbosity modes (brief/standard/detailed), continuous scanning, and recognition history (last 50 results)

2. **Navigation Capabilities (FR9-FR16):** GPS-based turn-by-turn guidance with 5-7 second advance warnings, route recalculation on deviation (>20m threshold), saved locations, offline map support with automatic GPS↔offline mode switching

3. **Voice Interaction Capabilities (FR17-FR20):** 15 core voice commands (Recognize, Navigate, Repeat, Cancel, Settings, etc.) with cross-environment recognition and immediate audio confirmation

4. **Accessibility & UI Interaction (FR21-FR28):** Complete TalkBack semantic annotations, logical focus order, 48×48 dp minimum touch targets, high-contrast mode, large text mode, Bluetooth audio routing, haptic feedback

5. **Text-to-Speech Capabilities (FR29-FR33):** Adjustable speech rate (0.5×-2.0×), voice selection, intelligent audio prioritization (navigation > recognition), confidence-aware phrasing

6. **Offline & Data Management (FR34-FR40):** 100% offline object recognition, embedded ~4MB TFLite model, local storage for preferences/history/locations with encryption, pre-cached map tiles

7. **Privacy & Security (FR41-FR45):** On-device inference with zero image uploads (validated via network traffic analysis), local data encryption (Android Keystore), user consent for network operations

8. **Settings & Personalization (FR46-FR52):** User-configurable speech rate, verbosity, high-contrast mode, haptic intensity, saved locations management, TTS voice selection—all persisted across restarts

9. **Permissions & Device Integration (FR53-FR58):** Runtime permission management (camera, location, microphone, Bluetooth) with graceful degradation and clear explanations

10. **Onboarding & First-Run Experience (FR59-FR62):** Optional tutorial demonstrating core voice commands, permission setup guidance, TalkBack validation, skippable for immediate access

**Non-Functional Requirements:**

VisionFocus defines **research-validated NFRs** across six critical dimensions:

1. **Performance:** ≤320ms average recognition latency (validated), ≤500ms max, ≥15 FPS camera processing; 5-7 second turn warnings; ≤200ms TTS initiation; ≤12% battery drain/hour (validated 12.3%); ≤25MB app size; ≤150MB runtime memory

2. **Reliability:** <0.1% crash rate, >99.9% launch success; ≥75% recognition accuracy threshold (validated 83.2%); ≤10% false positive rate; ≥85% voice command accuracy (validated 92.1%); zero data loss for preferences/locations

3. **Security & Privacy:** 100% on-device inference with zero image uploads (network traffic validated); Android Keystore encryption for local data; HTTPS/TLS 1.2+ for all network traffic; user consent required for network operations; no analytics without explicit consent

4. **Accessibility:** WCAG 2.1 AA compliance (automated + manual testing); 100% TalkBack operability (validated); 48×48 dp touch targets (validated); 7:1 contrast ratio in high-contrast mode; 150% large text scaling; adjustable TTS rate and haptic intensity

5. **Usability:** ≤3 minute first-task completion; <5 minute onboarding; ≤3 attempts average for primary workflows; SUS score ≥68 threshold, target ≥75 (validated 78.5); ≥85% task success in UAT (validated 91.3%); ≥80% recommendation intent

6. **Compatibility:** Android API 26+ (Oreo) minimum, API 34+ target; TFLite 2.x+; mid-range device support (validated); major OEM compatibility (Samsung, Pixel, OnePlus, Xiaomi); TalkBack 9.1+; Google Maps Directions API + offline maps

### Scale & Complexity Assessment

**Project Complexity: Medium-High**

**Complexity Drivers:**
- **Real-time AI inference:** Sub-500ms latency constraint with on-device TFLite processing requires careful optimization; validated ~320ms average on mid-range hardware
- **Safety-critical use case:** User mistakes can cause physical harm (missed obstacles, incorrect navigation); demands >99.9% reliability and honest uncertainty communication
- **Multi-modal integration:** Simultaneously manages camera input, GPS positioning, TTS output, voice input, haptic feedback—requires robust state management and audio priority queuing
- **Strict accessibility compliance:** WCAG 2.1 AA mandatory, not optional; TalkBack-first design with semantic annotations throughout; multiple feedback modalities for diverse user needs
- **Privacy-by-design architecture:** Zero-trust model for image data; network traffic analysis validation requirement; local encryption for sensitive data
- **Offline-first operation:** Core functionality without connectivity; graceful mode switching; embedded models and cached data management
- **Research validation burden:** All metrics must be reproducible and defensible with evidence; dissertation-grade rigor

**Primary Technical Domain: Native Android Mobile Application**

- **Language:** Kotlin
- **Architecture Pattern:** Clean Architecture + MVVM (validated in research implementation)
- **Key Technologies:** TensorFlow Lite (INT8 quantized models), Android TalkBack APIs, GPS/FusedLocationProvider, Google Maps Directions API, Android Keystore, BLE (Phase 2)
- **Development Complexity:** Native Android chosen over cross-platform for maximum TalkBack fidelity and on-device AI performance optimization

**Estimated Architectural Components: 8-12 Major Modules**

Based on functional domain analysis:
1. **Recognition Module** (camera capture, TFLite inference, confidence filtering, NMS)
2. **Navigation Module** (GPS tracking, route calculation, turn-by-turn guidance, offline maps)
3. **Voice Command Module** (speech recognition, command parsing, execution)
4. **TTS Module** (text-to-speech synthesis, audio priority management, speech rate control)
5. **Accessibility Module** (TalkBack integration, semantic annotations, focus management)
6. **UI Module** (screens, high-contrast mode, large text, touch target sizing)
7. **Data Persistence Module** (local storage, encryption, recognition history, saved locations)
8. **Settings Module** (user preferences management, persistence)
9. **Permission Module** (runtime permission requests, graceful degradation)
10. **Audio Routing Module** (Bluetooth support, priority queue for nav vs recognition)
11. **Haptic Feedback Module** (adjustable intensity, distinct patterns)
12. **Onboarding Module** (first-run tutorial, permission setup)

### Technical Constraints & Dependencies

**Platform Constraints:**
- **Minimum SDK API 26+ (Android 8.0 Oreo):** Required for TensorFlow Lite optimization support and modern accessibility APIs
- **Mid-range device performance target:** Architecture must optimize for non-flagship hardware (validated on mid-range test devices)
- **Battery constraints:** ≤12% drain/hour maximum for continuous operation; requires careful background processing management
- **Memory constraints:** ≤150MB runtime, ≤25MB app size including embedded model; demands efficient resource management

**Third-Party Dependencies:**
- **TensorFlow Lite 2.x+:** Embedded quantized model (~4MB INT8); NNAPI hardware acceleration optional; version compatibility critical for inference performance
- **Google Maps Directions API:** Required for outdoor navigation; offline maps support essential; graceful degradation when API unavailable
- **Android TalkBack 9.1+:** Core accessibility dependency; semantic API compatibility across versions; no vendor-specific TalkBack variants
- **Google Text-to-Speech:** Android system TTS with adjustable rate/voice; must handle various TTS engines across OEMs

**Research Validation Constraints:**
- All architectural decisions must be defensible with evidence from dissertation research
- Performance metrics (latency, accuracy, battery) must be reproducible
- Network traffic analysis validation required for privacy claims
- UAT results (15 visually impaired participants, 91.3% task success, SUS 78.5) inform UX architectural decisions

**Privacy & Security Constraints:**
- **Zero-trust image data policy:** No images leave device under any circumstances; architecture must enforce at compile-time where possible
- **Network traffic validation:** All network operations auditable and verifiable as zero-image-transmission
- **Local encryption mandatory:** Android Keystore required for recognition history, saved locations, preferences
- **No telemetry without consent:** Analytics opt-in only; default state is zero data collection

**Offline-First Constraints:**
- Core recognition must function with zero connectivity (airplane mode viable)
- Embedded model bundle in APK (no model downloads post-install)
- Navigation must gracefully degrade with pre-cached maps
- All settings/preferences/history accessible offline

### Cross-Cutting Concerns Identified

**1. Performance Optimization (Critical)**
- **Real-time inference latency:** ≤500ms constraint affects architecture at every layer; requires optimized data flow from camera → TFLite → confidence filter → TTS
- **Battery efficiency:** 6-hour continuous use target demands careful lifecycle management, background processing limits, and sensor polling optimization
- **Memory management:** No leaks over 8+ hour sessions; efficient bitmap handling for camera frames; model loading strategy
- **UI responsiveness:** TalkBack users require <300ms feedback; any blocking operations break accessibility contract

**2. Accessibility Compliance (Critical)**
- **TalkBack integration pervasive:** Every UI element requires semantic annotation; affects component design across all modules
- **Focus management:** Logical focus order and restoration after interruptions (calls, notifications) spans all screens
- **Multiple feedback modalities:** Voice, haptic, visual must coordinate; state changes trigger appropriate feedback based on user preferences
- **Touch target sizing:** 48×48 dp minimum enforced in design system; affects all interactive elements

**3. Privacy & Security (Critical)**
- **Image data isolation:** Camera frames never serialized to disk or network; architecture must enforce boundaries between recognition module and data persistence
- **Local data encryption:** Android Keystore integration spans all modules that store sensitive data (recognition history, saved locations, preferences)
- **Permission management:** Runtime permission state affects multiple modules (camera, location, mic, Bluetooth); graceful degradation logic distributed across architecture
- **Network consent:** User consent state gates all network operations; requires centralized consent management

**4. Offline-First Data Management (Critical)**
- **Embedded model strategy:** TFLite model bundled in APK affects app size budget and update strategy
- **Local caching:** Recognition history (50 results), saved locations, offline maps, user preferences—all require local persistence with encryption
- **Mode switching logic:** GPS ↔ offline navigation transitions must be seamless; affects navigation module state management
- **Graceful degradation:** Missing permissions or connectivity must not crash app; affects error handling across all modules

**5. Audio Routing & Priority Management (High Priority)**
- **Multi-source audio:** Navigation announcements, recognition results, voice command confirmations, TTS feedback—requires priority queue
- **Bluetooth audio routing:** External audio devices affect latency and user experience; must handle device connect/disconnect gracefully
- **Interrupt handling:** Phone calls, notifications, other apps' audio must pause/resume VisionFocus appropriately
- **Adjustable TTS:** Speech rate (0.5×-2.0×) and voice selection affects TTS module configuration

**6. State Management & Lifecycle (High Priority)**
- **Complex state machine:** Recognition mode, navigation mode, voice listening mode, settings screens—each with entry/exit logic
- **Android lifecycle complexity:** App pause/resume, configuration changes, memory pressure—must maintain state consistency
- **Background processing limits:** Navigation announcements may need foreground service; affects battery and UX
- **Persistence across restarts:** User preferences, saved locations, recognition history must survive app termination

**7. Testing & Validation Strategy (High Priority)**
- **Accessibility testing:** Automated (Accessibility Scanner) + manual with TalkBack users; affects CI/CD pipeline
- **Performance benchmarking:** Latency, battery, memory monitoring in automated tests; regression detection critical
- **Network traffic validation:** Privacy claims require repeatable traffic analysis tests
- **UAT with target users:** Visually impaired participants required for validation; affects release criteria

**8. Error Handling & User Trust (High Priority)**
- **Confidence-aware feedback:** Recognition confidence (high/medium/low) displayed honestly; affects TTS phrasing and user trust
- **Graceful error messages:** Clear recovery guidance when things fail (GPS lost, permission denied, low battery)
- **Safety-critical reliability:** Mistakes can cause harm; requires defensive programming and extensive error scenario testing
- **Transparent uncertainty:** "Not sure, possibly a chair..." phrasing builds trust; affects recognition result presentation logic

## Starter Template & Foundation Decision

### Primary Technology Domain

**Native Android Mobile Application** - Assistive technology with specialized requirements (on-device AI, strict accessibility compliance, privacy-first architecture, offline-first operation)

### Development Environment Context

**Primary IDE:** VS Code (not Android Studio)
- Requires manual Android SDK configuration and Gradle integration
- Command-line driven development workflow
- Manual setup for Kotlin language support and debugging

### Starter Options Evaluated

**Option 1: Now in Android (Google's Official Reference Architecture)**
- **Repository:** https://github.com/android/nowinandroid
- **Status:** Actively maintained (20.2k stars, updated 5 days ago)
- **Provides:** Clean Architecture, Jetpack Compose, Hilt DI, Material 3, comprehensive modularization (12+ modules), testing infrastructure, baseline profiles
- **Considerations:**
  - Heavy focus on Jetpack Compose (may be overkill for accessibility-first TalkBack UI)
  - Complex modularization requires adaptation effort
  - Content/news app domain differs significantly from assistive AI domain
  - Would require removing/replacing components for TFLite, camera, GPS, specialized accessibility patterns
  - Better suited for Android Studio workflow than VS Code

**Option 2: Minimal Android Project with Research-Validated Architecture**
- **Approach:** Start from minimal Android project structure and implement validated research architecture directly
- **Rationale:**
  - VisionFocus has 17 dissertation chapters documenting validated architecture (Clean Architecture + MVVM)
  - Research implementation already proved patterns work (83.2% accuracy, 320ms latency, SUS 78.5)
  - Specialized requirements (TFLite, TalkBack, privacy-first, offline-first) not represented in standard starters
  - VS Code workflow benefits from clean, controlled setup
  - Dissertation-to-implementation fidelity critical for academic integrity
  - Implementation freedom to build exactly what research validated

**Option 3: Hybrid Approach**
- Start with minimal Android project
- Cherry-pick specific best practices from Now in Android (Hilt setup, baseline profiles, testing patterns)
- Implement VisionFocus-specific modules from research architecture

### Selected Approach: Minimal Android Project with Research-Validated Architecture

**Decision:** Start with a minimal Android project and implement the research-validated architecture directly, with selective adoption of best practices from the Android ecosystem.

**Rationale for Selection:**

1. **Research-Complete Status:** 17 dissertation chapters document validated architecture, implementation patterns, and performance benchmarks. The hard validation work is already done—implementation should match proven research.

2. **Dissertation-to-Implementation Fidelity:** Academic integrity requires that the production implementation matches the research architecture that was validated with 15 visually impaired participants (91.3% task success, SUS 78.5).

3. **Specialized Requirements Mismatch:** VisionFocus needs (TFLite inference, strict TalkBack integration, privacy-by-design, offline-first) are not represented in standard Android starters. Adaptation overhead would exceed clean implementation effort.

4. **VS Code Workflow Compatibility:** Minimal setup provides better control over build configuration and dependency management for non-Android Studio environments.

5. **Implementation Efficiency:** Build only what's needed based on 62 validated functional requirements, avoiding time spent removing unused starter components.

6. **Architecture Validation Integrity:** Performance metrics (320ms latency, 12.3% battery/hour, 83.2% accuracy) are tied to specific architectural choices. Wholesale adoption of different patterns might invalidate research findings.

### Implementation Foundation

**Project Structure (Research-Validated Modules):**

```
VisionFocus/
├── app/
│   └── src/main/
│       ├── kotlin/com/visionfocus/
│       │   ├── recognition/           # FR1-FR8: Object Recognition Module
│       │   │   ├── camera/           # Camera capture, frame processing
│       │   │   ├── inference/        # TFLite model loading, inference engine
│       │   │   ├── processing/       # Confidence filtering, NMS, result formatting
│       │   │   └── models/           # Recognition result data classes
│       │   │
│       │   ├── navigation/            # FR9-FR16: Navigation Module
│       │   │   ├── gps/              # FusedLocationProvider, GPS tracking
│       │   │   ├── routing/          # Route calculation, recalculation logic
│       │   │   ├── guidance/         # Turn-by-turn announcements, advance warnings
│       │   │   └── offline/          # Offline map management, cached routes
│       │   │
│       │   ├── voice/                 # FR17-FR20: Voice Command Module
│       │   │   ├── recognition/      # Speech recognition, command parsing
│       │   │   ├── commands/         # 15 core command implementations
│       │   │   └── feedback/         # Voice command confirmation logic
│       │   │
│       │   ├── tts/                   # FR29-FR33: Text-to-Speech Module
│       │   │   ├── engine/           # TTS engine integration, speech synthesis
│       │   │   ├── priority/         # Audio priority queue (nav > recognition)
│       │   │   └── formatter/        # Confidence-aware phrasing logic
│       │   │
│       │   ├── accessibility/         # FR21-FR28: Accessibility Module
│       │   │   ├── talkback/         # TalkBack semantic annotations
│       │   │   ├── focus/            # Logical focus order management
│       │   │   ├── haptic/           # Haptic feedback patterns, intensity control
│       │   │   └── contrast/         # High-contrast mode, large text support
│       │   │
│       │   ├── data/                  # FR34-FR40: Data Persistence Module
│       │   │   ├── local/            # Local database (recognition history, saved locations)
│       │   │   ├── encryption/       # Android Keystore integration
│       │   │   ├── preferences/      # User preferences storage
│       │   │   └── repository/       # Data access layer (Clean Architecture)
│       │   │
│       │   ├── ui/                    # User Interface Module
│       │   │   ├── screens/          # Activity/Fragment screens
│       │   │   ├── components/       # Reusable UI components (48×48 dp targets)
│       │   │   ├── theme/            # High-contrast theme, large text scaling
│       │   │   └── viewmodels/       # MVVM ViewModels
│       │   │
│       │   ├── settings/              # FR46-FR52: Settings Module
│       │   │   ├── preferences/      # Settings management logic
│       │   │   ├── locations/        # Saved locations CRUD operations
│       │   │   └── ui/               # Settings screens
│       │   │
│       │   ├── permissions/           # FR53-FR58: Permission Module
│       │   │   ├── manager/          # Runtime permission requests
│       │   │   └── degradation/      # Graceful degradation logic
│       │   │
│       │   ├── onboarding/            # FR59-FR62: Onboarding Module
│       │   │   ├── tutorial/         # Voice command demonstration
│       │   │   └── setup/            # Permission setup flow
│       │   │
│       │   ├── audio/                 # Audio Routing Module (Cross-cutting)
│       │   │   ├── routing/          # Bluetooth audio device management
│       │   │   └── priority/         # Multi-source audio priority queue
│       │   │
│       │   └── di/                    # Dependency Injection (Hilt)
│       │       └── modules/          # Hilt modules for each layer
│       │
│       ├── res/                       # Android resources
│       │   ├── layout/               # XML layouts (accessibility-optimized)
│       │   ├── values/               # Strings, dimensions, colors, themes
│       │   ├── xml/                  # Preferences, network security config
│       │   └── raw/                  # Embedded TFLite model (~4MB)
│       │
│       └── AndroidManifest.xml       # App manifest, permissions
│
├── build.gradle.kts                  # App-level Gradle build configuration
├── settings.gradle.kts               # Project-level settings
└── gradle.properties                 # Gradle properties
```

**Core Dependencies (Research-Validated):**

```kotlin
// build.gradle.kts (app level)
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Architecture Components (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // TensorFlow Lite (Validated version from research)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // Google Maps & Location Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Room Database (Local persistence with encryption)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Security & Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coroutines (Async operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.5.1")
}
```

**Architecture Pattern: Clean Architecture + MVVM**

Validated in research implementation with proven performance characteristics:

**Layer 1: Presentation (UI + ViewModels)**
- Activities/Fragments with TalkBack semantic annotations
- ViewModels managing UI state and user interactions
- High-contrast themes, large text support, 48×48 dp touch targets

**Layer 2: Domain (Use Cases)**
- Business logic encapsulation for each feature
- Recognition use cases: CaptureFrameUseCase, InferObjectsUseCase, FilterConfidenceUseCase
- Navigation use cases: CalculateRouteUseCase, MonitorDeviationUseCase, AnnounceNavigationUseCase
- Voice use cases: RecognizeCommandUseCase, ExecuteCommandUseCase

**Layer 3: Data (Repositories + Data Sources)**
- Repository pattern for data access abstraction
- Local data sources: Room database, SharedPreferences (encrypted), TFLite model loader
- Remote data sources: Google Maps Directions API (optional, with consent)
- Offline-first strategy with graceful degradation

**VS Code Development Setup:**

```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "kotlin.languageServer.enabled": true,
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true
  }
}
```

**Build & Run Commands (Terminal-based):**

```bash
# Build debug variant
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew testDebug

# Generate code coverage
./gradlew jacocoTestReport

# Check accessibility
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=androidx.test.espresso.accessibility.AccessibilityChecks
```

**Cherry-Picked Best Practices from Android Ecosystem:**

1. **Hilt Dependency Injection:** Industry standard, simplifies testing and modularity
2. **Baseline Profiles:** Performance optimization for app startup and critical paths
3. **Screenshot Testing:** Verify UI rendering (especially important for accessibility)
4. **Network Security Config:** Enforce HTTPS/TLS 1.2+ requirements
5. **ProGuard/R8 Rules:** Code shrinking and obfuscation for release builds

**What This Approach Enables:**

✅ **Direct implementation of validated research architecture** without adaptation overhead  
✅ **VS Code compatibility** with manual Android SDK and Gradle configuration  
✅ **Dissertation fidelity** - production code matches academically validated patterns  
✅ **Clean, focused codebase** - only what's needed for 62 functional requirements  
✅ **Performance integrity** - maintain validated metrics (320ms latency, 12.3% battery/hour)  
✅ **Flexibility** - add best practices selectively as needed without starter lock-in  

**Note for Implementation:** Project initialization should be the first implementation story, setting up the minimal Android project structure with core dependencies before implementing feature modules.

## Core Architectural Decisions

### Decision 1: Data Architecture & Local Persistence

**Selected Approach: DataStore + Room (Hybrid Strategy)**

**Rationale:**
- **DataStore for Simple Preferences:** User settings (speech rate, verbosity mode, high-contrast toggle, haptic intensity, TTS voice selection) stored as typed key-value pairs with coroutine support
- **Room for Structured Data:** Recognition history (last 50 results with timestamps), saved locations (name, lat/long, metadata) stored in relational tables with type-safe DAOs
- **Clean Separation:** Preferences vs. entities align with different access patterns and persistence requirements
- **Encryption Integration:** Both support Android Keystore encryption for sensitive data (recognition history, saved locations)
- **Coroutine-Native:** Both frameworks integrate seamlessly with Kotlin coroutines for async operations
- **MVVM Compatibility:** Repository pattern abstracts DataStore/Room behind clean interfaces in data layer

**Implementation Details:**

**DataStore Configuration:**
```kotlin
// UserPreferencesRepository.kt
data class UserPreferences(
    val speechRate: Float = 1.0f,  // 0.5x-2.0x range
    val verbosityMode: VerbosityMode = VerbosityMode.STANDARD,
    val highContrastEnabled: Boolean = false,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    val ttsVoice: String = "default"
)

enum class VerbosityMode { BRIEF, STANDARD, DETAILED }
enum class HapticIntensity { OFF, LIGHT, MEDIUM, STRONG }
```

**Room Database Schema:**
```kotlin
// RecognitionHistoryEntity.kt
@Entity(tableName = "recognition_history")
data class RecognitionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectLabel: String,
    val confidence: Float,
    val timestamp: Long,
    val verbosityMode: String
)

// SavedLocationEntity.kt
@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long
)

@Dao
interface RecognitionHistoryDao {
    @Query("SELECT * FROM recognition_history ORDER BY timestamp DESC LIMIT 50")
    fun getLast50Results(): Flow<List<RecognitionHistoryEntity>>
    
    @Insert
    suspend fun insert(entry: RecognitionHistoryEntity)
    
    @Query("DELETE FROM recognition_history WHERE id NOT IN (SELECT id FROM recognition_history ORDER BY timestamp DESC LIMIT 50)")
    suspend fun pruneOldEntries()
}

@Dao
interface SavedLocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>
    
    @Insert
    suspend fun insert(location: SavedLocationEntity)
    
    @Delete
    suspend fun delete(location: SavedLocationEntity)
}
```

**Encryption Strategy:**
- DataStore: Use EncryptedSharedPreferences wrapper for sensitive preference values
- Room: Use SQLCipher for database encryption with Android Keystore-managed keys
- Recognition history and saved locations encrypted at rest per privacy requirements

### Decision 2: State Management Pattern

**Selected Approach: StateFlow + SharedFlow (Kotlin Coroutines)**

**Rationale:**
- **Complex Multi-Source State:** VisionFocus manages simultaneous state from camera frames (15+ FPS), GPS updates (1Hz), voice input events, TTS playback status, permission states
- **Coroutine-Native:** Aligns with existing async operations (TFLite inference, Room queries, network calls)
- **Backpressure Handling:** Built-in handling for high-frequency camera frames prevents memory overflow
- **Type-Safe State:** Sealed classes for UI state (Idle, Recognizing, Navigating, Listening) provide compile-time safety
- **Hot/Cold Streams:** StateFlow for current state (last value retained), SharedFlow for events (one-time actions like "navigation started")
- **Lifecycle-Aware:** Collect flows in lifecycleScope with repeatOnLifecycle for proper lifecycle management
- **Modern Best Practice:** StateFlow is the recommended approach for Android MVVM in 2024+

**Implementation Pattern:**

**ViewModel State Management:**
```kotlin
// RecognitionViewModel.kt
sealed class RecognitionUiState {
    object Idle : RecognitionUiState()
    object Recognizing : RecognitionUiState()
    data class ResultReady(val label: String, val confidence: Float) : RecognitionUiState()
    data class Error(val message: String) : RecognitionUiState()
}

class RecognitionViewModel @Inject constructor(
    private val recognitionUseCase: RecognizeObjectUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<RecognitionEvent>()
    val events: SharedFlow<RecognitionEvent> = _events.asSharedFlow()
    
    fun recognizeObject() {
        viewModelScope.launch {
            _uiState.value = RecognitionUiState.Recognizing
            
            recognitionUseCase.execute()
                .catch { error -> 
                    _uiState.value = RecognitionUiState.Error(error.message ?: "Unknown error")
                }
                .collect { result ->
                    _uiState.value = RecognitionUiState.ResultReady(result.label, result.confidence)
                    _events.emit(RecognitionEvent.AnnounceResult(result))
                }
        }
    }
}

sealed class RecognitionEvent {
    data class AnnounceResult(val result: RecognitionResult) : RecognitionEvent()
}
```

**Fragment State Collection:**
```kotlin
// RecognitionFragment.kt
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    // Collect state with lifecycle awareness
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                when (state) {
                    is RecognitionUiState.Idle -> showIdleState()
                    is RecognitionUiState.Recognizing -> showRecognizingState()
                    is RecognitionUiState.ResultReady -> showResult(state.label, state.confidence)
                    is RecognitionUiState.Error -> showError(state.message)
                }
            }
        }
    }
    
    // Collect one-time events
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is RecognitionEvent.AnnounceResult -> ttsManager.announce(event.result)
                }
            }
        }
    }
}
```

**Audio Priority Queue (Shared State):**
```kotlin
// AudioPriorityManager.kt (Shared across modules)
class AudioPriorityManager @Inject constructor() {
    
    sealed class AudioRequest {
        data class Navigation(val message: String, val priority: Int = 100) : AudioRequest()
        data class Recognition(val message: String, val priority: Int = 50) : AudioRequest()
        data class VoiceConfirmation(val message: String, val priority: Int = 75) : AudioRequest()
    }
    
    private val _audioQueue = MutableStateFlow<List<AudioRequest>>(emptyList())
    val audioQueue: StateFlow<List<AudioRequest>> = _audioQueue.asStateFlow()
    
    fun enqueue(request: AudioRequest) {
        _audioQueue.update { currentQueue ->
            (currentQueue + request).sortedByDescending { 
                when(it) {
                    is AudioRequest.Navigation -> it.priority
                    is AudioRequest.Recognition -> it.priority
                    is AudioRequest.VoiceConfirmation -> it.priority
                }
            }
        }
    }
    
    fun dequeue() {
        _audioQueue.update { it.drop(1) }
    }
}
```

### Decision 3: UI Architecture Approach

**Selected Approach: Traditional XML Layouts + View Binding**

**Rationale:**
- **Research-Validated:** Dissertation implementation used traditional Android Views with TalkBack, achieving 100% operability and 91.3% task success
- **TalkBack Maturity:** XML-based accessibility has decades of battle-testing; explicit content descriptions, focus order, and touch target sizing are well-understood
- **Accessibility-First Precision:** 48×48 dp touch target requirements, 7:1 contrast ratios, and focus order explicitly defined in XML layouts
- **Dissertation Fidelity:** Maintains consistency with validated research implementation patterns
- **High-Contrast Themes:** XML themes and styles provide mature support for runtime theme switching (high-contrast mode, large text)
- **Lower Risk:** Compose accessibility for assistive technology still maturing; XML approach reduces implementation uncertainty
- **Performance Proven:** Research validated performance metrics (320ms latency, 12.3% battery) achieved with XML Views

**Implementation Pattern:**

**View Binding Setup:**
```kotlin
// RecognitionFragment.kt
class RecognitionFragment : Fragment() {
    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAccessibility()
        observeViewModel()
    }
    
    private fun setupAccessibility() {
        // Explicit content descriptions for TalkBack
        binding.recognizeButton.contentDescription = getString(R.string.recognize_button_description)
        binding.resultTextView.contentDescription = getString(R.string.result_text_description)
        
        // Set focus order
        binding.recognizeButton.nextFocusDownId = binding.resultTextView.id
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**Accessibility-Optimized XML Layout:**
```xml
<!-- fragment_recognition.xml -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- 48x48 dp minimum touch target -->
    <Button
        android:id="@+id/recognizeButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/recognize"
        android:contentDescription="@string/recognize_button_description"
        android:accessibilityTraversalAfter="@id/resultTextView"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="16dp" />
    
    <!-- Large text support, high contrast -->
    <TextView
        android:id="@+id/resultTextView"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:textColor="?attr/colorOnSurface"
        android:contentDescription="@string/result_text_description"
        app:layout_constraintTop_toBottomOf="@id/recognizeButton"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="16dp" />
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Theme Configuration (High-Contrast Support):**
```xml
<!-- themes.xml -->
<resources>
    <!-- Standard theme -->
    <style name="Theme.VisionFocus" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnSurface">@color/on_surface</item>
        <!-- WCAG AA compliant contrast ratios -->
    </style>
    
    <!-- High-contrast theme -->
    <style name="Theme.VisionFocus.HighContrast" parent="Theme.VisionFocus">
        <item name="colorPrimary">@color/primary_high_contrast</item>
        <item name="colorOnSurface">@color/on_surface_high_contrast</item>
        <!-- 7:1 contrast ratio minimum -->
    </style>
</resources>
```

**Runtime Theme Switching:**
```kotlin
// SettingsViewModel.kt
fun setHighContrastMode(enabled: Boolean) {
    viewModelScope.launch {
        preferencesRepository.updateHighContrast(enabled)
        _themeChangeEvent.emit(if (enabled) R.style.Theme_VisionFocus_HighContrast else R.style.Theme_VisionFocus)
    }
}

// MainActivity.kt
private fun observeThemeChanges() {
    lifecycleScope.launch {
        settingsViewModel.themeChangeEvent.collect { themeResId ->
            setTheme(themeResId)
            recreate()  // Recreate activity to apply theme
        }
    }
}
```

### Decision 4: Testing Strategy

**Selected Approaches: Unit Tests, Integration Tests, Accessibility Tests**

**Rationale:**
- **Academic Validation:** Research claims require reproducible testing to validate implementation matches dissertation findings
- **Safety-Critical Application:** Mistakes can cause physical harm to users; comprehensive testing reduces risk
- **Accessibility Compliance:** WCAG 2.1 AA validation requires automated testing to prevent regressions
- **Focused Scope:** MVP prioritizes core validation (accuracy, latency, accessibility) over exhaustive performance benchmarking
- **Performance/Privacy Deferred:** Detailed performance benchmarking (Decision 4D) and privacy validation testing (Decision 4E) can be implemented in later iterations once core functionality is stable

**A. Unit Testing Strategy**

**Scope:** Business logic validation, recognition accuracy, confidence filtering, TTS phrasing correctness

**Framework:** JUnit 4 + Mockito + Kotlin Coroutines Test

**Key Test Areas:**
```kotlin
// RecognitionUseCaseTest.kt
class RecognizeObjectUseCaseTest {
    
    @Test
    fun `confidence filtering removes low confidence results`() = runTest {
        // Validate confidence threshold ~0.6 works correctly
        val results = listOf(
            RecognitionResult("chair", 0.85f),  // Keep
            RecognitionResult("table", 0.45f),  // Filter
            RecognitionResult("person", 0.72f)  // Keep
        )
        
        val filtered = recognitionUseCase.filterByConfidence(results, threshold = 0.6f)
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.confidence >= 0.6f })
    }
    
    @Test
    fun `NMS removes duplicate overlapping detections`() = runTest {
        // Validate Non-Maximum Suppression logic
        val detections = listOf(
            Detection("chair", 0.85f, BoundingBox(10, 10, 50, 50)),
            Detection("chair", 0.78f, BoundingBox(12, 12, 52, 52))  // Overlapping
        )
        
        val deduped = recognitionUseCase.applyNMS(detections, iouThreshold = 0.5f)
        
        assertEquals(1, deduped.size)
        assertEquals(0.85f, deduped[0].confidence)
    }
    
    @Test
    fun `confidence-aware phrasing generates correct announcements`() {
        val highConfidence = RecognitionResult("chair", 0.92f)
        val mediumConfidence = RecognitionResult("table", 0.68f)
        val lowConfidence = RecognitionResult("bottle", 0.52f)
        
        assertEquals("Chair, high confidence", formatter.format(highConfidence, VerbosityMode.STANDARD))
        assertEquals("Possibly a table, medium confidence", formatter.format(mediumConfidence, VerbosityMode.STANDARD))
        assertEquals("Not sure, possibly a bottle", formatter.format(lowConfidence, VerbosityMode.STANDARD))
    }
}
```

**B. Integration Testing Strategy**

**Scope:** End-to-end pipeline validation, Camera → TFLite → TTS flow, GPS → Route → Voice guidance, persistence integration

**Framework:** AndroidX Test (Instrumented Tests) + Hilt Test + Espresso

**Key Test Areas:**
```kotlin
// RecognitionPipelineIntegrationTest.kt
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecognitionPipelineIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var cameraManager: CameraManager
    
    @Inject
    lateinit var tfliteInferenceEngine: TFLiteInferenceEngine
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    @Test
    fun `camera capture to TTS announcement completes within latency target`() = runTest {
        // Validate ≤500ms end-to-end latency requirement
        val startTime = System.currentTimeMillis()
        
        // Capture frame
        val frame = cameraManager.captureFrame()
        assertNotNull(frame)
        
        // Run inference
        val results = tfliteInferenceEngine.infer(frame)
        assertTrue(results.isNotEmpty())
        
        // Filter and format
        val topResult = results.maxByOrNull { it.confidence }!!
        val announcement = formatter.format(topResult, VerbosityMode.STANDARD)
        
        // Queue TTS
        ttsManager.announce(announcement)
        
        val totalTime = System.currentTimeMillis() - startTime
        assertTrue("Latency $totalTime ms exceeds 500ms target", totalTime <= 500)
    }
    
    @Test
    fun `recognition history persists across app restarts`() = runTest {
        // Insert recognition result
        val result = RecognitionResult("chair", 0.85f)
        recognitionRepository.saveToHistory(result)
        
        // Simulate app restart (clear ViewModels)
        activityScenario.recreate()
        
        // Verify history persists
        val history = recognitionRepository.getHistory().first()
        assertTrue(history.any { it.label == "chair" })
    }
}
```

**C. Accessibility Testing Strategy**

**Scope:** TalkBack operability, semantic annotations, focus order, touch target sizing, WCAG 2.1 AA compliance

**Framework:** Espresso Accessibility + Android Accessibility Scanner + Manual TalkBack Testing

**Key Test Areas:**
```kotlin
// AccessibilityComplianceTest.kt
@RunWith(AndroidJUnit4::class)
class AccessibilityComplianceTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        // Enable accessibility checks
        AccessibilityChecks.enable()
    }
    
    @Test
    fun `all interactive elements meet 48dp touch target minimum`() {
        // Validate FR21-FR28 touch target requirement
        onView(withId(R.id.recognizeButton))
            .check(matches(isDisplayed()))
            .check { view, _ ->
                val height = view.height / view.resources.displayMetrics.density
                assertTrue("Touch target height ${height}dp < 48dp", height >= 48)
            }
    }
    
    @Test
    fun `all UI elements have content descriptions for TalkBack`() {
        // Validate semantic annotations
        onView(withId(R.id.recognizeButton))
            .check { view, _ ->
                assertNotNull("Missing content description", view.contentDescription)
                assertTrue(view.contentDescription.isNotBlank())
            }
    }
    
    @Test
    fun `focus order follows logical reading sequence`() {
        // Validate logical focus progression
        onView(withId(R.id.recognizeButton))
            .check { view, _ ->
                val nextFocus = view.findViewById<View>(view.nextFocusDownId)
                assertNotNull("Missing next focus target", nextFocus)
            }
    }
    
    @Test
    fun `high contrast theme meets 7_1 contrast ratio minimum`() {
        // Apply high-contrast theme
        activityRule.scenario.onActivity { activity ->
            activity.setTheme(R.style.Theme_VisionFocus_HighContrast)
        }
        
        // Validate contrast ratios (requires color extraction and calculation)
        // Implementation would extract background/foreground colors and calculate ratio
        // assertTrue(contrastRatio >= 7.0)
    }
}
```

**Testing Execution Strategy:**

**Local Development:**
```bash
# Run unit tests
./gradlew testDebug

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Run accessibility checks
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=androidx.test.espresso.accessibility.AccessibilityChecks
```

**CI/CD Integration:**
- Unit tests run on every commit (fast feedback)
- Integration tests run on pull requests
- Accessibility tests run nightly and before releases
- Manual TalkBack testing performed during UAT with visually impaired participants

**Test Coverage Targets:**
- Unit tests: ≥80% coverage for business logic (use cases, repositories, formatters)
- Integration tests: 100% coverage for critical paths (recognition pipeline, navigation flow, TTS system)
- Accessibility tests: 100% coverage for primary user flows (recognize, navigate, settings)

**Performance & Privacy Testing (Deferred):**

While critical for final validation, performance benchmarking (Decision 4D) and privacy validation testing (Decision 4E) are deferred to later implementation phases:

**Performance Benchmarking (Future):**
- Android Profiler for latency monitoring
- Macrobenchmark for startup performance
- Battery Historian for power consumption analysis
- Memory leak detection with LeakCanary

**Privacy Validation Testing (Future):**
- OkHttp Interceptor for network traffic capture
- mitmproxy for HTTPS traffic analysis
- Automated verification of zero image uploads
- Network security config validation

These will be implemented once core functionality is stable and validated through unit, integration, and accessibility testing.
