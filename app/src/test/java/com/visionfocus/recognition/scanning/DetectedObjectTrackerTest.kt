package com.visionfocus.recognition.scanning

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DetectedObjectTracker
 * Story 4.4 Task 14: Test duplicate suppression logic
 * 
 * Test Coverage:
 * - isNewObject returns true for unseen objects
 * - isNewObject returns false for already seen objects
 * - Case-insensitive tracking (Chair = chair = CHAIR)
 * - count returns correct number
 * - reset clears all tracked objects
 * - getAllDetectedObjects returns immutable list
 * 
 * @since Story 4.4
 */
class DetectedObjectTrackerTest {
    
    private lateinit var tracker: DetectedObjectTracker
    
    @Before
    fun setup() {
        tracker = DetectedObjectTracker()
    }
    
    /**
     * Test: isNewObject returns true for unseen object
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `isNewObject returns true for unseen object`() {
        // When: Check new object
        val isNew = tracker.isNewObject("chair")
        
        // Then: Returns true
        assertTrue("Unseen object should be new", isNew)
    }
    
    /**
     * Test: isNewObject returns false for already seen object
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `isNewObject returns false for already seen object`() {
        // Given: Object already tracked
        tracker.addObject("chair")
        
        // When: Check same object again
        val isNew = tracker.isNewObject("chair")
        
        // Then: Returns false (duplicate)
        assertFalse("Seen object should not be new", isNew)
    }
    
    /**
     * Test: Case-insensitive tracking
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `isNewObject is case-insensitive`() {
        // Given: Object tracked in lowercase
        tracker.addObject("chair")
        
        // When: Check with different cases
        val isNewUpper = tracker.isNewObject("CHAIR")
        val isNewMixed = tracker.isNewObject("Chair")
        
        // Then: All return false (same object)
        assertFalse("CHAIR should be duplicate of chair", isNewUpper)
        assertFalse("Chair should be duplicate of chair", isNewMixed)
    }
    
    /**
     * Test: count returns correct number of detected objects
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `count returns correct number of detected objects`() {
        // Given: Multiple objects tracked
        tracker.addObject("chair")
        tracker.addObject("table")
        tracker.addObject("bottle")
        
        // When: Get count
        val count = tracker.count()
        
        // Then: Count is 3
        assertEquals("Count should be 3", 3, count)
    }
    
    /**
     * Test: Adding duplicate object doesn't increase count
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `adding duplicate object does not increase count`() {
        // Given: Object already tracked
        tracker.addObject("chair")
        assertEquals(1, tracker.count())
        
        // When: Add duplicate
        tracker.addObject("chair")
        
        // Then: Count stays at 1 (set behavior)
        assertEquals("Duplicate should not increase count", 1, tracker.count())
    }
    
    /**
     * Test: reset clears all detected objects
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `reset clears all detected objects`() {
        // Given: Multiple objects tracked
        tracker.addObject("chair")
        tracker.addObject("table")
        assertEquals(2, tracker.count())
        
        // When: Reset
        tracker.reset()
        
        // Then: Count is 0, object is new again
        assertEquals("Count should be 0 after reset", 0, tracker.count())
        assertTrue("Object should be new after reset", tracker.isNewObject("chair"))
    }
    
    /**
     * Test: getAllDetectedObjects returns immutable list
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `getAllDetectedObjects returns immutable list`() {
        // Given: Multiple objects tracked
        tracker.addObject("chair")
        tracker.addObject("table")
        tracker.addObject("bottle")
        
        // When: Get all objects
        val objects = tracker.getAllDetectedObjects()
        
        // Then: List contains all objects
        assertEquals("List should contain 3 objects", 3, objects.size)
        assertTrue("List should contain chair", objects.contains("chair"))
        assertTrue("List should contain table", objects.contains("table"))
        assertTrue("List should contain bottle", objects.contains("bottle"))
    }
    
    /**
     * Test: Empty tracker state
     * Story 4.4 Task 14.5: Duplicate suppression test
     */
    @Test
    fun `empty tracker returns zero count and empty list`() {
        // When: No objects added
        val count = tracker.count()
        val objects = tracker.getAllDetectedObjects()
        
        // Then: Count is 0, list is empty
        assertEquals("Count should be 0", 0, count)
        assertTrue("List should be empty", objects.isEmpty())
    }
    
    /**
     * Test: Thread-safe concurrent access
     * Story 4.4: Tracker uses synchronized methods
     */
    @Test
    fun `thread-safe concurrent access`() {
        // Given: Multiple threads adding objects
        val threads = List(10) { threadIndex ->
            Thread {
                repeat(10) { iteration ->
                    tracker.addObject("object$threadIndex-$iteration")
                }
            }
        }
        
        // When: Run threads concurrently
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        // Then: All objects tracked correctly (100 unique objects)
        assertEquals("Should track 100 unique objects", 100, tracker.count())
    }
}
