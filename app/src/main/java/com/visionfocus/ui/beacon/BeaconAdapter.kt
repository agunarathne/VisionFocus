package com.visionfocus.ui.beacon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.visionfocus.data.local.entity.BeaconEntity
import com.visionfocus.databinding.ItemBeaconBinding

/**
 * Adapter for displaying paired beacons.
 */
class BeaconAdapter(
    private val onDeleteClick: (BeaconEntity) -> Unit,
    private val onItemClick: (BeaconEntity) -> Unit
) : ListAdapter<BeaconEntity, BeaconAdapter.BeaconViewHolder>(BeaconDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        val binding = ItemBeaconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BeaconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BeaconViewHolder(private val binding: ItemBeaconBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(beacon: BeaconEntity) {
            binding.beaconName.text = beacon.name
            binding.beaconMac.text = beacon.macAddress
            
            // Accessibility
            binding.root.contentDescription = "${beacon.name}, ${beacon.macAddress}"
        }
    }

    class BeaconDiffCallback : DiffUtil.ItemCallback<BeaconEntity>() {
        override fun areItemsTheSame(oldItem: BeaconEntity, newItem: BeaconEntity): Boolean {
            return oldItem.macAddress == newItem.macAddress
        }

        override fun areContentsTheSame(oldItem: BeaconEntity, newItem: BeaconEntity): Boolean {
            return oldItem == newItem
        }
    }
}