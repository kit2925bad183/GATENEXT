param()

$baseUrl = "http://localhost:8080"
$residentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$guardSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Get-CsrfHeader($sessionObj) {
    $cookies = $sessionObj.Cookies.GetCookies((New-Object System.Uri($baseUrl)))
    $csrf = $cookies | Where-Object { $_.Name -eq "XSRF-TOKEN" }
    if ($csrf) {
        return @{ "X-XSRF-TOKEN" = $csrf.Value }
    }
    return @{}
}

function Test-Step($name, [scriptblock]$action) {
    Write-Host "`n>>> TESTING: $name" -ForegroundColor Cyan
    try {
        & $action
        Write-Host "[PASSED] $name" -ForegroundColor Green
    } catch {
        $msg = $_.ErrorDetails.Message
        if (-not $msg) { $msg = $_.Exception.Message }
        Write-Host "[FAILED] $name => $msg" -ForegroundColor Red
        throw $_
    }
}

$residentUser = $null
$passToken = $null
$visitRef = $null
$createdVisitId = $null
$createdParcelId = $null

# 1. Login as Verified Resident
Test-Step "1. Resident Authentication (POST /api/v1/auth/login)" {
    $loginBody = @{ email = "vishvaganesan123@gmail.com"; password = "Password123!" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $residentSession
    $script:residentUser = $res.data
    Write-Host "Logged in as: $($res.data.fullName) (User ID: $($res.data.id), Flat ID: $($res.data.flatId), Role: $($res.data.role))"
}

# 2. Resident Creates Pre-Invitation Pass
Test-Step "2. Pre-Invite Visitor Pass (POST /api/v1/resident/invitations)" {
    $headers = Get-CsrfHeader $residentSession
    $visitBody = @{
        visitorName = "Kavitha Raman"
        visitorMobile = "+919876500099"
        visitorCategory = "GUEST"
        purpose = "Family Dinner"
        arrivalWindowStart = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssZ")
        arrivalWindowEnd = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ssZ")
        expectedDurationMinutes = 120
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/resident/invitations" -Method Post -Body $visitBody -ContentType "application/json" -Headers $headers -WebSession $residentSession
    $script:passToken = $res.data.passToken
    $script:visitRef = $res.data.visitReference
    Write-Host "Created Pass: Token = $($script:passToken), Ref = $($script:visitRef)"
}

# 3. Resident Saves Frequent Contact
Test-Step "3. Save Frequent Visitor (POST /api/v1/resident/saved-contacts)" {
    $headers = Get-CsrfHeader $residentSession
    $contactBody = @{
        visitorName = "Kavitha Raman"
        visitorMobile = "+919876500099"
        visitorCategory = "GUEST"
        defaultPurpose = "Family Dinner"
        notes = "Sister in law"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/resident/saved-contacts" -Method Post -Body $contactBody -ContentType "application/json" -Headers $headers -WebSession $residentSession
    Write-Host "Saved frequent contact ID: $($res.data.id) for $($res.data.visitorName)"
}

# 4. Resident Submits Maintenance Complaint
Test-Step "4. File Maintenance Ticket (POST /api/v1/complaints)" {
    $headers = Get-CsrfHeader $residentSession
    $complaintBody = @{
        category = "ELECTRICAL"
        subject = "Corridor light fixture flickering"
        description = "Fixture outside Flat 102 needs replacement bulb"
        locationDetails = "Block A Floor 1 Corridor"
        priority = "MEDIUM"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/complaints" -Method Post -Body $complaintBody -ContentType "application/json" -Headers $headers -WebSession $residentSession
    Write-Host "Complaint filed: ID = $($res.data.id), Reference = $($res.data.referenceNumber), Status = $($res.data.status)"
}

# 5. Resident Triggers Security Assistance
Test-Step "5. Security Assistance Trigger (POST /api/v1/assistance)" {
    $headers = Get-CsrfHeader $residentSession
    $alertBody = @{
        category = "GENERAL"
        locationDetails = "Block A Flat 102"
        description = "Need security assistance with oversized package delivery"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/assistance" -Method Post -Body $alertBody -ContentType "application/json" -Headers $headers -WebSession $residentSession
    Write-Host "Assistance alert logged: ID = $($res.data.id), Status = $($res.data.status)"
}

# 6. Security Guard Authentication
Test-Step "6. Security Guard Authentication (POST /api/v1/auth/login)" {
    $guardLogin = @{ email = "guard.north@greenfield.com"; password = "guard1234" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $guardLogin -ContentType "application/json" -WebSession $guardSession
    Write-Host "Logged in as: $($res.data.fullName) | Role: $($res.data.role)"
}

# 7. Security Verifies Pass Code & Checks In Visitor
Test-Step "7. Guard Scans Pass & Checks In Visitor" {
    $guardHeaders = Get-CsrfHeader $guardSession
    $verifyBody = @{ code = $script:passToken } | ConvertTo-Json
    $verifyRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/security/verify-pass" -Method Post -Body $verifyBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    $script:createdVisitId = $verifyRes.data.visit.id
    Write-Host "Pass Verified: Valid=$($verifyRes.data.valid), Visitor=$($verifyRes.data.visit.visitorName), VisitId=$($script:createdVisitId)"

    $checkinBody = @{ gateId = 1; notes = "Verified and admitted via North Gate" } | ConvertTo-Json
    $checkinRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/security/check-in/$script:createdVisitId" -Method Post -Body $checkinBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    Write-Host "Visitor Checked In: Status = $($checkinRes.data.status)"
}

# 8. Security Checks Out Visitor
Test-Step "8. Guard Checks Out Visitor (POST /api/v1/security/check-out)" {
    $guardHeaders = Get-CsrfHeader $guardSession
    $checkoutBody = @{ gateId = 1; notes = "Exited safely via North Gate" } | ConvertTo-Json
    $checkoutRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/security/check-out/$script:createdVisitId" -Method Post -Body $checkoutBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    Write-Host "Visitor Checked Out: Status = $($checkoutRes.data.status)"
}

# 9. Security Receives Inbound Parcel for Resident
Test-Step "9. Parcel Intake & Logging (POST /api/v1/parcels)" {
    $guardHeaders = Get-CsrfHeader $guardSession
    $parcelBody = @{
        recipientUserId = $script:residentUser.id
        destinationFlatId = $script:residentUser.flatId
        courierName = "BlueDart Express"
        trackingNumber = "BD-998877221"
        description = "Electronics Box"
        storageLocation = "Locker Bay B, Slot 4"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/parcels" -Method Post -Body $parcelBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    $script:createdParcelId = $res.data.id
    Write-Host "Parcel Registered: ID = $($res.data.id), Status = $($res.data.status), Location = $($res.data.storageLocation)"
}

# 10. Resident Delegates Parcel Pickup
Test-Step "10. Resident Delegates Parcel (POST /api/v1/parcels/{id}/delegate)" {
    $headers = Get-CsrfHeader $residentSession
    $delegateBody = @{ delegateName = "Neighbor Suresh"; notes = "Authorized to collect on my behalf" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/parcels/$script:createdParcelId/delegate" -Method Post -Body $delegateBody -ContentType "application/json" -Headers $headers -WebSession $residentSession
    Write-Host "Parcel Delegated: Delegate = $($res.data.delegateCollectorName)"
}

# 11. Security Delivers/Collects Parcel
Test-Step "11. Security Marks Parcel Collected (POST /api/v1/parcels/{id}/collect)" {
    $guardHeaders = Get-CsrfHeader $guardSession
    $collectBody = @{ collectorName = "Neighbor Suresh" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/parcels/$script:createdParcelId/collect" -Method Post -Body $collectBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    Write-Host "Parcel Status: $($res.data.status)"
}

# 12. Security Handover Log
Test-Step "12. Shift Handover Note (POST /api/v1/security/handover)" {
    $guardHeaders = Get-CsrfHeader $guardSession
    $noteBody = @{
        shiftName = "Evening Shift"
        gateId = 1
        noteText = "North Gate automatic boom barrier lubricated. All visitor lanes operational."
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/security/handover" -Method Post -Body $noteBody -ContentType "application/json" -Headers $guardHeaders -WebSession $guardSession
    Write-Host "Handover note logged: ID = $($res.data.id), Shift = $($res.data.shiftName)"
}

# 13. Admin Login & System Overview
Test-Step "13. Admin Authentication & Reports (GET /api/v1/reports/dashboard)" {
    $adminLogin = @{ email = "admin@greenfield.com"; password = "admin1234" } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $adminLogin -ContentType "application/json" -WebSession $adminSession
    Write-Host "Admin Logged In: $($res.data.fullName)"

    $dash = Invoke-RestMethod -Uri "$baseUrl/api/v1/reports/dashboard" -Method Get -WebSession $adminSession
    Write-Host "Dashboard metrics loaded successfully. Total Visits: $($dash.data.totalVisits), Inside Now: $($dash.data.activeInsideCount)"
}

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host "ALL 13 FULL-STACK END-TO-END VERIFICATIONS PASSED!" -ForegroundColor Green
Write-Host "=======================================================`n" -ForegroundColor Green
