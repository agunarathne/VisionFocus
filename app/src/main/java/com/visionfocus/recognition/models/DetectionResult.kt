package com.visionfocus.recognition.models

import com.visionfocus.recognition.spatial.SpatialInfo

/**
 * Represents a single object detection result from TFLite inference
 * 
 * @property label The COCO category name (e.g., "person", "chair")
 * @property confidence Detection confidence score [0.0-1.0]
 * @property boundingBox Normalized bounding box coordinates [ymin, xmin, ymax, xmax] in range [0-1]
 * @property spatialInfo Optional spatial information (position and distance) - Story 4.5
 */
data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox,
    val spatialInfo: SpatialInfo? = null
)

/**
 * Normalized bounding box coordinates relative to image dimensions
 * All values are in range [0-1] where 0 is top/left and 1 is bottom/right
 */
data class BoundingBox(
    val yMin: Float,
    val xMin: Float,
    val yMax: Float,
    val xMax: Float
)
