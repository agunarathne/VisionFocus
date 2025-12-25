package com.visionfocus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for saved locations storage.
 * 
 * Foundation schema for Story 1.4. Full columns will be added in Story 7.1
 * when saved locations feature is implemented (Epic 7).
 * 
 * Future columns (Story 7.1):
 * - name: String (user-provided location name)
 * - latitude: Double (GPS coordinate)
 * - longitude: Double (GPS coordinate)
 * - createdAt: Long (timestamp in milliseconds)
 */
@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
    
    // Columns will be added in Story 7.1 (Epic 7: Saved Locations)
)
