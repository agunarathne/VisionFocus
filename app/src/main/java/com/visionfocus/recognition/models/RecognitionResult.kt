package com.visionfocus.recognition.models

/**
 * Complete recognition result including all detections and timing information
 * 
 * @property detections List of detected objects with confidence scores and bounding boxes
 * @property timestampMs Unix timestamp when recognition was performed
 * @property latencyMs Inference latency in milliseconds (capture + preprocessing + inference + parsing)
 */
data class RecognitionResult(
    val detections: List<DetectionResult>,
    val timestampMs: Long,
    val latencyMs: Long
)
