package com.visionfocus.ui.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.visionfocus.R
import com.visionfocus.databinding.FragmentSavedLocationsBinding
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Fragment for viewing and managing saved locations.
 * 
 * Story 7.2: Saved Locations Management UI
 * 
 * Features:
 * - RecyclerView displaying all saved locations sorted by most recently used
 * - Each item shows: location name, address (if available), save timestamp
 * - TalkBack content descriptions for full accessibility
 * - Action menu for Navigate, Edit, Delete operations
 * - Empty state when no locations exist
 * 
 * @AndroidEntryPoint enables Hilt dependency injection
 */
@AndroidEntryPoint
class SavedLocationsFragment : Fragment() {
    
    private var _binding: FragmentSavedLocationsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SavedLocationsViewModel by viewModels()
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private lateinit var adapter: SavedLocationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeUiState()
        observeEvents()
        
        Timber.d("SavedLocationsFragment initialized")
    }
    
    /**
     * Setup RecyclerView with adapter and layout manager.
     * Story 7.2 Task 1.6: View binding setup
     */
    private fun setupRecyclerView() {
        adapter = SavedLocationAdapter(
            onItemClick = { location ->
                showActionMenu(location)
            }
        )
        
        binding.locationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SavedLocationsFragment.adapter
            
            // Accessibility: Each item is focusable
            isFocusable = true
        }
    }
    
    /**
     * Observe UI state changes from ViewModel.
     * Story 7.2 Task 3.5: StateFlow observation
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SavedLocationsUiState.Loading -> {
                        showLoading()
                    }
                    is SavedLocationsUiState.Success -> {
                        showLocations(state.locations)
                    }
                    is SavedLocationsUiState.Empty -> {
                        showEmptyState()
                    }
                    is SavedLocationsUiState.Error -> {
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    /**
     * Observe one-time events from ViewModel.
     * Story 7.2 Task 3: Event handling for TalkBack announcements
     */
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is SavedLocationsEvent.NavigationReady -> {
                        navigateToNavigationActiveFragment(event.route, event.destinationName)
                    }
                    is SavedLocationsEvent.NavigationStarted -> {
                        announceNavigationStarted(event.locationName)
                    }
                    is SavedLocationsEvent.LocationUpdated -> {
                        announceLocationUpdated(event.locationName)
                    }
                    is SavedLocationsEvent.LocationDeleted -> {
                        announceLocationDeleted(event.locationName)
                    }
                    is SavedLocationsEvent.Error -> {
                        announceError(event.message)
                    }
                }
            }
        }
    }
    
    /**
     * Show loading state.
     * Code Review Fix: Added loading indicator for better UX.
     */
    private fun showLoading() {
        binding.locationsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE
    }
    
    /**
     * Show locations list.
     * Story 7.2 AC1: RecyclerView displays all locations
     */
    private fun showLocations(locations: List<SavedLocationUiModel>) {
        binding.locationsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.loadingIndicator.visibility = View.GONE
        adapter.submitList(locations)
        
        Timber.d("Displaying ${locations.size} saved locations")
    }
    
    /**
     * Show empty state.
     * Story 7.2 AC10: Empty state UI
     */
    private fun showEmptyState() {
        binding.locationsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.loadingIndicator.visibility = View.GONE
        
        // Announce empty state for TalkBack
        announceEmptyState()
        
        Timber.d("Showing empty state")
    }
    
    /**
     * Show error state.
     */
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        Timber.e("Error loading saved locations: $message")
        // Show error UI or toast
        announceError(message)
    }
    
    /**
     * Show action menu dialog for location.
     * Story 7.2 AC5: Action menu with Navigate, Edit, Delete
     */
    private fun showActionMenu(location: SavedLocationUiModel) {
        val dialog = LocationActionDialogFragment.newInstance(location)
        dialog.show(childFragmentManager, "action_menu")
    }
    
    /**
     * Handle Navigate action from action menu.
     * Story 7.2 AC6: Navigate to selected location
     */
    fun onNavigateClicked(location: SavedLocationUiModel) {
        viewModel.navigateToLocation(location)
    }
    
    /**
     * Handle Edit action from action menu.
     * Story 7.2 AC7: Edit location name
     */
    fun onEditClicked(location: SavedLocationUiModel) {
        val dialog = EditLocationDialogFragment.newInstance(location.id, location.name)
        dialog.show(childFragmentManager, "edit_location")
    }
    
    /**
     * Handle Delete action from action menu.
     * Story 7.2 AC8: Show delete confirmation dialog
     */
    fun onDeleteClicked(location: SavedLocationUiModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(location)
        dialog.show(childFragmentManager, "delete_confirmation")
    }
    
    /**
     * Callback from EditLocationDialogFragment.
     * Story 7.2 AC7: Update location name
     */
    fun onLocationNameUpdated(locationId: Long, newName: String) {
        viewModel.updateLocationName(locationId, newName)
    }
    
    /**
     * Callback from DeleteConfirmationDialogFragment.
     * Story 7.2 AC9: Delete location and restore focus
     * Code Review Fix: Improved focus restoration to same position.
     */
    fun onDeleteConfirmed(location: SavedLocationUiModel) {
        val currentPosition = adapter.currentList.indexOf(location)
        viewModel.deleteLocation(location)
        
        // Story 7.2 AC9: Return focus to RecyclerView at intelligent position
        binding.locationsRecyclerView.post {
            val newPosition = if (currentPosition > 0) currentPosition - 1 else 0
            binding.locationsRecyclerView.layoutManager?.findViewByPosition(newPosition)?.requestFocus()
                ?: binding.locationsRecyclerView.requestFocus()
        }
    }
    
    // TalkBack announcements
    
    /**
     * Navigate to NavigationActiveFragment with calculated route.
     * Story 7.2 AC6: Navigate to saved location
     */
    private fun navigateToNavigationActiveFragment(route: NavigationRoute, destinationName: String) {
        try {
            val action = SavedLocationsFragmentDirections
                .actionSavedLocationsToNavigationActive(route, destinationName)
            findNavController().navigate(action)
            Timber.d("Navigated to NavigationActiveFragment for $destinationName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate to NavigationActiveFragment")
            announceError("Navigation failed. Please try again.")
        }
    }
    
    /**
     * Announce navigation started.
     * Story 7.2 AC6: TalkBack announcement
     */
    private fun announceNavigationStarted(locationName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val announcement = getString(R.string.starting_navigation_to, locationName)
            ttsManager.announce(announcement)
            Timber.d("Announced: $announcement")
        }
    }
    
    /**
     * Announce location name updated.
     * Story 7.2 AC7: TalkBack announcement
     */
    private fun announceLocationUpdated(locationName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val announcement = getString(R.string.location_name_updated_to, locationName)
            ttsManager.announce(announcement)
            Timber.d("Announced: $announcement")
        }
    }
    
    /**
     * Announce location deleted.
     * Story 7.2 AC9: TalkBack announcement
     */
    private fun announceLocationDeleted(locationName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val announcement = getString(R.string.location_deleted, locationName)
            ttsManager.announce(announcement)
            Timber.d("Announced: $announcement")
        }
    }
    
    /**
     * Announce empty state.
     * Story 7.2 AC10: TalkBack announcement
     */
    private fun announceEmptyState() {
        viewLifecycleOwner.lifecycleScope.launch {
            val announcement = getString(R.string.no_saved_locations_announcement)
            ttsManager.announce(announcement)
            Timber.d("Announced: $announcement")
        }
    }
    
    /**
     * Announce error message.
     */
    private fun announceError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            ttsManager.announce(message)
            Timber.d("Announced error: $message")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
