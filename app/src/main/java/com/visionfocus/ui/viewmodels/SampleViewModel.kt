package com.visionfocus.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.visionfocus.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Sample ViewModel demonstrating Hilt injection with MVVM pattern.
 * 
 * @HiltViewModel enables Hilt to provide ViewModel instances
 * @Inject constructor tells Hilt to inject dependencies automatically
 * 
 * ViewModel lifecycle is managed by ViewModelComponent (survives configuration changes)
 */
@HiltViewModel
class SampleViewModel @Inject constructor(
    private val sampleRepository: SampleRepository
) : ViewModel() {
    
    /**
     * Gets sample data from repository.
     * 
     * Demonstrates dependency injection chain:
     * Activity -> ViewModel -> Repository
     */
    fun getSampleData(): String {
        return sampleRepository.getSampleData()
    }
    
    // Future ViewModels will manage UI state with StateFlow/SharedFlow
    // Example: RecognitionViewModel, NavigationViewModel, SettingsViewModel
}
