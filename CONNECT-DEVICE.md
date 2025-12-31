# Quick Device Connection Guide

## Option 1: USB Cable (Fastest)
1. Connect phone to PC via USB cable
2. Enable USB debugging on phone
3. Run: `adb devices`
4. Approve debugging prompt on phone

## Option 2: Wireless Debugging (Your Setup)

### On Your Phone (Samsung SM-A127F):
1. **Settings** → **Developer Options**
2. **Enable "Wireless debugging"**
3. **Tap "Wireless debugging"**
4. Note the **IP address and port** shown (e.g., 192.168.8.103:5555)

### On PC:
```powershell
adb connect 192.168.8.103:5555
```

### Quick Check:
```powershell
adb devices
```

Should show:
```
192.168.8.103:5555      device
```

---

## If Connection Fails

**1. Check both devices are on same WiFi network**

**2. Restart ADB server:**
```powershell
adb kill-server
adb start-server
adb connect 192.168.8.103:5555
```

**3. Check firewall (allow port 5555)**

**4. Use USB cable method instead (most reliable)**

---

## Once Connected - Run This:

```powershell
# Navigate to project
cd "e:\MSC_Allan\Final Project\Project_Dissertation\VisionFocus"

# Install app
.\gradlew.bat installDebug

# Launch app
adb shell am start -n com.visionfocus/.MainActivity

# Monitor logs (keep this running in separate terminal)
adb logcat -c
adb logcat -s HapticFeedbackManager:D SettingsFragment:D RecognitionViewModel:D VisionFocus:D
```
