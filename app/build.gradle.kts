plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")  // Story 6.1: Safe Args for navigation
}

import java.util.Properties
import java.io.FileInputStream

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
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        
        // Story 6.2: Load API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        
        // CODE REVIEW FIX (Issue #1): Validate API key at build time
        if (mapsApiKey.isEmpty() || mapsApiKey == "YOUR_API_KEY_HERE") {
            throw GradleException(
                "\n╔═══════════════════════════════════════════════════════════╗\n" +
                "║  ERROR: MAPS_API_KEY not configured                      ║\n" +
                "╚═══════════════════════════════════════════════════════════╝\n" +
                "\nPlease add your Google Maps API key to local.properties:\n" +
                "  MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE\n\n" +
                "Get API key: https://console.cloud.google.com/google/maps-apis/credentials\n" +
                "Enable 'Directions API' in Google Cloud Console\n"
            )
        }
        
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        
        // Story 6.2: Add Maps API key to AndroidManifest.xml
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        
        // Story 7.4: Load Mapbox access token from local.properties
        val mapboxToken = localProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""
        
        // Validate Mapbox token at build time
        if (mapboxToken.isEmpty() || mapboxToken == "YOUR_MAPBOX_TOKEN_HERE") {
            throw GradleException(
                "\n╔═══════════════════════════════════════════════════════════╗\n" +
                "║  ERROR: MAPBOX_ACCESS_TOKEN not configured               ║\n" +
                "╚═══════════════════════════════════════════════════════════╝\n" +
                "\nPlease add your Mapbox access token to local.properties:\n" +
                "  MAPBOX_ACCESS_TOKEN=pk.YOUR_ACTUAL_TOKEN_HERE\n\n" +
                "Get token: https://account.mapbox.com/access-tokens/\n" +
                "Token must have downloads:read scope for offline maps\n"
            )
        }
        
        buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"$mapboxToken\"")
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
        buildConfig = true  // Enable BuildConfig generation (Code Review Fix)
    }
    
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "schemas")
        }
    }
    
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            
            // Enable ByteBuddy experimental mode for Java 23 support
            all {
                it.jvmArgs("-Dnet.bytebuddy.experimental=true")
            }
        }
    }
}

// Room schema export configuration
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.11.0")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // Architecture Components (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")  // For Fragment.viewModels()
    
    // Navigation Component (Story 6.1)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
    
    // Google Maps Services (Story 6.2)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Mapbox Maps SDK for offline navigation - Story 7.4
    implementation("com.mapbox.maps:android:10.16.0")
    implementation("com.mapbox.navigation:android:2.17.0")
    implementation("com.mapbox.navigation:ui:2.17.0")
    
    // WorkManager for periodic expiration checks - Story 7.4
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    androidTestImplementation("androidx.work:work-testing:2.9.0")
    
    // Retrofit & Gson (Story 6.2)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // OkHttp Logging (Story 6.2)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // MockWebServer for testing (Story 6.2)
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // DataStore Preferences (Story 1.3)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    
    // Room Database (Story 1.4)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // SQLCipher for database encryption (Story 4.2)
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    
    // Coroutines for DataStore
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")  // For Google Play Services await()
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Timber Logging (Story 4.2)
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // TensorFlow Lite - Story 2.1 (with namespace fix for Story 6.2)
    implementation("org.tensorflow:tensorflow-lite:2.14.0") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    }
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite-support-api")
    }
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    
    // CameraX - Story 2.1
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("javax.inject:javax.inject:1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("com.google.dagger:hilt-android-testing:2.50")
    kaptTest("com.google.dagger:hilt-compiler:2.50")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    
    // AndroidX Test Framework (Story 1.5)
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    
    // Espresso Accessibility (Story 1.5)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.5.1")
    
    // Accessibility Test Framework (Story 1.5)
    androidTestImplementation("com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:4.0.0")
    
    // Test Orchestrator (Story 1.5)
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.50")
}

// Allow references to generated code (required for Hilt)
kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
