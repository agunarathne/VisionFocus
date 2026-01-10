# Story 7.5: Automatic GPS ↔ Offline Navigation Mode Switching

Status: ready-for-dev

Date Created: 2026-01-10

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a visually impaired user,
I want the app to automatically use offline maps when internet is unavailable,
So that navigation continues seamlessly without manual intervention.

## Acceptance Criteria

**Given** offline maps are downloaded from Story 7.4 and navigation is active
**When** internet connectivity is lost during navigation
**Then** app automatically switches to offline navigation mode without interrupting guidance
**And** mode switch announces: "Lost internet connection. Continuing navigation using offline maps."
**And** offline navigation uses pre-cached route from offline maps
**And** turn-by-turn guidance continues using cached route data
**And** offline navigation does NOT include live traffic or route recalculation (static route only)
**And** deviation from offline route announces: "Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity."
**And** when connectivity is restored announces: "Internet connected. Updating route with live traffic."
**And** route recalculation resumes when online (integrates with Story 6.4)
**And** offline navigation gracefully falls back to basic guidance if detailed offline maps unavailable

## Tasks / Subtasks

- [ ] Task 1: Implement offline routing engine using Mapbox Navigation SDK (AC: 3, 4)
  - [ ] 1.1: Research Mapbox Navigation SDK offline routing APIs
  - [ ] 1.2: Add Mapbox Navigation SDK dependencies to build.gradle.kts (if not already added in Story 7.4)
  - [ ] 1.3: Create `getOfflineRoute(origin: LatLng, destination: LatLng): Result<NavigationRoute>` in MapboxOfflineManager
  - [ ] 1.4: Query Mapbox offline tile store for available cached tiles
  - [ ] 1.5: Calculate route using Mapbox offline routing engine (uses pre-cached tiles from Story 7.4)
  - [ ] 1.6: Convert Mapbox DirectionsRoute to NavigationRoute domain model (match Google Maps format)
  - [ ] 1.7: Return static route (no traffic, no recalculation capability)
  - [ ] 1.8: Handle errors gracefully: no offline tiles available, route calculation failure

- [ ] Task 2: Enhance NavigationManager with routing strategy pattern (AC: 1, 3, 8)
  - [ ] 2.1: Open NavigationManager.kt and NavigationManagerImpl.kt
  - [ ] 2.2: Inject OfflineMapRepository and NetworkStateMonitor
  - [ ] 2.3: Create enum `NavigationMode { ONLINE, OFFLINE, UNAVAILABLE }`
  - [ ] 2.4: Implement `determineNavigationMode(destination, isOnline, hasOfflineMaps): NavigationMode`
  - [ ] 2.5: Refactor `startNavigation()` to use routing strategy:
    ```kotlin
    when (mode) {
        ONLINE -> navigationRepository.getRoute() // Google Maps API
        OFFLINE -> mapboxOfflineManager.getOfflineRoute()
        UNAVAILABLE -> Result.failure(...)
    }
    ```
  - [ ] 2.6: Add `_navigationMode: MutableStateFlow<NavigationMode>` to track current mode
  - [ ] 2.7: Expose `navigationMode: StateFlow<NavigationMode>` for UI observation

- [ ] Task 3: Implement network state monitoring in NavigationService (AC: 1, 2, 6, 7)
  - [ ] 3.1: Open NavigationService.kt (foreground service from Story 6.3)
  - [ ] 3.2: Inject NetworkStateMonitor and OfflineMapRepository
  - [ ] 3.3: Create `monitorNetworkTransitions()` coroutine function
  - [ ] 3.4: Observe `networkStateMonitor.isNetworkAvailable` StateFlow with 2-second debounce (reuse Story 6.6 pattern)
  - [ ] 3.5: On network loss (online → offline):
    - [ ] 3.5a: Check if offline maps available: `offlineMapRepository.isOfflineMapAvailable(destinationLocationId)`
    - [ ] 3.5b: If yes: call `switchToOfflineMode()`
    - [ ] 3.5c: If no: announce "Lost internet connection. Cannot continue navigation. Waiting for connectivity."
  - [ ] 3.6: On network restored (offline → online):
    - [ ] 3.6a: Call `switchToOnlineMode()`
    - [ ] 3.6b: Trigger route recalculation (Story 6.4's recalculateRoute())

- [ ] Task 4: Implement `switchToOfflineMode()` in NavigationService (AC: 2, 3, 4)
  - [ ] 4.1: Create suspend fun `switchToOfflineMode()`
  - [ ] 4.2: Announce: "Lost internet connection. Continuing navigation using offline maps."
  - [ ] 4.3: Update NavigationManager.navigationMode = OFFLINE
  - [ ] 4.4: Pause any pending Google Maps API requests
  - [ ] 4.5: Load offline route from MapboxOfflineManager if not already loaded
  - [ ] 4.6: Continue turn-by-turn guidance using cached route steps
  - [ ] 4.7: Disable live traffic updates
  - [ ] 4.8: Update NavigationProgress state to reflect offline mode

- [ ] Task 5: Implement `switchToOnlineMode()` in NavigationService (AC: 6, 7)
  - [ ] 5.1: Create suspend fun `switchToOnlineMode()`
  - [ ] 5.2: Announce: "Internet connected. Updating route with live traffic."
  - [ ] 5.3: Update NavigationManager.navigationMode = ONLINE
  - [ ] 5.4: Trigger immediate route recalculation using Story 6.4's `recalculateRoute()`
  - [ ] 5.5: Resume live traffic updates
  - [ ] 5.6: Update NavigationProgress with new route and ETA
  - [ ] 5.7: Continue turn-by-turn guidance with recalculated route

- [ ] Task 6: Modify DeviationDetector for offline mode awareness (AC: 5)
  - [ ] 6.1: Open DeviationDetector.kt (created in Story 6.4)
  - [ ] 6.2: Inject NavigationManager to observe navigationMode StateFlow
  - [ ] 6.3: In `detectDeviation()`, check current navigation mode
  - [ ] 6.4: If mode == OFFLINE and deviation detected:
    - [ ] 6.4a: Announce: "Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity."
    - [ ] 6.4b: Do NOT trigger automatic recalculation
    - [ ] 6.4c: Continue providing guidance for original route
  - [ ] 6.5: If mode == ONLINE and deviation detected:
    - [ ] 6.5a: Use existing Story 6.4 recalculation logic
    - [ ] 6.5b: Announce: "You have gone off route. Recalculating directions."

- [ ] Task 7: Update NavigationAnnouncementManager with mode transition announcements (AC: 2, 5, 6)
  - [ ] 7.1: Open NavigationAnnouncementManager.kt (created in Story 6.3)
  - [ ] 7.2: Add method: `announceNetworkLoss(hasOfflineMaps: Boolean)`
    - If hasOfflineMaps: "Lost internet connection. Continuing navigation using offline maps."
    - Else: "Lost internet connection. Cannot continue navigation. Waiting for connectivity."
  - [ ] 7.3: Add method: `announceNetworkRestored()`
    - "Internet connected. Updating route with live traffic."
  - [ ] 7.4: Add method: `announceOfflineDeviation()`
    - "Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity."
  - [ ] 7.5: Ensure announcements have navigation priority (Priority 1 from Epic 8)
  - [ ] 7.6: All announcements respect speech rate preference (Story 5.1)

- [ ] Task 8: Create NavigationModeIndicator UI component (AC: UI feedback)
  - [ ] 8.1: Create navigation_mode_indicator.xml layout
  - [ ] 8.2: Include icon (WiFi/Offline icon) + text label
  - [ ] 8.3: Three states:
    - ONLINE: Green WiFi icon, "Live directions"
    - OFFLINE: Blue offline icon, "Using offline maps"
    - UNAVAILABLE: Red warning icon, "No navigation available"
  - [ ] 8.4: TalkBack contentDescription: "Navigation mode: [mode]"
  - [ ] 8.5: High-contrast mode support (7:1 contrast ratio)
  - [ ] 8.6: Large text mode support (scales with user preference)

- [ ] Task 9: Integrate NavigationModeIndicator into NavigationActiveFragment (AC: UI feedback)
  - [ ] 9.1: Open NavigationActiveFragment.kt (created in Story 6.3)
  - [ ] 9.2: Add NavigationModeIndicator to fragment_navigation_active.xml layout
  - [ ] 9.3: Position indicator: top-right corner (doesn't obstruct turn indicator)
  - [ ] 9.4: Observe NavigationManager.navigationMode StateFlow in ViewModel
  - [ ] 9.5: Update indicator UI when mode changes
  - [ ] 9.6: Announce mode change via TalkBack when indicator updates
  - [ ] 9.7: Test that indicator remains visible and accessible throughout navigation

- [ ] Task 10: Update NavigationActiveViewModel with mode state (AC: UI feedback)
  - [ ] 10.1: Open NavigationActiveViewModel.kt
  - [ ] 10.2: Inject NavigationManager
  - [ ] 10.3: Expose navigationMode: StateFlow<NavigationMode> from NavigationManager
  - [ ] 10.4: Combine with navigationProgress for comprehensive UI state
  - [ ] 10.5: Add computed property: `isOfflineMode: Boolean`
  - [ ] 10.6: Add computed property: `canRecalculate: Boolean` (false when offline)

- [ ] Task 11: Handle edge case - offline maps expire during navigation (AC: 8)
  - [ ] 11.1: In `switchToOfflineMode()`, check offline map expiration status
  - [ ] 11.2: If map expires within 1 hour: add warning to announcement
    - "Lost internet connection. Continuing navigation using offline maps. Note: Offline maps will expire soon."
  - [ ] 11.3: If map already expired: graceful fallback
    - "Lost internet connection. Offline maps expired. Basic guidance only."
  - [ ] 11.4: Basic guidance mode: announce straight-line distance and bearing to destination
    - "Destination is 500 meters northeast. Continue in current direction."

- [ ] Task 12: Implement fallback guidance when offline maps unavailable (AC: 8)
  - [ ] 12.1: Create `BasicGuidanceProvider.kt` in navigation/offline/
  - [ ] 12.2: Calculate bearing from current location to destination
  - [ ] 12.3: Calculate straight-line distance (haversine formula)
  - [ ] 12.4: Announce every 100 meters: "Destination is 400 meters northeast."
  - [ ] 12.5: Announce cardinal direction changes: "Turn north to continue toward destination."
  - [ ] 12.6: Announce arrival: "You are within 10 meters of destination."
  - [ ] 12.7: Basic guidance does NOT provide turn-by-turn (only compass-style directions)

- [ ] Task 13: Update NavigationProgress model to include mode (AC: State management)
  - [ ] 13.1: Open NavigationProgress.kt data class
  - [ ] 13.2: Add field: `navigationMode: NavigationMode`
  - [ ] 13.3: Add field: `isOfflineMapAvailable: Boolean`
  - [ ] 13.4: Add field: `canRecalculate: Boolean` (derived from mode)
  - [ ] 13.5: Ensure all NavigationProgress emissions include mode state

- [ ] Task 14: Write unit tests for mode switching logic (AC: All)
  - [ ] 14.1: Create NavigationManagerModeTest.kt
  - [ ] 14.2: Test determineNavigationMode() decision matrix:
    - isOnline=true, hasOfflineMaps=true → ONLINE
    - isOnline=false, hasOfflineMaps=true → OFFLINE
    - isOnline=false, hasOfflineMaps=false → UNAVAILABLE
  - [ ] 14.3: Test switchToOfflineMode() behavior:
    - Announces correctly
    - Updates mode state
    - Disables recalculation
  - [ ] 14.4: Test switchToOnlineMode() behavior:
    - Announces correctly
    - Triggers recalculation
    - Resumes live traffic
  - [ ] 14.5: Test DeviationDetector offline mode handling:
    - No recalculation attempted
    - Correct announcement
  - [ ] 14.6: Use MockK for dependencies, kotlinx-coroutines-test for Flow testing

- [ ] Task 15: Write integration tests for network transitions (AC: All)
  - [ ] 15.1: Create NavigationModeTransitionIntegrationTest.kt
  - [ ] 15.2: Test full online → offline transition during active navigation
  - [ ] 15.3: Test full offline → online transition during active navigation
  - [ ] 15.4: Test deviation handling in both modes
  - [ ] 15.5: Test TalkBack announcements for all mode changes
  - [ ] 15.6: Test UI indicator updates correctly
  - [ ] 15.7: Use Hilt AndroidTest, Espresso, mock NetworkStateMonitor

- [ ] Task 16: Manual device testing (AC: All)
  - [ ] 16.1: Start navigation with internet enabled (online mode)
  - [ ] 16.2: Enable airplane mode mid-navigation → verify offline switch announcement
  - [ ] 16.3: Verify navigation continues with cached route
  - [ ] 16.4: Deviate from route in offline mode → verify no recalculation announcement
  - [ ] 16.5: Disable airplane mode → verify online switch announcement
  - [ ] 16.6: Verify route recalculates immediately
  - [ ] 16.7: Test with destination that has NO offline maps → verify unavailable handling
  - [ ] 16.8: Test offline map expiration during navigation
  - [ ] 16.9: Verify TalkBack announces all mode transitions
  - [ ] 16.10: Verify navigation mode indicator updates correctly
  - [ ] 16.11: Test on Samsung Galaxy A12 (project device)

- [ ] Task 17: Update sprint-status.yaml to mark 7-5 as ready-for-dev
  - [ ] 17.1: Load _bmad-output/implementation-artifacts/sprint-status.yaml
  - [ ] 17.2: Find development_status key: 7-5-automatic-gps-offline-navigation-mode-switching
  - [ ] 17.3: Update status from "backlog" to "ready-for-dev"
  - [ ] 17.4: Save file preserving all comments and structure

## Dev Notes

### Critical Story 7.5 Context and Dependencies

**Epic 7 Goal:** Users navigate to favorite destinations quickly and maintain navigation capability in low-connectivity environments.

From [epics.md#Epic 7: Saved Locations & Offline Navigation]:

**Story 7.5 (THIS STORY):** Automatic GPS ↔ Offline Navigation Mode Switching
- **Purpose:** Enable seamless navigation mode switching based on network availability without user intervention
- **Deliverable:** Automatic mode detection, offline routing engine, mode transition announcements, UI indicator, deviation handling for both modes

**Story 7.5 Dependencies:**

**From Story 7.4 (Offline Map Pre-Caching):**
- **CRITICAL:** MapboxOfflineManager with offline tile storage (Mapbox SDK v10.16.0)
- **CRITICAL:** OfflineMapRepository.isOfflineMapAvailable(locationId) method
- **CRITICAL:** OfflineMapEntity database schema with status and expiration tracking
- **INTEGRATION POINT:** NavigationManager.startNavigation() must check offline map availability before routing
- **DEFERRED FROM 7.4:** getOfflineRoute() implementation - NOW REQUIRED for Story 7.5

**From Story 6.6 (Network Availability Indication):**
- **CRITICAL:** NetworkStateMonitor with reactive StateFlow<Boolean> for network state changes
- **CRITICAL:** 2-second debounce pattern to prevent flicker on unstable networks
- **INTEGRATION:** NetworkStateMonitor.isNetworkAvailable observed in NavigationService

**From Story 6.4 (Route Deviation Detection and Recalculation):**
- **CRITICAL:** DeviationDetector with 20m threshold and 5-second confirmation
- **CRITICAL:** NavigationRepository.recalculateRoute() method (Google Maps API)
- **INTEGRATION:** Disable automatic recalculation when in offline mode
- **ANNOUNCEMENT:** Different deviation messages for online vs offline modes

**From Story 6.3 (Turn-by-Turn Voice Guidance):**
- **REQUIRED:** NavigationService foreground service managing active navigation
- **REQUIRED:** NavigationAnnouncementManager for TTS announcements
- **REQUIRED:** NavigationActiveFragment UI showing turn-by-turn guidance
- **INTEGRATION:** Mode switching must not interrupt ongoing turn announcements

**From Story 6.2 (Google Maps Directions API):**
- **REQUIRED:** NavigationRepository with getRoute() and recalculateRoute() methods
- **REQUIRED:** NavigationRoute domain model
- **INTEGRATION:** Offline routing must produce compatible NavigationRoute format

**Critical Design Principle:**
> Mode switching must be completely automatic and seamless - users should hear a single announcement and navigation continues without interruption. Offline mode provides static routing (no traffic, no recalculation) but maintains full turn-by-turn guidance. Online mode resumes dynamic routing with live traffic when connectivity returns.

### Technical Requirements from Architecture Document

From [architecture.md#Offline Navigation Architecture]:

**Routing Strategy Pattern:**

Story 7.5 implements a **routing strategy pattern** in NavigationManager that intelligently chooses between:

1. **Online Routing (Google Maps API):** Live directions with real-time traffic, route recalculation on deviation
2. **Offline Routing (Mapbox):** Static cached routes from pre-downloaded tiles, no recalculation capability
3. **Basic Guidance (Fallback):** Compass-style bearing and distance when offline maps unavailable

**NavigationMode State Machine:**

```
┌─────────────────────────────────────────────────────────────────┐
│                      NAVIGATION START                            │
│  Check: isOnline? hasOfflineMaps? → Determine initial mode      │
└──────────────┬──────────────────────────────────────────────────┘
               │
     ┌─────────┴──────────┐
     │                    │
     ▼                    ▼
┌─────────┐        ┌─────────────┐
│ ONLINE  │◄──────►│   OFFLINE   │
│  MODE   │        │    MODE     │
└─────────┘        └─────────────┘
     │                    │
     │ Network Loss       │ Network Restored
     │ + hasOfflineMaps   │
     │                    │
     ▼                    ▼
  Announce:          Announce:
  "Lost internet     "Internet connected.
  connection.        Updating route with
  Continuing         live traffic."
  navigation         
  using offline      Trigger:
  maps."             - Route recalculation
                     - Resume live traffic
  Continue:          
  - Turn guidance    
  - Static route     
  - No recalc        
```

**Mode Determination Logic:**

```kotlin
fun determineNavigationMode(
    isOnline: Boolean,
    hasOfflineMaps: Boolean
): NavigationMode {
    return when {
        isOnline -> NavigationMode.ONLINE
        hasOfflineMaps -> NavigationMode.OFFLINE
        else -> NavigationMode.UNAVAILABLE
    }
}
```

**CRITICAL:** Offline maps are **preferred when online** - if user has offline maps, use them even with internet to save data and improve privacy. However, Story 7.5 focuses on automatic fallback for now.

### Library and Framework Requirements

**Mapbox Navigation SDK Integration (Story 7.5):**

From [app/build.gradle.kts]:

```kotlin
dependencies {
    // Story 7.4 added Mapbox Maps SDK
    implementation("com.mapbox.maps:android:10.16.0")
    
    // Story 7.5: Add Mapbox Navigation SDK for offline routing
    implementation("com.mapbox.navigation:android:2.17.0")
    implementation("com.mapbox.navigation:ui:2.17.0")
    
    // Note: Mapbox Navigation SDK includes offline routing APIs
    // - MapboxNavigation.requestRoutes() works with offline tiles
    // - OfflineRouter provides route calculation without network
}
```

**Mapbox Offline Routing API:**

```kotlin
// In MapboxOfflineManager (Story 7.5 enhancement)
suspend fun getOfflineRoute(
    origin: LatLng,
    destination: LatLng
): Result<NavigationRoute> = withContext(Dispatchers.IO) {
    try {
        // 1. Create RouteOptions for offline routing
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .coordinatesList(listOf(
                Point.fromLngLat(origin.longitude, origin.latitude),
                Point.fromLngLat(destination.longitude, destination.latitude)
            ))
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .build()
        
        // 2. Use OfflineRouter (works with cached tiles from Story 7.4)
        val router = OfflineRouter()
        val routes = router.getRoute(routeOptions).await()
        
        // 3. Convert Mapbox DirectionsRoute to NavigationRoute domain model
        val navigationRoute = routes.firstOrNull()?.let { mapboxRoute ->
            convertMapboxRouteToNavigationRoute(mapboxRoute)
        } ?: return@withContext Result.failure(
            IllegalStateException("No offline route found")
        )
        
        Result.success(navigationRoute)
    } catch (e: Exception) {
        Timber.e(e, "Failed to calculate offline route")
        Result.failure(e)
    }
}

private fun convertMapboxRouteToNavigationRoute(
    mapboxRoute: DirectionsRoute
): NavigationRoute {
    // Convert Mapbox route format to VisionFocus NavigationRoute
    // Ensure compatibility with existing turn-by-turn guidance
    return NavigationRoute(
        steps = mapboxRoute.legs()?.firstOrNull()?.steps()?.map { step ->
            NavigationStep(
                instruction = step.maneuver()?.instruction() ?: "",
                distance = step.distance(),
                duration = step.duration(),
                maneuverType = step.maneuver()?.type() ?: "",
                location = LatLng(
                    step.maneuver()?.location()?.latitude() ?: 0.0,
                    step.maneuver()?.location()?.longitude() ?: 0.0
                )
            )
        } ?: emptyList(),
        totalDistance = mapboxRoute.distance(),
        totalDuration = mapboxRoute.duration(),
        isOfflineRoute = true  // NEW field for Story 7.5
    )
}
```

**Key Differences: Google Maps API vs Mapbox Offline:**

| Feature | Google Maps API (Online) | Mapbox Offline |
|---------|--------------------------|----------------|
| Network Required | YES | NO |
| Live Traffic | YES | NO |
| Route Recalculation | YES (Story 6.4) | NO |
| Tile Storage | N/A | ~126 MB per 2km radius |
| Expiration | N/A | 30 days |
| Accuracy | Highest (live data) | Good (cached tiles) |

### File Structure Requirements

From [Project Structure Analysis]:

```
app/src/main/java/com/visionfocus/
├── navigation/
│   ├── NavigationManager.kt  [UPDATE - routing strategy pattern]
│   ├── NavigationManagerImpl.kt  [UPDATE - mode switching logic]
│   ├── NavigationService.kt  [UPDATE - network state monitoring]
│   ├── NavigationProgress.kt  [UPDATE - add navigationMode field]
│   ├── DeviationDetector.kt  [UPDATE - offline mode awareness]
│   ├── offline/
│   │   ├── MapboxOfflineManager.kt  [UPDATE - add getOfflineRoute()]
│   │   └── BasicGuidanceProvider.kt  [NEW - fallback guidance]
│   └── ui/
│       ├── NavigationActiveFragment.kt  [UPDATE - mode indicator]
│       ├── NavigationActiveViewModel.kt  [UPDATE - expose mode state]
│       └── NavigationModeIndicator.kt  [NEW - UI component]
├── data/
│   └── repository/
│       ├── OfflineMapRepository.kt  [EXISTS from Story 7.4]
│       └── NavigationRepository.kt  [EXISTS from Story 6.2]
├── core/
│   └── network/
│       └── NetworkStateMonitor.kt  [EXISTS from Story 6.6]
└── ui/
    └── components/
        └── NavigationModeIndicatorView.kt  [NEW - custom view]

app/src/main/res/
├── layout/
│   ├── fragment_navigation_active.xml  [UPDATE - add mode indicator]
│   └── navigation_mode_indicator.xml  [NEW - indicator layout]
├── drawable/
│   ├── ic_navigation_online.xml  [NEW - WiFi icon]
│   ├── ic_navigation_offline.xml  [NEW - offline icon]
│   └── ic_navigation_unavailable.xml  [NEW - warning icon]
└── values/
    └── strings.xml  [UPDATE - mode transition announcements]

app/src/test/java/com/visionfocus/
└── navigation/
    └── NavigationManagerModeTest.kt  [NEW]

app/src/androidTest/java/com/visionfocus/
└── navigation/
    └── NavigationModeTransitionIntegrationTest.kt  [NEW]
```

### Testing Requirements

From [Architecture Decision #4: Testing Strategy]:

**Unit Tests (≥80% coverage for business logic):**

**NavigationManagerModeTest.kt:**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationManagerModeTest {
    
    private lateinit var navigationManager: NavigationManagerImpl
    private lateinit var offlineMapRepository: OfflineMapRepository
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var navigationRepository: NavigationRepository
    private lateinit var mapboxOfflineManager: MapboxOfflineManager
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        offlineMapRepository = mockk(relaxed = true)
        networkStateMonitor = mockk(relaxed = true)
        navigationRepository = mockk(relaxed = true)
        mapboxOfflineManager = mockk(relaxed = true)
        
        navigationManager = NavigationManagerImpl(
            navigationRepository,
            offlineMapRepository,
            networkStateMonitor,
            mapboxOfflineManager
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `determineNavigationMode - online with offline maps returns ONLINE`() = runTest {
        // Given
        val isOnline = true
        val hasOfflineMaps = true
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.ONLINE, mode)
    }
    
    @Test
    fun `determineNavigationMode - offline with offline maps returns OFFLINE`() = runTest {
        // Given
        val isOnline = false
        val hasOfflineMaps = true
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.OFFLINE, mode)
    }
    
    @Test
    fun `determineNavigationMode - offline without offline maps returns UNAVAILABLE`() = runTest {
        // Given
        val isOnline = false
        val hasOfflineMaps = false
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.UNAVAILABLE, mode)
    }
    
    @Test
    fun `switchToOfflineMode - announces correctly and updates state`() = runTest {
        // Given
        val navigationService = mockk<NavigationService>(relaxed = true)
        coEvery { offlineMapRepository.isOfflineMapAvailable(any()) } returns true
        
        // When
        navigationService.switchToOfflineMode()
        advanceUntilIdle()
        
        // Then
        verify { 
            ttsManager.announce("Lost internet connection. Continuing navigation using offline maps.")
        }
        assertEquals(NavigationMode.OFFLINE, navigationManager.navigationMode.value)
    }
    
    @Test
    fun `switchToOnlineMode - triggers recalculation and announces`() = runTest {
        // Given
        val navigationService = mockk<NavigationService>(relaxed = true)
        coEvery { navigationRepository.recalculateRoute(any(), any()) } returns Result.success(mockk())
        
        // When
        navigationService.switchToOnlineMode()
        advanceUntilIdle()
        
        // Then
        verify { 
            ttsManager.announce("Internet connected. Updating route with live traffic.")
        }
        coVerify { navigationRepository.recalculateRoute(any(), any()) }
    }
    
    @Test
    fun `DeviationDetector - offline mode prevents recalculation`() = runTest {
        // Given
        val deviationDetector = DeviationDetector(navigationManager, navigationRepository)
        every { navigationManager.navigationMode } returns MutableStateFlow(NavigationMode.OFFLINE)
        
        // When
        val result = deviationDetector.detectDeviation(
            currentLocation = LatLng(51.5, -0.1),
            routePoints = listOf(LatLng(51.6, -0.2))
        )
        advanceUntilIdle()
        
        // Then
        assertTrue(result.isOffRoute)
        coVerify(exactly = 0) { navigationRepository.recalculateRoute(any(), any()) }
        verify { 
            ttsManager.announce("Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity.")
        }
    }
    
    @Test
    fun `getOfflineRoute - returns NavigationRoute with offline flag`() = runTest {
        // Given
        val origin = LatLng(51.5, -0.1)
        val destination = LatLng(51.6, -0.2)
        val mockRoute = mockk<NavigationRoute>(relaxed = true)
        coEvery { mapboxOfflineManager.getOfflineRoute(origin, destination) } returns Result.success(mockRoute)
        
        // When
        val result = navigationManager.getOfflineRoute(origin, destination)
        advanceUntilIdle()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isOfflineRoute == true)
    }
}
```

**Integration Tests:**

**NavigationModeTransitionIntegrationTest.kt:**
```kotlin
@HiltAndroidTest
class NavigationModeTransitionIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var navigationManager: NavigationManager
    
    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun onlineToOfflineTransition_duringNavigation_switchesMode() = runTest {
        // Given - Start navigation online
        val destination = SavedLocationEntity(1, "Home", 51.6, -0.2, 1000, 1000)
        navigationManager.startNavigation(destination.latitude, destination.longitude, destination.name)
        
        // When - Simulate network loss
        networkStateMonitor.setNetworkAvailable(false)
        Thread.sleep(2500)  // Wait for debounce + processing
        
        // Then
        onView(withText("Lost internet connection. Continuing navigation using offline maps."))
            .check(matches(isDisplayed()))
        onView(withId(R.id.navigationModeIndicator))
            .check(matches(withText("Using offline maps")))
    }
    
    @Test
    fun offlineToOnlineTransition_duringNavigation_recalculatesRoute() = runTest {
        // Given - Start in offline mode
        navigationManager.switchToOfflineMode()
        
        // When - Simulate network restored
        networkStateMonitor.setNetworkAvailable(true)
        Thread.sleep(2500)
        
        // Then
        onView(withText("Internet connected. Updating route with live traffic."))
            .check(matches(isDisplayed()))
        // Verify route recalculation triggered (check API call mock)
    }
    
    @Test
    fun deviationInOfflineMode_doesNotRecalculate() = runTest {
        // Given - Navigation active in offline mode
        navigationManager.switchToOfflineMode()
        
        // When - User deviates from route
        // Simulate location update 50m off route
        
        // Then
        onView(withText("Off route. Cannot recalculate without internet."))
            .check(matches(isDisplayed()))
        // Verify no API call to recalculateRoute
    }
}
```

### Accessibility Compliance

From [AccessibilityGuidelines.md] (Story 2.7):

**Navigation Mode Indicator Accessibility:**
- **Size:** 48×48 dp minimum (icon + text label)
- **contentDescription:** "Navigation mode: using offline maps" / "Navigation mode: live directions"
- **TalkBack announcements:** Mode changes announced automatically via TTS
- **High-contrast mode:** 7:1 contrast ratio for icon and text
- **Large text mode:** Text scales with user preference (SP units)

**Mode Transition Announcements:**
- **Priority:** Navigation priority (Priority 1) - interrupts recognition but not turn warnings
- **Timing:** Immediate announcement when mode changes (within 200ms)
- **Clarity:** Natural language, avoid technical jargon
  - ✅ "Lost internet connection. Continuing navigation using offline maps."
  - ❌ "Network unavailable. Switching to offline routing mode."

**Offline Deviation Announcement:**
- **Timing:** Immediate when deviation detected (same timing as online deviation)
- **Clarity:** Explain limitation explicitly
  - ✅ "Off route. Cannot recalculate without internet. Continue following directions or wait for connectivity."
  - ❌ "Off route. Recalculation unavailable."

**Touch Targets:**
- Navigation mode indicator: 48×48 dp minimum (accessible but not interactive - informational only)
- All buttons in navigation UI: 48×48 dp minimum maintained from Stories 6.3, 7.2

### Previous Story Intelligence

**From Story 7.4 (Offline Map Pre-Caching):**

**Critical Learnings:**
1. **Mapbox SDK Integration Complexity:** First use of Mapbox in project alongside Google Maps
   - Story 7.5 must understand both SDKs operate simultaneously
   - Google Maps API for online routing, Mapbox for offline routing
   - Careful initialization and API key management required

2. **Offline Map Availability Detection:** `isOfflineMapAvailable(locationId)` already implemented
   - Returns true only if status == "AVAILABLE" AND not expired
   - Story 7.5 uses this method extensively in routing strategy

3. **Repository Pattern Consistency:** OfflineMapRepository follows project architecture
   - Story 7.5's MapboxOfflineManager.getOfflineRoute() should follow same pattern
   - Use Result<T> for error handling
   - All database operations through repository, not direct DAO access

4. **Database Migration Already Complete:** Migration v5→v6 created offline_maps table
   - Story 7.5 does NOT need new migrations (NavigationRoute already exists)
   - Only enhancement: Add isOfflineRoute: Boolean field to NavigationRoute model

5. **TTS Announcements for Download:** Progress announcements at 25%, 50%, 75%, 100%
   - Story 7.5 mode transition announcements should follow same TTS patterns
   - Use NavigationAnnouncementManager (Story 6.3) for consistency

**From Story 6.6 (Network Availability Indication):**

**Critical Learnings:**
1. **NetworkStateMonitor Pattern:** Reactive StateFlow<Boolean> for network state
   - Story 7.5 observes this StateFlow in NavigationService
   - 2-second debounce prevents rapid mode switching on unstable networks
   - Pattern already validated in device testing

2. **Network Consent Already Granted:** User consented to network in Story 6.2
   - Story 7.5 does NOT need additional consent dialogs
   - Offline maps are user-triggered downloads (implicit consent)

3. **Network Status UI Component:** NetworkStatusComponent shows "Online"/"Offline"
   - Story 7.5's NavigationModeIndicator follows same design patterns
   - Consistent icon set (WiFi/Offline icons from Material Symbols)

**From Story 6.4 (Route Deviation Detection and Recalculation):**

**Critical Learnings:**
1. **Deviation Detection Thresholds:** 20m distance, 5-second confirmation
   - Story 7.5 reuses same thresholds for offline mode
   - Only difference: offline mode does NOT trigger recalculation

2. **DeviationDetector Architecture:** Coroutine-based, observes GPS location updates
   - Story 7.5 adds mode awareness: check NavigationMode before recalculating
   - Announcement logic already exists, just add offline-specific message

3. **Recalculation Timing:** Must complete within 3 seconds
   - Offline mode skips recalculation entirely (0 seconds)
   - Maintain same announcement timing for consistency

**From Story 6.3 (Turn-by-Turn Voice Guidance):**

**Critical Learnings:**
1. **NavigationService Lifecycle:** Foreground service with notification
   - Story 7.5 enhances this service with network state monitoring
   - Service already manages GPS location updates (1Hz)
   - Add network state observer to existing lifecycle

2. **NavigationAnnouncementManager:** Centralized TTS for navigation
   - Story 7.5 adds mode transition methods to this manager
   - Maintains consistent speech rate and priority handling

3. **Turn Warning Timing:** 70m advance, 15m immediate
   - Story 7.5 maintains same timing in offline mode
   - Offline routes use same NavigationRoute format (compatibility)

### Git Intelligence Summary

**Recent Commits (Last 5):**

```
1ab5db0 (HEAD → main) Story 7.3: Quick Navigation to Saved Locations - COMPLETE
4ab8fc2 Story 7.2 documentation update with completion details
4e40023 Story 7.2: Complete Saved Locations Management UI
4ab5db0 Story 7.1: Save Current Location - COMPLETE
9ca81fe Story 6.6: Network Availability Indication - Complete
```

**Commit Message Pattern:** `Story X.Y: [Component] - [Action]`
- Story 7.5 commits should follow: `Story 7.5: [Component] - [Action]`

**Testing After Implementation:**
- Every story has code review with 10-15 issues
- Manual device testing on Samsung Galaxy A12
- Story 7.5 requires thorough network transition testing (airplane mode toggling)

**Code Patterns Established:**

**1. StateFlow Observation in Services (Stories 6.3, 6.6):**
```kotlin
// Pattern for Story 7.5's NavigationService
lifecycleScope.launch {
    networkStateMonitor.isNetworkAvailable
        .debounce(2000L)
        .collect { isOnline ->
            handleNetworkTransition(isOnline)
        }
}
```

**2. Repository Error Handling (Stories 7.1-7.4):**
```kotlin
suspend fun operation(): Result<T> {
    return withContext(ioDispatcher) {
        try {
            // Validation
            // DAO/API operations
            Result.success(value)
        } catch (e: Exception) {
            Timber.e(e, "Operation failed")
            Result.failure(e)
        }
    }
}
```

**3. TTS Announcement Pattern (Stories 6.3, 7.4):**
```kotlin
// NavigationAnnouncementManager pattern
fun announceEvent(message: String, priority: Priority = Priority.NAVIGATION) {
    ttsManager.announceWithPriority(message, priority)
}
```

### Known Limitations and Future Work

**Story 7.5 Limitations:**

1. **Offline Routing Quality:** Mapbox offline routes may differ from Google Maps online routes
   - Same origin/destination might produce different turn sequences
   - User may notice route change when switching from online to offline mid-navigation
   - Mitigation: Announce mode switch clearly so user understands why route might differ

2. **No Traffic Awareness in Offline Mode:** Static routes without live traffic
   - User accustomed to traffic-aware routing may be surprised
   - Offline ETAs may be inaccurate (no congestion data)
   - Announcement: "Using offline maps. Estimated times may not reflect current traffic."

3. **Offline Map Coverage Gaps:** 2km radius may not cover entire route
   - If user deviates beyond cached tile boundary → no routing available
   - Falls back to basic guidance (compass-style)
   - Future enhancement: Expand offline map radius or multi-region caching

4. **Mode Switching Not Instantaneous:** Network state debounce + processing ~2 seconds
   - User may hear 1-2 turn announcements in old mode before switch completes
   - Mitigation: 2-second debounce prevents rapid switching on unstable networks (validated in Story 6.6)

5. **Offline Maps Expire:** 30-day expiration from Mapbox SDK
   - If maps expire mid-navigation → automatic degradation to basic guidance
   - Announcement warns user: "Offline maps expired. Basic guidance only."
   - Future enhancement: Background auto-refresh when on WiFi (Story 7.4 Task 9)

6. **Single-Destination Offline Maps:** Currently only destination has offline maps
   - If route passes through areas without cached tiles → gaps possible
   - Future enhancement: Multi-point route coverage analysis

**Future Story Dependencies:**

- **Story 8.1 (Audio Priority Queue):** Ensure mode switch announcements have correct priority
- **Story 8.2 (Confidence-Aware TTS):** May apply confidence phrasing to offline route quality
- **Epic 9 (Onboarding):** Explain offline navigation capability to new users

**Mapbox Offline Routing Limitations:**

**From Mapbox Documentation:**
- Offline routing uses tiles cached in Story 7.4 (2km radius default)
- Route quality depends on tile zoom levels (10-16 configured)
- No real-time traffic or incident data in offline mode
- Turn restrictions and road closures use cached data (may be outdated)
- Walking profile only (driving/cycling not supported in dissertation scope)

**Performance Considerations:**

- Offline route calculation: ~100-200ms (faster than Google Maps API ~500-1000ms)
- No network latency, but requires cached tiles present
- Mode switching overhead: ~2 seconds (network state debounce + TTS announcement)
- Battery impact: Offline mode slightly better (no API calls), GPS still active

### Security & Privacy Considerations

**Data Sensitivity:**
- Network state is not sensitive (no PII)
- Navigation mode preference may be stored (offline-first preference)
- No additional privacy concerns beyond Stories 6.2-6.6, 7.4

**Permission Requirements:**
- No new permissions required (network state already monitored in Story 6.6)
- All existing permissions apply: CAMERA, LOCATION, MICROPHONE

**Privacy-by-Design:**
- Offline mode enhances privacy (zero API calls to Google)
- Location data never transmitted during offline navigation
- Only online mode makes network requests (already consented in Story 6.2)

**Network Data Usage:**
- Online mode: Google Maps API calls (~10 KB per route request)
- Offline mode: ZERO network usage (all routing local)
- Mode switching minimizes data usage when connectivity poor

### References

**Technical Details with Source Paths:**

1. **Story 7.5 Requirements:**
   - [Source: _bmad-output/project-planning-artifacts/epics.md#Epic 7: Story 7.5]
   - AC1: Automatic mode switching without interruption
   - AC3: Offline navigation uses pre-cached route
   - AC5: Deviation handling without recalculation
   - AC7: Route recalculation resumes when online

2. **Mapbox Offline Routing:**
   - [Source: https://docs.mapbox.com/android/navigation/guides/offline/]
   - OfflineRouter API for route calculation
   - Works with cached tiles from Story 7.4
   - DirectionsRoute to NavigationRoute conversion

3. **NetworkStateMonitor Pattern:**
   - [Source: app/src/main/java/com/visionfocus/core/network/NetworkStateMonitor.kt]
   - Created in Story 6.6
   - Provides StateFlow<Boolean> for reactive observation
   - 2-second debounce prevents rapid switching

4. **DeviationDetector Enhancement:**
   - [Source: app/src/main/java/com/visionfocus/navigation/DeviationDetector.kt]
   - Created in Story 6.4
   - Story 7.5 adds mode awareness
   - Disables recalculation in offline mode

5. **NavigationAnnouncementManager:**
   - [Source: app/src/main/java/com/visionfocus/navigation/NavigationAnnouncementManager.kt]
   - Created in Story 6.3
   - Story 7.5 adds mode transition announcement methods
   - Maintains Priority 1 for navigation announcements

6. **OfflineMapRepository:**
   - [Source: app/src/main/java/com/visionfocus/data/repository/OfflineMapRepository.kt]
   - Created in Story 7.4
   - isOfflineMapAvailable() method used extensively
   - Story 7.5 integrates this with NavigationManager

7. **NavigationRoute Model:**
   - [Source: app/src/main/java/com/visionfocus/navigation/models/NavigationRoute.kt]
   - Story 7.5 adds: isOfflineRoute: Boolean field
   - Ensures compatibility between Google Maps and Mapbox routes
   - Used by NavigationActiveViewModel for UI state

8. **Accessibility Guidelines:**
   - [Source: docs/AccessibilityGuidelines.md]
   - Created in Story 2.7
   - Mode indicator accessibility requirements
   - TTS announcement patterns

9. **Testing Strategy:**
   - [Source: _bmad-output/architecture.md#Decision 4: Testing Strategy]
   - Unit tests: ≥80% coverage for NavigationManager mode logic
   - Integration tests: Network transition flows
   - Manual testing: Airplane mode toggling validation

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (via GitHub Copilot)

### Debug Log References

(To be populated during implementation)

### Completion Notes List

(To be populated after implementation)

### File List

(To be populated after implementation)
