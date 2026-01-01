# Known Issues - VisionFocus

## Story 2.5: Theme Toggle Limitation (Dec 30, 2024)

**Issue:** Theme toggles (High Contrast, Large Text) only work ONCE per app launch.

**Symptoms:**
- Launch app with settings OFF
- Toggle High Contrast ON → Works (app recreates, black theme applies)
- Toggle High Contrast OFF → Doesn't work (no theme change, no recreate)
- Must force-stop and restart app to toggle again

**Impact:** 
- Users can enable theme modes but cannot disable them without restarting
- Workaround: Force-stop app, relaunch, toggle works again once

**Technical Context:**
- Settings switches respond to user input (no crash)
- First toggle triggers: DataStore write → recreate() → theme applies correctly
- Second toggle appears blocked (no logs, no recreate)
- Possibly related to: Activity lifecycle after recreate(), observer state corruption, or listener de-registration

**Location:** 
- `app/src/main/java/com/visionfocus/ui/settings/SettingsFragment.kt` (lines 136-182)
- Listener setup in `setupListeners()` method

**Priority:** Medium - Feature works but with poor UX

**Next Steps:**
1. Add comprehensive logging to track listener/observer state after recreate()
2. Verify listeners are re-registered in new Fragment instance
3. Check if guard flag `isUpdatingFromObserver` gets corrupted
4. Consider alternative to Activity.recreate() (e.g., manual theme reapplication without recreate)

---

**Related Files:**
- SettingsFragment.kt
- SettingsViewModel.kt  
- MainActivity.kt (onCreate theme loading)
- ThemeManager.kt

---

## Feature Request: Optional Camera Preview for Low Vision Users (Jan 1, 2026)

**Story Idea:** "Optional Camera Preview for Low Vision Users"

**Description:**
- Add toggle in Settings: "Show camera preview" (default: OFF for blind users)
- When enabled: Show live camera feed before recognition
- Target users: Low vision users, testing, sighted helpers
- Priority: Medium (accessibility enhancement)

**Rationale:**
- Current app has no camera viewfinder (intentional for blind users to avoid clutter)
- Low vision users would benefit from visual feedback when framing objects
- Helpful for testing and verification during development
- Sighted helpers/family members could assist with positioning

**Implementation Considerations:**
- Toggle should default to OFF (maintain blind-first design)
- Preview could overlay on main screen or be optional separate view
- Consider CameraX PreviewView component
- Ensure preview doesn't interfere with TalkBack navigation
- May need separate layouts for preview-enabled vs preview-disabled modes

**User Request:** Noted during Story 4.1 testing - difficulty confirming object framing without visual feedback

**Next Steps:**
1. Create full story specification in Epic 10 (Future Enhancements)
2. Design mockups for preview layout (with/without preview modes)
3. Test with low vision users for usability
4. Consider performance impact on older devices

**Status:** Backlog - Future Enhancement
---

## Story 2.4: Intermittent Camera Buffer Errors on Samsung Devices (Jan 1, 2026)

**Issue:** CameraX buffer errors (errorCode 3/4/5) causing intermittent camera capture failures, especially on rapid recognition attempts.

**Symptoms:**
- User taps FAB button → Vibration feedback occurs
- Camera fails to capture frame → No recognition/announcement
- Logs show: `CameraDeviceClient: notifyError: errorCode=4` (ERROR_BUFFER) followed by `errorCode=5` (ERROR_REQUEST)
- Multiple dropped frames: `errorCode=3` for frames 122-128
- Occurs more frequently when recognition triggered in quick succession

**Impact:**
- **Severity:** HIGH - Blocks primary recognition functionality intermittently
- **Frequency:** ~30-50% of recognition attempts fail on Samsung SM-A127F (Android 13)
- **Workaround:** User must tap FAB multiple times until capture succeeds
- **Story 4.1 Impact:** Verbosity feature works correctly when camera succeeds, but testing blocked by camera crashes

**Technical Context:**
- Root Cause: CameraX unbindAll() releases camera resources asynchronously
- Current 300ms delay before rebinding is insufficient on Samsung devices
- Samsung Galaxy A12 (SM-A127F) requires 500-1000ms for full camera release
- Issue NOT present on Pixel devices (tested on emulators)
- Camera lifecycle management in RecognitionFragment.kt line 263-275

**Test Results (Jan 1, 2026):**
- First recognition after app launch: **SUCCESS** (wine glass detected)
- Second recognition (12 sec later): **FAIL** (camera crash, no detection)
- Third recognition attempt: **FAIL** (camera crash, vibration only)
- After force-stop/restart: **SUCCESS** again

**Evidence from Logs:**
```
15:17:53.684 E/CameraDeviceClient: notifyError: pid=7276, errorCode=4, frameNumber=122
15:17:53.685 E/CameraDeviceClient: notifyError: pid=7276, errorCode=5, frameNumber=122
15:17:53.686-693 E/CameraDeviceClient: notifyError: errorCode=3, frames 123-128 (6 dropped frames)
```

**Location:**
- `app/src/main/java/com/visionfocus/ui/recognition/RecognitionFragment.kt`
  - bindCameraUseCases() method (lines 236-275)
  - captureFrame() method (lines 278-318)
  - Camera unbind in onCaptureSuccess (line 311)

**Attempted Fixes:**
1. Added 100ms delay after unbindAll() → Still failing
2. Increased to 300ms delay → Still failing on Samsung device
3. Need 500-1000ms delay OR alternative approach

**Priority:** HIGH - Core functionality impacted

**Next Steps:**
1. **Recommended Fix:** Increase delay to 500ms (test on Samsung device)
2. **Alternative 1:** Don't unbind camera between captures (keep preview running)
3. **Alternative 2:** Use single ImageCapture instance, don't rebind lifecycle
4. **Alternative 3:** Implement retry logic with exponential backoff
5. Test fixes specifically on Samsung Galaxy A-series devices (known for strict camera timing)

**Related Stories:**
- Story 2.4: Camera Capture with Accessibility Focus Management (original implementation)
- Story 4.1: Verbosity Mode Selection (testing blocked by this issue)

**Status:** Open - Requires fix before production release