# PowerShell Administrator Bootstrap Script for GateNest

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1/auth/bootstrap-admin",
    [string]$BootstrapToken = "bootstrap-gatenest-admin-secret-2026",
    [string]$FullName = "System Administrator",
    [string]$Email = "admin@greenfield.com",
    [string]$Mobile = "+91 80 4912 0001",
    [string]$Password = "admin1234"
)

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " GateNest - Administrator Bootstrap Setup" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

$body = @{
    bootstrapToken = $BootstrapToken
    fullName = $FullName
    email = $Email
    mobile = $Mobile
    password = $Password
} | ConvertTo-Json

try {
    Write-Host "Connecting to GateNest backend at: $BaseUrl" -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri $BaseUrl -Method Post -Body $body -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "SUCCESS! Administrator account created successfully." -ForegroundColor Green
        Write-Host "  Email:    $Email" -ForegroundColor White
        Write-Host "  Password: $Password" -ForegroundColor White
        Write-Host "You can now log in at http://localhost:5173/login" -ForegroundColor Green
    } else {
        Write-Host "ERROR: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "Failed to bootstrap administrator: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Ensure the GateNest backend is running on port 8080." -ForegroundColor Yellow
}
