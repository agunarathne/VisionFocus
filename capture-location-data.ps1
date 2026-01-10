# Capture Saved Location Data from Navigation Logs
# Story 7.3 - Extract location coordinates from voice navigation

param(
    [string]$DeviceSerial = "192.168.1.95:39547"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Capture Location Data from Logs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📱 Device: $DeviceSerial" -ForegroundColor White
Write-Host ""

Write-Host "INSTRUCTIONS:" -ForegroundColor Yellow
Write-Host "  1. Navigate to FIRST location using voice command" -ForegroundColor White
Write-Host "     Say: 'Navigate to [first location name]'" -ForegroundColor Gray
Write-Host "  2. Wait 5 seconds for route calculation" -ForegroundColor White
Write-Host "  3. Cancel navigation and return to home" -ForegroundColor White
Write-Host ""
Write-Host "  4. Navigate to SECOND location using voice command" -ForegroundColor White
Write-Host "     Say: 'Navigate to [second location name]'" -ForegroundColor Gray
Write-Host "  5. Wait 5 seconds for route calculation" -ForegroundColor White
Write-Host ""
Write-Host "Press ENTER when you're ready to start capturing logs..." -ForegroundColor Yellow
Read-Host

Write-Host ""
Write-Host "✅ Log capture started! Navigate to your locations now..." -ForegroundColor Green
Write-Host "   (This will run for 60 seconds)" -ForegroundColor Gray
Write-Host ""

# Capture logs in real-time
$logFile = "location-capture-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
Write-Host "📝 Saving logs to: $logFile" -ForegroundColor Cyan

Start-Sleep -Seconds 60

# Get the captured logs
Write-Host ""
Write-Host "Analyzing logs..." -ForegroundColor Yellow
$logs = adb -s $DeviceSerial logcat -d

# Save full logs
$logs | Out-File $logFile

# Extract location data
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Extracted Location Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$destinations = $logs | Select-String "Destination\(query=|destination=Destination\("
$routes = $logs | Select-String "Route calculated:.*to "
$startNav = $logs | Select-String "Starting navigation to "

if ($destinations) {
    Write-Host ""
    Write-Host "DESTINATIONS:" -ForegroundColor Green
    $destinations | Select-Object -Last 4 | ForEach-Object { 
        Write-Host "  $_" -ForegroundColor White
    }
}

if ($routes) {
    Write-Host ""
    Write-Host "ROUTES:" -ForegroundColor Green
    $routes | Select-Object -Last 4 | ForEach-Object { 
        Write-Host "  $_" -ForegroundColor White
    }
}

if ($startNav) {
    Write-Host ""
    Write-Host "NAVIGATION STARTED:" -ForegroundColor Green
    $startNav | Select-Object -Last 4 | ForEach-Object { 
        Write-Host "  $_" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "Full logs saved to: $logFile" -ForegroundColor Cyan
Write-Host ""
Write-Host "Now searching for detailed coordinate data..." -ForegroundColor Yellow

# Search for specific patterns
$coordLogs = $logs | Select-String "latitude|longitude|Destination|formattedAddress" -Context 0,1

if ($coordLogs) {
    Write-Host ""
    Write-Host "COORDINATE DATA:" -ForegroundColor Green
    $coordLogs | Select-Object -Last 20 | ForEach-Object {
        Write-Host "  $_" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Capture complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next: Review the output above and provide location details" -ForegroundColor Yellow
