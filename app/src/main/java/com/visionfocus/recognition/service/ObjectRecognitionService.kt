package com.visionfocus.recognition.service

import android.graphics.Bitmap
import android.util.Log
import com.visionfocus.permissions.manager.PermissionManager
import com.visionfocus.recognition.camera.CameraManager
import com.visionfocus.recognition.inference.TFLiteInferenceEngine
import com.visionfocus.recognition.models.DetectionResult
import com.visionfocus.recognition.models.RecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recognition state machine for accessibility integration (Story 2.4+)
 */
sealed class RecognitionState {
    object Idle : RecognitionState()
    object Capturing : RecognitionState()
    object Analyzing : RecognitionState()
    data class Success(val results: List<DetectionResult>) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

/**
 * Service orchestrating camera capture and TFLite inference for object recognition
 * 
 * Flow: Check permissions → Initialize TFLite → Capture frame → Run inference → Return results
 * 
 * Validates latency requirements: ≤320ms average, ≤500ms maximum
 * Ensures zero network calls (all processing on-device)
 * 
 * State management: Exposes RecognitionState via StateFlow for TalkBack announcements (Story 2.4)
 */
@Singleton
class ObjectRecognitionService @Inject constructor(
    private val permissionManager: PermissionManager,
    private val cameraManager: CameraManager,
    private val tfliteEngine: TFLiteInferenceEngine
) {
    companion object {
        private const val TAG = "ObjectRecognitionService"
        private const val MAX_LATENCY_MS = 500
        private const val TARGET_LATENCY_MS = 320
    }
    
    private var isInitialized = false
    
    // State management for accessibility integration (Story 2.4+)
    private val _state = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val state: StateFlow<RecognitionState> = _state.asStateFlow()
    
    /**
     * Initialize the recognition service
     * Must be called once before recognizeObject()
     * 
     * CRITICAL: This should be called in Application.onCreate() to ensure
     * service is ready before any Fragment/Activity uses it
     */
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Service already initialized - skipping")
            return
        }
        
        Log.d(TAG, "Initializing recognition service...")
        
        try {
            val startTime = System.currentTimeMillis()
            tfliteEngine.initialize()
            val duration = System.currentTimeMillis() - startTime
            isInitialized = true
            Log.d(TAG, "Recognition service initialized successfully in ${duration}ms")
        } catch (e: Exception) {
            isInitialized = false
            Log.e(TAG, "Service initialization failed", e)
            throw IllegalStateException("Failed to initialize recognition service: ${e.message}", e)
        }
    }
    
    /**
     * Start the recognition camera for continuous scanning mode (Story 4.4)
     * Must be called before attempting frame capture for continuous scanning
     * 
     * @param lifecycleOwner Fragment/Activity to bind camera lifecycle
     * @throws IllegalStateException if camera start fails
     */
    suspend fun startRecognitionCamera(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        try {
            Log.d(TAG, "Starting recognition camera...")
            // CameraManager.startCamera() requires a callback for frame processing
            // For continuous scanning, we don't use the callback - we call captureFrame() explicitly
            cameraManager.startCamera(lifecycleOwner) { /* No-op callback */ }
            Log.d(TAG, "Recognition camera started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recognition camera", e)
            throw IllegalStateException("Failed to start recognition camera: ${e.message}", e)
        }
    }
    
    /**
     * Perform object recognition on current camera frame
     * 
     * Pipeline:
     * 1. Check camera permission (from Epic 1)
     * 2. Capture camera frame
     * 3. Run TFLite inference
     * 4. Parse and return results
     * 
     * @return RecognitionResult with detected objects, timestamp, and latency
     * @throws SecurityException if camera permission not granted
     * @throws IllegalStateException if service not initialized
     */
    suspend fun recognizeObject(): RecognitionResult = withContext(Dispatchers.Default) {
        // Pre-check: Story 1.5 permission system
        if (!permissionManager.isCameraPermissionGranted()) {
            _state.value = RecognitionState.Error("Camera permission not granted")
            throw SecurityException("Camera permission not granted")
        }
        
        // Verify initialization
        if (!isInitialized) {
            _state.value = RecognitionState.Error("Service not initialized")
            throw IllegalStateException("Service not initialized. Call initialize() first.")
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            // State: Starting capture
            _state.value = RecognitionState.Capturing
            
            // Capture frame from camera
            val frame = cameraManager.captureFrame()
            
            // State: Running inference
            _state.value = RecognitionState.Analyzing
            
            // Run TFLite inference
            val detections = tfliteEngine.infer(frame)
            
            // Calculate latency
            val latency = System.currentTimeMillis() - startTime
            
            // Log performance
            logPerformance(latency)
            
            // Validate latency requirements
            if (latency > MAX_LATENCY_MS) {
                Log.w(TAG, "Latency exceeded maximum: ${latency}ms > ${MAX_LATENCY_MS}ms")
            }
            
            val result = RecognitionResult(
                detections = detections,
                timestampMs = System.currentTimeMillis(),
                latencyMs = latency
            )
            
            // State: Success
            _state.value = RecognitionState.Success(detections)
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Recognition failed", e)
            _state.value = RecognitionState.Error(e.message ?: "Unknown error")
            throw IllegalStateException("Recognition failed: ${e.message}", e)
        }
    }
    
    /**
     * Perform object recognition on provided Bitmap (Story 2.4)
     * 
     * Pipeline:
     * 1. Verify service initialized
     * 2. Convert Bitmap to ByteBuffer
     * 3. Run TFLite inference on ByteBuffer
     * 4. Parse and return results
     * 
     * @param bitmap Camera-captured frame to analyze
     * @return RecognitionResult with detected objects, timestamp, and latency
     * @throws IllegalStateException if service not initialized
     */
    suspend fun recognizeObject(bitmap: Bitmap): RecognitionResult = withContext(Dispatchers.Default) {
        // Verify initialization
        if (!isInitialized) {
            _state.value = RecognitionState.Error("Service not initialized")
            throw IllegalStateException("Service not initialized. Call initialize() first.")
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            // State: Running inference
            _state.value = RecognitionState.Analyzing
            
            // Convert Bitmap to ByteBuffer
            val byteBuffer = bitmapToByteBuffer(bitmap)
            
            // Run TFLite inference on ByteBuffer
            val detections = tfliteEngine.infer(byteBuffer)
            
            // Calculate latency
            val latency = System.currentTimeMillis() - startTime
            
            // Log performance
            logPerformance(latency)
            
            // Validate latency requirements
            if (latency > MAX_LATENCY_MS) {
                Log.w(TAG, "Latency exceeded maximum: ${latency}ms > ${MAX_LATENCY_MS}ms")
            }
            
            val result = RecognitionResult(
                detections = detections,
                timestampMs = System.currentTimeMillis(),
                latencyMs = latency
            )
            
            // State: Success
            _state.value = RecognitionState.Success(detections)
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Recognition failed", e)
            _state.value = RecognitionState.Error(e.message ?: "Unknown error")
            throw IllegalStateException("Recognition failed: ${e.message}", e)
        }
    }
    
    /**
     * Convert Bitmap to ByteBuffer for TFLite inference
     * Story 2.4: Helper method for bitmap-based recognition
     * 
     * @param bitmap Camera-captured frame (will be scaled to 300×300)
     * @return ByteBuffer with normalized RGB values [0-255]
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap): java.nio.ByteBuffer {
        val modelInputWidth = 300
        val modelInputHeight = 300
        val bytesPerChannel = 1 // INT8 quantized model
        val pixelSize = 3 // RGB
        
        val inputBuffer = java.nio.ByteBuffer.allocateDirect(
            modelInputWidth * modelInputHeight * pixelSize * bytesPerChannel
        )
        inputBuffer.order(java.nio.ByteOrder.nativeOrder())
        
        // Scale bitmap to 300×300 if needed
        val scaledBitmap = if (bitmap.width != modelInputWidth || bitmap.height != modelInputHeight) {
            android.graphics.Bitmap.createScaledBitmap(
                bitmap,
                modelInputWidth,
                modelInputHeight,
                true
            )
        } else {
            bitmap
        }
        
        // Convert bitmap to ByteBuffer (RGB format)
        val intValues = IntArray(modelInputWidth * modelInputHeight)
        scaledBitmap.getPixels(
            intValues,
            0,
            scaledBitmap.width,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height
        )
        
        var pixel = 0
        for (i in 0 until modelInputWidth) {
            for (j in 0 until modelInputHeight) {
                val value = intValues[pixel++]
                
                // Extract RGB values and put in buffer
                inputBuffer.put(((value shr 16) and 0xFF).toByte()) // R
                inputBuffer.put(((value shr 8) and 0xFF).toByte())  // G
                inputBuffer.put((value and 0xFF).toByte())          // B
            }
        }
        
        inputBuffer.rewind()
        return inputBuffer
    }
    
    /**
     * Shutdown service and release resources
     */
    fun shutdown() {
        cameraManager.shutdown()
        tfliteEngine.close()
        isInitialized = false
        _state.value = RecognitionState.Idle
        Log.d(TAG, "Recognition service shutdown")
    }
    
    private fun logPerformance(latency: Long) {
        val status = when {
            latency <= TARGET_LATENCY_MS -> "✓ EXCELLENT"
            latency <= MAX_LATENCY_MS -> "✓ ACCEPTABLE"
            else -> "✗ EXCEEDS MAX"
        }
        
        Log.d(TAG, "Recognition latency: ${latency}ms ($status)")
        
        if (latency > TARGET_LATENCY_MS) {
            Log.w(TAG, "Latency ${latency}ms exceeds target ${TARGET_LATENCY_MS}ms")
        }
    }
}
