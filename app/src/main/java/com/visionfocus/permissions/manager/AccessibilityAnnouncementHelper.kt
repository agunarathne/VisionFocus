package com.visionfocus.permissions.manager

import android.view.View
import android.view.accessibility.AccessibilityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for accessibility announcements to TalkBack users.
 * 
 * Used for:
 * - Permission grant/deny announcements (Story 1.5)
 * - Recognition result announcements (Story 2.2)
 * - Navigation instruction announcements (Story 6.3)
 * - Voice command confirmations (Story 3.3)
 */
@Singleton
class AccessibilityAnnouncementHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val accessibilityManager: AccessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    
    /**
     * Announce message to TalkBack if enabled.
     * 
     * @param view View to send announcement from
     * @param message Text to announce
     */
    fun announce(view: View, message: String) {
        if (accessibilityManager.isEnabled) {
            view.announceForAccessibility(message)
        }
    }
    
    /**
     * Check if TalkBack is currently enabled.
     * Used for conditional UI behavior in onboarding (Story 9.4).
     */
    fun isTalkBackEnabled(): Boolean {
        return accessibilityManager.isEnabled &&
                accessibilityManager.isTouchExplorationEnabled
    }
}
