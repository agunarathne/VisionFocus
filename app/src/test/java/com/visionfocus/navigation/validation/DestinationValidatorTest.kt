package com.visionfocus.navigation.validation

import com.visionfocus.navigation.models.Destination
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.navigation.repository.NavigationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for DestinationValidator.
 * 
 * Story 6.1 Task 12: Test destination validation logic
 * 
 * Tests:
 * - Empty input returns ValidationResult.Empty
 * - Too short input (<3 chars) returns ValidationResult.TooShort
 * - Valid destinations return ValidationResult.Valid
 * - Ambiguous inputs return ValidationResult.Ambiguous
 * - Special characters handling
 * - Unicode characters (international addresses)
 * - Exception handling returns ValidationResult.Invalid
 */
class DestinationValidatorTest {
    
    @Mock
    private lateinit var mockRepository: NavigationRepository
    
    private lateinit var validator: DestinationValidator
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        validator = DestinationValidator(mockRepository)
    }
    
    /**
     * Test empty input returns Empty result.
     * AC: 7 - Empty destination validation
     */
    @Test
    fun `validateDestination with empty string returns Empty`() = runTest {
        // Given empty input
        val query = ""
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Empty
        assertTrue(result is ValidationResult.Empty)
        verifyNoInteractions(mockRepository)
    }
    
    /**
     * Test blank input returns Empty result.
     * AC: 7 - Empty destination validation
     */
    @Test
    fun `validateDestination with blank string returns Empty`() = runTest {
        // Given blank input (spaces only)
        val query = "   "
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Empty
        assertTrue(result is ValidationResult.Empty)
        verifyNoInteractions(mockRepository)
    }
    
    /**
     * Test too short input returns TooShort result.
     * AC: 7 - Minimum character length validation
     */
    @Test
    fun `validateDestination with two characters returns TooShort`() = runTest {
        // Given input < 3 characters
        val query = "NY"
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns TooShort
        assertTrue(result is ValidationResult.TooShort)
        verifyNoInteractions(mockRepository)
    }
    
    /**
     * Test single character input returns TooShort result.
     * AC: 7 - Minimum character length validation
     */
    @Test
    fun `validateDestination with one character returns TooShort`() = runTest {
        // Given input < 3 characters
        val query = "A"
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns TooShort
        assertTrue(result is ValidationResult.TooShort)
        verifyNoInteractions(mockRepository)
    }
    
    /**
     * Test valid destination passes validation.
     * AC: 6 - Valid destination returns Valid result
     */
    @Test
    fun `validateDestination with valid address returns Valid`() = runTest {
        // Given valid destination
        val query = "Times Square, New York"
        val expectedDestination = Destination(
            query = query,
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        // Mock repository returns valid result
        `when`(mockRepository.validateDestination(query))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Valid with destination
        assertTrue(result is ValidationResult.Valid)
        assertEquals(expectedDestination, (result as ValidationResult.Valid).destination)
        verify(mockRepository).validateDestination(query)
    }
    
    /**
     * Test valid landmark passes validation.
     * AC: 6 - Valid destination returns Valid result
     */
    @Test
    fun `validateDestination with valid landmark returns Valid`() = runTest {
        // Given valid landmark
        val query = "Statue of Liberty"
        val expectedDestination = Destination(
            query = query,
            name = "Statue of Liberty",
            latitude = 40.6892,
            longitude = -74.0445,
            formattedAddress = "Liberty Island, New York, NY 10004, USA"
        )
        
        // Mock repository returns valid result
        `when`(mockRepository.validateDestination(query))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Valid
        assertTrue(result is ValidationResult.Valid)
        assertEquals(expectedDestination, (result as ValidationResult.Valid).destination)
        verify(mockRepository).validateDestination(query)
    }
    
    /**
     * Test ambiguous destination returns Ambiguous result with options.
     * AC: 7 - Ambiguous destinations trigger clarification
     */
    @Test
    fun `validateDestination with ambiguous input returns Ambiguous with options`() = runTest {
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
        
        // Mock repository returns ambiguous result
        `when`(mockRepository.validateDestination(query))
            .thenReturn(ValidationResult.Ambiguous(listOf(option1, option2)))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Ambiguous with options
        assertTrue(result is ValidationResult.Ambiguous)
        val options = (result as ValidationResult.Ambiguous).options
        assertEquals(2, options.size)
        assertEquals(option1, options[0])
        assertEquals(option2, options[1])
        verify(mockRepository).validateDestination(query)
    }
    
    /**
     * Test special characters are handled correctly.
     * AC: 7 - Special characters validation
     */
    @Test
    fun `validateDestination with special characters passes validation`() = runTest {
        // Given query with special characters
        val query = "Main St & 5th Ave"
        val expectedDestination = Destination(
            query = query,
            name = "Main St & 5th Ave",
            latitude = 40.7614,
            longitude = -73.9776,
            formattedAddress = "Main St & 5th Ave, New York, NY, USA"
        )
        
        // Mock repository returns valid result
        `when`(mockRepository.validateDestination(query))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Valid
        assertTrue(result is ValidationResult.Valid)
        verify(mockRepository).validateDestination(query)
    }
    
    /**
     * Test Unicode characters (international addresses) are handled correctly.
     * AC: 7 - Unicode characters validation
     */
    @Test
    fun `validateDestination with Unicode characters passes validation`() = runTest {
        // Given query with Unicode characters (Japanese)
        val query = "東京駅" // Tokyo Station in Japanese
        val expectedDestination = Destination(
            query = query,
            name = "東京駅",
            latitude = 35.6812,
            longitude = 139.7671,
            formattedAddress = "東京駅, 東京都千代田区, Japan"
        )
        
        // Mock repository returns valid result
        `when`(mockRepository.validateDestination(query))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Valid
        assertTrue(result is ValidationResult.Valid)
        verify(mockRepository).validateDestination(query)
    }
    
    /**
     * Test trimming whitespace works correctly.
     * AC: 7 - Input trimming
     */
    @Test
    fun `validateDestination trims whitespace correctly`() = runTest {
        // Given query with leading/trailing spaces
        val query = "  Times Square  "
        val trimmedQuery = "Times Square"
        val expectedDestination = Destination(
            query = trimmedQuery,
            name = "Times Square",
            latitude = 40.7580,
            longitude = -73.9855,
            formattedAddress = "Times Square, Manhattan, NY 10036, USA"
        )
        
        // Mock repository returns valid result for TRIMMED query
        `when`(mockRepository.validateDestination(trimmedQuery))
            .thenReturn(ValidationResult.Valid(expectedDestination))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Valid and repository received trimmed query
        assertTrue(result is ValidationResult.Valid)
        verify(mockRepository).validateDestination(trimmedQuery)
    }
    
    /**
     * Test exception handling returns Invalid result.
     * AC: 7 - Error handling
     */
    @Test
    fun `validateDestination handles repository exception gracefully`() = runTest {
        // Given query that causes exception
        val query = "Valid Query"
        
        // Mock repository throws exception
        `when`(mockRepository.validateDestination(query))
            .thenThrow(RuntimeException("Network error"))
        
        // When validating
        val result = validator.validateDestination(query)
        
        // Then returns Invalid with error message
        assertTrue(result is ValidationResult.Invalid)
        val reason = (result as ValidationResult.Invalid).reason
        assertTrue(reason.contains("Unable to validate destination"))
        verify(mockRepository).validateDestination(query)
    }
}
