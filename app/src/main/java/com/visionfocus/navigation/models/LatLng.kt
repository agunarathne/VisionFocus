package com.visionfocus.navigation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * GPS coordinates for navigation.
 * 
 * @property latitude Decimal degrees latitude (-90 to +90)
 * @property longitude Decimal degrees longitude (-180 to +180)
 */
@Parcelize
data class LatLng(
    val latitude: Double,
    val longitude: Double
) : Parcelable {
    override fun toString(): String = "$latitude,$longitude"
}
