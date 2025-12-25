package com.visionfocus.permissions

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.visionfocus.BaseAccessibilityTest
import com.visionfocus.MainActivity
import com.visionfocus.R
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for camera permission flow.
 * 
 * Validates:
 * - AC 2: Permission UI has TalkBack semantic labels
 * - AC 8: Content descriptions present
 * - AC 9: Touch targets meet 48×48 dp minimum
 * - WCAG 2.1 AA compliance for permission dialogs
 * 
 * FIXED: Added missing test methods from story requirements
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CameraPermissionAccessibilityTest : BaseAccessibilityTest() {
    
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
    fun mainActivity_passesAutomatedAccessibilityChecks() {
        // This test automatically validates WCAG 2.1 AA via AccessibilityChecks.enable()
        // Any violations will cause test failure with detailed report
        
        // Perform basic interaction to trigger checks
        onView(isRoot())
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun mainActivity_hasTextView() {
        // Verify the main text view is displayed
        onView(withId(R.id.textView))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun permissionButtons_meetMinimumTouchTargetSize() {
        // Validate AC 9: 48×48 dp minimum touch targets
        // Note: This test verifies the layout definitions, not runtime dialog
        // Runtime dialog testing requires device with permission denied state
        
        activityScenario.onActivity { activity ->
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_permission_rationale, null)
            
            val allowButton = dialogView.findViewById<View>(R.id.rationaleAllowButton)
            val declineButton = dialogView.findViewById<View>(R.id.rationaleDeclineButton)
            
            // Measure views
            dialogView.measure(
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            dialogView.layout(0, 0, dialogView.measuredWidth, dialogView.measuredHeight)
            
            val density = activity.resources.displayMetrics.density
            
            val allowHeightDp = allowButton.measuredHeight / density
            val allowWidthDp = allowButton.measuredWidth / density
            
            val declineHeightDp = declineButton.measuredHeight / density
            val declineWidthDp = declineButton.measuredWidth / density
            
            assert(allowHeightDp >= 48) { 
                "Allow button height ${allowHeightDp}dp < 48dp minimum" 
            }
            assert(allowWidthDp >= 88) { 
                "Allow button width ${allowWidthDp}dp < 88dp minimum (Material Design guideline)" 
            }
            
            assert(declineHeightDp >= 48) { 
                "Deny button height ${declineHeightDp}dp < 48dp minimum" 
            }
            assert(declineWidthDp >= 88) { 
                "Deny button width ${declineWidthDp}dp < 88dp minimum (Material Design guideline)" 
            }
        }
    }
    
    @Test
    fun permissionButtons_haveProperContentDescriptions() {
        // Validate AC 8: Content descriptions for TalkBack
        
        activityScenario.onActivity { activity ->
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_permission_rationale, null)
            
            val allowButton = dialogView.findViewById<View>(R.id.rationaleAllowButton)
            val declineButton = dialogView.findViewById<View>(R.id.rationaleDeclineButton)
            
            val allowDesc = allowButton.contentDescription
            val declineDesc = declineButton.contentDescription
            
            assert(allowDesc != null && allowDesc.isNotEmpty()) {
                "Allow button missing content description"
            }
            
            assert(declineDesc != null && declineDesc.isNotEmpty()) {
                "Decline button missing content description"
            }
            
            // Verify they match string resources
            assert(allowDesc == activity.getString(R.string.permission_allow_description)) {
                "Allow button content description doesn't match expected string"
            }
            
            assert(declineDesc == activity.getString(R.string.permission_deny_description)) {
                "Decline button content description doesn't match expected string"
            }
        }
    }
    
    @Test
    fun permissionDialog_hasFocusableTitle() {
        // Validate heading semantics for TalkBack navigation
        
        activityScenario.onActivity { activity ->
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_permission_rationale, null)
            
            val titleView = dialogView.findViewById<View>(R.id.rationaleTitle)
            
            assert(titleView.isAccessibilityHeading) { 
                "Title should be marked as accessibility heading" 
            }
            
            assert(titleView.contentDescription != null) {
                "Title should have content description"
            }
        }
    }
    
    @Test
    fun permissionDialog_hasAllRequiredElements() {
        // Validate dialog structure completeness
        
        activityScenario.onActivity { activity ->
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_permission_rationale, null)
            
            // Verify all required views exist
            val title = dialogView.findViewById<View>(R.id.rationaleTitle)
            val message = dialogView.findViewById<View>(R.id.rationaleMessage)
            val allowButton = dialogView.findViewById<View>(R.id.rationaleAllowButton)
            val declineButton = dialogView.findViewById<View>(R.id.rationaleDeclineButton)
            
            assert(title != null) { "Dialog missing title view" }
            assert(message != null) { "Dialog missing message view" }
            assert(allowButton != null) { "Dialog missing allow button" }
            assert(declineButton != null) { "Dialog missing decline button" }
        }
    }
}
