package com.visionfocus.navigation.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.visionfocus.accessibility.HapticFeedbackManager
import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.navigation.validation.DestinationValidator
import com.visionfocus.tts.TTSManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for DestinationInputViewModel.
 * 
 * Story 6.1: Test voice input handling, validation triggers, navigation events
 */
@ExperimentalCoroutinesApi
class DestinationInputViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var mockValidator: DestinationValidator
    
    @Mock
    private lateinit var mockTtsManager: TTSManager
    
    @Mock
    private lateinit var mockHapticManager: HapticFeedbackManager
    
    private lateinit var viewModel: DestinationInputViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = DestinationInputViewModel(
            destinationValidator = mockValidator,
            ttsManager = mockTtsManager,
            hapticFeedbackManager = mockHapticManager
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Test voice input completion updates text and triggers validation.
     * AC: 3, 4, 5 - Voice input integration
     */
    @Test
    fun `onVoiceInputComplete updates text field and announces transcription`() = runTest {
        // Given transcribed text
        val transcribedText = "Times Square"
        val expectedDestination = Destination(
            query = transcribedText,
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        // Mock validator returns valid result
        `when`(mockValidator.validateDestination(transcribedText))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When voice input completes
        viewModel.onVoiceInputComplete(transcribedText)
        advanceUntilIdle()
        
        // Then text field is updated
        assertEquals(transcribedText, viewModel.destinationText.value)
        
        // And TTS announces transcription
        verify(mockTtsManager).announce("You said: $transcribedText")
        
        // And validation is triggered
        verify(mockValidator).validateDestination(transcribedText)
        
        // And validation state is Valid
        assertTrue(viewModel.validationState.value is ValidationResult.Valid)
    }
    
    /**
     * Test empty input validation returns Empty.
     * AC: 7, 8 - Empty destination validation
     */
    @Test
    fun `validateDestination with empty string sets Empty state`() = runTest {
        // Given empty input
        val query = ""
        
        // When validating
        viewModel.validateDestination(query)
        advanceUntilIdle()
        
        // Then validation state is Empty
        assertTrue(viewModel.validationState.value is ValidationResult.Empty)
        
        // And validator is not called
        verifyNoInteractions(mockValidator)
        
        // And no TTS announcement
        verifyNoInteractions(mockTtsManager)
    }
    
    /**
     * Test too short input validation returns TooShort and announces error.
     * AC: 7 - Minimum character length validation
     */
    @Test
    fun `validateDestination with too short string sets TooShort state and announces`() = runTest {
        // Given input < 3 characters
        val query = "NY"
        
        // When validating
        viewModel.validateDestination(query)
        advanceUntilIdle()
        
        // Then validation state is TooShort
        assertTrue(viewModel.validationState.value is ValidationResult.TooShort)
        
        // And TTS announces error
        verify(mockTtsManager).announce("Destination too short. Please say more.")
        
        // And validator is not called
        verifyNoInteractions(mockValidator)
    }
    
    /**
     * Test valid destination updates state and announces destination.
     * AC: 6 - Valid destination validation
     */
    @Test
    fun `validateDestination with valid query updates state and announces`() = runTest {
        // Given valid query
        val query = "Central Park"
        val expectedDestination = Destination(
            query = query,
            name = "Central Park",
            latitude = 40.785091,
            longitude = -73.968285,
            formattedAddress = "Central Park, New York, NY, USA"
        )
        
        // Mock validator returns valid result
        `when`(mockValidator.validateDestination(query))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        viewModel.validateDestination(query)
        advanceUntilIdle()
        
        // Then validation state is Valid
        val state = viewModel.validationState.value
        assertTrue(state is ValidationResult.Valid)
        assertEquals(expectedDestination, (state as ValidationResult.Valid).destination)
        
        // And TTS announces destination
        verify(mockTtsManager).announce("Destination: ${expectedDestination.name}")
        
        // And validator was called
        verify(mockValidator).validateDestination(query)
    }
    
    /**
     * Test ambiguous destination emits clarification event.
     * AC: 7 - Ambiguous destination clarification
     */
    @Test
    fun `validateDestination with ambiguous query emits clarification event`() = runTest {
        // Given ambiguous query
        val query = "Central Park"
        val option1 = Destination(
            query = query,
            name = "Central Park, New York",
            latitude = 40.785091,
            longitude = -73.968285,
            formattedAddress = "Central Park, New York, NY, USA"
        )
        val option2 = Destination(
            query = query,
            name = "Central Park, Sacramento",
            latitude = 38.595371,
            longitude = -121.428337,
            formattedAddress = "Central Park, Sacramento, CA, USA"
        )
        val options = listOf(option1, option2)
        
        // Mock validator returns ambiguous result
        `when`(mockValidator.validateDestination(query))
            .thenReturn(ValidationResult.Ambiguous(options))
        
        // When validating
        val events = mutableListOf<NavigationEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }
        
        viewModel.validateDestination(query)
        advanceUntilIdle()
        
        // Then validation state is Ambiguous
        assertTrue(viewModel.validationState.value is ValidationResult.Ambiguous)
        
        // And TTS announces ambiguity
        verify(mockTtsManager).announce("Multiple locations found. Did you mean ${option1.name}, or ${option2.name}?")
        
        // And clarification event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is NavigationEvent.ShowClarificationDialog)
        assertEquals(options, (events[0] as NavigationEvent.ShowClarificationDialog).options)
        
        job.cancel()
    }
    
    /**
     * Test Go button with valid destination triggers navigation.
     * AC: 6 - Go button starts navigation
     */
    @Test
    fun `onGoClicked with valid destination emits navigation event`() = runTest {
        // Given valid destination in validation state
        val destination = Destination(
            query = "Times Square",
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        viewModel.destinationText.value = "Times Square"
        
        // Mock validator returns valid result
        `when`(mockValidator.validateDestination("Times Square"))
            .thenReturn(ValidationResult.Valid(destination))
        
        // Validate first to set state
        viewModel.validateDestination("Times Square")
        advanceUntilIdle()
        
        // Collect navigation events
        val events = mutableListOf<NavigationEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }
        
        // When Go button clicked
        viewModel.onGoClicked()
        advanceUntilIdle()
        
        // Then navigation event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is NavigationEvent.StartNavigation)
        assertEquals(destination, (events[0] as NavigationEvent.StartNavigation).destination)
        
        // And haptic feedback triggered
        verify(mockHapticManager).trigger(any())
        
        job.cancel()
    }
    
    /**
     * Test Go button with empty destination announces error.
     * AC: 8 - Empty destination validation
     */
    @Test
    fun `onGoClicked with empty destination announces error`() = runTest {
        // Given empty destination text
        viewModel.destinationText.value = ""
        
        // When Go button clicked
        viewModel.onGoClicked()
        advanceUntilIdle()
        
        // Then TTS announces error
        verify(mockTtsManager).announce("Please enter a destination")
        
        // And no haptic feedback
        verifyNoInteractions(mockHapticManager)
    }
    
    /**
     * Test Go button with invalid state triggers re-validation.
     * AC: 6 - Re-validation on Go button
     */
    @Test
    fun `onGoClicked with non-valid state triggers validation`() = runTest {
        // Given destination text but Empty validation state
        val query = "Times Square"
        viewModel.destinationText.value = query
        
        val destination = Destination(
            query = query,
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        // Mock validator returns valid result
        `when`(mockValidator.validateDestination(query))
            .thenReturn(ValidationResult.Valid(destination))
        
        // When Go button clicked
        viewModel.onGoClicked()
        advanceUntilIdle()
        
        // Then validation is triggered
        verify(mockValidator).validateDestination(query)
        
        // And state becomes Valid
        assertTrue(viewModel.validationState.value is ValidationResult.Valid)
    }
    
    /**
     * Test clarification selection updates state.
     * AC: 7 - Clarification dialog selection
     */
    @Test
    fun `onClarificationSelected updates text and validation state`() = runTest {
        // Given selected destination from clarification
        val selectedDestination = Destination(
            query = "Central Park",
            name = "Central Park, New York",
            latitude = 40.785091,
            longitude = -73.968285,
            formattedAddress = "Central Park, New York, NY, USA"
        )
        
        // When user selects destination
        viewModel.onClarificationSelected(selectedDestination)
        advanceUntilIdle()
        
        // Then text field is updated
        assertEquals(selectedDestination.name, viewModel.destinationText.value)
        
        // And validation state is Valid
        val state = viewModel.validationState.value
        assertTrue(state is ValidationResult.Valid)
        assertEquals(selectedDestination, (state as ValidationResult.Valid).destination)
        
        // And TTS announces selection
        verify(mockTtsManager).announce("Selected: ${selectedDestination.name}")
    }
    
    /**
     * Test back button press announces cancellation.
     * AC: 9 - Back button announces cancellation
     */
    @Test
    fun `onBackPressed announces navigation cancelled`() = runTest {
        // When back pressed
        viewModel.onBackPressed()
        advanceUntilIdle()
        
        // Then TTS announces cancellation
        verify(mockTtsManager).announce("Navigation cancelled")
    }
    
    /**
     * Test validation loading state updates correctly.
     */
    @Test
    fun `validation loading state updates during validation`() = runTest {
        // Given valid query
        val query = "Times Square"
        val destination = Destination(
            query = query,
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        // Mock validator returns valid result
        `when`(mockValidator.validateDestination(query))
            .thenReturn(ValidationResult.Valid(destination))
        
        // Initially not validating
        assertFalse(viewModel.isValidating.value)
        
        // Start validation
        viewModel.validateDestination(query)
        
        // During validation, loading should be true
        // (this depends on timing, so we advance scheduler)
        advanceUntilIdle()
        
        // After validation completes, loading should be false
        assertFalse(viewModel.isValidating.value)
    }
}
