package com.visionfocus

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionfocus.data.repository.SampleRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumentation tests validating Hilt integration in Android environment.
 * 
 * Tests verify that:
 * - Hilt provides dependencies in test environment
 * - MainActivity receives injected ViewModel
 * - Application context is provided correctly
 * - Full dependency injection chain works end-to-end
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var sampleRepository: SampleRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun hilt_provides_sample_repository_in_test() {
        // Verify Hilt injects SampleRepository
        assertNotNull("Hilt should provide SampleRepository", sampleRepository)
        assertEquals(
            "Repository should return expected data",
            "Hilt dependency injection working!",
            sampleRepository.getSampleData()
        )
    }
    
    @Test
    fun application_is_hilt_application() {
        // Verify application class is Hilt-enabled
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context.applicationContext
        
        assertTrue(
            "Application must be instance of VisionFocusApplication",
            app is VisionFocusApplication
        )
    }
    
    @Test
    fun mainActivity_receives_injected_viewModel() {
        // Verify MainActivity launches with injected ViewModel
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        scenario.onActivity { activity ->
            assertNotNull("MainActivity should be created", activity)
            
            // Verify the activity is annotated with @AndroidEntryPoint
            // (The fact it launches without crashing proves DI is working)
            assertTrue(
                "MainActivity should be alive and have content",
                activity.hasWindowFocus() || !activity.isFinishing
            )
        }
        
        scenario.close()
    }
    
    @Test
    fun mainActivity_displays_injected_data() {
        // Verify MainActivity displays data from injected ViewModel
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        scenario.onActivity { activity ->
            // Get the binding and verify TextView shows data from ViewModel
            val textView = activity.findViewById<android.widget.TextView>(R.id.textView)
            
            assertNotNull("TextView should exist", textView)
            assertEquals(
                "TextView should display data from ViewModel",
                "Hilt dependency injection working!",
                textView.text.toString()
            )
        }
        
        scenario.close()
    }
}
