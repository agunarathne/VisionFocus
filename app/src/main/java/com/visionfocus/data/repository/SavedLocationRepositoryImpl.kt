package com.visionfocus.data.repository

import com.visionfocus.data.local.dao.SavedLocationDao
import com.visionfocus.data.local.entity.SavedLocationEntity
import com.visionfocus.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of SavedLocationRepository.
 * 
 * Story 7.1: Manages saved locations with validation and error handling.
 * All database operations performed on IO dispatcher for thread safety.
 * 
 * Validation rules:
 * - Location name must be at least 2 characters
 * - Duplicate names are rejected (caller must handle confirmation)
 * - GPS coordinates validated for valid range
 * 
 * Error handling strategy:
 * - Database errors logged and wrapped in SavedLocationException
 * - Validation errors return Result.failure with specific exceptions
 * - Callers handle failures gracefully with user-friendly messages
 * 
 * @property savedLocationDao Data access object for saved locations database operations
 * @property ioDispatcher IO dispatcher for database operations (injected for testability)
 */
class SavedLocationRepositoryImpl @Inject constructor(
    private val savedLocationDao: SavedLocationDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : SavedLocationRepository {
    
    companion object {
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 100
        private const val MIN_LATITUDE = -90.0
        private const val MAX_LATITUDE = 90.0
        private const val MIN_LONGITUDE = -180.0
        private const val MAX_LONGITUDE = 180.0
    }
    
    override suspend fun saveLocation(
        name: String,
        latitude: Double,
        longitude: Double
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            // Validate name length
            val trimmedName = name.trim()
            if (trimmedName.length < MIN_NAME_LENGTH) {
                Timber.w("Location name too short: '${trimmedName}' (${trimmedName.length} chars)")
                return@withContext Result.failure(
                    IllegalArgumentException("Location name must be at least $MIN_NAME_LENGTH characters")
                )
            }
            
            if (trimmedName.length > MAX_NAME_LENGTH) {
                Timber.w("Location name too long: ${trimmedName.length} chars")
                return@withContext Result.failure(
                    IllegalArgumentException("Location name must be at most $MAX_NAME_LENGTH characters")
                )
            }
            
            // Validate GPS coordinates
            if (latitude !in MIN_LATITUDE..MAX_LATITUDE) {
                Timber.e("Invalid latitude: $latitude")
                return@withContext Result.failure(
                    IllegalArgumentException("Latitude must be between $MIN_LATITUDE and $MAX_LATITUDE")
                )
            }
            
            if (longitude !in MIN_LONGITUDE..MAX_LONGITUDE) {
                Timber.e("Invalid longitude: $longitude")
                return@withContext Result.failure(
                    IllegalArgumentException("Longitude must be between $MIN_LONGITUDE and $MAX_LONGITUDE")
                )
            }
            
            // Check for duplicate name
            val existing = savedLocationDao.findByName(trimmedName)
            if (existing != null) {
                Timber.i("Duplicate location name detected: $trimmedName (id=${existing.id})")
                return@withContext Result.failure(DuplicateLocationException(trimmedName))
            }
            
            // Create and save location entity
            val currentTime = System.currentTimeMillis()
            val entity = SavedLocationEntity(
                name = trimmedName,
                latitude = latitude,
                longitude = longitude,
                createdAt = currentTime,
                lastUsedAt = currentTime,
                address = null  // Future enhancement: reverse geocoding
            )
            
            val locationId = savedLocationDao.insert(entity)
            Timber.d("Saved location: $trimmedName (id=$locationId) at ($latitude, $longitude)")
            
            Result.success(locationId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save location: $name")
            Result.failure(SavedLocationException("Database save failed", e))
        }
    }
    
    override fun getAllLocationsSorted(): Flow<List<SavedLocationEntity>> {
        return savedLocationDao.getAllLocationsSorted()
    }
    
    override suspend fun deleteLocation(location: SavedLocationEntity): Result<Unit> = 
        withContext(ioDispatcher) {
            try {
                savedLocationDao.delete(location)
                Timber.i("Deleted location: ${location.name} (id=${location.id})")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete location: ${location.name}")
                Result.failure(SavedLocationException("Delete location failed", e))
            }
        }
    
    override suspend fun updateLocation(location: SavedLocationEntity): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                // Validate updated location
                val trimmedName = location.name.trim()
                if (trimmedName.length < MIN_NAME_LENGTH) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Location name must be at least $MIN_NAME_LENGTH characters")
                    )
                }
                
                if (location.latitude !in MIN_LATITUDE..MAX_LATITUDE) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid latitude: ${location.latitude}")
                    )
                }
                
                if (location.longitude !in MIN_LONGITUDE..MAX_LONGITUDE) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid longitude: ${location.longitude}")
                    )
                }
                
                savedLocationDao.update(location)
                Timber.d("Updated location: ${location.name} (id=${location.id})")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update location: ${location.name}")
                Result.failure(SavedLocationException("Update location failed", e))
            }
        }
    
    override suspend fun findLocationByName(name: String): SavedLocationEntity? =
        withContext(ioDispatcher) {
            try {
                savedLocationDao.findByName(name.trim())
            } catch (e: Exception) {
                Timber.e(e, "Failed to find location by name: $name")
                null
            }
        }
}
