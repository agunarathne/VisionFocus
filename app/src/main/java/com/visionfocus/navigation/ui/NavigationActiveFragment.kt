package com.visionfocus.navigation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.visionfocus.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Navigation Active screen - placeholder for Story 6.3.
 * 
 * Story 6.1: Placeholder fragment for navigation action
 * Story 6.3: Full turn-by-turn navigation implementation
 */
@AndroidEntryPoint
class NavigationActiveFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Story 6.3 will implement full navigation UI
        return inflater.inflate(R.layout.fragment_navigation_active, container, false)
    }
}
