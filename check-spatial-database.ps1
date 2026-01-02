# Quick script to check spatial info in database
# Story 4.5: Verify positionText and distanceText are being saved

Write-Host "`n=== CHECKING SPATIAL INFO IN DATABASE ===" -ForegroundColor Cyan

$query = "SELECT id, category, confidence, positionText, distanceText, timestamp FROM recognition_history ORDER BY timestamp DESC LIMIT 10;"

Write-Host "`nRunning query: $query`n" -ForegroundColor Yellow

adb shell "run-as com.visionfocus sqlite3 -header -column databases/visionfocus_db '$query'"

Write-Host "`n=== END DATABASE CHECK ===" -ForegroundColor Cyan
