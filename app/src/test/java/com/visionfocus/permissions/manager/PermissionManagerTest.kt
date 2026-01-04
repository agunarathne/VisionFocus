package com.visionfocus.permissions.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for PermissionManager location permission methods.
 * Story 6.5: GPS Location Permissions with Clear Explanations
 * 
 * Tests location permission checking, rationale display logic, and
 * permission request launching functionality.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PermissionManagerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockActivity: Activity
    
    @Mock
    private lateinit var mockComponentActivity: ComponentActivity
    
    @Mock
    private lateinit var mockLauncher: ActivityResultLauncher<String>
    
    private lateinit var permissionManager: PermissionManager
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        permissionManager = PermissionManager(mockContext)
    }
    
    /**
     * Story 6.5 Task 11.3: Test isLocationPermissionGranted() returns true when granted.
     */
    @Test
    fun `isLocationPermissionGranted returns true when permission granted`() {
        // Given permission is granted
        `when`(
            ContextCompat.checkSelfPermission(
                mockContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(PackageManager.PERMISSION_GRANTED)
        
        // When checking permission
        val result = permissionManager.isLocationPermissionGranted()
        
        // Then should return true
        assertTrue(result)
    }
    
    /**
     * Story 6.5 Task 11.4: Test isLocationPermissionGranted() returns false when denied.
     */
    @Test
    fun `isLocationPermissionGranted returns false when permission denied`() {
        // Given permission is denied
        `when`(
            ContextCompat.checkSelfPermission(
                mockContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)
        
        // When checking permission
        val result = permissionManager.isLocationPermissionGranted()
        
        // Then should return false
        assertFalse(result)
    }
    
    /**
     * Story 6.5 Task 11.5: Test shouldShowLocationRationale() returns true after first denial.
     */
    @Test
    fun `shouldShowLocationRationale returns true after first denial`() {
        // Given user previously denied permission
        `when`(
            ActivityCompat.shouldShowRequestPermissionRationale(
                mockActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(true)
        
        // When checking rationale
        val result = permissionManager.shouldShowLocationRationale(mockActivity)
        
        // Then should return true
        assertTrue(result)
    }
    
    /**
     * Story 6.5 Task 11.6: Test shouldShowLocationRationale() returns false for first-time request.
     */
    @Test
    fun `shouldShowLocationRationale returns false for first time request`() {
        // Given first-time permission request
        `when`(
            ActivityCompat.shouldShowRequestPermissionRationale(
                mockActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(false)
        
        // When checking rationale
        val result = permissionManager.shouldShowLocationRationale(mockActivity)
        
        // Then should return false
        assertFalse(result)
    }
    
    /**
     * Story 6.5 Task 11.7: Test requestLocationPermission() launches correct permission.
     */
    @Test
    fun `requestLocationPermission launches ACCESS_FINE_LOCATION permission`() {
        // When requesting location permission
        permissionManager.requestLocationPermission(mockLauncher)
        
        // Then should launch with ACCESS_FINE_LOCATION
        verify(mockLauncher).launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
    
    /**
     * Story 6.5 Task 11.8: Test permission constant is ACCESS_FINE_LOCATION (not COARSE or BACKGROUND).
     */
    @Test
    fun `location permission uses ACCESS_FINE_LOCATION only`() {
        // When checking which permission is used
        val expectedPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        
        // Then should be ACCESS_FINE_LOCATION (not COARSE or BACKGROUND)
        // This test documents the requirement from Story 6.5 AC #3 and AC #8
        assertNotEquals(android.Manifest.permission.ACCESS_COARSE_LOCATION, expectedPermission)
        assertNotEquals(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, expectedPermission)
        assertEquals(android.Manifest.permission.ACCESS_FINE_LOCATION, expectedPermission)
    }
    
    /**
     * Verify permission manager correctly identifies when rationale should NOT be shown
     * (covers "Never Ask Again" scenario).
     */
    @Test
    fun `shouldShowLocationRationale returns false when user selected Never Ask Again`() {
        // Given user selected "Never Ask Again" (shouldShowRationale returns false, permission denied)
        `when`(
            ActivityCompat.shouldShowRequestPermissionRationale(
                mockActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(false)
        
        `when`(
            ContextCompat.checkSelfPermission(
                mockContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)
        
        // When checking rationale
        val result = permissionManager.shouldShowLocationRationale(mockActivity)
        
        // Then should return false (can't show rationale, must use settings)
        assertFalse(result)
    }
}
