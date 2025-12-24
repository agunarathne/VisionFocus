package com.visionfocus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating Hilt dependency injection setup.
 * 
 * These are lightweight tests that verify the DI structure is correct
 * without requiring Android runtime or Hilt container.
 * 
 * Full end-to-end Hilt injection with Android lifecycle is tested
 * in instrumentation tests (HiltIntegrationTest.kt).
 */
class HiltDependencyInjectionTest {
    
    @Test
    fun `hilt dependency injection annotations are present`() {
        // Verify that the required Hilt annotations exist in the codebase
        // This test ensures the project structure is correct for DI
        
        // VisionFocusApplication should have @HiltAndroidApp
        val appClass = Class.forName("com.visionfocus.VisionFocusApplication")
        val hiltAppAnnotation = appClass.annotations.any { 
            it.annotationClass.simpleName == "HiltAndroidApp" 
        }
        assertTrue("VisionFocusApplication should be annotated with @HiltAndroidApp", hiltAppAnnotation)
    }
    
    @Test
    fun `repository classes can be loaded`() {
        // Verify repository classes exist and can be loaded
        val repositoryInterface = Class.forName("com.visionfocus.data.repository.SampleRepository")
        val repositoryImpl = Class.forName("com.visionfocus.data.repository.SampleRepositoryImpl")
        
        assertTrue("SampleRepository interface should be loadable", repositoryInterface.isInterface)
        assertTrue("SampleRepositoryImpl should implement SampleRepository", 
            repositoryInterface.isAssignableFrom(repositoryImpl))
    }
    
    @Test
    fun `viewmodel class can be loaded`() {
        // Verify ViewModel class exists and extends ViewModel
        val viewModelClass = Class.forName("com.visionfocus.ui.viewmodels.SampleViewModel")
        val androidViewModelClass = Class.forName("androidx.lifecycle.ViewModel")
        
        assertTrue("SampleViewModel should extend ViewModel",
            androidViewModelClass.isAssignableFrom(viewModelClass))
    }
    
    @Test
    fun `hilt modules can be loaded`() {
        // Verify Hilt modules exist
        val appModule = Class.forName("com.visionfocus.di.AppModule")
        val repositoryModule = Class.forName("com.visionfocus.di.RepositoryModule")
        
        val appModuleAnnotations = appModule.annotations.any { 
            it.annotationClass.simpleName == "Module" 
        }
        val repoModuleAnnotations = repositoryModule.annotations.any { 
            it.annotationClass.simpleName == "Module" 
        }
        
        assertTrue("AppModule should be annotated with @Module", appModuleAnnotations)
        assertTrue("RepositoryModule should be annotated with @Module", repoModuleAnnotations)
    }
}




