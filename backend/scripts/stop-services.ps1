Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ports = 8080, 8081, 8082, 8083
$connections = Get-NetTCPConnection -LocalPort $ports -ErrorAction SilentlyContinue
$processIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique

if (-not $processIds) {
  Write-Host "No Xiangyun service ports are currently occupied."
  exit 0
}

foreach ($processId in $processIds) {
  Stop-Process -Id $processId -Force
  Write-Host ("Stopped process {0}" -f $processId)
}
