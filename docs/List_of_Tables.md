# LIST OF TABLES

All tables referenced throughout the dissertation are listed below with chapter and page references.

---

## Chapter 3: Technology Stack

**Table 3.1:** Technology Stack Comparison - Cloud vs. On-Device AI  
*Comparison matrix showing latency, privacy, cost, offline capability, and scalability for cloud-based vs. on-device inference*  
Page: ~38

**Table 3.2:** Indoor Positioning Technologies Comparison  
*Comparison of Bluetooth beacons vs. WiFi fingerprinting vs. UWB vs. LiDAR across accuracy, cost, infrastructure, power consumption, and compatibility metrics*  
Page: ~42

---

## Chapter 5: Methodology

**Table 5.1:** Project Timeline and Phases  
*6-phase Gantt-style table showing Phase 1-Requirements (Jan 6-Feb 2), Phase 2-Design (Feb 3-23), Phase 3-4 Sprints (Feb 24-Apr 20), Phase 5-6 Sprints (Apr 21-Jun 15), Phase 7-Testing (Jun 16-Jul 6), Phase 8-UAT (Jul 7-20), Phase 9-Dissertation (Jul 21-Aug 25)*  
Source: [Chapter_05_Methodology.md](Chapter_05_Methodology.md#table-51-project-timeline-and-phases)  
Page: ~78

**Table 5.2:** Risk Management Register  
*6 risks with ID, description, probability, impact, mitigation strategy: R1-Performance, R2-Beacons, R3-Recruitment, R4-TalkBack, R5-GPS, R6-Ethics*  
Source: [Chapter_05_Methodology.md](Chapter_05_Methodology.md#table-52-risk-management-register)  
Page: ~82

---

## Chapter 6: Requirements Analysis

**Table 6.1:** Functional Requirements (FR1-FR20)  
*Complete list of 20 functional requirements organized by category: Object Recognition (FR1-FR4), Navigation (FR5-FR10), Voice (FR11-FR13), Settings (FR14-FR15), Data Management (FR16-FR17), Accessibility (FR18-FR20)*  
Source: [Chapter_06_Requirements_Analysis.md](Chapter_06_Requirements_Analysis.md#62-functional-requirements)  
Page: ~88

**Table 6.2:** Non-Functional Requirements (NFR1-NFR30)  
*Complete list of 30 non-functional requirements organized by category: Performance (NFR1-NFR6), Usability (NFR7-NFR12), Accessibility (NFR13-NFR18), Security (NFR19-NFR24), Reliability (NFR25-NFR27), Maintainability (NFR28-NFR30)*  
Source: [Chapter_06_Requirements_Analysis.md](Chapter_06_Requirements_Analysis.md#63-non-functional-requirements)  
Page: ~90

**Table 6.3:** User Stories Mapped to Requirements  
*15 user stories in "As a [role], I want [feature] so that [benefit]" format, each mapped to corresponding functional requirements*  
Source: [Chapter_06_Requirements_Analysis.md](Chapter_06_Requirements_Analysis.md#65-user-stories)  
Page: ~94

**Table 6.4:** Requirements Traceability Matrix  
*Matrix linking functional requirements (FR1-FR20) to design components, implementation modules, and test cases*  
Source: [Chapter_06_Requirements_Analysis.md](Chapter_06_Requirements_Analysis.md#68-requirements-traceability)  
Page: ~96

---

## Chapter 7: Implementation

**Table 7.1:** Development Environment and Tools  
*List of tools: Android Studio Electric Eel 2022.1.1, Kotlin 1.8.10, Gradle 7.4, JDK 11, TensorFlow Lite 2.8.0, Git 2.39, Figma (UI design)*  
Source: [Chapter_07_Implementation.md](Chapter_07_Implementation.md#71-development-environment)  
Page: ~100

**Table 7.2:** Project Dependencies (build.gradle.kts)  
*Key dependencies with versions: Jetpack Compose 1.1.0, Hilt 2.44, Room 2.5.0, CameraX 1.2.0, TensorFlow Lite 2.8.0, Location Services 21.0.1, Retrofit 2.9.0*  
Page: ~102

**Table 7.3:** Implementation Challenges and Solutions  
*6 challenges with descriptions and solutions: Camera initialization, Model optimization, Beacon interference, TalkBack compatibility, Memory management, Navigation accuracy*  
Source: [Chapter_07_Implementation.md](Chapter_07_Implementation.md#table-73-implementation-challenges-and-solutions)  
Page: ~116

---

## Chapter 8: Testing and Evaluation

**Table 8.1:** Unit Testing Summary  
*Test coverage by module: AIInferenceService (92%), LocationService (85%), BeaconScanner (88%), VoiceRecognitionService (81%), TTSService (95%), CameraService (78%), NavigationService (82%), Overall (84%)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#821-unit-testing)  
Page: ~110

**Table 8.2:** Object Recognition Test Results  
*10 test scenarios with expected vs. actual results, confidence scores, pass/fail: Person detection (Pass, 0.94), Chair (Pass, 0.87), Car (Pass, 0.91), Table (Pass, 0.82), Door (Pass, 0.79), Phone (Pass, 0.76), Book (Fail, 0.51), Cup (Pass, 0.73), Laptop (Pass, 0.88), Multiple objects (Pass, avg 0.84)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-82-object-recognition-test-results)  
Page: ~112

**Table 8.3:** Navigation Test Results (Indoor and Outdoor)  
*12 test scenarios across indoor (6 tests) and outdoor (6 tests): Room-to-room (Pass, 2.1m error), Long corridor (Pass, 3.4m), Multi-floor (Pass, 2.8m), Complex route (Pass, 3.9m), Elevator integration (Pass, 1.9m), Emergency exit (Pass, 2.6m), Campus walk (Pass, 5.8m), Street crossing (Pass, 6.7m), Bus stop (Pass, 5.2m), Point of interest (Pass, 7.1m), Dense urban (Pass, 8.3m), Park pathway (Pass, 4.9m)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-83-navigation-test-results)  
Page: ~114

**Table 8.4:** Voice Command Recognition Results  
*15 voice commands tested with recognition accuracy: "What's in front of me?" (100%), "Find a chair" (93.3%), "Navigate to exit" (100%), "Read text" (86.7%), "Increase voice speed" (100%), "Where am I?" (100%), "Save location" (93.3%), "Detect colors" (86.7%), "Measure distance" (80.0%), "Repeat" (100%), "Help" (100%), "Settings" (93.3%), "Cancel" (100%), "Pause" (100%), "Continue" (93.3%), Overall (92.1%)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#833-voice-command-testing)  
Page: ~116

**Table 8.5:** Accessibility Testing Results (WCAG 2.1 Compliance)  
*9 accessibility tests: Screen reader compatibility (Pass), Color contrast (Pass), Keyboard navigation (Pass), Text resizing (Pass), Focus indicators (Pass), Alternative text (Pass), Audio descriptions (Pass), Time limits (Pass), Seizure prevention (Pass), 100% pass rate*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#84-accessibility-testing)  
Page: ~118

**Table 8.6:** Performance Benchmarks Across Devices  
*9 metrics tested on 4 devices (Budget Nokia 5.3, Mid-range Samsung A52, High-end Pixel 6, Flagship Galaxy S21): Inference latency (427ms → 183ms), App launch time (3.2s → 1.1s), Memory usage (287MB → 198MB), Battery consumption (18.7% → 8.2%/hr), Frame rate (22fps → 58fps), Object detection accuracy (78.2% → 87.1%), Indoor positioning (2.9m → 1.8m error), Voice recognition (88.3% → 95.2%), UI responsiveness (342ms → 87ms)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-86-performance-benchmarks-across-devices)  
Page: ~120

**Table 8.7:** Object Recognition Accuracy Metrics by Category  
*Detailed accuracy for 15 object categories: Person (100%, 30/30), Chair (93.3%, 28/30), Car (91.7%, 22/24), Table (86.7%, 26/30), Door (83.3%, 25/30), Phone (80.0%, 24/30), Bottle (76.7%, 23/30), Cup (73.3%, 22/30), Keyboard (73.3%, 22/30), Laptop (86.7%, 26/30), Book (60.0%, 18/30), Clock (70.0%, 21/30), Backpack (80.0%, 24/30), Handbag (76.7%, 23/30), TV (83.3%, 25/30), Overall (83.2%, 359/432)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-87-object-recognition-accuracy-metrics)  
Page: ~122

**Table 8.8:** User Acceptance Testing Participant Demographics  
*15 participants: Age range (23-68), Gender (9M/6F), Vision status (6 totally blind/5 low vision/4 recently blind), TalkBack experience (3 novice/7 intermediate/5 expert), Education (2 secondary/8 undergraduate/5 postgraduate), Employment (7 employed/4 unemployed/3 retired/1 student)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-88-user-acceptance-testing-participant-demographics)  
Page: ~124

**Table 8.9:** Task Success Rate by Participant  
*10 tasks tested with 15 participants (P01-P15): Object identification (14/15, 93.3%), Voice command (15/15, 100%), Indoor navigation (13/15, 86.7%), Outdoor navigation (12/15, 80.0%), Location saving (15/15, 100%), Text reading (13/15, 86.7%), Color detection (14/15, 93.3%), Distance measurement (11/15, 73.3%), Settings adjustment (15/15, 100%), Multi-object scene (13/15, 86.7%), Overall (137/150, 91.3%)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-89-task-success-rate)  
Page: ~126

**Table 8.10:** System Usability Scale (SUS) Scores  
*15 participants with individual SUS scores ranging 62-91, Mean 78.5, SD 8.3, Median 79, Grade B (Good), Percentile Rank 73rd*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-810-sus-scores)  
Page: ~128

**Table 8.11:** WCAG 2.1 Level AA Compliance Checklist  
*16 key criteria tested: Perceivable (4.1.1-4.1.4 all pass), Operable (4.2.1-4.2.4 all pass), Understandable (4.3.1-4.3.4 all pass), Robust (4.4.1-4.4.3 all pass), Result: 16/16 criteria passed (100%)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-811-wcag-21-level-aa-compliance)  
Page: ~130

**Table 8.12:** Security Testing Results  
*8 security tests: Data encryption (Pass), Secure storage (Pass), Network security (Pass), Permission handling (Pass), Input validation (Pass), Authentication (Pass), Privacy compliance (Pass), Vulnerability scan (Pass), 100% pass rate*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#87-security-testing)  
Page: ~132

**Table 8.13:** Edge Case Testing Results  
*12 edge cases: No internet (Pass), Low battery (Pass), Poor lighting (Pass), No GPS signal (Pass), Multiple simultaneous objects (Pass), Rapid movement (Partial), Extreme angles (Partial), Occluded objects (Partial), Background noise (Pass), Fast speech (Pass), Accented speech (Pass), Long-running session (Pass), 9/12 pass, 3/12 partial*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#88-edge-case-testing)  
Page: ~134

**Table 8.14:** Competitive Comparison - VisionFocus vs. Existing Solutions  
*Comparison across 10 dimensions with VisionFocus, Seeing AI, Google Lookout, Be My Eyes: Object recognition (83.2% vs 87% vs 79% vs N/A), Indoor navigation (87% vs N/A vs N/A vs N/A), Offline capability (Full vs Partial vs Partial vs None), Privacy (No uploads vs Cloud vs Cloud vs Cloud), Voice interface (92.1% vs 88% vs N/A vs N/A), Cost (Free vs Free vs Free vs Free/Premium), Accuracy latency (320ms vs 420ms vs 510ms vs N/A), WCAG compliance (AA vs AA vs A vs N/A), Platform (Android vs iOS/Android vs Android vs iOS/Android), Integration (High vs Medium vs Low vs None)*  
Source: [Chapter_08_Testing_Evaluation.md](Chapter_08_Testing_Evaluation.md#table-814-competitive-comparison)  
Page: ~136

---

## Chapter 10: Results and Analysis

**Table 10.1:** Object Recognition Accuracy Summary  
*Overall: 359/432 correct (83.2%), True Positives 359, False Positives 28, False Negatives 73, Precision 92.8%, Recall 83.2%, F1 Score 87.7%*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-101-object-recognition-accuracy-summary)  
Page: ~142

**Table 10.2:** Object Recognition Accuracy by Category (Top 15)  
*Person (100%), Chair (93.3%), Car (91.7%), Laptop (86.7%), Table (86.7%), Door (83.3%), TV (83.3%), Phone (80.0%), Backpack (80.0%), Bottle (76.7%), Handbag (76.7%), Cup (73.3%), Keyboard (73.3%), Clock (70.0%), Book (60.0%), Overall (83.2%)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-102-accuracy-by-category)  
Page: ~144

**Table 10.3:** Object Recognition Performance by Environmental Condition  
*6 conditions tested: Optimal indoor (89.3%, 134/150), Low light <10 lux (64.2%, 96/150), Bright sunlight >10000 lux (78.5%, 118/150), Cluttered scenes (72.1%, 108/150), Partially occluded (51.8%, 78/150), Extreme angles >45° (64.8%, 97/150)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-103-environmental-conditions)  
Page: ~146

**Table 10.4:** Inference Latency Breakdown Across Device Tiers  
*4 devices with 3-stage latency: Nokia 5.3 (52ms preprocessing, 267ms inference, 108ms postprocessing, 427ms total), Samsung A52 (32ms, 181ms, 67ms, 280ms), Pixel 6 (26ms, 163ms, 54ms, 243ms), Galaxy S21 (18ms, 138ms, 27ms, 183ms)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-104-latency-breakdown)  
Page: ~148

**Table 10.5:** Indoor Positioning Accuracy by Beacon Configuration  
*5 configurations: 1 beacon (8.3m ±3.7m error, 42.3% <3m target), 2 beacons (4.7m ±2.1m, 68.5%), 3 beacons (3.1m ±1.3m, 83.2%), 4 beacons (2.6m ±1.1m, 91.7%), 5+ beacons (2.3m ±0.9m, 94.8%)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-105-indoor-positioning-accuracy)  
Page: ~150

**Table 10.6:** Navigation Task Performance Summary  
*Indoor (6 tasks): 87% avg success, 2.8m avg error, 3.2min avg time | Outdoor (6 tasks): 83% avg success, 6.2m avg error, 4.7min avg time | Overall (12 tasks): 85% avg success, 4.5m avg error, 4.0min avg time*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-106-navigation-performance)  
Page: ~152

**Table 10.7:** Resource Consumption Metrics  
*5 metrics: Battery drain (12.3% per hour, 8.1 hours typical usage), Memory usage (Peak 312MB, Average 247MB, <500MB target), CPU utilization (Active recognition 42%, Navigation 28%, Idle 8%), Network data (Offline 0 KB/day, Map updates 2.3 MB/day, Analytics 0.5 MB/day), Storage (App 87MB, ML models 26MB, Cache 15MB, User data 8MB, Total 136MB)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-107-resource-consumption)  
Page: ~154

**Table 10.8:** System Reliability Metrics  
*6 metrics: Crash rate (0.08% sessions, 1 crash per 1250 sessions), Error rate (2.3% operations, majority recoverable), Recovery time (Avg 1.8s from errors), Uptime (99.4% availability), Data loss incidents (0 reported), Network resilience (95.8% offline capability maintained)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-108-reliability-metrics)  
Page: ~156

**Table 10.9:** Task Success Summary by Task Type  
*10 task types: Object identification (14/15, 93.3%), Voice command (15/15, 100%), Indoor navigation (13/15, 86.7%), Outdoor navigation (12/15, 80.0%), Location saving (15/15, 100%), Text reading (13/15, 86.7%), Color detection (14/15, 93.3%), Distance measurement (11/15, 73.3%), Settings adjustment (15/15, 100%), Multi-object recognition (13/15, 86.7%), Overall (137/150, 91.3%)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-109-task-success-summary)  
Page: ~158

**Table 10.10:** User Satisfaction Scores by Demographic Segment  
*By age group (18-35: 81.2, 36-50: 79.8, 51-65: 76.1, 66+: 73.5) | By vision status (Totally blind: 80.3, Low vision: 78.2, Recently blind: 76.8) | By tech proficiency (Novice: 72.1, Intermediate: 79.3, Expert: 83.8) | Overall: 78.5 (Grade B)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-1010-user-satisfaction)  
Page: ~160

**Table 10.11:** Competitive Performance Comparison  
*4 apps across 8 dimensions: Object accuracy (VisionFocus 83.2% vs Seeing AI 87% vs Lookout 79% vs Be My Eyes N/A), Latency (320ms vs 420ms vs 510ms vs N/A), Privacy score (10/10 vs 6/10 vs 6/10 vs 4/10), Navigation capability (Yes vs No vs No vs No), Offline mode (Full vs Partial vs Partial vs None), Voice interface (92.1% vs 88% vs N/A vs N/A), Cost (Free vs Free vs Free vs Free+Premium), WCAG level (AA vs AA vs A vs N/A)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-1011-competitive-comparison)  
Page: ~162

**Table 10.12:** Requirements Fulfillment Analysis  
*65 total requirements: Fully met (60 req, 92.3%), Partially met (4 req, 6.2%), Not met (1 req, 1.5%) | By category: Functional (18/20 fully, 2/20 partially), Performance (5/6 fully, 1/6 partially), Usability (6/6 fully), Accessibility (6/6 fully), Security (6/6 fully), Reliability (5/6 fully, 1/6 not met), Maintainability (3/3 fully)*  
Source: [Chapter_10_Results_Analysis.md](Chapter_10_Results_Analysis.md#table-1012-requirements-fulfillment)  
Page: ~164

---

**Total Tables: 41**

*Note: Page numbers are approximate and will be finalized during document compilation. All tables are available in full detail in their respective chapter markdown files.*

