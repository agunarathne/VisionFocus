# Quick Reference - Story 3.1 Voice Testing

## 🚀 5-Minute Smoke Test

**Before full testing, run this quick validation:**

### 1. Permission Flow (30 sec)
- Launch app → No auto permission request ✅
- Tap voice button → Permission dialog shows ✅
- Grant permission → Button enabled ✅

### 2. Basic Voice (1 min)
- Tap voice button
- Hear "Listening for command" ✅
- Say "recognize" clearly
- Check log for lowercase: `adb logcat -d | Select-String "transcription"`

### 3. Timeout (5 sec)
- Tap voice button
- Stay silent 5 seconds
- Hear "Voice command timed out" ✅

### 4. Error Handling (10 sec)
- Tap voice button
- Make random noise
- Hear "Didn't catch that" ✅

### 5. TalkBack (1 min)
- Enable TalkBack
- Navigate to voice button
- Hear "Voice commands, button" ✅
- Double-tap → Listening starts ✅

**If ALL 5 pass:** Proceed to full testing  
**If ANY fail:** Check logs and investigate

---

## 🎯 Critical Tests (Must Pass)

### Task 11.1: Quiet Room Baseline
```
Goal: ≥85% accuracy (4/5 correct)
Commands: recognize, navigate, settings, history, help
```

### HIGH-2: TTS Race Condition
```
Listen carefully: TTS should NOT recognize itself
Symptom if broken: "listening" appears in transcription
```

### HIGH-8: Button Debouncing
```
Tap button 5x rapidly
Expected: Only 1 session starts
Check log: "debounced" messages
```

---

## 📊 Useful Commands

### Clear App Data
```powershell
adb shell pm clear com.visionfocus
```

### Monitor Voice Logs
```powershell
adb logcat -c  # Clear first
adb logcat -s VoiceRecognitionManager:D VoiceRecognitionVM:D
```

### Check Last Recognition
```powershell
adb logcat -d | Select-String -Pattern "transcription" | Select-Object -Last 5
```

### Check Debouncing
```powershell
adb logcat -d | Select-String -Pattern "debounced"
```

### Check Watchdog
```powershell
adb logcat -d | Select-String -Pattern "watchdog"
```

### Check Permission State
```powershell
adb shell dumpsys package com.visionfocus | Select-String -Pattern "RECORD_AUDIO"
```

---

## 🔍 Common Issues & Solutions

### Issue: No TTS Announcement
**Solution:** Check device volume, ensure TTS engine installed

### Issue: Voice Not Recognized
**Solution:** 
- Speak louder/clearer
- Reduce background noise
- Check microphone not covered
- Try offline/online mode

### Issue: Button Stays Disabled
**Solution:**
- Check permission granted: Settings → Apps → VisionFocus
- Clear app data and retry

### Issue: Animation Stuck
**Solution:**
- Wait 15 seconds (watchdog will reset)
- If still stuck, check logs for errors

### Issue: TTS Self-Recognition
**Solution:**
- This was HIGH-2 fix (250ms delay)
- If still happens, check logs: transcription should NOT show "listening"

---

## 📝 Quick Logcat Filters

### All Voice Activity
```powershell
adb logcat | Select-String -Pattern "Voice"
```

### Errors Only
```powershell
adb logcat -s AndroidRuntime:E
```

### Permission Changes
```powershell
adb logcat | Select-String -Pattern "permission"
```

### State Changes
```powershell
adb logcat | Select-String -Pattern "State changed"
```

---

## ✅ Pass/Fail Criteria

**PASS Story 3.1 if:**
- All 11 ACs validated ✅
- Task 11 acoustic tests ≥85% accuracy ✅
- No critical bugs found ✅
- TalkBack fully functional ✅
- Code review fixes verified ✅

**FAIL Story 3.1 if:**
- Any AC fails
- Task 11 accuracy <85%
- Memory leaks detected
- TTS self-recognition occurs
- TalkBack navigation broken

---

## 🎬 Test Session Template

```
Session: __________
Tester: Allan
Device: __________
Environment: Quiet/Noisy/Outdoor
Duration: ____ minutes

Tests Run: ___ / 11 ACs
Results: ___ Pass / ___ Fail
Task 11 Accuracy: ____%

Critical Issues: _______________
Minor Issues: _______________
Ready for Done: Yes / No
```
