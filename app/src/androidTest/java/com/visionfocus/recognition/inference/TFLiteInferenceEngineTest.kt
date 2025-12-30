package com.visionfocus.recognition.inference

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unit tests for TFLiteInferenceEngine
 * 
 * Tests:
 * - Model loading from assets
 * - Inference with mock input
 * - Class ID to label mapping
 * - Output tensor format validation
 */
@RunWith(AndroidJUnit4::class)
class TFLiteInferenceEngineTest {
    
    private lateinit var context: Context
    private lateinit var engine: TFLiteInferenceEngine
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        engine = TFLiteInferenceEngine(context)
    }
    
    @After
    fun teardown() {
        engine.close()
    }
    
    @Test
    fun testModelLoadsSuccessfully() {
        // Should not throw exception
        engine.initialize()
        
        // Verify engine is ready by running a simple inference
        val mockInput = createMockByteBuffer()
        val results = engine.infer(mockInput)
        
        // Results can be empty if no detections, but should not crash
        assertNotNull(results)
    }
    
    @Test
    fun testInferenceReturnsValidDetectionResults() {
        engine.initialize()
        
        val mockInput = createMockByteBuffer()
        val results = engine.infer(mockInput)
        
        // Validate result format
        assertTrue("Results size should be ≤ 10", results.size <= 10)
        
        results.forEach { detection ->
            // Confidence should be in valid range
            assertTrue(
                "Confidence ${detection.confidence} out of range",
                detection.confidence in 0.0f..1.0f
            )
            
            // Label should not be empty
            assertFalse("Label should not be empty", detection.label.isEmpty())
            
            // Bounding box should have normalized coordinates [0-1]
            with(detection.boundingBox) {
                assertTrue("yMin out of range", yMin in 0.0f..1.0f)
                assertTrue("xMin out of range", xMin in 0.0f..1.0f)
                assertTrue("yMax out of range", yMax in 0.0f..1.0f)
                assertTrue("xMax out of range", xMax in 0.0f..1.0f)
                
                // Logical constraints
                assertTrue("yMax should be > yMin", yMax > yMin)
                assertTrue("xMax should be > xMin", xMax > xMin)
            }
        }
    }
    
    @Test
    fun testClassIdToLabelMapping() {
        engine.initialize()
        
        // Test known COCO labels
        assertEquals("person", engine.mapClassIdToLabel(0))
        assertEquals("bicycle", engine.mapClassIdToLabel(1))
        assertEquals("car", engine.mapClassIdToLabel(2))
        assertEquals("chair", engine.mapClassIdToLabel(56))
        assertEquals("tv", engine.mapClassIdToLabel(62))
        
        // Test invalid class ID
        val invalidLabel = engine.mapClassIdToLabel(999)
        assertEquals("unknown", invalidLabel)
    }
    
    @Test
    fun testInferenceWithoutInitializationThrowsException() {
        val mockInput = createMockByteBuffer()
        
        try {
            engine.infer(mockInput)
            fail("Should throw IllegalStateException when not initialized")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("not initialized") == true)
        }
    }
    
    @Test
    fun testMultipleInferenceCalls() {
        engine.initialize()
        
        val mockInput = createMockByteBuffer()
        
        // Run inference multiple times
        repeat(5) {
            val results = engine.infer(mockInput)
            assertNotNull(results)
            assertTrue(results.size <= 10)
        }
    }
    
    @Test
    fun testEngineCanBeClosedAndReopened() {
        engine.initialize()
        
        // First inference
        val mockInput = createMockByteBuffer()
        val results1 = engine.infer(mockInput)
        assertNotNull(results1)
        
        // Close engine
        engine.close()
        
        // Reinitialize
        engine.initialize()
        
        // Second inference should work
        val results2 = engine.infer(mockInput)
        assertNotNull(results2)
    }
    
    // Helper method to create mock ByteBuffer input
    private fun createMockByteBuffer(): ByteBuffer {
        // Create 300×300×3 RGB image buffer (float32)
        val buffer = ByteBuffer.allocateDirect(4 * 300 * 300 * 3)
        buffer.order(ByteOrder.nativeOrder())
        
        // Fill with random pixel values [0-255]
        repeat(300 * 300 * 3) {
            buffer.putFloat((0..255).random().toFloat())
        }
        
        buffer.rewind()
        return buffer
    }
}
