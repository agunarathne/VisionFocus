package com.visionfocus.accessibility

import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.BaseAccessibilityTest
import com.visionfocus.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generic touch target size validation test.
 * 
 * Validates FR23: All touch targets minimum 48×48 dp.
 * Can be extended in future stories for additional UI elements.
 * 
 * FIXED: Issue #6 - Now properly iterates through ALL clickable elements
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
        // Fixed: Now iterates through all elements instead of just one
        
        activityScenario.onActivity { activity ->
            val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
            val clickableViews = mutableListOf<View>()
            
            // Recursively find all clickable views
            findClickableViews(rootView, clickableViews)
            
            val density = activity.resources.displayMetrics.density
            val violations = mutableListOf<String>()
            
            for (view in clickableViews) {
                if (!view.isShown) continue  // Skip hidden views
                
                val heightDp = view.height / density
                val widthDp = view.width / density
                
                if (heightDp < 48 || widthDp < 48) {
                    violations.add(
                        "Touch target ${view.javaClass.simpleName} " +
                        "(id=${view.id}) size ${widthDp.toInt()}×${heightDp.toInt()} dp " +
                        "< 48×48 dp minimum (FR23)"
                    )
                }
            }
            
            assert(violations.isEmpty()) {
                "Touch target size violations:\n${violations.joinToString("\n")}"
            }
        }
    }
    
    /**
     * Recursively find all clickable views in the view hierarchy.
     */
    private fun findClickableViews(view: View, result: MutableList<View>) {
        if (view.isClickable || view.isFocusable) {
            result.add(view)
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findClickableViews(view.getChildAt(i), result)
            }
        }
    }
}
