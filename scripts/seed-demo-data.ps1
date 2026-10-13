# GateNest Apartment Visitor Management System
# Script: seed-demo-data.ps1
# Description: Triggers guarded demo data seeding on the dedicated demo backend

param(
    [string]$BackendUrl = "http://localhost:8080"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  GateNest Demo Data Seeder (Bluehaven Residency)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$seedUrl = "$BackendUrl/api/v1/admin/demo/seed"

try {
    Write-Host "Triggering guarded demo seeding at $seedUrl..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Method Post -Uri $seedUrl -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "`n[SUCCESS] Demo dataset successfully seeded!" -ForegroundColor Green
        Write-Host "----------------------------------------------------------" -ForegroundColor Gray
        Write-Host "  Community:        $($response.data.communityName)" -ForegroundColor White
        Write-Host "  Reference Time:   $($response.data.referenceTimestamp) ($($response.data.timezone))" -ForegroundColor White
        Write-Host "  Blocks / Flats:   $($response.data.blocksCount) blocks / $($response.data.flatsCount) flats" -ForegroundColor White
        Write-Host "  Active Gates:     $($response.data.gatesCount)" -ForegroundColor White
        Write-Host "  Total Users:      $($response.data.totalUsersCount) (Admins, Guards, Residents)" -ForegroundColor White
        Write-Host "  Seeded Visits:    $($response.data.visitsCount)" -ForegroundColor White
        Write-Host "  Parcels:          $($response.data.parcelsCount)" -ForegroundColor White
        Write-Host "  Complaints:       $($response.data.complaintsCount)" -ForegroundColor White
        Write-Host "  Announcements:    $($response.data.announcementsCount)" -ForegroundColor White
        Write-Host "  Domestic Staff:   $($response.data.staffCount)" -ForegroundColor White
        Write-Host "  Parking Slots:    $($response.data.parkingSlotsCount)" -ForegroundColor White
        Write-Host "----------------------------------------------------------" -ForegroundColor Gray
    } else {
        Write-Host "`n[ERROR] Seeding failed: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "`n[ERROR] Failed to reach GateNest backend at $BackendUrl" -ForegroundColor Red
    Write-Host "Details: $_" -ForegroundColor Red
    exit 1
}
