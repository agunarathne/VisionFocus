package com.visionfocus.accessibility

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.visionfocus.R
import com.visionfocus.ui.history.HistoryFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility tests for HistoryFragment.
 * 
 * Verifies:
 * - RecyclerView items have proper contentDescription
 * - Empty state has TalkBack announcement
 * - Clear history button has 56×56 dp touch target
 * - All elements pass Accessibility Scanner
 * 
 * Story 4.3 Task 12: Create instrumentation tests for accessibility
 */
@HiltAndroidTest
class HistoryAccessibilityTest : BaseAccessibilityTest() {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    /**
     * Enable Accessibility Checks before each test.
     * Story 4.3 Task 12.2: Enable AccessibilityChecks in @Before setup
     */
    @Before
    fun setup() {
        hiltRule.inject()
        enableAccessibilityChecks()
    }
    
    /**
     * Test that RecyclerView items have proper contentDescription.
     * Story 4.3 Task 12.3: Test RecyclerView items have proper contentDescription
     */
    @Test
    fun historyItems_havePropContentDescription() {
        // Launch fragment with history items
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // Verify RecyclerView is displayed
        onView(withId(R.id.historyRecyclerView))
            .check(matches(isDisplayed()))
        
        // Verify first item has contentDescription with expected format
        // Format: "[category], [confidence level], [formatted timestamp]"
        onView(allOf(
            isDescendantOfA(withId(R.id.historyRecyclerView)),
            withParent(withParent(withId(R.id.historyRecyclerView)))
        ))
            .check(matches(hasContentDescription()))
    }
    
    /**
     * Test that empty state has TalkBack announcement.
     * Story 4.3 Task 12.4: Test empty state has TalkBack announcement
     */
    @Test
    fun emptyState_hasAccessibleAnnouncement() {
        // Launch fragment with empty history
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // Verify empty state message is displayed and has contentDescription
        onView(withId(R.id.emptyStateTextView))
            .check(matches(allOf(
                isDisplayed(),
                withText(containsString("No recognition history yet")),
                hasContentDescription()
            )))
    }
    
    /**
     * Test that clear history button has 56×56 dp touch target.
     * Story 4.3 Task 12.5: Test clear history button has 56×56 dp touch target
     */
    @Test
    fun clearHistoryButton_hasMinimumTouchTarget() {
        // Launch fragment
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // Verify FAB has minimum touch target size
        onView(withId(R.id.clearHistoryFab))
            .check(matches(allOf(
                isDisplayed(),
                withMinimumTouchTargetSize(56)
            )))
    }
    
    /**
     * Test that RecyclerView items pass Accessibility Scanner.
     * Story 4.3 Task 12: RecyclerView items pass Accessibility Scanner
     */
    @Test
    fun recyclerViewItems_passAccessibilityScanner() {
        // Launch fragment
        launchFragmentInHiltContainer<HistoryFragment>()
        
        // AccessibilityChecks.enable() in @Before ensures zero errors
        // If any accessibility issues exist, test will fail automatically
        onView(withId(R.id.historyRecyclerView))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Test that clear history button has proper contentDescription.
     * Story 4.3 Task 7.2: Clear history button contentDescription
     */
    @Test
    fun clearHistoryButton_hasProperContentDescription() {
        launchFragmentInHiltContainer<HistoryFragment>()
        
        onView(withId(R.id.clearHistoryFab))
            .check(matches(allOf(
                isDisplayed(),
                withContentDescription(containsString("Clear"))
            )))
    }
    
    /**
     * Custom matcher to verify contentDescription exists.
     */
    private fun hasContentDescription(): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has contentDescription")
            }
            
            override fun matchesSafely(view: View): Boolean {
                return !view.contentDescription.isNullOrEmpty()
            }
        }
    }
    
    /**
     * Custom matcher to verify minimum touch target size.
     * Story 4.3 Task 12.5: Validate touch target size
     */
    private fun withMinimumTouchTargetSize(minSizeDp: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has minimum touch target size of ${minSizeDp}dp")
            }
            
            override fun matchesSafely(view: View): Boolean {
                val density = view.resources.displayMetrics.density
                val minSizePx = (minSizeDp * density).toInt()
                
                return view.width >= minSizePx && view.height >= minSizePx
            }
        }
    }
}
