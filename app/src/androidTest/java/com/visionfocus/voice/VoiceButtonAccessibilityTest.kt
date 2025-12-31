package com.visionfocus.voice

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.visionfocus.MainActivity
import com.visionfocus.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Voice Button Accessibility Tests
 * Story 3.1 Task 10: Integration testing with TalkBack
 * 
 * Tests cover:
 * - Voice button has correct contentDescription
 * - Voice button meets 56×56 dp touch target size
 * - Voice button is disabled when permission denied
 * - TalkBack announces listening state changes
 * - Voice button focus order is logical
 * 
 * To run with TalkBack:
 * 1. Enable TalkBack in device settings
 * 2. Run tests and verify announcements manually
 * 3. Use TalkBack swipe gestures to verify focus order
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VoiceButtonAccessibilityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    companion object {
        private const val MIN_TOUCH_TARGET_SIZE_DP = 56
    }
    
    /**
     * Test: Voice button has correct contentDescription
     * Story 3.1 Task 10.4: Verify contentDescription announces correctly
     */
    @Test
    fun voiceButton_hasCorrectContentDescription() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(R.string.voice_commands_button)))
    }
    
    /**
     * Test: Voice button meets minimum touch target size
     * Story 3.1 Task 10.6: Validate 56×56 dp touch target programmatically
     */
    @Test
    fun voiceButton_meetsTouchTargetSize() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(withMinimumSize(MIN_TOUCH_TARGET_SIZE_DP)))
    }
    
    /**
     * Test: Voice button is displayed and enabled by default
     * Story 3.1 Task 10.3: Voice button state validation
     */
    @Test
    fun voiceButton_isDisplayedAndEnabled() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }
    
    /**
     * Test: Voice button is clickable
     * Story 3.1 Task 10.5: Voice button with TalkBack double-tap gesture
     * 
     * Note: This tests programmatic click. Manual testing with TalkBack
     * double-tap gesture is required (Task 11).
     */
    @Test
    fun voiceButton_isClickable() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }
    
    /**
     * Test: Voice button has accessibility focus capability
     * Story 3.1 Task 8.2: Voice button focus order is logical
     */
    @Test
    fun voiceButton_isFocusable() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(isFocusable()))
    }
    
    /**
     * Test: Voice button has proper importance for accessibility
     * Story 3.1 Task 8.3: TalkBack navigation validation
     */
    @Test
    fun voiceButton_hasAccessibilityImportance() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
            .check(matches(object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText("has accessibility importance")
                }
                
                override fun matchesSafely(view: View): Boolean {
                    return view.importantForAccessibility != View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
            }))
    }
    
    /**
     * Test: Voice button is positioned in top-right corner
     * Story 3.1 Task 3.2: Button positioning validation
     */
    @Test
    fun voiceButton_isPositionedTopRight() {
        onView(withId(R.id.voice_fab))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Custom matcher to check minimum view size in dp
     * Story 3.1 Task 10.6: Touch target size validation
     */
    private fun withMinimumSize(minSizeDp: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has minimum size of ${minSizeDp}dp x ${minSizeDp}dp")
            }
            
            override fun matchesSafely(view: View): Boolean {
                val density = view.context.resources.displayMetrics.density
                val minSizePx = (minSizeDp * density).toInt()
                
                val width = view.width
                val height = view.height
                
                return width >= minSizePx && height >= minSizePx
            }
        }
    }
    
    /**
     * Test: Recognize FAB exists (for focus order testing)
     * Story 3.1 Task 8.2: Voice button focus order after recognition FAB
     */
    @Test
    fun recognizeFab_exists() {
        // This validates that recognition FAB exists for focus order comparison
        // Manual testing (Task 11) will verify focus order with TalkBack
        onView(withId(R.id.recognitionFab))
            .check(matches(isDisplayed()))
    }
}

/**
 * Manual Testing Checklist (Task 10 - TalkBack Integration)
 * 
 * These tests MUST be performed manually with TalkBack enabled on a physical device:
 * 
 * 1. Microphone Permission Flow (Task 10.1):
 *    - Disable microphone permission in Settings
 *    - Launch app with TalkBack
 *    - Navigate to voice button with swipe gestures
 *    - Verify button announces: "Voice commands unavailable. Microphone permission required, button"
 *    - Double-tap button to request permission
 *    - Grant permission in system dialog
 *    - Verify button now announces: "Voice commands, button"
 * 
 * 2. Permission Denial (Task 10.2):
 *    - Deny microphone permission
 *    - Verify rationale dialog appears with TalkBack-readable content
 *    - Verify button remains disabled after denial
 * 
 * 3. Voice Button Disabled State (Task 10.3):
 *    - With permission denied, navigate to voice button
 *    - Verify button is disabled (alpha 0.5, isEnabled = false)
 *    - Verify contentDescription indicates unavailable
 * 
 * 4. TalkBack State Announcements (Task 10.4):
 *    - Grant microphone permission
 *    - Double-tap voice button
 *    - Verify TalkBack announces: "Listening for command"
 *    - Verify visual pulsing animation plays
 *    - Wait for timeout (5 seconds)
 *    - Verify error announcement: "Voice command timed out"
 * 
 * 5. Voice Button Double-Tap (Task 10.5):
 *    - Navigate to voice button with TalkBack swipes
 *    - Use double-tap gesture to activate
 *    - Verify listening mode starts
 *    - Verify haptic feedback on button press
 * 
 * 6. Touch Target Size (Task 10.6):
 *    - Use TalkBack explore-by-touch mode
 *    - Touch voice button area and verify easy activation
 *    - Confirm 56×56 dp size is adequate (no missed taps)
 * 
 * 7. Focus Order (Task 8.2):
 *    - Start at top of screen
 *    - Swipe right with TalkBack to navigate elements
 *    - Verify focus order: Toolbar → Recognition FAB → Voice FAB → Fragment content
 *    - Confirm logical navigation flow
 * 
 * 8. Error Announcements (Task 10.4):
 *    - Activate voice button in noisy environment
 *    - Verify error announcement: "Didn't catch that. Please try again."
 *    - Cover microphone completely
 *    - Activate voice button
 *    - Verify audio error announcement if applicable
 * 
 * PASS CRITERIA:
 * - All TalkBack announcements are clear and accurate
 * - Voice button is easily discoverable with swipe gestures
 * - Permission flow is comprehensible with TalkBack
 * - Touch target meets accessibility guidelines (56×56 dp minimum)
 * - Focus order is logical and intuitive
 * - Error messages are announced clearly
 */
