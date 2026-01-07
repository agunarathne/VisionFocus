# VisionFocus - Rebuild and Install Fresh APK
# Builds latest code and installs to currently connected device

param(
    [string]$PackageName = "com.visionfocus",
    [string]$ActivityName = ".MainActivity",
    [switch]$SkipBuild = $false,
    [switch]$SkipLaunch = $false
)

# Ensure ADB is in PATH
$env:PATH += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   VisionFocus - Fresh APK Install" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Detect connected device
Write-Host "📱 Detecting connected device..." -ForegroundColor Cyan
$deviceList = adb devices | Select-String -Pattern "device$" | Select-String -NotMatch "List of devices"

if (-not $deviceList) {
    Write-Host "❌ No device connected!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Connect your device first:" -ForegroundColor Yellow
    Write-Host "  USB: Enable USB debugging and connect cable" -ForegroundColor Yellow
    Write-Host "  Wireless: Run .\launch-wireless.ps1" -ForegroundColor Yellow
    exit 1
}

$deviceId = ($deviceList[0] -split '\s+')[0]
Write-Host "✅ Found device: $deviceId" -ForegroundColor Green
Write-Host ""

# Step 2: Build APK (unless skipped)
if (-not $SkipBuild) {
    Write-Host "🔨 Building APK with latest code..." -ForegroundColor Cyan
    Write-Host "   (This may take 30-60 seconds...)" -ForegroundColor Gray
    Write-Host ""
    
    $buildOutput = .\gradlew assembleDebug 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Build failed! Error details:" -ForegroundColor Red
        Write-Host $buildOutput -ForegroundColor Red
        exit 1
    }
    
    # Check if APK exists
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path $apkPath)) {
        Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
        exit 1
    }
    
    # Get APK timestamp
    $apkInfo = Get-Item $apkPath
    Write-Host "✅ Build successful!" -ForegroundColor Green
    Write-Host "   APK: $($apkInfo.Name) ($([math]::Round($apkInfo.Length / 1MB, 2)) MB)" -ForegroundColor Gray
    Write-Host "   Built: $($apkInfo.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "⏭️  Skipping build (using existing APK)..." -ForegroundColor Yellow
    Write-Host ""
}

# Step 3: Uninstall old app
Write-Host "🗑️  Uninstalling old app (clears all data)..." -ForegroundColor Cyan
$uninstallResult = adb -s $deviceId uninstall $PackageName 2>&1

if ($uninstallResult -match "Success") {
    Write-Host "✅ Old app removed!" -ForegroundColor Green
} elseif ($uninstallResult -match "not installed") {
    Write-Host "ℹ️  App not previously installed (fresh install)" -ForegroundColor Yellow
} else {
    Write-Host "⚠️  Uninstall warning: $uninstallResult" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Install fresh APK
Write-Host "📦 Installing fresh APK to device..." -ForegroundColor Cyan
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
$installResult = adb -s $deviceId install $apkPath 2>&1

if ($installResult -match "Success") {
    Write-Host "✅ Installation successful!" -ForegroundColor Green
} else {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    Write-Host $installResult -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 5: Launch app (unless skipped)
if (-not $SkipLaunch) {
    Write-Host "🚀 Launching VisionFocus..." -ForegroundColor Cyan
    $launchResult = adb -s $deviceId shell am start -n "$PackageName/$ActivityName" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ App launched successfully!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Launch warning: $launchResult" -ForegroundColor Yellow
    }
    Write-Host ""
} else {
    Write-Host "⏭️  Skipping launch..." -ForegroundColor Yellow
    Write-Host ""
}

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ INSTALLATION COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Device: $deviceId" -ForegroundColor Gray
Write-Host "Package: $PackageName" -ForegroundColor Gray
Write-Host "Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 Tip: Use -SkipBuild to skip rebuilding" -ForegroundColor Gray
Write-Host "   Example: .\Install-Fresh-APK.ps1 -SkipBuild" -ForegroundColor Gray
Write-Host ""
