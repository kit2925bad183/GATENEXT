param(
    [string]$ContainerName = "gatenest-prod-db",
    [string]$DbName = "gatenest_db",
    [string]$DbUser = "gatenest_user",
    [string]$DbPassword = "gatenest_secure_pass_2026",
    [string]$BackupDir = "./backups"
)

$timestamp = (Get-Date).ToString("yyyyMMdd_HHmmss")
if (!(Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
}

$backupFile = Join-Path $BackupDir "gatenest_backup_$timestamp.sql"
Write-Host "Starting GateNest MySQL Backup -> $backupFile" -ForegroundColor Cyan

docker exec $ContainerName mysqldump -u $DbUser -p$DbPassword --single-transaction --routines --triggers $DbName > $backupFile

if ($LASTEXITCODE -eq 0 -and (Test-Path $backupFile)) {
    $size = (Get-Item $backupFile).Length
    Write-Host "[SUCCESS] Database backup completed successfully! ($size bytes)" -ForegroundColor Green
} else {
    Write-Host "[FAILED] Backup failed with exit code: $LASTEXITCODE" -ForegroundColor Red
}
