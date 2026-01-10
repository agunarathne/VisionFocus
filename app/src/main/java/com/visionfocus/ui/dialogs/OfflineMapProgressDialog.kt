package com.visionfocus.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.visionfocus.R
import com.visionfocus.navigation.offline.DownloadProgress
import timber.log.Timber

/**
 * Story 7.4: Dialog showing offline map download progress
 * Displays progress bar and status updates during download
 * Announces progress milestones via TTS (25%, 50%, 75%, 100%)
 */
class OfflineMapProgressDialog : DialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var percentText: TextView
    private lateinit var cancelButton: Button
    
    private var tts: TextToSpeech? = null
    private var lastAnnouncedPercent = 0
    private var onCancelListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_offline_map_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        progressBar = view.findViewById(R.id.download_progress_bar)
        statusText = view.findViewById(R.id.download_status_text)
        percentText = view.findViewById(R.id.download_percent_text)
        cancelButton = view.findViewById(R.id.cancel_download_button)
        
        // Initialize TTS
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Timber.d("TTS initialized successfully")
            }
        }
        
        // Setup cancel button
        cancelButton.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
        
        // Make dialog non-cancelable during download
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
    }

    fun updateProgress(progress: DownloadProgress) {
        when (progress) {
            is DownloadProgress.Idle -> {
                progressBar.isIndeterminate = true
                statusText.text = "Starting download..."
                percentText.text = ""
                statusText.contentDescription = "Starting download"
            }
            is DownloadProgress.Preparing -> {
                progressBar.isIndeterminate = true
                statusText.text = "Preparing offline maps..."
                percentText.text = ""
                announceTTS("Preparing offline maps")
                statusText.contentDescription = "Preparing offline maps"
            }
            is DownloadProgress.Downloading -> {
                progressBar.isIndeterminate = false
                progressBar.progress = progress.percent
                statusText.text = progress.getFormattedProgress()
                percentText.text = "${progress.percent}%"
                statusText.contentDescription = "Downloading offline maps. ${progress.percent} percent complete"
                
                // Announce at milestones: 25%, 50%, 75%
                if (progress.percent in listOf(25, 50, 75) && progress.percent != lastAnnouncedPercent) {
                    announceTTS("Downloading offline maps. ${progress.percent} percent complete.")
                    lastAnnouncedPercent = progress.percent
                }
            }
            is DownloadProgress.Complete -> {
                progressBar.progress = 100
                statusText.text = "Offline maps downloaded (${progress.getFormattedSize()})"
                percentText.text = "100%"
                cancelButton.visibility = View.GONE
                statusText.contentDescription = "Offline maps downloaded. You can navigate to ${progress.regionName} without internet."
                
                announceTTS("Offline maps downloaded. You can navigate to ${progress.regionName} without internet.")
                
                // Auto-dismiss after announcement
                view?.postDelayed({ dismiss() }, 2000)
            }
            is DownloadProgress.Error -> {
                progressBar.isIndeterminate = false
                val message = if (progress.isCancelled) {
                    "Download cancelled"
                } else {
                    "Download failed: ${progress.message}"
                }
                statusText.text = message
                percentText.text = ""
                cancelButton.text = "Close"
                statusText.contentDescription = message
                
                announceTTS(message)
                
                // Allow dismissal
                isCancelable = true
            }
        }
    }
    
    private fun announceTTS(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_ADD, null, null)
    }
    
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        tts?.shutdown()
        tts = null
    }

    companion object {
        const val TAG = "OfflineMapProgressDialog"
        
        fun newInstance(locationName: String): OfflineMapProgressDialog {
            return OfflineMapProgressDialog().apply {
                arguments = Bundle().apply {
                    putString("location_name", locationName)
                }
            }
        }
    }
}
