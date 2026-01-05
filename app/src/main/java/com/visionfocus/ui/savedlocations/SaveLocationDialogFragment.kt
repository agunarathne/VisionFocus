package com.visionfocus.ui.savedlocations

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.data.repository.DuplicateLocationException
import com.visionfocus.data.repository.SavedLocationRepository
import com.visionfocus.databinding.DialogSaveLocationBinding
import com.visionfocus.navigation.location.LocationError
import com.visionfocus.navigation.location.LocationManager
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Dialog fragment for saving current GPS location with custom name.
 * 
 * Story 7.1: Provides accessible UI for location saving with:
 * - GPS coordinate retrieval from LocationManager
 * - Name input validation (min 2 characters)
 * - Duplicate name detection with overwrite confirmation
 * - Voice input button (placeholder for future Epic 3 integration)
 * - TalkBack announcements for accessibility
 * 
 * All touch targets are 48×48 dp minimum for accessibility compliance.
 * High-contrast mode compatible (7:1 contrast ratio).
 * 
 * @property repository Saved location repository for database operations
 * @property locationManager GPS location manager
 * @property ttsManager TTS manager for announcements
 */
@AndroidEntryPoint
class SaveLocationDialogFragment : DialogFragment() {
    
    @Inject
    lateinit var repository: SavedLocationRepository
    
    @Inject
    lateinit var locationManager: LocationManager
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private var _binding: DialogSaveLocationBinding? = null
    private val binding get() = _binding!!
    
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    
    companion object {
        private const val TAG = "SaveLocationDialog"
        private const val MIN_NAME_LENGTH = 2
        
        /**
         * Note: Hilt requires no-argument constructor for DialogFragment injection.
         * Use direct instantiation: SaveLocationDialogFragment()
         * Do NOT use factory methods like newInstance() - they bypass Hilt injection.
         */
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSaveLocationBinding.inflate(LayoutInflater.from(requireContext()))
        
        setupViews()
        fetchCurrentLocation()
        
        // Announce dialog title for TalkBack (AC #1)
        lifecycleScope.launch {
            ttsManager.announce(getString(R.string.save_location_announcement))
        }
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    private fun setupViews() {
        // Setup name input with real-time validation
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateNameInput(s?.toString() ?: "")
            }
        })
        
        // Setup voice input button (placeholder for Epic 3 integration)
        binding.voiceInputButton.setOnClickListener {
            lifecycleScope.launch {
                ttsManager.announce("Voice input feature coming soon. Please type location name.")
            }
            Timber.tag(TAG).d("Voice input button clicked (placeholder)")
        }
        
        // Setup save button
        binding.saveButton.setOnClickListener {
            handleSaveButtonClick()
        }
        
        // Setup cancel button
        binding.cancelButton.setOnClickListener {
            Timber.tag(TAG).d("Save location cancelled by user")
            dismiss()
        }
        
        // Initial state: Save button disabled until valid name entered (AC #5)
        binding.saveButton.isEnabled = false
    }
    
    private fun validateNameInput(name: String) {
        val trimmedName = name.trim()
        
        when {
            trimmedName.isEmpty() -> {
                binding.nameInputLayout.error = null
                binding.saveButton.isEnabled = false
            }
            trimmedName.length < MIN_NAME_LENGTH -> {
                binding.nameInputLayout.error = getString(R.string.location_name_too_short)
                binding.saveButton.isEnabled = false
            }
            else -> {
                binding.nameInputLayout.error = null
                // Enable save button only if we have GPS coordinates
                binding.saveButton.isEnabled = (currentLatitude != null && currentLongitude != null)
            }
        }
    }
    
    private fun fetchCurrentLocation() {
        // Show loading indicator (AC #8)
        binding.loadingLayout.isVisible = true
        lifecycleScope.launch {
            ttsManager.announce(getString(R.string.getting_location))
            
            val result = locationManager.getCurrentLocation()
            
            binding.loadingLayout.isVisible = false
            
            result.fold(
                onSuccess = { latLng ->
                    currentLatitude = latLng.latitude
                    currentLongitude = latLng.longitude
                    Timber.tag(TAG).d("GPS location retrieved: $latLng")
                    
                    // Re-validate name to enable Save button if name is valid
                    validateNameInput(binding.nameEditText.text?.toString() ?: "")
                },
                onFailure = { error ->
                    handleLocationError(error)
                }
            )
        }
    }
    
    private fun handleLocationError(error: Throwable) {
        Timber.tag(TAG).e(error, "Failed to get current location")
        
        val (title, message) = when (error) {
            is LocationError.PermissionDenied -> {
                Pair(
                    getString(R.string.location_permission_title),
                    getString(R.string.location_permission_required_for_save)
                )
            }
            is LocationError.GpsDisabled -> {
                Pair(
                    getString(R.string.gps_unavailable_title),
                    getString(R.string.gps_unavailable_message)
                )
            }
            else -> {
                Pair(
                    getString(R.string.gps_unavailable_title),
                    getString(R.string.gps_unavailable_message)
                )
            }
        }
        
        // Show error dialog (AC #3, #4)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }
            .show()
        
        lifecycleScope.launch {
            ttsManager.announce("$title. $message")
        }
    }
    
    private fun handleSaveButtonClick() {
        val name = binding.nameEditText.text?.toString()?.trim() ?: ""
        val latitude = currentLatitude
        val longitude = currentLongitude
        
        // Validate inputs
        if (name.length < MIN_NAME_LENGTH) {
            lifecycleScope.launch {
                ttsManager.announce(getString(R.string.location_name_too_short))
            }
            return
        }
        
        if (latitude == null || longitude == null) {
            lifecycleScope.launch {
                ttsManager.announce(getString(R.string.gps_unavailable_message))
            }
            return
        }
        
        // Disable save button to prevent double-click
        binding.saveButton.isEnabled = false
        
        lifecycleScope.launch {
            val result = repository.saveLocation(name, latitude, longitude)
            
            result.fold(
                onSuccess = { locationId ->
                    Timber.tag(TAG).d("Location saved successfully: $name (id=$locationId)")
                    
                    // Announce success (AC #10)
                    val successMessage = getString(R.string.location_saved, name)
                    ttsManager.announce(successMessage)
                    
                    dismiss()
                },
                onFailure = { error ->
                    handleSaveError(name, latitude, longitude, error)
                }
            )
        }
    }
    
    private fun handleSaveError(
        name: String,
        latitude: Double,
        longitude: Double,
        error: Throwable
    ) {
        when (error) {
            is DuplicateLocationException -> {
                // Show duplicate confirmation dialog (AC #6)
                showDuplicateConfirmationDialog(name, latitude, longitude)
            }
            else -> {
                Timber.tag(TAG).e(error, "Failed to save location: $name")
                lifecycleScope.launch {
                    ttsManager.announce(getString(R.string.save_location_error))
                }
                
                // Re-enable save button for retry
                binding.saveButton.isEnabled = true
            }
        }
    }
    
    private fun showDuplicateConfirmationDialog(
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        val message = getString(R.string.duplicate_location_message, name)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.duplicate_location_title)
            .setMessage(message)
            .setPositiveButton(R.string.overwrite) { dialog, _ ->
                dialog.dismiss()
                handleOverwriteLocation(name, latitude, longitude)
            }
            .setNegativeButton(R.string.choose_different) { dialog, _ ->
                dialog.dismiss()
                // Re-enable save button and keep dialog open for new name
                binding.saveButton.isEnabled = true
                binding.nameEditText.requestFocus()
            }
            .show()
        
        lifecycleScope.launch {
            ttsManager.announce("$message. Overwrite or choose different?")
        }
    }
    
    private fun handleOverwriteLocation(
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        lifecycleScope.launch {
            // Find existing location
            val existing = repository.findLocationByName(name)
            
            if (existing != null) {
                // Update existing location with new coordinates (atomic operation, no race condition)
                val currentTime = System.currentTimeMillis()
                val updated = existing.copy(
                    latitude = latitude,
                    longitude = longitude,
                    lastUsedAt = currentTime
                )
                
                val updateResult = repository.updateLocation(updated)
                
                updateResult.fold(
                    onSuccess = {
                        Timber.tag(TAG).d("Location overwritten: $name (id=${existing.id})")
                        
                        val successMessage = getString(R.string.location_saved, name)
                        ttsManager.announce(successMessage)
                        
                        dismiss()
                    },
                    onFailure = { updateError ->
                        Timber.tag(TAG).e(updateError, "Failed to update existing location")
                        ttsManager.announce(getString(R.string.save_location_error))
                        binding.saveButton.isEnabled = true
                    }
                )
            } else {
                // Existing location not found (race condition?), try save again
                handleSaveButtonClick()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
