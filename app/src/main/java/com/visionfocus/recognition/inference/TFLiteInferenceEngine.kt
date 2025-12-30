package com.visionfocus.recognition.inference

import android.content.Context
import android.os.Build
import android.util.Log
import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TensorFlow Lite inference engine for object detection using SSD MobileNet V1
 * 
 * Model: ssd_mobilenet_v1_quantized.tflite (~4MB INT8 quantized)
 * Input: 300×300×3 RGB image as ByteBuffer
 * Output: Bounding boxes, class IDs, confidence scores, detection count
 * 
 * Thread-safe singleton managed by Hilt
 */
@Singleton
class TFLiteInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val labels: List<String> by lazy { loadLabels() }
    
    companion object {
        private const val TAG = "TFLiteInferenceEngine"
        private const val MODEL_PATH = "models/ssd_mobilenet_v1_quantized.tflite"
        private const val LABELS_PATH = "models/coco_labels.txt"
        private const val MAX_DETECTIONS = 10
        private const val NUM_THREADS = 4
    }
    
    /**
     * Initialize the TFLite interpreter and load the model
     * Must be called before inference()
     */
    fun initialize() {
        if (interpreter != null) {
            Log.d(TAG, "Interpreter already initialized")
            return
        }
        
        try {
            val modelBuffer = loadModelFile()
            val options = createInterpreterOptions()
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "TFLite interpreter initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TFLite interpreter", e)
            throw IllegalStateException("TFLite initialization failed: ${e.message}", e)
        }
    }
    
    /**
     * Run object detection inference on input image
     * 
     * @param input ByteBuffer containing 300×300×3 RGB image (normalized [0-255])
     * @return List of DetectionResult with label, confidence, and bounding box
     * @throws IllegalStateException if interpreter not initialized
     */
    fun infer(input: ByteBuffer): List<DetectionResult> {
        val interpreter = interpreter 
            ?: throw IllegalStateException("Interpreter not initialized. Call initialize() first.")
        
        // Allocate output tensors
        // Output 0: Bounding boxes [1, 10, 4] - (ymin, xmin, ymax, xmax) normalized [0-1]
        val boundingBoxes = Array(1) { Array(MAX_DETECTIONS) { FloatArray(4) } }
        
        // Output 1: Class IDs [1, 10] - COCO class indices [0-79]
        val classIds = Array(1) { FloatArray(MAX_DETECTIONS) }
        
        // Output 2: Confidence scores [1, 10] - [0-1]
        val scores = Array(1) { FloatArray(MAX_DETECTIONS) }
        
        // Output 3: Detection count [1]
        val count = FloatArray(1)
        
        // Create output map
        val outputs = mapOf(
            0 to boundingBoxes,
            1 to classIds,
            2 to scores,
            3 to count
        )
        
        try {
            // Run inference
            interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)
            
            // Parse results
            val detectionCount = count[0].toInt().coerceIn(0, MAX_DETECTIONS)
            return parseDetections(boundingBoxes, classIds, scores, detectionCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            return emptyList()
        }
    }
    
    /**
     * Map COCO class ID to human-readable label
     * 
     * @param classId COCO class index [0-79]
     * @return Label string (e.g., "person", "chair") or "unknown" if invalid
     */
    fun mapClassIdToLabel(classId: Int): String {
        return labels.getOrNull(classId) ?: "unknown"
    }
    
    /**
     * Release interpreter resources
     * Should be called when engine is no longer needed
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "TFLite interpreter closed")
    }
    
    // Private helper methods
    
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    private fun createInterpreterOptions(): Interpreter.Options {
        return Interpreter.Options().apply {
            // Use 4 threads for optimal mid-range device performance
            setNumThreads(NUM_THREADS)
            
            // Enable NNAPI delegate if available (Android 9+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    addDelegate(NnApiDelegate())
                    Log.d(TAG, "NNAPI delegate enabled")
                } catch (e: Exception) {
                    Log.w(TAG, "NNAPI delegate unavailable, using CPU: ${e.message}")
                }
            }
        }
    }
    
    private fun loadLabels(): List<String> {
        return try {
            context.assets.open(LABELS_PATH).bufferedReader().use { reader ->
                reader.readLines().map { it.trim() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load labels", e)
            emptyList()
        }
    }
    
    private fun parseDetections(
        boundingBoxes: Array<Array<FloatArray>>,
        classIds: Array<FloatArray>,
        scores: Array<FloatArray>,
        detectionCount: Int
    ): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        
        for (i in 0 until detectionCount) {
            val classId = classIds[0][i].toInt()
            val confidence = scores[0][i]
            val bbox = boundingBoxes[0][i]
            
            // Get label for class ID
            val label = mapClassIdToLabel(classId)
            
            // Skip invalid detections
            if (label == "???" || label == "unknown") {
                continue
            }
            
            // Create bounding box
            val boundingBox = BoundingBox(
                yMin = bbox[0],
                xMin = bbox[1],
                yMax = bbox[2],
                xMax = bbox[3]
            )
            
            results.add(
                DetectionResult(
                    label = label,
                    confidence = confidence,
                    boundingBox = boundingBox
                )
            )
        }
        
        return results
    }
}
