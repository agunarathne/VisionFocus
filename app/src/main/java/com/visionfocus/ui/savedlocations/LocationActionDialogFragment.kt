package com.visionfocus.ui.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.visionfocus.R
import com.visionfocus.databinding.DialogLocationActionBinding

/**
 * Dialog fragment for location action menu.
 * 
 * Story 7.2 AC5: Action menu with Navigate, Edit, Delete options
 * Story 7.4: Added Download Offline Map and Delete Offline Map actions
 * 
 * Actions:
 * - Navigate: Start turn-by-turn navigation to location
 * - Edit: Show edit dialog to change location name
 * - Delete: Show confirmation dialog before deletion
 * - Download Offline Map: Download map for offline use
 * - Delete Offline Map: Remove downloaded offline map
 */
class LocationActionDialogFragment : DialogFragment() {
    
    private var _binding: DialogLocationActionBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val ARG_LOCATION = "location"
        
        fun newInstance(location: SavedLocationUiModel): LocationActionDialogFragment {
            return LocationActionDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LOCATION, location)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLocationActionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val location = arguments?.getParcelable<SavedLocationUiModel>(ARG_LOCATION)
            ?: throw IllegalStateException("Location required")
        
        setupActionButtons(location)
    }
    
    /**
     * Setup action button click listeners.
     * Story 7.2 AC5: Navigate, Edit, Delete actions
     * Story 7.4: Added offline map actions
     */
    private fun setupActionButtons(location: SavedLocationUiModel) {
        // Story 7.2 AC6: Navigate action
        binding.navigateButton.apply {
            contentDescription = getString(R.string.navigate_to_location_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onNavigateClicked(location)
                dismiss()
            }
        }
        
        // Story 7.2 AC7: Edit action
        binding.editButton.apply {
            contentDescription = getString(R.string.edit_location_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onEditClicked(location)
                dismiss()
            }
        }
        
        // Story 7.2 AC8: Delete action
        binding.deleteButton.apply {
            contentDescription = getString(R.string.delete_location_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onDeleteClicked(location)
                dismiss()
            }
        }
        
        // Story 7.4: Download offline map action
        binding.downloadOfflineMapButton?.apply {
            visibility = if (location.hasOfflineMap) View.GONE else View.VISIBLE
            contentDescription = getString(R.string.download_offline_map_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onDownloadOfflineMapClicked(location)
                dismiss()
            }
        }
        
        // Story 7.4: Delete offline map action
        binding.deleteOfflineMapButton?.apply {
            visibility = if (location.hasOfflineMap) View.VISIBLE else View.GONE
            contentDescription = getString(R.string.delete_offline_map_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onDeleteOfflineMapClicked(location)
                dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
