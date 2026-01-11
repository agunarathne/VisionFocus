package com.visionfocus.ui.voice

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.visionfocus.R
import com.visionfocus.voice.processor.VoiceCommandProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCommandsFragment : Fragment(R.layout.fragment_voice_commands) {

    @Inject
    lateinit var voiceCommandProcessor: VoiceCommandProcessor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Helper function to setup button click listeners
        fun setupButton(id: Int, command: String) {
            view.findViewById<Button>(id)?.setOnClickListener {
                lifecycleScope.launch {
                    voiceCommandProcessor.processCommand(command)
                }
            }
        }

        // Recognition Commands
        setupButton(R.id.btn_recognize, "Recognize")
        setupButton(R.id.btn_what_do_i_see, "What do I see")
        setupButton(R.id.btn_repeat, "Repeat")

        // Navigation Commands
        setupButton(R.id.btn_navigate, "Navigate")
        setupButton(R.id.btn_where_am_i, "Where am I")
        setupButton(R.id.btn_save_location, "Save location")
        setupButton(R.id.btn_saved_locations, "Saved Locations")
        setupButton(R.id.btn_cancel, "Cancel")

        // Indoor Navigation (Beacon)
        setupButton(R.id.btn_find_keys, "Find keys")
        setupButton(R.id.btn_locate_backpack, "Locate backpack")

        // Settings Commands
        setupButton(R.id.btn_settings, "Settings")
        setupButton(R.id.btn_high_contrast_on, "High contrast on")
        setupButton(R.id.btn_high_contrast_off, "High contrast off")
        setupButton(R.id.btn_large_text_on, "Large text on")
        setupButton(R.id.btn_large_text_off, "Large text off")
        setupButton(R.id.btn_increase_speed, "Increase speed")
        setupButton(R.id.btn_decrease_speed, "Decrease speed")
        
        setupButton(R.id.btn_verbosity_brief, "Verbosity brief")
        setupButton(R.id.btn_verbosity_standard, "Verbosity standard")
        setupButton(R.id.btn_verbosity_detailed, "Verbosity detailed")
        
        setupButton(R.id.btn_haptic_off, "Haptic off")
        setupButton(R.id.btn_haptic_medium, "Haptic medium")

        // General Commands
        setupButton(R.id.btn_history, "History")
        setupButton(R.id.btn_back, "Back")
        setupButton(R.id.btn_home, "Home")
    }
}

