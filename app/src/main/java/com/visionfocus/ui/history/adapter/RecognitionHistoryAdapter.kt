package com.visionfocus.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.visionfocus.R
import com.visionfocus.data.local.entity.RecognitionHistoryEntity
import com.visionfocus.databinding.ItemRecognitionHistoryBinding
import com.visionfocus.util.DateTimeFormatter

/**
 * RecyclerView adapter for recognition history items.
 * 
 * Uses ListAdapter with DiffUtil for efficient updates and TalkBack support.
 * 
 * Story 4.3 Task 2: Create RecyclerView adapter for history items
 */
class RecognitionHistoryAdapter(
    private val onItemClick: (RecognitionHistoryEntity) -> Unit
) : ListAdapter<RecognitionHistoryEntity, RecognitionHistoryAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecognitionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder for recognition history items.
     * Story 4.3 Task 2.4 & Task 3: ViewHolder binding with TalkBack support
     */
    class ViewHolder(
        private val binding: ItemRecognitionHistoryBinding,
        private val onItemClick: (RecognitionHistoryEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Bind recognition history data to views.
         * Story 4.3 Task 2.4: Implement ViewHolder binding
         * Story 4.3 Task 3: Implement TalkBack content descriptions
         */
        fun bind(item: RecognitionHistoryEntity) {
            binding.categoryTextView.text = item.category
            binding.confidenceTextView.text = formatConfidence(item.confidence)
            binding.timestampTextView.text = formatTimestamp(item.timestamp)
            
            // CRITICAL: TalkBack content description for entire item (Task 3.1 & 3.2)
            binding.root.contentDescription = buildContentDescription(item)
            
            // Ensure item is focusable for TalkBack (Task 3.3)
            binding.root.isFocusable = true
            binding.root.isClickable = true
            
            // Task 4.1: Add OnClickListener for TTS announcement
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
        
        /**
         * Build TalkBack content description for history item.
         * Format: "[category], [confidence level], [formatted timestamp]"
         * Story 4.3 Task 3.2: Format description
         */
        private fun buildContentDescription(item: RecognitionHistoryEntity): String {
            val confidence = formatConfidence(item.confidence)
            val timestamp = formatTimestamp(item.timestamp)
            return "${item.category}, $confidence, $timestamp"
        }
        
        /**
         * Format confidence level as text.
         * Story 4.3 Task 2.4: Format confidence for display
         */
        private fun formatConfidence(confidence: Float): String {
            val context = binding.root.context
            return when {
                confidence >= 0.85f -> context.getString(R.string.confidence_high)
                confidence >= 0.70f -> context.getString(R.string.confidence_medium)
                else -> context.getString(R.string.confidence_low)
            }
        }
        
        /**
         * Format timestamp as human-readable text.
         * Story 4.3 Task 2.5: Format timestamp using DateTimeFormatter
         * Code Review Fix: Use centralized DateTimeFormatter utility for consistency
         */
        private fun formatTimestamp(timestamp: Long): String {
            return DateTimeFormatter.formatTimestamp(timestamp)
        }
    }
    
    /**
     * DiffUtil callback for efficient RecyclerView updates.
     * Story 4.3 Task 2.3: Create adapter with DiffUtil
     */
    private class DiffCallback : DiffUtil.ItemCallback<RecognitionHistoryEntity>() {
        override fun areItemsTheSame(
            oldItem: RecognitionHistoryEntity,
            newItem: RecognitionHistoryEntity
        ): Boolean = oldItem.id == newItem.id
        
        override fun areContentsTheSame(
            oldItem: RecognitionHistoryEntity,
            newItem: RecognitionHistoryEntity
        ): Boolean = oldItem == newItem
    }
}
