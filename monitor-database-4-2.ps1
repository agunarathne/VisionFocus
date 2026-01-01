# Database Monitoring Script for Story 4.2
# Recognition History Storage

param(
    [string]$DeviceIP = "192.168.8.102:43965",
    [switch]$Continuous,
    [int]$RefreshSeconds = 3
)

function Show-Header {
    Clear-Host
    Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║     Story 4.2: Recognition History Database Monitor      ║" -ForegroundColor Cyan
    Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Check-DatabaseFiles {
    Write-Host "📁 Database Files:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    $dbList = adb -s $DeviceIP shell "run-as com.visionfocus ls -lh databases/ 2>&1"
    
    if ($dbList -match "visionfocus_database") {
        Write-Host "  ✅ Database created successfully!" -ForegroundColor Green
        Write-Host ""
        
        # Parse file listing
        $dbList -split "`n" | ForEach-Object {
            if ($_ -match "visionfocus_database") {
                Write-Host "  $_" -ForegroundColor Gray
            }
        }
        return $true
    } else {
        Write-Host "  ⚠️  Database not yet created" -ForegroundColor Yellow
        Write-Host "     Perform object recognitions to create database" -ForegroundColor Gray
        return $false
    }
    Write-Host ""
}

function Show-RecentLogs {
    Write-Host "`n📝 Recent Recognition History Logs:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    # Check for save logs
    $saveLogs = adb -s $DeviceIP logcat -d -s "RecognitionHistoryRepositoryImpl:D" | Select-String "Saved recognition" | Select-Object -Last 10
    
    if ($saveLogs) {
        $saveLogs | ForEach-Object {
            $line = $_.Line
            if ($line -match "Saved recognition: (.+?) \(confidence: ([\d.]+)%\)") {
                $category = $matches[1]
                $confidence = $matches[2]
                $timestamp = if ($line -match "(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})") { $matches[1] } else { "Unknown" }
                Write-Host "  ✓ [$timestamp] " -NoNewline -ForegroundColor Green
                Write-Host "$category " -NoNewline -ForegroundColor White
                Write-Host "($confidence% confidence)" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  No save logs found yet" -ForegroundColor Gray
    }
    
    # Check for validation errors
    $errorLogs = adb -s $DeviceIP logcat -d -s "RecognitionHistoryRepositoryImpl:E" | Select-Object -Last 5
    if ($errorLogs) {
        Write-Host "`n  ⚠️  Errors detected:" -ForegroundColor Yellow
        $errorLogs | ForEach-Object {
            Write-Host "     $_" -ForegroundColor Red
        }
    }
}

function Show-RecognitionStats {
    Write-Host "`n📊 Recognition Statistics:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    $logs = adb -s $DeviceIP logcat -d -s "RecognitionHistoryRepositoryImpl:D" | Select-String "Saved recognition"
    $totalSaves = ($logs | Measure-Object).Count
    
    Write-Host "  Total Recognitions Saved: " -NoNewline
    Write-Host "$totalSaves" -ForegroundColor Cyan
    
    if ($totalSaves -gt 0) {
        # Count by category
        $categories = @{}
        $logs | ForEach-Object {
            if ($_.Line -match "Saved recognition: (.+?) \(") {
                $cat = $matches[1]
                if ($categories.ContainsKey($cat)) {
                    $categories[$cat]++
                } else {
                    $categories[$cat] = 1
                }
            }
        }
        
        Write-Host "`n  Breakdown by Category:" -ForegroundColor White
        $categories.GetEnumerator() | Sort-Object Value -Descending | ForEach-Object {
            $bar = "█" * [Math]::Min($_.Value, 20)
            Write-Host "    $($_.Key): " -NoNewline -ForegroundColor Gray
            Write-Host "$bar" -NoNewline -ForegroundColor Cyan
            Write-Host " ($($_.Value))" -ForegroundColor White
        }
    }
}

function Show-DatabaseInfo {
    Write-Host "`n🔐 Database Configuration:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "  Encryption: " -NoNewline
    Write-Host "✅ SQLCipher (AES-256)" -ForegroundColor Green
    Write-Host "  Key Management: " -NoNewline
    Write-Host "Android Keystore" -ForegroundColor Green
    Write-Host "  Schema Version: " -NoNewline
    Write-Host "2" -ForegroundColor Cyan
    Write-Host "  Max Entries: " -NoNewline
    Write-Host "50 (auto-pruned)" -ForegroundColor Cyan
    Write-Host "  Table: " -NoNewline
    Write-Host "recognition_history" -ForegroundColor Cyan
    Write-Host "  Columns: " -NoNewline
    Write-Host "id, category, confidence, timestamp, verbosityMode, detailText" -ForegroundColor Gray
    Write-Host "  Index: " -NoNewline
    Write-Host "idx_recognition_timestamp (optimized queries)" -ForegroundColor Green
}

function Show-LiveMonitoring {
    Write-Host "`n📡 Live Monitoring (Ctrl+C to stop):" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "  Watching for new recognitions..." -ForegroundColor Gray
    Write-Host ""
    
    # Clear logcat and monitor new entries
    adb -s $DeviceIP logcat -c
    
    adb -s $DeviceIP logcat -s "RecognitionHistoryRepositoryImpl:*" "RecognitionViewModel:*" | ForEach-Object {
        if ($_ -match "Saved recognition: (.+?) \(confidence: ([\d.]+)%\)") {
            $category = $matches[1]
            $confidence = $matches[2]
            $time = Get-Date -Format "HH:mm:ss"
            Write-Host "  [$time] " -NoNewline -ForegroundColor Green
            Write-Host "✓ Saved: " -NoNewline -ForegroundColor White
            Write-Host "$category " -NoNewline -ForegroundColor Cyan
            Write-Host "($confidence%)" -ForegroundColor Gray
        }
        elseif ($_ -match "Story 4.2: Recognition saved to history") {
            $time = Get-Date -Format "HH:mm:ss"
            Write-Host "  [$time] " -NoNewline -ForegroundColor Green
            Write-Host "✓ History save confirmed" -ForegroundColor White
        }
        elseif ($_ -match "Failed to save recognition history") {
            $time = Get-Date -Format "HH:mm:ss"
            Write-Host "  [$time] " -NoNewline -ForegroundColor Red
            Write-Host "✗ Save failed!" -ForegroundColor Red
        }
    }
}

function Show-TestInstructions {
    Write-Host "`n💡 Testing Instructions:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "  1. Open VisionFocus app on your phone" -ForegroundColor White
    Write-Host "  2. Tap the FAB (Floating Action Button)" -ForegroundColor White
    Write-Host "  3. Point camera at objects" -ForegroundColor White
    Write-Host "  4. Watch this terminal for database saves!" -ForegroundColor White
    Write-Host ""
    Write-Host "  Each recognition automatically saves to encrypted database" -ForegroundColor Gray
    Write-Host ""
}

# Main execution
Show-Header

# Check device connection
$deviceCheck = adb -s $DeviceIP get-state 2>&1
if ($deviceCheck -ne "device") {
    Write-Host "❌ Device not connected!" -ForegroundColor Red
    Write-Host ""
    Write-Host "To reconnect wireless debugging:" -ForegroundColor Yellow
    Write-Host "  1. On phone: Settings → Developer Options → Wireless Debugging" -ForegroundColor Gray
    Write-Host "  2. Enable and note the IP address" -ForegroundColor Gray
    Write-Host "  3. Run: adb connect <IP>:<PORT>" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Example: adb connect 192.168.8.102:43965" -ForegroundColor Cyan
    exit 1
}

if ($Continuous) {
    # Continuous monitoring mode
    while ($true) {
        Show-Header
        $dbExists = Check-DatabaseFiles
        Show-RecentLogs
        Show-RecognitionStats
        Show-DatabaseInfo
        
        Write-Host "`n⏱️  Refreshing in $RefreshSeconds seconds... (Ctrl+C to stop)" -ForegroundColor DarkGray
        Start-Sleep -Seconds $RefreshSeconds
    }
} else {
    # Single snapshot
    $dbExists = Check-DatabaseFiles
    
    if ($dbExists) {
        Show-RecentLogs
        Show-RecognitionStats
        Show-DatabaseInfo
    } else {
        Show-TestInstructions
    }
    
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  Run with -Continuous flag for live monitoring" -ForegroundColor Gray
    Write-Host "  Example: .\monitor-database-4-2.ps1 -Continuous" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Or watch live logs:" -ForegroundColor Gray
    Write-Host "  .\monitor-database-4-2.ps1 -Continuous -RefreshSeconds 2" -ForegroundColor Cyan
    Write-Host ""
}
