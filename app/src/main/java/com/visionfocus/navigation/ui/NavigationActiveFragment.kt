package com.visionfocus.navigation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.visionfocus.R
import com.visionfocus.navigation.models.NavigationRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Story 6.3: Navigation Active screen with turn-by-turn voice guidance.
 * 
 * Displays current navigation instruction, distance/time remaining,
 * and provides cancel button. Updates in real-time based on GPS location.
 * 
 * Accessibility:
 * - Live regions announce progress updates via TalkBack
 * - Large text sizes for visibility
 * - Cancel button with 56×56 dp touch target
 * - Works with screen locked (minimal UI design)
 */
@AndroidEntryPoint
class NavigationActiveFragment : Fragment() {
    
    private val viewModel: NavigationActiveViewModel by viewModels()
    private val args: NavigationActiveFragmentArgs by navArgs()
    
    private lateinit var currentInstructionText: TextView
    private lateinit var distanceRemainingText: TextView
    private lateinit var timeRemainingText: TextView
    private lateinit var cancelNavigationButton: MaterialButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation_active, container, false)
        
        // Bind views
        currentInstructionText = view.findViewById(R.id.currentInstructionText)
        distanceRemainingText = view.findViewById(R.id.distanceRemainingText)
        timeRemainingText = view.findViewById(R.id.timeRemainingText)
        cancelNavigationButton = view.findViewById(R.id.cancelNavigationButton)
        
        // Set up cancel button
        cancelNavigationButton.setOnClickListener {
            viewModel.cancelNavigation()
            // Navigate back to destination input
            findNavController().popBackStack()
        }
        
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Story 6.3 Task 9: Start navigation with route from Safe Args (Story 6.2 → 6.3)
        // Story 6.5: Pass destination name for NavigationService
        if (savedInstanceState == null) {
            viewModel.startNavigation(args.route, args.destinationName)
        }
        
        // Collect state flows
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Current instruction
                launch {
                    viewModel.currentInstruction.collect { instruction ->
                        currentInstructionText.text = instruction
                    }
                }
                
                // Distance remaining
                launch {
                    viewModel.distanceRemaining.collect { distance ->
                        distanceRemainingText.text = distance
                    }
                }
                
                // Time remaining
                launch {
                    viewModel.timeRemaining.collect { time ->
                        timeRemainingText.text = time
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // CRITICAL FIX: Stop navigation service when fragment is destroyed
        // This ensures voice instructions stop when:
        // - User taps X button and navigates away
        // - User presses back button
        // - User switches tabs via bottom navigation
        // - App is killed by user or system
        viewModel.cancelNavigation()
    }
}
