package com.visionfocus.recognition.spatial

/**
 * Screen dimensions helper for spatial calculations
 * 
 * Story 4.5 AC: Position calculations work correctly across device orientations
 */
data class Size(
    val width: Int,
    val height: Int
) {
    /**
     * Screen aspect ratio (width / height)
     */
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
    
    /**
     * True if screen is in landscape orientation (width > height)
     */
    val isLandscape: Boolean
        get() = aspectRatio > 1.0f
    
    /**
     * True if screen is in portrait orientation (width ≤ height)
     */
    val isPortrait: Boolean
        get() = !isLandscape
}
