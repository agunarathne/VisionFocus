# VisionFocus Wireless Launch Script
# Connects to Samsung SM-A127F via wireless debugging and launches the app

param(
    [string]$DeviceIP = "192.168.8.101:36995",
    [string]$PackageName = "com.visionfocus",
    [string]$ActivityName = ".MainActivity"
)

# Ensure ADB is in PATH
$env:PATH += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"

Write-Host "🔌 Connecting to device at $DeviceIP..." -ForegroundColor Cyan

# Connect to wireless device
$connectResult = adb connect $DeviceIP 2>&1
Write-Host $connectResult

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to connect. Make sure:" -ForegroundColor Red
    Write-Host "   1. Wireless debugging is enabled on your phone" -ForegroundColor Yellow
    Write-Host "   2. Phone and PC are on the same Wi-Fi network" -ForegroundColor Yellow
    Write-Host "   3. IP address is correct: $DeviceIP" -ForegroundColor Yellow
    exit 1
}

# Verify connection
Write-Host "`n📱 Checking connected devices..." -ForegroundColor Cyan
adb devices

# Wait a moment for connection to stabilize
Start-Sleep -Milliseconds 500

# Launch the app
Write-Host "`n🚀 Launching VisionFocus..." -ForegroundColor Green
$launchResult = adb -s $DeviceIP shell am start -n "$PackageName/$ActivityName" 2>&1
Write-Host $launchResult

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ VisionFocus launched successfully on wireless device!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Failed to launch app. Make sure the app is installed." -ForegroundColor Red
    exit 1
}
