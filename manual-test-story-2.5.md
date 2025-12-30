# Manual Test for Story 2.5 - High-Contrast Mode & Large Text Support
**Date:** December 30, 2025  
**Device:** SM-A127F (Android 13)  
**App Version:** debug build

---

## ✅ Test 1: Enable High-Contrast Mode

### Steps:
1. Launch VisionFocus app (should be on Recognition screen)
2. Tap overflow menu (3 dots in top-right corner)
3. Tap "Settings"
4. Find "High Contrast Mode" switch
5. Toggle it ON
6. App should restart automatically

### Expected Results:
- [ ] Background changes to pure black (#000000)
- [ ] Text changes to pure white (#FFFFFF)
- [ ] "High Contrast Mode" switch remains checked after restart
- [ ] No visual flicker or crashes

### Actual Results:
```
[Fill in after testing]
```

---

## ✅ Test 2: Enable Large Text Mode

### Steps:
1. In Settings screen (from Test 1)
2. Find "Large Text Mode" switch
3. Toggle it ON
4. App should restart automatically

### Expected Results:
- [ ] All text increases in size (noticeably larger)
- [ ] No text truncation or overlap
- [ ] "Large Text Mode" switch remains checked after restart
- [ ] Both switches (High Contrast + Large Text) remain ON

### Actual Results:
```
[Fill in after testing]
```

---

## ✅ Test 3: Theme Persistence (Critical AC8)

### Steps:
1. With both modes enabled (from Test 2)
2. Tap back button to return to Recognition screen
3. Force close the app (swipe up from Recent Apps)
4. Relaunch VisionFocus from app drawer
5. Go back to Settings

### Expected Results:
- [ ] App launches with black background immediately (not default theme first)
- [ ] Text is large and white on black
- [ ] Both switches in Settings remain checked
- [ ] Theme persisted correctly across app restart

### Actual Results:
```
[Fill in after testing]
```

---

## ✅ Test 4: Disable Themes

### Steps:
1. In Settings screen
2. Toggle "Large Text Mode" OFF
3. App restarts
4. Go back to Settings
5. Toggle "High Contrast Mode" OFF
6. App restarts

### Expected Results:
- [ ] After disabling Large Text: Background still black, text returns to normal size
- [ ] After disabling High Contrast: Background returns to dark theme (#121212), text normal size
- [ ] Both switches remain OFF after restart
- [ ] Default Material Design 3 theme applied

### Actual Results:
```
[Fill in after testing]
```

---

## ✅ Test 5: Ripple Effect Visibility (HIGH-7 Fix)

### Steps:
1. Enable High Contrast Mode again
2. In Settings, tap the High Contrast Mode switch multiple times
3. Observe the ripple effect when you tap

### Expected Results:
- [ ] White ripple effect visible on black background
- [ ] Ripple provides clear visual feedback
- [ ] Switch thumb visible (not invisible)

### Actual Results:
```
[Fill in after testing]
```

---

## 📊 Test Summary

| Test | Status | Notes |
|------|--------|-------|
| Test 1 (High Contrast) | ⏸️ PENDING | |
| Test 2 (Large Text) | ⏸️ PENDING | |
| Test 3 (Persistence) | ⏸️ PENDING | Critical AC8 |
| Test 4 (Disable) | ⏸️ PENDING | |
| Test 5 (Ripple) | ⏸️ PENDING | HIGH-7 fix |

---

## Issues Found

```
[List any issues discovered during testing]
```

---

## Final Verdict

Story 2.5 is ready for production: ✅ / ❌

**Reason:**
```
[Fill in after completing all tests]
```

---

## Screenshots (Optional)

1. Default theme (before): 
2. High-contrast mode:
3. Large text mode:
4. Combined mode:
5. Settings screen with both toggles ON:
