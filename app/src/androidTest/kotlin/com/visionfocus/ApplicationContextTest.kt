package com.visionfocus

import android.content.res.Resources
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented tests for VisionFocus application.
 * Validates Android context, resources, and accessibility requirements.
 */
@RunWith(AndroidJUnit4::class)
class ApplicationContextTest {
    
    @Test
    fun useAppContext() {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.visionfocus", appContext.packageName)
    }
    
    @Test
    fun appName_isCorrect() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = appContext.getString(R.string.app_name)
        assertEquals("VisionFocus", appName)
    }
    
    @Test
    fun mainScreen_contentDescription_exists() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val contentDesc = appContext.getString(R.string.main_screen_placeholder_description)
        assertNotNull("Content description must exist for TalkBack accessibility", contentDesc)
        assertTrue("Content description must not be empty", contentDesc.isNotEmpty())
    }
    
    @Test
    fun touchTargetSize_meetsAccessibilityRequirement() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val minTouchTargetDp = appContext.resources.getDimension(R.dimen.min_touch_target_size)
        val minTouchTargetPx = 48 * appContext.resources.displayMetrics.density
        
        assertTrue(
            "Minimum touch target size must be at least 48dp (${minTouchTargetPx}px on this device)",
            minTouchTargetDp >= minTouchTargetPx
        )
    }
    
    @Test
    fun bodyTextSize_meetsAccessibilityRequirement() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val bodyTextSizeSp = appContext.resources.getDimension(R.dimen.text_size_body)
        val expectedSizePx = 20 * appContext.resources.displayMetrics.scaledDensity
        
        assertTrue(
            "Body text size must be 20sp for accessibility (${expectedSizePx}px on this device)",
            bodyTextSizeSp >= expectedSizePx
        )
    }
    
    @Test
    fun darkThemeColors_areConfigured() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val backgroundColor = appContext.resources.getColor(R.color.background, null)
        val onBackgroundColor = appContext.resources.getColor(R.color.on_background, null)
        
        // Verify dark theme colors exist (not checking exact values, just that they're defined)
        assertNotEquals("Background color must be defined", 0, backgroundColor)
        assertNotEquals("On-background color must be defined", 0, onBackgroundColor)
    }
    
    @Test
    fun highContrastColors_areConfigured() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val highContrastBg = appContext.resources.getColor(R.color.high_contrast_background, null)
        val highContrastFg = appContext.resources.getColor(R.color.high_contrast_on_surface, null)
        
        // Verify high-contrast colors exist for future theme switching
        assertNotEquals("High-contrast background must be defined", 0, highContrastBg)
        assertNotEquals("High-contrast foreground must be defined", 0, highContrastFg)
    }
    
    @Test
    fun textView_meetsAccessibilityMinHeight() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val textView = activity.findViewById<View>(R.id.textView)
                assertNotNull("TextView must exist", textView)
                
                val heightDp = textView.minimumHeight / activity.resources.displayMetrics.density
                assertTrue(
                    "TextView minimum height must be at least 48dp for touch targets, got ${heightDp}dp",
                    heightDp >= 48f
                )
            }
        }
    }
    
    @Test
    fun mainActivity_hasAccessibleContentDescription() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val textView = activity.findViewById<View>(R.id.textView)
                val contentDesc = textView.contentDescription
                
                assertNotNull("TextView must have content description for TalkBack", contentDesc)
                assertTrue(
                    "Content description must not be blank",
                    contentDesc.isNotBlank()
                )
            }
        }
    }
}
