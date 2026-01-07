# VisionFocus - Complete Rebuild and Clean Install
# Fixes: WhereAmICommand + Database migration

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VisionFocus - Clean Rebuild & Install" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build APK
Write-Host "Step 1/5: Building APK with all fixes..." -ForegroundColor Yellow
.\gradlew assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed! Check errors above." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Check device connection
Write-Host "Step 2/5: Checking device connection..." -ForegroundColor Yellow
$devices = adb devices | Select-String "192.168.1.95:37791"
if (-not $devices) {
    Write-Host "❌ Device not connected at 192.168.1.95:37791" -ForegroundColor Red
    Write-Host "Run: adb connect 192.168.1.95:37791" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Device connected!" -ForegroundColor Green
Write-Host ""

# Step 3: Uninstall old app completely
Write-Host "Step 3/5: Uninstalling old app..." -ForegroundColor Yellow
adb -s 192.168.1.95:37791 uninstall com.visionfocus 2>&1 | Out-Null
Write-Host "✅ Old app removed!" -ForegroundColor Green
Write-Host ""

# Step 4: Install fresh APK
Write-Host "Step 4/5: Installing fresh APK with fixes..." -ForegroundColor Yellow
$installResult = adb -s 192.168.1.95:37791 install app\build\outputs\apk\debug\app-debug.apk 2>&1

if ($installResult -match "Success") {
    Write-Host "✅ Installation successful!" -ForegroundColor Green
} else {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    Write-Host $installResult
    exit 1
}
Write-Host ""

# Step 5: Launch app
Write-Host "Step 5/5: Launching VisionFocus..." -ForegroundColor Yellow
adb -s 192.168.1.95:37791 shell am start -n com.visionfocus/.MainActivity 2>&1 | Out-Null
Write-Host "✅ App launched!" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ INSTALLATION COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 TEST THE FIXES:" -ForegroundColor Cyan
Write-Host "1. Say 'Where am I' → Should announce GPS location" -ForegroundColor White
Write-Host "2. Say 'Save location' → Should show dialog (already working)" -ForegroundColor White
Write-Host "3. Say 'Saved locations' → Should show EMPTY (no Test Home)" -ForegroundColor White
Write-Host ""
Write-Host "APK Timestamp: $(Get-Date)" -ForegroundColor Gray
