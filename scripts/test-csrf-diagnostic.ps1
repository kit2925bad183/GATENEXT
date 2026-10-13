param()

$baseUrl = "http://localhost:8080"
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 1. Login
$loginBody = @{ email = "vishvaganesan123@gmail.com"; password = "Password123!" } | ConvertTo-Json
$res = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $session
Write-Host "Logged in as: $($res.data.fullName)"

# 2. Inspect Cookies
$allCookies = $session.Cookies.GetCookies((New-Object System.Uri($baseUrl)))
Write-Host "Cookies count: $($allCookies.Count)"
foreach ($c in $allCookies) {
    Write-Host "Cookie: $($c.Name) = $($c.Value)"
}

$csrfCookie = $allCookies | Where-Object { $_.Name -eq "XSRF-TOKEN" }
$headers = @{}
if ($csrfCookie) {
    $headers["X-XSRF-TOKEN"] = $csrfCookie.Value
    Write-Host "Using CSRF Token: $($csrfCookie.Value)" -ForegroundColor Green
} else {
    Write-Host "WARNING: No XSRF-TOKEN cookie found in session!" -ForegroundColor Red
}

# 3. Create pre-invitation
$visitBody = @{
    visitorName = "Kavitha R"
    visitorMobile = "+919876500099"
    visitorCategory = "GUEST"
    purpose = "Family Gathering"
    arrivalWindowStart = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    arrivalWindowEnd = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    expectedDurationMinutes = 120
} | ConvertTo-Json

try {
    $visitRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/visits/pre-invitation" -Method Post -Body $visitBody -ContentType "application/json" -Headers $headers -WebSession $session
    Write-Host "SUCCESS! Created visit pass: $($visitRes.data.visitReference)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) { Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red }
}
