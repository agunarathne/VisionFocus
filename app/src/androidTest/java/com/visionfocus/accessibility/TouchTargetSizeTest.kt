package com.visionfocus.accessibility

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.BaseAccessibilityTest
import com.visionfocus.MainActivity
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generic touch target size validation test.
 * 
 * Validates FR23: All touch targets minimum 48×48 dp.
 * Can be extended in future stories for additional UI elements.
 */
@RunWith(AndroidJUnit4::class)
class TouchTargetSizeTest : BaseAccessibilityTest() {
    
    private lateinit var activityScenario: ActivityScenario<MainActivity>
    
    @Before
    fun setup() {
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }
    
    @After
    fun teardown() {
        activityScenario.close()
    }
    
    @Test
    fun allClickableElements_meetMinimumTouchTargetSize() {
        // Validate all clickable/focusable elements meet 48×48 dp minimum
        
        onView(allOf(isClickable(), isDisplayed()))
            .perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = isDisplayed()
                
                override fun getDescription(): String = "validate touch target size"
                
                override fun perform(uiController: UiController, view: View) {
                    val density = view.resources.displayMetrics.density
                    val heightDp = view.height / density
                    val widthDp = view.width / density
                    
                    assert(heightDp >= 48) {
                        "Touch target ${view.javaClass.simpleName} height ${heightDp}dp < 48dp minimum (FR23)"
                    }
                    
                    assert(widthDp >= 48) {
                        "Touch target ${view.javaClass.simpleName} width ${widthDp}dp < 48dp minimum (FR23)"
                    }
                }
            })
    }
}
