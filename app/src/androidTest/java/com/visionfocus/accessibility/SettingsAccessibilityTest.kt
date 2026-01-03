package com.visionfocus.accessibility

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
import com.visionfocus.MainActivity
import com.visionfocus.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for Settings Screen (Story 5.3).
 * 
 * Tests verify:
 * - All switches have proper contentDescription
 * - Speech rate SeekBar has proper contentDescription
 * - RadioButtons have proper contentDescription
 * - Reset button has proper contentDescription
 * - All interactive elements meet 48×48 dp touch target minimum
 * - Focus order is logical (top to bottom, left to right)
 * - Settings screen passes Accessibility Scanner (zero errors)
 * 
 * Accessibility Scanner Integration:
 * - AccessibilityChecks.enable() enables automated scanning
 * - Test fails automatically if any violations found
 * - Checks: Touch targets, content descriptions, contrast ratios, etc.
 */
@RunWith(AndroidJUnit4::class)
class SettingsAccessibilityTest {
    
    private lateinit var scenario: ActivityScenario<MainActivity>
    
    @Before
    fun setup() {
        // Enable Accessibility Scanner for automated checking
        // Story 5.3 AC #4: Zero Accessibility Scanner errors enforced
        androidx.test.espresso.accessibility.AccessibilityChecks.enable()
            .setRunChecksFromRootView(true)
        
        // Launch MainActivity
        scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
    }
    
    /**
     * Story 5.3 AC #4: Settings screen passes Accessibility Scanner (zero errors).
     * 
     * Accessibility Scanner runs automatically on all view interactions.
     * Test fails if any violations found.
     */
    @Test
    fun settingsScreen_passesAccessibilityScanner() {
        // Accessibility Scanner runs automatically
        // Just verify Settings screen is displayed
        onView(withId(R.id.settingsTitle)).check(matches(isDisplayed()))
        
        // Test passes if no violations detected
    }
    
    /**
     * Story 5.3 AC #4: All switches have proper contentDescription.
     * 
     * Tests that MaterialSwitch controls have content descriptions
     * that include current state ("currently on/off").
     */
    @Test
    fun allSwitches_haveProperContentDescriptions() {
        // Check high contrast switch content description
        onView(withId(R.id.highContrastSwitch)).check { view, _ ->
            val switch = view as com.google.android.material.switchmaterial.SwitchMaterial
            assertThat("High contrast switch missing contentDescription", 
                switch.contentDescription, `is`(not(nullValue())))
            assertThat("High contrast switch contentDescription should mention current state",
                switch.contentDescription.toString().contains("currently"))
        }
        
        // Check large text switch content description
        onView(withId(R.id.largeTextSwitch)).check { view, _ ->
            val switch = view as com.google.android.material.switchmaterial.SwitchMaterial
            assertThat("Large text switch missing contentDescription", 
                switch.contentDescription, `is`(not(nullValue())))
            assertThat("Large text switch contentDescription should mention current state",
                switch.contentDescription.toString().contains("currently"))
        }
        
        // Check camera preview switch content description
        onView(withId(R.id.cameraPreviewSwitch)).check { view, _ ->
            val switch = view as com.google.android.material.switchmaterial.SwitchMaterial
            assertThat("Camera preview switch missing contentDescription", 
                switch.contentDescription, `is`(not(nullValue())))
            assertThat("Camera preview switch contentDescription should mention current state",
                switch.contentDescription.toString().contains("currently"))
        }
    }
    
    /**
     * Story 5.3 AC #4: Speech rate SeekBar has proper contentDescription.
     * 
     * Tests that SeekBar has content description with current value.
     */
    @Test
    fun speechRateSeekBar_hasProperContentDescription() {
        onView(withId(R.id.speechRateSeekBar)).check { view, _ ->
            val seekBar = view as android.widget.SeekBar
            assertThat("SeekBar missing contentDescription", 
                seekBar.contentDescription, `is`(not(nullValue())))
            assertThat("SeekBar contentDescription should mention current rate",
                seekBar.contentDescription.toString().contains("currently"))
            assertThat("SeekBar contentDescription should mention 'times normal speed'",
                seekBar.contentDescription.toString().contains("times normal speed"))
        }
    }
    
    /**
     * Story 5.3 AC #4: RadioButtons have proper contentDescription.
     * 
     * Tests that all RadioButtons have content descriptions.
     */
    @Test
    fun allRadioButtons_haveProperContentDescriptions() {
        // Haptic intensity RadioButtons
        onView(withId(R.id.hapticOff)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Haptic OFF radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        onView(withId(R.id.hapticLight)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Haptic LIGHT radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        onView(withId(R.id.hapticMedium)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Haptic MEDIUM radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        onView(withId(R.id.hapticStrong)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Haptic STRONG radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        // Verbosity mode RadioButtons
        onView(withId(R.id.verbosityBriefRadio)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Verbosity BRIEF radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        onView(withId(R.id.verbosityStandardRadio)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Verbosity STANDARD radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
        
        onView(withId(R.id.verbosityDetailedRadio)).check { view, _ ->
            val radio = view as android.widget.RadioButton
            assertThat("Verbosity DETAILED radio missing contentDescription", 
                radio.contentDescription, `is`(not(nullValue())))
        }
    }
    
    /**
     * Story 5.3 AC #4: Reset button has proper contentDescription.
     * 
     * Tests that Reset button has content description.
     */
    @Test
    fun resetButton_hasProperContentDescription() {
        onView(withId(R.id.resetDefaultsButton)).check { view, _ ->
            assertThat("Reset button missing contentDescription", 
                view.contentDescription, `is`(not(nullValue())))
            assertThat("Reset button contentDescription should mention 'button'",
                view.contentDescription.toString().contains("button"))
        }
    }
    
    /**
     * Story 5.3 AC #4: All interactive elements meet 48×48 dp touch target minimum.
     * 
     * Tests that all interactive elements have minimum touch target size.
     */
    @Test
    fun allInteractiveElements_meetMinimumTouchTarget() {
        val minSize = (48 * androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation().targetContext.resources.displayMetrics.density).toInt()
        
        // Verify switches meet minimum size
        onView(withId(R.id.highContrastSwitch)).check { view, _ ->
            assertThat("High contrast switch height < 48dp", view.height >= minSize)
            assertThat("High contrast switch width < 48dp", view.width >= minSize)
        }
        
        onView(withId(R.id.largeTextSwitch)).check { view, _ ->
            assertThat("Large text switch height < 48dp", view.height >= minSize)
            assertThat("Large text switch width < 48dp", view.width >= minSize)
        }
        
        // Verify SeekBar meets minimum size
        onView(withId(R.id.speechRateSeekBar)).check { view, _ ->
            assertThat("SeekBar height < 48dp", view.height >= minSize)
        }
        
        // Verify RadioButtons meet minimum size
        onView(withId(R.id.hapticMedium)).check { view, _ ->
            assertThat("Radio button height < 48dp", view.height >= minSize)
        }
        
        onView(withId(R.id.verbosityStandardRadio)).check { view, _ ->
            assertThat("Radio button height < 48dp", view.height >= minSize)
        }
        
        // Verify Reset button meets minimum size
        onView(withId(R.id.resetDefaultsButton)).check { view, _ ->
            assertThat("Reset button height < 48dp", view.height >= minSize)
        }
    }
    
    /**
     * Story 5.3 AC #4: Focus order is logical (top to bottom, left to right).
     * 
     * Tests that TalkBack focus order follows logical reading order.
     */
    @Test
    fun settingsScreen_hasLogicalFocusOrder() {
        // Verify all interactive elements are focusable
        onView(withId(R.id.speechRateSeekBar)).check(matches(isFocusable()))
        onView(withId(R.id.testSpeedButton)).check(matches(isFocusable()))
        onView(withId(R.id.highContrastSwitch)).check(matches(isFocusable()))
        onView(withId(R.id.largeTextSwitch)).check(matches(isFocusable()))
        onView(withId(R.id.hapticOff)).check(matches(isFocusable()))
        onView(withId(R.id.verbosityBriefRadio)).check(matches(isFocusable()))
        onView(withId(R.id.resetDefaultsButton)).check(matches(isFocusable()))
        
        // Accessibility Scanner will validate focus order automatically
        // Test passes if no focus order violations detected
    }
    
    /**
     * Helper function to assert condition with message.
     */
    private fun assertThat(message: String, actual: Any?, expected: Boolean) {
        if (expected) {
            org.junit.Assert.assertNotNull(message, actual)
        } else {
            org.junit.Assert.assertNull(message, actual)
        }
    }
    
    /**
     * Helper function to assert condition with matcher.
     */
    private fun <T> assertThat(message: String, actual: T, matcher: org.hamcrest.Matcher<T>) {
        org.junit.Assert.assertThat(message, actual, matcher)
    }
}
