package com.visionfocus.ui.beacon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.visionfocus.beacon.ScannedBeacon
import com.visionfocus.databinding.ItemScannedBeaconBinding

/**
 * Adapter for displaying discovered beacons during scanning.
 */
class ScannedBeaconAdapter(
    private val onItemClick: (ScannedBeacon) -> Unit
) : ListAdapter<ScannedBeacon, ScannedBeaconAdapter.ViewHolder>(BeaconDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScannedBeaconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemScannedBeaconBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(beacon: ScannedBeacon) {
            binding.deviceName.text = beacon.name.ifEmpty { "Unknown Device" }
            binding.deviceMac.text = beacon.macAddress
            binding.deviceRssi.text = "${beacon.rssi} dBm"
            
            // Accessibility
            binding.root.contentDescription = "Found ${beacon.name}, Signal strength ${beacon.rssi} decibels"
        }
    }

    private object BeaconDiffCallback : DiffUtil.ItemCallback<ScannedBeacon>() {
        override fun areItemsTheSame(oldItem: ScannedBeacon, newItem: ScannedBeacon): Boolean {
            return oldItem.macAddress == newItem.macAddress
        }

        override fun areContentsTheSame(oldItem: ScannedBeacon, newItem: ScannedBeacon): Boolean {
            return oldItem == newItem
        }
    }
}
