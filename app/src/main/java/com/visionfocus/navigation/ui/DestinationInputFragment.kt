package com.visionfocus.navigation.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.databinding.FragmentDestinationInputBinding
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.recognizer.VoiceRecognitionManager
import com.visionfocus.voice.recognizer.VoiceRecognitionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Destination input screen for navigation feature.
 * 
 * Story 6.1: Destination Input via Voice and Text
 */
@AndroidEntryPoint
class DestinationInputFragment : Fragment() {
    
    private var _binding: FragmentDestinationInputBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DestinationInputViewModel by viewModels()
    
    @Inject
    lateinit var voiceRecognitionManager: VoiceRecognitionManager
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    @Inject
    lateinit var hapticFeedbackManager: com.visionfocus.accessibility.haptic.HapticFeedbackManager
    
    companion object {
        private const val TAG = "DestinationInputFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDestinationInputBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAccessibility()
        setupTextInput()
        setupVoiceInput()
        setupGoButton()
        setupBackButton()
        observeViewModel()
        
        // Initial TalkBack announcement
        view.post {
            lifecycleScope.launch {
                ttsManager.announce(getString(R.string.destination_input_title))
            }
        }
    }
    
    private fun setupAccessibility() {
        binding.destinationEditText.contentDescription = getString(R.string.destination_input_field_description)
        binding.destinationEditText.nextFocusDownId = binding.goButton.id
    }
    
    private fun setupTextInput() {
        binding.destinationEditText.addTextChangedListener { text ->
            viewModel.destinationText.value = text?.toString() ?: ""
        }
        
        viewModel.destinationText.observe(viewLifecycleOwner) { text ->
            if (binding.destinationEditText.text?.toString() != text) {
                binding.destinationEditText.setText(text)
                binding.destinationEditText.setSelection(text?.length ?: 0)
            }
        }
        
        binding.destinationEditText.setOnEditorActionListener { _, _, _ ->
            viewModel.onGoClicked()
            true
        }
    }
    
    private fun setupVoiceInput() {
        binding.destinationInputLayout.setEndIconOnClickListener {
            Log.d(TAG, "Microphone button clicked - starting voice recognition")
            
            // Haptic feedback on voice button press (Story 2.3 pattern)
            lifecycleScope.launch {
                hapticFeedbackManager.trigger(HapticPattern.ButtonPress)
            }
            
            // Story 6.1: Full voice input integration with VoiceRecognitionManager
            // Set callbacks for result and error handling
            voiceRecognitionManager.setOnRecognizedTextCallback { transcribedText ->
                Log.d(TAG, "Voice input result: $transcribedText")
                viewModel.onVoiceInputComplete(transcribedText)
                
                // Restore focus to text field after voice input
                binding.destinationEditText.requestFocus()
            }
            
            voiceRecognitionManager.setOnStateChangeCallback { state ->
                if (state is VoiceRecognitionState.Error) {
                    Log.e(TAG, "Voice recognition error: ${state.errorCode}")
                    lifecycleScope.launch {
                        ttsManager.announce("Didn't catch that. Please try again.")
                    }
                    
                    // Restore focus to text field after error
                    binding.destinationEditText.requestFocus()
                }
            }
            
            // Start listening
            voiceRecognitionManager.startListening()
        }
    }
    
    private fun setupGoButton() {
        binding.goButton.setOnClickListener {
            Log.d(TAG, "Go button clicked")
            viewModel.onGoClicked()
        }
    }
    
    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        )
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validationState.collect { state ->
                    updateUIForValidationState(state)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isValidating.collect { isValidating ->
                    binding.validationProgressIndicator.visibility = 
                        if (isValidating) View.VISIBLE else View.GONE
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }
    
    private fun updateUIForValidationState(state: ValidationResult) {
        when (state) {
            is ValidationResult.Empty -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = null
            }
            is ValidationResult.TooShort -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = getString(R.string.destination_too_short)
            }
            is ValidationResult.Valid -> {
                binding.goButton.isEnabled = true
                binding.destinationInputLayout.error = null
            }
            is ValidationResult.Ambiguous -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = null
            }
            is ValidationResult.Invalid -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = getString(R.string.invalid_destination, state.reason)
            }
        }
    }
    
    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.StartNavigation -> {
                Log.d(TAG, "Starting navigation to: ${event.destination.name}")
                
                lifecycleScope.launch {
                    ttsManager.announce("Navigation feature will be implemented in Story 6.3")
                }
            }
            is NavigationEvent.ShowClarificationDialog -> {
                showClarificationDialog(event.options)
            }
        }
    }
    
    private fun showClarificationDialog(options: List<Destination>) {
        // MEDIUM-3 FIX: Validate options list before showing dialog
        if (options.isEmpty()) {
            Log.w(TAG, "Empty options list for clarification - cannot show dialog")
            lifecycleScope.launch {
                ttsManager.announce("No destination options available. Please try again.")
            }
            return
        }
        
        val optionNames = options.map { it.formattedAddress ?: it.name }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clarification_dialog_title)
            .setItems(optionNames) { dialog, which ->
                val selectedDestination = options[which]
                viewModel.onClarificationSelected(selectedDestination)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
