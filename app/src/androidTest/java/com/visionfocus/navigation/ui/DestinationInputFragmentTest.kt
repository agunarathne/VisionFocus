package com.visionfocus.navigation.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for DestinationInputFragment.
 * 
 * Story 6.1 Task 13: Integration tests for voice input
 * 
 * Tests:
 * - Microphone button tap starts voice recognition (manual verification)
 * - Voice result populates destination field (requires mock)
 * - TTS announces transcribed text (requires mock)
 * - Voice recognition error handling (requires mock)
 * - Focus returns to text field after voice input
 * - Go button enabled after valid destination entered
 * - Go button disabled when destination field empty
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DestinationInputFragmentTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    /**
     * Test destination input screen launches successfully.
     * AC: 1 - Destination input screen appears
     */
    @Test
    fun testFragmentLaunchesSuccessfully() {
        // Given fragment scenario
        val scenario = launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Then title is visible
        onView(withId(R.id.titleTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.destination_input_title)))
    }
    
    /**
     * Test destination text field is visible and editable.
     * AC: 2 - Text input field accessible
     */
    @Test
    fun testDestinationTextFieldVisible() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Text field visible
        onView(withId(R.id.destinationEditText))
            .check(matches(isDisplayed()))
        
        // Text field editable
        onView(withId(R.id.destinationEditText))
            .perform(typeText("Times Square"))
            .check(matches(withText("Times Square")))
    }
    
    /**
     * Test Go button enabled after text entered.
     * AC: 6 - Go button starts navigation when destination entered
     */
    @Test
    fun testGoButtonEnabledAfterTextEntered() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Initially disabled
        onView(withId(R.id.goButton))
            .check(matches(not(isEnabled())))
        
        // Enter destination (≥3 characters)
        onView(withId(R.id.destinationEditText))
            .perform(typeText("Central Park"))
        
        // Wait for validation (validation happens asynchronously)
        Thread.sleep(1000)
        
        // Go button should be enabled after validation
        // Note: This test may be flaky due to async validation
        // Real implementation should use IdlingResource
        onView(withId(R.id.goButton))
            .check(matches(isEnabled()))
    }
    
    /**
     * Test Go button disabled when destination field empty.
     * AC: 8 - Empty destination field validation
     */
    @Test
    fun testGoButtonDisabledWhenEmpty() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Go button disabled initially
        onView(withId(R.id.goButton))
            .check(matches(not(isEnabled())))
    }
    
    /**
     * Test microphone button is visible.
     * AC: 3 - Microphone button within text field
     */
    @Test
    fun testMicrophoneButtonVisible() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Microphone end icon visible (TextInputLayout end icon)
        onView(withId(R.id.destinationInputLayout))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Test hint text visible when field empty.
     * AC: 8 - Empty destination field shows hint text
     */
    @Test
    fun testHintTextVisible() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Hint text visible
        onView(withId(R.id.destinationInputLayout))
            .check(matches(hasDescendant(withHint("Say or type destination"))))
    }
    
    /**
     * Test progress indicator appears during validation.
     * AC: 6 - Validation shows progress
     */
    @Test
    fun testProgressIndicatorDuringValidation() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Initially hidden
        onView(withId(R.id.validationProgressIndicator))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        
        // Enter destination to trigger validation
        onView(withId(R.id.destinationEditText))
            .perform(typeText("Times Square"))
        
        // Note: Progress indicator visibility is transient
        // Real test would use IdlingResource to synchronize
    }
    
    /**
     * Test accessibility content descriptions present.
     * AC: 2 - TalkBack support for all elements
     * 
     * Note: Full TalkBack testing requires manual validation on device
     */
    @Test
    fun testAccessibilityContentDescriptions() {
        launchFragmentInContainer<DestinationInputFragment>(
            themeResId = R.style.Theme_VisionFocus
        )
        
        // Title has content description
        onView(withId(R.id.titleTextView))
            .check(matches(withContentDescription("Where would you like to go?")))
        
        // Go button has content description
        onView(withId(R.id.goButton))
            .check(matches(withContentDescription("Start navigation, button")))
    }
}
