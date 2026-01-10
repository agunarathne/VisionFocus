# Get Saved Locations from VisionFocus Database
# Extracts location data for backup/restore purposes

Write-Host "=== VisionFocus Saved Locations ===" -ForegroundColor Cyan
Write-Host ""

# Method 1: Check logs for location data
Write-Host "[1] Checking recent navigation logs..." -ForegroundColor Yellow
$navLogs = adb -s 192.168.1.95:39547 logcat -d | Select-String "Route calculated.*to|Starting navigation to|Updated lastUsedAt for location:"

if ($navLogs) {
    Write-Host "Recent locations used:" -ForegroundColor Green
    $navLogs | Select-Object -Last 10 | ForEach-Object {
        Write-Host "  $_"
    }
} else {
    Write-Host "  No recent navigation logs found" -ForegroundColor Gray
}

Write-Host ""

# Method 2: Trigger a voice command to list all locations
Write-Host "[2] To get full location details, please:" -ForegroundColor Yellow
Write-Host "  1. Say 'navigate to test' (use non-existent name)" -ForegroundColor White
Write-Host "  2. Or open Saved Locations screen in the app" -ForegroundColor White
Write-Host "  3. Then provide location names and coordinates" -ForegroundColor White

Write-Host ""
Write-Host "=== Location Data Format ===" -ForegroundColor Cyan
Write-Host "Please provide for each location:" -ForegroundColor White
Write-Host "  - Name (e.g., 'Home', 'Work')" -ForegroundColor Gray
Write-Host "  - Latitude (e.g., 13.0827)" -ForegroundColor Gray
Write-Host "  - Longitude (e.g., 80.2707)" -ForegroundColor Gray
Write-Host "  - Address (e.g., 'Chennai, Tamil Nadu')" -ForegroundColor Gray
Write-Host ""
