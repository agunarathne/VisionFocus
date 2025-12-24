package com.visionfocus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.visionfocus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Demonstrate ViewBinding usage pattern for future stories
        binding.textView.text = getString(R.string.app_name)
        
        // Future stories will add:
        // - Fragment container for navigation (Story 1.2+)
        // - FAB for recognition trigger (Epic 2)
        // - Bottom navigation for main features (Epic 2-3)
    }
}
