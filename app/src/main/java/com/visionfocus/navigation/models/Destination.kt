package com.visionfocus.navigation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a navigation destination.
 * 
 * Story 6.1: Destination Input via Voice and Text
 * 
 * @property query User-entered query (address, landmark, or coordinates)
 * @property name Human-readable name for TTS announcements
 * @property latitude GPS latitude in decimal degrees
 * @property longitude GPS longitude in decimal degrees
 * @property formattedAddress Full address for display (optional)
 */
@Parcelize
data class Destination(
    val query: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String? = null
) : Parcelable
