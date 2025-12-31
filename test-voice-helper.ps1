# Voice Recognition Testing Helper
# Story 3.1 - Quick commands for manual testing

Write-Host "`n🎤 VOICE RECOGNITION TEST HELPER`n" -ForegroundColor Green

function Show-Menu {
    Write-Host "Select test action:" -ForegroundColor Cyan
    Write-Host "  [1] Fresh start (clear app data)" -ForegroundColor White
    Write-Host "  [2] Monitor voice logs (live)" -ForegroundColor White
    Write-Host "  [3] Show last transcription" -ForegroundColor White
    Write-Host "  [4] Check debouncing logs" -ForegroundColor White
    Write-Host "  [5] Check permission state" -ForegroundColor White
    Write-Host "  [6] Install latest build" -ForegroundColor White
    Write-Host "  [7] Clear logs and start fresh" -ForegroundColor White
    Write-Host "  [8] Show errors only" -ForegroundColor White
    Write-Host "  [Q] Quit`n" -ForegroundColor White
}

function Clear-AppData {
    Write-Host "`n🔧 Clearing app data..." -ForegroundColor Yellow
    adb shell pm clear com.visionfocus
    Write-Host "✅ Done! Launch app for fresh start.`n" -ForegroundColor Green
}

function Monitor-VoiceLogs {
    Write-Host "`n📡 Monitoring voice logs (Ctrl+C to stop)..." -ForegroundColor Yellow
    Write-Host "Filters: VoiceRecognition, transcription, error`n" -ForegroundColor Gray
    adb logcat -c
    adb logcat -s VoiceRecognitionManager:D VoiceRecognitionVM:D MainActivity:D
}

function Show-LastTranscription {
    Write-Host "`n📝 Last 5 transcriptions:" -ForegroundColor Yellow
    adb logcat -d | Select-String -Pattern "transcription" | Select-Object -Last 5
    Write-Host ""
}

function Check-Debouncing {
    Write-Host "`n🔍 Checking debouncing logs..." -ForegroundColor Yellow
    $logs = adb logcat -d | Select-String -Pattern "debounced"
    if ($logs) {
        Write-Host "Found debouncing events:`n" -ForegroundColor Green
        $logs | Select-Object -Last 10
    } else {
        Write-Host "No debouncing events found (rapid clicks not detected)`n" -ForegroundColor Gray
    }
}

function Check-Permission {
    Write-Host "`n🔐 Checking microphone permission..." -ForegroundColor Yellow
    $perm = adb shell dumpsys package com.visionfocus | Select-String -Pattern "RECORD_AUDIO"
    if ($perm) {
        Write-Host $perm -ForegroundColor Green
    } else {
        Write-Host "Could not retrieve permission info`n" -ForegroundColor Red
    }
}

function Install-Build {
    Write-Host "`n🔨 Building and installing..." -ForegroundColor Yellow
    .\gradlew.bat installDebug --quiet
    Write-Host "✅ Installation complete!`n" -ForegroundColor Green
}

function Clear-LogsStart {
    Write-Host "`n🧹 Clearing logs..." -ForegroundColor Yellow
    adb logcat -c
    Write-Host "✅ Logs cleared. Ready for testing!`n" -ForegroundColor Green
}

function Show-Errors {
    Write-Host "`n❌ Last 20 errors:" -ForegroundColor Yellow
    adb logcat -d -s AndroidRuntime:E | Select-Object -Last 20
    Write-Host ""
}

# Main loop
do {
    Show-Menu
    $choice = Read-Host "Enter choice"
    
    switch ($choice) {
        "1" { Clear-AppData }
        "2" { Monitor-VoiceLogs }
        "3" { Show-LastTranscription }
        "4" { Check-Debouncing }
        "5" { Check-Permission }
        "6" { Install-Build }
        "7" { Clear-LogsStart }
        "8" { Show-Errors }
        "Q" { 
            Write-Host "`n👋 Happy testing!`n" -ForegroundColor Green
            exit 
        }
        default { Write-Host "`nInvalid choice. Try again.`n" -ForegroundColor Red }
    }
    
    if ($choice -ne "Q") {
        Write-Host "Press Enter to continue..." -ForegroundColor Gray
        Read-Host
        Clear-Host
    }
} while ($choice -ne "Q")
