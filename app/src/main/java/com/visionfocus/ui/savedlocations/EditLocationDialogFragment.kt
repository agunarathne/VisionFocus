package com.visionfocus.ui.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.visionfocus.R
import com.visionfocus.databinding.DialogEditLocationBinding

/**
 * Dialog fragment for editing location name.
 * 
 * Story 7.2 AC7: Edit location name with validation
 * 
 * Features:
 * - Pre-filled EditText with current location name
 * - Voice input button (stub for Epic 3 integration)
 * - Validation: minimum 2 characters, not empty
 * - Save and Cancel buttons with proper TalkBack support
 */
class EditLocationDialogFragment : DialogFragment() {
    
    private var _binding: DialogEditLocationBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val ARG_LOCATION_ID = "locationId"
        private const val ARG_CURRENT_NAME = "currentName"
        
        fun newInstance(locationId: Long, currentName: String): EditLocationDialogFragment {
            return EditLocationDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_LOCATION_ID, locationId)
                    putString(ARG_CURRENT_NAME, currentName)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditLocationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val locationId = arguments?.getLong(ARG_LOCATION_ID) 
            ?: throw IllegalStateException("Location ID required")
        val currentName = arguments?.getString(ARG_CURRENT_NAME) 
            ?: throw IllegalStateException("Current name required")
        
        setupEditText(currentName)
        setupButtons(locationId)
    }
    
    /**
     * Setup EditText with current location name.
     * Story 7.2 AC7: Pre-fill with current name, cursor at end
     */
    private fun setupEditText(currentName: String) {
        binding.nameEditText.apply {
            setText(currentName)
            setSelection(currentName.length)  // Cursor at end
            contentDescription = getString(R.string.location_name_edit_description)
            requestFocus()
        }
        
        // Show keyboard
        binding.nameEditText.postDelayed({
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) 
                as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.nameEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }
    
    /**
     * Setup button click listeners.
     * Story 7.2 AC7: Save/Cancel actions
     */
    private fun setupButtons(locationId: Long) {
        // Story 7.2 AC7: Voice input button (stub for Epic 3)
        binding.voiceInputButton.apply {
            contentDescription = getString(R.string.voice_input_description)
            setOnClickListener {
                // TODO: Integrate with voice recognition from Epic 3
                Toast.makeText(context, "Voice input coming in Epic 3", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Save button
        binding.saveButton.apply {
            contentDescription = getString(R.string.save_location_name_description)
            setOnClickListener {
                val newName = binding.nameEditText.text.toString().trim()
                
                // Validation
                if (newName.isBlank() || newName.length < 2) {
                    binding.nameEditText.error = getString(R.string.name_too_short_error)
                    binding.nameEditText.announceForAccessibility(getString(R.string.name_too_short_error))
                    return@setOnClickListener
                }
                
                // Code Review Fix: Hide keyboard before dismiss
                hideKeyboard()
                
                // Notify parent fragment
                (parentFragment as? SavedLocationsFragment)?.onLocationNameUpdated(locationId, newName)
                dismiss()
            }
        }
        
        // Cancel button
        binding.cancelButton.apply {
            contentDescription = getString(R.string.cancel_description)
            setOnClickListener {
                // Code Review Fix: Hide keyboard before dismiss
                hideKeyboard()
                dismiss()
            }
        }
    }
    
    /**
     * Hide soft keyboard.
     * Code Review Fix: Explicit keyboard management.
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) 
            as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.nameEditText.windowToken, 0)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
