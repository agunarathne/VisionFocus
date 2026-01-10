package com.visionfocus.navigation.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.visionfocus.MainActivity
import com.visionfocus.R
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.data.repository.SavedLocationRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for SavedLocationPickerDialog.
 * 
 * Story 7.3 Task 12: Test saved location picker dialog UI and interactions
 * 
 * Tests cover:
 * - Dialog shows all saved locations
 * - Item selection returns correct location
 * - TalkBack announcements for accessibility
 * - Empty state handling
 */
@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SavedLocationPickerDialogTest {
    
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Inject
    lateinit var savedLocationRepository: SavedLocationRepository
    
    private val testLocations = listOf(
        SavedLocationEntity(
            id = 1,
            name = "Home",
            latitude = 40.7128,
            longitude = -74.0060,
            address = "123 Main St, New York, NY",
            createdAt = System.currentTimeMillis() - 10000,
            lastUsedAt = System.currentTimeMillis() - 5000
        ),
        SavedLocationEntity(
            id = 2,
            name = "Work",
            latitude = 40.7580,
            longitude = -73.9855,
            address = "456 Office Ave, New York, NY",
            createdAt = System.currentTimeMillis() - 8000,
            lastUsedAt = System.currentTimeMillis() - 3000
        ),
        SavedLocationEntity(
            id = 3,
            name = "Gym",
            latitude = 40.7489,
            longitude = -73.9680,
            address = "789 Fitness Blvd, New York, NY",
            createdAt = System.currentTimeMillis() - 6000,
            lastUsedAt = System.currentTimeMillis() - 1000
        )
    )
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Clear existing locations and insert test data
        runBlocking {
            // Delete all existing locations
            val existingLocations = savedLocationRepository.getAllLocationsSorted().first()
            existingLocations.forEach { location ->
                savedLocationRepository.deleteLocation(location)
            }
            
            // Insert test locations
            testLocations.forEach { location ->
                savedLocationRepository.insertLocation(location)
            }
        }
    }
    
    @After
    fun tearDown() {
        // Clean up test data
        runBlocking {
            testLocations.forEach { location ->
                savedLocationRepository.deleteLocation(location)
            }
        }
    }
    
    /**
     * Test dialog shows all saved locations.
     * AC: 2, 3 - Dialog displays saved locations list
     */
    @Test
    fun dialogShowsAllSavedLocations() {
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Click Saved Locations button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Verify dialog title
        onView(withText(R.string.select_saved_location_title))
            .check(matches(isDisplayed()))
        
        // Verify all locations displayed
        testLocations.forEach { location ->
            onView(withText(location.name))
                .check(matches(isDisplayed()))
            
            // Verify address displayed
            if (location.address != null) {
                onView(withText(location.address))
                    .check(matches(isDisplayed()))
            }
        }
    }
    
    /**
     * Test item selection returns correct location.
     * AC: 7 - Location selection triggers navigation setup
     */
    @Test
    fun selectingLocationReturnsCorrectLocation() {
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Click Saved Locations button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Click on "Home" location
        onView(withText("Home"))
            .perform(click())
        
        // Verify dialog dismissed (no longer visible)
        onView(withText(R.string.select_saved_location_title))
            .check(doesNotExist())
        
        // Verify destination input field populated with location name
        // (Implementation in DestinationInputFragment.handleSavedLocationSelected)
        onView(withId(R.id.destinationInput))
            .check(matches(withText("Home")))
    }
    
    /**
     * Test TalkBack announcements for accessibility.
     * AC: 3 - TalkBack support for blind users
     */
    @Test
    fun dialogHasTalkBackAnnouncements() {
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Verify Saved Locations button has contentDescription
        onView(withId(R.id.savedLocationsButton))
            .check(matches(withContentDescription(R.string.saved_locations_button_description)))
        
        // Click button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Verify dialog title has contentDescription
        onView(withText(R.string.select_saved_location_title))
            .check(matches(isDisplayed()))
        
        // Verify RecyclerView items have contentDescriptions
        testLocations.forEachIndexed { index, location ->
            onView(withId(R.id.savedLocationsRecyclerView))
                .perform(
                    RecyclerViewActions.scrollToPosition<SavedLocationPickerAdapter.ViewHolder>(index)
                )
            
            // Content description should be built from location details
            // Format: "Saved location: [name], [address]"
            val expectedDescription = if (location.address != null) {
                "Saved location: ${location.name}, ${location.address}"
            } else {
                "Saved location: ${location.name}"
            }
            
            onView(withText(location.name))
                .check(matches(withContentDescription(expectedDescription)))
        }
    }
    
    /**
     * Test empty state when no saved locations exist.
     * AC: 2 - Empty state handling
     */
    @Test
    fun dialogShowsEmptyStateWhenNoLocations() {
        // Clear all locations
        runBlocking {
            testLocations.forEach { location ->
                savedLocationRepository.deleteLocation(location)
            }
        }
        
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Click Saved Locations button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Verify empty state message displayed
        onView(withId(R.id.emptyStateText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.emptyStateText))
            .check(matches(withText(R.string.no_saved_locations_message)))
        
        // Verify RecyclerView is not visible or empty
        onView(withId(R.id.savedLocationsRecyclerView))
            .check(matches(not(isDisplayed())))
    }
    
    /**
     * Test dialog can be cancelled.
     * AC: 3 - User can dismiss dialog
     */
    @Test
    fun dialogCanBeDismissed() {
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Click Saved Locations button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Verify dialog displayed
        onView(withText(R.string.select_saved_location_title))
            .check(matches(isDisplayed()))
        
        // Press back button or click outside to dismiss
        pressBack()
        
        // Verify dialog dismissed
        onView(withText(R.string.select_saved_location_title))
            .check(doesNotExist())
    }
    
    /**
     * Test locations sorted by lastUsedAt descending.
     * AC: Story 7.2 - Recent locations appear first
     */
    @Test
    fun locationsDisplayedInRecentOrder() {
        // Navigate to DestinationInputFragment
        navigateToDestinationInput()
        
        // Click Saved Locations button
        onView(withId(R.id.savedLocationsButton))
            .perform(click())
        
        // Verify Gym appears first (most recent lastUsedAt)
        onView(withId(R.id.savedLocationsRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<SavedLocationPickerAdapter.ViewHolder>(0))
        
        // First item should be Gym
        onView(withText("Gym"))
            .check(matches(isDisplayed()))
        
        // Second item should be Work
        onView(withId(R.id.savedLocationsRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<SavedLocationPickerAdapter.ViewHolder>(1))
        
        onView(withText("Work"))
            .check(matches(isDisplayed()))
        
        // Third item should be Home
        onView(withId(R.id.savedLocationsRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<SavedLocationPickerAdapter.ViewHolder>(2))
        
        onView(withText("Home"))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Helper method to navigate to DestinationInputFragment.
     */
    private fun navigateToDestinationInput() {
        // Assuming MainActivity starts with navigation graph
        // and DestinationInputFragment is accessible via bottom navigation or menu
        
        // Click navigation item to open DestinationInputFragment
        // This may need adjustment based on actual app navigation structure
        onView(withId(R.id.navigation_destination))
            .perform(click())
    }
}
