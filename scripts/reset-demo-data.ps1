# GateNest Apartment Visitor Management System
# Script: reset-demo-data.ps1
# Description: Resets and reseeds the dedicated demo database (guarded)

param(
    [string]$BackendUrl = "http://localhost:8080",
    [switch]$Force
)

Write-Host "==========================================================" -ForegroundColor Magenta
Write-Host "  GateNest Demo Database Reset & Reseed" -ForegroundColor Magenta
Write-Host "==========================================================" -ForegroundColor Magenta

if (-not $Force) {
    $confirm = Read-Host "Are you sure you want to reset the DEMO database? (y/N)"
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "Operation aborted by user." -ForegroundColor Yellow
        exit 0
    }
}

$resetUrl = "$BackendUrl/api/v1/admin/demo/reset"

try {
    Write-Host "Executing guarded demo database reset at $resetUrl..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Method Post -Uri $resetUrl -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "`n[SUCCESS] Demo database reset & reseeded successfully!" -ForegroundColor Green
        Write-Host "----------------------------------------------------------" -ForegroundColor Gray
        Write-Host "  Community:        $($response.data.communityName)" -ForegroundColor White
        Write-Host "  Reference Time:   $($response.data.referenceTimestamp)" -ForegroundColor White
        Write-Host "  Blocks / Flats:   $($response.data.blocksCount) blocks / $($response.data.flatsCount) flats" -ForegroundColor White
        Write-Host "  Total Users:      $($response.data.totalUsersCount)" -ForegroundColor White
        Write-Host "  Visits Count:     $($response.data.visitsCount)" -ForegroundColor White
        Write-Host "----------------------------------------------------------" -ForegroundColor Gray
    } else {
        Write-Host "`n[ERROR] Reset failed: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "`n[ERROR] Failed to reach GateNest backend at $BackendUrl" -ForegroundColor Red
    Write-Host "Details: $_" -ForegroundColor Red
    exit 1
}
