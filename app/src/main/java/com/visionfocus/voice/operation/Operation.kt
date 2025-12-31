package com.visionfocus.voice.operation

/**
 * Operation sealed class representing cancellable operations
 * Story 3.3 Task 2.2: Define Operation types for tracking active operations
 * 
 * This sealed class hierarchy provides type-safe operation tracking for the
 * OperationManager. Each operation type encapsulates a cancellation callback
 * that will be invoked when the user issues a "Cancel" voice command.
 * 
 * AC: Cancel command works mid-recognition and mid-navigation
 * 
 * Architecture:
 * - RecognitionOperation: Camera + TFLite inference (Epic 2)
 * - NavigationOperation: Turn-by-turn guidance (Epic 6 - placeholder)
 * - None: No active cancellable operation
 * 
 * Integration Points:
 * - RecognitionViewModel.cancelRecognition() callback
 * - NavigationViewModel.cancelNavigation() callback (Epic 6)
 * - OperationManager tracks current active operation via StateFlow
 * 
 * @since Story 3.3
 */
sealed class Operation {
    /**
     * Recognition operation active (camera capture + object detection)
     * 
     * Triggered by: "Recognize" voice command or recognition FAB tap
     * Cancellable phases:
     * - Camera initialization
     * - TFLite inference
     * - TTS result announcement
     * 
     * @param onCancel Callback to invoke cancellation (RecognitionViewModel.cancelRecognition)
     * @param onComplete Callback after operation completes (Story 3.5 AC #3: context preservation)
     */
    data class RecognitionOperation(
        val onCancel: suspend () -> Unit,
        val onComplete: (() -> Unit)? = null
    ) : Operation()
    
    /**
     * Navigation operation active (turn-by-turn GPS guidance)
     * 
     * Triggered by: "Navigate" voice command
     * Cancellable phases:
     * - Destination input
     * - Route calculation
     * - Active guidance announcements
     * 
     * Note: Epic 6 not yet implemented - placeholder for forward compatibility
     * 
     * @param onCancel Callback to invoke cancellation (NavigationViewModel.cancelNavigation)
     * @param onComplete Callback after operation completes (Story 3.5 AC #3: context preservation)
     */
    data class NavigationOperation(
        val onCancel: suspend () -> Unit,
        val onComplete: (() -> Unit)? = null
    ) : Operation()
    
    /**
     * No active cancellable operation
     * 
     * Default state when app is idle or non-cancellable operations are running.
     * CancelCommand will announce "Nothing to cancel" in this state.
     */
    object None : Operation()
}
