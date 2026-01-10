# Manual Test Plan - Story 7.3: Quick Navigation to Saved Locations

**Story:** Quick Navigation to Saved Locations  
**Device:** Samsung Galaxy A12s  
**Test Date:** {{TO BE COMPLETED}}  
**Tester:** {{TO BE COMPLETED}}

## Prerequisites

1. VisionFocus APK installed on device
2. Location permissions granted
3. TalkBack enabled for accessibility tests
4. At least 3 saved locations configured:
   - "Home" - 123 Main St, New York, NY
   - "Work" - 456 Office Ave, New York, NY  
   - "Gym" - 789 Fitness Blvd, New York, NY

---

## Test Cases

### TC 13.1: Save Test Locations

**Objective:** Set up saved locations for testing  
**Steps:**
1. Launch VisionFocus app
2. Navigate to SavedLocationsFragment (via menu/navigation)
3. Add location "Home" with address "123 Main St, New York, NY"
4. Add location "Work" with address "456 Office Ave, New York, NY"
5. Add location "Gym" with address "789 Fitness Blvd, New York, NY"

**Expected Result:**  
✅ 3 locations saved successfully  
✅ Locations visible in SavedLocationsFragment

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.2: Saved Locations Button in NavigationInputFragment

**Acceptance Criteria:** AC 1, 2, 3  
**Steps:**
1. Navigate to DestinationInputFragment (navigation menu)
2. Locate "Saved Locations" button (bookmark icon, 56x56dp)
3. Verify button is visible and accessible

**Expected Result:**  
✅ Button visible with bookmark icon  
✅ Button size 56x56dp (matches voice button)  
✅ Button positioned below destination input field  
✅ TalkBack announces: "Select saved location, button"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.3: Open Saved Locations Picker Dialog

**Acceptance Criteria:** AC 2, 3  
**Steps:**
1. From DestinationInputFragment, tap "Saved Locations" button
2. Observe dialog appearance

**Expected Result:**  
✅ Dialog opens with title "Select Saved Location"  
✅ All 3 saved locations displayed (Gym, Work, Home in order of lastUsedAt)  
✅ Each location shows name and address  
✅ TalkBack announces: "Select saved location dialog"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.4: Select Location from Picker Dialog

**Acceptance Criteria:** AC 7, 8  
**Steps:**
1. Open Saved Locations picker dialog
2. Tap "Home" location in list
3. Observe destination input field

**Expected Result:**  
✅ Dialog dismisses  
✅ Destination input field populated with "Home"  
✅ Navigation route requested (turn-by-turn guidance begins)  
✅ TTS announces: "Starting navigation to Home"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.5: Voice Command - Exact Match

**Acceptance Criteria:** AC 4, 5, 8  
**Steps:**
1. From any screen, activate voice input
2. Say: "navigate to home"
3. Observe navigation behavior

**Expected Result:**  
✅ Location "Home" matched  
✅ Navigation starts immediately to Home coordinates  
✅ Turn-by-turn guidance begins (Google Maps Directions API triggered)  
✅ TTS announces: "Starting navigation to Home"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.6: Voice Command - Case Insensitive

**Acceptance Criteria:** AC 5  
**Steps:**
1. Activate voice input
2. Say: "Navigate to HOME" (uppercase)
3. Observe navigation behavior

**Expected Result:**  
✅ Location "Home" matched (case-insensitive)  
✅ Navigation starts to Home

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.7: Voice Command - Fuzzy Match (Typo)

**Acceptance Criteria:** AC 5, 6  
**Steps:**
1. Activate voice input
2. Say: "navigate to hme" (missing 'o', distance: 1)
3. Observe navigation behavior

**Expected Result:**  
✅ Location "Home" matched via fuzzy matching  
✅ Navigation starts to Home  
✅ TTS announces: "Starting navigation to Home"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.8: Voice Command - No Match

**Acceptance Criteria:** AC 5  
**Steps:**
1. Activate voice input
2. Say: "navigate to xyz" (non-existent location)
3. Observe error handling

**Expected Result:**  
✅ Navigation does NOT start  
✅ TTS announces: "No saved location found matching: xyz"  
✅ CommandResult.Failure returned

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.9: Voice Command - Disambiguation (Multiple Matches)

**Acceptance Criteria:** AC 6  
**Steps:**
1. Add second location: "Home Depot" (similar to "Home")
2. Activate voice input
3. Say: "navigate to hom" (ambiguous - matches both "Home" and "Home Depot")
4. Observe disambiguation dialog

**Expected Result:**  
✅ Disambiguation dialog appears with both options  
✅ Dialog shows max 5 matches sorted by distance  
✅ TTS announces: "Multiple locations found. Select one."  
✅ User can tap desired location to proceed

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.10: Different Voice Command Keywords

**Acceptance Criteria:** AC 4  
**Steps:**
1. Test each command variation:
   - "navigate to Work"
   - "go to Work"
   - "take me to Work"
   - "directions to Work"
2. Verify all commands work

**Expected Result:**  
✅ All 4 command variations start navigation to Work  
✅ Location name extracted correctly in each case

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.11: TalkBack Announcements - Picker Dialog

**Acceptance Criteria:** AC 3  
**Steps:**
1. Enable TalkBack
2. Open Saved Locations picker dialog
3. Navigate through list items with swipe gestures

**Expected Result:**  
✅ Dialog title announced: "Select saved location dialog"  
✅ Each list item announces: "Saved location: [Name], [Address]"  
✅ Button announces: "Select saved location, button"

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.12: TalkBack Announcements - Voice Command

**Acceptance Criteria:** AC 3  
**Steps:**
1. With TalkBack enabled
2. Say: "navigate to Gym"
3. Listen for TTS announcements

**Expected Result:**  
✅ TTS announces: "Starting navigation to Gym"  
✅ Navigation instructions announced turn-by-turn

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.13: Verify lastUsedAt Timestamp Update

**Acceptance Criteria:** Story 7.2 implicit - timestamp for sorting  
**Steps:**
1. From SavedLocationsFragment, note current location order
2. Use voice command: "navigate to Home"
3. After navigation starts, return to SavedLocationsFragment
4. Observe location order

**Expected Result:**  
✅ "Home" moved to top of list (most recent lastUsedAt)  
✅ Location order reflects recency: Home, then others

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.14: Navigation Route Matches Manual Entry

**Acceptance Criteria:** AC 8  
**Steps:**
1. Manually enter "123 Main St, New York, NY" in destination input
2. Start navigation, observe route
3. Cancel navigation
4. Use voice command: "navigate to Home" (same address)
5. Compare routes

**Expected Result:**  
✅ Both routes identical (same origin, destination, steps)  
✅ Google Maps Directions API called in both cases  
✅ Turn-by-turn guidance identical

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

### TC 13.15: Empty Saved Locations - Button Behavior

**Acceptance Criteria:** AC 2  
**Steps:**
1. Delete all saved locations
2. Navigate to DestinationInputFragment
3. Tap "Saved Locations" button

**Expected Result:**  
✅ Dialog opens  
✅ Empty state message displayed: "No saved locations yet"  
✅ TTS announces empty state

**Actual Result:** {{TO BE COMPLETED}}  
**Pass/Fail:** {{TO BE COMPLETED}}

---

## Test Summary

**Total Test Cases:** 15  
**Passed:** {{TO BE COMPLETED}}  
**Failed:** {{TO BE COMPLETED}}  
**Blocked:** {{TO BE COMPLETED}}

**Issues Found:**
{{TO BE COMPLETED}}

**Notes:**
{{TO BE COMPLETED}}

---

## Acceptance Criteria Validation

| AC | Description | Test Cases | Status |
|----|-------------|-----------|--------|
| 1 | "Saved Locations" button in DestinationInputFragment | TC 13.2 | {{TO BE COMPLETED}} |
| 2 | Button opens picker dialog with saved locations list | TC 13.2, 13.3, 13.15 | {{TO BE COMPLETED}} |
| 3 | TalkBack announces button and dialog content | TC 13.2, 13.3, 13.11, 13.12 | {{TO BE COMPLETED}} |
| 4 | Voice command "navigate to [location]" supported | TC 13.5, 13.10 | {{TO BE COMPLETED}} |
| 5 | Fuzzy matching with Levenshtein distance ≤2 | TC 13.5, 13.6, 13.7, 13.8 | {{TO BE COMPLETED}} |
| 6 | Disambiguation for multiple fuzzy matches | TC 13.7, 13.9 | {{TO BE COMPLETED}} |
| 7 | Picker selection populates destination and triggers navigation | TC 13.4 | {{TO BE COMPLETED}} |
| 8 | Navigation flow identical to manual destination entry | TC 13.4, 13.5, 13.14 | {{TO BE COMPLETED}} |

---

## Device Information

**Device:** Samsung Galaxy A12s  
**Android Version:** {{TO BE COMPLETED}}  
**VisionFocus Version:** {{TO BE COMPLETED}}  
**Build Number:** {{TO BE COMPLETED}}  
**TalkBack Version:** {{TO BE COMPLETED}}

---

## Sign-off

**Tester Signature:** ______________________  
**Date:** {{TO BE COMPLETED}}  
**Result:** PASS / FAIL / CONDITIONAL PASS
