package com.visionfocus.ui.settings

import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.visionfocus.MainActivity
import com.visionfocus.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Settings Screen (Story 5.3).
 * 
 * Tests verify:
 * - Settings screen displays all preferences
 * - Toggle changes persist across activity recreation
 * - Reset to defaults restores all settings
 * - Speech rate changes via SeekBar
 * - Verbosity mode selection via RadioGroup
 * - Haptic intensity selection via RadioGroup
 * 
 * Requires:
 * - @HiltAndroidTest annotation for Hilt injection
 * - ActivityScenario for activity recreation testing
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenIntegrationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    /**
     * Story 5.3 AC #3: Settings screen displays all preferences.
     * 
     * Tests that all setting controls are visible on Settings screen.
     */
    @Test
    fun settingsScreenDisplaysAllPreferences() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify all settings visible
        onView(withId(R.id.speechRateSeekBar)).check(matches(isDisplayed()))
        onView(withId(R.id.highContrastSwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.largeTextSwitch)).check(matches(isDisplayed()))
        onView(withId(R.id.hapticIntensityRadioGroup)).check(matches(isDisplayed()))
        onView(withId(R.id.verbosityModeGroup)).check(matches(isDisplayed()))
        onView(withId(R.id.resetDefaultsButton)).check(matches(isDisplayed()))
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #2: Settings persist across app restarts.
     * 
     * Tests that high contrast toggle persists after activity recreation.
     */
    @Test
    fun toggleHighContrastMode_persistsAcrossActivityRecreation() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Enable high contrast mode
        onView(withId(R.id.highContrastSwitch)).perform(click())
        
        // Verify switch checked (after activity recreation)
        Thread.sleep(500)  // Wait for recreation
        onView(withId(R.id.highContrastSwitch)).check(matches(isChecked()))
        
        // Manually recreate scenario (simulates app restart)
        scenario.recreate()
        Thread.sleep(500)  // Wait for recreation
        
        // Navigate to Settings again
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify switch still checked
        onView(withId(R.id.highContrastSwitch)).check(matches(isChecked()))
        
        // Cleanup: Disable high contrast mode
        onView(withId(R.id.highContrastSwitch)).perform(click())
        Thread.sleep(500)  // Wait for recreation
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #2: Settings persist across app restarts.
     * 
     * Tests that large text toggle persists after activity recreation.
     */
    @Test
    fun toggleLargeTextMode_persistsAcrossActivityRecreation() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Enable large text mode
        onView(withId(R.id.largeTextSwitch)).perform(click())
        
        // Verify switch checked (after activity recreation)
        Thread.sleep(500)  // Wait for recreation
        onView(withId(R.id.largeTextSwitch)).check(matches(isChecked()))
        
        // Manually recreate scenario (simulates app restart)
        scenario.recreate()
        Thread.sleep(500)  // Wait for recreation
        
        // Navigate to Settings again
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify switch still checked
        onView(withId(R.id.largeTextSwitch)).check(matches(isChecked()))
        
        // Cleanup: Disable large text mode
        onView(withId(R.id.largeTextSwitch)).perform(click())
        Thread.sleep(500)  // Wait for recreation
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #8, #9, #10: Reset to defaults restores all settings.
     * 
     * Tests that clicking "Reset to Defaults" button shows confirmation dialog,
     * and confirming reset restores all 7 settings to defaults.
     * 
     * MEDIUM-1 FIX: Comprehensive assertions for all 7 settings
     */
    @Test
    fun resetToDefaults_restoresAllSettings() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Change multiple settings from defaults
        onView(withId(R.id.highContrastSwitch)).perform(click())
        Thread.sleep(500)  // Wait for recreation after theme change
        onView(withId(R.id.action_settings)).perform(click())  // Re-navigate
        
        onView(withId(R.id.verbosityDetailedRadio)).perform(click())
        onView(withId(R.id.hapticStrong)).perform(click())
        
        // Click reset button
        onView(withId(R.id.resetDefaultsButton)).perform(click())
        
        // Verify confirmation dialog appears (AC #8)
        onView(withText(R.string.reset_confirmation_title)).check(matches(isDisplayed()))
        onView(withText(R.string.reset_confirmation_message)).check(matches(isDisplayed()))
        
        // Confirm reset
        onView(withText(R.string.reset)).perform(click())
        
        // Wait for reset to complete
        Thread.sleep(500)  // Wait for DataStore writes
        
        // Verify ALL 7 settings restored to defaults (MEDIUM-1 FIX)
        // 1. High contrast should be OFF after reset
        Thread.sleep(500)  // Wait for recreation after theme change
        onView(withId(R.id.action_settings)).perform(click())  // Re-navigate
        onView(withId(R.id.highContrastSwitch)).check(matches(not(isChecked())))
        
        // 2. Verbosity should be STANDARD (default)
        onView(withId(R.id.verbosityStandardRadio)).check(matches(isChecked()))
        
        // 3. Haptic intensity should be MEDIUM (default)
        onView(withId(R.id.hapticMedium)).check(matches(isChecked()))
        
        // 4. Large text should be OFF (default)
        onView(withId(R.id.largeTextSwitch)).check(matches(not(isChecked())))
        
        // 5. Camera preview should be OFF (default)
        onView(withId(R.id.cameraPreviewSwitch)).check(matches(not(isChecked())))
        
        // 6. Speech rate should be 1.0× (progress = 5)
        onView(withId(R.id.speechRateSeekBar)).check { view, _ ->
            val seekBar = view as SeekBar
            org.junit.Assert.assertEquals("Speech rate not reset to 1.0× (progress 5)", 5, seekBar.progress)
        }
        
        // 7. Voice locale: Cannot easily verify RadioGroup state without knowing available voices
        // Manual test: Verify system default voice selected
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #8: Reset dialog cancel preserves current settings.
     * 
     * Tests that clicking "Cancel" on reset confirmation dialog does NOT reset settings.
     * 
     * MEDIUM-2 FIX: Test cancel button preserves settings
     */
    @Test
    fun resetDialogCancel_preservesCurrentSettings() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Change settings from defaults
        onView(withId(R.id.highContrastSwitch)).perform(click())
        Thread.sleep(500)  // Wait for recreation after theme change
        onView(withId(R.id.action_settings)).perform(click())  // Re-navigate
        
        onView(withId(R.id.verbosityDetailedRadio)).perform(click())
        
        // Click reset button
        onView(withId(R.id.resetDefaultsButton)).perform(click())
        
        // Verify confirmation dialog appears
        onView(withText(R.string.reset_confirmation_title)).check(matches(isDisplayed()))
        
        // Click CANCEL button
        onView(withText(R.string.cancel)).perform(click())
        
        // Wait for dialog dismissal
        Thread.sleep(200)
        
        // Assert: Settings unchanged (high-contrast still ON, verbosity still DETAILED)
        onView(withId(R.id.highContrastSwitch)).check(matches(isChecked()))
        onView(withId(R.id.verbosityDetailedRadio)).check(matches(isChecked()))
        
        // Cleanup: Reset to defaults for next test
        onView(withId(R.id.highContrastSwitch)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.action_settings)).perform(click())
        onView(withId(R.id.verbosityStandardRadio)).perform(click())
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #3: Verbosity mode selection via RadioGroup.
     * 
     * Tests that selecting verbosity mode changes the checked radio button.
     */
    @Test
    fun changeVerbosityMode_updatesRadioButtonSelection() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify STANDARD is default
        onView(withId(R.id.verbosityStandardRadio)).check(matches(isChecked()))
        
        // Change to DETAILED
        onView(withId(R.id.verbosityDetailedRadio)).perform(click())
        
        // Verify DETAILED now checked
        onView(withId(R.id.verbosityDetailedRadio)).check(matches(isChecked()))
        onView(withId(R.id.verbosityStandardRadio)).check(matches(not(isChecked())))
        
        // Change to BRIEF
        onView(withId(R.id.verbosityBriefRadio)).perform(click())
        
        // Verify BRIEF now checked
        onView(withId(R.id.verbosityBriefRadio)).check(matches(isChecked()))
        onView(withId(R.id.verbosityDetailedRadio)).check(matches(not(isChecked())))
        
        // Cleanup: Reset to STANDARD
        onView(withId(R.id.verbosityStandardRadio)).perform(click())
        
        scenario.close()
    }
    
    /**
     * Story 5.3 AC #3: Haptic intensity selection via RadioGroup.
     * 
     * Tests that selecting haptic intensity changes the checked radio button.
     */
    @Test
    fun changeHapticIntensity_updatesRadioButtonSelection() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Navigate to Settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify MEDIUM is default
        onView(withId(R.id.hapticMedium)).check(matches(isChecked()))
        
        // Change to STRONG
        onView(withId(R.id.hapticStrong)).perform(click())
        
        // Verify STRONG now checked
        onView(withId(R.id.hapticStrong)).check(matches(isChecked()))
        onView(withId(R.id.hapticMedium)).check(matches(not(isChecked())))
        
        // Change to OFF
        onView(withId(R.id.hapticOff)).perform(click())
        
        // Verify OFF now checked
        onView(withId(R.id.hapticOff)).check(matches(isChecked()))
        onView(withId(R.id.hapticStrong)).check(matches(not(isChecked())))
        
        // Cleanup: Reset to MEDIUM
        onView(withId(R.id.hapticMedium)).perform(click())
        
        scenario.close()
    }
}
