# Restore Test Locations - Manual Input Version
# Story 7.3 Manual Testing - Interactive location restoration
# 
# HOW TO GET YOUR COORDINATES:
# Option 1: Open app → Saved Locations → Long press location → Share → Copy coordinates
# Option 2: Navigate to location and check logcat for destination coordinates
# Option 3: Use Google Maps to find the location and copy coordinates

param(
    [string]$DeviceSerial = "192.168.1.95:39547"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   VisionFocus - Restore Test Locations" -ForegroundColor Cyan
Write-Host "   (Manual Input Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Detected from logs
Write-Host "📍 Detected from recent navigation:" -ForegroundColor Yellow
Write-Host "   Location 1: Home (distance: ~16.4 km from current)" -ForegroundColor White
Write-Host "   Location 2: Home2 (distance: ~3 m from current)" -ForegroundColor White
Write-Host "   Current position: ~7.255, 80.545" -ForegroundColor Gray
Write-Host ""

# Collect Location 1
Write-Host "=== LOCATION 1 ===" -ForegroundColor Cyan
$loc1_name = Read-Host "Enter name (default: Home)"
if ([string]::IsNullOrWhiteSpace($loc1_name)) { $loc1_name = "Home" }

$loc1_lat = Read-Host "Enter latitude (e.g., 7.3850)"
while ([string]::IsNullOrWhiteSpace($loc1_lat)) {
    Write-Host "  ⚠️  Latitude required!" -ForegroundColor Red
    $loc1_lat = Read-Host "Enter latitude"
}

$loc1_lon = Read-Host "Enter longitude (e.g., 80.5450)"
while ([string]::IsNullOrWhiteSpace($loc1_lon)) {
    Write-Host "  ⚠️  Longitude required!" -ForegroundColor Red
    $loc1_lon = Read-Host "Enter longitude"
}

$loc1_addr = Read-Host "Enter address (optional)"
if ([string]::IsNullOrWhiteSpace($loc1_addr)) { 
    $loc1_addr = "$loc1_name Location" 
}

Write-Host ""

# Collect Location 2
Write-Host "=== LOCATION 2 ===" -ForegroundColor Cyan
$loc2_name = Read-Host "Enter name (default: Home2)"
if ([string]::IsNullOrWhiteSpace($loc2_name)) { $loc2_name = "Home2" }

$loc2_lat = Read-Host "Enter latitude (e.g., 7.2550)"
while ([string]::IsNullOrWhiteSpace($loc2_lat)) {
    Write-Host "  ⚠️  Latitude required!" -ForegroundColor Red
    $loc2_lat = Read-Host "Enter latitude"
}

$loc2_lon = Read-Host "Enter longitude (e.g., 80.5455)"
while ([string]::IsNullOrWhiteSpace($loc2_lon)) {
    Write-Host "  ⚠️  Longitude required!" -ForegroundColor Red
    $loc2_lon = Read-Host "Enter longitude"
}

$loc2_addr = Read-Host "Enter address (optional)"
if ([string]::IsNullOrWhiteSpace($loc2_addr)) { 
    $loc2_addr = "$loc2_name Location" 
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CONFIRMATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Location 1: $loc1_name" -ForegroundColor Green
Write-Host "  Lat:     $loc1_lat" -ForegroundColor White
Write-Host "  Lon:     $loc1_lon" -ForegroundColor White
Write-Host "  Address: $loc1_addr" -ForegroundColor White
Write-Host ""
Write-Host "Location 2: $loc2_name" -ForegroundColor Green
Write-Host "  Lat:     $loc2_lat" -ForegroundColor White
Write-Host "  Lon:     $loc2_lon" -ForegroundColor White
Write-Host "  Address: $loc2_addr" -ForegroundColor White
Write-Host ""

$confirm = Read-Host "Proceed with restore? (Y/N)"
if ($confirm -ne 'Y' -and $confirm -ne 'y') {
    Write-Host "Cancelled." -ForegroundColor Yellow
    exit
}

# Check device connection
Write-Host ""
Write-Host "Checking device connection..." -ForegroundColor Yellow
$devices = adb devices | Select-String $DeviceSerial
if (-not $devices) {
    Write-Host "❌ Device not found: $DeviceSerial" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Device connected: $DeviceSerial" -ForegroundColor Green
Write-Host ""

# Insert locations
Write-Host "Inserting locations..." -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

# Location 1
Write-Host "  📍 $loc1_name..." -ForegroundColor White
$sql1 = "INSERT OR REPLACE INTO saved_locations (name, latitude, longitude, address, createdAt, lastUsedAt) VALUES ('$loc1_name', $loc1_lat, $loc1_lon, '$loc1_addr', $timestamp, $timestamp);"
$cmd1 = "run-as com.visionfocus sh -c `"cd databases && echo \`"$sql1\`" | sqlite3 visionfocus.db`""
adb -s $DeviceSerial shell $cmd1
if ($LASTEXITCODE -eq 0) {
    Write-Host "     ✅ $loc1_name inserted" -ForegroundColor Green
} else {
    Write-Host "     ⚠️  May need manual verification" -ForegroundColor Yellow
}

Start-Sleep -Milliseconds 500

# Location 2
Write-Host "  📍 $loc2_name..." -ForegroundColor White
$sql2 = "INSERT OR REPLACE INTO saved_locations (name, latitude, longitude, address, createdAt, lastUsedAt) VALUES ('$loc2_name', $loc2_lat, $loc2_lon, '$loc2_addr', $timestamp, $timestamp);"
$cmd2 = "run-as com.visionfocus sh -c `"cd databases && echo \`"$sql2\`" | sqlite3 visionfocus.db`""
adb -s $DeviceSerial shell $cmd2
if ($LASTEXITCODE -eq 0) {
    Write-Host "     ✅ $loc2_name inserted" -ForegroundColor Green
} else {
    Write-Host "     ⚠️  May need manual verification" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ RESTORE COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Verification:" -ForegroundColor Yellow
Write-Host "   1. Say 'Navigate to $loc1_name'" -ForegroundColor White
Write-Host "   2. Say 'Navigate to $loc2_name'" -ForegroundColor White
Write-Host "   3. Check Saved Locations screen in app" -ForegroundColor White
Write-Host ""

# Save configuration for future use
$configContent = @"
# VisionFocus Test Locations Configuration
# Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

`$locations = @(
    @{
        Name = "$loc1_name"
        Latitude = $loc1_lat
        Longitude = $loc1_lon
        Address = "$loc1_addr"
    },
    @{
        Name = "$loc2_name"
        Latitude = $loc2_lat
        Longitude = $loc2_lon
        Address = "$loc2_addr"
    }
)
"@

$configFile = "test-locations-config.ps1"
$configContent | Out-File $configFile
Write-Host "📝 Configuration saved to: $configFile" -ForegroundColor Cyan
Write-Host "   (You can use restore-test-locations.ps1 with this config)" -ForegroundColor Gray
Write-Host ""
