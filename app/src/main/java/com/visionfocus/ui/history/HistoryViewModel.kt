package com.visionfocus.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import com.visionfocus.data.repository.RecognitionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Sealed class representing UI state for history screen.
 * Story 4.3 Task 5.3: Create sealed class HistoryUiState
 */
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val items: List<RecognitionHistoryEntity>) : HistoryUiState()
    object Empty : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

/**
 * ViewModel for recognition history screen.
 * 
 * Manages history data loading and clear operations.
 * Uses StateFlow for reactive UI updates.
 * 
 * Story 4.3 Task 5: Create HistoryViewModel with StateFlow
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: RecognitionHistoryRepository
) : ViewModel() {
    
    // Task 5.4: Expose StateFlow<HistoryUiState> for fragment observation
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        // Task 5.5: Load history via repository in init block
        loadHistory()
    }
    
    /**
     * Load recognition history from repository.
     * Story 4.3 Task 5.5: Load history via repository.getRecentHistory()
     */
    private fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getRecentHistory()
                .catch { error ->
                    Timber.e(error, "Failed to load recognition history")
                    _uiState.value = HistoryUiState.Error(
                        error.message ?: "Failed to load history"
                    )
                }
                .collect { items ->
                    _uiState.value = if (items.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(items)
                    }
                }
        }
    }
    
    /**
     * Clear all recognition history.
     * Story 4.3 Task 9.1 & 9.2: Clear history functionality
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearAllHistory()
                Timber.i("Recognition history cleared successfully")
                // Task 9.3: Update uiState to Empty after successful deletion
                _uiState.value = HistoryUiState.Empty
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear recognition history")
                _uiState.value = HistoryUiState.Error(
                    "Failed to clear history: ${e.message}"
                )
            }
        }
    }
}
