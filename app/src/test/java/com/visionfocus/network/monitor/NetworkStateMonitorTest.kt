package com.visionfocus.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NetworkStateMonitor.
 * 
 * Story 6.6: Network Availability Indication
 * Tests network state monitoring, StateFlow emission, and callback registration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkStateMonitorTest {
    
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private var capturedCallback: ConnectivityManager.NetworkCallback? = null
    
    @Before
    fun setup() {
        // Mock Android framework dependencies
        context = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        network = mockk(relaxed = true)
        networkCapabilities = mockk(relaxed = true)
        
        // Setup context to return mocked ConnectivityManager
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        
        // Capture network callback when registered
        every { 
            connectivityManager.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>())
        } answers {
            capturedCallback = secondArg()
            mockk(relaxed = true)
        }
        
        // Default: Network available with internet capability
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
    
    @After
    fun tearDown() {
        // Clean up monitor
        if (::networkStateMonitor.isInitialized) {
            networkStateMonitor.unregister()
        }
        unmockkAll()
    }
    
    /**
     * Test: NetworkStateMonitor initializes with correct network state.
     * AC: Initial state reflects getCurrentNetworkState()
     */
    @Test
    fun `initializes with current network state online`() = runTest {
        // Given: Network is available
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        
        // When: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Then: Initial state should be true (online)
        val initialState = networkStateMonitor.isNetworkAvailable.value
        assertTrue("Initial state should be true (online)", initialState)
    }
    
    /**
     * Test: NetworkStateMonitor initializes offline when no network.
     * AC: Initial state reflects getCurrentNetworkState()
     */
    @Test
    fun `initializes with current network state offline`() = runTest {
        // Given: No network available
        every { connectivityManager.activeNetwork } returns null
        
        // When: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Then: Initial state should be false (offline)
        val initialState = networkStateMonitor.isNetworkAvailable.value
        assertFalse("Initial state should be false (offline)", initialState)
    }
    
    /**
     * Test: onAvailable() callback emits true to StateFlow.
     * AC: Network becomes available → StateFlow emits true
     */
    @Test
    fun `onAvailable callback emits true`() = runTest {
        // Given: Monitor starts offline
        every { connectivityManager.activeNetwork } returns null
        networkStateMonitor = NetworkStateMonitor(context)
        assertFalse("Should start offline", networkStateMonitor.isNetworkAvailable.value)
        
        // When: Network becomes available
        capturedCallback?.onAvailable(network)
        
        // Then: StateFlow should emit true
        val currentState = networkStateMonitor.isNetworkAvailable.value
        assertTrue("State should be true after onAvailable()", currentState)
    }
    
    /**
     * Test: onLost() callback emits false to StateFlow.
     * AC: Network is lost → StateFlow emits false
     */
    @Test
    fun `onLost callback emits false`() = runTest {
        // Given: Monitor starts online
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        networkStateMonitor = NetworkStateMonitor(context)
        assertTrue("Should start online", networkStateMonitor.isNetworkAvailable.value)
        
        // When: Network is lost
        capturedCallback?.onLost(network)
        
        // Then: StateFlow should emit false
        val currentState = networkStateMonitor.isNetworkAvailable.value
        assertFalse("State should be false after onLost()", currentState)
    }
    
    /**
     * Test: getCurrentNetworkState() returns true when network available.
     * AC: Immediate check returns correct state
     */
    @Test
    fun `getCurrentNetworkState returns true when online`() {
        // Given: Network available with internet capability
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        
        // When: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Then: getCurrentNetworkState() should return true
        val currentState = networkStateMonitor.getCurrentNetworkState()
        assertTrue("getCurrentNetworkState() should return true", currentState)
    }
    
    /**
     * Test: getCurrentNetworkState() returns false when network unavailable.
     * AC: Immediate check returns correct state
     */
    @Test
    fun `getCurrentNetworkState returns false when offline`() {
        // Given: No network available
        every { connectivityManager.activeNetwork } returns null
        
        // When: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Then: getCurrentNetworkState() should return false
        val currentState = networkStateMonitor.getCurrentNetworkState()
        assertFalse("getCurrentNetworkState() should return false", currentState)
    }
    
    /**
     * Test: getCurrentNetworkState() returns false when network lacks internet capability.
     * AC: Connected but no internet → returns false (avoid false positives)
     */
    @Test
    fun `getCurrentNetworkState returns false when connected but no internet`() {
        // Given: Network connected but no internet capability
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        
        // When: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Then: getCurrentNetworkState() should return false
        val currentState = networkStateMonitor.getCurrentNetworkState()
        assertFalse("getCurrentNetworkState() should return false when no internet", currentState)
    }
    
    /**
     * Test: unregister() cleans up network callback without exceptions.
     * AC: No memory leaks from callback registration
     */
    @Test
    fun `unregister cleans up callback without exceptions`() {
        // Given: Monitor is created and callback registered
        networkStateMonitor = NetworkStateMonitor(context)
        assertNotNull("Callback should be captured", capturedCallback)
        
        // When: unregister() is called
        networkStateMonitor.unregister()
        
        // Then: Should call unregisterNetworkCallback
        verify(exactly = 1) { connectivityManager.unregisterNetworkCallback(capturedCallback!!) }
    }
    
    /**
     * Test: unregister() handles IllegalArgumentException gracefully.
     * AC: Double unregister doesn't crash
     */
    @Test
    fun `unregister handles double unregister gracefully`() {
        // Given: Monitor is created
        networkStateMonitor = NetworkStateMonitor(context)
        
        // Simulate Android throwing IllegalArgumentException on double unregister
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } throws
                IllegalArgumentException("Callback not registered")
        
        // When: unregister() is called twice
        networkStateMonitor.unregister()
        networkStateMonitor.unregister()
        
        // Then: Should not throw exception (graceful handling)
        // Test passes if no exception thrown
    }
}
