package com.visionfocus.voice.commands.settings

import android.content.Context
import com.visionfocus.data.repository.SettingsRepository
import com.visionfocus.voice.processor.CommandResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for Large Text voice commands
 * Story 5.5 Task 1: Test LargeTextOnCommand and LargeTextOffCommand
 * 
 * Test Coverage:
 * - execute() success paths
 * - execute() failure paths (DataStore exceptions)
 * - Keyword matching
 * - DataStore persistence verification
 */
class LargeTextCommandsTest {
    
    @Mock
    private lateinit var settingsRepository: SettingsRepository
    
    @Mock
    private lateinit var context: Context
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }
    
    // ========== LargeTextOnCommand Tests ==========
    
    @Test
    fun `LargeTextOnCommand execute() should set large text mode to true`() = runTest {
        // Given
        val command = LargeTextOnCommand(settingsRepository)
        
        // When
        val result = command.execute(context)
        verify(settingsRepository).setLargeTextMode(true)
        assertTrue(result is CommandResult.Success
        assertTrue(result is CommandResult.Success)
        assertEquals("Large text enabled", (result as CommandResult.Success).message)
    }
    
    @Test
    fun `LargeTextOnCommand execute() should return failure on DataStore exception`() = runTest {
        // Given
        val command = LargeTextOnCommand(settingsRepository)
        coEvery { settingsRepository.setLargeTextMode(any()) } throws java.io.IOException("Storage full")
        whenever(settingsRepository.setLargeTextMode(anyBoolean())).thenThrow(java.io.IOException("Storage full"))
        
        // When
        val result = command.execute(context)
        
        // Then
        assertTrue(result is CommandResult.Failure
    
    @Test
    fun `LargeTextOnCommand should have correct displayName`() {
        // Given
        val command = LargeTextOnCommand(settingsRepository)
        
        // Then
        assertEquals("Large Text On", command.displayName)
    }
    
    @Test
    fun `LargeTextOnCommand should have expected keywords`() {
        // Given
        val command = LargeTextOnCommand(settingsRepository)
        
        // Then
        assertTrue(command.keywords.contains("large text on"))
        assertTrue(command.keywords.contains("enable large text"))
        assertTrue(command.keywords.contains("big text on"))
        assertTrue(command.keywords.contains("increase text size"))
        assertTrue(command.keywords.contains("bigger text"))
    }
    
    // ========== LargeTextOffCommand Tests ==========
    
    @Test
    fun `LargeTextOffCommand execute() should set large text mode to false`() = runTest {
        // Given
        val command = LargeTextOffCommand(settingsRepository)
        
        // When
        val result = command.execute(context)
        
        // Then
        coVerify { settingsRepository.setLargeTextMode(false) }
        assertTrue(result is CommandResult.Success)
        verify(settingsRepository).setLargeTextMode(false)
        assertTrue(result is CommandResult.Success
    @Test
    fun `LargeTextOffCommand execute() should return failure on DataStore exception`() = runTest {
        // Given
        val command = LargeTextOffCommand(settingsRepository)
        coEvery { settingsRepository.setLargeTextMode(any()) } throws java.io.IOException("Storage full")
        
        whenever(settingsRepository.setLargeTextMode(anyBoolean())).thenThrow(java.io.IOException("Storage full"))
        
        // When
        val result = command.execute(context)
        
        // Then
        assertTrue(result is CommandResult.Failure
    @Test
    fun `LargeTextOffCommand should have correct displayName`() {
        // Given
        val command = LargeTextOffCommand(settingsRepository)
        
        // Then
        assertEquals("Large Text Off", command.displayName)
    }
    
    @Test
    fun `LargeTextOffCommand should have expected keywords`() {
        // Given
        val command = LargeTextOffCommand(settingsRepository)
        
        // Then
        assertTrue(command.keywords.contains("large text off"))
        assertTrue(command.keywords.contains("disable large text"))
        assertTrue(command.keywords.contains("big text off"))
        assertTrue(command.keywords.contains("decrease text size"))
        assertTrue(command.keywords.contains("normal text"))
        assertTrue(command.keywords.contains("smaller text"))
    }
}
