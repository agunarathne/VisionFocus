# Manual Testing Guide: Story 4.4 - Continuous Scanning Mode

## Prerequisites
- Android device with camera
- VisionFocus app installed (debug build)
- Test environment with multiple distinct objects (chair, table, cup, bottle, laptop, etc.)

## Test Scenarios

### Test 1: Long-Press FAB Activation
**Objective:** Verify long-press gesture activates continuous scanning

**Steps:**
1. Launch VisionFocus app
2. Navigate to Recognition screen
3. Long-press the camera FAB for 2+ seconds
4. Release

**Expected Results:**
- ✅ Haptic feedback on long-press detection
- ✅ TTS announces: "Continuous scanning active. I'll announce objects as I detect them. Say 'Stop' to end."
- ✅ FAB icon changes from camera to stop icon
- ✅ FAB contentDescription changes to "Stop scanning"

---

### Test 2: Voice Command Activation
**Objective:** Verify "Scan environment" voice command works

**Steps:**
1. Launch VisionFocus app
2. Navigate to Recognition screen
3. Say "Scan environment"

**Expected Results:**
- ✅ TTS announces: "Continuous scanning active..."
- ✅ FAB icon changes to stop icon
- ✅ Scanning begins immediately

---

### Test 3: Object Detection and Announcement
**Objective:** Verify objects are detected and announced every 3 seconds

**Setup:** Place 3-5 distinct objects in camera view (chair, table, cup, bottle, laptop)

**Steps:**
1. Activate continuous scanning (long-press FAB or voice)
2. Point camera at first object (e.g., chair)
3. Wait 3 seconds
4. Move camera to second object (e.g., table)
5. Wait 3 seconds
6. Continue for all objects

**Expected Results:**
- ✅ Each NEW object is announced within ~3 seconds of being in view
- ✅ Announcements follow verbosity mode setting (Brief: "Chair", Standard: "Chair with high confidence", etc.)
- ✅ Confidence threshold ≥0.6 is respected (low confidence objects are NOT announced)

---

### Test 4: Duplicate Suppression
**Objective:** Verify duplicate objects are NOT re-announced

**Steps:**
1. Activate continuous scanning
2. Point camera at chair → Wait for announcement
3. Move camera away from chair
4. Move camera back to SAME chair
5. Wait 3+ seconds

**Expected Results:**
- ✅ Chair is announced the FIRST time
- ✅ Chair is NOT announced the second time (duplicate suppressed)

---

### Test 5: Manual Stop via FAB
**Objective:** Verify tapping FAB stops scanning with summary

**Setup:** Detect 3-5 objects during scanning

**Steps:**
1. Activate continuous scanning
2. Detect 3-5 distinct objects (listen for announcements)
3. Tap the FAB (stop icon)

**Expected Results:**
- ✅ Scanning stops immediately
- ✅ TTS announces summary: "Scanning stopped. I detected [count] objects: [list of objects]"
- ✅ FAB icon changes back to camera icon
- ✅ FAB contentDescription changes back to "Recognize objects"

---

### Test 6: Manual Stop via Voice Command
**Objective:** Verify "Stop" voice command stops scanning

**Steps:**
1. Activate continuous scanning
2. Detect 2-3 objects
3. Say "Stop" or "Stop scanning"

**Expected Results:**
- ✅ Scanning stops immediately
- ✅ TTS announces summary with detected objects
- ✅ FAB icon changes back to camera icon

---

### Test 7: Auto-Stop After 60 Seconds
**Objective:** Verify scanning auto-stops after 60-second timeout

**Steps:**
1. Activate continuous scanning
2. Let scanning run for full 60 seconds (do NOT manually stop)
3. Continue pointing camera at various objects

**Expected Results:**
- ✅ At 60 seconds, scanning stops automatically
- ✅ TTS announces: "Scan complete. I detected [count] objects: [list]"
- ✅ FAB icon changes back to camera icon

---

### Test 8: Announcement Queue (Multiple Objects)
**Objective:** Verify multiple objects detected simultaneously are queued

**Setup:** Arrange 3+ objects in camera view at once

**Steps:**
1. Activate continuous scanning
2. Point camera at scene with 3+ NEW objects visible
3. Wait for first capture interval (3 seconds)

**Expected Results:**
- ✅ All detected objects are announced sequentially
- ✅ Brief pause (~500ms) between announcements
- ✅ Announcements do NOT overlap
- ✅ Maximum 5 announcements queued (overflow is dropped)

---

### Test 9: Summary with >5 Objects
**Objective:** Verify summary truncates long object lists

**Setup:** Detect 6+ distinct objects during scanning

**Steps:**
1. Activate continuous scanning
2. Detect 6+ distinct objects (move camera to show different objects)
3. Stop scanning (manually or wait for timeout)

**Expected Results:**
- ✅ Summary announces: "I detected [count] objects including [first 5 objects]"
- ✅ Full count is stated but only first 5 objects listed

---

### Test 10: TalkBack Integration
**Objective:** Verify TalkBack compatibility

**Prerequisites:** Enable TalkBack in Android Accessibility Settings

**Steps:**
1. With TalkBack enabled, navigate to Recognition screen
2. Focus on camera FAB
3. Double-tap and hold FAB for 2+ seconds (TalkBack long-press)
4. Listen for TalkBack announcements during scanning

**Expected Results:**
- ✅ FAB contentDescription is read by TalkBack when focused
- ✅ Long-press activates scanning (with TalkBack feedback)
- ✅ Scanning start announcement is spoken by TalkBack
- ✅ Object detection announcements are spoken by TalkBack
- ✅ Summary announcement is spoken by TalkBack

---

### Test 11: Battery Performance
**Objective:** Verify battery drain is within acceptable range

**Equipment:** Fully charged Android device, battery monitor app

**Steps:**
1. Note starting battery percentage
2. Activate continuous scanning
3. Let scan run for full 60 seconds (auto-stop)
4. Note ending battery percentage

**Expected Results:**
- ✅ Battery drain ≤1-2% for 60-second scan
- ✅ Estimated drain ≤10-12% per hour if sustained
- ✅ Camera preview pauses between captures (not continuous)

---

### Test 12: Single-Tap Recognition Still Works
**Objective:** Verify single-tap FAB recognition is NOT affected

**Steps:**
1. Navigate to Recognition screen
2. Single-tap (quick tap) the camera FAB
3. Point camera at an object

**Expected Results:**
- ✅ Single recognition is triggered (NOT continuous scanning)
- ✅ Object is announced once
- ✅ FAB icon remains camera icon (does NOT change to stop)

---

## Regression Tests

### Regression 1: Story 2.3 FAB Behavior
- ✅ Single-tap FAB still works for single recognition
- ✅ FAB haptic feedback still works
- ✅ FAB TalkBack contentDescription still reads correctly

### Regression 2: Story 3.2 Voice Commands
- ✅ Existing voice commands still work ("Recognize", "What do you see?")
- ✅ "Stop" command only triggers when scanning is active

### Regression 3: Story 4.1 Verbosity Mode
- ✅ Continuous scanning announcements respect verbosity mode setting
- ✅ Brief mode: "Chair"
- ✅ Standard mode: "Chair with high confidence"
- ✅ Detailed mode: Full announcement (if applicable)

---

## Known Issues
- Pre-existing test compilation errors in HelpCommandTest, BackCommandTest, HomeCommandTest (unrelated to Story 4.4)
- Tests cannot be run via `gradlew test` due to these errors
- **Story 4.4 implementation compiles successfully** - assembleDebug passes

---

## Test Results Template

```
Date: __________
Tester: __________
Device: __________
Android Version: __________
Build: __________

| Test ID | Test Name | Result | Notes |
|---------|-----------|--------|-------|
| Test 1  | Long-Press FAB Activation | ☐ Pass ☐ Fail | |
| Test 2  | Voice Command Activation | ☐ Pass ☐ Fail | |
| Test 3  | Object Detection | ☐ Pass ☐ Fail | |
| Test 4  | Duplicate Suppression | ☐ Pass ☐ Fail | |
| Test 5  | Manual Stop (FAB) | ☐ Pass ☐ Fail | |
| Test 6  | Manual Stop (Voice) | ☐ Pass ☐ Fail | |
| Test 7  | Auto-Stop (60s) | ☐ Pass ☐ Fail | |
| Test 8  | Announcement Queue | ☐ Pass ☐ Fail | |
| Test 9  | Summary (>5 objects) | ☐ Pass ☐ Fail | |
| Test 10 | TalkBack Integration | ☐ Pass ☐ Fail | |
| Test 11 | Battery Performance | ☐ Pass ☐ Fail | |
| Test 12 | Single-Tap Regression | ☐ Pass ☐ Fail | |

Overall Status: ☐ Pass ☐ Fail
```
