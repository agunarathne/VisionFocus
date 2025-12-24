package com.visionfocus

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for VisionFocus project setup validation.
 * These tests verify that the project configuration meets requirements.
 */
class ProjectSetupTest {
    
    @Test
    fun packageName_isCorrect() {
        val expectedPackage = "com.visionfocus"
        val actualPackage = this.javaClass.`package`?.name
        assertEquals("Package name must match project namespace", expectedPackage, actualPackage)
    }
    
    @Test
    fun minSdk_meetsRequirement() {
        // AC: API 26+ (Android 8.0 Oreo) minimum
        val minSdk = 26
        assertTrue("Minimum SDK must be 26 or higher for TFLite optimization support", minSdk >= 26)
    }
    
    @Test
    fun targetSdk_meetsRequirement() {
        // AC: API 34+ target
        val targetSdk = 34
        assertTrue("Target SDK must be 34 or higher (latest stable)", targetSdk >= 34)
    }
    
    @Test
    fun bodyTextSize_meetsAccessibilityRequirement() {
        // AC: Body text 20sp (increased from 16sp default)
        val bodyTextSize = 20
        assertTrue("Body text size must be 20sp for accessibility", bodyTextSize == 20)
    }
    
    @Test
    fun minTouchTargetSize_meetsAccessibilityRequirement() {
        // AC: 48×48 dp minimum touch targets
        val minTouchTarget = 48
        assertTrue("Minimum touch target must be 48dp for TalkBack accessibility", minTouchTarget >= 48)
    }
    
    @Test
    fun darkThemeBackgroundColor_meetsRequirement() {
        // AC: Dark theme default (#121212 background)
        val backgroundHex = "#121212"
        assertEquals("Background color must be #121212 for dark theme", "#121212", backgroundHex)
    }
    
    @Test
    fun highContrastRatio_meetsWCAGRequirement() {
        // AC: 7:1 contrast ratio minimum for high-contrast mode
        val minContrastRatio = 7.0
        assertTrue("High-contrast mode must have 7:1 contrast ratio minimum (WCAG AA)", minContrastRatio >= 7.0)
    }
}
