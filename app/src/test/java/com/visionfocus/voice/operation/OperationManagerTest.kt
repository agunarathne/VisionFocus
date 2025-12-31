package com.visionfocus.voice.operation

import android.content.Context
import com.visionfocus.R
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

/**
 * Unit tests for OperationManager
 * Story 3.3 Task 8: Comprehensive operation lifecycle and cancellation tests
 * 
 * Test Coverage:
 * - Task 8.1: Operation tracking (start/complete/cancel)
 * - Task 8.2: Cancel active recognition operation
 * - Task 8.3: Cancel with no active operation
 * - Task 8.6: Haptic feedback triggers on cancellation
 * - Task 8.7: Operation lifecycle validation
 * 
 * @since Story 3.3
 */
class OperationManagerTest {
    
    private lateinit var operationManager: OperationManager
    private lateinit var mockContext: Context
    private lateinit var mockTTSManager: TTSManager
    private lateinit var mockHapticManager: HapticFeedbackManager
    
    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockTTSManager = mock(TTSManager::class.java)
        mockHapticManager = mock(HapticFeedbackManager::class.java)
        
        // Mock string resources
        `when`(mockContext.getString(R.string.voice_cancelled)).thenReturn("Cancelled")
        `when`(mockContext.getString(R.string.voice_nothing_to_cancel)).thenReturn("Nothing to cancel")
        `when`(mockContext.getString(R.string.voice_cancellation_failed)).thenReturn("Cancellation failed. Please try again.")
        
        operationManager = OperationManager(mockContext, mockTTSManager, mockHapticManager)
    }
    
    /**
     * Task 8.1: Test operation tracking - startOperation registers active operation
     */
    @Test
    fun `startOperation registers active operation`() = runTest {
        // Given: No active operation
        assertTrue("Initial state should be None", operationManager.activeOperation.value is Operation.None)
        
        // When: Start recognition operation
        val recognitionOp = Operation.RecognitionOperation(onCancel = {})
        operationManager.startOperation(recognitionOp)
        
        // Then: Operation is registered
        val activeOp = operationManager.activeOperation.first()
        assertTrue("Operation should be RecognitionOperation", activeOp is Operation.RecognitionOperation)
    }
    
    /**
     * Task 8.1: Test operation tracking - completeOperation clears active operation
     */
    @Test
    fun `completeOperation clears active operation`() = runTest {
        // Given: Active recognition operation
        val recognitionOp = Operation.RecognitionOperation(onCancel = {})
        operationManager.startOperation(recognitionOp)
        assertTrue(operationManager.activeOperation.value is Operation.RecognitionOperation)
        
        // When: Complete operation
        operationManager.completeOperation()
        
        // Then: Operation is cleared
        assertTrue("Operation should be None after completion", operationManager.activeOperation.value is Operation.None)
    }
    
    /**
     * Task 8.2: Test cancelOperation stops active recognition
     */
    @Test
    fun `cancelOperation stops active recognition and announces cancelled`() = runTest {
        // Given: Active recognition operation with cancellation callback
        var cancelled = false
        val recognitionOp = Operation.RecognitionOperation(onCancel = { cancelled = true })
        operationManager.startOperation(recognitionOp)
        
        // When: Cancel operation
        val result = operationManager.cancelOperation()
        
        // Then: Callback invoked
        assertTrue("onCancel callback should be invoked", cancelled)
        
        // Then: Operation cleared
        assertTrue("Operation should be None after cancellation", operationManager.activeOperation.value is Operation.None)
        
        // Then: TTS announces "Cancelled"
        val messageCaptor = argumentCaptor<String>()
        verify(mockTTSManager).announce(messageCaptor.capture())
        assertEquals("Cancelled", messageCaptor.firstValue)
        
        // Then: Result is success
        assertTrue("Result should be Success", result is CommandResult.Success)
    }
    
    /**
     * Task 8.6: Test haptic feedback triggers on cancellation
     */
    @Test
    fun `cancelOperation triggers Cancelled haptic pattern`() = runTest {
        // Given: Active recognition operation
        val recognitionOp = Operation.RecognitionOperation(onCancel = {})
        operationManager.startOperation(recognitionOp)
        
        // When: Cancel operation
        operationManager.cancelOperation()
        
        // Then: Cancelled haptic pattern triggered (not CommandExecuted)
        verify(mockHapticManager).trigger(HapticPattern.Cancelled)
        verify(mockHapticManager, never()).trigger(HapticPattern.CommandExecuted)
    }
    
    /**
     * Task 8.3: Test cancelOperation with no active operation
     */
    @Test
    fun `cancelOperation with no active operation announces nothing to cancel`() = runTest {
        // Given: No active operation
        assertTrue(operationManager.activeOperation.value is Operation.None)
        
        // When: Cancel operation
        val result = operationManager.cancelOperation()
        
        // Then: TTS announces "Nothing to cancel"
        val messageCaptor = argumentCaptor<String>()
        verify(mockTTSManager).announce(messageCaptor.capture())
        assertEquals("Nothing to cancel", messageCaptor.firstValue)
        
        // Then: Result is failure
        assertTrue("Result should be Failure", result is CommandResult.Failure)
        
        // Then: No haptic feedback for "nothing to cancel" case
        verify(mockHapticManager, never()).trigger(any())
    }
    
    /**
     * Task 8.2: Test cancellation error handling - callback throws exception
     */
    @Test
    fun `cancelOperation handles callback exception gracefully`() = runTest {
        // Given: Active recognition operation with failing callback
        val recognitionOp = Operation.RecognitionOperation(
            onCancel = { throw RuntimeException("Cancellation failed") }
        )
        operationManager.startOperation(recognitionOp)
        
        // When: Cancel operation
        val result = operationManager.cancelOperation()
        
        // Then: Operation still cleared (prevent lock)
        assertTrue("Operation should be None even after error", operationManager.activeOperation.value is Operation.None)
        
        // Then: TTS announces failure
        val messageCaptor = argumentCaptor<String>()
        verify(mockTTSManager).announce(messageCaptor.capture())
        assertEquals("Cancellation failed. Please try again.", messageCaptor.firstValue)
        
        // Then: Result is failure
        assertTrue("Result should be Failure", result is CommandResult.Failure)
    }
    
    /**
     * Task 8.7: Test operation lifecycle - start → cancel → start again
     */
    @Test
    fun `operation lifecycle allows start after cancellation`() = runTest {
        // Given: Cancelled operation
        val recognitionOp1 = Operation.RecognitionOperation(onCancel = {})
        operationManager.startOperation(recognitionOp1)
        operationManager.cancelOperation()
        assertTrue(operationManager.activeOperation.value is Operation.None)
        
        // When: Start new operation
        val recognitionOp2 = Operation.RecognitionOperation(onCancel = {})
        operationManager.startOperation(recognitionOp2)
        
        // Then: New operation is active
        assertTrue("Should allow starting new operation after cancel", 
            operationManager.activeOperation.value is Operation.RecognitionOperation)
    }
    
    /**
     * Task 8.1: Test navigation operation tracking (Epic 6 placeholder)
     */
    @Test
    fun `startOperation supports NavigationOperation type`() = runTest {
        // Given: Navigation operation (Epic 6 future implementation)
        val navOp = Operation.NavigationOperation(onCancel = {})
        
        // When: Start navigation operation
        operationManager.startOperation(navOp)
        
        // Then: Operation is registered
        val activeOp = operationManager.activeOperation.first()
        assertTrue("Operation should be NavigationOperation", activeOp is Operation.NavigationOperation)
    }
    
    /**
     * Task 8.2: Test navigation cancellation (Epic 6 placeholder)
     */
    @Test
    fun `cancelOperation stops active navigation`() = runTest {
        // Given: Active navigation operation
        var cancelled = false
        val navOp = Operation.NavigationOperation(onCancel = { cancelled = true })
        operationManager.startOperation(navOp)
        
        // When: Cancel operation
        val result = operationManager.cancelOperation()
        
        // Then: Callback invoked, operation cleared, announced
        assertTrue(cancelled)
        assertTrue(operationManager.activeOperation.value is Operation.None)
        verify(mockTTSManager).announce("Cancelled")
        assertTrue(result is CommandResult.Success)
    }
}
