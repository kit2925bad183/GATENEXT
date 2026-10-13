# GateNest Apartment Visitor Management System
# Script: verify-demo-suite.ps1
# Description: Automated end-to-end verification of demo accounts, APIs, filtering, and presentation workflows

param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Password = "DemoPass2026!"
)

$ErrorActionPreference = "Stop"
$passed = 0
$failed = 0

function Test-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host -NoNewline "[TEST] $Name ... "
    try {
        & $Action
        Write-Host "PASS" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "FAIL" -ForegroundColor Red
        Write-Host "       Error: $_" -ForegroundColor DarkRed
        $script:failed++
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  GateNest Demo Environment Comprehensive Test Suite" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Check Demo Status
Test-Step "1. Verify Demo Status API" {
    $res = Invoke-RestMethod -Uri "$BackendUrl/api/v1/admin/demo/status"
    if (-not $res.data.isDemoEnvironment -or $res.data.communityName -notmatch "Bluehaven Residency") {
        throw "Unexpected status response: $($res | ConvertTo-Json -Compress)"
    }
}

# 2. Check Community Info
Test-Step "2. Verify Public Community Settings (Demo Mode Flag)" {
    $res = Invoke-RestMethod -Uri "$BackendUrl/api/v1/community/public-settings"
    if (-not $res.data.demoMode -or $res.data.communityName -notmatch "Bluehaven Residency") {
        throw "Community public info does not reflect demo mode: $($res | ConvertTo-Json -Compress)"
    }
}

# 3. Test Admin Login
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "3. Authenticate Administrator (admin@gatenest.example)" {
    $body = @{
        email = "admin@gatenest.example"
        password = $Password
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/v1/auth/login" -Body $body -ContentType "application/json" -WebSession $adminSession
    if ($res.data.role -ne "ADMINISTRATOR") {
        throw "Expected ADMINISTRATOR role, got: $($res.data.role)"
    }
}

# 4. Test Security Login
$guardSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "4. Authenticate Main Gate Security (guard.main@gatenest.example)" {
    $body = @{
        email = "guard.main@gatenest.example"
        password = $Password
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/v1/auth/login" -Body $body -ContentType "application/json" -WebSession $guardSession
    if ($res.data.role -ne "SECURITY") {
        throw "Expected SECURITY role, got: $($res.data.role)"
    }
}

# 5. Test Resident Presenter Login
$resSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "5. Authenticate Presenter Resident (resident@gatenest.example)" {
    $body = @{
        email = "resident@gatenest.example"
        password = $Password
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/v1/auth/login" -Body $body -ContentType "application/json" -WebSession $resSession
    if ($res.data.role -ne "RESIDENT" -or $res.data.flatNumber -ne "A-101") {
        throw "Expected RESIDENT in A-101, got: $($res.data.flatNumber)"
    }
}

function Get-CsrfHeaders($session) {
    $cookies = $session.Cookies.GetCookies($BackendUrl)
    $xsrf = $cookies | Where-Object { $_.Name -eq 'XSRF-TOKEN' }
    if ($xsrf) {
        return @{ "X-XSRF-TOKEN" = $xsrf.Value }
    }
    return @{}
}

# 6. Admin Analytics Dashboard Calculation Verification
Test-Step "6. Verify Admin Dashboard Analytics & Block-wise Aggregations" {
    $res = Invoke-RestMethod -Uri "$BackendUrl/api/v1/reports/dashboard" -WebSession $adminSession
    if ($res.data.totalVisitsToday -lt 0) {
        throw "Dashboard stats missing totalVisitsToday"
    }
    $blockRes = Invoke-RestMethod -Uri "$BackendUrl/api/v1/reports/block-wise" -WebSession $adminSession
    if ($blockRes.data.Count -ne 3) {
        throw "Expected 3 blocks in block-wise breakdown, found: $($blockRes.data.Count)"
    }
    $visitsRes = Invoke-RestMethod -Uri "$BackendUrl/api/v1/visits?page=1&limit=10" -WebSession $adminSession
    if ($visitsRes.data.total -lt 150) {
        throw "Total visits count abnormally low: $($visitsRes.data.total)"
    }
}

# 7. Resident Pending Walk-ins
Test-Step "7. Verify Presenter Resident Pending Walk-ins (2 seeded)" {
    $res = Invoke-RestMethod -Uri "$BackendUrl/api/v1/resident/approval-inbox" -WebSession $resSession
    if ($res.data.Count -lt 2) {
        throw "Expected at least 2 pending visits, found $($res.data.Count)"
    }
}

# 8. Security Pass Verification
Test-Step "8. Verify Pass Verification for Pre-seeded Demo Pass (PASS-BLUEHAVEN-DEMO-01)" {
    $body = @{
        code = "PASS-BLUEHAVEN-DEMO-01"
    } | ConvertTo-Json
    $csrfHeaders = Get-CsrfHeaders $guardSession
    $res = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/v1/security/verify-pass" -Headers $csrfHeaders -Body $body -ContentType "application/json" -WebSession $guardSession
    if ($res.data.valid -ne $true -or $res.data.visit.visitorName -notmatch "Siddharth Verma") {
        throw "Pass verification failed: $($res | ConvertTo-Json -Compress)"
    }
}

# 9. Additional Modules Verification
Test-Step "9. Verify Parcels, Complaints, Announcements & Staff Modules" {
    $parcels = Invoke-RestMethod -Uri "$BackendUrl/api/v1/parcels/history" -WebSession $adminSession
    if ($parcels.data.Count -lt 10) { throw "Parcels count low: $($parcels.data.Count)" }
    
    $complaints = Invoke-RestMethod -Uri "$BackendUrl/api/v1/complaints" -WebSession $adminSession
    if ($complaints.data.Count -lt 5) { throw "Complaints count low: $($complaints.data.Count)" }
    
    $announcements = Invoke-RestMethod -Uri "$BackendUrl/api/v1/announcements/admin-all" -WebSession $adminSession
    if ($announcements.data.Count -lt 3) { throw "Announcements count low: $($announcements.data.Count)" }
    
    $staff = Invoke-RestMethod -Uri "$BackendUrl/api/v1/staff" -WebSession $adminSession
    if ($staff.data.Count -lt 5) { throw "Staff count low: $($staff.data.Count)" }
}

# 10. Guarded Reset and Pristine Demo State Restoration
Test-Step "10. Test Guarded Database Reseed & Restore Pristine Presentation Scenario" {
    $res = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/v1/admin/demo/reset" -ContentType "application/json"
    if (-not $res.success -or $res.data.visitsCount -lt 180) {
        throw "Reseed failed: $($res | ConvertTo-Json -Compress)"
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  TEST RESULTS: Passed: $passed, Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "==========================================================" -ForegroundColor Cyan

if ($failed -gt 0) { exit 1 }
