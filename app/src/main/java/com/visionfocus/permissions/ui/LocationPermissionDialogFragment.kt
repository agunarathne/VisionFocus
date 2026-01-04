package com.visionfocus.permissions.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.databinding.DialogLocationPermissionRationaleBinding
import com.visionfocus.tts.engine.TTSManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Location Permission Rationale Dialog
 * Story 6.5: GPS Location Permissions with Clear Explanations
 * 
 * Displays clear explanation of why location permission is needed before
 * showing system permission dialog. Full TalkBack accessibility with proper
 * focus order and TTS announcements.
 * 
 * Features:
 * - Material Design 3 dialog with CardView layout
 * - TalkBack focus order: title → message → Allow → Not Now
 * - TTS announcement of rationale text when dialog appears
 * - Callback interface for permission decision (allow/deny)
 * - Cancellation handled as denial (back button)
 */
@AndroidEntryPoint
class LocationPermissionDialogFragment : DialogFragment() {
    
    /**
     * Callback interface for permission dialog decisions.
     * Implement in hosting fragment/activity to handle user's choice.
     */
    interface PermissionDialogListener {
        /**
         * Called when user taps "Allow" button.
         * Host should then launch the system permission request.
         */
        fun onAllowClicked()
        
        /**
         * Called when user taps "Not Now" button or dismisses dialog.
         * Host should update UI to show permission-denied state.
         */
        fun onDenyClicked()
    }
    
    private var _binding: DialogLocationPermissionRationaleBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    private lateinit var listener: PermissionDialogListener
    
    // Use CoroutineScope for TTS announcements since DialogFragment doesn't have lifecycleScope
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLocationPermissionRationaleBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        
        setupButtons()
        announceTTSRationale()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(true) // Allow back button dismissal
            .create()
    }
    
    private fun setupButtons() {
        binding.allowButton.setOnClickListener {
            listener.onAllowClicked()
            dismiss()
        }
        
        binding.notNowButton.setOnClickListener {
            listener.onDenyClicked()
            dismiss()
        }
    }
    
    /**
     * Announce rationale text via TTS when dialog appears.
     * Uses HIGH priority to ensure announcement is heard immediately.
     */
    private fun announceTTSRationale() {
        coroutineScope.launch {
            ttsManager.announce(getString(R.string.location_permission_rationale))
        }
    }
    
    /**
     * Handle dialog cancellation (back button) as denial.
     */
    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)
        listener.onDenyClicked()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        /**
         * Factory method to create dialog with listener.
         * Must be called before showing dialog.
         * 
         * @param listener Callback for permission decision
         * @return Configured LocationPermissionDialogFragment
         */
        fun newInstance(listener: PermissionDialogListener): LocationPermissionDialogFragment {
            return LocationPermissionDialogFragment().apply {
                this.listener = listener
            }
        }
    }
}
