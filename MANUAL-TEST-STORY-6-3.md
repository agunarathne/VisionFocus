# Manual Testing Guide: Story 6.3 - Turn-by-Turn Voice Guidance

## Test Setup
- **Device**: Samsung Galaxy (API 34)
- **Location**: Enable GPS, mock locations OFF
- **Build**: Fresh APK with Story 6.3 implementation
- **Prerequisites**: Stories 6.1, 6.2, 3.1, 2.3, 1.3 completed

## Pre-Test Checklist
- [ ] Clear app data: Settings → Apps → VisionFocus → Storage → Clear Data
- [ ] Enable Location: Settings → Location → On
- [ ] Enable TalkBack: Settings → Accessibility → TalkBack → On
- [ ] Charge device to >80% (GPS drains battery)
- [ ] Connect to stable network (for Directions API)
- [ ] Choose test route with multiple turns (~500-1000m)

## Test Scenarios

### AC #1: 5-7 Second Advance Warnings
**Objective**: Verify advance warnings announce 5-7 seconds before turn

**Steps**:
1. Launch VisionFocus, enable TalkBack
2. Say "navigate to [destination 500m away with 2+ turns]"
3. Wait for route download, tap "Start Navigation"
4. Walk toward first turn
5. Listen for advance warning: "In 70 meters, turn right onto Main Street"
6. Start timing when advance warning announces
7. Continue walking at normal pace
8. Note when you reach the actual turn point

**Expected**:
- Advance warning should announce 5-7 seconds before reaching turn
- Assuming walking speed ~1.4 m/s, 70 meters ≈ 50 seconds
- Warning should be clear and natural language

**Pass Criteria**:
- [ ] Advance warning heard 5-7 seconds before turn
- [ ] Distance calculation accurate (±10 meters)
- [ ] Natural language announcement (not "Turn type: RIGHT")

---

### AC #2: Advance Warning Content
**Objective**: Verify advance warning includes distance and maneuver

**Steps**:
1. Continue from AC #1 test
2. Listen to exact wording of advance warning
3. Record announcement text

**Expected Announcement Format**:
"In 70 meters, turn right onto Main Street"
OR
"In 70 meters, keep left onto Highway 101"

**Pass Criteria**:
- [ ] Includes distance in meters ("In X meters...")
- [ ] Includes maneuver ("turn right", "turn left", "keep straight")
- [ ] Includes street name if available
- [ ] Uses natural language (not robotic)

---

### AC #3: Immediate Turn Announcements
**Objective**: Verify immediate warning at turn point

**Steps**:
1. After hearing advance warning, continue walking
2. Listen for immediate warning when reaching turn
3. Time delay between advance and immediate warnings

**Expected Immediate Announcement**:
"Turn right now onto Main Street"
OR
"Keep left now onto Highway 101"

**Pass Criteria**:
- [ ] Immediate warning announces within 1-2 seconds of turn point
- [ ] Uses "now" to indicate urgency
- [ ] Does not duplicate advance warning (should only announce once per turn)
- [ ] Clear and concise (no unnecessary words)

---

### AC #4: Natural Language and Cardinal Directions
**Objective**: Verify natural language instructions with cardinal directions

**Steps**:
1. Test multiple turn types:
   - Right turn
   - Left turn
   - Slight right
   - Sharp left
   - U-turn
   - Merge
2. Note if cardinal directions provided when helpful

**Expected Examples**:
- "In 70 meters, turn right heading north onto Oak Avenue"
- "Turn left now, heading west"
- "Make a slight right onto Highway 1"
- "Make a U-turn heading south"

**Pass Criteria**:
- [ ] All 16 maneuver types have natural descriptions
- [ ] Cardinal directions included when helpful
- [ ] No technical jargon or codes
- [ ] Instructions match Google Maps quality

---

### AC #5: Straight Checkpoint Announcements
**Objective**: Verify checkpoints on long straight sections

**Steps**:
1. Navigate route with straight section >200m
2. Listen for checkpoint announcements
3. Measure distance between checkpoints

**Expected Announcement**:
"Continue straight for 300 meters"
OR
"Keep going straight on Main Street"

**Pass Criteria**:
- [ ] Checkpoints announce every ~200 meters on straight sections
- [ ] No checkpoints on short sections (<200m)
- [ ] Checkpoints provide distance remaining
- [ ] Reassures user they're on correct path

---

### AC #6: Audio Priority (Interrupts Recognition)
**Objective**: Verify navigation interrupts object recognition

**Steps**:
1. Start navigation with TalkBack enabled
2. Navigate toward first turn
3. While waiting for turn warning, trigger object recognition:
   - Say "what do I see" OR point phone at object
4. Listen if recognition announcement interrupted by turn warning

**Expected Behavior**:
- Navigation announcement should INTERRUPT recognition
- Recognition should stop speaking immediately
- Navigation has audio priority

**Test TTSManager Priority**:
- Check TTSManager uses `QUEUE_FLUSH` for navigation
- Check TTSManager uses `QUEUE_ADD` for recognition

**Pass Criteria**:
- [ ] Navigation interrupts recognition mid-sentence
- [ ] No audio overlap between navigation and recognition
- [ ] Navigation announcements never delayed
- [ ] Recognition resumes after navigation announcement (optional)

---

### AC #7: 10% Volume Increase for Safety
**Objective**: Verify navigation announcements 10% louder than recognition

**Steps**:
1. Start navigation
2. Trigger recognition announcement: "what do I see"
3. Wait for turn warning announcement
4. Compare perceived volume levels

**Expected**:
- Navigation should be noticeably louder than recognition
- ~10% volume increase (subjective, but perceptible)

**Technical Check**:
- Open NavigationAnnouncementManager.kt
- Verify `announceWithPriority()` increases volume by 0.1

**Pass Criteria**:
- [ ] Navigation announcements perceptibly louder
- [ ] Volume increase not jarring or uncomfortable
- [ ] Louder volume improves safety in noisy environments

---

### AC #8: Complex Intersections (Roundabouts, Multi-Exit)
**Objective**: Verify clear instructions for complex intersections

**Steps**:
1. Navigate route with roundabout
2. Listen to roundabout instructions:
   - Entry: "Enter the roundabout"
   - Exit: "Take the second exit onto Main Street"
3. Test multi-lane intersections with specific lane guidance

**Expected Roundabout Announcements**:
- "In 70 meters, enter the roundabout"
- "Take the second exit onto Main Street"
- "Exit the roundabout onto Oak Avenue"

**Pass Criteria**:
- [ ] Roundabout entry announced
- [ ] Exit number specified ("second exit", "third exit")
- [ ] Street name included
- [ ] Multi-lane guidance clear (when available)

---

### AC #9: Arrival Announcement
**Objective**: Verify arrival announcement when destination reached

**Steps**:
1. Navigate complete route to destination
2. Walk until within 10 meters of destination
3. Listen for arrival announcement

**Expected Announcement**:
"You have arrived at your destination"
OR
"Arriving at [destination name]"

**Expected Behavior**:
- Navigation service stops
- Foreground notification dismissed
- GPS tracking stops
- Return to destination input screen

**Pass Criteria**:
- [ ] Arrival announces within 10 meters of destination
- [ ] Clear arrival message
- [ ] Navigation service stops automatically
- [ ] Notification dismissed
- [ ] Battery usage reduced (GPS off)

---

## Accessibility Testing

### TalkBack Focus Order
**Steps**:
1. Enable TalkBack
2. Launch navigation active screen
3. Swipe right through all elements
4. Note focus order

**Expected Focus Order**:
1. Current instruction TextView
2. Distance/time remaining TextView
3. Cancel Navigation button

**Pass Criteria**:
- [ ] Focus order logical (top to bottom)
- [ ] Cancel button announces: "Cancel navigation, button"
- [ ] Progress updates announced via live region

### Screen Lock Compatibility
**Steps**:
1. Start navigation
2. Lock screen (power button)
3. Listen for turn warnings through locked screen
4. Unlock and verify UI still responsive

**Pass Criteria**:
- [ ] Turn warnings announce even when screen locked
- [ ] Foreground service keeps GPS active
- [ ] Notification visible on lock screen
- [ ] Cancel action works from notification

---

## Error Scenarios

### Test 1: GPS Signal Loss
**Steps**:
1. Start navigation
2. Move indoors (lose GPS signal)
3. Note behavior

**Expected**:
- Announcement: "GPS signal lost, searching..."
- No crash or silent failure
- Resume when GPS signal restored

**Pass Criteria**:
- [ ] GPS loss announced
- [ ] Navigation pauses gracefully
- [ ] Resumes when signal restored

### Test 2: Cancel Navigation
**Steps**:
1. Start navigation
2. Tap "Cancel Navigation" button
3. Verify cleanup

**Expected**:
- Confirmation dialog: "Cancel navigation?"
- Service stops
- GPS tracking stops
- Return to destination input screen

**Pass Criteria**:
- [ ] Confirmation dialog shown
- [ ] Service cleanup complete
- [ ] No lingering notifications
- [ ] Can start new navigation

### Test 3: Off-Route Detection
**Steps**:
1. Start navigation
2. Deliberately walk off-route (wrong turn)
3. Note behavior

**Expected**:
- Announcement: "Recalculating route..."
- Request new route from current location
- Resume navigation with updated route

**Pass Criteria**:
- [ ] Off-route detected within 50 meters
- [ ] Recalculation announced
- [ ] New route downloaded
- [ ] Navigation continues smoothly

---

## Performance Testing

### Battery Usage
**Steps**:
1. Note battery % before navigation
2. Navigate for 10 minutes
3. Note battery % after navigation

**Expected**:
- ~5-10% battery drain for 10-minute navigation
- GPS is battery-intensive but optimized

**Pass Criteria**:
- [ ] Battery drain reasonable (<15% per 10 min)
- [ ] No excessive CPU usage
- [ ] Foreground service properly configured

### GPS Update Rate
**Steps**:
1. Monitor logcat during navigation: `adb logcat | Select-String "Location update"`
2. Count location updates per second

**Expected**:
- 1Hz GPS update rate (1 update per second)
- Consistent updates, no skipped seconds

**Pass Criteria**:
- [ ] GPS updates at ~1Hz
- [ ] No large gaps (>2 seconds without update)
- [ ] Location accuracy <20 meters

---

## Test Results Template

| Test Case | Pass/Fail | Notes |
|-----------|-----------|-------|
| AC #1: Advance Warning 5-7s | ⬜ | |
| AC #2: Advance Content | ⬜ | |
| AC #3: Immediate Turn | ⬜ | |
| AC #4: Natural Language | ⬜ | |
| AC #5: Straight Checkpoints | ⬜ | |
| AC #6: Audio Priority | ⬜ | |
| AC #7: Volume Increase | ⬜ | |
| AC #8: Complex Intersections | ⬜ | |
| AC #9: Arrival | ⬜ | |
| TalkBack Focus Order | ⬜ | |
| Screen Lock | ⬜ | |
| GPS Signal Loss | ⬜ | |
| Cancel Navigation | ⬜ | |
| Off-Route Detection | ⬜ | |
| Battery Usage | ⬜ | |
| GPS Update Rate | ⬜ | |

---

## Known Issues to Watch For

1. **GPS Drift**: Indoor GPS can drift 20-50 meters, may trigger false turn warnings
2. **Walking Speed Variance**: Slow walkers may get warnings too early, fast walkers too late
3. **Street Name Parsing**: Some Google Maps instructions lack street names
4. **Roundabout Exit Numbers**: Google API may not always provide exit numbers
5. **Audio Interruption**: Android audio focus may not always work perfectly
6. **Foreground Service Restrictions**: Android 14+ has stricter foreground service rules

---

## Debugging Commands

```powershell
# Monitor GPS location updates
adb logcat | Select-String "Location update"

# Monitor turn warning triggers
adb logcat | Select-String "TurnWarning"

# Monitor navigation service lifecycle
adb logcat | Select-String "NavigationService"

# Check foreground service status
adb shell dumpsys activity services com.visionfocus
```

---

## Test Completion Checklist

- [ ] All 9 acceptance criteria tested
- [ ] TalkBack accessibility verified
- [ ] Screen lock compatibility confirmed
- [ ] Error scenarios tested (GPS loss, cancel, off-route)
- [ ] Performance acceptable (battery, GPS rate)
- [ ] No crashes or ANRs observed
- [ ] Test results documented
- [ ] Issues logged in GitHub (if any)
- [ ] Story marked as "Done" in sprint-status.yaml

**Tester Name**: ________________  
**Test Date**: ________________  
**Device**: Samsung Galaxy (API 34)  
**Build Version**: ________________
