# Manual Test Story 10.1: Beacon Scanner & Management

## Story Description
**As a** visually impaired user  
**I want to** pair Bluetooth Low Energy (BLE) beacons to the app  
**So that** I can associate physical objects (like keys or rooms) with digital tags for later retrieval.

## Prerequisites
1.  Android Device with Bluetooth Low Energy (BLE) support (Android 8.0+).
2.  Bluetooth enabled on the device.
3.  Location permissions granted (for scanning on older Android versions).
4.  At least one BLE Beacon device (e.g., iSearching tag, Tile, or another phone simulating a beacon).

## Test Scenarios

### 1. Navigation to Settings
- [ ] **Step**: Open the application and navigate to the "Settings" menu via voice command "Settings" or the bottom navigation bar.
- [ ] **Expected**: The Settings screen is displayed.
- [ ] **Step**: Locate the "Manage Smart Tags" option.
- [ ] **Expected**: Users can see (or hear via TalkBack) the "Manage Smart Tags" button.

### 2. Scanning for Beacons
- [ ] **Step**: Tap on "Manage Smart Tags" to open the management screen.
- [ ] **Step**: Tap the floating action button (+) to start adding a tag.
- [ ] **Expected**: 
    - A dialog opens.
    - Permission checks run (Bluetooth/Location). If missing, a system prompt appears.
    - The status text reads "Bringing device close to your phone...".
    - A progress indicator shows scanning is active.

### 3. Discovering Devices
- [ ] **Step**: Bring a BLE beacon close to the phone.
- [ ] **Expected**:
    - The device appears in the list.
    - The item shows a name (if available) or MAC address.
    - The signal strength (RSSI) is displayed (e.g., "-55 dBm").

### 4. Naming and Saving a Tag
- [ ] **Step**: Tap on one of the discovered devices in the list.
- [ ] **Expected**: A "Name Your Tag" dialog appears.
- [ ] **Step**: Enter a friendly name (e.g., "Lab Keys") and tap "Save".
- [ ] **Expected**: 
    - The dialog closes.
    - The new tag appears in the main "My Smart Tags" list in the management screen.
    - The name and MAC address match what was saved.

### 5. Persistent Storage
- [ ] **Step**: Close the app completely and reopen it.
- [ ] **Step**: Return to "Manage Smart Tags".
- [ ] **Expected**: The saved tag ("Lab Keys") is still present in the list.

### 6. Deleting a Tag
- [ ] **Step**: Find the "Lab Keys" tag in the list.
- [ ] **Step**: Tap the delete (trash can) icon associated with it.
- [ ] **Expected**: 
    - A confirmation dialog appears: "Are you sure you want to delete Lab Keys?".
    - Tapping "Delete" removes the item from the list.
    - The database is updated (verified by re-opening the screen).

## Accessibility Verification
- [ ] **TalkBack**: Ensure all elements (Scan button, list items, dialog fields) have valid `contentDescription` attributes.
- [ ] **Haptics**: N/A for this story, but standard touch feedback should apply.

## Troubleshooting
- **No devices found**: Ensure the beacon is turned on and not connected to another device.
- **Permission Denied**: Go to Android Settings -> Apps -> VisionFocus -> Permissions and manually grant Location/Nearby Devices.
