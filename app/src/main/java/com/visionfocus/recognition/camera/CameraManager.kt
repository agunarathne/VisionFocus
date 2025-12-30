package com.visionfocus.recognition.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Camera manager for capturing frames using CameraX
 * 
 * Responsibilities:
 * - Bind CameraX lifecycle to Fragment/Activity
 * - Capture frames via ImageAnalysis use case
 * - Convert ImageProxy → Bitmap → 300×300 ByteBuffer for TFLite
 * 
 * Thread-safe singleton managed by Hilt
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    
    companion object {
        private const val TAG = "CameraManager"
        private const val TARGET_WIDTH = 640
        private const val TARGET_HEIGHT = 480
        private const val MODEL_INPUT_SIZE = 300
        private const val CHANNELS = 3
        private const val BYTES_PER_FLOAT = 4
        private const val BUFFER_SIZE = BYTES_PER_FLOAT * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * CHANNELS
    }
    
    /**
     * Start camera with ImageAnalysis use case
     * 
     * @param lifecycleOwner Lifecycle owner (Fragment/Activity) to bind camera
     * @param onFrameReady Callback invoked when frame is processed into ByteBuffer
     */
    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        onFrameReady: (ByteBuffer) -> Unit
    ) = withContext(Dispatchers.Main) {
        try {
            val provider = getCameraProvider()
            cameraProvider = provider
            
            // Configure ImageAnalysis use case
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(TARGET_WIDTH, TARGET_HEIGHT))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            val byteBuffer = processImageProxy(imageProxy)
                            onFrameReady(byteBuffer)
                        } catch (e: Exception) {
                            Log.e(TAG, "Frame processing failed", e)
                        } finally {
                            imageProxy.close()
                        }
                    }
                }
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Bind to lifecycle
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )
            
            Log.d(TAG, "Camera started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Camera startup failed", e)
            throw IllegalStateException("Failed to start camera: ${e.message}", e)
        }
    }
    
    /**
     * Capture a single frame and convert to ByteBuffer for TFLite inference
     * This is a simplified interface for single-shot recognition
     * 
     * @return ByteBuffer containing 300×300×3 RGB image normalized [0-255]
     */
    suspend fun captureFrame(): ByteBuffer = suspendCancellableCoroutine { continuation ->
        var resumed = false
        var tempAnalysis: ImageAnalysis? = null
        
        val provider = cameraProvider ?: run {
            if (!resumed) {
                resumed = true
                continuation.resumeWithException(
                    IllegalStateException("Camera not started. Call startCamera() first.")
                )
            }
            return@suspendCancellableCoroutine
        }
        
        // Create temporary ImageAnalysis use case
        tempAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(TARGET_WIDTH, TARGET_HEIGHT))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (!resumed) {
                        resumed = true
                        try {
                            val byteBuffer = processImageProxy(imageProxy)
                            continuation.resume(byteBuffer)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        } finally {
                            imageProxy.close()
                            // Unbind to prevent memory leak
                            try {
                                provider.unbind(analysis)
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to unbind temp analysis", e)
                            }
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            }
        
        continuation.invokeOnCancellation {
            tempAnalysis?.let { provider.unbind(it) }
        }
    }
    
    /**
     * Stop camera and release resources
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
        imageAnalysis = null
        cameraProvider = null
        Log.d(TAG, "Camera stopped")
    }
    
    /**
     * Shutdown executor when manager is destroyed
     */
    fun shutdown() {
        stopCamera()
        cameraExecutor.shutdown()
        Log.d(TAG, "Camera executor shutdown")
    }
    
    // Private helper methods
    
    private suspend fun getCameraProvider(): ProcessCameraProvider = 
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    continuation.resume(provider)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    
    /**
     * Process ImageProxy and convert to TFLite input ByteBuffer
     * 
     * Pipeline: ImageProxy (YUV) → Bitmap (RGBA) → Resized Bitmap (300×300) → ByteBuffer (RGB float32)
     */
    private fun processImageProxy(imageProxy: ImageProxy): ByteBuffer {
        // Convert YUV ImageProxy to Bitmap
        val bitmap = imageProxyToBitmap(imageProxy)
        
        // Resize to 300×300 (model input requirement)
        val resized = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
        
        // Convert to ByteBuffer
        val byteBuffer = bitmapToByteBuffer(resized)
        
        // Clean up - check recycled state before recycling
        if (resized != bitmap && !resized.isRecycled) {
            resized.recycle()
        }
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
        
        return byteBuffer
    }
    
    /**
     * Convert YUV420 ImageProxy to RGBA Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    /**
     * Convert Bitmap to ByteBuffer for TFLite inference
     * Output: 300×300×3 RGB image as Float32 [0-255]
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var pixel = 0
        for (i in 0 until MODEL_INPUT_SIZE) {
            for (j in 0 until MODEL_INPUT_SIZE) {
                val pixelValue = intValues[pixel++]
                
                // Extract RGB channels (INT8 quantized model expects [0-255])
                byteBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())  // R
                byteBuffer.putFloat(((pixelValue shr 8) and 0xFF).toFloat())   // G
                byteBuffer.putFloat((pixelValue and 0xFF).toFloat())           // B
            }
        }
        
        byteBuffer.rewind()
        return byteBuffer
    }
}
