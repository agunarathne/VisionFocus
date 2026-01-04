package com.visionfocus.permissions.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for PermissionSettingsLauncher utility.
 * Story 6.5: GPS Location Permissions with Clear Explanations
 * 
 * Tests system settings intent creation and error handling for
 * opening app permission settings.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PermissionSettingsLauncherTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    private val testPackageName = "com.visionfocus"
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.packageName).thenReturn(testPackageName)
    }
    
    /**
     * Story 6.5 Task 12.3: Test openAppSettings() creates correct intent.
     */
    @Test
    fun `openAppSettings creates intent with correct action`() {
        // When opening app settings
        PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then should start activity with Settings intent
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockContext).startActivity(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
        
        // Story 6.5 Task 12.4: Verify action
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, capturedIntent.action)
    }
    
    /**
     * Story 6.5 Task 12.5: Test intent data contains correct package name.
     */
    @Test
    fun `openAppSettings creates intent with correct package URI`() {
        // When opening app settings
        PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then intent data should contain package name
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockContext).startActivity(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
        val expectedUri = Uri.fromParts("package", testPackageName, null)
        
        assertEquals(expectedUri, capturedIntent.data)
    }
    
    /**
     * Story 6.5 Task 12.6: Test FLAG_ACTIVITY_NEW_TASK is set.
     */
    @Test
    fun `openAppSettings sets FLAG_ACTIVITY_NEW_TASK flag`() {
        // When opening app settings
        PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then intent should have NEW_TASK flag
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockContext).startActivity(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
        val hasNewTaskFlag = (capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0
        
        assertTrue("Intent should have FLAG_ACTIVITY_NEW_TASK", hasNewTaskFlag)
    }
    
    /**
     * Story 6.5 Task 12.7: Test error handling when settings intent unavailable.
     */
    @Test
    fun `openAppSettings returns false when ActivityNotFoundException thrown`() {
        // Given settings intent is unavailable
        `when`(mockContext.startActivity(any(Intent::class.java)))
            .thenThrow(ActivityNotFoundException::class.java)
        
        // When opening app settings
        val result = PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then should return false
        // Story 6.5 Task 12.8: Boolean return indicates failure
        assertFalse(result)
    }
    
    /**
     * Story 6.5 Task 12.8: Test Boolean return value indicates success.
     */
    @Test
    fun `openAppSettings returns true when intent launches successfully`() {
        // Given settings intent launches successfully
        doNothing().`when`(mockContext).startActivity(any(Intent::class.java))
        
        // When opening app settings
        val result = PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then should return true
        assertTrue(result)
    }
    
    /**
     * Verify intent is configured correctly for all required properties.
     */
    @Test
    fun `openAppSettings creates fully configured intent`() {
        // When opening app settings
        PermissionSettingsLauncher.openAppSettings(mockContext)
        
        // Then intent should have all required properties
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockContext).startActivity(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
        
        // Verify action
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, capturedIntent.action)
        
        // Verify data URI
        assertNotNull(capturedIntent.data)
        assertEquals("package", capturedIntent.data?.scheme)
        assertEquals(testPackageName, capturedIntent.data?.schemeSpecificPart)
        
        // Verify flags
        val hasNewTaskFlag = (capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0
        assertTrue(hasNewTaskFlag)
    }
}
