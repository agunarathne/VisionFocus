package com.visionfocus.ui.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.recognition.processing.ConfidenceFilter
import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.formatter.TTSPhraseFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for recognition UI (Story 2.3)
 * 
 * Orchestrates:
 * - Story 2.1: RecognitionRepository.performRecognition() (camera + TFLite)
 * - Story 2.2: TTSPhraseFormatter + TTSManager (formatting + announcement)
 * - Story 2.3: UI state management with StateFlow
 * 
 * State transitions:
 * Idle → Recognizing → Announcing → Success → Idle (2s delay)
 * Idle → Recognizing → Error → Idle (3s delay)
 * 
 * @param recognitionRepository Story 2.1 - Object recognition with TFLite
 * @param confidenceFilter Story 2.2 - Convert DetectionResult to FilteredDetection
 * @param ttsManager Story 2.2 - Text-to-speech engine
 * @param ttsFormatter Story 2.2 - Confidence-aware phrase formatting
 */
@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
    private val confidenceFilter: ConfidenceFilter,
    private val ttsManager: TTSManager,
    private val ttsFormatter: TTSPhraseFormatter
) : ViewModel() {
    
    companion object {
        private const val TAG = "RecognitionViewModel"
        
        /**
         * Delay before returning to Idle state after Success
         * Allows user to see/hear success state briefly
         */
        private const val SUCCESS_DELAY_MS = 2000L
        
        /**
         * Delay before returning to Idle state after Error
         * Allows user to hear error announcement
         */
        private const val ERROR_DELAY_MS = 3000L
    }
    
    // Story 2.3 Task 1.5: Expose uiState as StateFlow
    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()
    
    /**
     * Trigger object recognition pipeline
     * 
     * Story 2.3 Task 1.6: Implement recognizeObject() function
     * Story 2.3 Task 1.7: Orchestrate complete pipeline
     * Story 2.3 Task 1.8: Handle state transitions
     * 
     * Pipeline:
     * 1. Transition to Recognizing state
     * 2. Call RecognitionRepository.performRecognition() (Story 2.1)
     * 3. Format announcement with TTSPhraseFormatter (Story 2.2)
     * 4. Transition to Announcing state
     * 5. Call TTSManager.announce() (Story 2.2)
     * 6. Transition to Success state with results
     * 7. Auto-return to Idle after delay
     * 
     * Error handling:
     * - SecurityException: Camera permission denied
     * - IllegalStateException: TTS not initialized or camera not ready
     * - Other exceptions: Generic recognition failure
     */
    fun recognizeObject() {
        // Ignore if already in progress
        if (_uiState.value !is RecognitionUiState.Idle) {
            return
        }
        
        viewModelScope.launch {
            try {
                // Story 2.3 Task 1.8: State transition - Idle → Recognizing
                _uiState.value = RecognitionUiState.Recognizing
                
                // Story 2.1: Camera → TFLite inference (≤320ms)
                val result = recognitionRepository.performRecognition()
                
                // Convert DetectionResult to FilteredDetection (Story 2.2)
                val filteredDetections = result.detections.map { detection ->
                    confidenceFilter.toFilteredDetection(detection)
                }
                
                // Story 2.2: Format announcement with confidence-aware phrasing
                val announcement = ttsFormatter.formatMultipleDetections(filteredDetections)
                
                // Story 2.3 Task 1.8: State transition - Recognizing → Announcing
                _uiState.value = RecognitionUiState.Announcing
                
                // Story 2.2: TTS announcement (≤200ms initiation)
                ttsManager.announce(announcement)
                
                // Story 2.3 Task 1.8: State transition - Announcing → Success
                _uiState.value = RecognitionUiState.Success(
                    results = filteredDetections,
                    announcement = announcement,
                    latency = result.latencyMs
                )
                
                // Auto-return to Idle after brief delay (allows user to see success)
                delay(SUCCESS_DELAY_MS)
                _uiState.value = RecognitionUiState.Idle
                
            } catch (e: SecurityException) {
                // Camera permission denied
                handleError("Camera permission required. Please enable camera access in settings.")
                
            } catch (e: IllegalStateException) {
                // TTS not initialized or camera not ready
                handleError(e.message ?: "Recognition service not ready. Please try again.")
                
            } catch (e: Exception) {
                // Generic recognition failure
                handleError(e.message ?: "Recognition failed. Please try again.")
            }
        }
    }
    
    /**
     * Handle error state and auto-recovery
     * 
     * @param message User-friendly error message
     */
    private suspend fun handleError(message: String) {
        // Transition to Error state
        _uiState.value = RecognitionUiState.Error(message = message)
        
        // Auto-return to Idle after error announcement delay
        delay(ERROR_DELAY_MS)
        _uiState.value = RecognitionUiState.Idle
    }
}
