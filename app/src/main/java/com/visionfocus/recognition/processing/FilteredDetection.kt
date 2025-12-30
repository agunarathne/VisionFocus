package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.BoundingBox

/**
 * Detection result after confidence filtering and NMS processing
 * 
 * Story 2.2: Post-processing pipeline output
 * Adds confidence level categorization for TTS phrasing
 * 
 * @property label The COCO category name (e.g., "person", "chair")
 * @property confidence Detection confidence score [0.6-1.0]
 * @property confidenceLevel Categorized confidence level (HIGH/MEDIUM/LOW)
 * @property boundingBox Normalized bounding box coordinates
 */
data class FilteredDetection(
    val label: String,
    val confidence: Float,
    val confidenceLevel: ConfidenceLevel,
    val boundingBox: BoundingBox
)
