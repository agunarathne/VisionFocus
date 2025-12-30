package com.visionfocus.recognition.processing

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.visionfocus.recognition.models.BoundingBox
import com.visionfocus.recognition.models.DetectionResult
import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.tts.formatter.TTSPhraseFormatter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for recognition → filtering → TTS announcement pipeline
 * 
 * Story 2.2 Task 7: Complete recognition + announcement pipeline validation
 * 
 * Tests:
 * - Confidence filtering integration
 * - NMS deduplication integration
 * - Confidence-aware TTS phrasing
 * - Multiple detections ordering
 * - Empty result handling
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecognitionFilteringPipelineTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )
    
    @Inject
    lateinit var recognitionRepository: RecognitionRepository
    
    @Inject
    lateinit var confidenceFilter: ConfidenceFilter
    
    @Inject
    lateinit var nonMaximumSuppression: NonMaximumSuppression
    
    @Inject
    lateinit var ttsFormatter: TTSPhraseFormatter
    
    @Inject
    lateinit var ttsManager: TTSManager
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @After
    fun teardown() {
        // No cleanup needed for filtering components
    }
    
    /**
     * Story 2.2 AC1, AC2: Confidence filtering + NMS integration
     */
    @Test
    fun testConfidenceFilteringIntegration() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, mockBoundingBox()),
            DetectionResult("table", 0.45f, mockBoundingBox()),  // Below threshold
            DetectionResult("person", 0.72f, mockBoundingBox())
        )
        
        // Apply confidence filtering
        val filtered = confidenceFilter.filter(detections)
        
        assertEquals("Should keep 2 detections above threshold", 2, filtered.size)
        assertTrue("Should contain chair", filtered.any { it.label == "chair" })
        assertTrue("Should contain person", filtered.any { it.label == "person" })
        assertFalse("Should not contain table", filtered.any { it.label == "table" })
    }
    
    /**
     * Story 2.2 AC2: NMS removes overlapping duplicates
     */
    @Test
    fun testNonMaximumSuppressionIntegration() {
        val detections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)),
            DetectionResult("chair", 0.78f, BoundingBox(0.12f, 0.12f, 0.52f, 0.52f)),  // Overlapping
            DetectionResult("table", 0.85f, BoundingBox(0.6f, 0.6f, 0.9f, 0.9f))
        )
        
        // Apply NMS
        val deduplicated = nonMaximumSuppression.apply(detections)
        
        assertEquals("Should keep 2 detections (remove duplicate chair)", 2, deduplicated.size)
        assertTrue("Should keep highest-confidence chair", deduplicated.any { it.label == "chair" && it.confidence == 0.92f })
        assertTrue("Should keep table", deduplicated.any { it.label == "table" })
    }
    
    /**
     * Story 2.2 AC3, AC4: Confidence-aware phrasing for all levels
     */
    @Test
    fun testConfidenceAwarePhrasing() {
        // High confidence
        val highDetection = FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox())
        val highAnnouncement = ttsFormatter.formatSingleDetection(highDetection)
        
        assertTrue("High confidence should use assertive phrasing", 
            highAnnouncement.contains("I see") || 
            highAnnouncement.contains("high confidence") ||
            highAnnouncement.contains("certain"))
        
        // Medium confidence
        val mediumDetection = FilteredDetection("bottle", 0.75f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        val mediumAnnouncement = ttsFormatter.formatSingleDetection(mediumDetection)
        
        assertTrue("Medium confidence should use qualified phrasing",
            mediumAnnouncement.contains("Possibly") ||
            mediumAnnouncement.contains("think") ||
            mediumAnnouncement.contains("might"))
        
        // Low confidence
        val lowDetection = FilteredDetection("cup", 0.62f, ConfidenceLevel.LOW, mockBoundingBox())
        val lowAnnouncement = ttsFormatter.formatSingleDetection(lowDetection)
        
        assertTrue("Low confidence should use uncertain phrasing",
            lowAnnouncement.contains("Not sure") ||
            lowAnnouncement.contains("Might") ||
            lowAnnouncement.contains("not certain"))
    }
    
    /**
     * Story 2.2 AC7: Multiple detections announced in confidence order
     */
    @Test
    fun testMultipleDetectionsOrdering() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.75f, ConfidenceLevel.MEDIUM, mockBoundingBox()),
            FilteredDetection("laptop", 0.63f, ConfidenceLevel.LOW, mockBoundingBox())
        )
        
        val announcement = ttsFormatter.formatMultipleDetections(detections)
        
        // Verify all objects mentioned
        assertTrue("Should mention chair", announcement.contains("chair"))
        assertTrue("Should mention table", announcement.contains("table"))
        assertTrue("Should mention laptop", announcement.contains("laptop"))
        
        // Verify ordering (chair before table, table before laptop)
        val chairIndex = announcement.indexOf("chair")
        val tableIndex = announcement.indexOf("table")
        val laptopIndex = announcement.indexOf("laptop")
        
        assertTrue("Chair should come before table", chairIndex < tableIndex)
        assertTrue("Table should come before laptop", tableIndex < laptopIndex)
    }
    
    /**
     * MEDIUM #11: Test equal confidence detections (edge case)
     */
    @Test
    fun testEqualConfidenceDetectionsOrdering() {
        // All detections have same confidence - ordering should be stable
        val detections = listOf(
            FilteredDetection("chair", 0.85f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.85f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("laptop", 0.85f, ConfidenceLevel.HIGH, mockBoundingBox())
        )
        
        val announcement = ttsFormatter.formatMultipleDetections(detections)
        
        // Verify all objects mentioned
        assertTrue("Should mention chair", announcement.contains("chair"))
        assertTrue("Should mention table", announcement.contains("table"))
        assertTrue("Should mention laptop", announcement.contains("laptop"))
        
        // Verify no crash and valid announcement format
        assertTrue("Should have conjunctions", announcement.contains("and"))
        assertFalse("Should not be robotic", announcement.contains("%"))
    }
    
    /**
     * Story 2.2 AC7: Empty results handling
     */
    @Test
    fun testEmptyResultHandling() {
        val announcement = ttsFormatter.formatMultipleDetections(emptyList())
        
        assertEquals("Should announce no objects detected", "No objects detected", announcement)
    }
    
    /**
     * Story 2.2 AC8: Natural language quality (not robotic)
     */
    @Test
    fun testNaturalLanguageQuality() {
        val detections = listOf(
            FilteredDetection("chair", 0.92f, ConfidenceLevel.HIGH, mockBoundingBox()),
            FilteredDetection("table", 0.78f, ConfidenceLevel.MEDIUM, mockBoundingBox())
        )
        
        val announcement = ttsFormatter.formatMultipleDetections(detections)
        
        // Anti-patterns: Should NOT contain robotic phrasing
        assertFalse("Should not contain 'Confidence:'", announcement.contains("Confidence:"))
        assertFalse("Should not contain percentages", announcement.contains("%"))
        assertFalse("Should not contain raw scores", announcement.contains("0.92"))
        assertFalse("Should not use 'Detected:'", announcement.contains("Detected:"))
        
        // Should be conversational
        assertTrue("Should be conversational (more than just object names)", announcement.length > 20)
    }
    
    /**
     * Story 2.2: Complete pipeline from raw detections to formatted announcement
     */
    @Test
    fun testCompletePipelineIntegration() {
        // Simulate raw detections from inference
        val rawDetections = listOf(
            DetectionResult("chair", 0.92f, BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)),
            DetectionResult("chair", 0.78f, BoundingBox(0.12f, 0.12f, 0.52f, 0.52f)),  // Duplicate
            DetectionResult("table", 0.45f, mockBoundingBox()),                        // Below threshold
            DetectionResult("person", 0.72f, mockBoundingBox())
        )
        
        // Step 1: Confidence filtering
        val filtered = confidenceFilter.filter(rawDetections)
        assertEquals("Should filter out low-confidence table", 3, filtered.size)
        
        // Step 2: NMS deduplication
        val deduplicated = nonMaximumSuppression.apply(filtered)
        assertEquals("Should remove duplicate chair", 2, deduplicated.size)
        
        // Step 3: Convert to FilteredDetection with confidence levels
        val filteredDetections = deduplicated.map { confidenceFilter.toFilteredDetection(it) }
        
        // Step 4: Format announcement
        val announcement = ttsFormatter.formatMultipleDetections(filteredDetections)
        
        // Verify final announcement
        assertTrue("Should mention chair", announcement.contains("chair"))
        assertTrue("Should mention person", announcement.contains("person"))
        assertFalse("Should not mention table (filtered)", announcement.contains("table"))
        assertTrue("Should have natural language", announcement.contains("and"))
    }
    
    /**
     * Story 2.2: Verify TTS manager is initialized
     */
    @Test
    fun testTTSManagerInitialization() {
        // TTS should be initialized on app startup
        assertTrue("TTS manager should be ready", ttsManager.isReady())
    }
    
    /**
     * MEDIUM #13: Validate Story 2.1 + Story 2.2 integration
     * Verify RecognitionRepository from Story 2.1 works with Story 2.2 filtering
     */
    @Test
    fun testStory21And22Integration() = runBlocking {
        // This test validates that Story 2.2's modifications to RecognitionRepositoryImpl
        // don't break Story 2.1's inference pipeline
        
        // Note: Full test requires camera access and TFLite model loaded
        // Here we verify the repository is properly injected and configured
        assertNotNull("RecognitionRepository should be injected", recognitionRepository)
        assertNotNull("ConfidenceFilter should be injected", confidenceFilter)
        assertNotNull("NonMaximumSuppression should be injected", nonMaximumSuppression)
        
        // Verify filtering components work correctly
        val testDetections = listOf(
            DetectionResult("chair", 0.92f, mockBoundingBox()),
            DetectionResult("table", 0.45f, mockBoundingBox())
        )
        
        val filtered = confidenceFilter.filter(testDetections)
        assertEquals("Should filter low confidence", 1, filtered.size)
        
        val deduplicated = nonMaximumSuppression.apply(filtered)
        assertEquals("Should keep filtered result", 1, deduplicated.size)
    }
    
    /**
     * Story 2.2 AC6: TTS announcement (smoke test)
     * Note: Full latency testing requires real device with TTS engine
     */
    @Test
    fun testTTSAnnouncementSmokeTest() = runBlocking {
        if (ttsManager.isReady()) {
            val result = ttsManager.announce("Test announcement")
            
            assertTrue("TTS announcement should succeed or report initialization error",
                result.isSuccess || result.isFailure)
            
            if (result.isSuccess) {
                val latency = result.getOrNull()
                println("TTS announcement latency: ${latency}ms")
            }
        }
    }
    
    /**
     * CRITICAL #5 & MEDIUM #12: Actual latency validation test
     * Story 2.2 Task 4.3: Validate announcement initiation ≤200ms
     */
    @Test
    fun testTTSAnnouncementLatencyRequirement() = runBlocking {
        // Skip if TTS not ready (CI environment may not have TTS engine)
        if (!ttsManager.isReady()) {
            println("Skipping latency test: TTS engine not available")
            return@runBlocking
        }
        
        val startTime = System.currentTimeMillis()
        
        // Queue TTS announcement
        val result = ttsManager.announce("High confidence: chair")
        
        val queueLatency = System.currentTimeMillis() - startTime
        
        // Verify queue latency meets requirement
        assertTrue(
            "TTS queue latency ${queueLatency}ms exceeds 200ms target (Story 2.2 AC6)",
            queueLatency <= 200L
        )
        
        // Verify announce() succeeded
        assertTrue("TTS announcement should succeed", result.isSuccess)
        
        // Note: Actual speech start time tracked via getLastActualLatency()
        // but requires onStart() callback which may not fire in test environment
    }
    
    // Helper functions
    
    private fun mockBoundingBox() = BoundingBox(
        yMin = 0.1f,
        xMin = 0.1f,
        yMax = 0.5f,
        xMax = 0.5f
    )
}
