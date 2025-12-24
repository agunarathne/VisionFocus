# LIST OF FIGURES

All figures referenced throughout the dissertation are listed below with chapter and page references.

---

## Chapter 2: Literature Review

**Figure 2.1:** Global Distribution of Visual Impairment (Source: WHO, 2020)  
*Pie chart showing 39 million blind, 246 million low vision, from 285 million total visually impaired globally*  
Page: ~18

**Figure 2.2:** Evolution of Assistive Technology Timeline (1920-2025)  
*Timeline diagram showing progression from white canes (1920s) through electronic travel aids (1970s) to AI-powered applications (2015-present)*  
Page: ~20

---

## Chapter 4: Design and Architecture

**Figure 4.1:** High-Level System Architecture  
*Mermaid diagram showing 5-layer architecture: Presentation Layer → Application Layer → Domain Layer → Data Layer → External Services Layer*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-41-high-level-system-architecture)  
Page: ~48

**Figure 4.2:** Component Architecture (Clean Architecture Pattern)  
*Mermaid diagram illustrating Clean Architecture with 6 concentric layers: Entities (core) → Use Cases → Interface Adapters → Frameworks & Drivers, showing dependency inversion*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-42-component-architecture)  
Page: ~50

**Figure 4.3:** Application Layer Modules  
*Mermaid diagram detailing 8 application modules: UI Module, Navigation Module, Recognition Module, Location Module, Camera Module, Database Module, Network Module, Analytics Module*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-43-application-layer-modules)  
Page: ~52

**Figure 4.4:** Data Flow Pipeline - Object Recognition  
*Mermaid flowchart showing Camera Input → Image Preprocessing → AI Inference → Postprocessing → TTS Output with timing metrics at each stage*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-44-data-flow-pipeline)  
Page: ~54

**Figure 4.5:** AI/ML Pipeline Architecture  
*Mermaid diagram showing TensorFlow Lite model pipeline: Input Tensor (300×300×3) → MobileNetV2 Backbone → SSD Detection Head → NMS → Output Detections*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-45-aiml-pipeline-architecture)  
Page: ~56

**Figure 4.6:** Navigation System Architecture  
*Mermaid diagram illustrating dual-mode navigation: GPS Module (outdoor) + Beacon Module (indoor) → Position Fusion → Route Calculation → Voice Guidance*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-46-navigation-system-architecture)  
Page: ~58

**Figure 4.7:** Deployment Architecture  
*Mermaid diagram showing Android Device (app + local ML models) ↔ Optional Cloud Services (maps, updates) + Bluetooth Beacons (indoor positioning)*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-47-deployment-architecture)  
Page: ~60

**Figure 4.8:** Object Recognition Sequence Diagram  
*Mermaid sequence diagram: User → UI → CameraService → AIInferenceService → TTSService showing complete recognition workflow with timing*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-48-object-recognition-sequence-diagram)  
Page: ~62

**Figure 4.9:** Indoor Navigation Sequence Diagram  
*Mermaid sequence diagram: User → NavigationService → BeaconScanner → PositionCalculator → RouteService → VoiceGuidance showing indoor navigation workflow*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-49-indoor-navigation-sequence-diagram)  
Page: ~64

**Figure 4.10:** Voice Command Processing Sequence Diagram  
*Mermaid sequence diagram: User Voice Input → SpeechRecognizer → CommandParser → ActionHandler → Feedback showing voice interaction flow*  
Source: [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md#figure-410-voice-command-sequence-diagram)  
Page: ~66

**Figure 4.11:** Database Entity-Relationship Diagram  
*Mermaid ER diagram showing 4 entities: User, SavedLocation, NavigationHistory, BeaconConfig with relationships and attributes*  
Page: ~68

**Figure 4.12:** User Interface Wireframes  
*Composite figure showing 5 screen layouts: Home Screen, Camera Screen, Object Recognition Results, Navigation Screen, Settings Screen with accessibility annotations*  
Page: ~70

---

## Chapter 5: Methodology

**Figure 5.1:** Design Science Research Framework  
*Diagram showing DSR cycle: Problem Identification → Objectives → Design & Development → Demonstration → Evaluation → Communication*  
Page: ~76

**Figure 5.2:** Agile Sprint Structure  
*Diagram illustrating 2-week sprint cycle: Sprint Planning → Daily Standups → Development → Sprint Review → Sprint Retrospective*  
Page: ~78

---

## Chapter 6: Requirements Analysis

**Figure 6.1:** Use Case Diagram  
*Mermaid use case diagram showing Visually Impaired User interacting with 10 use cases: Recognize Objects, Navigate Indoors, Navigate Outdoors, Voice Commands, Adjust Settings, Save Locations, View History, Text Reading, Color Detection, Distance Estimation*  
Page: ~84

**Figure 6.2:** User Persona - Sarah (Totally Blind)  
*Persona card with photo placeholder, demographics (45, admin assistant), goals, frustrations, tech proficiency, and user scenario*  
Page: ~86

**Figure 6.3:** User Persona - Michael (Low Vision)  
*Persona card with photo placeholder, demographics (62, retired teacher), goals, frustrations, tech proficiency, and user scenario*  
Page: ~87

**Figure 6.4:** User Persona - Aisha (Recently Blind)  
*Persona card with photo placeholder, demographics (28, software developer), goals, frustrations, tech proficiency, and user scenario*  
Page: ~88

---

## Chapter 7: Implementation

**Figure 7.1:** Project Structure Diagram  
*Directory tree showing VisionFocus project structure organized by Clean Architecture layers*  
Page: ~96

**Figure 7.2:** AI Inference Pipeline Performance Breakdown  
*Bar chart showing inference stage durations: Preprocessing (28ms), Inference (183ms), Postprocessing (109ms), Total (320ms)*  
Page: ~100

---

## Chapter 8: Testing and Evaluation

**Figure 8.1:** Testing Pyramid  
*Pyramid diagram showing test distribution: 118 Unit Tests (base) → 54 Integration Tests (middle) → 32 System Tests (top)*  
Page: ~108

**Figure 8.2:** Object Recognition Accuracy by Category  
*Horizontal bar chart showing accuracy percentages for top 15 object categories: Person (100%), Chair (93.3%), Car (91.7%), etc.*  
Page: ~112

**Figure 8.3:** Navigation Task Success Rate by Participant Group  
*Grouped bar chart comparing totally blind (88%), low vision (90%), recently blind (83%) success rates*  
Page: ~116

**Figure 8.4:** System Usability Scale Score Distribution  
*Histogram showing SUS score distribution: Range 62-91, Mean 78.5, SD 8.3, with grade thresholds marked*  
Page: ~120

---

## Chapter 10: Results and Analysis

**Figure 10.1:** Performance Latency Across Device Tiers  
*Line graph showing preprocessing, inference, and postprocessing latency for 4 devices: Nokia 5.3 (427ms total), Samsung A52 (280ms), Pixel 6 (243ms), Galaxy S21 (183ms)*  
Page: ~130

**Figure 10.2:** Indoor Positioning Accuracy vs. Beacon Count  
*Scatter plot with trendline showing positioning error decreasing as beacon count increases: 1 beacon (8.3m), 2 beacons (4.7m), 3 beacons (3.1m), 5+ beacons (2.3m)*  
Page: ~134

**Figure 10.3:** Object Recognition Accuracy by Environmental Condition  
*Radar chart showing accuracy across 6 conditions: Optimal Indoor (89.3%), Low Light (64.2%), Bright Sunlight (78.5%), Cluttered (72.1%), Occluded (51.8%), Extreme Angles (64.8%)*  
Page: ~136

**Figure 10.4:** User Satisfaction by Demographic Segment  
*Grouped bar chart showing satisfaction scores (1-5) across age groups (18-35, 36-50, 51-65, 66+), vision status (totally blind, low vision, recently blind), tech proficiency (novice, intermediate, expert)*  
Page: ~140

---

## Chapter 11: Discussion

**Figure 11.1:** Competitive Performance Comparison  
*Radar chart comparing VisionFocus vs. Seeing AI vs. Lookout vs. Be My Eyes across 8 dimensions: Accuracy, Latency, Privacy, Offline, Navigation, Integration, Cost, Accessibility*  
Page: ~150

---

## Chapter 12: Conclusion

**Figure 12.1:** VisionFocus Impact Summary  
*Infographic summarizing key achievements: 83.2% accuracy, 320ms latency, 91.3% task success, 78.5 SUS score, 0 privacy violations, 100% WCAG compliance*  
Page: ~158

---

**Total Figures: 30**

*Note: Page numbers are approximate and will be finalized during document compilation. All Mermaid diagrams from Chapter 4 are available in full detail in [Dissertation_Architecture_Diagrams.md](Dissertation_Architecture_Diagrams.md).*

