package com.visionfocus.ui.recognition

import android.graphics.Bitmap
import com.visionfocus.recognition.models.DetectionResult
import com.visionfocus.recognition.models.RecognitionResult
import com.visionfocus.recognition.processing.ConfidenceFilter
import com.visionfocus.recognition.processing.ConfidenceLevel
import com.visionfocus.recognition.processing.FilteredDetection
import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.formatter.TTSPhraseFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for RecognitionViewModel
 * 
 * Story 2.3 Task 8: Test ViewModel state transitions and error handling
 * Story 2.4 Task 10: Test camera lifecycle integration
 * 
 * Test coverage:
 * - Task 8.2: State transitions (Idle → Recognizing → Announcing → Success)
 * - Task 8.3: Error handling (Idle → Recognizing → Error)
 * - Story 2.4: Camera lifecycle states (Idle → Capturing → Recognizing → Success)
 * - Story 2.4: Cancellation support (cancelRecognition)
 * - Story 2.4: Camera error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionViewModelTest {
    
    // Mocked dependencies
    private lateinit var mockRepository: RecognitionRepository
    private lateinit var mockConfidenceFilter: ConfidenceFilter
    private lateinit var mockTtsManager: TTSManager
    private lateinit var mockTtsFormatter: TTSPhraseFormatter
    
    // System under test
    private lateinit var viewModel: RecognitionViewModel
    
    // Test coroutine dispatcher
    private val testDispatcher = StandardTestDispatcher()
    
    /**
     * Helper to create test bounding box
     * BoundingBox is a data class so we use real instances, not mocks
     */
    private fun createTestBoundingBox() = com.visionfocus.recognition.models.BoundingBox(
        yMin = 0.1f,
        xMin = 0.1f,
        yMax = 0.9f,
        xMax = 0.9f
    )
    
    @Before
    fun setup() {
        // Task 8.4: Create mocks
        mockRepository = mock()
        mockConfidenceFilter = mock()
        mockTtsManager = mock()
        mockTtsFormatter = mock()
        val mockHapticFeedbackManager = mock<com.visionfocus.accessibility.haptic.HapticFeedbackManager>()
        val mockOperationManager = mock<com.visionfocus.voice.operation.OperationManager>()
        
        // Set test dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
        
        // Create ViewModel with mocked dependencies
        viewModel = RecognitionViewModel(
            recognitionRepository = mockRepository,
            confidenceFilter = mockConfidenceFilter,
            ttsManager = mockTtsManager,
            ttsFormatter = mockTtsFormatter,
            hapticFeedbackManager = mockHapticFeedbackManager,
            operationManager = mockOperationManager
        )
    }
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Task 8.2: Test successful recognition state transitions
     * 
     * Expected flow: Idle → Capturing (Story 2.4)
     * Story 2.4: recognizeObject() only transitions to Capturing, performRecognition(bitmap) does the work
     */
    @Test
    fun `recognizeObject transitions through expected states on success`() = runTest {
        // When: Trigger recognition
        viewModel.recognizeObject()
        
        // Then: Should transition to Capturing state (Story 2.4 architecture)
        val state = viewModel.uiState.value
        assertTrue("Should be in Capturing state", state is RecognitionUiState.Capturing)
    }
    
    /**
     * Task 8.3: Test error handling state transitions
     * 
     * Expected flow: Idle → Capturing → Recognizing → Error → Idle (Story 2.4)
     */
    @Test
    fun `recognizeObject handles SecurityException and transitions to Error state`() = runTest {
        // Given: Mock SecurityException (camera permission denied)
        val mockBitmap = mock<Bitmap>()
        whenever(mockRepository.performRecognition(mockBitmap)).thenThrow(
            SecurityException("Camera permission denied")
        )
        
        // When: Trigger recognition with bitmap (not recognizeObject - that only goes to Capturing)
        viewModel.performRecognition(mockBitmap)
        
        // Advance past ERROR_DELAY_MS (3000ms)
        testScheduler.advanceTimeBy(3500)
        advanceUntilIdle()
        
        // Then: Should be in Idle state after error recovery
        val currentState = viewModel.uiState.first()
        assertTrue("Should be in Idle state after error recovery", 
            currentState is RecognitionUiState.Idle)
    }
    
    /**
     * Task 8.5: Verify repository.performRecognition() is called (Story 2.4 - with bitmap)
     */
    @Test
    fun `recognizeObject calls repository performRecognition`() = runTest {
        // Given: Mock successful result
        val mockBitmap = mock<Bitmap>()
        val mockDetections = listOf(DetectionResult("object", 0.7f, createTestBoundingBox()))
        val mockResult = RecognitionResult(
            detections = mockDetections,
            timestampMs = System.currentTimeMillis(),
            latencyMs = 200
        )
        val mockFilteredDetection = FilteredDetection("object", 0.7f, ConfidenceLevel.MEDIUM, createTestBoundingBox())
        
        whenever(mockRepository.performRecognition(mockBitmap)).thenReturn(mockResult)
        whenever(mockConfidenceFilter.toFilteredDetection(any())).thenReturn(mockFilteredDetection)
        whenever(mockTtsFormatter.formatMultipleDetections(any())).thenReturn("No objects detected")
        whenever(mockTtsManager.announce(any())).thenReturn(Result.success(100L))
        
        // When: Trigger recognition with bitmap
        viewModel.performRecognition(mockBitmap)
        advanceUntilIdle()
        
        // Then: Verify repository method was called
        verify(mockRepository, times(1)).performRecognition(mockBitmap)
    }
    
    /**
     * Task 8.6: Verify TTS announcement triggered with formatted string (Story 2.4 - with bitmap)
     */
    @Test
    fun `recognizeObject triggers TTS announcement with formatted string`() = runTest {
        // Given: Mock recognition result with detections
        val mockBitmap = mock<Bitmap>()
        val mockDetections = listOf(
            DetectionResult("bottle", 0.75f, createTestBoundingBox())
        )
        val mockFilteredDetection = FilteredDetection("bottle", 0.75f, ConfidenceLevel.MEDIUM, createTestBoundingBox())
        val mockResult = RecognitionResult(
            detections = mockDetections,
            timestampMs = System.currentTimeMillis(),
            latencyMs = 300
        )
        val expectedAnnouncement = "Possibly a bottle"
        
        whenever(mockRepository.performRecognition(mockBitmap)).thenReturn(mockResult)
        whenever(mockConfidenceFilter.toFilteredDetection(any())).thenReturn(mockFilteredDetection)
        whenever(mockTtsFormatter.formatMultipleDetections(listOf(mockFilteredDetection)))
            .thenReturn(expectedAnnouncement)
        whenever(mockTtsManager.announce(any())).thenReturn(Result.success(150L))
        
        // When: Trigger recognition with bitmap
        viewModel.performRecognition(mockBitmap)
        advanceUntilIdle()
        
        // Then: Verify TTS manager was called with formatted announcement
        verify(mockTtsManager, times(1)).announce(expectedAnnouncement)
    }
    
    /**
     * Test: Verify formatter is called with detection results (Story 2.4 - with bitmap)
     */
    @Test
    fun `recognizeObject calls formatter with detection results`() = runTest {
        // Given: Mock detection results
        val mockBitmap = mock<Bitmap>()
        val mockDetections = listOf(
            DetectionResult("cup", 0.90f, createTestBoundingBox())
        )
        val mockFilteredDetection = FilteredDetection("cup", 0.90f, ConfidenceLevel.HIGH, createTestBoundingBox())
        val mockResult = RecognitionResult(
            detections = mockDetections,
            timestampMs = System.currentTimeMillis(),
            latencyMs = 280
        )
        
        whenever(mockRepository.performRecognition(mockBitmap)).thenReturn(mockResult)
        whenever(mockConfidenceFilter.toFilteredDetection(any())).thenReturn(mockFilteredDetection)
        whenever(mockTtsFormatter.formatMultipleDetections(any())).thenReturn("I see a cup")
        whenever(mockTtsManager.announce(any())).thenReturn(Result.success(120L))
        
        // When: Trigger recognition with bitmap
        viewModel.performRecognition(mockBitmap)
        advanceUntilIdle()
        
        // Then: Verify formatter was called with filtered detection list
        verify(mockTtsFormatter, times(1)).formatMultipleDetections(listOf(mockFilteredDetection))
    }
    
    /**
     * Test: Multiple rapid calls should be ignored (prevents race conditions)
     * Story 2.4: Debouncing works on Capturing state
     */
    @Test
    fun `recognizeObject ignores rapid calls while processing`() = runTest {
        // When: Trigger recognition multiple times rapidly
        viewModel.recognizeObject()
        val firstState = viewModel.uiState.value
        assertTrue("First call transitions to Capturing", firstState is RecognitionUiState.Capturing)
        
        viewModel.recognizeObject() // Should be ignored - state not Idle
        viewModel.recognizeObject() // Should be ignored - state not Idle
        
        val finalState = viewModel.uiState.value
        
        // Then: Should remain in Capturing state (other calls ignored)
        assertTrue("State stays in Capturing (debounced)", finalState is RecognitionUiState.Capturing)
    }
    
    /**
     * Test: IllegalStateException handling (TTS not initialized) - Story 2.4 with bitmap
     */
    @Test
    fun `recognizeObject handles IllegalStateException gracefully`() = runTest {
        // Given: Mock IllegalStateException
        val mockBitmap = mock<Bitmap>()
        whenever(mockRepository.performRecognition(mockBitmap)).thenThrow(
            IllegalStateException("TTS not initialized")
        )
        
        // When: Trigger recognition with bitmap (not recognizeObject - that only goes to Capturing)
        viewModel.performRecognition(mockBitmap)
        
        // Advance past ERROR_DELAY_MS (3000ms)
        testScheduler.advanceTimeBy(3500)
        advanceUntilIdle()
        
        // Then: Should reach Idle state after error recovery
        val currentState = viewModel.uiState.value
        assertTrue("Should recover to Idle state (was ${currentState::class.simpleName})", currentState is RecognitionUiState.Idle)
    }
    
    /**
     * Story 2.3 Task 8.10: Test generic exception handling (Story 2.4 - with bitmap)
     */
    @Test
    fun `recognizeObject handles generic exceptions gracefully`() = runTest {
        // Given: Mock generic exception
        val mockBitmap = mock<Bitmap>()
        whenever(mockRepository.performRecognition(mockBitmap)).thenThrow(
            RuntimeException("Unexpected error")
        )
        
        // When: Trigger recognition with bitmap
        viewModel.performRecognition(mockBitmap)
        
        // Advance past ERROR_DELAY_MS (3000ms) to reach Idle state
        testScheduler.advanceTimeBy(3500)
        advanceUntilIdle()
        
        // Then: Should recover to Idle state after error delay
        val currentState = viewModel.uiState.value
        assertTrue("Should recover to Idle state (was ${currentState::class.simpleName})", currentState is RecognitionUiState.Idle)
    }
    
    // ==================== Story 2.4 Tests ====================
    
    /**
     * Story 2.4 Task 10.1: Test state transitions with camera capture
     * 
     * Expected flow: Idle → Capturing → Recognizing → Success
     */
    @Test
    fun `recognizeObject transitions to Capturing state`() = runTest {
        // When: Trigger recognition
        viewModel.recognizeObject()
        
        // Then: Should transition to Capturing state
        val state = viewModel.uiState.value
        assertTrue("Should be in Capturing state", state is RecognitionUiState.Capturing)
    }
    
    /**
     * Story 2.4 Task 10.1: Test performRecognition with Bitmap
     */
    @Test
    fun `performRecognition with bitmap transitions through states correctly`() = runTest {
        // Given: Mock bitmap and successful recognition
        val mockBitmap = mock<Bitmap>()
        val mockDetections = listOf(
            DetectionResult("table", 0.88f, createTestBoundingBox())
        )
        val mockFilteredDetection = FilteredDetection("table", 0.88f, ConfidenceLevel.HIGH, createTestBoundingBox())
        val mockResult = RecognitionResult(
            detections = mockDetections,
            timestampMs = System.currentTimeMillis(),
            latencyMs = 320
        )
        val mockAnnouncement = "I see a table"
        
        whenever(mockRepository.performRecognition(mockBitmap)).thenReturn(mockResult)
        whenever(mockConfidenceFilter.toFilteredDetection(any())).thenReturn(mockFilteredDetection)
        whenever(mockTtsFormatter.formatMultipleDetections(any())).thenReturn(mockAnnouncement)
        whenever(mockTtsManager.announce(any())).thenReturn(Result.success(150L))
        
        // When: Perform recognition with bitmap
        viewModel.performRecognition(mockBitmap)
        advanceUntilIdle()
        
        // Then: Verify repository called with bitmap
        verify(mockRepository, times(1)).performRecognition(mockBitmap)
        verify(mockTtsManager, times(1)).announce(mockAnnouncement)
        
        // Verify final state returned to Idle
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Idle", finalState is RecognitionUiState.Idle)
    }
    
    /**
     * Story 2.4 Task 10.2: Test cancelRecognition transitions to Idle
     */
    @Test
    fun `cancelRecognition transitions to Idle state immediately`() = runTest {
        // Given: Start recognition
        viewModel.recognizeObject()
        
        // When: Cancel recognition
        viewModel.cancelRecognition()
        
        // Then: Should be in Idle state
        val state = viewModel.uiState.value
        assertTrue("Should be in Idle state after cancellation", state is RecognitionUiState.Idle)
    }
    
    /**
     * Story 2.4 Task 10.3: Test camera error handling
     */
    @Test
    fun `onCameraError transitions to CameraError state`() = runTest {
        // When: Report camera error
        viewModel.onCameraError("Camera unavailable")
        advanceTimeBy(100) // Allow coroutine to start
        
        // Then: Should be in CameraError state
        val state = viewModel.uiState.value
        assertTrue("Should be in CameraError state", state is RecognitionUiState.CameraError)
        assertEquals("Camera unavailable", (state as RecognitionUiState.CameraError).message)
    }
    
    /**
     * Story 2.4: Test performRecognition with empty results
     */
    @Test
    fun `performRecognition with empty results transitions to Error state`() = runTest {
        // Given: Mock empty detection results
        val mockBitmap = mock<Bitmap>()
        val mockResult = RecognitionResult(
            detections = emptyList(),
            timestampMs = System.currentTimeMillis(),
            latencyMs = 300
        )
        
        whenever(mockRepository.performRecognition(mockBitmap)).thenReturn(mockResult)
        
        // When: Perform recognition
        viewModel.performRecognition(mockBitmap)
        advanceUntilIdle()
        
        // Then: Should be in Idle state after error recovery
        val finalState = viewModel.uiState.value
        assertTrue("Should recover to Idle after empty results", finalState is RecognitionUiState.Idle)
    }
    
    /**
     * Story 2.4: Test onCameraReady (no-op, but verifies it doesn't crash)
     */
    @Test
    fun `onCameraReady completes successfully`() {
        // When: Notify camera ready
        viewModel.onCameraReady()
        
        // Then: Should remain in Idle state
        val state = viewModel.uiState.value
        assertTrue("Should remain in Idle state", state is RecognitionUiState.Idle)
    }
}


