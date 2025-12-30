# VisionFocus Accessibility Guidelines

**Version:** 1.0  
**Date:** December 30, 2025  
**Author:** Story 2.7 Implementation  
**Applies To:** All future VisionFocus development (Epics 3-9)

---

## Purpose

This document establishes **accessibility standards** for all VisionFocus development. These guidelines ensure consistent TalkBack experience across all features and maintain WCAG 2.1 AA compliance.

> **Critical Principle:** VisionFocus is built **accessibility-first**, not retrofitted. TalkBack is the primary interface for our blind/low vision users, not an edge case.

---

## 1. TalkBack-First Design Principles

### Core Philosophy

- **User Base:** Primary users are blind or low vision; TalkBack is not an afterthought—it's the primary interface
- **WCAG 2.1 AA Compliance:** Mandatory, not optional; every UI element must meet accessibility standards
- **Research Validation:** 15 visually impaired participants validated TalkBack operability (91.3% task success, SUS 78.5)
- **Legal/Ethical:** Assistive technology apps have higher ethical duty to be accessible themselves
- **Testing Strategy:** Manual TalkBack testing required (not just automated scanner) for every story

### Design Workflow

1. **Design for TalkBack first** → Then add visual enhancements
2. **Test with eyes closed** → Validate user flow using only TalkBack audio + gestures
3. **Zero Accessibility Scanner errors** → Automated validation for every feature
4. **Code review accessibility checklist** → Mandatory for all PRs

---

## 2. Content Description Best Practices

### Guidelines

#### Be Descriptive But Concise (10-20 Words)
- ✅ Good: "Recognize objects. Double-tap to activate camera and identify objects in your environment."
- ❌ Bad: "Button" (not descriptive)
- ❌ Bad: "This button, when tapped by the user, will activate the camera and recognition system..." (too verbose)

#### Explain Action, Not Visual Appearance
- ✅ Good: "Recognize objects" (describes what it does)
- ❌ Bad: "Blue camera icon button" (describes visual appearance)

#### No Redundant Role Suffixes
- ✅ Good: "Recognize objects"
- ❌ Bad: "Recognize objects button" (TalkBack announces role automatically)

#### Include State Information If Applicable
- ✅ Good: "Recognition in progress, disabled"
- ✅ Good: "Recognition ready, double-tap to activate"

#### Use Action Verbs
- ✅ Good: "Activate", "Open", "Close", "Navigate to"
- ❌ Bad: "Button to activate", "Link for opening"

### Examples from Story 2.7

```xml
<!-- RecognitionFragment FAB (56x56 dp) -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/recognizeFab"
    android:layout_width="56dp"
    android:layout_height="56dp"
    android:contentDescription="@string/recognize_fab_description"
    android:importantForAccessibility="yes"
    android:screenReaderFocusable="true"
    app:fabSize="normal" />

<!-- strings.xml -->
<string name="recognize_fab_description">Recognize objects. Double-tap to activate camera and identify objects in your environment.</string>
```

**Analysis:**
- Length: 17 words ✅
- Explains action: "Recognize objects" ✅
- Explains interaction: "Double-tap to activate" ✅
- Explains outcome: "identify objects in environment" ✅
- No redundant "button" suffix ✅

---

## 3. Focus Order Standards

### Principles

#### Follow Visual Layout (Top-to-Bottom, Left-to-Right)
- Default XML layout order usually correct for ConstraintLayout
- Test with manual TalkBack swipe right/left gestures

#### Use `accessibilityTraversalBefore/After` Sparingly
- Only when visual layout differs from logical order
- Example: Reading order differs from visual stacking

```xml
<!-- Only if needed for non-standard layouts -->
<TextView
    android:id="@+id/instructionsTextView"
    android:accessibilityTraversalBefore="@id/recognizeFab" />
```

#### Restore Focus After Interruptions

**Critical: Story 2.7 Pattern**

```kotlin
// Save focus in onPause()
private var lastFocusedViewId: Int? = null
private var accessibilityManager: AccessibilityManager? = null

override fun onPause() {
    super.onPause()
    if (isAccessibilityServiceEnabled()) {
        lastFocusedViewId = view?.findFocus()?.id
    }
}

// Restore focus in onResume()
override fun onResume() {
    super.onResume()
    if (isAccessibilityServiceEnabled() && lastFocusedViewId != null) {
        val viewToFocus = view?.findViewById<View>(lastFocusedViewId!!)
            ?: binding.primaryActionButton  // Default fallback
        
        viewToFocus.post {
            viewToFocus.requestFocus()
            viewToFocus.sendAccessibilityEvent(
                AccessibilityEvent.TYPE_VIEW_FOCUSED
            )
        }
    }
}

private fun isAccessibilityServiceEnabled(): Boolean {
    return accessibilityManager?.isEnabled == true && 
           accessibilityManager?.isTouchExplorationEnabled == true
}
```

**Handles:**
- Phone call interruptions ✅
- Notification expansion ✅
- Split-screen mode changes ✅
- Configuration changes ✅

---

## 4. `importantForAccessibility` Usage

### When to Use Each Value

| Value | Use Case | Example |
|-------|----------|---------|
| `yes` | Interactive elements, meaningful content | Buttons, text fields, images with information |
| `no` | Decorative elements, redundant labels | Decorative icons, repeated app title |
| `no-hide-descendants` | Containers with custom accessibility | Custom views with AccessibilityDelegate |

### Examples

```xml
<!-- Interactive FAB: yes -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/recognizeFab"
    android:importantForAccessibility="yes"
    android:screenReaderFocusable="true" />

<!-- Decorative app title (visible in status bar): no -->
<TextView
    android:id="@+id/titleTextView"
    android:text="@string/app_name"
    android:importantForAccessibility="no" />

<!-- Invisible camera preview (blind users don't need): no -->
<androidx.camera.view.PreviewView
    android:id="@+id/previewView"
    android:visibility="invisible"
    android:importantForAccessibility="no" />

<!-- Instructions redundant with FAB description: no -->
<TextView
    android:id="@+id/instructionsTextView"
    android:text="Tap button to recognize objects"
    android:importantForAccessibility="no" />
```

### Decision Criteria

**Question 1:** Is this element interactive?
- **Yes** → `importantForAccessibility="yes"`
- **No** → Continue to Q2

**Question 2:** Does this element provide unique information?
- **Yes** → `importantForAccessibility="yes"`
- **No** → Continue to Q3

**Question 3:** Is this element redundant with another accessible element?
- **Yes** → `importantForAccessibility="no"`
- **No** → `importantForAccessibility="yes"`

---

## 5. Announcement Patterns

### Two Announcement Methods

#### Method 1: `announceForAccessibility()` (Non-Critical Updates)

**Use For:**
- State changes (Loading, Processing)
- Completion messages (Recognition complete)
- Non-blocking information

```kotlin
private fun announceForAccessibility(message: String) {
    if (!isAdded || _binding == null) {
        return  // Fragment destroyed
    }
    
    // Set live region polite (non-interrupting)
    binding.primaryView.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
    
    binding.primaryView.post {
        binding.primaryView.announceForAccessibility(message)
    }
}
```

#### Method 2: `sendAccessibilityEvent(TYPE_ANNOUNCEMENT)` (Critical Updates)

**Use For:**
- Critical errors (Camera failed, Permission denied)
- Safety warnings (GPS lost, Low battery)
- Urgent state changes

```kotlin
private fun announceCritical(message: String) {
    val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    event.text.add(message)
    event.contentDescription = message
    
    binding.root.sendAccessibilityEvent(event)
}
```

### Announcement Timing Guidelines

#### Avoid Overlapping Announcements

**Bad Practice:**
```kotlin
announceForAccessibility("Starting recognition")
delay(100)  // Too short - announcements overlap
announceForAccessibility("Camera ready")
```

**Good Practice:**
```kotlin
// State machine naturally spaces announcements
when (state) {
    Capturing -> announceForAccessibility("Starting recognition")
    // 1 second stabilization delay
    Recognizing -> announceForAccessibility("Analyzing image")
    // 320ms inference + TTS duration
    Success -> // TTS announces result
}
```

#### Test Announcement Priority (Epic 8 Preview)

Future stories (Epic 8: Audio Priority Queue) will implement:
- **Navigation > Recognition > General**
- Interruption logic for high-priority announcements

---

## 6. Testing Checklist for All Stories

### Automated Tests (Required)

- [ ] **Accessibility Scanner:** Zero errors in all screens
- [ ] **Content Description Validation:** All interactive elements have descriptive text
- [ ] **Touch Target Validation:** All targets ≥48×48 dp (enforced by scanner)
- [ ] **Focus Order Test:** Logical sequence validated programmatically

### Manual Tests (Required)

- [ ] **Eyes-Closed Navigation:** Complete user flow using only TalkBack
- [ ] **Swipe Right/Left:** Focus order follows logical sequence
- [ ] **Double-Tap Activation:** All actions work correctly
- [ ] **Interruption Recovery:** Focus restored after phone call
- [ ] **Error Scenarios:** Accessible recovery guidance provided
- [ ] **State Announcements:** Clear, non-repetitive, actionable

### Test Template

```kotlin
// Automated Accessibility Test
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FeatureAccessibilityTest {
    
    @Before
    fun setup() {
        AccessibilityChecks.enable()  // Story 2.7 requirement
    }
    
    @Test
    fun featureScreen_passesAccessibilityScanner() {
        // Test launches screen and validates zero errors
        onView(withId(R.id.primaryAction))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}
```

---

## 7. Common Accessibility Pitfalls

### ❌ Pitfall 1: Missing Content Descriptions

```xml
<!-- BAD: No contentDescription -->
<ImageButton
    android:id="@+id/shareButton"
    android:src="@drawable/ic_share" />

<!-- GOOD: Descriptive contentDescription -->
<ImageButton
    android:id="@+id/shareButton"
    android:src="@drawable/ic_share"
    android:contentDescription="Share recognition result" />
```

### ❌ Pitfall 2: Redundant Announcements

```kotlin
// BAD: Redundant announcements
announceForAccessibility("Starting recognition")
tts.speak("Starting recognition")  // Overlaps with TalkBack

// GOOD: Single announcement
announceForAccessibility("Starting recognition")
// TTS announces only recognition result
```

### ❌ Pitfall 3: Small Touch Targets

```xml
<!-- BAD: 32x32 dp icon (too small) -->
<ImageButton
    android:layout_width="32dp"
    android:layout_height="32dp" />

<!-- GOOD: 48×48 dp minimum (56×56 dp recommended) -->
<ImageButton
    android:layout_width="56dp"
    android:layout_height="56dp"
    android:padding="16dp"  <!-- 24dp icon + 16dp padding -->
    android:src="@drawable/ic_action" />
```

### ❌ Pitfall 4: Forgetting Focus Restoration

```kotlin
// BAD: No focus restoration after interruption
override fun onResume() {
    super.onResume()
    // User returns from phone call - focus lost
}

// GOOD: Focus restoration implemented (Story 2.7 pattern)
override fun onResume() {
    super.onResume()
    if (isAccessibilityServiceEnabled() && lastFocusedViewId != null) {
        restoreFocus()
    }
}
```

---

## 8. Accessibility Requirements by Feature Type

### Buttons and Interactive Elements

- [ ] `contentDescription` present and descriptive (10-20 words)
- [ ] Touch target ≥48×48 dp (56×56 dp recommended)
- [ ] `importantForAccessibility="yes"`
- [ ] `isFocusable="true"`
- [ ] Ripple effect for visual feedback

### Lists and RecyclerViews (Epic 4+)

- [ ] List item role announced (`ViewCompat.setAccessibilityDelegate`)
- [ ] Item position announced ("Item 1 of 10")
- [ ] Grouping for related elements (`accessibilityHeading`)
- [ ] Custom actions for swipe-to-delete, etc.

### Forms and Text Inputs (Epic 9+)

- [ ] Label associated with input (`android:labelFor`)
- [ ] Error messages announced automatically
- [ ] Input type hints (`android:inputType`)
- [ ] Required fields indicated

### Navigation and Menus (Epic 5+)

- [ ] Current screen announced on navigation
- [ ] Back button accessible with clear description
- [ ] Menu items have unique descriptions
- [ ] Focus moves to first item on menu open

---

## 9. WCAG 2.1 AA Compliance Mapping

### Perceivable

| Guideline | VisionFocus Implementation |
|-----------|----------------------------|
| 1.1 Text Alternatives | All interactive elements have `contentDescription` |
| 1.3 Adaptable | Semantic HTML/XML structure, logical focus order |
| 1.4 Distinguishable | 7:1 contrast in high-contrast mode, large text support |

### Operable

| Guideline | VisionFocus Implementation |
|-----------|----------------------------|
| 2.1 Keyboard Accessible | Full TalkBack navigation (swipe, double-tap) |
| 2.2 Enough Time | No time limits on user actions |
| 2.4 Navigable | Logical focus order, skip links (future) |
| 2.5 Input Modalities | ≥48×48 dp touch targets, voice commands |

### Understandable

| Guideline | VisionFocus Implementation |
|-----------|----------------------------|
| 3.1 Readable | Clear language, confidence-aware phrasing |
| 3.2 Predictable | Consistent navigation, state announcements |
| 3.3 Input Assistance | Error prevention, clear recovery guidance |

### Robust

| Guideline | VisionFocus Implementation |
|-----------|----------------------------|
| 4.1 Compatible | TalkBack, Switch Access, external keyboard support |

---

## 10. Resources and References

### Android Accessibility Documentation

- [Android Accessibility Overview](https://developer.android.com/guide/topics/ui/accessibility)
- [Testing App Accessibility](https://developer.android.com/guide/topics/ui/accessibility/testing)
- [Accessibility Scanner](https://support.google.com/accessibility/android/answer/6376570)
- [TalkBack User Guide](https://support.google.com/accessibility/android/answer/6283677)

### WCAG 2.1 Guidelines

- [WCAG 2.1 Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/)
- [Understanding WCAG 2.1](https://www.w3.org/WAI/WCAG21/Understanding/)

### VisionFocus Internal References

- **Story 2.7:** Complete TalkBack Navigation (establishes baseline)
- **Architecture Doc:** [architecture.md#Decision 3: UI Architecture Approach]
- **Epic 2 Requirements:** [epics.md#Epic 2: Accessible Object Recognition]

### Manual Testing Resources

- **TalkBack Gestures:** Settings → Accessibility → TalkBack → Settings → Customize gestures
- **Test Device Setup:** Enable TalkBack, disable visual feedback, close eyes during testing

---

## 11. Code Review Checklist

### For All PRs

- [ ] All new interactive elements have `contentDescription`
- [ ] Touch targets ≥48×48 dp (verified with Accessibility Scanner)
- [ ] Decorative elements marked `importantForAccessibility="no"`
- [ ] Focus order validated (swipe right/left in TalkBack)
- [ ] State changes announced via `announceForAccessibility()`
- [ ] Error states provide clear recovery guidance
- [ ] Accessibility Scanner tests pass (zero errors)
- [ ] Manual TalkBack testing completed (documented in PR)

### Reviewer Questions

1. **"Can a blind user complete this feature using only TalkBack?"**
   - If no, implementation incomplete

2. **"Are all announcements clear, concise, and actionable?"**
   - No technical jargon, no redundant announcements

3. **"Does focus restoration work after interruptions?"**
   - Test with phone call, notification, split-screen

4. **"Are touch targets large enough?"**
   - ≥48×48 dp minimum, ≥56×56 dp recommended

---

## 12. Future Enhancements (Epic 8: Audio Priority Queue)

### Advanced Announcement Management

```kotlin
// Future: Epic 8 implementation
class AnnouncementQueue {
    enum class Priority {
        NAVIGATION,    // Highest: Turn-by-turn directions
        RECOGNITION,   // Medium: Object detection results
        GENERAL        // Low: State changes, confirmations
    }
    
    fun announce(message: String, priority: Priority) {
        // Queue management with interruption logic
    }
}
```

### Custom View Accessibility (If Needed)

```kotlin
// Custom view accessibility pattern
class CustomView : View {
    init {
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                
                // Set role
                info.roleDescription = "Custom Control"
                info.className = Button::class.java.name
                
                // Set state
                info.isChecked = isChecked
                info.contentDescription = "Custom action, double-tap to activate"
                
                // Add custom actions
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        "Activate"
                    )
                )
            }
        })
    }
}
```

---

## Conclusion

These guidelines establish **VisionFocus's accessibility baseline** for all future development. Every feature must maintain:

✅ **Zero Accessibility Scanner errors**  
✅ **WCAG 2.1 AA compliance**  
✅ **100% TalkBack operability**  
✅ **Manual testing with eyes closed**

**Remember:** For blind and low vision users, TalkBack is not an assistive technology—it's **the** technology. Design accordingly.

---

**Document Version:** 1.0  
**Last Updated:** December 30, 2025  
**Next Review:** After Epic 3 completion (Voice Commands)  
**Owner:** VisionFocus Development Team
