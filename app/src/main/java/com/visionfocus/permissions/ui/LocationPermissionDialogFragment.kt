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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
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
     * 
     * Implement this interface in hosting Fragment to handle user's permission decision.
     * 
     * **Usage Example:**
     * ```kotlin
     * LocationPermissionDialogFragment.newInstance(
     *     object : PermissionDialogListener {
     *         override fun onAllowClicked() {
     *             locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
     *         }
     *         override fun onDenyClicked() {
     *             showPermissionDeniedUI()
     *         }
     *     }
     * ).show(parentFragmentManager, "location_rationale")
     * ```
     * 
     * Story 6.5 AC #1-2: Rationale dialog with TalkBack support before system permission request.
     */
    interface PermissionDialogListener {
        /**
         * Called when user taps "Allow" button.
         * Host should then launch the system permission request.
         */
        fun onAllowClicked()
        
        /**
         * Called when user taps "Not Now" button.
         * Host should update UI to show permission-denied state.
         * 
         * Note: Back button dismissal does NOT trigger this callback (see onCancel).
         */
        fun onDenyClicked()
    }
    
    private var _binding: DialogLocationPermissionRationaleBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    // HIGH-2 FIX: WeakReference prevents memory leak if Activity destroyed before dialog dismissed
    private var listener: WeakReference<PermissionDialogListener>? = null
    
    // Use CoroutineScope for TTS announcements since DialogFragment doesn't have lifecycleScope
    // HIGH-3 FIX: Will be cancelled in onDestroyView to prevent coroutine leak
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLocationPermissionRationaleBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        
        setupButtons()
        // HIGH-4 FIX: TTS moved to onStart() where Hilt injection guaranteed complete
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(true) // Allow back button dismissal
            .create()
    }
    
    private fun setupButtons() {
        binding.allowButton.setOnClickListener {
            listener?.get()?.onAllowClicked()
            dismiss()
        }
        
        binding.notNowButton.setOnClickListener {
            listener?.get()?.onDenyClicked()
            dismiss()
        }
    }
    
    /**
     * HIGH-4 FIX: Announce TTS after Hilt injection complete.
     * Called after onCreateDialog, ensuring ttsManager initialized.
     */
    override fun onStart() {
        super.onStart()
        announceTTSRationale()
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
     * HIGH-9 FIX: Back button dismissal allows retry, doesn't treat as permanent denial.
     * Dialog can be shown again on next "Go" button press.
     */
    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)
        // HIGH-9: Do NOT call onDenyClicked() - back button allows retry
        // User can press "Go" again to show permission rationale
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // HIGH-3 FIX: Cancel coroutine scope to prevent leaks
        coroutineScope.cancel()
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
                // HIGH-2 FIX: Store as WeakReference to prevent memory leak
                this.listener = WeakReference(listener)
            }
        }
    }
}
