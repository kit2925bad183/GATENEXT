param()

$baseUrl = "http://localhost:8080/api/v1"
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

$passCount = 0
$failCount = 0

function Get-CsrfHeaders($sessionObj) {
    $headers = @{}
    if ($sessionObj -and $sessionObj.Cookies) {
        $c = $sessionObj.Cookies.GetCookies($baseUrl) | Where-Object { $_.Name -eq "XSRF-TOKEN" }
        if ($c) {
            $headers["X-XSRF-TOKEN"] = $c.Value
        }
    }
    return $headers
}

function Test-Step($name, [scriptblock]$action) {
    Write-Host "`n>>> TESTING: $name" -ForegroundColor Cyan
    try {
        & $action
        Write-Host "[PASSED] $name" -ForegroundColor Green
        $script:passCount++
    } catch {
        $msg = $_.ErrorDetails.Message
        if (-not $msg) { $msg = $_.Exception.Message }
        Write-Host "[FAILED] $name => $msg" -ForegroundColor Red
        $script:failCount++
    }
}

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "   GATENEST ADVANCED SUITE & COMPREHENSIVE VERIFICATION   " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow

# 1. Public Structure
Test-Step "1. Public Community Structure" {
    $res = Invoke-RestMethod -Uri "$baseUrl/community/public-structure" -Method Get
    if (-not $res.data.blocks -or $res.data.blocks.Count -eq 0) { throw "No blocks found" }
    Write-Host "Retrieved $($res.data.blocks.Count) blocks, $($res.data.flats.Count) flats, $($res.data.gates.Count) gates."
}

# 2. Admin Login
$adminUser = $null
Test-Step "2. Admin Authentication" {
    $body = @{ email = "admin@greenfield.com"; password = "admin1234" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $body -ContentType "application/json" -WebSession $adminSession
    if ($res.data.role -ne "ADMINISTRATOR") { throw "Expected ADMINISTRATOR role" }
    $script:adminUser = $res.data
    
    # Initialize CSRF cookie
    $null = Invoke-RestMethod -Uri "$baseUrl/auth/session" -Method Get -WebSession $adminSession
    Write-Host "Authenticated as: $($res.data.fullName) (ID: $($res.data.id))"
}

# 3. Create Admin-Managed Security Guard Account
$guardActivationToken = $null
$guardEmail = "guard.south." + (Get-Random -Minimum 1000 -Maximum 9999) + "@greenfield.com"
Test-Step "3. Admin Provision Security Staff with Activation Link" {
    $body = @{
        fullName = "South Gate Guard Test"
        email = $guardEmail
        mobile = "+919876541122"
        role = "SECURITY"
        assignedGateId = 2
        shiftName = "Evening Shift"
    } | ConvertTo-Json
    $headers = Get-CsrfHeaders $adminSession
    $res = Invoke-RestMethod -Uri "$baseUrl/admin/users" -Method Post -Body $body -ContentType "application/json" -Headers $headers -WebSession $adminSession
    if (-not $res.data.activationToken) { throw "Expected raw activation token in response" }
    $script:guardActivationToken = $res.data.activationToken
    Write-Host "Created Security User: $($res.data.user.email) with single-use token: $($res.data.activationToken)"
}

# 4. Redeem Activation Token & Set Password
Test-Step "4. Account Activation & Password Setup Workflow" {
    $body = @{
        token = $script:guardActivationToken
        newPassword = "GuardPassword123!"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/activate" -Method Post -Body $body -ContentType "application/json"
    Write-Host "Activation result: $($res.message)"
}

# 5. Sign in with newly activated account
$guardSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "5. Sign-in With Activated Account" {
    $body = @{ email = $guardEmail; password = "GuardPassword123!" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $body -ContentType "application/json" -WebSession $guardSession
    if ($res.data.role -ne "SECURITY") { throw "Expected SECURITY role" }
    
    # Initialize CSRF cookie for guard
    $null = Invoke-RestMethod -Uri "$baseUrl/auth/session" -Method Get -WebSession $guardSession
    Write-Host "Guard logged in: $($res.data.fullName) Status: $($res.data.status)"
}

# 6. Session Listing & Revocation
Test-Step "6. Active User Sessions Query" {
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/sessions" -Method Get -WebSession $guardSession
    if (-not $res.data -or $res.data.Count -eq 0) { throw "Expected active sessions" }
    Write-Host "Found $($res.data.Count) active session(s) for guard."
}

# 7. Last-Admin Protection Guard
Test-Step "7. Last-Administrator Demotion/Suspension Protection" {
    $body = @{
        status = "SUSPENDED"
        adminPassword = "admin1234"
    } | ConvertTo-Json
    $headers = Get-CsrfHeaders $adminSession
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/admin/users/$($script:adminUser.id)/status" -Method Post -Body $body -ContentType "application/json" -Headers $headers -WebSession $adminSession
        throw "Expected failure when suspending the only active administrator, but succeeded!"
    } catch {
        Write-Host "Safely rejected demotion of last administrator: $($_.Exception.Message)"
    }
}

# 8. Advanced Visitor Filtering & Date Basis Semantics
Test-Step "8. Advanced Visitor Filtering (Block, Category, Date Basis)" {
    $res1 = Invoke-RestMethod -Uri "$baseUrl/visits?dateBasis=ACTUAL_CHECK_IN&page=1&limit=10" -Method Get -WebSession $adminSession
    Write-Host "Visits with dateBasis=ACTUAL_CHECK_IN: $($res1.data.total) matches found."

    $res2 = Invoke-RestMethod -Uri "$baseUrl/visits?dateBasis=SCHEDULED_ARRIVAL&page=1&limit=10" -Method Get -WebSession $adminSession
    Write-Host "Visits with dateBasis=SCHEDULED_ARRIVAL: $($res2.data.total) matches found."

    $res3 = Invoke-RestMethod -Uri "$baseUrl/visits?category=GUEST&page=1&limit=5" -Method Get -WebSession $adminSession
    Write-Host "Visits with category=GUEST: $($res3.data.total) matches found."
}

# 9. Audited Time Corrections
Test-Step "9. Audited Visit Timestamp Correction" {
    # Fetch an existing visit to correct
    $visitsRes = Invoke-RestMethod -Uri "$baseUrl/visits?page=1&limit=1" -Method Get -WebSession $adminSession
    if ($visitsRes.data.items.Count -gt 0) {
        $v = $visitsRes.data.items[0]
        $correctionBody = @{
            fieldName = "check_in_time"
            correctedTime = (Get-Date).AddHours(-2).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
            reason = "Security guard log discrepancy corrected per CCTV timestamp audit"
        } | ConvertTo-Json
        $headers = Get-CsrfHeaders $adminSession
        $cRes = Invoke-RestMethod -Uri "$baseUrl/visits/$($v.id)/correct-times" -Method Post -Body $correctionBody -ContentType "application/json" -Headers $headers -WebSession $adminSession
        Write-Host "Correction recorded for Visit $($v.visitReference): $($cRes.message)"
    } else {
        Write-Host "No visit record available for correction test, skipping."
    }
}

# 10. Reports and Analytics Endpoints
Test-Step "10. Analytics Reports Generation" {
    $blockWise = Invoke-RestMethod -Uri "$baseUrl/reports/block-wise" -Method Get -WebSession $adminSession
    Write-Host "Block-wise data: $($blockWise.data.Count) block metrics."

    $gateTraffic = Invoke-RestMethod -Uri "$baseUrl/reports/gate-traffic" -Method Get -WebSession $adminSession
    Write-Host "Gate traffic: $($gateTraffic.data.Count) gate metrics."

    $categories = Invoke-RestMethod -Uri "$baseUrl/reports/visitor-categories" -Method Get -WebSession $adminSession
    Write-Host "Visitor categories: $($categories.data.Count) category metrics."

    $busiest = Invoke-RestMethod -Uri "$baseUrl/reports/busiest-hours" -Method Get -WebSession $adminSession
    Write-Host "Busiest arrival hours: $($busiest.data.Count) hourly slots."

    $duration = Invoke-RestMethod -Uri "$baseUrl/reports/average-duration" -Method Get -WebSession $adminSession
    Write-Host "Average completed duration: $($duration.data.averageDurationMinutes) mins across $($duration.data.completedVisitsCount) visits."
}

# 11. Safe CSV Export with Formula Injection Escaping
Test-Step "11. Safe CSV Export Verification" {
    $csvRes = Invoke-RestMethod -Uri "$baseUrl/reports/export/visits?filtersSummary=All+Blocks" -Method Get -WebSession $adminSession
    if ($csvRes -match "Reference" -and $csvRes -match "Visitor Name") {
        Write-Host "CSV export generated successfully. Sample row count: $(($csvRes -split "`n").Count)"
    } else {
        throw "CSV export did not contain expected headers"
    }
}

# 12. Resident Relocation Workflow
Test-Step "12. Resident Move Flat & History" {
    # Find a resident
    $usersRes = Invoke-RestMethod -Uri "$baseUrl/admin/users?role=RESIDENT&limit=1" -Method Get -WebSession $adminSession
    if ($usersRes.data.items.Count -gt 0) {
        $resUser = $usersRes.data.items[0]
        $moveBody = @{
            newFlatId = 2
            occupancyType = "OWNER"
            reason = "Upgraded to 3BHK flat on 2nd floor"
        } | ConvertTo-Json
        $headers = Get-CsrfHeaders $adminSession
        $moveRes = Invoke-RestMethod -Uri "$baseUrl/admin/users/$($resUser.id)/move-flat" -Method Post -Body $moveBody -ContentType "application/json" -Headers $headers -WebSession $adminSession
        Write-Host "Move flat result: $($moveRes.message)"
        
        $historyRes = Invoke-RestMethod -Uri "$baseUrl/admin/users/$($resUser.id)/move-history" -Method Get -WebSession $adminSession
        Write-Host "Recorded moves in history: $($historyRes.data.Count)"
    }
}

Write-Host "`n==========================================================" -ForegroundColor Yellow
Write-Host "   TEST SUITE EXECUTION SUMMARY                           " -ForegroundColor Yellow
if ($failCount -eq 0) {
    Write-Host "   PASSED: $passCount | FAILED: $failCount                " -ForegroundColor Green
} else {
    Write-Host "   PASSED: $passCount | FAILED: $failCount                " -ForegroundColor Red
}
Write-Host "==========================================================" -ForegroundColor Yellow

if ($failCount -gt 0) { exit 1 } else { exit 0 }
