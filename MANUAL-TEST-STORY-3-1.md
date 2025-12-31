# Manual Testing Guide - Story 3.1: Voice Recognition

**Date:** December 31, 2025  
**Tester:** Allan  
**Build:** Code review fixes applied (commit d6eab30)  
**Device:** [Your test device]

---

## 🎯 Testing Objectives

1. Verify all 11 Acceptance Criteria are met
2. Complete Task 11: Acoustic environment testing
3. Validate code review fixes (memory leak, TTS race condition, debouncing)
4. Ensure TalkBack accessibility compliance
5. Document any issues for follow-up

---

## 📱 Pre-Test Setup

### 1. Install Updated App
```powershell
# App already installed with fixes
adb shell pm list packages | Select-String visionfocus
# Should show: package:com.visionfocus
```

### 2. Clear App Data (Fresh Start)
```powershell
adb shell pm clear com.visionfocus
```

### 3. Enable TalkBack (For Accessibility Tests)
```
Settings → Accessibility → TalkBack → Turn On
```

### 4. Prepare Test Environments
- **Quiet room:** Close windows, turn off fans/AC
- **Background noise:** Play music at moderate volume (~50%)
- **Outdoor:** Near street with traffic/wind
- **Offline:** Enable Airplane mode

---

## ✅ TEST SECTION 1: Permission Flow (AC 1, Task 2)

### Test 1.1: First Launch - No Auto-Request (HIGH-4 Fix)
**Expected:** App should NOT show microphone permission dialog on first launch

1. Launch VisionFocus
2. **VERIFY:** No permission dialog appears automatically ✅ / ❌
3. **VERIFY:** Voice button visible in top-right corner ✅ / ❌
4. **VERIFY:** Voice button appears disabled (alpha 0.5) ✅ / ❌

**Result:** _________________________________________

---

### Test 1.2: Voice Button - Just-In-Time Permission Request
**Expected:** Permission requested ONLY when user taps voice button

1. Tap voice button (top-right, microphone icon)
2. **VERIFY:** System permission dialog appears ✅ / ❌
3. **VERIFY:** Dialog shows "Allow" and "Deny" options ✅ / ❌
4. Tap "Allow"
5. **VERIFY:** Button becomes enabled (alpha 1.0) ✅ / ❌
6. **VERIFY:** TalkBack announces "Microphone permission granted..." ✅ / ❌

**Result:** _________________________________________

---

### Test 1.3: Permission Denial & Rationale
**Expected:** Rationale shown on second denial

1. Uninstall and reinstall app (or clear data)
2. Tap voice button → Deny permission
3. **VERIFY:** Button remains disabled ✅ / ❌
4. Tap voice button again
5. **VERIFY:** Rationale dialog appears with privacy message ✅ / ❌
6. Tap "Allow" in rationale → Grant permission
7. **VERIFY:** Button now enabled ✅ / ❌

**Result:** _________________________________________

---

## ✅ TEST SECTION 2: Basic Voice Recognition (AC 2-9)

### Test 2.1: Voice Button UI & Animation (AC 3, 6)
**Expected:** Button meets 56×56 dp size with pulsing animation

1. With permission granted, tap voice button
2. **VERIFY:** Pulsing animation starts (scale + alpha) ✅ / ❌
3. **VERIFY:** Animation is smooth (no lag) ✅ / ❌
4. **VERIFY:** contentDescription changes to "Listening for command..." ✅ / ❌
5. Wait 5 seconds (timeout)
6. **VERIFY:** Animation stops on timeout ✅ / ❌

**Result:** _________________________________________

---

### Test 2.2: TTS Announcement (AC 5, HIGH-2 Fix)
**Expected:** TTS announces "Listening for command" without self-recognition

1. Ensure device volume is audible
2. Tap voice button
3. **VERIFY:** Hear "Listening for command" announcement ✅ / ❌
4. **LISTEN CAREFULLY:** Microphone should NOT recognize TTS audio ✅ / ❌
5. Speak "recognize" after TTS stops
6. **VERIFY:** Command is recognized (check logcat) ✅ / ❌

**To check logs:**
```powershell
adb logcat -d | Select-String -Pattern "VoiceRecognitionManager" | Select-Object -Last 20
```

**Result:** _________________________________________

---

### Test 2.3: Speech Capture & Lowercase Conversion (AC 7, 9)
**Expected:** Speech captured and converted to lowercase

1. Clear logcat: `adb logcat -c`
2. Tap voice button
3. Wait for "Listening for command" to finish
4. Clearly say: **"RECOGNIZE"** (say it loudly/clearly)
5. Wait for recognition to complete

**Check logs:**
```powershell
adb logcat -d | Select-String -Pattern "transcription"
```

6. **VERIFY:** Log shows: `transcription = "recognize"` (lowercase) ✅ / ❌

**Result:** _________________________________________

---

### Test 2.4: Timeout Handling (AC 8, 10)
**Expected:** 5-second timeout with clear announcement

1. Tap voice button
2. Stay SILENT for full 5 seconds
3. **VERIFY:** Timeout occurs after ~5 seconds ✅ / ❌
4. **VERIFY:** TTS announces "Voice command timed out" ✅ / ❌
5. **VERIFY:** Pulsing animation stops ✅ / ❌
6. **VERIFY:** Button returns to normal state ✅ / ❌

**Result:** _________________________________________

---

### Test 2.5: Error Handling - No Match (AC 11)
**Expected:** Clear error message for unintelligible speech

1. Tap voice button
2. Make random noise (hum, whistle, or unclear mumbling)
3. Wait for recognition to complete
4. **VERIFY:** TTS announces "Didn't catch that. Please try again." ✅ / ❌
5. **VERIFY:** Button returns to idle state ✅ / ❌

**Result:** _________________________________________

---

## ✅ TEST SECTION 3: Code Review Fixes Validation

### Test 3.1: Button Debouncing (HIGH-8 Fix)
**Expected:** Rapid taps ignored (500ms debounce)

1. Tap voice button rapidly 5 times in quick succession
2. **VERIFY:** Only ONE listening session starts ✅ / ❌
3. **VERIFY:** No crashes or errors ✅ / ❌

**Check logs for "debounced":**
```powershell
adb logcat -d | Select-String -Pattern "debounced"
```

**Result:** _________________________________________

---

### Test 3.2: Memory Leak Fix (HIGH-1 Fix)
**Expected:** No multiple SpeechRecognizer instances created

1. Tap voice button → Cancel (tap again quickly)
2. Repeat 10 times rapidly
3. **VERIFY:** App remains responsive ✅ / ❌

**Check logs for "already active":**
```powershell
adb logcat -d | Select-String -Pattern "already active"
```

**Result:** _________________________________________

---

### Test 3.3: Watchdog Timer (HIGH-6 Fix)
**Expected:** State resets after 15 seconds if recognizer hangs

**Note:** This is hard to test without simulating recognizer failure.

1. Monitor app for 15 seconds after starting recognition
2. **VERIFY:** No indefinite animation if timeout occurs ✅ / ❌

**Result:** _________________________________________

---

### Test 3.4: Permission Revocation During Recognition (MEDIUM-2 Fix)
**Expected:** Graceful handling if permission revoked mid-session

1. Grant microphone permission
2. Tap voice button to start listening
3. **While listening,** open Settings → Apps → VisionFocus → Permissions
4. Revoke microphone permission
5. Return to app
6. **VERIFY:** Error announced with permission prompt ✅ / ❌

**Result:** _________________________________________

---

## ✅ TEST SECTION 4: Task 11 - Acoustic Environment Testing

### Test 4.1: Quiet Room (Baseline)
**Environment:** Close windows, turn off fans, minimal background noise

1. Tap voice button
2. Say: **"recognize"**
3. **VERIFY:** Recognized correctly ✅ / ❌

Repeat 5 times with different commands:
- "navigate"
- "settings"
- "history"
- "help"
- "cancel"

**Success Rate:** _____ / 5 (Target: ≥85% = 4-5 correct)

**Result:** _________________________________________

---

### Test 4.2: Background Noise - Music
**Environment:** Play music at 50% volume on another device

1. Start music playback
2. Tap voice button
3. Say: **"recognize"** (speak clearly, normal volume)
4. **VERIFY:** Recognized correctly ✅ / ❌

Repeat with 5 commands (as above)

**Success Rate:** _____ / 5 (Target: ≥85%)

**Notes:** _________________________________________

**Result:** _________________________________________

---

### Test 4.3: Background Noise - Conversation
**Environment:** Have someone talk nearby while you use voice button

1. Have assistant talk at conversational volume
2. Tap voice button
3. Say: **"recognize"** (speak clearly)
4. **VERIFY:** Recognized correctly despite conversation ✅ / ❌

Repeat with 5 commands

**Success Rate:** _____ / 5 (Target: ≥85%)

**Result:** _________________________________________

---

### Test 4.4: Outdoor Environment
**Environment:** Outside with street noise/wind

1. Go outside (street, parking lot, park)
2. Tap voice button
3. Say: **"recognize"** (speak louder than indoors)
4. **VERIFY:** Recognized correctly ✅ / ❌

Repeat with 5 commands

**Success Rate:** _____ / 5 (Target: ≥78-85%)

**Notes:** Wind and traffic can significantly degrade accuracy

**Result:** _________________________________________

---

### Test 4.5: Offline Mode (On-Device Recognition)
**Environment:** Airplane mode enabled

1. Enable Airplane mode in device settings
2. **VERIFY:** App still launches ✅ / ❌
3. Tap voice button
4. Say: **"recognize"**

**Expected Behavior (API 33+):** On-device recognition works  
**Expected Behavior (API 26-32):** Error "Network unavailable..."

**On API 33+:**
- **VERIFY:** Recognition works offline ✅ / ❌

**On API 26-32:**
- **VERIFY:** Error announced appropriately ✅ / ❌

**Device API Level:** ___________

**Result:** _________________________________________

---

### Test 4.6: Different Voices/Accents
**Environment:** Test with different people if available

1. Have 3 different people test:
   - Male voice
   - Female voice
   - Non-native English speaker (if available)

2. Each person says: **"recognize"**

**Results:**
- Person 1: ✅ / ❌
- Person 2: ✅ / ❌
- Person 3: ✅ / ❌

**Notes:** _________________________________________

**Result:** _________________________________________

---

## ✅ TEST SECTION 5: TalkBack Accessibility (AC 3, Task 8, 10)

### Test 5.1: Enable TalkBack and Navigate to Voice Button
**Expected:** Voice button discoverable with TalkBack gestures

1. Enable TalkBack (Settings → Accessibility → TalkBack)
2. Launch VisionFocus
3. Swipe right from top of screen to navigate elements
4. **VERIFY:** TalkBack announces "Voice commands, button" ✅ / ❌
5. **VERIFY:** Focus order: Toolbar → Recognition FAB → Voice FAB ✅ / ❌

**Result:** _________________________________________

---

### Test 5.2: Voice Button - TalkBack Double-Tap
**Expected:** Double-tap activates listening mode

1. With TalkBack on, navigate to voice button
2. Use TalkBack double-tap gesture to activate
3. **VERIFY:** Listening mode starts ✅ / ❌
4. **VERIFY:** TalkBack announces "Listening for command" ✅ / ❌
5. **VERIFY:** Haptic feedback on button press ✅ / ❌

**Result:** _________________________________________

---

### Test 5.3: Voice Button State Changes with TalkBack
**Expected:** contentDescription changes announced

1. Navigate to voice button with TalkBack
2. Note announcement: "Voice commands, button"
3. Double-tap to activate
4. **VERIFY:** Announcement changes to "Listening for command, button" ✅ / ❌
5. Wait for error/timeout
6. **VERIFY:** Error state announcement: "Voice command failed, try again, button" ✅ / ❌

**Result:** _________________________________________

---

### Test 5.4: Touch Target Size (AC 3, Task 10.6)
**Expected:** 56×56 dp minimum touch target

1. Use TalkBack explore-by-touch mode
2. Touch voice button area
3. **VERIFY:** Button easily activates (no missed taps) ✅ / ❌
4. Test edges of button (top, bottom, left, right)
5. **VERIFY:** All edges responsive ✅ / ❌

**Result:** _________________________________________

---

## ✅ TEST SECTION 6: Performance & Stability

### Test 6.1: Repeated Use (Memory Leak Check)
**Expected:** No performance degradation over time

1. Use voice button 20 times in a row:
   - Tap → Say command → Wait for result → Repeat
2. **VERIFY:** No noticeable slowdown after 20 uses ✅ / ❌
3. **VERIFY:** Animations remain smooth ✅ / ❌
4. **VERIFY:** App memory usage reasonable (check Settings → Apps) ✅ / ❌

**Result:** _________________________________________

---

### Test 6.2: App Lifecycle (Pause/Resume)
**Expected:** Voice recognition survives app lifecycle

1. Start listening mode (tap voice button)
2. Press Home button (app goes to background)
3. Return to app (tap VisionFocus in recent apps)
4. **VERIFY:** App returns to normal state (not stuck) ✅ / ❌
5. Tap voice button again
6. **VERIFY:** Voice recognition works normally ✅ / ❌

**Result:** _________________________________________

---

## 📊 TEST SUMMARY

### Acceptance Criteria Results

| AC | Description | Status | Notes |
|----|-------------|--------|-------|
| 1 | Microphone permission granted | ⬜ Pass ⬜ Fail | |
| 2 | SpeechRecognizer initializes | ⬜ Pass ⬜ Fail | |
| 3 | Microphone button 56×56 dp | ⬜ Pass ⬜ Fail | |
| 4 | Button activates listening | ⬜ Pass ⬜ Fail | |
| 5 | TTS announces listening | ⬜ Pass ⬜ Fail | |
| 6 | Pulsing visual indicator | ⬜ Pass ⬜ Fail | |
| 7 | Speech audio captured | ⬜ Pass ⬜ Fail | |
| 8 | 5-second timeout | ⬜ Pass ⬜ Fail | |
| 9 | Lowercase conversion | ⬜ Pass ⬜ Fail | |
| 10 | Timeout announced | ⬜ Pass ⬜ Fail | |
| 11 | Errors announced clearly | ⬜ Pass ⬜ Fail | |

---

### Task 11 Results

| Environment | Success Rate | Notes |
|-------------|--------------|-------|
| Quiet Room | _____ / 5 | Target: ≥85% |
| Music Noise | _____ / 5 | Target: ≥85% |
| Conversation | _____ / 5 | Target: ≥85% |
| Outdoor | _____ / 5 | Target: ≥78% |
| Offline | ⬜ Pass ⬜ Fail ⬜ N/A | API level: ____ |
| Different Voices | _____ / 3 | |

**Overall Accuracy:** ______% (Target: ≥85%)

---

### Code Review Fixes Validation

| Fix | Status | Notes |
|-----|--------|-------|
| HIGH-1: Memory leak | ⬜ Pass ⬜ Fail | |
| HIGH-2: TTS race condition | ⬜ Pass ⬜ Fail | |
| HIGH-4: No auto-request | ⬜ Pass ⬜ Fail | |
| HIGH-5: Animation on error | ⬜ Pass ⬜ Fail | |
| HIGH-6: Watchdog timer | ⬜ Pass ⬜ Fail | |
| HIGH-8: Button debouncing | ⬜ Pass ⬜ Fail | |
| MEDIUM-2: Permission handling | ⬜ Pass ⬜ Fail | |
| MEDIUM-4: Error descriptions | ⬜ Pass ⬜ Fail | |

---

### Issues Found

**Critical Issues:**
1. _______________________________________________________
2. _______________________________________________________

**Minor Issues:**
1. _______________________________________________________
2. _______________________________________________________

**Observations:**
_______________________________________________________________
_______________________________________________________________
_______________________________________________________________

---

### Recommendation

⬜ **PASS** - Story 3.1 ready to mark "done"  
⬜ **FAIL** - Issues must be addressed before "done"

**Next Steps:**
1. _______________________________________________________
2. _______________________________________________________
3. _______________________________________________________

---

**Tested By:** ___________________  
**Date:** ___________________  
**Signature:** ___________________
