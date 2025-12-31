package com.visionfocus.voice.operation

import android.util.Log
import android.content.Context
import com.visionfocus.R
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.processor.CommandResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operation Manager - Tracks cancellable operations
 * Story 3.3 Task 2: Central registry for active operations that can be cancelled
 * 
 * Provides centralized tracking of active operations (recognition, navigation)
 * to enable context-aware cancellation via the "Cancel" voice command.
 * 
 * Features:
 * - Tracks single active operation via StateFlow (reactive updates)
 * - Registers operations with cancellation callbacks
 * - Executes cancellation with TTS + haptic feedback
 * - Handles "nothing to cancel" edge case gracefully
 * 
 * AC: Cancel works mid-recognition and mid-navigation
 * 
 * Integration Points:
 * - VoiceCommandProcessor: Registers operations before command execution
 * - RecognitionViewModel: Provides cancelRecognition() callback
 * - NavigationViewModel: Provides cancelNavigation() callback (Epic 6)
 * - CancelCommand: Queries active operation and triggers cancellation
 * 
 * Thread Safety:
 * - StateFlow provides thread-safe reactive updates
 * - All public methods are suspend functions for coroutine safety
 * 
 * @param ttsManager TTS engine for cancellation announcements
 * @param hapticFeedbackManager Haptic feedback for cancellation confirmation
 * @since Story 3.3
 */
@Singleton
class OperationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsManager: TTSManager,
    private val hapticFeedbackManager: HapticFeedbackManager
) {
    
    companion object {
        private const val TAG = "OperationManager"
    }
    
    // Current active operation (reactive via StateFlow)
    private val _activeOperation = MutableStateFlow<Operation>(Operation.None)
    
    /**
     * Current active operation state.
     * 
     * Observers can reactively respond to operation changes:
     * - UI: Show/hide cancel button based on operation state
     * - Commands: Query before execution to avoid conflicts
     * - ViewModels: React to cancellation triggers
     * 
     * @return StateFlow of current Operation (None, RecognitionOperation, NavigationOperation)
     */
    val activeOperation: StateFlow<Operation> = _activeOperation.asStateFlow()
    
    /**
     * Start tracking an operation.
     * Story 3.3 Task 2.3: Register operation for cancellation support
     * 
     * Call this BEFORE executing the operation to enable cancellation.
     * Only one operation can be active at a time (last one wins).
     * 
     * Example:
     * ```
     * operationManager.startOperation(
     *     Operation.RecognitionOperation(
     *         onCancel = { recognitionViewModel.cancelRecognition() }
     *     )
     * )
     * ```
     * 
     * @param operation The operation being started (with cancellation callback)
     */
    fun startOperation(operation: Operation) {
        _activeOperation.value = operation
        val operationType = when (operation) {
            is Operation.RecognitionOperation -> "RecognitionOperation"
            is Operation.NavigationOperation -> "NavigationOperation"
            is Operation.None -> "None"
        }
        Log.d(TAG, "Operation started: $operationType")
    }
    
    /**
     * Complete the current operation.
     * Story 3.3 Task 2.3: Deregister operation after completion
     * 
     * Call this AFTER operation completes successfully or on error.
     * Resets state to Operation.None.
     * 
     * Example:
     * ```
     * try {
     *     // Execute operation
     *     recognitionService.recognizeObjects()
     * } finally {
     *     operationManager.completeOperation()
     * }
     * ```
     */
    fun completeOperation() {
        _activeOperation.value = Operation.None
        Log.d(TAG, "Operation completed")
    }
    
    /**
     * Cancel the current active operation.
     * Story 3.3 Task 3: Context-aware cancellation
     * 
     * AC: Cancel command works for recognition and navigation
     * AC: Announce "Cancelled" after successful cancellation
     * AC: Handle case where no operation is active: "Nothing to cancel"
     * 
     * Flow:
     * 1. Query current active operation
     * 2. Invoke operation-specific cancellation callback
     * 3. Reset state to Operation.None
     * 4. Announce "Cancelled" with haptic feedback
     * 
     * Thread Safety: Suspend function for safe coroutine execution
     * 
     * @return CommandResult.Success if cancelled, CommandResult.Failure if nothing to cancel
     */
    suspend fun cancelOperation(): CommandResult {
        val current = _activeOperation.value
        
        return when (current) {
            is Operation.RecognitionOperation -> {
                Log.d(TAG, "Cancelling recognition operation")
                
                // Cancel recognition via callback
                try {
                    current.onCancel()
                    
                    // Reset state only after successful cancellation
                    _activeOperation.value = Operation.None
                    
                    // Announce cancellation (AC: 6, Story 3.3)
                    ttsManager.announce(context.getString(R.string.voice_cancelled))
                    
                    // Haptic feedback with Cancelled pattern (distinct from CommandExecuted)
                    try {
                        hapticFeedbackManager.trigger(HapticPattern.Cancelled)
                    } catch (e: Exception) {
                        Log.w(TAG, "Haptic feedback failed during cancellation", e)
                        // Non-fatal - continue
                    }
                    
                    Log.d(TAG, "Recognition cancelled successfully")
                    CommandResult.Success("Recognition cancelled")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during recognition cancellation", e)
                    // Still reset state to prevent lock, but inform user of failure
                    _activeOperation.value = Operation.None
                    ttsManager.announce(context.getString(R.string.voice_cancellation_failed))
                    CommandResult.Failure("Cancellation error: ${e.message}")
                }
            }
            
            is Operation.NavigationOperation -> {
                Log.d(TAG, "Cancelling navigation operation")
                
                // Cancel navigation via callback
                try {
                    current.onCancel()
                    
                    // Reset state only after successful cancellation
                    _activeOperation.value = Operation.None
                    
                    // Announce cancellation (AC: 6, Story 3.3)
                    ttsManager.announce(context.getString(R.string.voice_cancelled))
                    
                    // Haptic feedback with Cancelled pattern
                    try {
                        hapticFeedbackManager.trigger(HapticPattern.Cancelled)
                    } catch (e: Exception) {
                        Log.w(TAG, "Haptic feedback failed during cancellation", e)
                    }
                    
                    Log.d(TAG, "Navigation cancelled successfully")
                    CommandResult.Success("Navigation cancelled")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during navigation cancellation", e)
                    _activeOperation.value = Operation.None
                    ttsManager.announce(context.getString(R.string.voice_cancellation_failed))
                    CommandResult.Failure("Cancellation error: ${e.message}")
                }
            }
            
            Operation.None -> {
                // No active operation to cancel (AC: 3.5)
                Log.d(TAG, "Cancel requested but no active operation")
                ttsManager.announce(context.getString(R.string.voice_nothing_to_cancel))
                CommandResult.Failure("No active operation")
            }
        }
    }
    
    /**
     * Check if any cancellable operation is currently active.
     * 
     * Useful for:
     * - UI: Show/hide cancel button
     * - Command validation: Prevent starting new operation if one is active
     * 
     * @return true if RecognitionOperation or NavigationOperation active, false if None
     */
    fun isOperationActive(): Boolean {
        return _activeOperation.value !is Operation.None
    }
}
