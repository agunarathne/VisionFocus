# Fix Database Pull - Proper Binary Transfer Method
# This script properly pulls the SQLite database from the Android device
# avoiding binary corruption that occurs with 'cat' redirection
#
# NOTE: The VisionFocus database is ENCRYPTED with SQLCipher (AES-256)
# The pulled file will NOT be readable in standard SQLite viewers.
# See DATABASE-ACCESS-GUIDE.md for viewing options.

Write-Host "=== VisionFocus Database Pull ===" -ForegroundColor Cyan
Write-Host "NOTE: Database is encrypted with SQLCipher" -ForegroundColor Yellow
Write-Host ""

# Step 1: Copy database to accessible location
Write-Host "[1/4] Copying database to accessible location..." -ForegroundColor Yellow
$copyResult = adb shell "run-as com.visionfocus cp databases/visionfocus_database /sdcard/Download/vf_temp.db" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Failed to copy database" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ Copied to /sdcard/Download/" -ForegroundColor Green

# Step 2: Pull using adb pull (preserves binary data)
Write-Host "[2/4] Pulling database file..." -ForegroundColor Yellow
$pullResult = adb pull /sdcard/Download/vf_temp.db visionfocus_database.db 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Failed to pull database" -ForegroundColor Red
    adb shell "rm /sdcard/Download/vf_temp.db" 2>&1
    exit 1
}
Write-Host "  ✓ Pulled to local directory" -ForegroundColor Green

# Step 3: Clean up temp file
Write-Host "[3/4] Cleaning up..." -ForegroundColor Yellow
adb shell "rm /sdcard/Download/vf_temp.db" 2>&1 | Out-Null
Write-Host "  ✓ Temp file removed" -ForegroundColor Green

# Step 4: Validate the database file
Write-Host "[4/4] Validating database..." -ForegroundColor Yellow
if (Test-Path visionfocus_database.db) {
    $file = Get-Item visionfocus_database.db
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    
    if ($bytes.Length -lt 16) {
        Write-Host "✗ File is too small ($($bytes.Length) bytes)" -ForegroundColor Red
        exit 1
    }
    
    $header = [System.Text.Encoding]::ASCII.GetString($bytes[0..15])
    
    if ($header.StartsWith("SQLite format 3")) {
        Write-Host "  ✓ Valid SQLite database!" -ForegroundColor Green
        Write-Host ""
        Write-Host "=== SUCCESS ===" -ForegroundColor Green
        Write-Host "Database: $($file.Name)"
        Write-Host "Size: $($file.Length) bytes"
        Write-Host "Modified: $($file.LastWriteTime)"
        Write-Host ""
        Write-Host "You can now open this file in VS Code or any SQLite viewer." -ForegroundColor Yellow
    } else {
        Write-Host "  ℹ Database is encrypted (SQLCipher)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "=== FILE PULLED SUCCESSFULLY ===" -ForegroundColor Green
        Write-Host "Database: $($file.Name)"
        Write-Host "Size: $($file.Length) bytes"
        Write-Host "Modified: $($file.LastWriteTime)"
        Write-Host "Encryption: SQLCipher (AES-256)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "⚠️  This database is ENCRYPTED and cannot be opened" -ForegroundColor Yellow
        Write-Host "    in standard SQLite viewers without the passphrase." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "To view database contents, use:" -ForegroundColor Cyan
        Write-Host "  1. Android Studio Database Inspector (recommended)" -ForegroundColor White
        Write-Host "  2. Monitor Database (Live) task" -ForegroundColor White
        Write-Host "  3. Database Stats task" -ForegroundColor White
        Write-Host ""
        Write-Host "See DATABASE-ACCESS-GUIDE.md for detailed instructions." -ForegroundColor Gray
    }
} else {
    Write-Host "✗ Database file was not created" -ForegroundColor Red
    exit 1
}
