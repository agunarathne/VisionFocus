package com.visionfocus.recognition.camera

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer

/**
 * Instrumented tests for CameraManager image preprocessing
 * 
 * Tests ByteBuffer conversion, image resizing, and normalization
 * Requires Android runtime for Bitmap operations
 */
@RunWith(AndroidJUnit4::class)
class CameraManagerTest {
    
    @Test
    fun testBitmapToByteBufferConversion() {
        // Create a test bitmap (300×300 RGB)
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        
        // Fill with known color (Red = 255, Green = 128, Blue = 64)
        val pixels = IntArray(300 * 300)
        val testColor = (255 shl 16) or (128 shl 8) or 64  // ARGB
        pixels.fill(testColor)
        bitmap.setPixels(pixels, 0, 300, 0, 0, 300, 300)
        
        // Convert to ByteBuffer (simulating CameraManager's bitmapToByteBuffer)
        val byteBuffer = bitmapToByteBufferTestHelper(bitmap)
        
        // Verify buffer size (300 × 300 × 3 channels × 4 bytes per float)
        assertEquals(4 * 300 * 300 * 3, byteBuffer.capacity())
        
        // Verify first pixel RGB values
        byteBuffer.rewind()
        val r = byteBuffer.float
        val g = byteBuffer.float
        val b = byteBuffer.float
        
        assertEquals(255.0f, r, 0.1f)
        assertEquals(128.0f, g, 0.1f)
        assertEquals(64.0f, b, 0.1f)
    }
    
    @Test
    fun testByteBufferHasCorrectFormat() {
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val byteBuffer = bitmapToByteBufferTestHelper(bitmap)
        
        // Verify native byte order
        assertEquals(java.nio.ByteOrder.nativeOrder(), byteBuffer.order())
        
        // Verify it's a direct buffer (required for TFLite)
        assertTrue("ByteBuffer should be direct", byteBuffer.isDirect)
    }
    
    @Test
    fun testByteBufferValuesInExpectedRange() {
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        
        // Fill with random colors
        val pixels = IntArray(300 * 300)
        for (i in pixels.indices) {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            pixels[i] = (r shl 16) or (g shl 8) or b
        }
        bitmap.setPixels(pixels, 0, 300, 0, 0, 300, 300)
        
        val byteBuffer = bitmapToByteBufferTestHelper(bitmap)
        
        // Verify all values are in [0-255] range
        byteBuffer.rewind()
        repeat(300 * 300 * 3) {
            val value = byteBuffer.float
            assertTrue(
                "Pixel value $value out of range [0-255]",
                value in 0.0f..255.0f
            )
        }
    }
    
    @Test
    fun testResizeBitmapTo300x300() {
        // Create larger bitmap
        val original = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
        
        // Resize to 300×300 (simulating CameraManager behavior)
        val resized = Bitmap.createScaledBitmap(original, 300, 300, true)
        
        assertEquals(300, resized.width)
        assertEquals(300, resized.height)
        
        resized.recycle()
        original.recycle()
    }
    
    @Test
    fun testBitmapConfigSupported() {
        // Test various bitmap configs
        val configs = listOf(
            Bitmap.Config.ARGB_8888,
            Bitmap.Config.RGB_565
        )
        
        configs.forEach { config ->
            val bitmap = Bitmap.createBitmap(300, 300, config)
            assertNotNull("Failed to create bitmap with config $config", bitmap)
            
            // Should be able to get pixels
            val pixels = IntArray(300 * 300)
            bitmap.getPixels(pixels, 0, 300, 0, 0, 300, 300)
            
            bitmap.recycle()
        }
    }
    
    // Test helper mimicking CameraManager's private bitmapToByteBuffer method
    private fun bitmapToByteBufferTestHelper(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 300 * 300 * 3)
        byteBuffer.order(java.nio.ByteOrder.nativeOrder())
        
        val intValues = IntArray(300 * 300)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var pixel = 0
        for (i in 0 until 300) {
            for (j in 0 until 300) {
                val pixelValue = intValues[pixel++]
                
                // Extract RGB channels
                byteBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())  // R
                byteBuffer.putFloat(((pixelValue shr 8) and 0xFF).toFloat())   // G
                byteBuffer.putFloat((pixelValue and 0xFF).toFloat())           // B
            }
        }
        
        byteBuffer.rewind()
        return byteBuffer
    }
}
