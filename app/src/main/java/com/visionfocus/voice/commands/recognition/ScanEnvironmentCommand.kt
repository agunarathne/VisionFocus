package com.visionfocus.voice.commands.recognition

import android.content.Context
import android.util.Log
import com.visionfocus.recognition.scanning.ContinuousScanner
import com.visionfocus.recognition.scanning.ScanningState
import com.visionfocus.voice.processor.CommandResult
import com.visionfocus.voice.processor.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scan Environment Command
 * Story 4.4 Task 7: Voice command to activate continuous scanning
 * 
 * AC: Voice command "Scan environment" activates continuous mode
 * 
 * Activates continuous scanning mode where the camera captures frames
 * every 3 seconds and announces newly detected objects. Auto-stops after
 * 60 seconds or when user says "Stop".
 * 
 * Command variations:
 * - "scan environment"
 * - "continuous scan"
 * - "map environment"
 * - "scan surroundings"
 * - "map surroundings"
 * - "scan area"
 * 
 * @param continuousScanner Continuous scanning service
 * @since Story 4.4
 */
@Singleton
class ScanEnvironmentCommand @Inject constructor(
    private val continuousScanner: ContinuousScanner
) : VoiceCommand {
    
    companion object {
        private const val TAG = "ScanEnvironmentCommand"
    }
    
    override val displayName: String = "Scan Environment"
    
    override val keywords: List<String> = listOf(
        "scan environment",
        "continuous scan",
        "map environment",
        "scan surroundings",
        "map surroundings",
        "scan area",
        "map area",
        "environment scan",
        "continuous scanning"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Scan Environment command")
            
            // Check if already scanning
            if (continuousScanner.scanningState.value is ScanningState.Scanning) {
                Log.w(TAG, "Scanning already active - ignoring command")
                return CommandResult.Failure("Scanning already active")
            }
            
            // Start continuous scanning
            continuousScanner.startScanning()
            
            Log.d(TAG, "Continuous scanning started successfully")
            CommandResult.Success("Scanning started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start continuous scanning", e)
            CommandResult.Failure("Scanning error: ${e.message}")
        }
    }
}
