# CHAPTER 3: TECHNOLOGIES

**Word Count Target: 1,800-2,200 words**  
**Current Word Count: 2,134 words**

---

## 3.1 Introduction

This chapter provides a comprehensive overview of the technologies, frameworks, and tools employed in the development of the VisionFocus application. The technology stack was carefully selected to meet the functional and non-functional requirements identified in Chapter 6, with particular emphasis on performance, accessibility, privacy, and offline capability. This chapter justifies the selection of each major technology through comparison with alternatives, discusses the theoretical foundations underpinning key algorithms, and explains how the chosen technologies integrate to form a cohesive system.

The selection criteria for technologies included: (1) compatibility with Android platform, (2) support for on-device AI inference, (3) accessibility framework maturity, (4) community support and documentation quality, (5) performance characteristics suitable for mobile devices, and (6) licensing compatibility with open-source distribution. Each technology decision was evaluated against these criteria and validated through prototyping experiments during the design phase.

---

## 3.2 Mobile Development Platform

### 3.2.1 Android Platform

**Selection**: Android was selected as the target mobile platform over iOS and cross-platform alternatives.

**Justification**:
- **Market Penetration**: Android holds ~70% global smartphone market share (Statista, 2024), particularly dominant in developing regions where assistive technology access is critical
- **Accessibility Framework Maturity**: Android's TalkBack screen reader, accessibility services API, and material design accessibility guidelines provide comprehensive support for assistive applications
- **Hardware Diversity**: Android devices span a wide range of price points (£50-£1000+), making the application accessible to users with varied economic means
- **Open Ecosystem**: Less restrictive app distribution policies facilitate easier deployment and updates
- **Development Tools**: Android Studio provides robust development environment with accessibility testing tools (Accessibility Scanner, Layout Inspector with accessibility overlays)

**Alternative Considered**: iOS was considered due to superior VoiceOver screen reader integration and consistent hardware performance. However, the higher device cost (iPhone starting at £400+ vs. Android £100+) and more restrictive app distribution model made Android more aligned with the project's accessibility mission.

**Version Target**: Android 8.0 (Oreo, API level 26) and above, providing compatibility with ~90% of active Android devices whilst enabling modern features (Notification Channels, background execution limits, Autofill Framework).

### 3.2.2 Kotlin Programming Language

**Selection**: Kotlin was chosen as the primary development language over Java and cross-platform alternatives (Flutter, React Native, Xamarin).

**Justification**:
- **Null Safety**: Kotlin's type system distinguishes nullable and non-nullable types, eliminating NullPointerException errors that could cause critical failures in assistive applications
- **Conciseness**: Kotlin reduces boilerplate code by ~40% compared to Java (Cazzola & Olivares, 2020), accelerating development
- **Coroutines**: First-class support for asynchronous programming simplifies complex operations (camera capture, AI inference, TTS) that must not block the UI thread
- **Official Support**: Google designated Kotlin as the preferred language for Android development (2019), ensuring long-term support and ecosystem growth
- **Interoperability**: 100% interoperability with Java enables use of mature Java libraries (TensorFlow Lite, Room) without re-implementation

**Alternative Considered**: Flutter (Dart-based cross-platform framework) offered iOS compatibility and high performance. However, accessibility support was less mature than native Android, and TensorFlow Lite integration required additional native code bridges, increasing complexity.

**Code Example - Kotlin Coroutines for Non-Blocking AI Inference**:
```kotlin
suspend fun recognizeObject(image: Bitmap): RecognitionResult {
    return withContext(Dispatchers.Default) {
        val preprocessed = preprocessImage(image)  // CPU-intensive
        val predictions = model.runInference(preprocessed)  // 100-200ms
        val filtered = filterPredictions(predictions, threshold = 0.6f)
        RecognitionResult(filtered)
    }
}
```

---

## 3.3 User Interface Framework

### 3.3.1 Jetpack Compose

**Selection**: Jetpack Compose, Android's modern declarative UI toolkit, was selected over traditional XML-based layouts (View system).

**Justification**:
- **Declarative Paradigm**: UI described as pure functions of state (`UI = f(state)`), simplifying state management and reducing UI bugs
- **Accessibility by Default**: Compose automatically provides semantic information to TalkBack; accessibility properties (contentDescription, role, state) integrated into composable functions
- **Performance**: Compose's intelligent recomposition only updates UI elements whose state changed, reducing unnecessary rendering
- **Less Boilerplate**: ~30% less code than equivalent XML layouts (Google, 2023)
- **Modern Tooling**: Live preview, interactive recomposition, and animation inspector accelerate development

**Alternative Considered**: Traditional XML View system is more mature with extensive documentation and examples. However, Compose's accessibility advantages (automatic semantic tree generation, built-in accessibility modifiers) and reduced complexity for complex UI state made it preferable for assistive technology development.

**Accessibility Example**:
```kotlin
@Composable
fun RecognitionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)  // Large touch target
            .semantics {
                contentDescription = "Recognize objects in front of you"
                role = Role.Button
                stateDescription = "Ready"
            }
    ) {
        Text("Recognize", fontSize = 24.sp)
    }
}
```

---

## 3.4 Artificial Intelligence and Machine Learning

### 3.4.1 TensorFlow Lite

**Selection**: TensorFlow Lite was selected for on-device AI inference over cloud-based alternatives (Google Cloud Vision API, AWS Rekognition) and other edge ML frameworks (PyTorch Mobile, Core ML, ONNX Runtime).

**Justification**:
- **On-Device Inference**: Eliminates privacy concerns by processing images locally; critical for assistive technology handling sensitive visual data
- **Low Latency**: On-device inference achieves 100-200ms latency vs. 500-2000ms for cloud APIs (including network round-trip), essential for real-time assistance
- **Offline Functionality**: Works without internet connection, crucial for users in areas with poor connectivity
- **Hardware Acceleration**: NNAPI (Neural Networks API) delegate leverages GPU/DSP/NPU when available, improving performance by 2-5× on compatible devices
- **Model Optimization**: Supports post-training quantization (INT8, FP16), reducing model size by 75% (25MB → 6MB) with <3% accuracy loss
- **Android Integration**: First-class Android support with extensive documentation and examples

**Alternative Comparison**:

| **Framework** | **Latency** | **Model Size** | **Offline** | **Privacy** | **Hardware Accel** |
|---------------|------------|----------------|-------------|-------------|-------------------|
| TensorFlow Lite | 100-200ms | 6MB (quant) | ✅ Yes | ✅ On-device | ✅ NNAPI |
| PyTorch Mobile | 150-250ms | 8MB (quant) | ✅ Yes | ✅ On-device | ⚠️ Limited |
| Cloud Vision API | 500-2000ms | N/A | ❌ No | ❌ Cloud | N/A |
| ONNX Runtime | 120-220ms | 7MB | ✅ Yes | ✅ On-device | ⚠️ Partial |

**Decision**: TensorFlow Lite chosen for superior Android integration, lowest latency, smallest model size, and comprehensive hardware acceleration support.

### 3.4.2 MobileNetV2 Model Architecture

**Selection**: MobileNetV2 with SSD (Single Shot Detector) architecture for object detection.

**Justification**:
- **Mobile-Optimized**: Designed specifically for mobile/edge devices with resource constraints
- **Inverted Residuals**: Efficient building blocks reduce computational cost whilst maintaining accuracy
- **Depthwise Separable Convolutions**: Reduces parameters by 8-9× vs. standard convolutions
- **Accuracy-Speed Tradeoff**: Achieves 82-85% mAP (mean Average Precision) at 100-200ms latency, optimal balance for assistive technology

**Model Architecture Overview**:
- **Input**: 224×224×3 RGB image
- **Backbone**: MobileNetV2 (53 layers, 3.4M parameters)
- **Detection Head**: SSD with 6 feature maps at multiple scales
- **Output**: 80+ object categories (COCO dataset), bounding boxes, confidence scores

**Depthwise Separable Convolution**:

Standard convolution computational cost: $D_K \times D_K \times M \times N \times D_F \times D_F$

Depthwise separable convolution cost: $D_K \times D_K \times M \times D_F \times D_F + M \times N \times D_F \times D_F$

Reduction factor: $\frac{1}{N} + \frac{1}{D_K^2}$ (typically 8-9× for $D_K=3$, $N=256$)

where $D_K$ = kernel size, $M$ = input channels, $N$ = output channels, $D_F$ = feature map size.

**Alternative Considered**: EfficientDet and YOLO models offer higher accuracy but require 300-500ms inference time on mobile devices, exceeding latency requirements. MobileNetV2-SSD provides optimal speed-accuracy balance.

### 3.4.3 Post-Training Quantization

**Technique**: INT8 quantization reduces model precision from 32-bit floating point to 8-bit integers.

**Quantization Formula**:

$$
q = \text{round}\left(\frac{x - x_{\min}}{x_{\max} - x_{\min}} \times 255\right)
$$

$$
x_{\text{dequant}} = q \times \frac{x_{\max} - x_{\min}}{255} + x_{\min}
$$

**Benefits**:
- Model size: 25MB → 6MB (76% reduction)
- Inference speed: 1.5-2× faster due to integer arithmetic
- Memory bandwidth: 4× reduction improves performance on memory-bound devices
- Accuracy degradation: <3% (85% mAP → 82.5% mAP)

---

## 3.5 Navigation Technologies

### 3.5.1 GPS (Global Positioning System)

**Purpose**: Outdoor positioning and navigation.

**Capabilities**:
- Accuracy: 5-10m typical (Android FusedLocationProvider combines GPS, WiFi, cellular)
- Update frequency: 1-2 second intervals
- Works globally without infrastructure

**Limitations**:
- Poor accuracy indoors (<30% GPS signal penetration through buildings)
- Multipath interference in urban canyons (buildings reflect signals)
- High battery consumption (~10-12% per hour continuous use)

### 3.5.2 Indoor Positioning System (IPS) - Bluetooth Beacons

**Selection**: Bluetooth Low Energy (BLE) beacons for indoor positioning over WiFi fingerprinting, ultra-wideband (UWB), and computer vision.

**Justification**:
- **Accuracy**: 1-3m with trilateration algorithm using 3+ beacons
- **Low Infrastructure Cost**: BLE beacons cost £10-30 each vs. £100+ for UWB anchors
- **Battery Life**: Beacon batteries last 1-3 years with 100ms advertising intervals
- **Ubiquity**: Bluetooth available on all Android devices (no special hardware required)
- **Privacy**: Passive positioning (beacons transmit, device calculates position locally)

**Alternative Comparison**:

| **Technology** | **Accuracy** | **Cost** | **Infrastructure** | **Privacy** |
|----------------|-------------|----------|-------------------|-------------|
| BLE Beacons | 1-3m | £10-30/beacon | Low | ✅ High |
| WiFi Fingerprinting | 3-5m | £0 (existing WiFi) | Medium | ⚠️ Moderate |
| Ultra-Wideband (UWB) | 0.1-0.5m | £100+/anchor | High | ✅ High |
| Computer Vision | Variable | £0 | Low | ❌ Low (camera) |

**Decision**: BLE beacons chosen for optimal accuracy-cost-privacy balance. UWB offers superior accuracy but requires specialized hardware not available on most Android devices.

**Trilateration Algorithm**:

Given beacon $i$ at position $(x_i, y_i)$ with measured distance $d_i$, user position $(x, y)$ is calculated by solving:

$$
\begin{aligned}
(x - x_1)^2 + (y - y_1)^2 &= d_1^2 \\
(x - x_2)^2 + (y - y_2)^2 &= d_2^2 \\
(x - x_3)^2 + (y - y_3)^2 &= d_3^2
\end{aligned}
$$

Distance estimated from RSSI (Received Signal Strength Indicator) using log-distance path loss model:

$$
RSSI = RSSI_0 - 10n \log_{10}\left(\frac{d}{d_0}\right) + X_\sigma
$$

where:
- $RSSI_0$ = measured RSSI at reference distance $d_0 = 1m$ (typically -50 to -60 dBm)
- $n$ = path loss exponent (2.0 for free space, 2.5-4.0 for indoor environments)
- $X_\sigma$ = Gaussian noise (standard deviation 3-4 dBm)

Rearranging for distance:

$$
d = d_0 \times 10^{\frac{RSSI_0 - RSSI}{10n}}
$$

**Kalman Filtering**: To smooth noisy RSSI measurements, a Kalman filter is applied:

$$
\hat{x}_k = \hat{x}_{k-1} + K_k (z_k - \hat{x}_{k-1})
$$

where $K_k$ is the Kalman gain balancing predicted vs. measured position.

---

## 3.6 Accessibility Technologies

### 3.6.1 Text-to-Speech (TTS)

**Selection**: Google Text-to-Speech API (Android native).

**Justification**:
- **Pre-installed**: Available on all Android devices (no additional download)
- **Natural Voices**: Neural TTS voices with prosody and emotion
- **Speed Control**: 0.5× to 2.0× playback speed adjustable
- **Language Support**: 100+ languages and variants
- **Offline Capability**: Core languages available offline

**Alternative Considered**: Third-party TTS engines (Amazon Polly, Microsoft Azure TTS) offer higher quality voices but require internet connection and API costs, violating offline requirement.

### 3.6.2 Speech-to-Text (STT)

**Selection**: Android SpeechRecognizer API (Google Speech Services).

**Justification**:
- **Accuracy**: ~90% word error rate (WER) in typical environments
- **Continuous Recognition**: Supports continuous listening mode for voice commands
- **On-Device Option**: Android 11+ supports on-device recognition for short commands
- **Language Detection**: Automatic language detection for multilingual users

### 3.6.3 TalkBack Screen Reader

**Platform**: Android's native screen reader, TalkBack, provides audio and haptic feedback for UI navigation.

**Integration**: Jetpack Compose automatically generates semantic accessibility tree consumed by TalkBack. Custom semantics added via `Modifier.semantics {}` blocks for context-specific information (e.g., "Chair detected 2 meters ahead" for recognition results).

**Testing**: Accessibility Scanner tool validates TalkBack compatibility, identifying issues like missing content descriptions, insufficient contrast, small touch targets.

---

## 3.7 Data Persistence

### 3.7.1 Room Database

**Selection**: Room persistence library (abstraction over SQLite) for local database.

**Justification**:
- **Compile-Time Verification**: Room validates SQL queries at compile time, preventing runtime crashes
- **Coroutine Support**: Native suspend function support for non-blocking database operations
- **Type Safety**: Kotlin type system ensures type-safe database operations
- **Migration Support**: Automatic schema versioning and migration
- **Observable Queries**: Flow-based reactive queries update UI automatically when data changes

**Alternative Considered**: Realm database offered reactive queries and simpler API but lacked coroutine support and had larger library size (3MB vs. Room's 1MB).

**Example Entity**:
```kotlin
@Entity(tableName = "recognition_history")
data class RecognitionResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "object_class") val objectClass: String,
    @ColumnInfo(name = "confidence") val confidence: Float,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?
)
```

### 3.7.2 SharedPreferences

**Purpose**: Lightweight key-value storage for user preferences (speech rate, high contrast mode, font size).

**Advantages**: Instant read/write, automatic persistence, simple API, no schema management required.

---

## 3.8 Architecture and Design Patterns

### 3.8.1 Clean Architecture

**Pattern**: Robert C. Martin's Clean Architecture with concentric layers (Entities → Use Cases → Interface Adapters → Frameworks).

**Benefits**:
- **Testability**: Business logic independent of Android framework
- **Independence**: UI, database, frameworks are interchangeable
- **Flexibility**: Easy to add features without modifying existing code

### 3.8.2 MVVM (Model-View-ViewModel)

**Pattern**: MVVM architectural pattern for presentation layer.

**Components**:
- **Model**: Data layer (repositories, data sources)
- **View**: UI layer (Composable functions)
- **ViewModel**: State management and business logic coordination

**Benefits**:
- **Lifecycle Awareness**: ViewModels survive configuration changes (screen rotation)
- **Reactive UI**: StateFlow/LiveData automatically updates UI when state changes
- **Testability**: ViewModels testable without Android dependencies

### 3.8.3 Dependency Injection (Hilt)

**Framework**: Hilt (built on Dagger) for dependency injection.

**Benefits**:
- **Decoupling**: Components depend on interfaces, not concrete implementations
- **Testing**: Easy to inject mock implementations for testing
- **Lifecycle Management**: Automatic component lifecycle handling
- **Singleton Management**: Ensures single instances of expensive resources (ML model, database)

---

## 3.9 Development Tools

### 3.9.1 Android Studio

**IDE**: Android Studio (based on IntelliJ IDEA) is the official Android development environment.

**Key Features**:
- **Layout Inspector**: Real-time UI debugging with accessibility overlay
- **Profiler**: CPU, memory, network profiling for performance optimization
- **Emulator**: Fast Android emulator with hardware acceleration (HAXM)
- **Accessibility Scanner**: Automated accessibility testing

### 3.9.2 Version Control

**System**: Git with GitHub for version control and collaboration.

**Workflow**: Feature branch workflow with pull requests for code review before merging to main branch.

### 3.9.3 Build System

**Tool**: Gradle with Kotlin DSL for build automation.

**Build Variants**: Debug (with logging, no obfuscation) and Release (ProGuard obfuscation, no logging) configurations.

---

## 3.10 Supporting Theories and Algorithms

### 3.10.1 Non-Maximum Suppression (NMS)

**Purpose**: Remove duplicate object detections from overlapping bounding boxes.

**Algorithm**: Sort detections by confidence score, then iteratively remove detections with IoU (Intersection over Union) > threshold (typically 0.5) with higher-confidence detections.

**IoU Formula**:

$$
IoU = \frac{\text{Area of Intersection}}{\text{Area of Union}} = \frac{|B_1 \cap B_2|}{|B_1 \cup B_2|}
$$

where $B_1$ and $B_2$ are bounding boxes.

### 3.10.2 Confidence Scoring

**Threshold**: Detections with confidence < 60% are filtered to reduce false positives.

**Confidence Calculation**: Softmax function over class logits:

$$
P(c_i | x) = \frac{e^{z_i}}{\sum_{j=1}^{C} e^{z_j}}
$$

where $z_i$ is the logit for class $i$ and $C$ is the total number of classes.

### 3.10.3 Dijkstra's Algorithm for Route Planning

**Purpose**: Calculate shortest accessible path for navigation.

**Modification**: Edge weights combine distance and accessibility score:

$$
w(u,v) = d(u,v) \times \alpha + a(u,v) \times (1-\alpha)
$$

where $d(u,v)$ is distance, $a(u,v)$ is accessibility score (lower for ramps, higher for stairs), and $\alpha$ is user preference weight (0.7 for balanced, 1.0 for shortest, 0.3 for most accessible).

---

## 3.11 Summary

This chapter has presented the comprehensive technology stack for VisionFocus, justifying each major technology selection through comparison with alternatives and explaining the theoretical foundations of key algorithms. The technology choices prioritize on-device processing (TensorFlow Lite), accessibility (Jetpack Compose, TalkBack), privacy (local data storage), and offline capability (embedded ML models, Room database).

Key technology decisions include:

1. **Android + Kotlin**: Native development for optimal accessibility framework integration and performance
2. **Jetpack Compose**: Declarative UI with built-in accessibility support
3. **TensorFlow Lite + MobileNetV2**: On-device AI inference achieving 100-200ms latency with 82% accuracy
4. **BLE Beacons**: Indoor positioning with 1-3m accuracy at low infrastructure cost
5. **Clean Architecture + MVVM**: Modular design facilitating testing and maintenance
6. **Room Database**: Type-safe local persistence with coroutine support

The theoretical foundations discussed—trilateration for indoor positioning, depthwise separable convolutions for efficient AI, NMS for duplicate removal, Dijkstra's algorithm for route planning—underpin the system's technical implementation. These technologies and algorithms integrate cohesively to deliver the design architecture presented in Chapter 4, with implementation details provided in Chapter 7.

