package com.visionfocus.voice.processor

import android.util.Log

/**
 * Levenshtein Distance Matcher
 * Story 3.2 Task 5: Fuzzy string matching with edit distance
 * 
 * Implements Levenshtein distance algorithm to tolerate minor typos
 * and variations in voice command transcriptions.
 * 
 * Edit distance threshold: ≤2 for matching
 * Examples:
 * - "recgonize" → "recognize" (distance: 1)
 * - "recognise" → "recognize" (distance: 1)
 * - "navig8" → "navigate" (distance: 2)
 * 
 * @since Story 3.2
 */
object LevenshteinMatcher {
    
    private const val TAG = "LevenshteinMatcher"
    
    /**
     * Maximum edit distance for fuzzy matching.
     * AC: Tolerates minor variations within distance ≤2
     */
    private const val MAX_DISTANCE = 2
    
    /**
     * Calculate Levenshtein distance between two strings.
     * 
     * Uses dynamic programming approach with O(m*n) complexity.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance (minimum number of single-character edits)
     */
    fun calculateDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        // Edge case: empty strings
        if (len1 == 0) return len2
        if (len2 == 0) return len1
        
        // Create distance matrix
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        // Initialize first row and column
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        // Fill matrix using dynamic programming
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Check if two strings match within acceptable edit distance.
     * 
     * @param input Transcribed voice input
     * @param keyword Command keyword to match against
     * @return True if strings match within MAX_DISTANCE threshold
     */
    fun isMatch(input: String, keyword: String): Boolean {
        val distance = calculateDistance(input, keyword)
        return distance <= MAX_DISTANCE
    }
    
    /**
     * Find best matching keyword from a list within edit distance threshold.
     * 
     * Prioritizes:
     * 1. Exact matches (distance 0)
     * 2. Closest matches (lowest distance)
     * 
     * @param input Transcribed voice input
     * @param keywords List of possible keywords to match
     * @return Pair of (best matching keyword, distance) or null if no match within threshold
     */
    fun findBestMatch(input: String, keywords: List<String>): Pair<String, Int>? {
        var bestKeyword: String? = null
        var bestDistance = Int.MAX_VALUE
        
        keywords.forEach { keyword ->
            val distance = calculateDistance(input, keyword)
            if (distance < bestDistance && distance <= MAX_DISTANCE) {
                bestKeyword = keyword
                bestDistance = distance
            }
        }
        
        return if (bestKeyword != null) {
            Log.d(TAG, "Fuzzy match: \"$input\" → \"$bestKeyword\" (distance: $bestDistance)")
            Pair(bestKeyword!!, bestDistance)
        } else {
            null
        }
    }
}
