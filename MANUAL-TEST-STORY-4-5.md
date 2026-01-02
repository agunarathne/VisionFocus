# Manual Test Guide - Story 4.5: Distance and Position Information

**Date:** January 2, 2026  
**Device:** Samsung (connected via ADB)  
**Build:** Clean install with code review fixes (commits fcd22f2, 989d64a)  
**Log Monitoring:** Active (separate PowerShell window)

---

## ✅ Pre-Test Checklist

- [x] Phone connected via ADB (192.168.8.100:32853)
- [x] Clean build completed (./gradlew clean + assembleDebug)
- [x] Old version uninstalled
- [x] Fresh APK installed
- [x] Log monitoring active (SpatialAnalyzer, VerbosityFormatter, RecognitionHistory)

---

## 🎯 Test Objectives

Validate Story 4.5 Acceptance Criteria:
1. Position calculation (left/center/right, top/bottom)
2. Distance estimation (close/medium/far based on box area)
3. Detailed mode announces spatial information
4. Brief/Standard modes do NOT announce spatial info
5. Multi-object spatial announcements
6. Spatial info saved to history database

---

## 📋 Test Procedure

### TEST 1: Basic Spatial Announcement (Detailed Mode)

**Setup:**
1. Launch VisionFocus app
2. Grant camera permission
3. Navigate to Settings → Verbosity Mode
4. Select "Detailed" mode
5. Return to Recognition screen

**Test Steps:**
1. Point camera at a **CHAIR** in **CENTER** of view (~1-2 meters away)
2. Tap Recognition FAB (floating button)
3. Listen to TTS announcement

**Expected Result:**
```
"High confidence: chair, close by, in center of view"
OR
"High confidence: chair, at medium distance, in center of view"
```

**What I'm Checking in Logs:**
- ✅ SpatialAnalyzer.analyze() called
- ✅ Position calculation: CENTER_CENTER
- ✅ Distance calculation: CLOSE or MEDIUM
- ✅ VerbosityFormatter includes spatial description
- ✅ RecognitionHistory saved with positionText + distanceText

**Pass Criteria:** TTS announces distance + position

---

### TEST 2: Position Variation - Left Side

**Test Steps:**
1. Move camera/object to **LEFT** side of view
2. Keep same distance (~1-2 meters)
3. Tap Recognition FAB

**Expected Result:**
```
"High confidence: chair, [distance], on the left"
OR
"High confidence: chair, [distance], on the left side, near the [top/bottom]"
```

**What I'm Checking:**
- ✅ Position: CENTER_LEFT, TOP_LEFT, or BOTTOM_LEFT
- ✅ Natural language: "on the left" (NOT "X: 150px")

---

### TEST 3: Position Variation - Right Side

**Test Steps:**
1. Move camera/object to **RIGHT** side of view
2. Tap Recognition FAB

**Expected Result:**
```
"High confidence: chair, [distance], on the right"
```

**What I'm Checking:**
- ✅ Position: CENTER_RIGHT, TOP_RIGHT, or BOTTOM_RIGHT

---

### TEST 4: Distance Variation - Close Object

**Test Steps:**
1. Move **VERY CLOSE** to object (within arm's reach, ~0.5m)
2. Object should fill >40% of screen
3. Tap Recognition FAB

**Expected Result:**
```
"High confidence: chair, close by, [position]"
```

**What I'm Checking:**
- ✅ Distance: CLOSE (box area > 40%)
- ✅ Phrase: "close by"

---

### TEST 5: Distance Variation - Far Object

**Test Steps:**
1. Move **FAR** from object (across room, >3 meters)
2. Object should be small (<20% of screen)
3. Tap Recognition FAB

**Expected Result:**
```
"High confidence: chair, far away, [position]"
```

**What I'm Checking:**
- ✅ Distance: FAR (box area < 20%)
- ✅ Phrase: "far away"

---

### TEST 6: Brief Mode - NO Spatial Info

**Setup:**
1. Navigate to Settings → Verbosity Mode
2. Select "Brief" mode
3. Return to Recognition screen

**Test Steps:**
1. Point at chair in center
2. Tap Recognition FAB

**Expected Result:**
```
"Chair"
```
(NO distance, NO position)

**What I'm Checking:**
- ✅ VerbosityFormatter skips spatial info in BRIEF mode
- ✅ Only object name announced

---

### TEST 7: Standard Mode - NO Spatial Info

**Setup:**
1. Navigate to Settings → Verbosity Mode
2. Select "Standard" mode
3. Return to Recognition screen

**Test Steps:**
1. Point at chair in center
2. Tap Recognition FAB

**Expected Result:**
```
"Chair with high confidence"
```
(NO distance, NO position)

**What I'm Checking:**
- ✅ VerbosityFormatter skips spatial info in STANDARD mode
- ✅ Only object name + confidence

---

### TEST 8: Multi-Object Spatial Announcements

**Setup:**
1. Set Verbosity Mode back to "Detailed"
2. Find a scene with 2-3 objects (e.g., chair + table)

**Test Steps:**
1. Point camera to capture **2-3 objects** simultaneously
2. Objects should be at different distances/positions
3. Tap Recognition FAB

**Expected Result:**
```
"I see a chair close by in the center, and a table at medium distance on the right"
```

**What I'm Checking:**
- ✅ Multiple objects formatted correctly
- ✅ Sorted by distance (CLOSE first)
- ✅ Natural sentence structure with "and"
- ✅ Each object has spatial description

---

### TEST 9: History Storage with Spatial Info

**Test Steps:**
1. Perform 3-5 recognitions with different positions
2. Navigate to Settings → Recognition History
3. Review history entries

**Expected Result:**
Each entry should show:
```
Chair - High confidence, close by, on the left - [timestamp]
Table - Medium confidence, at medium distance, in center of view - [timestamp]
```

**What I'm Checking:**
- ✅ Database contains positionText field
- ✅ Database contains distanceText field
- ✅ History UI displays spatial information
- ✅ No crashes or database errors

**Fallback Check:** If UI doesn't show spatial info, verify in database:
```
adb shell "run-as com.visionfocus sqlite3 databases/visionfocus_db 'SELECT category, positionText, distanceText FROM recognition_history LIMIT 5;'"
```

---

### TEST 10: Orientation Change (Portrait → Landscape)

**Test Steps:**
1. Point at chair in center (portrait mode)
2. Tap Recognition FAB - note announcement
3. Rotate phone to landscape
4. Point at SAME object in center
5. Tap Recognition FAB - note announcement

**Expected Result:**
- Portrait: "...in center of view"
- Landscape: "...in center of view" (still accurate)

**What I'm Checking:**
- ✅ SpatialAnalyzer handles aspect ratio changes
- ✅ Position calculation adapts to screen orientation
- ✅ No crashes or incorrect position announcements

---

## 📊 Log Verification Checklist

After each test, I'll verify in logs:

**SpatialAnalyzer Logs:**
```
D/SpatialAnalyzer: Analyzing bounding box: [coordinates]
D/SpatialAnalyzer: Position: CENTER_CENTER, Distance: CLOSE
```

**VerbosityFormatter Logs:**
```
D/VerbosityFormatter: Formatting with spatial info: position=..., distance=...
D/VerbosityFormatter: Generated announcement: "High confidence: chair, close by, in center of view"
```

**RecognitionHistory Logs:**
```
D/RecognitionViewModel: Story 4.2/4.5: Recognition saved to history with spatial info
D/RecognitionRepositoryImpl: Saved recognition with positionText="on the left", distanceText="close by"
```

---

## 🐛 Known Issues to Watch For

1. **Screen size 0x0:** If preview not measured, spatial analysis disabled (fallback to Story 4.1 logic)
2. **Invalid bounding boxes:** Negative dimensions default to FAR
3. **Null spatial info:** Brief/Standard modes will have null spatial fields in history
4. **Pre-existing test failures:** Instrumentation tests still blocked (not affecting manual test)

---

## 📝 Test Results Template

| Test | Status | Notes | Log Verified |
|------|--------|-------|--------------|
| 1. Basic Spatial (Detailed) | ⏳ | | ⏳ |
| 2. Position - Left | ⏳ | | ⏳ |
| 3. Position - Right | ⏳ | | ⏳ |
| 4. Distance - Close | ⏳ | | ⏳ |
| 5. Distance - Far | ⏳ | | ⏳ |
| 6. Brief Mode - No Spatial | ⏳ | | ⏳ |
| 7. Standard Mode - No Spatial | ⏳ | | ⏳ |
| 8. Multi-Object Spatial | ⏳ | | ⏳ |
| 9. History Storage | ⏳ | | ⏳ |
| 10. Orientation Change | ⏳ | | ⏳ |

Legend: ⏳ Pending | ✅ Pass | ❌ Fail | ⚠️ Partial

---

## 🚀 Ready to Start!

**I'm monitoring logs in real-time. When you're ready:**

1. **Unlock your phone**
2. **Launch VisionFocus app**
3. **Tell me when you see the Recognition screen**
4. I'll guide you through each test step-by-step and monitor logs simultaneously

**Let me know when you're ready to begin TEST 1!** 🎯
