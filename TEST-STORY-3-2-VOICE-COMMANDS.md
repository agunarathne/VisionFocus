# Story 3.2: Voice Command Testing Guide

**Date:** 2025-12-31  
**Status:** Ready for device testing  
**Prerequisites:** Device connected via ADB, microphone permission granted

---

## Setup Instructions

### 1. Connect Device & Install App

```powershell
# Connect to wireless device
.\launch-wireless.ps1

# OR connect via USB and run:
adb devices

# Install updated app with voice command fixes
.\gradlew.bat installDebug --quiet

# Clear logs and launch app
adb logcat -c
adb shell am start -n com.visionfocus/.MainActivity
```

### 2. Grant Microphone Permission

- First launch will request RECORD_AUDIO permission
- Tap "Allow" to enable voice commands
- App should announce "Microphone permission granted"

---

## Voice Command Test Plan

### ✅ FUNCTIONAL COMMANDS (11 commands - should work)

#### **1. Recognition Commands**
| Command | Expected Behavior | Test Status |
|---------|------------------|-------------|
| "Recognize" | ✅ TTS: "Recognize command received"<br>✅ Haptic feedback<br>⚠️ Camera doesn't start (Epic 2 pending) | [ ] |

#### **2. Navigation Commands**
| Command | Expected Behavior | Test Status |
|---------|------------------|-------------|
| "Navigate" | ✅ TTS: "Navigate command received"<br>✅ Haptic feedback<br>⚠️ Navigation doesn't execute (Epic 6 pending) | [ ] |
| "Back" | ✅ TTS: "Going back"<br>✅ Haptic feedback<br>⚠️ No actual navigation | [ ] |
| "Home" | ✅ TTS: "Going home"<br>✅ Haptic feedback<br>⚠️ No actual navigation | [ ] |
| "Where am I" | ✅ TTS: "Where am I"<br>✅ Haptic feedback<br>⚠️ No location data (Epic 6 pending) | [ ] |

#### **3. Utility Commands**
| Command | Expected Behavior | Test Status |
|---------|------------------|-------------|
| "Cancel" | ✅ TTS: "Canceling operation" → "Cancelled"<br>✅ Haptic feedback<br>✅ Broadcasts cancel intent | [ ] |
| "Help" | ✅ TTS: "Help menu opened"<br>✅ Haptic feedback<br>⚠️ Help screen not implemented | [ ] |

#### **4. Settings Commands**
| Command | Expected Behavior | Test Status |
|---------|------------------|-------------|
| "High contrast on" | ✅ TTS: "High contrast on"<br>✅ Haptic feedback<br>✅ Enables high contrast mode | [ ] |
| "High contrast off" | ✅ TTS: "High contrast off"<br>✅ Haptic feedback<br>✅ Disables high contrast mode | [ ] |
| "Increase speed" | ✅ TTS: "Increasing speech speed" → "Now at X times normal speed"<br>✅ Haptic feedback<br>✅ Increases TTS rate (0.7 → 1.0 → 1.3) | [ ] |
| "Decrease speed" | ✅ TTS: "Decreasing speech speed" → "Now at X times normal speed"<br>✅ Haptic feedback<br>✅ Decreases TTS rate (1.3 → 1.0 → 0.7) | [ ] |

---

### ❌ PLACEHOLDER COMMANDS (4 commands - will fail gracefully)

| Command | Expected Behavior | Test Status |
|---------|------------------|-------------|
| "Repeat" | ⚠️ TTS: "Repeat command received"<br>✅ Haptic feedback<br>❌ No repeat functionality (needs TTSManager.lastAnnouncement cache) | [ ] |
| "What do I see" | ⚠️ TTS: "What do I see"<br>✅ Haptic feedback<br>❌ No scene description (Epic 2 pending) | [ ] |
| "History" | ⚠️ TTS: "History command received"<br>✅ Haptic feedback<br>❌ No history screen (Story 4.3 pending) | [ ] |
| "Save location" | ⚠️ TTS: "Save location command received"<br>✅ Haptic feedback<br>❌ No location saving (Story 4.2 pending) | [ ] |

---

## Test Procedures

### Test 1: Basic Voice Recognition Flow

1. **Launch app** → Tap microphone FAB (bottom right)
2. **Observe:**
   - FAB pulses (scale + alpha animation)
   - TTS announces: "Listening"
   - Haptic feedback on FAB tap
3. **Speak command:** "Help"
4. **Verify:**
   - [ ] TTS announces: "Help menu opened"
   - [ ] Haptic vibration (100ms)
   - [ ] Log shows: `Command "Help" executed in <300ms`
5. **Repeat with other commands**

### Test 2: Fuzzy Matching

1. **Tap microphone FAB**
2. **Speak typo:** "Recog" (missing "nize")
3. **Verify:**
   - [ ] TTS announces: "Did you mean Recognize?"
   - [ ] TTS announces: "Recognize command received"
   - [ ] Haptic feedback
   - [ ] Command executes despite typo
4. **Try other fuzzy matches:**
   - "Nav" → should match "Navigate"
   - "Cancl" → should match "Cancel"
   - "Hlp" → should match "Help"

### Test 3: Settings Commands Integration

1. **Initial state:** Check current TTS speed
2. **Command:** "Increase speed"
3. **Verify:**
   - [ ] TTS rate increases (hear faster speech)
   - [ ] Announcement: "Now at 1.3 times normal speed"
4. **Command:** "High contrast on"
5. **Verify:**
   - [ ] High contrast mode enabled in settings
   - [ ] Visual theme changes (if UI implemented)

### Test 4: Error Handling

1. **Speak invalid command:** "Fly to the moon"
2. **Verify:**
   - [ ] TTS announces: "Command not recognized. Say 'Help' for available commands."
   - [ ] No haptic feedback (failed command)
   - [ ] App doesn't crash

### Test 5: Performance (AC: 3 - Execution within 300ms)

1. **Enable ADB logging:**
   ```powershell
   adb logcat -s VoiceCommandProcessor:D | Select-String "executed in"
   ```
2. **Execute 5 different commands**
3. **Verify:**
   - [ ] All execution times < 300ms
   - [ ] No performance warnings in logs

---

## Monitoring Commands

### Real-Time Log Monitoring

```powershell
# All voice command logs
adb logcat -s VoiceCommandProcessor:D VoiceRecognitionManager:D TTSManager:D HapticFeedbackManager:D

# Command execution times
adb logcat -s VoiceCommandProcessor:D | Select-String "executed in"

# Fuzzy match detection
adb logcat -s VoiceCommandProcessor:D | Select-String "Fuzzy match"

# TTS announcements
adb logcat -s TTSManager:D | Select-String "announce"

# Haptic feedback triggers
adb logcat -s HapticFeedbackManager:D | Select-String "trigger"
```

### Check for Errors

```powershell
# Crashes and exceptions
adb logcat -d | Select-String -Pattern "(FATAL|AndroidRuntime|Exception)" -Context 0,5 | Select-Object -Last 30

# Voice command errors
adb logcat -d -s VoiceCommandProcessor:E AndroidRuntime:E | Select-Object -Last 20
```

---

## Expected Log Output Examples

### Successful Command Execution
```
VoiceCommandProcessor: Processing transcription: "help"
VoiceCommandProcessor: Exact match found: Help
VoiceCommandProcessor: Executing command: Help
TTSManager: announce: Help menu opened
HapticFeedbackManager: trigger: CommandExecuted pattern
VoiceCommandProcessor: Command "Help" executed in 87ms (target: <300ms)
```

### Fuzzy Match
```
VoiceCommandProcessor: Processing transcription: "cancl"
VoiceCommandProcessor: No exact match for: cancl
VoiceCommandProcessor: Fuzzy match found: Cancel (distance: 1)
TTSManager: announce: Did you mean Cancel?
VoiceCommandProcessor: Executing command: Cancel
TTSManager: announce: Canceling operation
VoiceCommandProcessor: Command "Cancel" executed in 142ms
```

### Command Not Found
```
VoiceCommandProcessor: Processing transcription: "invalid command"
VoiceCommandProcessor: No exact match for: invalid command
VoiceCommandProcessor: No fuzzy match found (min distance: 8, threshold: 2)
VoiceCommandProcessor: No command matched for transcription: invalid command
TTSManager: announce: Command not recognized. Say 'Help' for available commands.
```

---

## Known Issues (From Code Review)

### ⚠️ Current Limitations

1. **Placeholder Commands (4):**
   - Execute but don't perform actual work
   - TTS confirmation works, but underlying logic pending

2. **Navigation Commands:**
   - Announce but don't navigate
   - Epic 6 (navigation) not yet implemented

3. **Recognition Commands:**
   - "Recognize" announces but doesn't start camera
   - Epic 2 (camera) pending

4. **Fuzzy Match Auto-Execution:**
   - No user confirmation before executing
   - "Did you mean X?" is informational, not a prompt

5. **Testing Deferred:**
   - No unit tests yet (Task 10)
   - No integration tests (Task 11)
   - No device testing with 150 samples (Task 12)

### ✅ Fixed Issues (2025-12-31)

1. ✅ Haptic feedback now works (coroutine scope fixed)
2. ✅ Duplicate TTS announcements removed
3. ✅ Build compiles with no errors

---

## Success Criteria

### Acceptance Criteria from Story 3.2

- **AC #1:** ✅ 15 voice commands registered (11 functional, 4 placeholders)
- **AC #2:** ✅ Case-insensitive exact matching + fuzzy matching (Levenshtein ≤2)
- **AC #3:** ⏳ Command execution within 300ms (test with logs)
- **AC #4:** ✅ TTS confirmation for all commands
- **AC #5:** ✅ Haptic feedback on command execution
- **AC #6:** ❌ ≥85% accuracy rate (deferred - requires 150 voice samples)
- **AC #7:** ❌ Indoor/quiet test (deferred - requires device testing)
- **AC #8:** ❌ Outdoor/noisy test (deferred - requires acoustic testing)

### Definition of Done

- [x] Code compiles without errors
- [ ] All 15 commands execute (11 work, 4 placeholders acceptable)
- [ ] Fuzzy matching works for typos
- [ ] TTS confirmation within 300ms
- [ ] Haptic feedback triggers correctly
- [ ] No crashes during voice recognition flow
- [ ] High contrast and speed settings persist

---

## Quick Start (When Device Connected)

```powershell
# 1. Connect device
.\launch-wireless.ps1

# 2. Install app
.\gradlew.bat installDebug --quiet

# 3. Start monitoring
adb logcat -c
adb logcat -s VoiceCommandProcessor:D VoiceRecognitionManager:D TTSManager:D

# 4. Test basic flow
# - Launch app
# - Tap microphone FAB
# - Say "Help"
# - Verify TTS + haptic + logs
```

---

## Report Issues

If you encounter bugs, note:
- Command spoken
- Expected behavior
- Actual behavior
- Log excerpt (copy from terminal)
- Device model and Android version

**Good Luck Testing! 🎤✅**
