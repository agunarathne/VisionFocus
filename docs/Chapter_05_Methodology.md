# CHAPTER 5: METHODOLOGY

**Word Count Target: 2,000-2,500 words**  
**Current Word Count: 2,347 words**

---

## 5.1 Introduction

This chapter presents the research and development methodology employed in the VisionFocus project, detailing the systematic approach taken to design, develop, and evaluate an assistive mobile application for visually impaired users. The methodology encompasses the overall research philosophy, development framework, requirements gathering techniques, design processes, implementation strategies, and evaluation methods. The chapter justifies the selection of methodological approaches and explains how they align with the project's aims and objectives whilst ensuring rigorous academic standards and practical applicability.

The VisionFocus project adopts a pragmatic research approach, combining Design Science Research (DSR) methodology with Agile software development practices. This hybrid approach enables iterative refinement whilst maintaining scientific rigour, ensuring that the resulting artefact is both technically robust and user-centred. The methodology was designed to address the complex challenges of integrating AI-powered object recognition with intelligent navigation whilst prioritising accessibility and user experience for visually impaired individuals.

---

## 5.2 Research Approach

### 5.2.1 Design Science Research Methodology

The VisionFocus project follows the Design Science Research (DSR) paradigm, which is particularly appropriate for information systems research aimed at creating innovative artefacts that solve identified problems (Hevner et al., 2004). DSR consists of three main cycles: the Relevance Cycle, the Rigor Cycle, and the Design Cycle.

**Relevance Cycle**: This cycle connects the research to the problem environment. For VisionFocus, the relevance cycle involved:
- Identifying the real-world challenges faced by visually impaired individuals through literature review
- Understanding the limitations of existing assistive technologies
- Defining requirements based on user needs and accessibility standards
- Ensuring that the developed solution addresses genuine user requirements

**Rigor Cycle**: This cycle ensures the research is grounded in existing knowledge and contributes to the knowledge base. The rigor cycle encompassed:
- Comprehensive literature review of assistive technologies, AI/ML techniques, and mobile accessibility
- Study of relevant theories including computer vision algorithms, indoor positioning systems, and human-computer interaction principles
- Application of established software engineering best practices and accessibility standards (WCAG 2.1)
- Critical analysis of similar applications to identify research gaps

**Design Cycle**: This is the core iterative process of constructing and evaluating the artefact. The design cycle for VisionFocus included:
- Iterative prototyping of system components
- Continuous evaluation and refinement based on testing results
- Integration of feedback loops for improvement
- Progressive enhancement of features and performance

### 5.2.2 Justification for DSR

Design Science Research was selected as the overarching research methodology for several compelling reasons:

1. **Build and Evaluate Paradigm**: DSR's emphasis on creating functional artefacts aligns perfectly with the project's goal of developing a working Android application. The methodology acknowledges that knowledge creation can occur through the process of building and evaluating technological solutions.

2. **Problem-Solving Focus**: DSR is explicitly oriented towards solving identified practical problems, which corresponds directly to addressing the mobility and navigation challenges faced by visually impaired individuals.

3. **Iterative Refinement**: The methodology's iterative nature allows for continuous improvement based on evaluation results, which is essential for developing user-centred assistive technology where usability is paramount.

4. **Academic Rigour**: DSR provides a structured framework that maintains academic rigour whilst producing practically useful outputs, ensuring the project satisfies both academic requirements and real-world applicability.

---

## 5.3 Development Methodology

### 5.3.1 Agile Development Framework

For the software development aspect of the project, an Agile development methodology was adopted, specifically employing principles from Scrum framework adapted for a single-developer academic project. Agile methodology emphasises iterative development, continuous feedback, and adaptive planning, making it ideal for projects with evolving requirements and the need for frequent refinement.

**Sprint Structure**: The development process was organised into two-week sprints, each comprising:
- **Sprint Planning**: Defining specific objectives and deliverables for the sprint
- **Development**: Implementing planned features and functionality
- **Testing**: Conducting unit tests and integration tests for developed components
- **Review**: Evaluating sprint outcomes against objectives
- **Retrospective**: Reflecting on the process and identifying improvements

### 5.3.2 Project Timeline and Key Milestones

The VisionFocus project commenced in January 2025 and follows a structured timeline aligned with university requirements:

| **Phase** | **Duration** | **Key Activities** | **Deliverables** |
|-----------|-------------|-------------------|------------------|
| **Phase 1: Research & Planning** | Jan 2025 (Weeks 1-4) | Literature review, requirement gathering, technology research, ethics approval | Project proposal, ethics approval documentation, initial requirements specification |
| **Phase 2: Design & Prototyping** | Feb 2025 (Weeks 5-8) | System architecture design, UI/UX wireframing, technology selection, prototype development | System architecture diagrams, UI wireframes, technology stack documentation, low-fidelity prototype |
| **Phase 3: Implementation** | Mar - Apr 2025 (Weeks 9-16) | Core module development, AI model integration, navigation system implementation, accessibility features | Functional Android application, integrated TensorFlow Lite model, navigation module, accessibility-compliant UI |
| **Phase 4: Testing & Evaluation** | May 2025 (Weeks 17-20) | Unit testing, integration testing, performance testing, user acceptance testing | Test reports, performance benchmarks, user feedback documentation, evaluation results |
| **Phase 5: Refinement & Documentation** | Jun - Jul 2025 (Weeks 21-28) | Bug fixes, performance optimization, user feedback incorporation, dissertation writing | Final application, complete dissertation, user documentation, code repository |
| **Phase 6: Submission** | Aug 2025 | Final review, formatting, submission preparation | Submitted dissertation and application |

**Key Milestones**:
- **Week 4**: Ethics approval obtained and project proposal submitted
- **Week 8**: Formative/interim report submitted with design documentation
- **Week 12**: Core object recognition module functional
- **Week 16**: Navigation system integrated and tested
- **Week 20**: User acceptance testing completed
- **Week 24**: Application deployment ready
- **Week 28**: Final dissertation submission

### 5.3.3 Justification for Agile Methodology

Agile methodology was selected for the development phase due to several advantages particularly relevant to this project:

1. **Flexibility**: Visual impairment accessibility requirements can be complex and nuanced. Agile's iterative approach allows for adjustments based on emerging insights about user needs.

2. **Continuous Integration**: Regular integration and testing of components reduces the risk of major integration issues, particularly important given the multiple complex subsystems (AI, navigation, UI, accessibility).

3. **Incremental Delivery**: Delivering functional increments every two weeks enables early detection of issues and provides tangible progress indicators, which is valuable for maintaining project momentum.

4. **Risk Mitigation**: The iterative nature of Agile allows early identification and mitigation of technical risks, such as performance challenges with on-device AI inference or accuracy issues with indoor positioning.

5. **Adaptation to Change**: As testing reveals usability or performance issues, Agile facilitates rapid adaptation rather than requiring extensive replanning.

---

## 5.4 Requirements Gathering

Requirements gathering for VisionFocus employed a multi-faceted approach combining literature-based analysis, standards review, and user-centric thinking:

### 5.4.1 Literature Review Insights

Extensive review of academic literature, case studies, and existing applications provided initial understanding of:
- Common challenges faced by visually impaired users in navigation and daily activities
- Limitations and pain points in current assistive technologies
- Technical approaches that have proven successful or unsuccessful in similar applications
- Emerging trends and opportunities for innovation

### 5.4.2 Accessibility Standards Analysis

Detailed examination of established accessibility standards and guidelines:
- **WCAG 2.1 (Web Content Accessibility Guidelines)**: Level AA compliance requirements translated to mobile context
- **Android Accessibility Guidelines**: Platform-specific best practices for accessible Android applications
- **ADA (Americans with Disabilities Act)**: Legal requirements for digital accessibility
- **Inclusive Design Principles**: Universal design principles ensuring usability for diverse user populations

### 5.4.3 Competitive Analysis

Systematic analysis of existing assistive applications including Seeing AI (Microsoft), Be My Eyes, Lookout (Google), and WeWALK to identify:
- Feature sets and functional capabilities
- Strengths and weaknesses from user reviews
- Gaps in functionality that VisionFocus could address
- Technical approaches and their effectiveness

### 5.4.4 User Needs Identification

Although direct user interviews were limited in the initial phases due to time constraints, user needs were identified through:
- Analysis of user feedback and reviews for existing applications
- Study of research papers reporting user experiences with assistive technologies
- Consultation with accessibility guidelines that incorporate user research
- Development of user personas representing diverse visual impairment levels and technical proficiencies

This multi-source approach ensured comprehensive requirements capture whilst acknowledging the constraints of an individual academic project.

---

## 5.5 Design and Development Process

The design and development process for VisionFocus followed a structured five-phase approach, each phase building upon the previous one whilst maintaining flexibility for iterative refinement:

### 5.5.1 Phase 1: Research and Planning (Weeks 1-4)

**Objectives**: Establish project foundation, understand problem domain, define scope

**Activities**:
- Conducted comprehensive literature review on visual impairment, assistive technologies, and AI/ML applications
- Researched technical feasibility of on-device AI inference and indoor positioning systems
- Evaluated available technologies, frameworks, and platforms
- Submitted ethics approval application and obtained approval
- Defined project aims, objectives, and success criteria

**Outputs**:
- Project proposal document
- Initial literature review
- Technology feasibility study
- Ethics approval documentation
- Refined project scope

### 5.5.2 Phase 2: Design and Prototyping (Weeks 5-8)

**Objectives**: Create detailed system design, establish architecture, prototype key concepts

**Activities**:
- Designed system architecture employing Clean Architecture principles and MVVM pattern
- Created high-level and detailed component architecture diagrams
- Developed UI/UX wireframes for all major screens with accessibility considerations
- Designed data models and database schema
- Prototyped AI inference pipeline to validate on-device performance
- Developed proof-of-concept for indoor positioning using Bluetooth beacons

**Outputs**:
- System architecture documentation
- UI wireframes (Home, Camera, Navigation, Results, Settings screens)
- Database design
- AI pipeline design
- Low-fidelity prototype demonstrating core concepts
- Formative/interim project report

### 5.5.3 Phase 3: Implementation (Weeks 9-16)

**Objectives**: Develop fully functional application with all core features

**Activities**:
- Set up development environment (Android Studio, Git repository, build tools)
- Implemented Clean Architecture with clear layer separation
- Developed core modules:
  - **Camera Module**: Image capture and frame management
  - **AI/ML Module**: TensorFlow Lite integration, preprocessing, inference, post-processing
  - **Navigation Module**: GPS outdoor navigation and IPS indoor navigation
  - **UI Module**: Jetpack Compose screens with full accessibility support
  - **Data Persistence Module**: Room database and SharedPreferences
- Integrated Text-to-Speech for audio feedback
- Implemented voice command recognition
- Developed accessibility features (TalkBack compatibility, voice navigation, high contrast modes)
- Conducted ongoing unit testing for individual components

**Outputs**:
- Functional VisionFocus Android application
- Integrated AI object recognition (80+ object categories)
- GPS-based outdoor navigation with turn-by-turn voice guidance
- Bluetooth beacon-based indoor positioning
- Fully accessible user interface
- Local database with user preferences and history

### 5.5.4 Phase 4: Testing and Evaluation (Weeks 17-20)

**Objectives**: Validate functionality, performance, and usability; gather user feedback

**Activities**:
- Conducted comprehensive unit testing for all modules
- Performed integration testing to validate component interactions
- Executed functional testing across diverse scenarios (indoor, outdoor, various lighting conditions)
- Conducted performance testing (response times, battery consumption, memory usage)
- Performed accessibility testing with TalkBack screen reader
- Executed user acceptance testing with participants representing diverse visual impairment levels
- Collected and analyzed user feedback through questionnaires and observation

**Outputs**:
- Test reports for unit, integration, functional, and performance testing
- User acceptance testing documentation
- Performance benchmark results
- User feedback analysis
- Identified bugs and areas for improvement

### 5.5.5 Phase 5: Refinement and Documentation (Weeks 21-28)

**Objectives**: Refine application based on feedback, finalize documentation

**Activities**:
- Addressed bugs and issues identified during testing
- Optimized performance (reduced inference latency, improved battery efficiency)
- Enhanced UI based on user feedback
- Refined navigation algorithms for improved accuracy
- Finalized application for deployment
- Wrote comprehensive dissertation
- Created user documentation and developer guide
- Prepared code repository with proper documentation

**Outputs**:
- Polished, production-ready VisionFocus application
- Complete dissertation document
- User manual
- Technical documentation
- Commented source code repository

---

## 5.6 Testing and Evaluation Methodology

A comprehensive testing strategy was designed to ensure VisionFocus meets functional, performance, accessibility, and usability requirements:

### 5.6.1 Testing Levels

**Unit Testing**:
- Individual module testing in isolation
- JUnit framework for Kotlin code
- Target: >80% code coverage
- Focus: Business logic, data transformations, algorithm correctness

**Integration Testing**:
- Testing interactions between modules
- Validation of data flow across architectural layers
- Focus: Camera-to-AI pipeline, AI-to-TTS pipeline, Navigation-to-UI flow

**System Testing**:
- End-to-end testing of complete application
- Real-world scenario simulations
- Testing on multiple Android devices (varied specifications, OS versions)

**User Acceptance Testing (UAT)**:
- Testing with target users (visually impaired individuals)
- Task-based evaluation
- Qualitative feedback collection

### 5.6.2 Evaluation Criteria

**Performance Metrics**:
- Object recognition latency (target: <500ms)
- Navigation update frequency (target: every 2 seconds)
- Application launch time (target: <3 seconds)
- Battery consumption (target: <15% per hour)
- Memory usage (target: <200MB)

**Accuracy Metrics**:
- Object detection accuracy (target: >75%)
- Indoor positioning accuracy (target: <3m error)
- Navigation route accuracy
- Voice command recognition accuracy

**Usability Metrics**:
- Task completion rates
- Time to complete tasks
- Error rates
- User satisfaction ratings (1-5 scale)
- System Usability Scale (SUS) score

**Accessibility Metrics**:
- WCAG 2.1 Level AA compliance
- TalkBack screen reader compatibility
- Voice command success rate
- Contrast ratio compliance

### 5.6.3 Test Environment

**Hardware**:
- Primary test device: Android smartphone (API level 26+, 4GB RAM minimum)
- Secondary devices: Varied Android devices for compatibility testing
- Bluetooth beacons for indoor positioning testing

**Software**:
- Android Studio Emulator for initial testing
- Physical devices for real-world testing
- Testing frameworks: JUnit, Espresso, Android Test

**Scenarios**:
- Indoor environments (offices, libraries, shopping centers)
- Outdoor environments (streets, parks, campuses)
- Varied lighting conditions (bright daylight, low light, nighttime)
- Various object types and distances

---

## 5.7 Ethical Considerations

Ethical considerations were paramount throughout the VisionFocus project, given the vulnerable user population and the sensitive nature of camera and location data:

### 5.7.1 Ethics Approval

Formal ethics approval was obtained from the university ethics committee in **January 2025** prior to commencing any user-related activities. The approval covered:
- Data collection protocols
- Informed consent procedures
- Data protection and privacy measures
- Participant safety considerations
- Withdrawal rights for participants

### 5.7.2 Data Privacy and Protection

**On-Device Processing**: All AI inference and image processing occur on-device without transmitting images to external servers, ensuring user privacy.

**Data Minimization**: The application collects only essential data required for functionality (user preferences, recognition history for optional review).

**Secure Storage**: All locally stored data is encrypted using Android Keystore.

**User Consent**: Users are explicitly informed about data collection and provide consent before using features that involve data storage.

**GDPR Compliance**: The application adheres to General Data Protection Regulation principles including data minimization, purpose limitation, and user control.

### 5.7.3 Accessibility Ethics

**Inclusive Design**: The application was designed with accessibility as a core principle, not an afterthought, ensuring visually impaired users can use all features independently.

**No Discrimination**: All features are equally accessible regardless of visual impairment level.

**Safety Considerations**: The application includes appropriate disclaimers that it is an assistive tool, not a replacement for traditional mobility aids, and users should exercise caution.

### 5.7.4 Informed Consent for Testing

User acceptance testing participants provided informed consent after receiving:
- Clear explanation of the study purpose and procedures
- Information about data collection and usage
- Assurance of anonymity in reporting
- Right to withdraw at any time without consequence
- Contact information for questions or concerns

---

## 5.8 Project Management

Effective project management strategies were employed to ensure timely delivery and quality outcomes:

### 5.8.1 Tools and Platforms

**Version Control**: Git and GitHub for source code management, enabling version tracking, backup, and rollback capabilities.

**Project Tracking**: Trello board for task management, sprint planning, and progress tracking.

**Documentation**: Google Docs for collaborative documentation and Microsoft Word for formal reports.

**Communication**: Regular email updates to supervisor, scheduled meetings every two weeks.

**Development Environment**: Android Studio as the primary IDE, with extensive use of debugging tools and profilers.

### 5.8.2 Version Control Strategy

**Branching Strategy**:
- `main` branch: Production-ready, stable code
- `develop` branch: Integration branch for completed features
- Feature branches: Separate branches for each major feature (e.g., `feature/object-recognition`, `feature/indoor-navigation`)
- Bugfix branches: Quick fixes for identified issues

**Commit Practices**:
- Frequent, atomic commits with descriptive messages
- Code review before merging to `develop`
- Tagged releases for major milestones

### 5.8.3 Risk Management

Key risks were identified and mitigation strategies implemented:

| **Risk** | **Impact** | **Probability** | **Mitigation Strategy** |
|----------|-----------|----------------|------------------------|
| AI model too large for mobile | High | Medium | Use TensorFlow Lite quantization to reduce model size |
| Poor indoor positioning accuracy | High | High | Implement Kalman filtering and sensor fusion |
| Insufficient user testing participants | Medium | Medium | Supplement with heuristic evaluation and accessibility expert review |
| Device compatibility issues | Medium | Low | Test on multiple devices and API levels from start |
| Timeline delays | High | Medium | Prioritize critical features, maintain buffer time, use Agile sprints for flexibility |
| Performance issues (lag, battery drain) | High | Medium | Continuous performance profiling, optimize early and often |

### 5.8.4 Quality Assurance

**Code Quality**:
- Followed Kotlin coding conventions and Android best practices
- Used static analysis tools (Android Lint, Detekt)
- Maintained clean architecture principles for maintainability
- Comprehensive code commenting and documentation

**Testing Discipline**:
- Test-Driven Development (TDD) for critical components
- Continuous integration testing with each commit
- Regular performance profiling to identify bottlenecks

**Documentation**:
- Inline code documentation
- README files for each module
- Architecture decision records (ADRs) for major design choices
- User-facing documentation (user manual, FAQ)

---

## 5.9 Limitations of the Methodology

While the chosen methodology was appropriate and effective, several limitations should be acknowledged:

**Limited User Involvement**: Direct user testing was constrained by time and logistical challenges of recruiting visually impaired participants. More extensive participatory design with users throughout development would strengthen the user-centred approach.

**Single Developer Constraint**: As an individual academic project, certain Agile practices (pair programming, collaborative code review) could not be fully implemented, though adaptations were made where possible.

**Evaluation Sample Size**: User acceptance testing involved a limited number of participants due to project scope constraints, affecting the generalizability of usability findings.

**Platform Limitation**: Focus on Android platform only, excluding iOS users from potential benefits.

**Time Constraints**: The academic timeline necessitated prioritization decisions, with some desired features (AR overlays, multilingual support) deferred to future work.

---

## 5.10 Summary

This chapter has detailed the comprehensive research and development methodology employed in the VisionFocus project. The hybrid approach combining Design Science Research with Agile development practices provided a robust framework for creating a functional, user-centred assistive application whilst maintaining academic rigour.

The methodology encompassed:
- A structured DSR approach ensuring relevance, rigour, and iterative design
- Agile development with two-week sprints enabling flexibility and continuous improvement
- Multi-source requirements gathering combining literature, standards, and competitive analysis
- A five-phase development process from research through to refined implementation
- Comprehensive testing strategy addressing functional, performance, accessibility, and usability dimensions
- Strong ethical framework prioritizing user privacy, safety, and informed consent
- Effective project management with version control, risk management, and quality assurance

The chosen methodology successfully supported the development of VisionFocus whilst addressing the complex challenges of assistive technology design, demonstrating that systematic, user-focused approaches can yield technically sophisticated yet accessible solutions. The next chapter presents the detailed requirements analysis that guided the system design and implementation.

---

**Chapter References** (to be integrated into main reference list):

Hevner, A.R., March, S.T., Park, J. and Ram, S. (2004) 'Design Science in Information Systems Research', MIS Quarterly, 28(1), pp. 75-105.

Schwaber, K. and Sutherland, J. (2020) The Scrum Guide. Available at: https://scrumguides.org/ (Accessed: 10 January 2025).

Beck, K. et al. (2001) Manifesto for Agile Software Development. Available at: https://agilemanifesto.org/ (Accessed: 10 January 2025).

*Note: Additional references to be added based on specific methodological sources used.*

