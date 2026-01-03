package com.visionfocus.navigation.models

/**
 * GPS coordinates for navigation.
 * 
 * @property latitude Decimal degrees latitude (-90 to +90)
 * @property longitude Decimal degrees longitude (-180 to +180)
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String = "$latitude,$longitude"
}
