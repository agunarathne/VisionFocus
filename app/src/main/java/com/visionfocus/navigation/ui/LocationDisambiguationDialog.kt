package com.visionfocus.navigation.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.databinding.DialogLocationDisambiguationBinding
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.savedlocations.SavedLocationUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Location Disambiguation Dialog for multiple fuzzy matches.
 * 
 * Story 7.3 Task 6: Handle ambiguous voice command matches
 * 
 * Shows list of potential matches when voice command matches multiple locations.
 * User selects the intended location from the list.
 * 
 * @property locations List of matching SavedLocationEntity objects
 * @property onLocationSelected Callback invoked when user selects a location
 */
@AndroidEntryPoint
class LocationDisambiguationDialog : DialogFragment() {
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private var _binding: DialogLocationDisambiguationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: SavedLocationPickerAdapter
    private var locations: List<SavedLocationEntity> = emptyList()
    
    var onLocationSelected: ((SavedLocationEntity) -> Unit)? = null
    
    companion object {
        const val TAG = "LocationDisambiguationDialog"
        private const val ARG_LOCATIONS = "locations"
        
        /**
         * Creates new instance of LocationDisambiguationDialog.
         * 
         * Story 7.3 Task 6.3: Show dialog when multiple fuzzy matches found
         * 
         * @param locations List of matching locations (up to 5)
         * @param onLocationSelected Callback for location selection
         * @return New dialog instance
         */
        fun newInstance(
            locations: List<SavedLocationEntity>,
            onLocationSelected: (SavedLocationEntity) -> Unit
        ): LocationDisambiguationDialog {
            return LocationDisambiguationDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_LOCATIONS, ArrayList(locations))
                }
                this.onLocationSelected = onLocationSelected
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        @Suppress("DEPRECATION")
        locations = arguments?.getParcelableArrayList<SavedLocationEntity>(ARG_LOCATIONS) ?: emptyList()
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLocationDisambiguationBinding.inflate(layoutInflater)
        
        setupRecyclerView()
        displayLocations()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    override fun onStart() {
        super.onStart()
        
        // Story 7.3 Task 6.5: TalkBack announcement
        lifecycleScope.launch {
            ttsManager.announce(getString(R.string.multiple_locations_select_one))
        }
    }
    
    /**
     * Sets up RecyclerView with SavedLocationPickerAdapter.
     * Story 7.3 Task 6.2: Create dialog with list of options
     */
    private fun setupRecyclerView() {
        adapter = SavedLocationPickerAdapter { entity ->
            // Story 7.3 Task 6.6: On item selection, start navigation
            onLocationSelected?.invoke(entity)
            dismiss()
        }
        
        binding.optionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LocationDisambiguationDialog.adapter
        }
    }
    
    /**
     * Displays matching locations in RecyclerView.
     * Story 7.3 Task 6.4: Display each match with name and distance score
     */
    private fun displayLocations() {
        // Story 7.3: Locations are already sorted by distance (closest first)
        // Take maximum 5 matches to avoid overwhelming user
        val displayLocations = locations.take(5)
        
        // Convert to UI model
        val uiModels = displayLocations.map { entity ->
            SavedLocationUiModel(
                id = entity.id,
                name = entity.name,
                latitude = entity.latitude,
                longitude = entity.longitude,
                address = entity.address,
                createdAt = entity.createdAt,
                lastUsedAt = entity.lastUsedAt
            )
        }
        
        adapter.submitListWithEntities(uiModels, displayLocations)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
