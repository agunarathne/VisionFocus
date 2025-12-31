# Story 2.6 Haptic Feedback Testing Guide
**Date:** December 30, 2025  
**Device:** 192.168.8.103:5555  
**Build:** Debug with code review fixes applied

## Prerequisites
- ✅ Device connected wirelessly
- ✅ App installed with latest haptic fixes
- ✅ BUILD SUCCESSFUL verified
- 🔋 Device should have >50% battery (haptic testing drains power)

---

## Test Suite 1: Haptic Settings UI (5-10 minutes)

### Test 1.1: Access Settings
**Steps:**
1. Launch VisionFocus app
2. Tap the Settings icon (top-right corner)
3. Scroll to "Haptic Feedback" section

**Expected:**
- ✅ Section titled "Haptic Feedback"
- ✅ Label "Vibration Intensity"
- ✅ RadioGroup with 4 options: Off, Light, Medium (Default), Strong
- ✅ Medium should be pre-selected (default)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 1.2: Sample Vibration - OFF
**Steps:**
1. In Haptic Feedback section, tap "Off" radio button
2. Wait 1 second

**Expected:**
- ✅ No vibration occurs
- ✅ Radio button selects "Off"
- ❌ NO tactile feedback (this is correct behavior)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 1.3: Sample Vibration - LIGHT (50% amplitude)
**Steps:**
1. Tap "Light" radio button
2. Feel for vibration

**Expected:**
- ✅ Brief vibration (~100ms)
- ✅ Gentle/subtle intensity
- ✅ Should feel noticeably weaker than previous default (if you tested Medium first)

**Result:** ⬜ PASS / ⬜ FAIL  
**Intensity Perception:** ⬜ Very Weak / ⬜ Weak / ⬜ Moderate / ⬜ Strong  
**Notes:** _______________________

---

### Test 1.4: Sample Vibration - MEDIUM (75% amplitude)
**Steps:**
1. Tap "Medium (Default)" radio button
2. Feel for vibration

**Expected:**
- ✅ Brief vibration (~100ms)
- ✅ Moderate intensity
- ✅ Should feel stronger than LIGHT

**Perception Test:** Can you distinguish LIGHT vs MEDIUM by touch alone?  
**Result:** ⬜ YES (patterns feel different) / ⬜ NO (too similar) / ⬜ UNSURE  
**Notes:** _______________________

---

### Test 1.5: Sample Vibration - STRONG (100% amplitude)
**Steps:**
1. Tap "Strong" radio button
2. Feel for vibration

**Expected:**
- ✅ Brief vibration (~100ms)
- ✅ Maximum intensity
- ✅ Should feel noticeably stronger than MEDIUM

**Perception Test:** Can you distinguish MEDIUM vs STRONG by touch alone?  
**Result:** ⬜ YES (patterns feel different) / ⬜ NO (too similar) / ⬜ UNSURE  
**Notes:** _______________________

---

### Test 1.6: Settings Persistence
**Steps:**
1. Set haptic intensity to "Light"
2. Press Home button (exit app)
3. Force-stop VisionFocus (Settings → Apps → VisionFocus → Force Stop)
4. Relaunch VisionFocus
5. Navigate to Settings → Haptic Feedback

**Expected:**
- ✅ "Light" radio button is still selected
- ✅ Setting persisted across app restart

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

## Test Suite 2: Recognition Flow Haptic Feedback (10-15 minutes)

**Setup:** Set haptic intensity to MEDIUM for these tests.

---

### Test 2.1: Recognition Start Haptic (Single 100ms)
**Steps:**
1. Navigate to Home screen (recognition screen)
2. Point camera at an object (e.g., cup, phone, keyboard)
3. Tap the large blue FAB (Floating Action Button)
4. **Focus on the vibration immediately after tap**

**Expected:**
- ✅ Single short vibration (~100ms)
- ✅ Occurs immediately when FAB is tapped
- ✅ Vibration feels like "brief tap" (acknowledging your action)

**Timing:** ⬜ Immediate (<100ms delay) / ⬜ Slightly delayed (100-300ms) / ⬜ Very delayed (>300ms)  
**Pattern:** ⬜ Single vibration / ⬜ Multiple vibrations (FAIL)  
**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 2.2: Recognition Success Haptic (Double Pattern: 100ms, 50ms gap, 100ms)
**Steps:**
1. Point camera at a **well-lit, clear object** (e.g., coffee mug, water bottle)
2. Tap FAB to recognize
3. Wait for TTS announcement (e.g., "cup")
4. **Focus on the vibration pattern when results are announced**

**Expected:**
- ✅ Double vibration pattern: "tap-tap" rhythm
- ✅ Two distinct vibrations with brief gap between them
- ✅ Feels like celebration/confirmation (positive feedback)
- ✅ Timing: First vibration (100ms), pause (50ms), second vibration (100ms)

**Pattern Perception:**  
- ⬜ Clear "tap-tap" rhythm (PASS)
- ⬜ Two vibrations but gap not noticeable (PARTIAL)
- ⬜ Felt like single long vibration (FAIL)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 2.3: Recognition Error Haptic (Long 300ms)
**Steps:**
1. Point camera at a **blank wall or solid color surface** (no objects)
2. Tap FAB to recognize
3. Wait for error message (e.g., "No objects detected")
4. **Focus on the vibration when error is announced**

**Expected:**
- ✅ Single long vibration (~300ms)
- ✅ Noticeably longer than recognition start (100ms)
- ✅ Feels urgent/alerting (negative feedback)
- ✅ Should be about 3x longer than the start pattern

**Pattern Perception:**  
- ⬜ Significantly longer than start vibration (PASS)
- ⬜ Somewhat longer but not 3x (PARTIAL)
- ⬜ Same duration as start (FAIL)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 2.4: Pattern Distinctness Test (Critical for Deaf-Blind Users)

**Scenario:** Close your eyes and perform 5 random recognitions.  
**Goal:** Identify pattern by haptic feedback alone.

**Trials:**

**Trial 1:**
- Action: ⬜ Point at object / ⬜ Point at blank wall
- Predicted outcome (by haptic alone): ⬜ Success / ⬜ Error
- Actual outcome: ⬜ Success / ⬜ Error
- ⬜ CORRECT / ⬜ INCORRECT

**Trial 2:**
- Action: ⬜ Point at object / ⬜ Point at blank wall
- Predicted outcome (by haptic alone): ⬜ Success / ⬜ Error
- Actual outcome: ⬜ Success / ⬜ Error
- ⬜ CORRECT / ⬜ INCORRECT

**Trial 3:**
- Action: ⬜ Point at object / ⬜ Point at blank wall
- Predicted outcome (by haptic alone): ⬜ Success / ⬜ Error
- Actual outcome: ⬜ Success / ⬜ Error
- ⬜ CORRECT / ⬜ INCORRECT

**Trial 4:**
- Action: ⬜ Point at object / ⬜ Point at blank wall
- Predicted outcome (by haptic alone): ⬜ Success / ⬜ Error
- Actual outcome: ⬜ Success / ⬜ Error
- ⬜ CORRECT / ⬜ INCORRECT

**Trial 5:**
- Action: ⬜ Point at object / ⬜ Point at blank wall
- Predicted outcome (by haptic alone): ⬜ Success / ⬜ Error
- Actual outcome: ⬜ Success / ⬜ Error
- ⬜ CORRECT / ⬜ INCORRECT

**Accuracy:** ___/5 correct predictions  
**Verdict:** ⬜ Patterns are tactilely distinct (≥4/5) / ⬜ Patterns too similar (<4/5)  
**Notes:** _______________________

---

### Test 2.5: Haptic Feedback with OFF Setting
**Steps:**
1. Navigate to Settings → Haptic Feedback
2. Select "Off" radio button
3. Return to Home screen
4. Perform 3 recognitions (success and error scenarios)

**Expected:**
- ❌ NO vibrations occur during any recognition
- ✅ TTS announcements still work
- ✅ UI state changes still work
- ✅ App functions normally (haptic is optional)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

## Test Suite 3: TalkBack Accessibility (Optional, 5-10 minutes)

**Prerequisites:** Enable TalkBack in device settings (Settings → Accessibility → TalkBack → ON)

---

### Test 3.1: TalkBack Navigation Through Haptic Settings
**Steps:**
1. Launch VisionFocus with TalkBack enabled
2. Navigate to Settings screen
3. Swipe right until you reach "Haptic Feedback" section
4. Continue swiping right through radio buttons

**Expected TalkBack Announcements:**
- ✅ "Haptic feedback intensity settings. Adjust vibration strength for recognition events"
- ✅ "Haptic intensity options. Select vibration strength"
- ✅ "Haptic feedback off, radio button"
- ✅ "Light haptic feedback, gentle vibrations, radio button"
- ✅ "Medium haptic feedback, moderate vibrations, radio button, selected" (default)
- ✅ "Strong haptic feedback, powerful vibrations, radio button"

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 3.2: Sample Vibration with TalkBack
**Steps:**
1. With TalkBack enabled, navigate to haptic settings
2. Double-tap "Light" radio button
3. Feel for vibration

**Expected:**
- ✅ TalkBack announces: "Sample vibration at Light intensity" (or similar)
- ✅ Vibration occurs after TalkBack announcement
- ✅ Sample vibration works correctly with TalkBack

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

## Test Suite 4: Edge Cases & Error Handling (5 minutes)

---

### Test 4.1: Rapid Intensity Changes
**Steps:**
1. Navigate to Haptic Settings
2. Rapidly tap radio buttons: Off → Light → Medium → Strong → Off (within 2 seconds)
3. Wait 2 seconds
4. Check which option is selected
5. Exit and restart app
6. Check Settings again

**Expected:**
- ✅ Final selection matches what's displayed
- ✅ Setting persists correctly (no desync from race condition)

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

### Test 4.2: Recognition During App Interruption
**Steps:**
1. Set haptic intensity to MEDIUM
2. Tap FAB to start recognition
3. Immediately press Home button (before recognition completes)
4. Wait 5 seconds
5. Reopen VisionFocus

**Expected:**
- ✅ App returns to Idle state (no crash)
- ✅ Haptic feedback still works on next recognition

**Result:** ⬜ PASS / ⬜ FAIL  
**Notes:** _______________________

---

## Test Results Summary

### Acceptance Criteria Coverage

| AC | Description | Status | Notes |
|----|-------------|--------|-------|
| AC1 | Recognition start triggers 100ms vibration | ⬜ PASS / ⬜ FAIL | |
| AC2 | Recognition success triggers double pattern | ⬜ PASS / ⬜ FAIL | |
| AC3 | Recognition error triggers 300ms vibration | ⬜ PASS / ⬜ FAIL | |
| AC4 | Patterns are tactilely distinct | ⬜ PASS / ⬜ FAIL | Accuracy: ___/5 |
| AC5 | Intensity respects user preference | ⬜ PASS / ⬜ FAIL | |
| AC6 | Uses Vibrator API with amplitude control | ⬜ PASS / ⬜ FAIL | |
| AC7 | Works on devices without advanced motors | ⬜ N/A (requires legacy device) | |
| AC8 | OFF setting disables all haptics | ⬜ PASS / ⬜ FAIL | |

---

## Critical Issues Discovered

**Issue #1:** ___________________________________________  
**Severity:** ⬜ CRITICAL / ⬜ HIGH / ⬜ MEDIUM / ⬜ LOW  
**Reproduction Steps:** ___________________________________

**Issue #2:** ___________________________________________  
**Severity:** ⬜ CRITICAL / ⬜ HIGH / ⬜ MEDIUM / ⬜ LOW  
**Reproduction Steps:** ___________________________________

**Issue #3:** ___________________________________________  
**Severity:** ⬜ CRITICAL / ⬜ HIGH / ⬜ MEDIUM / ⬜ LOW  
**Reproduction Steps:** ___________________________________

---

## Final Verdict

**Overall Story 2.6 Status:**  
⬜ **READY FOR PRODUCTION** - All tests passed, patterns are distinct  
⬜ **NEEDS MINOR FIXES** - Most tests passed, minor issues found  
⬜ **NEEDS REWORK** - Critical issues found, patterns not distinct  

**Tester Signature:** _________________  
**Date:** December 30, 2025  
**Testing Duration:** _____ minutes

---

## Logcat Monitoring (Advanced)

Run this command in a separate terminal to monitor haptic-related logs:

```powershell
adb logcat -s HapticFeedbackManager:D RecognitionViewModel:D SettingsFragment:D | Select-String "Haptic"
```

Look for:
- "Haptic trigger ignored: Device has no vibrator"
- "Haptic trigger ignored: Intensity set to OFF"
- "Haptic trigger failed: ..." (indicates error, should not appear)
- "Vibrator initialized: hasVibrator=true"

---

## Quick Test Commands

**Launch app:**
```powershell
adb shell am start -n com.visionfocus/.MainActivity
```

**Clear app data (reset settings):**
```powershell
adb shell pm clear com.visionfocus
```

**Check device vibrator capability:**
```powershell
adb shell service call vibrator 3
```

**Force-stop app:**
```powershell
adb shell am force-stop com.visionfocus
```
