package com.visionfocus.ui.savedlocations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.visionfocus.R
import com.visionfocus.databinding.ItemSavedLocationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for saved locations list.
 * 
 * Story 7.2: Accessibility-first adapter with comprehensive content descriptions
 * 
 * Features:
 * - DiffUtil for efficient list updates
 * - TalkBack content descriptions: "[Name], [address], saved [date]"
 * - Click listener for action menu
 * - Touch target minimum: 48×48 dp (handled in layout)
 */
class SavedLocationAdapter(
    private val onItemClick: (SavedLocationUiModel) -> Unit
) : ListAdapter<SavedLocationUiModel, SavedLocationAdapter.ViewHolder>(LocationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedLocationBinding.inflate(
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
     * ViewHolder for saved location list item.
     * Story 7.2 AC3: TalkBack content description setup
     */
    class ViewHolder(
        private val binding: ItemSavedLocationBinding,
        private val onItemClick: (SavedLocationUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(location: SavedLocationUiModel) {
            // Display location name
            binding.locationNameText.text = location.name
            
            // Display address (or fallback message if unavailable)
            binding.locationAddressText.text = location.address 
                ?: binding.root.context.getString(R.string.no_address_available)
            
            // Format and display timestamp
            binding.timestampText.text = formatTimestamp(location.createdAt)
            
            // Story 7.2 AC3: Comprehensive TalkBack content description
            // Format: "[Location name], [address if available], saved [date]"
            val contentDesc = buildContentDescription(location)
            binding.root.contentDescription = contentDesc
            
            // Setup click listener
            binding.root.setOnClickListener {
                onItemClick(location)
            }
        }
        
        /**
         * Build comprehensive content description for TalkBack.
         * Story 7.2 AC3: Format: "[Name], [address], saved [date]"
         */
        private fun buildContentDescription(location: SavedLocationUiModel): String {
            return buildString {
                append(location.name)
                if (!location.address.isNullOrBlank()) {
                    append(", ")
                    append(location.address)
                }
                append(", saved ")
                append(formatTimestamp(location.createdAt))
            }
        }
        
        /**
         * Format timestamp to readable date.
         * Story 7.2 AC2: Readable date format: "December 20, 2025"
         */
        private fun formatTimestamp(millis: Long): String {
            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            return formatter.format(Date(millis))
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates.
     * Compares locations by ID and content.
     */
    private class LocationDiffCallback : DiffUtil.ItemCallback<SavedLocationUiModel>() {
        override fun areItemsTheSame(
            oldItem: SavedLocationUiModel,
            newItem: SavedLocationUiModel
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: SavedLocationUiModel,
            newItem: SavedLocationUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
