package com.visionfocus.recognition.spatial

/**
 * Position enum representing 9 screen zones (3x3 grid)
 * 
 * Story 4.5 AC: Position calculations work correctly across device orientations
 * 
 * Screen is divided into:
 * - Horizontal: LEFT (0-33%), CENTER (33-66%), RIGHT (66-100%)
 * - Vertical: TOP (0-33%), CENTER (33-66%), BOTTOM (66-100%)
 * 
 * Combined positions form a 3x3 grid:
 * TOP_LEFT | TOP_CENTER | TOP_RIGHT
 * ------------------------------------
 * CENTER_LEFT | CENTER_CENTER | CENTER_RIGHT
 * ------------------------------------
 * BOTTOM_LEFT | BOTTOM_CENTER | BOTTOM_RIGHT
 */
enum class Position {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER_CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;
    
    /**
     * Check if position is on the left side of screen
     */
    fun isLeft(): Boolean = this in listOf(TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT)
    
    /**
     * Check if position is on the right side of screen
     */
    fun isRight(): Boolean = this in listOf(TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT)
    
    /**
     * Check if position is in the center horizontally
     */
    fun isCenter(): Boolean = this in listOf(TOP_CENTER, CENTER_CENTER, BOTTOM_CENTER)
    
    /**
     * Check if position is near the top of screen
     */
    fun isTop(): Boolean = this in listOf(TOP_LEFT, TOP_CENTER, TOP_RIGHT)
    
    /**
     * Check if position is near the bottom of screen
     */
    fun isBottom(): Boolean = this in listOf(BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT)
}
