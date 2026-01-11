# Manual Test Story 10.2: Proximity Navigation to Beacons

## Story Description
**As a** visually impaired user  
**I want to** initiate navigation to a paired Bluetooth beacon using voice commands  
**So that** I can locate specific items or locations (like "Keys" or "Bathroom") using audio and haptic feedback.

## Prerequisites
1.  **Story 10.1 Completed**: At least one beacon must be paired and named (e.g., "Keys").
2.  **Voice Input**: The device microphone must be working.
3.  **Permissions**: Bluetooth (Nearby Devices) permission linked to the app.

## Test Scenarios

### 1. Voice Command Activation
- [ ] **Step**: Activate the voice assistant (e.g., long press or wake word).
- [ ] **Step**: Say one of the following variations:
    - "Find Keys"
    - "Locate Keys"
    - "Track Keys"
    - "Where is Keys"
- [ ] **Expected**:
    - The app announces "Searching for Keys beacon".
    - The `ProximityNavigationService` starts (verified by logs or immediate feedback).

### 2. Feedback at "Far" Range (> 6m)
- [ ] **Step**: Move far away from the beacon (> 6 meters).
- [ ] **Expected**:
    - **Audio**: "Signal found, so far".
    - **Haptic**: Slow pulse (every 2000ms).

### 3. Feedback at "Close" Range (4m - 6m)
- [ ] **Step**: Move closer (within 6 meters).
- [ ] **Expected**:
    - **Audio**: "Closer".
    - **Haptic**: Medium interval pulse (500ms).

### 4. Feedback at "Very Closer" Range (2m - 4m)
- [ ] **Step**: Continue approaching (within 4 meters).
- [ ] **Expected**:
    - **Audio**: "Very closer".
    - **Haptic**: Faster pulse (250ms).

### 5. Arrival (< 2m)
- [ ] **Step**: Place the phone within 2 meters of the beacon.
- [ ] **Expected**:
    - **Audio**: "Reached".
    - **Haptic**: Distinct "Arrival Pattern" (triple pulse).
    - **Action**: Navigation stops automatically.

### 7. Voice Fallback (Integration Test)
- [ ] **Step**: Say "Navigate to Keys".
- [ ] **Step**: Ensure no *Saved Location* exists with the name "Keys" (if checking conflict resolution).
- [ ] **Expected**:
    - The app checks for a GPS location named "Keys".
    - Finding none, it falls back to the Beacon repository.
    - It finds the beacon "Keys" and announces "Found beacon Keys. Starting proximity tracking."

## Accessibility Verification
- [ ] **Audio Feedback**: Announcements should be clear and distinct from navigation instructions (e.g., turn-by-turn).
- [ ] **Haptics**: Vibration patterns must be distinguishable by duration/intensity.

## Troubleshooting
- **Interference**: Large metal objects or human bodies block BLE signals. RSSI may fluctuate. The app uses a Kalman filter to smooth this, but some jitter is normal.
