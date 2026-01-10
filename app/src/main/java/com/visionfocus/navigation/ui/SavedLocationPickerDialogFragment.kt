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
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.databinding.DialogSavedLocationPickerBinding
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.ui.savedlocations.SavedLocationUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Saved Location Picker Dialog for quick navigation.
 * 
 * Story 7.3 Task 2: Create SavedLocationPickerDialog
 * 
 * Shows list of saved locations sorted by lastUsedAt (most recent first).
 * On location selection, returns selected location to parent fragment.
 * 
 * @property onLocationSelected Callback invoked when user selects a location
 */
@AndroidEntryPoint
class SavedLocationPickerDialogFragment : DialogFragment() {
    
    @Inject
    lateinit var repository: SavedLocationRepository
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private var _binding: DialogSavedLocationPickerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: SavedLocationPickerAdapter
    
    var onLocationSelected: ((SavedLocationEntity) -> Unit)? = null
    
    companion object {
        const val TAG = "SavedLocationPickerDialog"
        
        /**
         * Creates new instance of SavedLocationPickerDialogFragment.
         * 
         * @param onLocationSelected Callback for location selection
         * @return New dialog instance
         */
        fun newInstance(onLocationSelected: (SavedLocationEntity) -> Unit): SavedLocationPickerDialogFragment {
            return SavedLocationPickerDialogFragment().apply {
                this.onLocationSelected = onLocationSelected
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSavedLocationPickerBinding.inflate(layoutInflater)
        
        setupRecyclerView()
        loadSavedLocations()
        
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
        
        // Story 7.3 Task 2.6: TalkBack announcement on dialog show
        lifecycleScope.launch {
            ttsManager.announce(getString(R.string.select_saved_location_title))
        }
    }
    
    /**
     * Sets up RecyclerView with SavedLocationPickerAdapter.
     * Story 7.3 Task 2.2: Create dialog with RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = SavedLocationPickerAdapter { entity ->
            // Story 7.3 Task 2.8: On item click, return selected location
            onLocationSelected?.invoke(entity)
            dismiss()
        }
        
        binding.savedLocationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SavedLocationPickerDialogFragment.adapter
        }
    }
    
    /**
     * Loads saved locations from repository.
     * Story 7.3 Task 2.4: Load all locations sorted by lastUsedAt
     */
    private fun loadSavedLocations() {
        lifecycleScope.launch {
            try {
                // Story 7.3 Task 2.4: Sort by lastUsedAt (most recent first)
                val locations = repository.getAllLocationsSorted().firstOrNull() ?: emptyList()
                
                if (locations.isEmpty()) {
                    // Show empty state
                    binding.savedLocationsRecyclerView.visibility = View.GONE
                    binding.emptyStateTextView.visibility = View.VISIBLE
                } else {
                    // Show locations
                    binding.savedLocationsRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateTextView.visibility = View.GONE
                    
                    // Convert entity to UI model
                    val uiModels = locations.map { entity ->
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
                    adapter.submitListWithEntities(uiModels, locations)
                }
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to load saved locations for picker")
                // Show empty state on error
                binding.savedLocationsRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
