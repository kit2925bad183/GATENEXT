param()

function Test-AccountLogin($email, $password) {
    try {
        $bodyObj = @{
            email = $email
            password = $password
        }
        $jsonBody = $bodyObj | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Body $jsonBody -ContentType "application/json"
        Write-Host "[SUCCESS] Logged in as: $email | Role: $($res.data.role) | Name: $($res.data.fullName)" -ForegroundColor Green
    } catch {
        $errResponse = $_.ErrorDetails.Message
        if (-not $errResponse) { $errResponse = $_.Exception.Message }
        Write-Host "[RESULT] $email => $errResponse" -ForegroundColor Yellow
    }
}

Write-Host "`n--- Testing Active Community Accounts ---" -ForegroundColor Cyan
Test-AccountLogin "admin@greenfield.com" "admin1234"
Test-AccountLogin "guard.north@greenfield.com" "guard1234"
Test-AccountLogin "resident.101@greenfield.com" "resident1234"
Test-AccountLogin "vishvaganesan123@gmail.com" "Password123!"

Write-Host "`n--- Testing Invalid Credentials Rejection ---" -ForegroundColor Cyan
Test-AccountLogin "unknown.user@example.com" "wrongpassword"
