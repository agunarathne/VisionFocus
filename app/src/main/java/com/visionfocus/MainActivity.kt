package com.visionfocus

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.visionfocus.databinding.ActivityMainBinding
import com.visionfocus.ui.viewmodels.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for VisionFocus.
 * 
 * @AndroidEntryPoint enables Hilt dependency injection in this Activity.
 * Required for injecting ViewModels and other dependencies.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Hilt automatically provides ViewModel via viewModels() delegate
    private val sampleViewModel: SampleViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Demonstrate dependency injection is working
        val sampleData = sampleViewModel.getSampleData()
        binding.textView.text = sampleData
        
        // Future stories will add:
        // - Fragment container for navigation (Story 1.2+)
        // - FAB for recognition trigger (Epic 2)
        // - Bottom navigation for main features (Epic 2-3)
    }
}
