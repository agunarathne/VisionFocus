package com.visionfocus.data.model

/**
 * Verbosity levels for object recognition announcements.
 * 
 * From FR4, FR47: Users can select verbosity mode (brief/standard/detailed)
 * to control how much detail they hear about recognized objects.
 */
enum class VerbosityMode {
    /**
     * Brief mode: Announces only object category.
     * Example: "Chair"
     */
    BRIEF,
    
    /**
     * Standard mode (default): Announces category + confidence.
     * Example: "Chair with high confidence"
     */
    STANDARD,
    
    /**
     * Detailed mode: Announces category + confidence + position + count.
     * Example: "High confidence: chair in center of view. Two chairs detected."
     */
    DETAILED;
    
    companion object {
        /**
         * Parse string to VerbosityMode enum.
         * Returns STANDARD as default if string doesn't match known values.
         */
        fun fromString(value: String): VerbosityMode {
            return entries.find { it.name == value } ?: STANDARD
        }
    }
}
