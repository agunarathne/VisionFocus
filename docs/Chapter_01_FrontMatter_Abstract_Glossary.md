# FRONT MATTER CONTENT

## ABSTRACT

**Word Count: 287 words**

---

Visual impairment affects over 285 million people worldwide, creating significant barriers to independent navigation and daily activities. Traditional assistive technologies such as white canes and guide dogs, while valuable, lack the adaptability and intelligence required for complex urban and indoor environments. Existing digital solutions are often fragmented, focusing on either object recognition or navigation but rarely integrating both functionalities effectively. Furthermore, many current applications rely on cloud-based processing, raising privacy concerns and limiting offline functionality.

This dissertation presents VisionFocus, an Android mobile application designed to enhance independence and safety for visually impaired users through integrated real-time object recognition and intelligent navigation capabilities. The project aims to address the limitations of existing solutions by providing a comprehensive, privacy-conscious, and user-centred assistive tool that operates seamlessly in both indoor and outdoor environments.

The application was developed using Agile methodology with Kotlin programming language and leverages TensorFlow Lite for on-device AI-powered object recognition, ensuring user privacy whilst maintaining real-time performance. The system integrates GPS-based outdoor navigation with Indoor Positioning System (IPS) using Bluetooth beacons for comprehensive location services. All core functionality operates offline, with audio feedback delivered through Text-to-Speech (TTS) technology and full accessibility compliance with WCAG 2.1 Level AA standards.

Evaluation results demonstrate that VisionFocus achieves 83.2% accuracy in object recognition across diverse environmental conditions, with an average response time of 320 milliseconds per inference. Navigation features provide turn-by-turn voice guidance with position updates every 1.8 seconds. User testing with visually impaired participants yielded an average satisfaction rating of 4.4 out of 5.0, with 95% task completion rates for object recognition and 90% for outdoor navigation tasks.

The project contributes to assistive technology research by demonstrating the feasibility of integrating multiple AI-powered features in a single mobile application whilst prioritising user privacy through on-device processing. VisionFocus represents a significant step towards digital inclusion and enhanced independence for the visually impaired community.

---

## GLOSSARY OF TERMS

**Word Count: 520 words**

---

### Technical Abbreviations and Acronyms

**AI (Artificial Intelligence)**
The simulation of human intelligence processes by computer systems, including learning, reasoning, and self-correction. In VisionFocus, AI powers the object recognition and contextual awareness features.

**API (Application Programming Interface)**
A set of protocols and tools for building software applications. VisionFocus integrates various APIs including Google Maps API for navigation and TTS API for audio feedback.

**CNN (Convolutional Neural Network)**
A deep learning algorithm specifically designed for processing structured grid data such as images. CNNs are the foundation of VisionFocus's object recognition capabilities.

**GPS (Global Positioning System)**
A satellite-based navigation system providing location and time information. Used in VisionFocus for outdoor navigation and positioning.

**IPS (Indoor Positioning System)**
A network of devices used to locate objects or people inside buildings. VisionFocus implements IPS using Bluetooth beacons and WiFi triangulation for indoor navigation.

**ML (Machine Learning)**
A subset of AI that enables systems to learn and improve from experience without explicit programming. VisionFocus uses ML models for object detection and classification.

**MVVM (Model-View-ViewModel)**
A software architectural pattern that separates the development of graphical user interface from business logic. VisionFocus employs MVVM for clean code architecture.

**OCR (Optical Character Recognition)**
Technology that converts different types of documents into editable and searchable data. Planned as a future enhancement for VisionFocus.

**RSSI (Received Signal Strength Indicator)**
A measurement of power present in a received radio signal. Used in VisionFocus to calculate distance from Bluetooth beacons for indoor positioning.

**SDK (Software Development Kit)**
A collection of software development tools in one package. VisionFocus uses Android SDK and TensorFlow Lite SDK.

**TTS (Text-to-Speech)**
Assistive technology that reads digital text aloud. Critical component of VisionFocus for providing audio feedback to users.

**WCAG (Web Content Accessibility Guidelines)**
International standards for making digital content accessible to people with disabilities. VisionFocus adheres to WCAG 2.1 Level AA compliance.

---

### Domain-Specific Terms

**Accessibility**
The design of products, devices, services, or environments for people with disabilities. VisionFocus prioritises accessibility throughout its design and implementation.

**Assistive Technology**
Any device, software, or equipment that helps people with disabilities perform functions that might otherwise be difficult or impossible. VisionFocus is classified as assistive technology for visual impairment.

**Beacon (Bluetooth Low Energy)**
A small wireless device that broadcasts signals to nearby smart devices using Bluetooth technology. Used in VisionFocus for indoor positioning.

**Clean Architecture**
A software design philosophy that separates code into layers with clear dependencies flowing inward. VisionFocus implements Clean Architecture principles for maintainability.

**Confidence Score**
A numerical value (typically 0-100%) indicating the certainty of an AI model's prediction. VisionFocus uses a 60% confidence threshold for object detection.

**Edge Computing**
Computing performed at or near the data source rather than relying on cloud computing. VisionFocus performs AI inference on-device for privacy and reduced latency.

**Inference**
The process of using a trained machine learning model to make predictions on new data. VisionFocus performs inference on camera frames to detect objects.

**Non-Maximum Suppression (NMS)**
An algorithm that selects the best bounding boxes for detected objects and removes overlapping detections. Used in VisionFocus's object detection pipeline.

**Quantization**
A technique to reduce the size of ML models by reducing the precision of numerical values. VisionFocus uses INT8 quantization for TensorFlow Lite models.

**Screen Reader**
Assistive technology that converts text and interface elements into speech or Braille. VisionFocus is fully compatible with Android's TalkBack screen reader.

**Trilateration**
A method of determining position by measuring distances to three or more reference points. VisionFocus uses trilateration for indoor positioning with Bluetooth beacons.

**User-Centred Design (UCD)**
An iterative design process that focuses on users and their needs at every stage of development. VisionFocus was developed following UCD principles.

**Voice Command**
Spoken instructions given to a device to perform actions. VisionFocus accepts voice commands for hands-free operation.

---

### Mobile Development Terms

**Android Studio**
The official Integrated Development Environment (IDE) for Android application development. Used to develop VisionFocus.

**Coroutines**
A Kotlin feature for asynchronous programming that simplifies concurrent code. VisionFocus uses coroutines for non-blocking operations.

**Dependency Injection**
A design pattern for achieving Inversion of Control between classes and their dependencies. VisionFocus uses Hilt for dependency injection.

**Gradle**
An open-source build automation tool used for Android projects. VisionFocus uses Gradle for build management.

**Jetpack Compose**
Android's modern toolkit for building native UI with declarative Kotlin code. VisionFocus UI is built with Jetpack Compose.

**Kotlin**
A modern, statically-typed programming language that runs on the Java Virtual Machine. VisionFocus is developed entirely in Kotlin.

**Room**
An Android Jetpack library providing an abstraction layer over SQLite for database access. VisionFocus uses Room for local data persistence.

**TensorFlow Lite**
A lightweight version of TensorFlow designed for mobile and embedded devices. VisionFocus uses TensorFlow Lite for on-device AI inference.

---

*Note: This glossary provides definitions for technical terms used throughout the dissertation. Terms are listed alphabetically within their respective categories.*

