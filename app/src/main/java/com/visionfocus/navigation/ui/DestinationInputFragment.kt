package com.visionfocus.navigation.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.visionfocus.R
import com.visionfocus.databinding.FragmentDestinationInputBinding
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.accessibility.haptic.HapticPattern
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.permissions.ui.LocationPermissionDialogFragment
import com.visionfocus.permissions.utils.PermissionSettingsLauncher
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.recognizer.VoiceRecognitionManager
import com.visionfocus.voice.recognizer.VoiceRecognitionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
    
    @Inject
    lateinit var networkConsentManager: com.visionfocus.navigation.consent.NetworkConsentManager
    
    @Inject
    lateinit var permissionManager: PermissionManager  // Story 6.5
    
    // Story 6.5: Location permission launcher
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    
    // HIGH-7 FIX: Track if returning from settings to optimize permission checks
    private var isReturningFromSettings = false
    
    companion object {
        private const val TAG = "DestinationInputFragment"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Story 6.5: Register permission launcher in onCreate (before view creation)
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleLocationPermissionResult(isGranted)
        }
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
        setupPermissionDeniedUI()  // Story 6.5
        observeViewModel()
        
        // Story 6.5: Check permission state on launch
        updateUIForPermissionState()
        
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
            val query = text?.toString() ?: ""
            viewModel.destinationText.value = query
            
            // AUTO-VALIDATE: Trigger validation as user types (Story 6.1 AC #6)
            viewModel.validateDestination(query)
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
            
            // Story 6.5: Check location permission before starting navigation
            if (permissionManager.isLocationPermissionGranted()) {
                viewModel.onGoClicked()
            } else {
                requestLocationPermissionWithRationale()
            }
        }
    }
    
    /**
     * Story 6.5: Request location permission with rationale dialog.
     * Shows rationale if user previously denied, otherwise launches directly.
     */
    private fun requestLocationPermissionWithRationale() {
        if (permissionManager.shouldShowLocationRationale(requireActivity())) {
            // Show rationale dialog before system permission prompt
            LocationPermissionDialogFragment.newInstance(
                object : LocationPermissionDialogFragment.PermissionDialogListener {
                    override fun onAllowClicked() {
                        locationPermissionLauncher.launch(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    
                    override fun onDenyClicked() {
                        handleLocationPermissionResult(false)
                    }
                }
            ).show(parentFragmentManager, "location_rationale")
        } else {
            // First-time request, launch system dialog directly
            locationPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    /**
     * Story 6.5: Handle location permission result from system dialog.
     * Updates ViewModel state, announces result via TTS, and updates UI.
     */
    private fun handleLocationPermissionResult(isGranted: Boolean) {
        viewModel.updateLocationPermissionState(isGranted)
        
        // Story 6.5 AC #7: Add 1-second delay before TTS to avoid collision with system dialog
        lifecycleScope.launch {
            delay(1000)
            
            if (isGranted) {
                // Permission granted - proceed with navigation
                viewModel.onGoClicked()
            } else {
                // Permission denied - show denied UI
                showPermissionDeniedUI()
            }
            
            updateUIForPermissionState()
        }
    }
    
    /**
     * Story 6.5: Setup permission denied UI elements.
     */
    private fun setupPermissionDeniedUI() {
        binding.openSettingsButton.setOnClickListener {
            // HIGH-7: Set flag so onResume knows to re-check permission
            isReturningFromSettings = true
            PermissionSettingsLauncher.openAppSettings(requireContext())
        }
    }
    
    /**
     * Story 6.5: Show permission denied message and settings button.
     */
    private fun showPermissionDeniedUI() {
        binding.permissionDeniedTextView.visibility = View.VISIBLE
        binding.openSettingsButton.visibility = View.VISIBLE
    }
    
    /**
     * Story 6.5: Update UI based on current permission state.
     * Called on launch and after permission changes.
     * 
     * HIGH-8 FIX: Added TTS announcement when permission state changes
     * HIGH-10 FIX: Button properly disabled when permission denied OR validation failed
     */
    private fun updateUIForPermissionState() {
        val isGranted = permissionManager.isLocationPermissionGranted()
        val currentValidation = viewModel.validationState.value
        
        if (isGranted) {
            // Permission granted - enable navigation
            binding.permissionDeniedTextView.visibility = View.GONE
            binding.openSettingsButton.visibility = View.GONE
            binding.goButton.text = getString(R.string.go_button_text)
            
            // HIGH-10 FIX: Button enabled state depends on BOTH permission AND validation
            binding.goButton.isEnabled = currentValidation is ValidationResult.Valid
            
            // HIGH-8 FIX: Announce when permission becomes granted (returning from settings)
            if (isReturningFromSettings) {
                lifecycleScope.launch {
                    ttsManager.announce("Location enabled. Navigation ready.")
                }
            }
        } else {
            // Permission denied - show message and disable navigation
            binding.permissionDeniedTextView.visibility = View.VISIBLE
            binding.openSettingsButton.visibility = View.VISIBLE
            binding.goButton.text = getString(R.string.enable_location_to_navigate)
            
            // HIGH-10 FIX: Keep button enabled to allow showing rationale dialog
            // But only if destination is valid
            binding.goButton.isEnabled = currentValidation is ValidationResult.Valid
        }
    }
    
    override fun onResume() {
        super.onResume()
        // HIGH-7 FIX: Only re-check permission if returning from settings
        // Avoids unnecessary checks when returning from voice input or other screens
        if (isReturningFromSettings) {
            updateUIForPermissionState()
            viewModel.checkLocationPermission()
            isReturningFromSettings = false
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
        
        // Story 6.2: Observe navigation state for route downloading
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationState.collect { state ->
                    handleNavigationState(state)
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
        
        // MEDIUM-4 FIX: Observe permission state StateFlow reactively
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLocationPermissionGranted.collect { isGranted ->
                    // Update UI when permission state changes
                    updateUIForPermissionState()
                }
            }
        }
    }
    
    private fun updateUIForValidationState(state: ValidationResult) {
        when (state) {
            is ValidationResult.Empty -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = null
                binding.destinationInputLayout.isErrorEnabled = false
            }
            is ValidationResult.TooShort -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.isErrorEnabled = true
                binding.destinationInputLayout.error = getString(R.string.destination_too_short)
                Log.d(TAG, "Showing TooShort error: ${getString(R.string.destination_too_short)}")
            }
            is ValidationResult.Valid -> {
                binding.goButton.isEnabled = true
                binding.destinationInputLayout.error = null
                binding.destinationInputLayout.isErrorEnabled = false
                Log.d(TAG, "Go button ENABLED - Valid destination")
            }
            is ValidationResult.Ambiguous -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.error = null
                binding.destinationInputLayout.isErrorEnabled = false
            }
            is ValidationResult.Invalid -> {
                binding.goButton.isEnabled = false
                binding.destinationInputLayout.isErrorEnabled = true
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
            is NavigationEvent.ShowNetworkConsentDialog -> {
                showNetworkConsentDialog()
            }
        }
    }
    
    /**
     * Story 6.2: Handle navigation state changes (route downloading, success, errors).
     */
    private fun handleNavigationState(state: NavigationState) {
        when (state) {
            is NavigationState.Idle -> {
                binding.routeProgressIndicator.visibility = View.GONE
            }
            is NavigationState.RequestingRoute -> {
                binding.routeProgressIndicator.visibility = View.VISIBLE
                binding.goButton.isEnabled = false
            }
            is NavigationState.RouteReady -> {
                binding.routeProgressIndicator.visibility = View.GONE
                binding.goButton.isEnabled = true
                // Story 6.3: Navigate to NavigationActiveFragment with route
                Log.d(TAG, "Route ready: ${state.route.steps.size} steps, ${state.route.totalDistance}m")
                
                // Navigate using Safe Args to pass route
                val action = DestinationInputFragmentDirections
                    .actionDestinationInputToNavigationActive(state.route)
                findNavController().navigate(action)
            }
            is NavigationState.Error -> {
                binding.routeProgressIndicator.visibility = View.GONE
                binding.goButton.isEnabled = true
                showErrorDialog(state.message)
            }
        }
    }
    
    /**
     * Story 6.2: Show network consent dialog.
     */
    private fun showNetworkConsentDialog() {
        val consentDialog = com.visionfocus.navigation.consent.NetworkConsentDialog()
        consentDialog.onConsentDecision = { granted ->
            // BUG FIX: Save consent decision to DataStore before proceeding
            lifecycleScope.launch {
                networkConsentManager.setConsent(granted)
                if (granted) {
                    viewModel.onNetworkConsentGranted()
                }
            }
        }
        consentDialog.show(parentFragmentManager, "network_consent")
    }
    
    /**
     * Story 6.2: Show error dialog with retry option.
     */
    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Navigation Error")
            .setMessage(message)
            .setPositiveButton("Retry") { dialog, _ ->
                viewModel.onGoClicked()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
