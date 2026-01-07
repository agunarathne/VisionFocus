package com.visionfocus.ui.savedlocations

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI model for saved location.
 * 
 * Story 7.2: Presentation layer model separate from entity
 * Parcelable for passing between dialogs and fragments.
 */
@Parcelize
data class SavedLocationUiModel(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val createdAt: Long,
    val lastUsedAt: Long
) : Parcelable
