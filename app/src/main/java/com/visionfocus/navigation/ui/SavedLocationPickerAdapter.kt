package com.visionfocus.navigation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.visionfocus.R
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.databinding.ItemSavedLocationPickerBinding
import com.visionfocus.ui.savedlocations.SavedLocationUiModel

/**
 * RecyclerView adapter for saved location picker dialog.
 * 
 * Story 7.3 Task 2: Simple adapter for location selection without action buttons.
 * 
 * Features:
 * - Click-only interface (no action menu)
 * - TalkBack content descriptions: "[Name], [address if available]"
 * - Touch target minimum: 56 dp
 */
class SavedLocationPickerAdapter(
    private val onLocationClick: (SavedLocationEntity) -> Unit
) : ListAdapter<SavedLocationUiModel, SavedLocationPickerAdapter.ViewHolder>(LocationDiffCallback()) {
    
    // Keep parallel list of entities for callback
    private var entities: List<SavedLocationEntity> = emptyList()
    
    fun submitListWithEntities(uiModels: List<SavedLocationUiModel>, entities: List<SavedLocationEntity>) {
        this.entities = entities
        submitList(uiModels)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedLocationPickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onLocationClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), entities[position])
    }
    
    /**
     * ViewHolder for location picker item.
     * Story 7.3 Task 2.7: Set content descriptions for each item
     */
    class ViewHolder(
        private val binding: ItemSavedLocationPickerBinding,
        private val onLocationClick: (SavedLocationEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(uiModel: SavedLocationUiModel, entity: SavedLocationEntity) {
            // Display location name
            binding.locationNameTextView.text = uiModel.name
            
            // Display address (or hide if unavailable)
            if (uiModel.address.isNullOrBlank()) {
                binding.locationAddressTextView.text = binding.root.context.getString(R.string.no_address_available)
            } else {
                binding.locationAddressTextView.text = uiModel.address
            }
            
            // Story 7.3 Task 2.7: Content description for TalkBack
            // Format: "[Location name], [address if available]"
            val contentDesc = buildContentDescription(uiModel)
            binding.locationCard.contentDescription = contentDesc
            binding.root.contentDescription = contentDesc  // Also set on root for better accessibility
            
            // Click listener
            binding.locationCard.setOnClickListener {
                onLocationClick(entity)
            }
        }
        
        /**
         * Build content description for TalkBack.
         * Story 7.3 Task 2.7: Format: "[Name], [address]"
         */
        private fun buildContentDescription(location: SavedLocationUiModel): String {
            return buildString {
                append(location.name)
                if (!location.address.isNullOrBlank()) {
                    append(", ")
                    append(location.address)
                }
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates.
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
