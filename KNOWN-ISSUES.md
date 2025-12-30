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
