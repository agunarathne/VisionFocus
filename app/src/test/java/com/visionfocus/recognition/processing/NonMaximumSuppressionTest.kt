package com.visionfocus.recognition.processing

import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NonMaximumSuppression
 * Story 2.2 Task 1.7: Add unit tests for NMS overlap detection
 */
class NonMaximumSuppressionTest {
    
    private lateinit var nms: NonMaximumSuppression
    
    @Before
    fun setup() {
        nms = NonMaximumSuppression()
    }
    
    // Test NMS duplicate removal
    
    @Test
    fun `apply removes overlapping detections with same label`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)),
            DetectionResult("chair", 0.78f, BoundingBox(0.12f, 0.12f, 0.52f, 0.52f))  // ~80% overlap
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(1, deduped.size)
        assertEquals(0.92f, deduped[0].confidence, 0.001f)  // Kept highest confidence
        assertEquals("chair", deduped[0].label)
    }
    
    @Test
    fun `apply keeps non-overlapping detections with same label`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.3f, 0.3f)),
            DetectionResult("chair", 0.78f, BoundingBox(0.6f, 0.6f, 0.9f, 0.9f))  // No overlap
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(2, deduped.size)
    }
    
    @Test
    fun `apply keeps overlapping detections with different labels`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)),
            DetectionResult("table", 0.78f, BoundingBox(0.12f, 0.12f, 0.52f, 0.52f))  // Different object
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(2, deduped.size)
    }
    
    @Test
    fun `apply handles empty input list`() {
        val deduped = nms.apply(emptyList())
        
        assertTrue(deduped.isEmpty())
    }
    
    @Test
    fun `apply handles single detection`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f))
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(1, deduped.size)
        assertEquals(detections[0], deduped[0])
    }
    
    @Test
    fun `apply sorts by confidence and keeps highest when overlapping`() {
        // Input not sorted by confidence
        val detections = listOf(
            DetectionResult("chair", 0.68f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)),
            DetectionResult("chair", 0.92f, BoundingBox(0.12f, 0.12f, 0.52f, 0.52f)),  // Highest
            DetectionResult("chair", 0.75f, BoundingBox(0.11f, 0.11f, 0.51f, 0.51f))
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(1, deduped.size)
        assertEquals(0.92f, deduped[0].confidence, 0.001f)
    }
    
    @Test
    fun `apply removes multiple overlapping detections correctly`() {
        val detections = listOf(
            DetectionResult("person", 0.88f, BoundingBox(0.2f, 0.2f, 0.6f, 0.6f)),
            DetectionResult("person", 0.92f, BoundingBox(0.21f, 0.21f, 0.61f, 0.61f)),  // Highest
            DetectionResult("person", 0.75f, BoundingBox(0.22f, 0.22f, 0.62f, 0.62f)),
            DetectionResult("person", 0.64f, BoundingBox(0.23f, 0.23f, 0.63f, 0.63f))
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(1, deduped.size)
        assertEquals(0.92f, deduped[0].confidence, 0.001f)
    }
    
    @Test
    fun `apply handles mixed overlapping and non-overlapping detections`() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.3f, 0.3f)),
            DetectionResult("chair", 0.78f, BoundingBox(0.11f, 0.11f, 0.31f, 0.31f)),  // Overlaps with first
            DetectionResult("table", 0.85f, BoundingBox(0.5f, 0.5f, 0.8f, 0.8f)),      // Different object
            DetectionResult("person", 0.68f, BoundingBox(0.7f, 0.1f, 0.9f, 0.3f))      // Different location
        )
        
        val deduped = nms.apply(detections)
        
        assertEquals(3, deduped.size)
        assertTrue(deduped.any { it.label == "chair" && it.confidence == 0.92f })
        assertTrue(deduped.any { it.label == "table" })
        assertTrue(deduped.any { it.label == "person" })
    }
    
    // Test IoU calculation
    
    @Test
    fun `calculateIoU returns correct overlap for partially overlapping boxes`() {
        val box1 = BoundingBox(0.0f, 0.0f, 0.5f, 0.5f)  // Area = 0.25
        val box2 = BoundingBox(0.25f, 0.25f, 0.75f, 0.75f)  // Area = 0.25
        
        val iou = box1.calculateIoU(box2)
        
        // Intersection = 0.25x0.25 = 0.0625
        // Union = 0.25 + 0.25 - 0.0625 = 0.4375
        // IoU = 0.0625 / 0.4375 ≈ 0.143
        assertTrue(iou > 0.1f && iou < 0.2f)
    }
    
    @Test
    fun `calculateIoU returns 0 for non-overlapping boxes`() {
        val box1 = BoundingBox(0.0f, 0.0f, 0.3f, 0.3f)
        val box2 = BoundingBox(0.6f, 0.6f, 0.9f, 0.9f)
        
        val iou = box1.calculateIoU(box2)
        
        assertEquals(0f, iou, 0.001f)
    }
    
    @Test
    fun `calculateIoU returns 1 for identical boxes`() {
        val box1 = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
        val box2 = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
        
        val iou = box1.calculateIoU(box2)
        
        assertEquals(1.0f, iou, 0.001f)
    }
    
    @Test
    fun `calculateIoU returns high value for mostly overlapping boxes`() {
        val box1 = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
        val box2 = BoundingBox(0.12f, 0.12f, 0.52f, 0.52f)  // Slightly shifted
        
        val iou = box1.calculateIoU(box2)
        
        // Should be significantly above 0.5 threshold
        assertTrue(iou > 0.7f)
    }
    
    @Test
    fun `calculateIoU handles boxes touching at edge`() {
        val box1 = BoundingBox(0.0f, 0.0f, 0.5f, 0.5f)
        val box2 = BoundingBox(0.5f, 0.0f, 1.0f, 0.5f)  // Touching at x=0.5
        
        val iou = box1.calculateIoU(box2)
        
        // No overlap (touching is not overlapping in continuous space)
        assertEquals(0f, iou, 0.001f)
    }
    
    @Test
    fun `calculateIoU handles box completely inside another`() {
        val box1 = BoundingBox(0.0f, 0.0f, 1.0f, 1.0f)  // Large box
        val box2 = BoundingBox(0.25f, 0.25f, 0.75f, 0.75f)  // Small box inside
        
        val iou = box1.calculateIoU(box2)
        
        // Intersection = 0.5*0.5 = 0.25
        // Union = 1.0 + 0.25 - 0.25 = 1.0
        // IoU = 0.25 / 1.0 = 0.25
        assertEquals(0.25f, iou, 0.01f)
    }
    
    @Test
    fun `calculateIoU is commutative`() {
        val box1 = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
        val box2 = BoundingBox(0.3f, 0.3f, 0.7f, 0.7f)
        
        val iou1 = box1.calculateIoU(box2)
        val iou2 = box2.calculateIoU(box1)
        
        assertEquals(iou1, iou2, 0.001f)
    }
}
