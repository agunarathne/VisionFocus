# Restore Test Locations to VisionFocus Database
# Inserts 2 saved test locations when database is empty
# Story 7.3 Manual Testing - Location restoration script

param(
    [string]$DeviceSerial = "192.168.1.95:39547"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   VisionFocus - Restore Test Locations" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test locations configuration
# INSTRUCTIONS: Update coordinates below with your actual test locations
# Current location detected: ~7.255, 80.545 (Sri Lanka)
# Home: ~16km away, Home2: ~3m away from current location

$locations = @(
    @{
        Name = "Home"
        Latitude = 7.3850   # UPDATE: Enter exact latitude from saved location
        Longitude = 80.5450  # UPDATE: Enter exact longitude from saved location
        Address = "Sigiriya Road, Dambulla, Sri Lanka"  # UPDATE: Enter exact address
    },
    @{
        Name = "Home2"
        Latitude = 7.2550   # Very close to current location (7.255, 80.545)
        Longitude = 80.5455  # UPDATE: Adjust if needed
        Address = "Near Current Location, Sri Lanka"  # UPDATE: Enter exact address
    }
)

Write-Host "📱 Device: $DeviceSerial" -ForegroundColor White
Write-Host "📍 Locations to restore: $($locations.Count)" -ForegroundColor White
Write-Host ""

# Check if device is connected
Write-Host "Checking device connection..." -ForegroundColor Yellow
$devices = adb devices | Select-String $DeviceSerial
if (-not $devices) {
    Write-Host "❌ Device not found: $DeviceSerial" -ForegroundColor Red
    Write-Host "   Run .\launch-wireless.ps1 to connect" -ForegroundColor Gray
    exit 1
}
Write-Host "✅ Device connected" -ForegroundColor Green
Write-Host ""

# Insert locations using ContentProvider
Write-Host "Inserting test locations..." -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

foreach ($loc in $locations) {
    Write-Host "  📍 $($loc.Name)..." -ForegroundColor White
    
    # Build adb shell command to insert via ContentProvider
    # Using INSERT statement directly through app's database
    $insertCmd = "run-as com.visionfocus sh -c `"" +
                 "cd databases && " +
                 "echo `\`"INSERT OR REPLACE INTO saved_locations (name, latitude, longitude, address, createdAt, lastUsedAt) " +
                 "VALUES ('$($loc.Name)', $($loc.Latitude), $($loc.Longitude), '$($loc.Address)', $timestamp, $timestamp);`\`" | " +
                 "sqlite3 visionfocus.db`""
    
    try {
        $result = adb -s $DeviceSerial shell $insertCmd 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "     ✅ Inserted: $($loc.Name)" -ForegroundColor Green
            Write-Host "        Lat: $($loc.Latitude), Lon: $($loc.Longitude)" -ForegroundColor Gray
        } else {
            Write-Host "     ⚠️  Warning: $result" -ForegroundColor Yellow
            
            # Fallback: Try using app's SavedLocationRepository via Intent
            Write-Host "     Trying alternative method..." -ForegroundColor Yellow
            $intentCmd = "am broadcast -a com.visionfocus.ADD_TEST_LOCATION " +
                        "--es name `"$($loc.Name)`" " +
                        "--ef latitude $($loc.Latitude) " +
                        "--ef longitude $($loc.Longitude) " +
                        "--es address `"$($loc.Address)`""
            
            adb -s $DeviceSerial shell $intentCmd | Out-Null
            Write-Host "     ℹ️  Broadcast sent (may require manual verification)" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "     ❌ Failed: $_" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ RESTORE COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Verification:" -ForegroundColor Yellow
Write-Host "   1. Open VisionFocus app" -ForegroundColor White
Write-Host "   2. Navigate to Saved Locations screen" -ForegroundColor White
Write-Host "   3. Verify both locations appear in the list" -ForegroundColor White
Write-Host "   4. Or say 'Navigate to Home' to test" -ForegroundColor White
Write-Host ""
Write-Host "📝 Note: If locations don't appear, you may need to:" -ForegroundColor Yellow
Write-Host "   - Update the coordinates in this script" -ForegroundColor Gray
Write-Host "   - Use the app's UI to manually add them" -ForegroundColor Gray
Write-Host "   - Check logcat for database errors" -ForegroundColor Gray
Write-Host ""
