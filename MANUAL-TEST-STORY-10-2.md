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

### 2. Feedback at "Far" Range (> 8m)
- [ ] **Step**: Move far away from the beacon (> 8 meters).
- [ ] **Expected**:
    - **Audio**: "Signal weak, search around" (repeated every ~2.5s).
    - **Haptic**: Slow pulse (every 2000ms).

### 3. Feedback at "Medium" Range (5m - 8m)
- [ ] **Step**: Move slightly closer.
- [ ] **Expected**:
    - **Audio**: "Keys is far, but signal detected".
    - **Haptic**: Medium interval pulse (1000ms).

### 4. Feedback at "Close" Range (3m - 5m)
- [ ] **Step**: Continue approaching.
- [ ] **Expected**:
    - **Audio**: "Getting closer".
    - **Haptic**: Faster pulse (500ms).

### 5. Feedback at "Very Close" Range (1m - 3m)
- [ ] **Step**: Get within 2 meters.
- [ ] **Expected**:
    - **Audio**: "Very close".
    - **Haptic**: Rapid buzzing (250ms).

### 6. Arrival (< 1m)
- [ ] **Step**: Place the phone directly next to the beacon.
- [ ] **Expected**:
    - **Audio**: "You have reached Keys".
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
