package com.visionfocus.navigation.consent

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R

/**
 * Dialog fragment requesting user consent for network access.
 * 
 * Story 6.2: Before first Google Maps Directions API call, user must explicitly
 * grant permission to use internet for downloading navigation directions.
 * 
 * Dialog features:
 * - Clear explanation of why internet is needed
 * - Two buttons: "Allow" (positive) and "Cancel" (negative)
 * - TalkBack content descriptions on all elements
 * - Non-cancelable (user must make explicit choice)
 * - Material Design 3 styling
 */
class NetworkConsentDialog : DialogFragment() {
    
    /**
     * Callback invoked when user makes consent decision.
     * true = allowed network access, false = denied
     */
    var onConsentDecision: ((Boolean) -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.network_consent_title)
            .setMessage(R.string.network_consent_message)
            .setPositiveButton(R.string.network_consent_allow) { _, _ ->
                onConsentDecision?.invoke(true)
            }
            .setNegativeButton(R.string.network_consent_cancel) { _, _ ->
                onConsentDecision?.invoke(false)
            }
            .setCancelable(true)  // Allow back button dismissal
            .create()
            .apply {
                // CODE REVIEW FIX (Issue #7): Handle dialog dismissal to prevent coroutine hang
                setOnCancelListener {
                    onConsentDecision?.invoke(false)
                }
                // Set content descriptions for TalkBack after dialog is shown
                setOnShowListener {
                    val positiveButton = getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = getButton(AlertDialog.BUTTON_NEGATIVE)
                    
                    positiveButton?.contentDescription = 
                        getString(R.string.network_consent_allow_content_description)
                    negativeButton?.contentDescription = 
                        getString(R.string.network_consent_cancel_content_description)
                }
            }
    }
}
