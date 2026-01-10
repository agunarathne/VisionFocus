package com.visionfocus.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.visionfocus.R
import com.visionfocus.ui.savedlocations.SavedLocationUiModel
import kotlin.math.cos

/**
 * Story 7.4: Dialog for confirming offline map download
 * Shows download confirmation with estimated size and WiFi requirement
 * Calculates real size based on 2km radius bounding box
 */
class OfflineMapDownloadDialog : DialogFragment() {

    private var location: SavedLocationUiModel? = null
    private var onConfirmListener: ((Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val locationName = location?.name ?: "this location"
        val estimatedSize = calculateEstimatedSize(
            location?.latitude ?: 0.0,
            location?.longitude ?: 0.0,
            2000 // 2km radius
        )
        
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.download_offline_map_action)
            .setMessage(
                String.format(
                    getString(R.string.offline_map_download_dialog_message),
                    locationName,
                    formatSize(estimatedSize)
                )
            )
            .setPositiveButton(R.string.download) { _, _ ->
                onConfirmListener?.invoke(true)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onConfirmListener?.invoke(false)
            }
            .create()
    }
    
    /**
     * Calculate estimated download size based on bounding box area
     * Formula: ~10 MB per square kilometer for zoom levels 10-16
     */
    private fun calculateEstimatedSize(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Long {
        val earthRadiusMeters = 6371000.0
        
        // Convert radius to degrees
        val latOffset = Math.toDegrees(radiusMeters / earthRadiusMeters)
        val lngOffset = Math.toDegrees(radiusMeters / 
            (earthRadiusMeters * cos(Math.toRadians(latitude))))
        
        // Calculate area in square kilometers
        val latDiff = latOffset * 2
        val lngDiff = lngOffset * 2
        val areaSqKm = (latDiff * 111.0) * (lngDiff * 111.0 * cos(Math.toRadians(latitude)))
        
        // Estimate: ~10 MB per square km
        return (areaSqKm * 10 * 1024 * 1024).toLong()
    }
    
    private fun formatSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            else -> String.format("%.0f MB", mb)
        }
    }

    companion object {
        fun newInstance(
            location: SavedLocationUiModel,
            onConfirm: (Boolean) -> Unit
        ): OfflineMapDownloadDialog {
            return OfflineMapDownloadDialog().apply {
                this.location = location
                this.onConfirmListener = onConfirm
            }
        }
    }
}
