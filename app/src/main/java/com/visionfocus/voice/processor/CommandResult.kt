package com.visionfocus.voice.processor

/**
 * Command execution result
 * Story 3.2 Task 1.2: Sealed class representing command execution outcome
 * 
 * Used to communicate command success/failure back to command processor.
 * 
 * @since Story 3.2
 */
sealed class CommandResult {
    /**
     * Command executed successfully.
     * 
     * @property message Success message for logging
     */
    data class Success(val message: String) : CommandResult()
    
    /**
     * Command execution failed.
     * 
     * @property error Error message describing failure
     */
    data class Failure(val error: String) : CommandResult()
}
