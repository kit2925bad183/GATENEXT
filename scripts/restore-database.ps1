param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    [string]$ContainerName = "gatenest-prod-db",
    [string]$DbName = "gatenest_db",
    [string]$DbUser = "gatenest_user",
    [string]$DbPassword = "gatenest_secure_pass_2026"
)

if (!(Test-Path $BackupFile)) {
    Write-Host "[ERROR] Backup file not found: $BackupFile" -ForegroundColor Red
    exit 1
}

Write-Host "WARNING: Restoring database will overwrite existing tables in $DbName." -ForegroundColor Yellow
Write-Host "Restoring $BackupFile into $ContainerName..." -ForegroundColor Cyan

Get-Content $BackupFile | docker exec -i $ContainerName mysql -u $DbUser -p$DbPassword $DbName

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Database restoration complete!" -ForegroundColor Green
} else {
    Write-Host "[FAILED] Restoration failed with code: $LASTEXITCODE" -ForegroundColor Red
}
