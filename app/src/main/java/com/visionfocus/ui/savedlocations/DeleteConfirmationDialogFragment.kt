package com.visionfocus.ui.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.visionfocus.R
import com.visionfocus.databinding.DialogDeleteConfirmationBinding

/**
 * Dialog fragment for delete confirmation.
 * 
 * Story 7.2 AC8: Delete confirmation dialog with warning
 * 
 * Features:
 * - Warning message: "Delete [location name]? This cannot be undone."
 * - Destructive styling on Delete button (red color)
 * - Cancel button for safe dismissal
 */
class DeleteConfirmationDialogFragment : DialogFragment() {
    
    private var _binding: DialogDeleteConfirmationBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val ARG_LOCATION = "location"
        
        fun newInstance(location: SavedLocationUiModel): DeleteConfirmationDialogFragment {
            return DeleteConfirmationDialogFragment().apply {
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
        _binding = DialogDeleteConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val location = arguments?.getParcelable<SavedLocationUiModel>(ARG_LOCATION)
            ?: throw IllegalStateException("Location required")
        
        setupDialog(location)
    }
    
    /**
     * Setup dialog content and buttons.
     * Story 7.2 AC8: Warning message and destructive styling
     */
    private fun setupDialog(location: SavedLocationUiModel) {
        // Story 7.2 AC8: Warning message with location name
        binding.messageText.text = getString(R.string.delete_confirmation_message, location.name)
        
        // Story 7.2 AC8: Delete button (destructive styling)
        binding.deleteButton.apply {
            contentDescription = getString(R.string.delete_location_confirm_description, location.name)
            setOnClickListener {
                (parentFragment as? SavedLocationsFragment)?.onDeleteConfirmed(location)
                dismiss()
            }
        }
        
        // Cancel button
        binding.cancelButton.apply {
            contentDescription = getString(R.string.cancel_description)
            setOnClickListener {
                dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
