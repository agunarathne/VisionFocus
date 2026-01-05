# Manual Testing Guide - Story 7.1: Save Current Location with Custom Labels

**Date:** January 5, 2026  
**Device:** Samsung Galaxy A12 (SM-A127F)  
**Build:** app-debug.apk with Code Review Fixes  
**Tester:** Allan  
**Monitoring:** Active (SaveLocationDialog, SavedLocationRepository, LocationManager logs)

---

## Pre-Test Setup ✅

- [x] APK installed successfully
- [x] Log monitoring active
- [x] Device connected: 192.168.1.95:41945
- [x] GPS enabled on device
- [x] Location permission granted (from Epic 6 testing)

---

## Test Suite: Story 7.1 - Save Current Location

### TEST 1: Voice Command "Save Location" Opens Dialog ✓
**Acceptance Criteria:** AC #1, AC #2  
**Expected:** Dialog appears with TalkBack announcement: "Save current location. Enter a name."

**Steps:**
1. Open VisionFocus app
2. Say: **"Save location"** (or "Save here", "Bookmark location")
3. Listen for TalkBack announcement
4. Verify dialog title: "Save Current Location"
5. Verify name input field has label: "Location name, edit text"

**Pass Criteria:**
- [ ] Dialog appears within 2 seconds
- [ ] TalkBack announces: "Save current location. Enter a name."
- [ ] Name input field has accessibility label
- [ ] Voice input button visible (microphone icon)
- [ ] Save button is DISABLED initially
- [ ] Cancel button is enabled

**Expected Logs:**
```
SaveLocationCommand: Executing Save Location command
SaveLocationDialog: Dialog shown
LocationManager: getCurrentLocation() called
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Notes:



```

---

### TEST 2: GPS Location Retrieval with Loading Indicator ✓
**Acceptance Criteria:** AC #4, AC #8  
**Expected:** Loading indicator shows "Getting your location..." then GPS coordinates retrieved

**Steps:**
1. Continue from Test 1 (dialog open)
2. Observe loading indicator at top of dialog
3. Wait for GPS location fetch (should complete within 5 seconds)
4. Verify loading indicator disappears

**Pass Criteria:**
- [ ] Loading spinner + "Getting your location..." text appears
- [ ] TTS announces: "Getting your location..."
- [ ] Loading completes within 5 seconds
- [ ] Save button becomes enabled ONLY after valid name entered

**Expected Logs:**
```
LocationManager: GPS location retrieved: lat=XX.XXXX, lng=XX.XXXX
SaveLocationDialog: currentLatitude=XX.XXXX, currentLongitude=XX.XXXX
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
GPS Coordinates Retrieved:
Latitude: 
Longitude: 
Fetch Time: ____ seconds

Notes:



```

---

### TEST 3: Name Validation (Minimum 2 Characters) ✓
**Acceptance Criteria:** AC #5  
**Expected:** Save button disabled until name has at least 2 characters

**Steps:**
1. Continue from Test 2 (GPS loaded)
2. Tap name input field
3. Type: **"H"** (1 character)
4. Verify Save button is DISABLED
5. Type: **"o"** (now "Ho" = 2 characters)
6. Verify Save button becomes ENABLED
7. Delete one character (back to 1 char)
8. Verify Save button becomes DISABLED again

**Pass Criteria:**
- [ ] Save button DISABLED with 0-1 characters
- [ ] Save button ENABLED with 2+ characters
- [ ] Error message appears if < 2 chars: "Location name must be at least 2 characters"
- [ ] Character counter shows "X/100"

**Expected Logs:**
```
SaveLocationDialog: Name validation: length=1, Save button disabled
SaveLocationDialog: Name validation: length=2, Save button enabled
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Behavior Observed:



```

---

### TEST 4: Save Location Successfully ✓
**Acceptance Criteria:** AC #7, AC #8, AC #9, AC #10  
**Expected:** Location saved to encrypted database with success announcement

**Steps:**
1. Continue from Test 3 (valid name entered)
2. Enter name: **"Test Home"**
3. Tap **Save** button
4. Listen for TTS confirmation
5. Verify dialog dismisses

**Pass Criteria:**
- [ ] TTS announces: "Location saved as Test Home"
- [ ] Dialog dismisses automatically
- [ ] No error messages
- [ ] Save completes within 2 seconds

**Expected Logs:**
```
SavedLocationRepository: Saved location: Test Home (id=1) at (XX.XXXX, XX.XXXX)
SaveLocationDialog: Location saved successfully
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Location ID: 
Confirmation Message: 
Notes:



```

---

### TEST 5: Duplicate Name Detection ✓
**Acceptance Criteria:** AC #6  
**Expected:** Duplicate name shows confirmation dialog with overwrite/choose different options

**Steps:**
1. Say: **"Save location"** again
2. Wait for dialog and GPS loading
3. Enter SAME name: **"Test Home"**
4. Tap **Save** button
5. Verify duplicate confirmation dialog appears
6. Read message: "You already have a location named Test Home. Overwrite or choose a different name?"
7. Tap **Choose Different**
8. Dialog stays open, name field focused

**Pass Criteria:**
- [ ] Duplicate confirmation dialog appears
- [ ] Message mentions existing location name
- [ ] Two buttons: "Overwrite" and "Choose Different"
- [ ] TTS announces confirmation message
- [ ] Choosing "Different" keeps dialog open

**Expected Logs:**
```
SavedLocationRepository: Duplicate location name detected: Test Home (id=1)
SaveLocationDialog: Showing duplicate confirmation dialog
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Dialog Message: 
User Choice: [ Overwrite / Choose Different ]
Notes:



```

---

### TEST 6: Overwrite Existing Location (Code Review Fix) ✓
**Acceptance Criteria:** AC #6 (overwrite branch)  
**Expected:** Overwrite updates existing location atomically (no race condition)

**Steps:**
1. Continue from Test 5
2. Enter name: **"Test Home"** again
3. Tap **Save** button
4. On duplicate dialog, tap **Overwrite**
5. Verify success message
6. Dialog dismisses

**Pass Criteria:**
- [ ] TTS announces: "Location saved as Test Home"
- [ ] Dialog dismisses
- [ ] No "duplicate name" error after overwrite
- [ ] Coordinates updated (verify in logs)

**Expected Logs (Code Review Fix Validation):**
```
SavedLocationRepository: findLocationByName(Test Home) returned existing
SaveLocationDialog: Location overwritten: Test Home (id=1)
SavedLocationRepository: Updated location: Test Home (id=1)
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Code Review Fix Verified: [ YES / NO ]
(Should see updateLocation() call, NOT delete + save)

Notes:



```

---

### TEST 7: Multiple Locations Save Successfully ✓
**Acceptance Criteria:** AC #7, AC #8  
**Expected:** Multiple locations stored in database with unique names

**Steps:**
1. Say: **"Save location"**
2. Enter name: **"Test Work"**
3. Tap **Save**
4. Wait for confirmation
5. Say: **"Save location"** again
6. Enter name: **"Test Gym"**
7. Tap **Save**
8. Wait for confirmation

**Pass Criteria:**
- [ ] Both locations saved successfully
- [ ] Unique IDs assigned (id=1, id=2, id=3...)
- [ ] No duplicate errors
- [ ] Success announcements for both

**Expected Logs:**
```
SavedLocationRepository: Saved location: Test Work (id=2) at (XX.XXXX, XX.XXXX)
SavedLocationRepository: Saved location: Test Gym (id=3) at (XX.XXXX, XX.XXXX)
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Total Locations Saved: 
Location IDs: 
Notes:



```

---

### TEST 8: Cancel Button Dismisses Dialog ✓
**Acceptance Criteria:** AC #1 (dialog interaction)  
**Expected:** Cancel button dismisses dialog without saving

**Steps:**
1. Say: **"Save location"**
2. Wait for dialog and GPS loading
3. Enter name: **"Test Cancel"**
4. Tap **Cancel** button
5. Verify dialog dismisses
6. No save operation occurs

**Pass Criteria:**
- [ ] Dialog dismisses immediately
- [ ] No save logs appear
- [ ] No TTS "location saved" announcement

**Expected Logs:**
```
SaveLocationDialog: Save location cancelled by user
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Notes:



```

---

### TEST 9: Voice Input Button (Placeholder) ✓
**Acceptance Criteria:** AC #3  
**Expected:** Voice input button shows placeholder message (Epic 3 not complete)

**Steps:**
1. Say: **"Save location"**
2. Wait for dialog
3. Tap **microphone button** (voice input)
4. Listen for TTS announcement

**Pass Criteria:**
- [ ] TTS announces: "Voice input feature coming soon. Please type location name."
- [ ] Dialog remains open
- [ ] No crash or error

**Expected Logs:**
```
SaveLocationDialog: Voice input button clicked (placeholder)
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Notes:



```

---

### TEST 10: GPS Permission Denied Handling (Code Review Fix) ✓
**Acceptance Criteria:** AC #4 (error handling)  
**Expected:** Clear error message if GPS permission denied

**Steps:**
1. Go to Android Settings → Apps → VisionFocus → Permissions
2. **Deny** Location permission
3. Return to VisionFocus
4. Say: **"Save location"**
5. Verify error dialog appears
6. Read message about location permission required

**Pass Criteria:**
- [ ] Error dialog title: "Location Permission Required"
- [ ] Clear message about needing location access
- [ ] Save button and input fields DISABLED (Code Review Fix)
- [ ] TTS announces error message
- [ ] Tapping "OK" dismisses both dialogs

**Expected Logs:**
```
LocationManager: Permission denied
SaveLocationDialog: Location error - permission denied
SaveLocationDialog: Save button disabled
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Code Review Fix Verified: [ YES / NO ]
(Inputs should be disabled when error shown)

Notes:



```

---

### TEST 11: GPS Disabled Handling ✓
**Acceptance Criteria:** AC #4 (error handling)  
**Expected:** Clear error message if GPS is turned off

**Steps:**
1. Re-enable Location permission in Settings
2. Turn OFF GPS/Location services in device settings
3. Return to VisionFocus
4. Say: **"Save location"**
5. Verify error dialog appears

**Pass Criteria:**
- [ ] Error dialog title: "GPS Unavailable"
- [ ] Message about enabling location services
- [ ] Save button and inputs disabled
- [ ] TTS announces error

**Expected Logs:**
```
LocationManager: GPS disabled
SaveLocationDialog: Location error - GPS unavailable
```

**Actual Results:**
```
Status: [ PASS / FAIL ]
Notes:



```

---

### TEST 12: TalkBack Navigation (Accessibility) ✓
**Acceptance Criteria:** AC #2, Story 2.7 accessibility requirements  
**Expected:** All interactive elements accessible via TalkBack

**Steps:**
1. Enable TalkBack on device
2. Say: **"Save location"**
3. Swipe right through dialog elements
4. Verify focus order: Title → Name Input → Voice Button → Save → Cancel

**Pass Criteria:**
- [ ] Title announces: "Save Current Location"
- [ ] Input announces: "Location name, edit text"
- [ ] Voice button announces: "Use voice to enter name"
- [ ] Save button announces: "Save location"
- [ ] Cancel button announces: "Cancel save location"
- [ ] All buttons minimum 48×48 dp (easy to tap)

**Actual Results:**
```
Status: [ PASS / FAIL ]
TalkBack Working: [ YES / NO ]
Notes:



```

---

### TEST 13: Database Encryption Verification ✓
**Acceptance Criteria:** AC #9 (SQLCipher encryption)  
**Expected:** Saved locations encrypted at rest

**Steps:**
1. Save 2-3 locations (Tests 4, 7)
2. Close VisionFocus
3. Run: `adb -s 192.168.1.95:41945 shell "run-as com.visionfocus cat databases/visionfocus_database" | head -n 20`
4. Verify output is binary/encrypted (not plain text)

**Pass Criteria:**
- [ ] Database file is encrypted (binary data, not readable text)
- [ ] Cannot see location names in plaintext
- [ ] SQLCipher working correctly

**Actual Results:**
```
Status: [ PASS / FAIL ]
Encryption Verified: [ YES / NO ]
Notes:



```

---

## Code Review Fix Validations

### FIX 1: @Index Annotations (HIGH-1) ✓
**Expected:** Query performance improved with indexes on `name` and `lastUsedAt`

**Validation:**
- Check Room schema export includes indexes
- Run: `.\gradlew.bat :app:exportSchema`
- Verify `app/schemas/com.visionfocus.data.local.AppDatabase/4.json` has indices array

**Result:**
```
Status: [ PASS / FAIL ]
Indexes Found: [ YES / NO ]
```

---

### FIX 2: Hilt Injection (HIGH-2) ✓
**Expected:** No dependency injection errors, proper repository/TTS/LocationManager injection

**Validation:**
- All tests above should pass without null pointer exceptions
- Check logs for Hilt injection success

**Result:**
```
Status: [ PASS / FAIL ]
No Null Pointer Errors: [ YES / NO ]
```

---

### FIX 3: @IODispatcher (MEDIUM-3) ✓
**Expected:** Repository uses injected dispatcher

**Validation:**
- Check `SavedLocationRepositoryImpl.kt` imports `@IODispatcher`
- Verify constructor injection working (no crashes)

**Result:**
```
Status: [ PASS / FAIL ]
```

---

### FIX 4: Race Condition Fix (MEDIUM-4) ✓
**Expected:** Overwrite uses `updateLocation()` not `delete + save`

**Validation:**
- Test 6 logs should show: "Updated location: Test Home (id=1)"
- Should NOT show: "Deleted location" followed by "Saved location"

**Result:**
```
Status: [ PASS / FAIL ]
Atomic Update Confirmed: [ YES / NO ]
```

---

### FIX 5: Dialog Dismiss Fix (MEDIUM-5) ✓
**Expected:** Inputs disabled when location error occurs

**Validation:**
- Test 10 should show disabled Save button and name input after permission denied
- No confusing double-dialog stacking

**Result:**
```
Status: [ PASS / FAIL ]
Inputs Disabled on Error: [ YES / NO ]
```

---

## Test Summary

**Total Tests:** 13  
**Passed:** ___  
**Failed:** ___  
**Skipped:** ___  

**Critical Issues Found:** ___  
**Medium Issues Found:** ___  
**Low Issues Found:** ___  

**Overall Status:** [ PASS / FAIL / PARTIAL ]

---

## Notes & Observations

### Performance
- Average dialog load time: ____ seconds
- Average GPS fetch time: ____ seconds
- Average save operation time: ____ seconds

### UX Observations
```




```

### Bugs Found
```




```

### Code Review Fixes Validation
- HIGH-1 (Indexes): [ PASS / FAIL ]
- HIGH-2 (Hilt): [ PASS / FAIL ]
- MEDIUM-3 (IODispatcher): [ PASS / FAIL ]
- MEDIUM-4 (Race Condition): [ PASS / FAIL ]
- MEDIUM-5 (Dialog Dismiss): [ PASS / FAIL ]

---

## Tester Sign-off

**Tested By:** Allan  
**Date:** January 5, 2026  
**Time Started:** _____  
**Time Completed:** _____  
**Total Duration:** _____ minutes  

**Recommendation:** [ APPROVE FOR PRODUCTION / NEEDS FIXES / BLOCKED ]

**Next Steps:**
```
1. 
2. 
3. 
```

---

## Log Monitoring Commands

**View saved location database stats:**
```powershell
adb -s 192.168.1.95:41945 shell "run-as com.visionfocus ls -lh databases/"
```

**Check saved locations count:**
```powershell
adb -s 192.168.1.95:41945 logcat -d | Select-String "Saved location:"
```

**View all VisionFocus logs:**
```powershell
adb -s 192.168.1.95:41945 logcat -d | Select-String "VisionFocus"
```
