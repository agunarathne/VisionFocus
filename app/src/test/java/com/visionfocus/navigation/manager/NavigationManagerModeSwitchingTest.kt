package com.visionfocus.navigation.manager

import android.content.Context
import com.visionfocus.data.repository.OfflineMapRepository
import com.visionfocus.maps.MapboxOfflineManager
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationMode
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.network.monitor.NetworkStateMonitor
import com.visionfocus.tts.engine.TTSManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Story 7.5 Task 14: Unit tests for NavigationManager mode switching logic.
 * 
 * Tests the routing strategy pattern and mode determination logic.
 */
class NavigationManagerModeSwitchingTest {
    
    private lateinit var navigationManager: NavigationManagerImpl
    private lateinit var context: Context
    private lateinit var ttsManager: TTSManager
    private lateinit var offlineMapRepository: OfflineMapRepository
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var mapboxOfflineManager: MapboxOfflineManager
    
    private val networkStateFlow = MutableStateFlow(true)
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        ttsManager = mockk(relaxed = true)
        offlineMapRepository = mockk(relaxed = true)
        networkStateMonitor = mockk(relaxed = true)
        mapboxOfflineManager = mockk(relaxed = true)
        
        // Mock network state
        every { networkStateMonitor.isNetworkAvailable } returns networkStateFlow
        
        navigationManager = NavigationManagerImpl(
            context,
            ttsManager,
            offlineMapRepository,
            networkStateMonitor,
            mapboxOfflineManager
        )
    }
    
    @Test
    fun `determineNavigationMode - online with offline maps returns ONLINE`() {
        // Given
        val isOnline = true
        val hasOfflineMaps = true
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.ONLINE, mode)
    }
    
    @Test
    fun `determineNavigationMode - online without offline maps returns ONLINE`() {
        // Given
        val isOnline = true
        val hasOfflineMaps = false
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.ONLINE, mode)
    }
    
    @Test
    fun `determineNavigationMode - offline with offline maps returns OFFLINE`() {
        // Given
        val isOnline = false
        val hasOfflineMaps = true
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.OFFLINE, mode)
    }
    
    @Test
    fun `determineNavigationMode - offline without offline maps returns UNAVAILABLE`() {
        // Given
        val isOnline = false
        val hasOfflineMaps = false
        
        // When
        val mode = navigationManager.determineNavigationMode(isOnline, hasOfflineMaps)
        
        // Then
        assertEquals(NavigationMode.UNAVAILABLE, mode)
    }
    
    @Test
    fun `startNavigation - sets mode to ONLINE when network available`() = runTest {
        // Given
        networkStateFlow.value = true
        coEvery { offlineMapRepository.isOfflineMapAvailable(any()) } returns false
        
        // When
        navigationManager.startNavigation(51.5, -0.1, "Test Location")
        
        // Then
        assertEquals(NavigationMode.ONLINE, navigationManager.navigationMode.value)
        coVerify { ttsManager.announce(any()) }
    }
    
    @Test
    fun `startNavigation - sets mode to OFFLINE when network unavailable but has offline maps`() = runTest {
        // Given
        networkStateFlow.value = false
        coEvery { offlineMapRepository.isOfflineMapAvailable(any()) } returns true
        
        // When
        navigationManager.startNavigation(51.5, -0.1, "Test Location")
        
        // Then
        assertEquals(NavigationMode.OFFLINE, navigationManager.navigationMode.value)
        coVerify { ttsManager.announce(any()) }
    }
    
    @Test
    fun `startNavigation - sets mode to UNAVAILABLE when network unavailable and no offline maps`() = runTest {
        // Given
        networkStateFlow.value = false
        coEvery { offlineMapRepository.isOfflineMapAvailable(any()) } returns false
        
        // When
        navigationManager.startNavigation(51.5, -0.1, "Test Location")
        
        // Then
        assertEquals(NavigationMode.UNAVAILABLE, navigationManager.navigationMode.value)
        coVerify { ttsManager.announce(any()) }
    }
    
    @Test
    fun `getOfflineRoute - delegates to MapboxOfflineManager`() = runTest {
        // Given
        val origin = LatLng(51.5, -0.1)
        val destination = LatLng(51.6, -0.2)
        val mockRoute = mockk<NavigationRoute>(relaxed = true)
        coEvery { mapboxOfflineManager.getOfflineRoute(origin, destination) } returns Result.success(mockRoute)
        
        // When
        val result = navigationManager.getOfflineRoute(origin, destination)
        
        // Then
        assert(result.isSuccess)
        assertEquals(mockRoute, result.getOrNull())
        coVerify { mapboxOfflineManager.getOfflineRoute(origin, destination) }
    }
    
    @Test
    fun `getOfflineRoute - returns failure when offline maps unavailable`() = runTest {
        // Given
        val origin = LatLng(51.5, -0.1)
        val destination = LatLng(51.6, -0.2)
        val error = IllegalStateException("No offline maps available")
        coEvery { mapboxOfflineManager.getOfflineRoute(origin, destination) } returns Result.failure(error)
        
        // When
        val result = navigationManager.getOfflineRoute(origin, destination)
        
        // Then
        assert(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
