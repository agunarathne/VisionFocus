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
 * Stop Scanning Command
 * Story 4.4 Task 7: Voice command to stop continuous scanning
 * 
 * AC: Speaking "Stop" or "Cancel" exits continuous mode
 * 
 * Stops continuous scanning mode and announces summary of detected objects.
 * Only executes if scanning is currently active to avoid conflicts with
 * other "stop" or "cancel" commands.
 * 
 * Command variations:
 * - "stop"
 * - "stop scanning"
 * - "cancel"
 * - "end scan"
 * - "cancel scanning"
 * 
 * Note: Priority resolution checks if scanning active first before executing
 * 
 * @param continuousScanner Continuous scanning service
 * @since Story 4.4
 */
@Singleton
class StopScanningCommand @Inject constructor(
    private val continuousScanner: ContinuousScanner
) : VoiceCommand {
    
    companion object {
        private const val TAG = "StopScanningCommand"
    }
    
    override val displayName: String = "Stop Scanning"
    
    override val keywords: List<String> = listOf(
        "stop",
        "stop scanning",
        "cancel",
        "end scan",
        "cancel scanning",
        "finish scan",
        "stop scan"
    )
    
    override suspend fun execute(context: Context): CommandResult {
        return try {
            Log.d(TAG, "Executing Stop Scanning command")
            
            // Check if scanning is active
            if (continuousScanner.scanningState.value !is ScanningState.Scanning) {
                Log.d(TAG, "Scanning not active - command not applicable")
                return CommandResult.Failure("No scanning active")
            }
            
            // Stop continuous scanning (will announce summary)
            continuousScanner.stopScanning(isAutoStop = false)
            
            Log.d(TAG, "Continuous scanning stopped successfully")
            CommandResult.Success("Scanning stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop continuous scanning", e)
            CommandResult.Failure("Stop error: ${e.message}")
        }
    }
}
