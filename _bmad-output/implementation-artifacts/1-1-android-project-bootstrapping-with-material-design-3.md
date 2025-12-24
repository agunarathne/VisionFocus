# Story 1.1: Android Project Bootstrapping with Material Design 3

Status: done

## Story

As a developer,
I want to initialize a minimal Android project with Kotlin and Material Design 3 theming,
So that the app foundation supports accessibility requirements and matches the UX specification.

## Acceptance Criteria

**Given** VS Code with Android SDK configured manually (not Android Studio)
**When** I create the project structure with minimal template approach
**Then** the project initializes with API 26+ minimum, API 34+ target
**And** Material Design 3 dependencies are configured (material:1.11.0+)
**And** Dark theme default (#121212 background) with high-contrast theme variant ready
**And** Roboto font with increased base sizes (body 20sp) configured
**And** MainActivity created with basic navigation structure
**And** Project builds successfully via Gradle in VS Code
**And** App launches on emulator/device displaying Material Design 3 themed empty activity

## Tasks / Subtasks

- [x] Task 1: Create minimal Android project structure (AC: 1, 2, 6, 7)
  - [x] 1.1: Set up Gradle build files (project-level and app-level)
  - [x] 1.2: Configure AndroidManifest.xml with API levels
  - [x] 1.3: Create basic folder structure (kotlin, res, assets)
  - [x] 1.4: Verify project builds via `./gradlew assembleDebug`
  
- [x] Task 2: Configure Material Design 3 dependencies (AC: 3)
  - [x] 2.1: Add material:1.11.0+ to app-level build.gradle.kts
  - [x] 2.2: Add androidx.core and appcompat dependencies
  - [x] 2.3: Sync Gradle dependencies
  
- [x] Task 3: Implement Material Design 3 theming (AC: 4)
  - [x] 3.1: Create themes.xml with Theme.VisionFocus (dark theme default)
  - [x] 3.2: Create themes.xml variant for high-contrast mode
  - [x] 3.3: Define color resources with #121212 background
  - [x] 3.4: Create night/themes.xml for dark theme support
  
- [x] Task 4: Configure typography with accessibility-first sizing (AC: 5)
  - [x] 4.1: Define Roboto font family in res/font/ (Note: Using system default sans-serif which maps to Roboto on Android. Directory created with .gitkeep. See docs/font-configuration.md for rationale and future enhancement path)
  - [x] 4.2: Create type scale in themes.xml with body 20sp
  - [x] 4.3: Define dimension resources for accessibility (48dp touch targets)
  
- [x] Task 5: Create MainActivity with basic structure (AC: 6)
  - [x] 5.1: Implement MainActivity.kt with AppCompatActivity
  - [x] 5.2: Create activity_main.xml layout
  - [x] 5.3: Apply Material Design 3 theme to activity
  - [x] 5.4: Add basic navigation structure (placeholder for future fragments)
  
- [x] Task 6: Verification and testing (AC: 7, 8)
  - [x] 6.1: Build project via `./gradlew assembleDebug`
  - [x] 6.2: Install on emulator/device via `./gradlew installDebug`
  - [x] 6.3: Launch app and verify Material Design 3 themed empty activity displays
  - [x] 6.4: Verify dark theme (#121212 background) renders correctly

## Dev Notes

### Critical Architecture Context

**Starter Approach:** Minimal Android project with research-validated architecture (NOT Now in Android template)

**Rationale:** VisionFocus has 17 dissertation chapters documenting validated architecture. Research implementation already proved Clean Architecture + MVVM patterns work (83.2% accuracy, 320ms latency, SUS 78.5). Implementation should match academically validated research for dissertation fidelity.

**Development IDE:** VS Code (not Android Studio)
- Requires manual Android SDK configuration
- Command-line driven Gradle workflow
- Manual Kotlin language support setup

### Technical Requirements from Architecture

**Platform Configuration:**
- Minimum SDK: API 26+ (Android 8.0 Oreo) - Required for TFLite optimization and modern accessibility APIs
- Target SDK: API 34+ (latest stable)
- Language: Kotlin (100% Kotlin codebase)
- Build System: Gradle with Kotlin DSL (build.gradle.kts)

**Core Dependencies (from Architecture Doc):**
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.11.0")
    
    // Architecture Components (MVVM) - will be added in Story 1.2+
    // Hilt DI - will be added in Story 1.2
    // TFLite - will be added in Epic 2
    // Room - will be added in Story 1.4
}
```

**Material Design 3 Theme Requirements:**
- Dark theme default with #121212 background (validated in research)
- High-contrast theme variant with 7:1 contrast ratio minimum (WCAG 2.1 AA)
- Roboto font with increased base sizes (body 20sp vs default 16sp)
- Semantic colors: success green (#4CAF50), warning amber (#FFC107), error red (#F44336)

**Accessibility Requirements:**
- Minimum touch target size: 48×48 dp enforced in dimension resources
- High-contrast mode: pure black (#000000) background, pure white (#FFFFFF) foreground
- Large text mode: 150% scaling support (30sp body text in large mode)
- No reliance on color alone to convey information

### Project Structure (from Architecture Doc)

```
VisionFocus/
├── app/
│   └── src/main/
│       ├── kotlin/com/visionfocus/
│       │   └── MainActivity.kt              # Story 1.1
│       │
│       ├── res/
│       │   ├── layout/
│       │   │   └── activity_main.xml        # Story 1.1
│       │   ├── values/
│       │   │   ├── strings.xml              # Story 1.1
│       │   │   ├── colors.xml               # Story 1.1
│       │   │   ├── themes.xml               # Story 1.1
│       │   │   └── dimens.xml               # Story 1.1
│       │   ├── values-night/
│       │   │   └── themes.xml               # Dark theme
│       │   └── font/                        # Roboto font family
│       │
│       └── AndroidManifest.xml              # Story 1.1
│
├── build.gradle.kts                         # App-level build config
├── settings.gradle.kts                      # Project-level settings
├── gradle.properties                        # Gradle properties
└── local.properties                         # SDK location (not in git)
```

### VS Code Development Setup

**.vscode/settings.json:**
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "kotlin.languageServer.enabled": true,
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true
  }
}
```

**Build & Run Commands (Terminal-based):**
```bash
# Build debug variant
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean

# Check for dependency updates
./gradlew dependencyUpdates
```

### Material Design 3 Theme Implementation Guide

**Primary Theme (themes.xml):**
```xml
<resources>
    <style name="Theme.VisionFocus" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Primary brand color -->
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        
        <!-- Surface colors (dark theme default) -->
        <item name="colorSurface">@color/surface</item>
        <item name="colorOnSurface">@color/on_surface</item>
        
        <!-- Background colors -->
        <item name="android:colorBackground">@color/background</item>
        <item name="colorOnBackground">@color/on_background</item>
        
        <!-- Typography -->
        <item name="textAppearanceBody1">@style/TextAppearance.VisionFocus.Body1</item>
        
        <!-- Shape -->
        <item name="shapeAppearanceSmallComponent">@style/ShapeAppearance.VisionFocus.SmallComponent</item>
    </style>
</resources>
```

**High-Contrast Theme Variant:**
```xml
<resources>
    <style name="Theme.VisionFocus.HighContrast" parent="Theme.VisionFocus">
        <!-- Pure black background, pure white foreground for 7:1+ contrast -->
        <item name="android:colorBackground">@color/high_contrast_background</item>
        <item name="colorSurface">@color/high_contrast_background</item>
        <item name="colorOnSurface">@color/high_contrast_on_surface</item>
        <item name="colorOnBackground">@color/high_contrast_on_surface</item>
    </style>
</resources>
```

**Color Resources (values/colors.xml):**
```xml
<resources>
    <!-- Standard Dark Theme Colors -->
    <color name="background">#121212</color>
    <color name="surface">#1E1E1E</color>
    <color name="on_surface">#E1E1E1</color>
    <color name="on_background">#E1E1E1</color>
    
    <!-- High-Contrast Mode Colors (7:1 ratio) -->
    <color name="high_contrast_background">#000000</color>
    <color name="high_contrast_on_surface">#FFFFFF</color>
    
    <!-- Semantic Colors -->
    <color name="success_green">#4CAF50</color>
    <color name="warning_amber">#FFC107</color>
    <color name="error_red">#F44336</color>
</resources>
```

**Typography (values/themes.xml):**
```xml
<resources>
    <style name="TextAppearance.VisionFocus.Body1" parent="TextAppearance.Material3.BodyLarge">
        <item name="android:textSize">20sp</item>  <!-- Increased from default 16sp -->
        <item name="android:fontFamily">@font/roboto_regular</item>
        <item name="android:lineHeight">30sp</item>  <!-- 1.5x line height -->
    </style>
    
    <!-- Large text mode variant (150% scaling) -->
    <style name="TextAppearance.VisionFocus.Body1.Large" parent="TextAppearance.VisionFocus.Body1">
        <item name="android:textSize">30sp</item>  <!-- 20sp * 1.5 -->
        <item name="android:lineHeight">45sp</item>
    </style>
</resources>
```

**Dimension Resources (values/dimens.xml):**
```xml
<resources>
    <!-- Accessibility-first touch targets -->
    <dimen name="min_touch_target_size">48dp</dimen>
    <dimen name="preferred_touch_target_size">56dp</dimen>
    
    <!-- Standard spacing -->
    <dimen name="spacing_small">8dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
    <dimen name="spacing_large">24dp</dimen>
    
    <!-- Text sizes -->
    <dimen name="text_size_body">20sp</dimen>
    <dimen name="text_size_body_large">30sp</dimen>  <!-- 150% scaling -->
</resources>
```

### AndroidManifest.xml Configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.visionfocus">

    <!-- Permissions will be added in subsequent stories -->
    <!-- CAMERA permission - Story 1.5 -->
    <!-- ACCESS_FINE_LOCATION - Epic 6 -->
    <!-- RECORD_AUDIO - Epic 3 -->
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.VisionFocus"
        android:supportsRtl="true">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>

</manifest>
```

### Gradle Configuration

**Project-level settings.gradle.kts:**
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VisionFocus"
include(":app")
```

**App-level build.gradle.kts:**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Hilt plugin will be added in Story 1.2
}

android {
    namespace = "com.visionfocus"
    compileSdk = 34  // Latest stable
    
    defaultConfig {
        applicationId = "com.visionfocus"
        minSdk = 26  // Android 8.0 Oreo minimum
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true  // Enabled for XML layouts
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.11.0")
    
    // Testing (basic setup)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### MainActivity Implementation

**MainActivity.kt:**
```kotlin
package com.visionfocus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.visionfocus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // View Binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Basic setup placeholder
        // Future stories will add fragments, navigation, etc.
    }
}
```

**activity_main.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorBackground"
    tools:context=".MainActivity">

    <!-- Placeholder for future content -->
    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/app_name"
        android:textAppearance="@style/TextAppearance.VisionFocus.Body1"
        android:textColor="?attr/colorOnBackground"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**strings.xml:**
```xml
<resources>
    <string name="app_name">VisionFocus</string>
</resources>
```

### Project Structure Notes

**Alignment with Unified Project Structure:**
- Package structure: `com.visionfocus` follows standard Android conventions
- Module organization ready for future expansion (recognition, navigation, voice, etc.)
- Minimal setup aligns with research-validated "minimal Android project" approach
- No conflicts with modularization strategy defined in Architecture Doc

**Detected Conflicts/Variances:**
- None - this is the foundation story establishing the baseline structure
- Future stories will add modules per Architecture Doc's modular structure

### References

**Technical Details with Source Paths:**

1. **Starter Approach Decision:**
   - [Source: _bmad-output/architecture.md#Starter Template & Foundation Decision]
   - Decision: Minimal Android project with research-validated architecture
   - Rationale: 17 dissertation chapters document validated architecture; implementation should match research for academic integrity

2. **Platform Requirements:**
   - [Source: _bmad-output/architecture.md#Project Context Analysis]
   - Minimum SDK: API 26+ (Android 8.0 Oreo)
   - Target SDK: API 34+ (latest stable)
   - Native Android chosen for maximum TalkBack fidelity

3. **Material Design 3 Theme Specification:**
   - [Source: _bmad-output/project-planning-artifacts/ux-design-specification.md#Design System]
   - Dark theme default: #121212 background
   - High-contrast mode: 7:1 minimum contrast ratio
   - Typography: Roboto font, body 20sp (increased from default 16sp)
   - Touch targets: 48×48 dp minimum

4. **Core Dependencies:**
   - [Source: _bmad-output/architecture.md#Implementation Foundation]
   - Material Design 3: material:1.11.0+
   - AndroidX Core: core-ktx:1.12.0
   - AppCompat: appcompat:1.6.1
   - ConstraintLayout: constraintlayout:2.1.4

5. **VS Code Development Setup:**
   - [Source: _bmad-output/architecture.md#VS Code Development Setup]
   - Manual Android SDK configuration required
   - Command-line Gradle workflow
   - .vscode/settings.json configuration for Kotlin support

6. **Accessibility Requirements:**
   - [Source: _bmad-output/prd.md#Accessibility Requirements]
   - WCAG 2.1 AA compliance mandatory
   - 48×48 dp touch targets validated
   - High-contrast mode: 7:1 contrast ratio minimum
   - Large text mode: 150% scaling support

7. **Research Validation Context:**
   - [Source: _bmad-output/prd.md#Project Classification]
   - Research-complete status: 17 dissertation chapters
   - Validated metrics: 83.2% accuracy, 320ms latency, SUS 78.5
   - Architecture patterns proven through research implementation

8. **Project Structure Rationale:**
   - [Source: _bmad-output/architecture.md#Implementation Foundation]
   - Modularized by feature (recognition, navigation, voice, etc.)
   - Clean Architecture + MVVM validated in research
   - View Binding for XML layouts (not Jetpack Compose)

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5

### Debug Log References

**Implementation Plan:**
- Created minimal Android project structure with Gradle Kotlin DSL
- Configured Material Design 3 with dark theme default (#121212 background)
- Implemented high-contrast theme variant for accessibility
- Set up typography with increased base sizes (body 20sp)
- Created MainActivity with ViewBinding
- Established project foundation for future epics

**Technical Decisions:**
- Used Gradle wrapper 8.2 with Android Gradle Plugin 8.2.0
- Kotlin 1.9.20 for modern language features
- Material Design 3 (material:1.11.0) for latest design system
- ViewBinding enabled for type-safe view access
- Min SDK 26 (Android 8.0) for TFLite optimization support
- Target SDK 34 (latest stable)

**Build Configuration:**
- Created Gradle wrapper files manually (gradlew.bat, gradlew, gradle-wrapper.properties)
- Downloaded gradle-wrapper.jar from official Gradle repository
- Configured project-level and app-level build.gradle.kts files
- Set up gradle.properties with AndroidX and performance optimizations

### Completion Notes List

**Story 1.1 Implementation Complete - December 24, 2025**

- ✅ Minimal Android project structure created with Kotlin and Gradle Kotlin DSL
- ✅ Material Design 3 (material:1.11.0) configured with dark theme default (#121212 background)
- ✅ High-contrast theme variant implemented for accessibility (7:1 contrast ratio)
- ✅ Typography configured with 20sp body text (increased from 16sp default)
- ✅ Accessibility dimensions defined (48dp minimum touch targets)
- ✅ MainActivity with ViewBinding created and verified
- ✅ Android SDK components auto-installed (Build Tools 34, Platform 34, Platform-Tools)
- ✅ JDK compatibility resolved (upgraded to AGP 8.3.0 + Gradle 8.4 for JDK 23 support)
- ✅ Project builds successfully: app-debug.apk created
- ✅ Unit tests pass (ProjectSetupTest)
- ✅ Instrumentation tests pass with accessibility validation (ApplicationContextTest)
- ✅ All acceptance criteria met and verified

**Code Review Fixes Applied - December 24, 2025**

- 🔧 Fixed theme parent from DayNight to Dark.NoActionBar to enforce dark theme default (AC compliance)
- 🔧 Added content description to TextView for TalkBack accessibility
- 🔧 Changed layout background from hardcoded color to theme attribute (?attr/colorBackground) for theme switching support
- 🔧 Applied dimension resources (@dimen/spacing_medium, @dimen/min_touch_target_size) for consistency
- 🔧 Updated MainActivity to demonstrate ViewBinding usage pattern
- 🔧 Improved unit tests to validate actual requirements (not placeholder assertions)
- 🔧 Enhanced instrumentation tests with accessibility validation (touch target size, content descriptions, theme colors)
- 🔧 Created res/font/ directory with .gitkeep and documented font configuration approach in docs/font-configuration.md
- 🔧 Updated File List to include local.properties and .gitignore documentation
- 🔧 Added high-contrast theme activation comment for future implementation (Story 5.3)

**Build Configuration:**
- Gradle 8.4 with Android Gradle Plugin 8.3.0
- Kotlin 1.9.22
- Min SDK 26, Target SDK 34
- Java 17 bytecode compatibility

**Testing:**
- Unit tests created and passing (ProjectSetupTest.kt) - validates project requirements
- Instrumentation tests created and passing (ApplicationContextTest.kt) - validates accessibility compliance
- Build verified: 38 tasks executed, BUILD SUCCESSFUL

### File List

**Project Configuration Files:**
- `settings.gradle.kts` - Project settings with module configuration
- `build.gradle.kts` - Root build configuration with plugin versions
- `gradle.properties` - Gradle JVM args and AndroidX configuration
- `gradlew.bat` / `gradlew` - Gradle wrapper scripts
- `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.4 wrapper config
- `gradle/wrapper/gradle-wrapper.jar` - Gradle wrapper JAR
- `local.properties` - SDK location configuration (not in git, developer-specific)
- `.gitignore` - Git exclusions (includes local.properties, build/, .gradle/)

**App Module Files:**
- `app/build.gradle.kts` - App module build configuration
- `app/proguard-rules.pro` - ProGuard configuration (empty, will be populated in future stories)
- `app/src/main/AndroidManifest.xml` - App manifest with API levels

**Source Code:**
- `app/src/main/kotlin/com/visionfocus/MainActivity.kt` - Main activity with ViewBinding

**Resources:**
- `app/src/main/res/layout/activity_main.xml` - Main activity layout with accessibility annotations
- `app/src/main/res/values/strings.xml` - String resources including content descriptions
- `app/src/main/res/values/colors.xml` - Color palette with dark theme colors
- `app/src/main/res/values/themes.xml` - Material Design 3 theme configuration with dark default
- `app/src/main/res/values/dimens.xml` - Accessibility dimensions (48dp touch targets, 20sp text)
- `app/src/main/res/values-night/themes.xml` - Dark theme enforcement
- `app/src/main/res/values/ic_launcher_background.xml` - Launcher icon background
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Launcher icon foreground
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` - Adaptive icon config
- `app/src/main/res/font/.gitkeep` - Placeholder for future font files (see docs/font-configuration.md)

**Documentation:**
- `docs/font-configuration.md` - Roboto font configuration notes and rationale

**Test Files:**
- `app/src/test/kotlin/com/visionfocus/ProjectSetupTest.kt` - Unit tests validating project requirements
- `app/src/androidTest/kotlin/com/visionfocus/ApplicationContextTest.kt` - Instrumentation tests with accessibility validation

**IDE Configuration:**
- `.vscode/settings.json` - VS Code settings for Android/Kotlin development
