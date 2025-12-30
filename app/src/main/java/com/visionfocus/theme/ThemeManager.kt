package com.visionfocus.theme

import android.app.Activity
import android.content.Context
import com.visionfocus.R

/**
 * Singleton for managing theme application at runtime.
 * 
 * Handles theme switching between 4 variants:
 * 1. Theme.VisionFocus - Standard dark theme (default)
 * 2. Theme.VisionFocus.HighContrast - Pure black/white 7:1 contrast
 * 3. Theme.VisionFocus.LargeText - 150% text scaling
 * 4. Theme.VisionFocus.HighContrast.LargeText - Combined mode
 * 
 * Theme Application Lifecycle:
 * 1. Call applyTheme() from MainActivity.onCreate() BEFORE setContentView()
 * 2. Theme must be set before view inflation to prevent flicker
 * 3. activity.recreate() triggers full recreation to apply theme
 * 
 * Thread Safety:
 * - Object singleton pattern ensures single instance
 * - No mutable state - stateless design
 * - Safe to call from any thread (UI operations happen on main thread)
 */
object ThemeManager {
    
    /**
     * Applies appropriate theme based on preferences and recreates activity.
     * 
     * Theme Selection Logic:
     * - Both enabled → Theme.VisionFocus.HighContrast.LargeText
     * - High-contrast only → Theme.VisionFocus.HighContrast
     * - Large text only → Theme.VisionFocus.LargeText
     * - Neither enabled → Theme.VisionFocus (default)
     * 
     * @param context Activity context for theme application
     * @param highContrast True to enable 7:1 contrast ratio
     * @param largeText True to enable 150% text scaling
     */
    fun applyTheme(context: Context, highContrast: Boolean, largeText: Boolean) {
        val themeResId = when {
            highContrast && largeText -> R.style.Theme_VisionFocus_HighContrast_LargeText
            highContrast -> R.style.Theme_VisionFocus_HighContrast
            largeText -> R.style.Theme_VisionFocus_LargeText
            else -> R.style.Theme_VisionFocus
        }
        
        (context as? Activity)?.let { activity ->
            activity.setTheme(themeResId)
            activity.recreate()  // Trigger activity recreation to apply theme
        }
    }
    
    /**
     * Applies theme without recreating activity.
     * 
     * Used in MainActivity.onCreate() to set initial theme before views inflate.
     * Does NOT call activity.recreate() - caller must handle view inflation.
     * 
     * @param context Activity context for theme application
     * @param highContrast True to enable 7:1 contrast ratio
     * @param largeText True to enable 150% text scaling
     */
    fun setThemeWithoutRecreate(context: Context, highContrast: Boolean, largeText: Boolean) {
        val themeResId = when {
            highContrast && largeText -> R.style.Theme_VisionFocus_HighContrast_LargeText
            highContrast -> R.style.Theme_VisionFocus_HighContrast
            largeText -> R.style.Theme_VisionFocus_LargeText
            else -> R.style.Theme_VisionFocus
        }
        
        context.setTheme(themeResId)
    }
}
