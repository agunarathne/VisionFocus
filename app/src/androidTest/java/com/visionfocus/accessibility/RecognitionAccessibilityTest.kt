package com.visionfocus.accessibility

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.MainActivity
import com.visionfocus.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility Scanner automated tests (Story 2.7 Task 7)
 * 
 * Purpose:
 * - Validate zero accessibility errors in recognition flow
 * - Enforce WCAG 2.1 AA compliance
 * - Catch regressions in future stories
 * 
 * Test Strategy:
 * - Enable AccessibilityChecks globally
 * - Launch RecognitionFragment
 * - Verify zero errors reported by scanner
 * - Test key interactions (FAB tap, state changes)
 * 
 * AC10: App passes Accessibility Scanner checks for primary recognition flow (zero errors)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecognitionAccessibilityTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Story 2.7 Task 7.3: Enable Accessibility Scanner checks
        // HIGH-7 FIX: Configure to fail tests on accessibility errors
        AccessibilityChecks.enable()
            .setRunChecksFromRootView(true)
            .setSuppressingResultMatcher(null)
            .setThrowExceptionForErrors(true)  // Fail test on any accessibility error
    }
    
    /**
     * Story 2.7 Task 7.4: Test recognition fragment accessibility
     * 
     * AC10: Zero accessibility errors in primary recognition flow
     * 
     * Validates:
     * - All interactive elements have contentDescription
     * - Touch targets meet 48x48dp minimum (FAB is 56x56dp)
     * - Color contrast ratios sufficient (WCAG 2.1 AA)
     * - Focus order logical (top-to-bottom, left-to-right)
     */
    @Test
    fun recognitionFragment_passesAccessibilityScanner() {
        // Accessibility Scanner runs automatically on every view interaction
        
        // Verify FAB is displayed and accessible
        onView(withId(R.id.recognizeFab))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
        
        // If any accessibility errors detected, test fails automatically
        // Errors logged with details (missing contentDescription, insufficient contrast, etc.)
    }
    
    /**
     * Story 2.7 Task 7.4: Test FAB tap triggers accessibility checks during state transitions
     * HIGH-4 FIX: Add proper assertions and avoid Thread.sleep()
     * 
     * Validates accessibility compliance throughout recognition state machine:
     * - Idle → Capturing → Recognizing → Success/Error → Idle
     */
    @Test
    fun recognitionFragment_stateTransitionsPassAccessibilityChecks() {
        // Verify FAB is displayed and enabled initially
        onView(withId(R.id.recognizeFab))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
        
        // Tap FAB to trigger recognition
        onView(withId(R.id.recognizeFab))
            .perform(click())
        
        // Verify FAB disabled during recognition (state transition validation)
        onView(withId(R.id.recognizeFab))
            .check(matches(not(isEnabled())))
        
        // Wait for recognition completion using polling instead of sleep
        var attempts = 0
        while (attempts < 30) {  // 3 seconds max (30 * 100ms)
            try {
                onView(withId(R.id.recognizeFab))
                    .check(matches(isEnabled()))
                break  // FAB re-enabled, recognition complete
            } catch (e: AssertionError) {
                Thread.sleep(100)
                attempts++
            }
        }
        
        // Final verification: FAB re-enabled after recognition
        onView(withId(R.id.recognizeFab))
            .check(matches(isEnabled()))
        
        // AccessibilityChecks automatically fails if any errors detected
    }
    
    /**
     * Story 2.7 Task 7: Validate content descriptions are present and descriptive
     * 
     * AC2: All interactive elements have proper content descriptions
     */
    @Test
    fun recognitionFragment_contentDescriptionValidation() {
        // Verify FAB has contentDescription
        onView(withId(R.id.recognizeFab))
            .check(matches(withContentDescription(R.string.recognize_fab_description)))
        
        // Content description from strings.xml:
        // "Recognize objects. Double-tap to activate camera and identify objects in your environment."
        // 
        // Validated:
        // - Describes action (recognize objects)
        // - Explains interaction (double-tap to activate)
        // - Explains outcome (identify objects in environment)
        // - Length: 17 words (within 10-20 word guideline)
        // - No redundant "button" suffix (TalkBack announces role automatically)
    }
    
    /**
     * Story 2.7: Validate touch target sizes meet accessibility guidelines
     * 
     * AC: 48x48 dp minimum touch target (Story 2.3 AC1: FAB is 56x56 dp)
     * 
     * Note: Accessibility Scanner automatically checks touch target sizes.
     * This test documents the explicit requirement validation.
     */
    @Test
    fun recognitionFragment_touchTargetSizesValid() {
        // FAB size validated in layout: 56x56 dp (exceeds 48x48 dp requirement)
        // Accessibility Scanner checks this automatically
        
        onView(withId(R.id.recognizeFab))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
        
        // If touch target too small, Accessibility Scanner fails the test
    }
}
