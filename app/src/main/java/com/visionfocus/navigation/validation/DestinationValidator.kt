package com.visionfocus.navigation.validation

import android.util.Log
import com.visionfocus.navigation.models.ValidationResult
import com.visionfocus.navigation.repository.NavigationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Destination validator for user input.
 * 
 * Story 6.1: Basic validation (length, non-empty)
 * Story 6.2: Geocoding validation via Google Maps API
 */
@Singleton
class DestinationValidator @Inject constructor(
    private val navigationRepository: NavigationRepository
) {
    
    companion object {
        private const val TAG = "DestinationValidator"
        private const val MIN_QUERY_LENGTH = 3
    }
    
    /**
     * Validates destination query.
     * 
     * Story 6.1: Basic validation + mock geocoding via repository
     * Story 6.2: Repository will call Google Maps Geocoding API
     * 
     * @param query User-entered destination query
     * @return ValidationResult indicating validity
     */
    suspend fun validateDestination(query: String): ValidationResult {
        Log.d(TAG, "Validating destination: $query")
        
        val trimmedQuery = query.trim()
        
        // Check for empty input
        if (trimmedQuery.isBlank()) {
            Log.d(TAG, "Validation result: Empty")
            return ValidationResult.Empty
        }
        
        // Check minimum length
        if (trimmedQuery.length < MIN_QUERY_LENGTH) {
            Log.d(TAG, "Validation result: TooShort (length: ${trimmedQuery.length})")
            return ValidationResult.TooShort
        }
        
        // Delegate to repository for geocoding validation
        // Story 6.1: Mock implementation in repository
        // Story 6.2: Repository will call Google Maps Geocoding API
        return try {
            val result = navigationRepository.validateDestination(trimmedQuery)
            Log.d(TAG, "Validation result: ${result.javaClass.simpleName}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Validation failed", e)
            ValidationResult.Invalid("Unable to validate destination. Please try again.")
        }
    }
}
