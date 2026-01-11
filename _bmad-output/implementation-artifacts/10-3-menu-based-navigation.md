# Story 10.3: Menu-Based Navigation for Accessibility

Status: done

## Story

As a visually impaired user,
I want to access key features like Saved Locations, Smart Tags, and History via the main menu,
So that I can navigate the application using standard menu structures without relying solely on voice commands or gestures.

## Acceptance Criteria

**Given** the user is on the main screen (Recognition mode)
**When** the user opens the Options Menu (top right, or via TalkBack)
**Then** the following menu items are available in this order:
  1. Saved Out-Door Locations
  2. Saved In-Door Tags
  3. Object Detection History
  4. Settings (existing)

**Given** the user selects "Saved Out-Door Locations"
**Then** the app navigates to the Saved Locations screen (`SavedLocationsFragment`)

**Given** the user selects "Saved In-Door Tags"
**Then** the app navigates to the Smart Tags / Beacon Management screen (`BeaconManagementFragment`)

**Given** the user selects "Object Detection History"
**Then** the app navigates to the Recognition History screen (`HistoryFragment`)

**Given** any menu item is selected
**Then** TalkBack announces the new screen title appropriately

## Tasks / Subtasks

- [x] Task 1: Update String Resources (AC: Menu Titles)
  - [x] 1.1: Add "Saved Out-Door Locations" string
  - [x] 1.2: Add "Saved In-Door Tags" string
  - [x] 1.3: Add "Object Detection History" string
  - [x] 1.4: Add content descriptions for these menu items

- [x] Task 2: Update Main Menu Resource (AC: Menu Items)
  - [x] 2.1: Edit `res/menu/main_menu.xml`
  - [x] 2.2: Add the three new items before "Settings"
  - [x] 2.3: Ensure proper IDs and string references

- [x] Task 3: Implement Navigation Logic in MainActivity (AC: Navigation)
  - [x] 3.1: Update `MainActivity.onOptionsItemSelected` to handle `action_saved_locations`
  - [x] 3.2: Update `MainActivity.onOptionsItemSelected` to handle `action_smart_tags`
  - [x] 3.3: Update `MainActivity.onOptionsItemSelected` to handle `action_history`
  - [x] 3.4: Ensure proper fragment navigation using `NavController`

- [x] Task 4: Verify Navigation Graph IDs (AC: Configuration)
  - [x] 4.1: Confirm `nav_graph.xml` IDs match code assumptions: `savedLocationsFragment`, `beaconManagementFragment`, `historyFragment`

## Dev Notes

**User Request:**
"I want to include some menu items which some currently functioning by the support of voice command.(Low vision people can manage with menus)"
Preferred names:
1) Saved Locations screen -> "Saved Out-Door Locations"
2) Manage Smart Tags Screen -> "Saved In-Door Tags"
3) Object Detection History screen -> "Object Detection History"

**Architecture:**
- Use `MainActivity`'s existing `NavController` logic.
- Reuse `navigateToHistory`, `navigateToSavedLocations`, `navigateToSettings`.
- Need to add `navigateToSmartTags` (or similar).

**Accessibility:**
- Menu items are standard Android `Options Menu` items, which are natively accessible.
- Titles should be clear and distinct.

**Files to Modify:**
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/menu/main_menu.xml`
- `app/src/main/java/com/visionfocus/MainActivity.kt`

## Dev Agent Record

### Agent Model Used
Gemini 3 Pro (Preview)

### Debug Log References

### Completion Notes List
- Implemented menu based navigation for Saved Locations, Smart Tags, and History.
- Updated `MainActivity` to handle new menu items.
- Updated `strings.xml` and `main_menu.xml`.
- Verified navigation graph IDs match `MainActivity` navigation logic.
- Complies with user request for specific menu names: "Saved Out-Door Locations", "Saved In-Door Tags", "Object Detection History".

### File List
- app/src/main/res/values/strings.xml
- app/src/main/res/menu/main_menu.xml
- app/src/main/java/com/visionfocus/MainActivity.kt

### Change Log
| Date | Author | Change |
|------|--------|--------|
| 2026-01-11 | Dev Agent (Gemini 3 Pro) | Implemented menu-based navigation for accessibility Story 10.1 |

