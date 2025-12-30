package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Non-Maximum Suppression (NMS) for removing duplicate detections
 * 
 * Story 2.2 AC2: Remove duplicate detections of same object
 * When bounding boxes overlap significantly (IoU > 0.5), keep highest confidence
 * 
 * Algorithm:
 * 1. Sort detections by confidence (highest first)
 * 2. For each detection, compare with remaining detections
 * 3. If same class and IoU > 0.5 → discard lower-confidence detection
 * 
 * Usage:
 * ```
 * val deduped = nms.apply(filteredDetections)
 * ```
 */
@Singleton
class NonMaximumSuppression @Inject constructor() {
    
    companion object {
        /**
         * Intersection over Union threshold for overlap detection
         * Standard NMS threshold for object detection
         */
        const val IOU_THRESHOLD = 0.5f
        
        /**
         * Maximum detections to process
         * Prevents O(n²) performance issues with pathological inputs
         * TFLite typically returns 10-100 detections; 200 is safe upper bound
         */
        const val MAX_DETECTIONS = 200
    }
    
    /**
     * Apply Non-Maximum Suppression to remove duplicate detections
     * 
     * @param detections Filtered detection results
     * @return Deduplicated list with overlapping detections removed
     */
    fun apply(detections: List<DetectionResult>): List<DetectionResult> {
        // Fast path: 0 or 1 detections
        if (detections.size <= 1) return detections
        
        // Sort by confidence descending (highest first)
        // Limit to MAX_DETECTIONS to prevent O(n²) performance issues
        val sorted = detections
            .sortedByDescending { it.confidence }
            .take(MAX_DETECTIONS)
        
        val keep = mutableListOf<DetectionResult>()
        val discarded = mutableSetOf<Int>()
        
        sorted.forEachIndexed { i, detection ->
            // Skip if already discarded
            if (i in discarded) return@forEachIndexed
            
            // Keep this detection
            keep.add(detection)
            
            // Compare with remaining detections
            for (j in (i + 1) until sorted.size) {
                if (j in discarded) continue
                
                val other = sorted[j]
                
                // Only suppress if same class label
                if (detection.label == other.label) {
                    val iou = detection.boundingBox.calculateIoU(other.boundingBox)
                    
                    // Discard lower-confidence detection if significant overlap
                    if (iou > IOU_THRESHOLD) {
                        discarded.add(j)
                    }
                }
            }
        }
        
        return keep
    }
}

/**
 * Extension function: Calculate Intersection over Union (IoU) between two bounding boxes
 * 
 * IoU = Intersection Area / Union Area
 * Range: [0, 1] where 0 = no overlap, 1 = perfect overlap
 * 
 * Internal visibility: Implementation detail of NMS module
 */
internal fun BoundingBox.calculateIoU(other: BoundingBox): Float {
    // Calculate intersection coordinates
    val intersectYMin = maxOf(this.yMin, other.yMin)
    val intersectXMin = maxOf(this.xMin, other.xMin)
    val intersectYMax = minOf(this.yMax, other.yMax)
    val intersectXMax = minOf(this.xMax, other.xMax)
    
    // Check if boxes overlap
    if (intersectXMax < intersectXMin || intersectYMax < intersectYMin) {
        return 0f  // No overlap
    }
    
    // Calculate intersection area
    val intersectionArea = (intersectXMax - intersectXMin) * (intersectYMax - intersectYMin)
    
    // Calculate union area
    val thisArea = (this.xMax - this.xMin) * (this.yMax - this.yMin)
    val otherArea = (other.xMax - other.xMin) * (other.yMax - other.yMin)
    val unionArea = thisArea + otherArea - intersectionArea
    
    // Avoid division by zero
    if (unionArea == 0f) return 0f
    
    return intersectionArea / unionArea
}
