package com.visionfocus.network.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.visionfocus.network.monitor.NetworkStateMonitor
import com.visionfocus.tts.engine.TTSManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for NetworkStatusViewModel.
 * 
 * Story 6.6: Network Availability Indication
 * Tests network status mapping, TTS announcements, and debouncing logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkStatusViewModelTest {
    
    // Rule to execute LiveData/StateFlow updates synchronously
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var ttsManager: TTSManager
    private lateinit var viewModel: NetworkStatusViewModel
    private lateinit var isNetworkAvailableFlow: MutableStateFlow<Boolean>
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        networkStateMonitor = mockk(relaxed = true)
        ttsManager = mockk(relaxed = true)
        
        // Create mutable flow to simulate network state changes
        isNetworkAvailableFlow = MutableStateFlow(true)  // Start online
        
        // Return flow when monitor is accessed
        every { networkStateMonitor.isNetworkAvailable } returns isNetworkAvailableFlow
        every { networkStateMonitor.getCurrentNetworkState() } returns true
        
        // CRITICAL #3 FIX: Remove coercion - let real code behavior be verified
        // Don't use 'every { ttsManager.announce(...) } just Runs'
        // Instead, use coEvery for suspend functions
        coEvery { ttsManager.announce(any()) } just Runs
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    /**
     * Test: networkStatus emits Online when network available.
     * AC #4: Online indicator announces "Online - live directions available"
     */
    @Test
    fun `networkStatus emits Online when network available`() = runTest {
        // Given: Network is online
        isNetworkAvailableFlow.value = true
        
        // When: ViewModel is created
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceUntilIdle()  // Wait for debounce
        
        // Then: networkStatus should be Online
        val status = viewModel.networkStatus.value
        assertTrue("Status should be Online", status is NetworkStatusViewModel.NetworkStatus.Online)
    }
    
    /**
     * Test: networkStatus emits Offline when network unavailable.
     * AC #5: Offline indicator announces "Offline"
     */
    @Test
    fun `networkStatus emits Offline when network unavailable`() = runTest {
        // Given: Network is offline
        isNetworkAvailableFlow.value = false
        every { networkStateMonitor.getCurrentNetworkState() } returns false
        
        // When: ViewModel is created
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceUntilIdle()  // Wait for debounce (2 seconds)
        
        // Then: networkStatus should be Offline
        val status = viewModel.networkStatus.value
        assertTrue("Status should be Offline", status is NetworkStatusViewModel.NetworkStatus.Offline)
    }
    
    /**
     * Test: Transition Online → Offline triggers TTS announcement.
     * AC #6: Transition announces "Lost internet connection..."
     * 
     * CRITICAL #3 FIX: Verify real behavior, not mock coercion
     */
    @Test
    fun `transition Online to Offline triggers TTS announcement`() = runTest {
        // Given: ViewModel starts online
        isNetworkAvailableFlow.value = true
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceUntilIdle()
        
        // When: Network goes offline
        isNetworkAvailableFlow.value = false
        advanceTimeBy(2100)  // Wait past 2-second debounce
        
        // Then: TTS should announce lost connection
        coVerify(atLeast = 1) {
            ttsManager.announce(
                match { it.contains("Lost internet connection", ignoreCase = true) }
            )
        }
    }
    
    /**
     * Test: Transition Offline → Online triggers TTS announcement.
     * AC #6: Transition announces "Internet connected..."
     * 
     * CRITICAL #3 FIX: Verify real behavior, not mock coercion
     */
    @Test
    fun `transition Offline to Online triggers TTS announcement`() = runTest {
        // Given: ViewModel starts offline
        isNetworkAvailableFlow.value = false
        every { networkStateMonitor.getCurrentNetworkState() } returns false
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceTimeBy(2100)  // Wait past debounce
        
        // When: Network comes online
        isNetworkAvailableFlow.value = true
        advanceTimeBy(2100)  // Wait past debounce
        
        // Then: TTS should announce connected
        coVerify(atLeast = 1) {
            ttsManager.announce(
                match { it.contains("Internet connected", ignoreCase = true) }
            )
        }
    }
    
    /**
     * Test: Initial state does NOT trigger announcement.
     * 
     * CRITICAL #3 FIX: Use coVerify for suspend function verification
     */
    @Test
    fun `initial state does not trigger announcement`() = runTest {
        // Given: Network is online
        isNetworkAvailableFlow.value = true
        
        // When: ViewModel is created
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceUntilIdle()
        
        // Then: TTS should NOT be called (initial state, no transition)
        coVerify(exactly = 0) {
            ttsManager.announce(any())
        }
    }
    
    /**
     * 
     * CRITICAL #3 FIX: Use coVerify for suspend function verification
     */
    @Test
    fun `rapid transitions within 2 seconds are debounced`() = runTest {
        // Given: ViewModel starts online
        isNetworkAvailableFlow.value = true
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceTimeBy(2100)
        
        // When: Network flickers (offline → online) within 1 second
        isNetworkAvailableFlow.value = false
        advanceTimeBy(500)  // Wait 0.5 seconds
        isNetworkAvailableFlow.value = true
        advanceTimeBy(1600)  // Total: 2.1 seconds after first transition
        
        // Then: Only ONE announcement (not two)
        coVerify(atMost = 1) {
            ttsManager.announce(any())
        }
    }
    
    /**
     * Test: Announcements use correct TTS API.
     * Story 6.6: TTSManager.announce() is a suspend function with single String parameter.
     * 
     * CRITICAL #3 FIX: Removed priority parameter (doesn't exist in actual API)
     */
    @Test
    fun `announcements call TTS manager correctly`() = runTest {
        // Given: ViewModel starts online
        isNetworkAvailableFlow.value = true
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        advanceTimeBy(2100)
        
        // When: Network goes offline
        isNetworkAvailableFlow.value = false
        advanceTimeBy(2100)
        
        // Then: TTS should be called with correct API signature
        coVerify {
            ttsManager.announce(any())  // Suspend function, no priority parameter
        }
    }
    
    /**
     * Test: debounce() operator delays emissions by 2 seconds.
     * Ensures rapid flickers don't trigger multiple announcements.
     */
    @Test
    fun `debounce delays state emissions by 2 seconds`() = runTest {
        // Given: ViewModel starts online
        isNetworkAvailableFlow.value = true
        viewModel = NetworkStatusViewModel(networkStateMonitor, ttsManager)
        
        // When: Network goes offline
        isNetworkAvailableFlow.value = false
        
        // Then: After 1.5 seconds, state should still be Online (debouncing)
        advanceTimeBy(1500)
        val statusBefore = viewModel.networkStatus.value
        assertTrue("Status should still be Online (debouncing)", statusBefore is NetworkStatusViewModel.NetworkStatus.Online)
        
        // After full 2 seconds, state should be Offline
        advanceTimeBy(600)  // Total: 2.1 seconds
        val statusAfter = viewModel.networkStatus.value
        assertTrue("Status should now be Offline", statusAfter is NetworkStatusViewModel.NetworkStatus.Offline)
    }
}
