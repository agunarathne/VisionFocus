---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/prd.md
  - _bmad-output/architecture.md
  - _bmad-output/project-planning-artifacts/ux-design-specification.md
---

# VisionFocus - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for VisionFocus, decomposing the requirements from the PRD, Architecture, and UX Design documents into implementable stories organized by user value.

## Requirements Inventory

### Functional Requirements

**FR1:** Users can activate real-time object recognition via voice command or touch
**FR2:** System can identify 80+ COCO object categories from camera input
**FR3:** System can announce detected objects with confidence levels (high/medium/low)
**FR4:** Users can select verbosity mode for object announcements (brief/standard/detailed)
**FR5:** System can filter low-confidence detections before announcement
**FR6:** System can perform continuous scanning mode for environment mapping
**FR7:** System can recognize objects at both close range and distance
**FR8:** Users can review recognition history (last 50 results with timestamps)

**FR9:** Users can input navigation destination via voice or saved location selection
**FR10:** System can provide GPS-based turn-by-turn voice guidance
**FR11:** System can announce upcoming turns with advance warnings (5-7 seconds)
**FR12:** System can detect user deviation from route and recalculate automatically
**FR13:** Users can save frequently visited locations for quick access
**FR14:** System can navigate using pre-cached offline maps when connectivity unavailable
**FR15:** System can switch automatically between GPS and offline navigation modes
**FR16:** System can provide destination arrival confirmation

**FR17:** Users can control app via 15 core voice commands (Recognize, Navigate, Repeat, Cancel, Settings, etc.)
**FR18:** System can recognize voice commands in various acoustic environments
**FR19:** System can provide immediate audio confirmation of voice command recognition
**FR20:** Users can cancel voice operations mid-execution via voice command

**FR21:** System can provide complete TalkBack semantic annotations for all UI elements
**FR22:** System can maintain logical focus order throughout all navigation flows
**FR23:** Users can interact with all touch targets sized minimum 48×48 dp
**FR24:** Users can enable high-contrast visual mode for low vision users
**FR25:** Users can enable large text mode for UI elements
**FR26:** System can route audio output to Bluetooth earpiece/headphones for discreet use
**FR27:** Users can access all primary app functions via TalkBack screen reader
**FR28:** System can provide haptic feedback for confirmations and alerts

**FR29:** System can announce all recognition results and navigation instructions via TTS
**FR30:** Users can adjust TTS speech rate (0.5×-2.0× range)
**FR31:** Users can select preferred TTS voice
**FR32:** System can prioritize navigation announcements over recognition announcements when both occur
**FR33:** System can provide confidence-aware TTS phrasing ("Not sure, possibly a chair...")

**FR34:** System can perform all object recognition operations without internet connectivity
**FR35:** System can store TFLite model (~4MB) locally within app package
**FR36:** System can store user preferences locally (speech rate, verbosity, high-contrast mode, haptic intensity)
**FR37:** System can store recognition history locally (last 50 results with timestamps)
**FR38:** System can store saved locations with local encryption
**FR39:** System can function fully when device is offline except for live map directions
**FR40:** Users can pre-cache map tiles for known routes

**FR41:** System can perform all object recognition inference on-device (zero image uploads)
**FR42:** System can encrypt sensitive stored data (recognition history, saved locations)
**FR43:** System can request user consent before any network communication
**FR44:** Users can verify no images are transmitted via network traffic inspection
**FR45:** System can clearly indicate when network connectivity is optional vs required

**FR46:** Users can adjust speech rate preference
**FR47:** Users can select verbosity level (brief/standard/detailed)
**FR48:** Users can toggle high-contrast mode
**FR49:** Users can adjust haptic feedback intensity
**FR50:** Users can manage saved locations (add, edit, delete)
**FR51:** Users can select preferred TTS voice
**FR52:** System can persist all user preferences across app restarts

**FR53:** System can request and manage camera permission for object recognition
**FR54:** System can request and manage location permissions for GPS navigation
**FR55:** System can request and manage microphone permission for voice commands
**FR56:** System can request and manage Bluetooth permissions for audio routing
**FR57:** System can function with graceful degradation when optional permissions denied
**FR58:** System can provide clear explanations for each permission request

**FR59:** System can guide new users through initial permission setup
**FR60:** System can demonstrate core voice commands during first-run
**FR61:** Users can skip onboarding and access app immediately if desired
**FR62:** System can validate TalkBack accessibility during onboarding

### Non-Functional Requirements

**Performance Requirements:**
- Average recognition inference latency ≤320ms per recognition cycle (validated)
- Maximum latency ≤500ms for 95th percentile operations
- Real-time camera feed processing at ≥15 FPS
- Turn announcement triggered 5-7 seconds before action required
- Route recalculation completes within 3 seconds of deviation detection
- GPS location updates at minimum 1Hz frequency
- TTS initiation latency ≤200ms after recognition completion
- Voice command acknowledgment within 300ms of detection
- Smooth audio transitions without clipping or stuttering
- Continuous recognition + navigation mode: ≤12% battery drain per hour (validated 12.3%)
- Recognition-only mode: ≤8% battery drain per hour
- Navigation-only mode: ≤5% battery drain per hour
- Device remains usable for minimum 6 hours continuous operation on mid-range devices
- App installation size ≤25MB including TFLite model
- Runtime memory usage ≤150MB during peak operation
- No memory leaks over extended use sessions (8+ hours)

**Reliability Requirements:**
- Crash rate <0.1% per session (validated target)
- Successful app launch rate >99.9%
- Graceful degradation when optional features unavailable (GPS signal loss, offline maps)
- Object recognition accuracy ≥75% threshold, target ≥80% (validated 83.2%)
- False positive rate ≤10% for high-confidence announcements
- Consistent accuracy across varying lighting conditions (daylight, indoor, low-light)
- Voice command recognition accuracy ≥85% (validated 92.1%)
- Command recognition success across acoustic environments (indoor, outdoor, transit)
- Zero data loss for saved locations and user preferences
- Recognition history persists reliably across app restarts
- Settings changes take effect immediately and persist

**Security & Privacy Requirements:**
- 100% of object recognition inference performed on-device (zero image uploads)
- Validated via network traffic analysis showing zero outbound image transmission
- TFLite model embedded within app package (no external model downloads)
- Sensitive local data encrypted at rest (recognition history, saved locations, user preferences)
- Encryption using Android Keystore system
- Secure deletion of cached data when requested
- User consent required before any network communication (Maps API, app updates)
- All network traffic over HTTPS/TLS 1.2+
- Clear UI indication when network is active vs optional
- Runtime permission requests with clear explanations
- Graceful operation when optional permissions denied
- No permission overreach (only request necessary permissions)
- Network traffic analysis verification confirms zero image uploads
- Audit logs available for user review (local-only, user-controlled)
- No analytics or telemetry without explicit user consent

**Accessibility Requirements:**
- Pass all automated accessibility checks (Accessibility Scanner)
- Manual testing verification for critical flows
- Maintain compliance across all app updates
- 100% TalkBack operability for all primary user flows (validated)
- Semantic annotations for all UI elements
- Logical focus order throughout navigation hierarchies
- Focus restoration after interruptions (calls, notifications)
- Minimum touch target size: 48×48 dp for all interactive elements (validated)
- High-contrast mode with minimum 7:1 contrast ratio
- Large text mode: 150% scaling supported without layout breakage
- No reliance on color alone to convey information
- Adjustable TTS speech rate (0.5×-2.0× range)
- Voice selection for preferred TTS voice
- Clear, natural TTS phrasing (avoid robotic announcements)
- Confidence-aware language ("Not sure, possibly a chair...")
- Adjustable haptic intensity (off, light, medium, strong)
- Haptic confirmation for critical actions (save, delete, navigation start)
- Distinct haptic patterns for different event types

**Usability Requirements:**
- First-time users complete core task (recognize object OR navigate) within 3 minutes
- Onboarding tutorial demonstrates 15 core voice commands in <5 minutes
- Optional onboarding (users can skip and access immediately)
- Users complete primary workflows (recognize, navigate, adjust settings) in ≤3 attempts average
- Voice command alternative available for all primary actions
- Quick access to frequently used features (saved locations, verbosity toggle)
- Clear confirmation for destructive actions (delete saved location, clear history)
- Validation prevents invalid input (empty location names, unreachable destinations)
- Graceful error messages with recovery guidance
- System Usability Scale (SUS) score ≥68 threshold, target ≥75 (validated 78.5)
- Task success rate ≥85% in user acceptance testing (validated 91.3%)
- User recommendation intent ≥80% ("Would you recommend this app?")

**Compatibility Requirements:**
- Minimum SDK: API 26+ (Android 8.0 Oreo)
- Target SDK: Latest stable (API 34+)
- Support mid-range Android devices (validated performance target)
- Maintain compatibility across major Android OEMs (Samsung, Google Pixel, OnePlus, Xiaomi)
- Compatible with TFLite runtime version 2.x+
- Model format: .tflite (quantized INT8)
- Hardware acceleration via NNAPI when available (optional optimization)
- Google Maps Directions API compatibility
- Offline maps support (Google Maps offline areas)
- Graceful degradation when Maps API unavailable
- TalkBack 9.1+ compatibility
- Semantic compatibility with future TalkBack versions via standard Android accessibility APIs

### Additional Requirements

**From Architecture Document:**

- **Starter Template:** Architecture specifies minimal Android project with research-validated architecture (not greenfield Android Studio template)
- **Architecture Pattern:** Clean Architecture + MVVM (research-validated)
- **Development IDE:** VS Code (not Android Studio) - requires manual Android SDK configuration
- **Dependency Injection:** Hilt for Android-optimized DI
- **Data Persistence Strategy:** DataStore for simple preferences + Room for structured data (recognition history, saved locations)
- **State Management:** StateFlow + SharedFlow (Kotlin Coroutines)
- **UI Framework:** Traditional XML Layouts + View Binding (NOT Jetpack Compose) for TalkBack maturity and research validation
- **Testing Strategy:** Unit tests (JUnit 4 + Mockito), Integration tests (AndroidX Test + Espresso), Accessibility tests (Espresso Accessibility + Accessibility Scanner)
- **Project Structure:** Modularized by feature (recognition, navigation, voice, tts, accessibility, data, ui, settings, permissions, onboarding, audio, di)
- **Core Dependencies:** androidx.core, lifecycle (MVVM), Hilt, TensorFlow Lite 2.14.0, Google Maps/Location Services, Room 2.6.1, Security Crypto, Coroutines

**From UX Design Document:**

- **Design System:** Material Design 3 with customization for high-contrast and large text
- **Color Palette:** Dark theme default (#121212 background), high-contrast mode with 7:1 ratio, semantic colors (success green, warning amber, error red)
- **Typography:** Roboto font, increased base sizes (body 20sp vs default 16sp), 1.5× line height
- **Iconography:** Material Symbols (Outlined variant), sizes 24/36/48 dp
- **Touch Targets:** Minimum 48×48 dp enforced, preferred 56×56 dp
- **Animations:** 200-300ms duration, respects reduced motion preferences
- **Primary Components:** Recognition FAB (56×56 dp, bottom-right), Confidence Result Card, Bottom Navigation Bar (80dp height), Voice Command Overlay, Navigation Turn Indicator (120×120 dp)
- **Voice-First Design:** "Point, Ask, Know" pattern - volume button long-press activates recognition from any screen state (including locked screen)
- **Audio Priority Queue:** Navigation (Priority 1) > Recognition (Priority 2) > TalkBack (Priority 3) > Background audio (Priority 4)
- **Haptic Patterns:** Distinct patterns for recognition success, obstacle detected, turn approaching, error states
- **Zero-Friction Activation:** Volume button long-press (1 second) triggers recognition without unlocking phone

### FR Coverage Map

FR1 → Epic 2 - Activate real-time object recognition via voice/touch
FR2 → Epic 2 - Identify 80+ COCO object categories
FR3 → Epic 2 - Announce detected objects with confidence levels
FR4 → Epic 4 - Select verbosity mode (brief/standard/detailed)
FR5 → Epic 2 - Filter low-confidence detections
FR6 → Epic 4 - Continuous scanning mode for environment mapping
FR7 → Epic 4 - Recognize objects at close range and distance
FR8 → Epic 4 - Review recognition history (last 50 results)

FR9 → Epic 6 - Input navigation destination via voice/saved location
FR10 → Epic 6 - GPS-based turn-by-turn voice guidance
FR11 → Epic 6 - Announce upcoming turns with 5-7 second warnings
FR12 → Epic 6 - Detect deviation and recalculate automatically
FR13 → Epic 7 - Save frequently visited locations
FR14 → Epic 7 - Navigate using pre-cached offline maps
FR15 → Epic 7 - Automatically switch GPS↔offline navigation modes
FR16 → Epic 7 - Provide destination arrival confirmation

FR17 → Epic 3 - Control app via 15 core voice commands
FR18 → Epic 3 - Recognize voice commands in various environments
FR19 → Epic 3 - Provide immediate audio confirmation
FR20 → Epic 3 - Cancel voice operations mid-execution

FR21 → Epic 2 - Complete TalkBack semantic annotations
FR22 → Epic 2 - Maintain logical focus order
FR23 → Epic 2 - All touch targets minimum 48×48 dp
FR24 → Epic 2 - Enable high-contrast visual mode
FR25 → Epic 2 - Enable large text mode
FR26 → Epic 8 - Route audio to Bluetooth earpiece/headphones
FR27 → Epic 2 - Access all functions via TalkBack
FR28 → Epic 2 - Provide haptic feedback for confirmations/alerts

FR29 → Epic 2 - Announce all results via TTS
FR30 → Epic 5 - Adjust TTS speech rate (0.5×-2.0×)
FR31 → Epic 5 - Select preferred TTS voice
FR32 → Epic 8 - Prioritize navigation over recognition announcements
FR33 → Epic 8 - Confidence-aware TTS phrasing

FR34 → Epic 2 - Perform recognition without internet
FR35 → Epic 2 - Store TFLite model (~4MB) locally
FR36 → Epic 5 - Store user preferences locally
FR37 → Epic 4 - Store recognition history locally (last 50)
FR38 → Epic 7 - Store saved locations with encryption
FR39 → Epic 7 - Function fully offline except live map directions
FR40 → Epic 7 - Pre-cache map tiles for known routes

FR41 → Epic 2 - Perform inference on-device (zero uploads)
FR42 → Epic 4 - Encrypt sensitive stored data

FR43 → Epic 6 - Request user consent before network communication
FR44 → (Testing/Validation) - Verify no image transmissions
FR45 → Epic 6 - Indicate when network is optional vs required

FR46 → Epic 5 - Adjust speech rate preference
FR47 → Epic 5 - Select verbosity level
FR48 → Epic 5 - Toggle high-contrast mode
FR49 → Epic 5 - Adjust haptic feedback intensity
FR50 → Epic 7 - Manage saved locations (add/edit/delete)
FR51 → Epic 5 - Select preferred TTS voice
FR52 → Epic 5 - Persist all preferences across restarts

FR53 → Epic 2 - Request/manage camera permission
FR54 → Epic 6 - Request/manage location permissions
FR55 → Epic 3 - Request/manage microphone permission
FR56 → Epic 8 - Request/manage Bluetooth permissions
FR57 → Epic 9 - Graceful degradation when permissions denied
FR58 → Epic 9 - Clear explanations for permission requests

FR59 → Epic 9 - Guide users through initial permission setup
FR60 → Epic 9 - Demonstrate core voice commands during first-run
FR61 → Epic 9 - Allow users to skip onboarding
FR62 → Epic 9 - Validate TalkBack accessibility during onboarding

## Epic List

### Epic 1: Project Foundation & Core Infrastructure
Set up the minimal Android project foundation with research-validated architecture patterns, enabling all future feature development with proper accessibility, dependency injection, and data persistence frameworks.

**User Outcome:** Development environment ready with core architectural patterns validated; foundation enables all future epics to be built efficiently.

**FRs covered:** Architecture requirements (Clean Architecture + MVVM, Hilt DI, DataStore + Room setup, XML layouts + View Binding, Material Design 3 theming)

### Epic 2: Accessible Object Recognition
Users can point their camera at objects and hear immediate audio announcements with honest confidence levels, with full TalkBack support, 48×48 dp touch targets, high-contrast mode, and haptic feedback—working completely offline with privacy-first on-device processing.

**User Outcome:** Blind and low vision users independently identify objects in their environment using voice or touch activation with confidence-aware feedback and complete accessibility compliance.

**FRs covered:** FR1, FR2, FR3, FR5, FR21, FR22, FR23, FR24, FR25, FR27, FR28, FR29, FR34, FR35, FR41, FR53

### Epic 3: Voice Command System
Users can control all primary app functions hands-free through natural voice commands with immediate audio confirmation, enabling complete eyes-free operation.

**User Outcome:** Users operate the app independently using 15 core voice commands with high accuracy (≥85%) across different acoustic environments.

**FRs covered:** FR17, FR18, FR19, FR20, FR55

### Epic 4: Advanced Recognition Features
Users can customize recognition verbosity, review recognition history, and use continuous scanning mode for environment mapping with local encrypted storage.

**User Outcome:** Users tailor recognition experience to their preferences and review past identifications for learning and verification.

**FRs covered:** FR4, FR6, FR7, FR8, FR37, FR42

### Epic 5: Personalization & Settings
Users can adjust TTS speed, voice selection, haptic intensity, and visual preferences to match their individual needs with all preferences persisting across app restarts.

**User Outcome:** Users customize the app experience for optimal comfort and usability across different contexts (home, work, transit).

**FRs covered:** FR30, FR31, FR46, FR47, FR48, FR49, FR51, FR52, FR36

### Epic 6: GPS-Based Navigation
Users can navigate independently to destinations using GPS-based turn-by-turn voice guidance with anticipatory warnings (5-7 seconds), automatic route recalculation, and basic audio priority ensuring navigation instructions are never missed.

**User Outcome:** Users reach unfamiliar destinations confidently with clear audio guidance, deviation recovery, and navigation announcements that take priority over recognition feedback.

**FRs covered:** FR9, FR10, FR11, FR12, FR54, FR43, FR45

### Epic 7: Saved Locations & Offline Navigation
Users can save frequently visited locations for quick access and navigate using pre-cached offline maps when connectivity is unavailable, with automatic GPS↔offline mode switching.

**User Outcome:** Users navigate to favorite destinations quickly and maintain navigation capability in low-connectivity environments (subways, buildings, rural areas).

**FRs covered:** FR13, FR14, FR15, FR16, FR38, FR39, FR40, FR50

### Epic 8: Enhanced Audio Priority & TTS Management
Users experience intelligent audio routing with a priority queue ensuring safety-critical navigation instructions never get buried, confidence-aware phrasing that builds trust, and Bluetooth audio routing for discreet use.

**User Outcome:** Users receive navigation instructions without interruption while still benefiting from recognition feedback, with audio seamlessly routed to Bluetooth headphones/earpieces when connected.

**FRs covered:** FR32, FR33, FR26, FR56

### Epic 9: Onboarding & First-Run Experience
New users can quickly understand core voice commands and set up essential permissions through an optional tutorial that respects their existing competence and enables first-task completion within 3 minutes.

**User Outcome:** Users complete their first recognition or navigation task within 3 minutes with optional guidance that doesn't patronize, with TalkBack validation ensuring accessibility from first launch.

**FRs covered:** FR59, FR60, FR61, FR62, FR57, FR58

---

## Epic 1: Project Foundation & Core Infrastructure

**Goal:** Establish core Android architecture with essential infrastructure enabling all future feature development.

### Story 1.1: Android Project Bootstrapping with Material Design 3

As a developer,
I want to initialize a minimal Android project with Kotlin and Material Design 3 theming,
So that the app foundation supports accessibility requirements and matches the UX specification.

**Acceptance Criteria:**

**Given** VS Code with Android SDK configured manually (not Android Studio)
**When** I create the project structure with minimal template approach
**Then** the project initializes with API 26+ minimum, API 34+ target
**And** Material Design 3 dependencies are configured (material:1.11.0+)
**And** Dark theme default (#121212 background) with high-contrast theme variant ready
**And** Roboto font with increased base sizes (body 20sp) configured
**And** MainActivity created with basic navigation structure
**And** Project builds successfully via Gradle in VS Code
**And** App launches on emulator/device displaying Material Design 3 themed empty activity

### Story 1.2: Dependency Injection Setup with Hilt

As a developer,
I want to configure Hilt dependency injection framework,
So that I can inject repositories, ViewModels, and services following Clean Architecture patterns.

**Acceptance Criteria:**

**Given** the Android project foundation from Story 1.1
**When** I configure Hilt for the application
**Then** Hilt dependencies (hilt-android:2.48+, hilt-compiler) are added to build.gradle
**And** Application class annotated with @HiltAndroidApp is created
**And** Application module (@Module @InstallIn) providing app-level dependencies exists
**And** MainActivity annotated with @AndroidEntryPoint successfully receives injected dependencies
**And** Sample repository can be injected into a ViewModel demonstrating DI works
**And** Project builds without Hilt annotation processing errors
**And** App launches with Hilt successfully initializing dependency graph

### Story 1.3: DataStore Preferences Infrastructure

As a developer,
I want to set up DataStore for key-value settings storage,
So that user preferences (speech rate, verbosity, high-contrast mode) can persist reliably.

**Acceptance Criteria:**

**Given** Hilt DI configured from Story 1.2
**When** I implement DataStore infrastructure
**Then** DataStore dependencies (datastore-preferences:1.0.0+) are added
**And** PreferencesDataStore singleton is created and provided via Hilt
**And** SettingsRepository interface with methods (getSpeechRate, setSpeechRate, getVerbosity, setVerbosity, getHighContrastMode, setHighContrastMode) exists
**And** SettingsRepositoryImpl implements interface using DataStore with Kotlin Flow
**And** Unit tests verify preferences write/read correctly with Flow emissions
**And** Preferences persist across app restarts (verified in integration test)
**And** Concurrent access is thread-safe without data corruption

### Story 1.4: Room Database Foundation

As a developer,
I want to configure Room database for structured data (recognition history, saved locations),
So that complex data can be stored locally with schema versioning support.

**Acceptance Criteria:**

**Given** Hilt DI and DataStore configured from Stories 1.2-1.3
**When** I set up Room database infrastructure
**Then** Room dependencies (room-runtime:2.6.1+, room-ktx, room-compiler) are added
**And** AppDatabase abstract class annotated with @Database exists with version = 1
**And** Database schema includes two empty entities: RecognitionHistoryEntity and SavedLocationEntity (schema only, no columns yet)
**And** Database provides abstract DAOs: RecognitionHistoryDao and SavedLocationDao (empty interfaces)
**And** Hilt module provides singleton AppDatabase instance with database builder
**And** Database migration strategy (fallbackToDestructiveMigration for development) is configured
**And** Database builds successfully and can be injected into repositories
**And** Unit test verifies database creation and DAO injection works

### Story 1.5: Camera Permissions & TalkBack Testing Framework

As a developer,
I want to implement camera permission flow with TalkBack announcements and create accessibility test harness,
So that blind users receive clear explanations when permissions are requested and accessibility compliance can be validated.

**Acceptance Criteria:**

**Given** the Android project foundation from Stories 1.1-1.4
**When** I implement camera permission request flow
**Then** AndroidManifest.xml declares camera permission (android.permission.CAMERA)
**And** Permission request UI includes TalkBack semantic label explaining why camera is needed ("VisionFocus needs camera access to identify objects in your environment")
**And** Permission rationale dialog appears if user previously denied permission
**And** Permission grant triggers TalkBack announcement: "Camera permission granted. You can now recognize objects."
**And** Permission denial triggers TalkBack announcement: "Camera permission denied. Object recognition will not work without camera access."
**And** Accessibility test harness using Espresso Accessibility is configured
**And** Accessibility Scanner integration allows automated WCAG 2.1 AA checks
**And** Sample accessibility test verifies camera permission dialog has proper content description
**And** All touch targets in permission flow are minimum 48×48 dp (validated programmatically)

---

## Epic 2: Accessible Object Recognition

**Goal:** Enable blind and low vision users to identify objects independently using voice or touch activation with complete accessibility compliance.

### Story 2.1: TFLite Model Integration & On-Device Inference

As a visually impaired user,
I want the app to identify objects using my phone's camera without uploading images,
So that my privacy is protected and recognition works offline.

**Acceptance Criteria:**

**Given** camera permission granted from Epic 1
**When** I activate object recognition
**Then** TFLite model file (ssd_mobilenet_v1_quantized.tflite, ~4MB) is bundled in app/assets
**And** TFLite dependencies (tensorflow-lite:2.14.0, tensorflow-lite-support) are configured
**And** ObjectRecognitionService loads TFLite interpreter on initialization
**And** CameraX captures image frame and converts to ByteBuffer (300×300 input tensor)
**And** TFLite inference executes on-device returning detection results (bounding boxes, class IDs, confidence scores)
**And** Inference completes within 320ms average latency (performance requirement)
**And** Recognition results include category label from 80+ COCO classes
**And** No network calls are made during inference (verified via network traffic monitoring)
**And** Recognition works identically with airplane mode enabled

### Story 2.2: High-Confidence Detection Filtering & TTS Announcement

As a visually impaired user,
I want the app to announce only confident detections with honest confidence levels,
So that I trust the app's identifications and don't act on incorrect information.

**Acceptance Criteria:**

**Given** TFLite inference returns detection results from Story 2.1
**When** detections are processed for announcement
**Then** confidence threshold filter (≥0.6) removes low-confidence detections
**And** Non-maximum suppression (NMS) removes duplicate detections of same object
**And** Confidence levels are categorized: High (≥0.85), Medium (0.7-0.84), Low (0.6-0.69)
**And** TTS announcement includes confidence level: "High confidence: chair", "Medium confidence: possibly a bottle", "Not sure, possibly a cup" (confidence-aware phrasing)
**And** Android TextToSpeech (TTS) service initializes on app launch
**And** TTS announcement initiates within 200ms of recognition completion (latency requirement)
**And** Multiple detections are announced in priority order (highest confidence first)
**And** TTS announcement is clear and natural (not robotic): "I see a chair with high confidence, and possibly a table"

### Story 2.3: Recognition FAB with TalkBack Semantic Annotations

As a blind TalkBack user,
I want to activate object recognition via an accessible floating action button,
So that I can independently trigger recognition without sighted assistance.

**Acceptance Criteria:**

**Given** the app home screen is displayed
**When** TalkBack is enabled
**Then** Recognition FAB (56×56 dp) appears in bottom-right corner
**And** FAB has proper content description: "Recognize objects. Double-tap to activate camera and identify objects in your environment."
**And** FAB touch target is minimum 56×56 dp (exceeds 48×48 dp requirement)
**And** FAB is focusable and receives TalkBack focus in logical order
**And** FAB announces when focused: "Recognize objects, button"
**And** Double-tap activates camera and triggers recognition (verified in TalkBack mode)
**And** FAB shows visual ripple effect and haptic feedback (medium intensity) on tap
**And** FAB icon (Material Symbols "photo_camera") has 24dp size with proper contrast
**And** FAB background color has minimum 7:1 contrast ratio in high-contrast mode

### Story 2.4: Camera Capture with Accessibility Focus Management

As a blind TalkBack user,
I want clear audio feedback when recognition starts and completes,
So that I know the system is working and when results are ready.

**Acceptance Criteria:**

**Given** user activates recognition FAB from Story 2.3
**When** camera capture begins
**Then** TalkBack announces immediately: "Starting recognition. Point camera at object."
**And** CameraX preview starts (not visible to user but functional)
**And** Camera captures single frame after 1 second stabilization delay
**And** Recognition loading state is announced: "Analyzing image..."
**And** Camera lifecycle properly pauses when recognition completes (prevents battery drain)
**And** Focus returns to recognition FAB after announcement completes
**And** User can immediately trigger another recognition via FAB double-tap
**And** Back button or "Cancel" voice command stops recognition mid-process
**And** Error states are announced clearly: "Camera error. Please try again." or "No objects detected. Try pointing camera at a different area."

### Story 2.5: High-Contrast Mode & Large Text Support

As a low vision user,
I want the app UI to be visible with high contrast colors and large text,
So that I can see visual elements without relying solely on audio feedback.

**Acceptance Criteria:**

**Given** the app is running
**When** I enable high-contrast mode in settings
**Then** high-contrast theme activates with minimum 7:1 contrast ratio (WCAG 2.1 AA)
**And** background color changes to #000000 (pure black)
**And** foreground text/icons change to #FFFFFF (pure white)
**And** semantic colors maintain contrast: success green (#4CAF50), warning amber (#FFC107), error red (#F44336)
**And** large text mode increases all text sizes by 150% (body: 20sp → 30sp)
**And** UI layouts adapt without text truncation or overlap
**And** FAB and all touch targets remain minimum 48×48 dp with scaling
**And** High-contrast mode persists across app restarts (stored in DataStore)
**And** Settings toggle for high-contrast mode has proper TalkBack label: "High contrast mode, switch, currently off"

### Story 2.6: Haptic Feedback for Recognition Events

As a deaf-blind user,
I want haptic feedback when recognition starts and completes,
So that I receive non-audio confirmation of system state changes.

**Acceptance Criteria:**

**Given** haptic feedback enabled in settings (default: medium intensity)
**When** recognition events occur
**Then** recognition start triggers single short vibration (100ms, medium intensity)
**And** recognition success triggers double vibration pattern (100ms, 50ms gap, 100ms)
**And** recognition error triggers long vibration (300ms)
**And** haptic patterns are distinct and recognizable by touch
**And** haptic intensity respects user preference (off, light, medium, strong)
**And** haptic feedback uses Android Vibrator API with amplitude control on API 26+
**And** haptic patterns work on devices with and without advanced vibration motors
**And** no haptic feedback occurs when intensity set to "off"

### Story 2.7: Complete TalkBack Navigation for Primary Flow

As a blind TalkBack user,
I want all UI elements properly labeled with logical focus order,
So that I can navigate the entire recognition flow independently.

**Acceptance Criteria:**

**Given** TalkBack is enabled throughout the app
**When** I navigate using TalkBack gestures
**Then** home screen elements announce in logical order: title → recognition FAB → bottom navigation (if present)
**And** all interactive elements (buttons, switches, text fields) have proper content descriptions
**And** all images have meaningful alt text or are marked decorative (importantForAccessibility="no")
**And** focus order follows visual layout (top to bottom, left to right)
**And** focus restoration works after interruptions (phone call, notification)
**And** TalkBack announces state changes: "Loading", "Recognition complete", "Error occurred"
**And** custom views (if any) properly announce their role (button, heading, list item)
**And** gestures work correctly: swipe right/left to navigate, double-tap to activate
**And** TalkBack reading stops when user double-taps to activate action
**And** app passes Accessibility Scanner checks for primary recognition flow (zero errors)

---

## Epic 3: Voice Command System

**Goal:** Enable hands-free operation through natural voice commands with high accuracy.

### Story 3.1: Android Speech Recognizer Integration

As a visually impaired user,
I want to control the app using voice commands,
So that I can operate it hands-free without touching the screen.

**Acceptance Criteria:**

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

### Story 3.2: Core Voice Command Processing Engine

As a visually impaired user,
I want 15 core voice commands to be recognized accurately,
So that I can perform primary actions without visual navigation.

**Acceptance Criteria:**

**Given** voice command listening mode active from Story 3.1
**When** I speak a voice command
**Then** command processor recognizes these 15 core commands: "Recognize", "Navigate", "Repeat", "Cancel", "Settings", "Save location", "High contrast on/off", "Increase speed", "Decrease speed", "History", "Help", "Back", "Home", "Where am I", "What do I see"
**And** command matching is case-insensitive and tolerates minor variations ("recognize" matches "Recognize", "recognition", "recognise")
**And** command execution triggers within 300ms of recognition (latency requirement)
**And** immediate TTS confirmation announces command: "Recognize command received" or "Opening settings"
**And** unrecognized commands trigger helpful response: "Command not recognized. Say 'Help' for available commands."
**And** voice command accuracy measured ≥85% in testing with 10 voice samples per command
**And** commands work across different acoustic environments (quiet room, outdoor street noise, transit)
**And** background noise filtering improves recognition in noisy environments

### Story 3.3: Voice Command Confirmation & Cancellation

As a visually impaired user,
I want immediate audio confirmation when my voice command is recognized,
So that I know the system understood me and is taking action.

**Acceptance Criteria:**

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

### Story 3.4: Voice Command Help System

As a visually impaired user,
I want to hear all available voice commands when I say "Help",
So that I can learn what commands are available without memorizing documentation.

**Acceptance Criteria:**

**Given** voice command listening mode active
**When** I say "Help"
**Then** TTS announces all 15 core commands in logical groups:
  - Recognition: "Say 'Recognize' to identify objects, 'What do I see' to hear last result, 'Repeat' to hear last announcement again"
  - Navigation: "Say 'Navigate' to start turn-by-turn directions, 'Where am I' to hear current location, 'Cancel' to stop navigation"
  - Settings: "Say 'Settings' to open preferences, 'High contrast on' or 'High contrast off' to toggle visual mode, 'Increase speed' or 'Decrease speed' to adjust speech rate"
  - General: "Say 'History' to review past recognitions, 'Save location' to bookmark current place, 'Back' to go back, 'Home' to return to home screen"
**And** help announcement respects speech rate preference
**And** help can be interrupted by speaking another command
**And** help announcement concludes with: "Say a command now, or tap the microphone button to exit voice mode"

### Story 3.5: Always-Available Voice Activation

As a visually impaired user,
I want voice commands to work from any screen without navigating to a specific mode,
So that I have quick access to core functions regardless of app state.

**Acceptance Criteria:**

**Given** the app is in foreground (any screen)
**When** I tap the microphone button (visible on all screens)
**Then** voice listening mode activates immediately
**And** microphone button is consistently positioned (top-right) across all screens
**And** current screen context is maintained after command execution (e.g., if on Settings screen and say "Recognize", return to Settings after recognition completes)
**And** voice commands work from: Home screen, Settings screen, History screen, Navigation active screen
**And** "Home" command returns to home screen from any location
**And** "Back" command navigates to previous screen in stack
**And** voice activation does NOT require unlocking phone (volume button long-press activates from lock screen per UX spec—implemented in future story)

---

## Epic 4: Advanced Recognition Features

**Goal:** Users tailor recognition experience and review past identifications.

### Story 4.1: Verbosity Mode Selection (Brief/Standard/Detailed)

As a visually impaired user,
I want to choose how much detail I hear about recognized objects,
So that I can get quick identifications or comprehensive descriptions based on my needs.

**Acceptance Criteria:**

**Given** verbosity setting is accessible in Settings screen
**When** I select a verbosity mode
**Then** three modes are available: Brief, Standard (default), Detailed
**And** Brief mode announces only object category: "Chair"
**And** Standard mode announces category + confidence: "Chair with high confidence"
**And** Detailed mode announces category + confidence + position + count: "High confidence: chair in center of view. Two chairs detected."
**And** verbosity preference persists in DataStore across app restarts
**And** voice command "Verbosity brief", "Verbosity standard", "Verbosity detailed" changes mode immediately
**And** mode change confirmation announces: "Verbosity set to detailed"
**And** current verbosity mode is used for next recognition result
**And** Settings screen verbosity selector has proper TalkBack labels: "Verbosity mode, radio button group. Brief selected."

### Story 4.2: Recognition History Storage with Room Database

As a visually impaired user,
I want to review my last 50 object recognitions,
So that I can verify past identifications or share results with others.

**Acceptance Criteria:**

**Given** Room database configured from Epic 1
**When** recognition completes successfully
**Then** RecognitionHistoryEntity schema includes: id (Int, PrimaryKey), category (String), confidence (Float), timestamp (Long), verbosityMode (String), detailText (String)
**And** RecognitionHistoryDao provides: insertRecognition(), getRecentRecognitions(limit: 50), clearHistory()
**And** RecognitionHistoryRepository saves each successful recognition to Room
**And** history is limited to 50 most recent entries (oldest auto-deleted when exceeding limit)
**And** history persists across app restarts
**And** history entries include timestamp formatted as "December 24, 2025 at 3:45 PM"
**And** history query returns results ordered by timestamp descending (newest first)
**And** history data is stored encrypted at rest (using Android Security Crypto library)

### Story 4.3: Recognition History Review UI

As a visually impaired user,
I want to navigate my recognition history using TalkBack,
So that I can hear past identifications without visual access.

**Acceptance Criteria:**

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

### Story 4.4: Continuous Scanning Mode for Environment Mapping

As a visually impaired user,
I want to continuously scan my environment and hear multiple object announcements,
So that I can build a mental map of objects around me without repeated button presses.

**Acceptance Criteria:**

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

### Story 4.5: Distance and Position Information in Detailed Mode

As a visually impaired user,
I want to hear approximate distance and position of detected objects in detailed mode,
So that I can locate objects spatially and move toward them safely.

**Acceptance Criteria:**

**Given** verbosity mode set to "Detailed" from Story 4.1
**When** recognition detects objects
**Then** bounding box position is analyzed to determine screen position: "left", "center", "right", "top", "bottom"
**And** bounding box size estimates distance: large box (>40% screen) = "close", medium (20-40%) = "medium distance", small (<20%) = "far"
**And** detailed announcement includes position + distance: "High confidence: chair, close, in center of view"
**And** multiple objects announced with spatial organization: "I see a chair close by in the center, and a table at medium distance on the right"
**And** position calculations work correctly across device orientations (portrait, landscape)
**And** distance estimates are calibrated for typical object sizes (chair ~0.5m width, person ~0.5m width)
**And** spatial announcements use natural language (avoid robotic "X: 150 pixels, Y: 200 pixels")

---

## Epic 5: Personalization & Settings

**Goal:** Users customize app experience for optimal comfort and usability.

### Story 5.1: TTS Speech Rate Adjustment

As a visually impaired user,
I want to adjust how fast the app speaks,
So that I can match the speed to my comprehension preference.

**Acceptance Criteria:**

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

### Story 5.2: TTS Voice Selection

As a visually impaired user,
I want to choose which TTS voice the app uses,
So that I can select a voice that is comfortable and clear for me.

**Acceptance Criteria:**

**Given** Settings screen voice selector is accessible
**When** I select a TTS voice
**Then** available voices are queried from Android TTS engine (getAvailableLanguages)
**And** voice selector displays available English voices: e.g., "en-US-male", "en-US-female", "en-GB-male"
**And** default voice is system default English voice
**And** TalkBack announces each voice option: "English US male voice, radio button"
**And** sample announcement plays when voice selected: "This is a preview of this voice" (in selected voice)
**And** voice preference persists in DataStore across app restarts
**And** selected voice applies to all TTS announcements (recognition results, navigation instructions, confirmations)
**And** if selected voice becomes unavailable (TTS engine uninstalled), fallback to system default with announcement: "Your preferred voice is unavailable. Using default voice."

### Story 5.3: Settings Screen with Persistent Preferences

As a visually impaired user,
I want all my settings saved automatically,
So that I don't have to reconfigure the app every time I use it.

**Acceptance Criteria:**

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

### Story 5.4: Haptic Feedback Intensity Adjustment

As a deaf-blind user,
I want to adjust haptic feedback intensity or disable it,
So that I can feel vibrations comfortably based on my sensitivity.

**Acceptance Criteria:**

**Given** Settings screen haptic intensity selector
**When** I adjust haptic intensity
**Then** four intensity levels available: Off, Light, Medium (default), Strong
**And** intensity selector is radio button group with TalkBack labels: "Haptic intensity, radio button group. Medium selected."
**And** selecting intensity triggers sample vibration at that intensity (100ms test vibration)
**And** intensity preference persists in DataStore across app restarts
**And** Off disables all haptic feedback throughout app
**And** Light uses 50% amplitude, Medium uses 75%, Strong uses 100% (on API 26+ devices with amplitude control)
**And** devices without amplitude control (pre-API 26) use duration variation: Light (50ms), Medium (100ms), Strong (200ms)
**And** haptic intensity applies to: recognition events (Story 2.6), button presses, navigation alerts

### Story 5.5: Quick Settings Toggle via Voice Commands

As a visually impaired user,
I want to change common settings using voice commands,
So that I can adjust preferences without navigating to Settings screen.

**Acceptance Criteria:**

**Given** voice command system active from Epic 3
**When** I speak a settings-related command
**Then** "High contrast on" enables high-contrast mode immediately with confirmation: "High contrast mode on"
**And** "High contrast off" disables mode with confirmation: "High contrast mode off"
**And** "Increase speed" increments TTS rate by 0.25× with confirmation: "Speech rate increased to 1.25 times"
**And** "Decrease speed" decrements TTS rate by 0.25× with confirmation: "Speech rate decreased to 0.75 times"
**And** "Verbosity brief/standard/detailed" changes verbosity with confirmation
**And** settings changes via voice command persist in DataStore
**And** quick toggle commands work from any screen in the app
**And** invalid state changes announce appropriately: "Speech rate already at maximum"

---

## Epic 6: GPS-Based Navigation

**Goal:** Users reach unfamiliar destinations confidently with clear audio guidance.

### Story 6.1: Destination Input via Voice and Text

As a visually impaired user,
I want to enter navigation destinations using voice or text input,
So that I can specify where I want to go without visual typing.

**Acceptance Criteria:**

**Given** navigation feature accessible from home screen
**When** I activate navigation (voice command "Navigate" or tap navigation button)
**Then** destination input screen appears with TalkBack announcement: "Where would you like to go?"
**And** text input field has TalkBack label: "Destination, edit text"
**And** microphone button within text field activates voice input
**And** voice input converts speech to text and populates destination field
**And** voice input announces transcribed text: "You said: Central Park, New York"
**And** "Go" button (56×56 dp) starts navigation when destination entered
**And** invalid/ambiguous destinations trigger clarification: "Multiple locations found. Did you mean Central Park, New York, or Central Park, Sacramento?"
**And** empty destination field shows hint text: "Say or type destination"
**And** back button cancels destination input and returns to home screen

### Story 6.2: Google Maps Directions API Integration

As a visually impaired user,
I want accurate turn-by-turn directions from my current location to destination,
So that I can navigate independently using GPS guidance.

**Acceptance Criteria:**

**Given** valid destination entered from Story 6.1 and location permission granted
**When** I start navigation
**Then** user consent dialog appears before first network call: "VisionFocus needs internet to download directions. Allow network access?"
**And** consent stored in DataStore to avoid repeated prompts
**And** FusedLocationProviderClient retrieves current GPS location (minimum 1Hz update rate)
**And** Google Maps Directions API called with origin (current location) and destination
**And** API key configured securely (not hardcoded in source, stored in local.properties)
**And** route response parsed to extract: total distance, total duration, step-by-step maneuvers
**And** each step includes: instruction text ("Turn left onto Main Street"), distance to step, step duration, maneuver type (turn-left, turn-right, straight, etc.)
**And** network error handling announces: "Cannot download directions. Check internet connection."
**And** API error handling (invalid API key, quota exceeded) announces: "Navigation service unavailable. Please try again later."

### Story 6.3: Turn-by-Turn Voice Guidance with Anticipatory Warnings

As a visually impaired user,
I want to hear upcoming turns announced before I need to act,
So that I have time to prepare and execute maneuvers safely.

**Acceptance Criteria:**

**Given** navigation active with route from Story 6.2
**When** I approach a turn
**Then** turn warning announces 5-7 seconds before maneuver (estimated based on walking speed ~1.4 m/s)
**And** first announcement: "In 50 meters, turn left onto Main Street"
**And** second announcement (at turn point): "Turn left now onto Main Street"
**And** announcements use natural language and cardinal directions when helpful
**And** straight sections announce distance checkpoints: "Continue straight for 200 meters"
**And** turn announcements interrupt any ongoing recognition announcements (navigation has priority per Epic 8)
**And** TTS uses increased volume for navigation (10% louder than recognition) for safety
**And** multi-step intersections provide detailed guidance: "At the roundabout, take the second exit onto Oak Avenue"
**And** arrival announcement: "You have arrived at your destination"

### Story 6.4: Route Deviation Detection and Recalculation

As a visually impaired user,
I want the app to recalculate my route if I go off-path,
So that I can recover from mistakes or detours without getting lost.

**Acceptance Criteria:**

**Given** navigation active with turn-by-turn guidance
**When** my GPS location deviates from planned route
**Then** deviation detected when distance from route > 20 meters for 5 consecutive seconds
**And** deviation announcement: "You have gone off route. Recalculating directions."
**And** new route calculated from current location to original destination via Maps API
**And** recalculation completes within 3 seconds (performance requirement)
**And** updated turn-by-turn guidance resumes immediately after recalculation
**And** recalculation does not interrupt if user temporarily near route edge (prevents false positives in urban canyons)
**And** excessive recalculations (>3 in 2 minutes) trigger helpful prompt: "Having trouble staying on route. Would you like more frequent turn warnings?"
**And** recalculation works in both walking and transit navigation modes

### Story 6.5: GPS Location Permissions with Clear Explanations

As a visually impaired user,
I want clear explanations when the app requests location permissions,
So that I understand why access is needed and can make informed decisions.

**Acceptance Criteria:**

**Given** first navigation attempt without location permission
**When** location permission is requested
**Then** permission rationale appears before system dialog: "VisionFocus needs location access to provide turn-by-turn navigation and help you reach your destination."
**And** rationale has TalkBack announcement with same text
**And** location permission types requested: ACCESS_FINE_LOCATION (for GPS accuracy)
**And** permission grant triggers confirmation: "Location permission granted. You can now use navigation."
**And** permission denial triggers explanation: "Location permission denied. Navigation requires location access. You can enable it in Settings."
**And** navigation gracefully disabled if permission denied (button shows "Enable location to navigate")
**And** in-app settings link to system permission settings for easy re-enabling
**And** background location NOT requested (navigation only works in foreground)

### Story 6.6: Network Availability Indication

As a visually impaired user,
I want to know when internet is required vs optional,
So that I can plan navigation based on connectivity availability.

**Acceptance Criteria:**

**Given** the app is monitoring network state
**When** navigation is initiated
**Then** network state checked before Maps API call
**And** no connectivity announces: "No internet connection. You can navigate using saved offline maps or wait for connectivity."
**And** saved offline maps option (from Epic 7) available if maps pre-cached
**And** online indicator in status area announces: "Online - live directions available"
**And** offline indicator announces: "Offline - using saved maps"
**And** transition from offline to online announces: "Internet connected. Updating route with live traffic."
**And** recognition feature always works regardless of network state (zero dependency)
**And** settings clearly indicate network requirements: "Navigation requires internet for live directions"

---

## Epic 7: Saved Locations & Offline Navigation

**Goal:** Users navigate to favorite destinations quickly and maintain navigation in low-connectivity environments.

### Story 7.1: Save Current Location with Custom Labels

As a visually impaired user,
I want to save my current location with a custom name,
So that I can quickly navigate to frequent destinations without typing addresses.

**Acceptance Criteria:**

**Given** I am at a location I want to save
**When** I use voice command "Save location" or tap "Save location" button
**Then** save dialog appears with TalkBack announcement: "Save current location. Enter a name."
**And** name text field has TalkBack label: "Location name, edit text"
**And** microphone button enables voice input for name
**And** current GPS coordinates retrieved from FusedLocationProviderClient
**And** validation ensures name is not empty (min 2 characters)
**And** duplicate name check prompts: "You already have a location named Home. Overwrite or choose a different name?"
**And** SavedLocationEntity schema includes: id (Int, PrimaryKey), name (String), latitude (Double), longitude (Double), timestamp (Long), address (String, nullable)
**And** SavedLocationDao provides: insertLocation(), getAllLocations(), deleteLocation(id), updateLocation()
**And** saved location stored encrypted in Room database (using Android Security Crypto)
**And** save confirmation announces: "Location saved as Home"

### Story 7.2: Saved Locations Management UI

As a visually impaired user,
I want to view, edit, and delete my saved locations,
So that I can maintain an organized list of frequent destinations.

**Acceptance Criteria:**

**Given** saved locations exist from Story 7.1
**When** I navigate to Saved Locations screen (voice command "Saved locations" or bottom navigation)
**Then** RecyclerView displays all saved locations ordered by most recently used
**And** each list item includes: location name, address (if available), save timestamp
**And** each item has TalkBack content description: "Home, 123 Main Street, saved December 20, 2025"
**And** TalkBack swipe right/left navigates between location items
**And** double-tap on location opens action menu: Navigate, Edit, Delete
**And** Navigate option starts turn-by-turn guidance to that location immediately
**And** Edit option allows changing location name with voice or text input
**And** Delete option shows confirmation dialog: "Delete Home? This cannot be undone."
**And** delete confirmation announces: "Home deleted"
**And** empty state announces: "No saved locations yet. Say 'Save location' when at a place you visit frequently."

### Story 7.3: Quick Navigation to Saved Locations

As a visually impaired user,
I want to navigate to saved locations without typing,
So that I can reach frequent destinations with minimal effort.

**Acceptance Criteria:**

**Given** saved locations exist and navigation is initiated
**When** I select destination
**Then** destination input screen shows "Saved Locations" button (56×56 dp) below text field
**And** tapping saved locations button opens picker with TalkBack announcement: "Select a saved location"
**And** saved locations list displays all saved places
**And** voice command "Navigate to [location name]" directly starts navigation without intermediate screens
**And** voice command matches location names flexibly: "Navigate to home" matches saved location "Home" (case-insensitive)
**And** ambiguous voice commands clarify: "Did you mean Home or Home Depot?"
**And** selecting saved location populates destination field and automatically starts navigation
**And** navigation flow identical to typed destination (uses same routing logic from Epic 6)

### Story 7.4: Offline Map Pre-Caching

As a visually impaired user,
I want to download map data for known routes,
So that I can navigate without internet connectivity.

**Acceptance Criteria:**

**Given** a saved location exists
**When** I choose to download offline maps for that route
**Then** "Download offline maps" option appears in saved location action menu
**And** download dialog prompts: "Download maps for navigation to Home? This will use approximately 50 MB of storage."
**And** user consent required before download starts
**And** progress announcement: "Downloading offline maps. 25% complete."
**And** Google Maps offline area downloaded using Maps SDK OfflineRegionManager
**And** offline map data stored in app-specific directory (cleared on uninstall)
**And** download completion announces: "Offline maps downloaded. You can navigate to Home without internet."
**And** offline map status shown in saved location details: "Offline maps available"
**And** map expiration (30 days) announces: "Offline maps for Home will expire in 5 days. Download again to refresh."
**And** automatic update prompt when connected to WiFi: "Offline maps are outdated. Update now?"

### Story 7.5: Automatic GPS ↔ Offline Navigation Mode Switching

As a visually impaired user,
I want the app to automatically use offline maps when internet is unavailable,
So that navigation continues seamlessly without manual intervention.

**Acceptance Criteria:**

**Given** offline maps downloaded from Story 7.4 and navigation active
**When** internet connectivity is lost during navigation
**Then** app automatically switches to offline navigation mode without interrupting guidance
**And** mode switch announces: "Lost internet connection. Continuing navigation using offline maps."
**And** offline navigation uses pre-cached route from offline maps
**And** turn-by-turn guidance continues using cached route data
**And** offline navigation does NOT include live traffic or route recalculation (static route only)
**And** deviation from offline route announces: "Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity."
**And** when connectivity restored announces: "Internet connected. Updating route with live traffic."
**And** route recalculation resumes when online
**And** offline navigation gracefully falls back to basic guidance if detailed offline maps unavailable

### Story 7.6: Destination Arrival Confirmation

As a visually impaired user,
I want clear confirmation when I reach my destination,
So that I know I can stop navigating and have arrived successfully.

**Acceptance Criteria:**

**Given** navigation active and approaching destination
**When** GPS location is within 10 meters of destination coordinates
**Then** arrival announcement: "You have arrived at [destination name]. Navigation complete."
**And** arrival haptic feedback: three short vibrations (100ms each, 50ms gaps)
**And** navigation session automatically ends (stops GPS tracking to save battery)
**And** post-navigation dialog offers: "Save this location?", "Navigate back home?", "Done"
**And** arrival logged to navigation history (for future review)
**And** recognition feature automatically re-enabled (if it was paused during navigation)
**And** arrival within 10-20 meters announces: "Approaching destination. About 15 meters away."
**And** arrival does not trigger if user passes through destination briefly (requires 5 seconds within range)

---

## Epic 8: Enhanced Audio Priority & TTS Management

**Goal:** Users receive navigation instructions without interruption while still benefiting from recognition feedback.

### Story 8.1: Audio Priority Queue Implementation

As a visually impaired user,
I want navigation instructions to always be heard immediately without being buried by other announcements,
So that I never miss safety-critical turn guidance.

**Acceptance Criteria:**

**Given** multiple audio announcements occur simultaneously (navigation + recognition)
**When** audio priority queue processes announcements
**Then** priority order enforced: Navigation (Priority 1) > Recognition (Priority 2) > TalkBack (Priority 3) > System sounds (Priority 4)
**And** navigation announcement immediately interrupts ongoing recognition announcement
**And** interrupted recognition announcement either discarded (if low priority) or queued for playback after navigation
**And** AudioManager.requestAudioFocus() called with AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK for navigation
**And** recognition announcements duck (reduce volume by 50%) if navigation speaks during recognition
**And** TalkBack announcements preserve priority when user actively navigating TalkBack (e.g., swiping through elements)
**And** audio priority queue implemented using Kotlin Flows or priority queue data structure
**And** priority queue clears when navigation ends (recognition returns to Priority 1)

### Story 8.2: Confidence-Aware TTS Phrasing

As a visually impaired user,
I want the app to speak honestly about detection confidence,
So that I can appropriately trust identifications and make safe decisions.

**Acceptance Criteria:**

**Given** object recognition results with varying confidence levels
**When** TTS announces results
**Then** high confidence (≥0.85) uses definitive phrasing: "I see a chair"
**And** medium confidence (0.7-0.84) uses qualified phrasing: "I see what appears to be a bottle" or "Possibly a bottle"
**And** low confidence (0.6-0.69) uses uncertain phrasing: "I'm not sure, but it might be a cup"
**And** very low confidence (<0.6) not announced at all (filtered before announcement)
**And** phrasing varies naturally to avoid repetitive robotic speech: "High confidence: chair" vs "I'm quite certain that's a chair"
**And** confidence phrasing respects verbosity mode: brief mode skips confidence qualifiers
**And** detailed mode includes numerical confidence: "Chair detected with 92% confidence"
**And** confidence-aware phrasing builds user trust by being transparent about uncertainty

### Story 8.3: Bluetooth Audio Routing for Discreet Use

As a visually impaired user,
I want the app to route audio through my Bluetooth earpiece or headphones,
So that I can use the app discreetly in public without disturbing others.

**Acceptance Criteria:**

**Given** Bluetooth headphones or earpiece connected to device
**When** the app starts audio playback (TTS announcements)
**Then** Bluetooth permission (BLUETOOTH_CONNECT on API 31+) requested with explanation: "VisionFocus can route audio to your Bluetooth devices for private listening."
**And** Android AudioManager detects connected Bluetooth audio devices
**And** audio automatically routes to Bluetooth device if connected
**And** no Bluetooth device connected routes to phone speaker (default)
**And** Bluetooth device connected announcement: "Audio routing to [device name]"
**And** Bluetooth device disconnected during use announces: "Bluetooth disconnected. Switching to phone speaker."
**And** user can manually select audio output in Settings: Phone speaker, Bluetooth device, Wired headphones
**And** audio routing preference persists in DataStore
**And** call interruptions (phone call) properly pause TTS and resume after call ends

### Story 8.4: TTS Speech Interruption and Resumption Control

As a visually impaired user,
I want to cancel long announcements when I've heard enough,
So that I'm not forced to wait for complete playback.

**Acceptance Criteria:**

**Given** TTS announcement playing (e.g., long detailed recognition result)
**When** I want to stop the announcement
**Then** voice command "Stop" or "Cancel" immediately stops TTS playback
**And** tapping anywhere on screen during TTS stops announcement
**And** TTS stop confirmation: single short haptic vibration (no audio confirmation to avoid clutter)
**And** recognition FAB remains responsive during TTS (can trigger new recognition mid-announcement)
**And** new recognition automatically stops previous TTS announcement
**And** navigation announcements cannot be cancelled (safety-critical)
**And** TTS queue cleared when user issues interruption command
**And** "Repeat" voice command replays last announcement from beginning

---

## Epic 9: Onboarding & First-Run Experience

**Goal:** Users complete their first task within 3 minutes with optional guidance that doesn't patronize.

### Story 9.1: First-Run Permission Flow with Clear Explanations

As a new visually impaired user,
I want clear explanations for each permission request during setup,
So that I understand what the app needs and can make informed decisions.

**Acceptance Criteria:**

**Given** first app launch after installation
**When** onboarding begins
**Then** welcome announcement: "Welcome to VisionFocus. This app helps you identify objects and navigate using voice guidance. Let's set up essential permissions."
**And** TalkBack detection checks if TalkBack is enabled
**And** TalkBack disabled triggers prompt: "TalkBack is not enabled. VisionFocus works best with TalkBack. Would you like to enable it now?" (with link to system settings)
**And** permission requests appear in logical order: Camera → Microphone → Location
**And** camera permission rationale: "VisionFocus needs camera access to identify objects in your environment. Your images stay private on your device."
**And** microphone permission rationale: "VisionFocus needs microphone access for voice commands, so you can control the app hands-free."
**And** location permission rationale: "VisionFocus needs location access for turn-by-turn navigation to help you reach destinations."
**And** each permission has skip option: "Skip for now" (app functions with graceful degradation)
**And** permission denial tracking shows impact: "Skipping camera means object recognition won't work. You can enable it later in Settings."

### Story 9.2: Quick-Start Tutorial for Core Voice Commands

As a new visually impaired user,
I want a brief tutorial demonstrating core voice commands,
So that I can learn how to use the app without reading documentation.

**Acceptance Criteria:**

**Given** permissions setup complete from Story 9.1
**When** tutorial begins
**Then** tutorial announcement: "Quick tutorial: Learn 5 essential voice commands in under 2 minutes. You can skip this anytime."
**And** tutorial demonstrates 5 core commands with interactive practice:
  1. "Recognize" - prompts user to say command, then shows live recognition
  2. "Navigate" - explains navigation feature (no live navigation in tutorial)
  3. "Repeat" - demonstrates repeating last announcement
  4. "Settings" - shows how to access preferences
  5. "Help" - explains full command list is available
**And** each command practice includes: instruction → user attempts command → confirmation or retry
**And** practice recognition validation: "Great! You said 'Recognize'. Let's try identifying an object. Point your camera at something and say 'Recognize' again."
**And** tutorial progress announced: "Command 1 of 5 complete"
**And** skip button (56×56 dp) always visible with TalkBack label: "Skip tutorial, button"
**And** tutorial completion announcement: "Tutorial complete! You're ready to use VisionFocus. Say 'Recognize' to identify objects or 'Navigate' for directions."

### Story 9.3: Optional Onboarding with Immediate App Access

As an experienced visually impaired user,
I want to skip onboarding and start using the app immediately,
So that I'm not forced through unnecessary tutorials.

**Acceptance Criteria:**

**Given** first app launch
**When** welcome screen appears
**Then** two options available: "Start tutorial" (default focus) and "Skip - I'm ready to use the app"
**And** skip option has TalkBack label: "Skip tutorial, button. Jump directly to the app."
**And** selecting skip immediately shows home screen with recognition FAB
**And** skip does NOT bypass permission requests (essential permissions still requested on first use of features)
**And** skip triggers brief orientation: "Home screen. Double-tap the large button at bottom-right to recognize objects. Say 'Help' anytime for voice commands."
**And** skipping tutorial sets preference in DataStore (tutorial not shown again)
**And** tutorial can be replayed from Settings: "Replay tutorial" button
**And** users who skip have identical app functionality to users who complete tutorial

### Story 9.4: TalkBack Accessibility Validation During Onboarding

As a blind TalkBack user,
I want the onboarding to validate that TalkBack is working correctly,
So that I can be confident the app is fully accessible before I rely on it.

**Acceptance Criteria:**

**Given** onboarding flow active
**When** TalkBack status is checked
**Then** TalkBack.isEnabled() queried to detect if TalkBack is active
**And** TalkBack enabled triggers confirmation: "TalkBack detected. VisionFocus is fully accessible."
**And** TalkBack disabled shows warning: "TalkBack is not enabled. VisionFocus is designed for screen reader users. Enable TalkBack in your device accessibility settings for the best experience."
**And** link to system accessibility settings provided: "Open accessibility settings" button (navigates to system settings via Intent)
**And** onboarding can continue without TalkBack (graceful degradation)
**And** tutorial validates TalkBack focus order throughout onboarding screens
**And** TalkBack validation runs Accessibility Scanner checks automatically
**And** validation failures (missing content descriptions, insufficient touch targets) logged for developer review
**And** onboarding completion sets validation flag in DataStore (not repeated on subsequent launches)

### Story 9.5: Graceful Degradation When Permissions Denied

As a visually impaired user who denies optional permissions,
I want the app to still function for features that don't require those permissions,
So that I can use the app on my terms without being locked out.

**Acceptance Criteria:**

**Given** user has denied one or more permissions during onboarding
**When** I try to use a feature requiring denied permission
**Then** camera denied disables recognition FAB with TalkBack label: "Recognition unavailable. Camera permission required. Double-tap to enable in settings."
**And** tapping disabled FAB shows rationale and settings link: "Camera permission is needed for object recognition. Enable in settings?"
**And** microphone denied disables voice command button with label: "Voice commands unavailable. Microphone permission required."
**And** voice command features show manual alternatives (buttons with text labels)
**And** location denied disables navigation with label: "Navigation unavailable. Location permission required."
**And** feature unavailability clearly communicated via TTS when user attempts access: "Camera permission is required for recognition. You can enable it in Settings."
**And** Settings screen shows permission status: "Camera: Denied", "Microphone: Granted", "Location: Denied"
**And** in-app permission re-request link navigates to system app settings (Intent to Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
**And** granted features work normally regardless of other denied permissions (e.g., voice commands work without camera if microphone granted)
