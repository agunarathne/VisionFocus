package com.visionfocus.accessibility.haptic

/**
 * Distinct haptic patterns for recognition events (Story 2.6)
 * 
 * Patterns designed for tactile discrimination by deaf-blind users:
 * 
 * - **RecognitionStart**: Single short vibration (100ms)
 *   - Feels like: Brief tap - "I heard your request"
 *   - Triggers on: Idle → Capturing state transition
 * 
 * - **RecognitionSuccess**: Double vibration (100ms, 50ms gap, 100ms)
 *   - Feels like: "Tap-tap" rhythm - "Task complete, here are results"
 *   - Triggers on: Announcing → Success state transition
 * 
 * - **RecognitionError**: Long vibration (300ms)
 *   - Feels like: Extended buzz - "Something went wrong, pay attention"
 *   - Triggers on: Any → Error or CameraError state transition
 * 
 * Pattern distinctness designed based on UX research principles (3x duration difference).
 * TODO: Validate with deaf-blind participants before production release.
 * 
 * @see HapticFeedbackManager for execution implementation
 */
sealed class HapticPattern {
    
    /**
     * Single short vibration indicating recognition started
     * Duration: 100ms
     * Timing: [0ms, 100ms]
     */
    object RecognitionStart : HapticPattern()
    
    /**
     * Double vibration pattern indicating recognition succeeded
     * Duration: 100ms + 50ms gap + 100ms = 250ms total
     * Timing: [0ms, 100ms, 50ms, 100ms]
     * 
     * The 50ms gap creates a distinct "tap-tap" tactile sensation
     * vs. single continuous vibration.
     */
    object RecognitionSuccess : HapticPattern()
    
    /**
     * Long vibration indicating recognition error
     * Duration: 300ms
     * Timing: [0ms, 300ms]
     * 
     * 3x longer than start pattern (300ms vs 100ms) ensures
     * immediate tactile distinction for urgent error attention.
     */
    object RecognitionError : HapticPattern()
}
