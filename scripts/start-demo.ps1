# GateNest Apartment Visitor Management System
# Script: start-demo.ps1
# Description: Starts GateNest backend with the isolated demo profile and launches frontend

param(
    [string]$DemoPassword = "DemoPass2026!"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  GateNest Demo Environment Launcher" -ForegroundColor Cyan
Write-Host "  Community: Bluehaven Residency — Demo" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$env:DEMO_ACCOUNT_PASSWORD = $DemoPassword
Write-Host "[CONFIG] DEMO_ACCOUNT_PASSWORD set for presentation accounts." -ForegroundColor Green
Write-Host "[CONFIG] Active profile: demo (Target DB: gatenest_demo)" -ForegroundColor Green

Write-Host "`nTo start the backend in demo mode:" -ForegroundColor Yellow
Write-Host "  cd backend" -ForegroundColor White
Write-Host "  .\mvnw.cmd spring-boot:run `"-Dspring-boot.run.profiles=demo`"" -ForegroundColor White

Write-Host "`nTo start the frontend:" -ForegroundColor Yellow
Write-Host "  cd frontend" -ForegroundColor White
Write-Host "  npm run dev" -ForegroundColor White

Write-Host "`nDemo Accounts available upon seeding:" -ForegroundColor Cyan
Write-Host "  • Administrator:  admin@gatenest.example / $DemoPassword" -ForegroundColor White
Write-Host "  • Administrator2: admin2@gatenest.example / $DemoPassword" -ForegroundColor White
Write-Host "  • Security (Main): guard.main@gatenest.example / $DemoPassword" -ForegroundColor White
Write-Host "  • Security (Svc):  guard.service@gatenest.example / $DemoPassword" -ForegroundColor White
Write-Host "  • Presenter Res:  resident@gatenest.example / $DemoPassword (A-101)" -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Cyan
