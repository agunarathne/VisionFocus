package com.visionfocus.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.databinding.FragmentHistoryBinding
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.history.adapter.RecognitionHistoryAdapter
import com.visionfocus.util.DateTimeFormatter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment displaying recognition history.
 * 
 * Displays last 50 recognition results in a RecyclerView with TalkBack support.
 * Users can review past recognitions and clear all history.
 * 
 * Story 4.3 Task 1: HistoryFragment with RecyclerView UI
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistoryViewModel by viewModels()
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private lateinit var adapter: RecognitionHistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClearHistoryButton()
        observeUiState()
    }
    
    /**
     * Configure RecyclerView with adapter and layout manager.
     * Story 4.3 Task 1.3: Configure RecyclerView with LinearLayoutManager
     */
    private fun setupRecyclerView() {
        adapter = RecognitionHistoryAdapter { historyItem ->
            // Handle item click - announce via TTS (Task 4)
            announceHistoryItem(historyItem)
        }
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
            // Accessibility: Let focus go to items, not container
            isFocusable = false
        }
    }
    
    /**
     * Setup clear history button with confirmation dialog.
     * Story 4.3 Task 7: Add "Clear History" button
     */
    private fun setupClearHistoryButton() {
        binding.clearHistoryFab.setOnClickListener {
            showClearHistoryConfirmationDialog()
        }
    }
    
    /**
     * Observe UI state from ViewModel and update UI accordingly.
     * Story 4.3 Task 6: Integrate HistoryViewModel with HistoryFragment
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HistoryUiState.Loading -> showLoading()
                        is HistoryUiState.Success -> showHistory(state.items)
                        is HistoryUiState.Empty -> showEmptyState()
                        is HistoryUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }
    
    /**
     * Show loading state with progress indicator.
     * Story 4.3 Task 6.4: Handle Loading state
     * Code Review Fix: Added progress indicator and TalkBack announcement
     */
    private fun showLoading() {
        binding.emptyStateTextView.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE
        
        // TalkBack announcement for accessibility
        binding.root.announceForAccessibility(
            getString(R.string.history_loading_announcement)
        )
    }
    
    /**
     * Display history items in RecyclerView.
     * Story 4.3 Task 6.3: Update RecyclerView when uiState is Success
     * Code Review Fix: Hide loading progress when data loads
     */
    private fun showHistory(items: List<com.visionfocus.data.local.entity.RecognitionHistoryEntity>) {
        binding.loadingProgressBar.visibility = View.GONE
        binding.emptyStateTextView.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.VISIBLE
        adapter.submitList(items)
    }
    
    /**
     * Show empty state when no history exists.
     * Story 4.3 Task 1.4 & 6.4: Implement empty state visibility logic
     * Bug fix: Hide loading progress when showing empty state
     */
    private fun showEmptyState() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.emptyStateTextView.visibility = View.VISIBLE
        binding.historyRecyclerView.visibility = View.GONE
    }
    
    /**
     * Show error state with user feedback.
     * Story 4.3 Task 6.5: Handle Error state
     * Code Review Fix: Added Snackbar with error message and TalkBack announcement
     */
    private fun showError(message: String) {
        binding.loadingProgressBar.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE
        binding.emptyStateTextView.visibility = View.VISIBLE
        binding.emptyStateTextView.text = getString(R.string.history_error_loading)
        
        // Show Snackbar with error details
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.history_error_retry) {
                // Retry loading by collecting state again
                observeUiState()
            }
            .show()
        
        // TalkBack announcement
        binding.root.announceForAccessibility(
            getString(R.string.history_error_announcement)
        )
    }
    
    /**
     * Announce history item details via TTS.
     * Story 4.3 Task 4: Implement list item click with TTS announcement
     */
    private fun announceHistoryItem(item: com.visionfocus.data.local.entity.RecognitionHistoryEntity) {
        val announcement = buildDetailedAnnouncement(item)
        // TTSManager.announce() is suspend, so launch in lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            ttsManager.announce(announcement)
        }
    }
    
    /**
     * Build detailed announcement for history item.
     * Uses same format as Story 2.2 TTSFormatter for consistency.
     * Code Review Fix: Extracted hardcoded strings to resources
     */
    private fun buildDetailedAnnouncement(item: com.visionfocus.data.local.entity.RecognitionHistoryEntity): String {
        val confidenceLevel = formatConfidence(item.confidence)
        val timestamp = DateTimeFormatter.formatTimestamp(item.timestamp)
        
        return if (item.detailText != null) {
            getString(R.string.history_announcement_recorded_detail, item.detailText, timestamp)
        } else {
            getString(R.string.history_announcement_recorded_basic, item.category, confidenceLevel, timestamp)
        }
    }
    
    /**
     * Format confidence level as text.
     * Story 4.3 Task 2.5 & 3.2: Format confidence for display and TalkBack
     */
    private fun formatConfidence(confidence: Float): String {
        return when {
            confidence >= 0.85f -> getString(R.string.confidence_high)
            confidence >= 0.70f -> getString(R.string.confidence_medium)
            else -> getString(R.string.confidence_low)
        }
    }
    
    /**
     * Show confirmation dialog before clearing history.
     * Story 4.3 Task 8: Implement clear history confirmation dialog
     */
    private fun showClearHistoryConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.history_clear_confirmation_title)
            .setMessage(R.string.history_clear_confirmation_message)
            .setPositiveButton(R.string.history_clear_confirmation_delete) { dialog, _ ->
                clearHistory()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.history_clear_confirmation_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Clear all history and announce completion.
     * Story 4.3 Task 9: Implement clear history functionality
     */
    private fun clearHistory() {
        viewModel.clearAllHistory()
        
        // Announce via TalkBack (Task 9.4)
        binding.root.announceForAccessibility(
            getString(R.string.history_cleared_announcement)
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
