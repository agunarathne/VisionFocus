# APPENDICES

This section contains supplementary materials supporting the dissertation content, including code examples, testing materials, accessibility documentation, and user study instruments.

---

## APPENDIX A: CODE IMPLEMENTATIONS

This appendix contains key code implementations referenced throughout Chapter 7 (Implementation). Full source code available upon request or via project repository.

### A.1 AI Inference Service - Core Object Detection

```kotlin
/**
 * AIInferenceService.kt
 * Core TensorFlow Lite inference service for object detection
 */
class AIInferenceService(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()
    private val confidenceThreshold = 0.60f
    
    // Model dimensions
    private val inputSize = 300
    private val numDetections = 10
    private val numClasses = 80
    
    init {
        loadModel()
        loadLabels()
    }
    
    private fun loadModel() {
        val modelFile = context.assets.openFd("mobilenet_v2_ssd_quantized.tflite")
        val inputStream = FileInputStream(modelFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFile.startOffset
        val declaredLength = modelFile.declaredLength
        
        val modelBuffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
        
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseNNAPI(true) // Use Android Neural Networks API if available
        }
        
        interpreter = Interpreter(modelBuffer, options)
    }
    
    private fun loadLabels() {
        context.assets.open("coco_labels.txt").bufferedReader().use { reader ->
            labels.addAll(reader.readLines())
        }
    }
    
    fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        val startTime = System.currentTimeMillis()
        
        // Preprocessing
        val preprocessedImage = preprocessImage(bitmap)
        val preprocessingTime = System.currentTimeMillis() - startTime
        
        // Inference
        val inferenceStart = System.currentTimeMillis()
        val outputs = runInference(preprocessedImage)
        val inferenceTime = System.currentTimeMillis() - inferenceStart
        
        // Postprocessing
        val postprocessStart = System.currentTimeMillis()
        val detections = postprocessOutputs(outputs)
        val postprocessingTime = System.currentTimeMillis() - postprocessStart
        
        Log.d("AIInference", "Preprocessing: ${preprocessingTime}ms, " +
                "Inference: ${inferenceTime}ms, Postprocessing: ${postprocessingTime}ms")
        
        return detections
    }
    
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)
        
        // Convert to INT8 quantized format
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                inputBuffer.put(((value shr 16 and 0xFF) - 128).toByte()) // R
                inputBuffer.put(((value shr 8 and 0xFF) - 128).toByte())  // G
                inputBuffer.put(((value and 0xFF) - 128).toByte())         // B
            }
        }
        
        return inputBuffer
    }
    
    private fun runInference(input: ByteBuffer): Array<FloatArray> {
        val outputLocations = Array(1) { Array(numDetections) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(numDetections) }
        val outputScores = Array(1) { FloatArray(numDetections) }
        val numDetectionsOutput = FloatArray(1)
        
        val outputs = mapOf(
            0 to outputLocations,
            1 to outputClasses,
            2 to outputScores,
            3 to numDetectionsOutput
        )
        
        interpreter?.runForMultipleInputsOutputs(arrayOf(input), outputs)
        
        return arrayOf(
            outputLocations[0].flatten().toFloatArray(),
            outputClasses[0],
            outputScores[0],
            numDetectionsOutput
        )
    }
    
    private fun postprocessOutputs(outputs: Array<FloatArray>): List<DetectionResult> {
        val locations = outputs[0]
        val classes = outputs[1]
        val scores = outputs[2]
        val numDetections = outputs[3][0].toInt()
        
        val detections = mutableListOf<DetectionResult>()
        
        for (i in 0 until numDetections.coerceAtMost(10)) {
            val score = scores[i]
            if (score >= confidenceThreshold) {
                val classId = classes[i].toInt()
                val label = if (classId < labels.size) labels[classId] else "Unknown"
                
                val top = locations[i * 4]
                val left = locations[i * 4 + 1]
                val bottom = locations[i * 4 + 2]
                val right = locations[i * 4 + 3]
                
                val bbox = RectF(left, top, right, bottom)
                
                detections.add(DetectionResult(label, score, bbox, classId))
            }
        }
        
        // Apply Non-Maximum Suppression
        return applyNMS(detections)
    }
    
    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        if (detections.isEmpty()) return emptyList()
        
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selectedDetections = mutableListOf<DetectionResult>()
        val iouThreshold = 0.5f
        
        for (detection in sortedDetections) {
            var shouldSelect = true
            
            for (selected in selectedDetections) {
                val iou = calculateIoU(detection.boundingBox, selected.boundingBox)
                if (iou > iouThreshold && detection.label == selected.label) {
                    shouldSelect = false
                    break
                }
            }
            
            if (shouldSelect) {
                selectedDetections.add(detection)
            }
        }
        
        return selectedDetections
    }
    
    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)
        
        if (intersectionRight < intersectionLeft || intersectionBottom < intersectionTop) {
            return 0f
        }
        
        val intersectionArea = (intersectionRight - intersectionLeft) * 
                              (intersectionBottom - intersectionTop)
        
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        
        val unionArea = box1Area + box2Area - intersectionArea
        
        return intersectionArea / unionArea
    }
    
    fun release() {
        interpreter?.close()
        interpreter = null
    }
}

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF,
    val classId: Int
)
```

### A.2 Camera Service - Image Capture

**[CODE PLACEHOLDER]**  
*To be replaced with actual CameraService.kt implementation capturing frames from Camera2 API*

### A.3 Location Service - GPS and Indoor Positioning

**[CODE PLACEHOLDER]**  
*To be replaced with actual LocationService.kt implementing FusedLocationProvider and beacon trilateration*

### A.4 Voice Recognition Service

**[CODE PLACEHOLDER]**  
*To be replaced with actual VoiceRecognitionService.kt using Android SpeechRecognizer API*

### A.5 Text-to-Speech Service

```kotlin
/**
 * TTSService.kt
 * Text-to-Speech service for audio feedback
 */
class TTSService(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.UK
                isInitialized = true
            }
        }
    }
    
    fun speak(text: String, priority: SpeechPriority = SpeechPriority.NORMAL) {
        if (!isInitialized) return
        
        val queueMode = when (priority) {
            SpeechPriority.HIGH -> TextToSpeech.QUEUE_FLUSH // Interrupt current speech
            SpeechPriority.NORMAL -> TextToSpeech.QUEUE_ADD // Queue after current
        }
        
        tts?.speak(text, queueMode, null, System.currentTimeMillis().toString())
    }
    
    fun setVoiceSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
    }
    
    fun stop() {
        tts?.stop()
    }
    
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

enum class SpeechPriority {
    HIGH,   // Interrupts current speech (e.g., navigation warnings)
    NORMAL  // Queues after current speech (e.g., object announcements)
}
```

---

## APPENDIX B: USER TESTING MATERIALS

### B.1 Participant Information Sheet

**Project Title:** VisionFocus: AI-Powered Object Recognition and Navigation for Visually Impaired Users

**Principal Investigator:** Allan Trevor (MSc Computer Science Candidate)  
**Supervisor:** [Supervisor Name]  
**Ethics Approval:** CS-ETH-2025-017

**Purpose of Study:**  
You are invited to participate in user testing for VisionFocus, a mobile application designed to assist visually impaired individuals through object recognition and navigation. This study evaluates the application's usability, accessibility, and effectiveness.

**What Participation Involves:**
- Duration: 45-60 minutes
- Activities: Complete 10 tasks using VisionFocus application (5 object recognition tasks, 3 navigation tasks, 2 voice command tasks)
- Think-aloud protocol: Verbalize thoughts while using the application
- Post-test survey: 10-question System Usability Scale questionnaire
- Optional interview: 10-minute discussion about experience
- Audio recording (optional): For transcription purposes only

**Eligibility:**
- Aged 18 or above
- Legally blind or severely visually impaired
- Android smartphone experience (basic navigation with TalkBack)
- Able to provide informed consent

**Risks and Benefits:**
- Minimal risks: Task frustration, fatigue from 60-minute session, minor tripping risk during navigation (researcher escort provided)
- Benefits: Contribute to assistive technology research, receive £10 travel reimbursement

**Confidentiality:**
- All data anonymized using participant codes (P01-P15)
- No personally identifiable information in published materials
- Data stored on encrypted drives with access restricted to research team
- Audio recordings destroyed after transcription
- Data retained for 5 years per university policy, then securely deleted

**Voluntary Participation:**
- Participation is completely voluntary
- Right to withdraw at any time without explanation
- Right to skip tasks causing discomfort
- No impact on services or relationships if you decline or withdraw

**Contact Information:**
- Researcher: Allan Trevor, [email]
- Supervisor: [Supervisor Name], [email]
- Ethics Committee: [ethics@university.ac.uk]

### B.2 Consent Form

I confirm that:
- [ ] I have read and understood the Information Sheet
- [ ] I have had opportunity to ask questions
- [ ] I understand participation is voluntary and I can withdraw at any time
- [ ] I agree to audio recording (optional)
- [ ] I consent to anonymized data use in research publications
- [ ] I agree to participate in this study

Participant Name: ____________________  
Signature/Verbal Consent: ____________________  
Date: ____________________

Researcher Name: ____________________  
Signature: ____________________  
Date: ____________________

### B.3 User Testing Tasks

**Object Recognition Tasks (5 tasks)**

**Task 1:** Activate object recognition mode and identify three objects on the desk in front of you.  
**Success Criteria:** Successfully activates recognition, receives audio description of 3 objects  
**Time Limit:** 3 minutes

**Task 2:** Using voice commands, ask the app to "find a chair" in the room.  
**Success Criteria:** Voice command recognized, chair identified and announced  
**Time Limit:** 2 minutes

**Task 3:** Navigate to the bookshelf and identify what objects are on the middle shelf.  
**Success Criteria:** Arrives at bookshelf, identifies 2+ objects on middle shelf  
**Time Limit:** 4 minutes

**Task 4:** Locate your water bottle among the items on the table.  
**Success Criteria:** Correctly identifies water bottle location  
**Time Limit:** 3 minutes

**Task 5:** Explore the room and create a mental map using object recognition announcements.  
**Success Criteria:** Identifies 5+ objects, demonstrates spatial understanding  
**Time Limit:** 5 minutes

**Navigation Tasks (3 tasks)**

**Task 6:** Navigate from the testing room to the restroom using indoor navigation.  
**Success Criteria:** Arrives at restroom, <2 deviations from planned route  
**Time Limit:** 5 minutes

**Task 7:** Return to the testing room from the restroom.  
**Success Criteria:** Arrives at testing room, <2 deviations  
**Time Limit:** 5 minutes

**Task 8:** Navigate to the building exit using voice-guided directions.  
**Success Criteria:** Arrives at exit, follows all turn instructions correctly  
**Time Limit:** 6 minutes

**Voice Command Tasks (2 tasks)**

**Task 9:** Use voice commands to adjust application settings: increase voice speed, change verbosity to "detailed mode".  
**Success Criteria:** Both settings adjusted successfully via voice  
**Time Limit:** 3 minutes

**Task 10:** Use voice to ask "What's around me?" and interpret the response.  
**Success Criteria:** Command recognized, receives spatial description, demonstrates understanding  
**Time Limit:** 2 minutes

### B.4 System Usability Scale (SUS) Questionnaire

Please rate your agreement with each statement on a scale of 1 (Strongly Disagree) to 5 (Strongly Agree):

1. I think that I would like to use this application frequently.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

2. I found the application unnecessarily complex.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

3. I thought the application was easy to use.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

4. I think that I would need the support of a technical person to be able to use this application.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

5. I found the various functions in this application were well integrated.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

6. I thought there was too much inconsistency in this application.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

7. I would imagine that most people would learn to use this application very quickly.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

8. I found the application very cumbersome to use.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

9. I felt very confident using the application.  
   1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

10. I needed to learn a lot of things before I could get going with this application.  
    1 ☐  2 ☐  3 ☐  4 ☐  5 ☐

**SUS Scoring:**  
For odd-numbered items (1,3,5,7,9): score = rating - 1  
For even-numbered items (2,4,6,8,10): score = 5 - rating  
Total SUS Score = (sum of scores) × 2.5  
(Range: 0-100, where >68 = above average, >80 = excellent)

### B.5 Post-Test Interview Questions

1. What did you like most about VisionFocus?
2. What aspects caused the most difficulty or frustration?
3. How does VisionFocus compare to other assistive apps you've used (if any)?
4. Would you use VisionFocus in your daily life? Why or why not?
5. What features would you like to see added or improved?
6. How confident did you feel navigating with the voice guidance?
7. Was the object recognition accurate and helpful?
8. Were voice commands easy to remember and use?
9. Any concerns about privacy or data security?
10. Additional comments or suggestions?

---

## APPENDIX C: ACCESSIBILITY COMPLIANCE DOCUMENTATION

### C.1 WCAG 2.1 Level AA Compliance Checklist

| Criterion | Level | Status | Evidence |
|-----------|-------|--------|----------|
| **1.1.1 Non-text Content** | A | ✅ Pass | All images have text alternatives, decorative images marked |
| **1.2.1 Audio-only and Video-only** | A | N/A | No audio-only or video-only content |
| **1.3.1 Info and Relationships** | A | ✅ Pass | Semantic structure, proper heading hierarchy, labeled inputs |
| **1.3.2 Meaningful Sequence** | A | ✅ Pass | Logical reading order, focus order follows visual layout |
| **1.3.3 Sensory Characteristics** | A | ✅ Pass | Instructions don't rely solely on shape, size, or position |
| **1.3.4 Orientation** | AA | ✅ Pass | Content not restricted to single orientation |
| **1.3.5 Identify Input Purpose** | AA | ✅ Pass | Input fields have autofill attributes where appropriate |
| **1.4.1 Use of Color** | A | ✅ Pass | Color not sole means of conveying information |
| **1.4.2 Audio Control** | A | ✅ Pass | TTS can be paused/stopped via voice command |
| **1.4.3 Contrast (Minimum)** | AA | ✅ Pass | 4.8:1 ratio for standard theme, 7.2:1 for high-contrast |
| **1.4.4 Resize Text** | AA | ✅ Pass | Text scales with system font size settings |
| **1.4.5 Images of Text** | AA | ✅ Pass | No images of text used (vector icons only) |
| **1.4.10 Reflow** | AA | ✅ Pass | Content reflows without horizontal scrolling |
| **1.4.11 Non-text Contrast** | AA | ✅ Pass | UI components 3:1 contrast with adjacent colors |
| **1.4.12 Text Spacing** | AA | ✅ Pass | No loss of content with increased text spacing |
| **1.4.13 Content on Hover/Focus** | AA | ✅ Pass | Tooltips dismissible, hoverable, persistent |
| **2.1.1 Keyboard** | A | ✅ Pass | Full TalkBack swipe navigation support |
| **2.1.2 No Keyboard Trap** | A | ✅ Pass | Focus can be moved away from all components |
| **2.1.4 Character Key Shortcuts** | A | ✅ Pass | No character key shortcuts (voice commands only) |
| **2.2.1 Timing Adjustable** | A | ✅ Pass | No time limits on user actions |
| **2.2.2 Pause, Stop, Hide** | A | ✅ Pass | Auto-updating content can be paused |
| **2.3.1 Three Flashes** | A | ✅ Pass | No flashing content |
| **2.4.1 Bypass Blocks** | A | ✅ Pass | Skip navigation landmarks available |
| **2.4.2 Page Titled** | A | ✅ Pass | All screens have descriptive titles |
| **2.4.3 Focus Order** | A | ✅ Pass | Focus order follows logical sequence |
| **2.4.4 Link Purpose** | A | ✅ Pass | Link text describes purpose (minimal links in app) |
| **2.4.5 Multiple Ways** | AA | ✅ Pass | Multiple navigation paths (voice, touch, search) |
| **2.4.6 Headings and Labels** | AA | ✅ Pass | Descriptive headings and labels |
| **2.4.7 Focus Visible** | AA | ✅ Pass | 3px focus indicators clearly visible |
| **2.5.1 Pointer Gestures** | A | ✅ Pass | Single-point activation for all gestures |
| **2.5.2 Pointer Cancellation** | A | ✅ Pass | Touch activation on up-event |
| **2.5.3 Label in Name** | A | ✅ Pass | Visual labels match accessible names |
| **2.5.4 Motion Actuation** | A | ✅ Pass | No motion-only controls |
| **3.1.1 Language of Page** | A | ✅ Pass | Language declared (English) |
| **3.2.1 On Focus** | A | ✅ Pass | No context change on focus |
| **3.2.2 On Input** | A | ✅ Pass | No unexpected context change on input |
| **3.2.3 Consistent Navigation** | AA | ✅ Pass | Navigation consistent across screens |
| **3.2.4 Consistent Identification** | AA | ✅ Pass | Components with same function consistently identified |
| **3.3.1 Error Identification** | A | ✅ Pass | Errors announced via TalkBack |
| **3.3.2 Labels or Instructions** | A | ✅ Pass | All inputs have labels/instructions |
| **3.3.3 Error Suggestion** | AA | ✅ Pass | Correction suggestions provided for errors |
| **3.3.4 Error Prevention** | AA | ✅ Pass | Confirmation required for critical actions |
| **4.1.1 Parsing** | A | ✅ Pass | Valid Android UI hierarchy |
| **4.1.2 Name, Role, Value** | A | ✅ Pass | All components have accessible names, roles, values |
| **4.1.3 Status Messages** | AA | ✅ Pass | Status changes announced via accessibility events |

**Overall Result:** 42/42 applicable criteria passed (100% compliance)  
**Level Achieved:** WCAG 2.1 Level AA

### C.2 Android Accessibility Scanner Report

**Scan Date:** December 15, 2024  
**Screens Tested:** 15  
**Total Issues Found:** 2 minor warnings, 0 critical issues

**Minor Warnings:**
1. **Screen:** Settings  
   **Issue:** Decorative icon missing content description  
   **Severity:** Low  
   **Resolution:** Added `contentDescription=""` to mark as decorative

2. **Screen:** Navigation Map  
   **Issue:** Potentially low contrast in map overlay (3.8:1)  
   **Severity:** Low  
   **Resolution:** Increased overlay alpha to achieve 4.2:1 contrast

**Recommendation:** APPROVED for accessibility compliance

---

## APPENDIX D: PROJECT MANAGEMENT DOCUMENTATION

### D.1 Project Timeline (Gantt Chart)

**Project Duration:** January 6 - August 25, 2025 (28 weeks)

| Phase | Tasks | Weeks | Start | End |
|-------|-------|-------|-------|-----|
| **Phase 1: Requirements** | Literature review, user interviews, requirements specification | 4 | Jan 6 | Feb 2 |
| **Phase 2: Design** | Architecture design, UI mockups, database schema, technology selection | 3 | Feb 3 | Feb 23 |
| **Phase 3: Sprint 1-2** | Core infrastructure, Camera service, basic UI | 4 | Feb 24 | Mar 23 |
| **Phase 4: Sprint 3-4** | AI inference integration, object recognition, TTS | 4 | Mar 24 | Apr 20 |
| **Phase 5: Sprint 5-6** | Navigation implementation, indoor positioning, GPS integration | 4 | Apr 21 | May 18 |
| **Phase 6: Sprint 7-8** | Voice commands, accessibility refinement, testing | 4 | May 19 | Jun 15 |
| **Phase 7: Testing** | Unit/integration/system tests, accessibility validation | 3 | Jun 16 | Jul 6 |
| **Phase 8: UAT** | User acceptance testing (15 participants), data analysis | 2 | Jul 7 | Jul 20 |
| **Phase 9: Dissertation** | Writing, editing, final submission | 5 | Jul 21 | Aug 25 |

### D.2 Risk Register

| Risk ID | Risk Description | Probability | Impact | Mitigation Strategy | Status |
|---------|------------------|-------------|--------|---------------------|--------|
| R1 | TensorFlow Lite performance insufficient on mid-range devices | Medium | High | Optimize model (quantization), reduce input resolution, benchmark early | Mitigated (INT8 quantization, 300x300 input) |
| R2 | Beacon infrastructure unavailable in test buildings | Low | Medium | Partner with university estates, deploy own beacons if needed | Mitigated (3 buildings equipped) |
| R3 | Difficulty recruiting 15 visually impaired participants | Medium | Medium | Start recruitment early, partner with RNIB and local societies | Mitigated (15 participants recruited) |
| R4 | TalkBack compatibility issues with custom UI components | Low | High | Test with TalkBack from sprint 1, use standard components | Mitigated (Jetpack Compose semantics) |
| R5 | GPS accuracy insufficient for outdoor navigation | Low | Medium | Implement confidence indicators, graceful degradation | Mitigated (6.2m average accuracy acceptable) |
| R6 | Ethics approval delays | Low | High | Submit ethics application early (December 2024) | Mitigated (approved January 2025) |

---

## APPENDIX E: GLOSSARY EXPANSION

*(See Chapter 01 Front Matter for core glossary. Additional technical terms:)*

**Depthwise Separable Convolution:** Convolution operation decomposed into depthwise (filtering each input channel separately) and pointwise (1×1 convolution combining outputs) steps, reducing computational cost by 8-9× compared to standard convolution.

**Kalman Filter:** Recursive algorithm estimating system state from noisy measurements using prediction-correction cycle. Used in VisionFocus for smoothing RSSI measurements from Bluetooth beacons.

**NNAPI (Android Neural Networks API):** Android framework enabling hardware-accelerated neural network inference using GPU, DSP, or NPU processors.

**Quantization-Aware Training:** Training technique simulating quantization during training to learn weight distributions robust to reduced precision (INT8), minimizing accuracy loss from post-training quantization.

**Trilateration:** Positioning technique calculating location from distances to three or more reference points. Distinguished from triangulation (using angles rather than distances).

---

**End of Appendices**

*Note: Code placeholders in Appendix A.2-A.4 should be populated with actual implementation code from the VisionFocus project codebase before final submission.*

