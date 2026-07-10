Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$targets = @(
  @{ Name = "Gateway"; Url = "http://127.0.0.1:8080/actuator/health" },
  @{ Name = "Auth"; Url = "http://127.0.0.1:8081/actuator/health" },
  @{ Name = "Operation"; Url = "http://127.0.0.1:8082/actuator/health" },
  @{ Name = "Analysis"; Url = "http://127.0.0.1:8083/actuator/health" }
)

foreach ($target in $targets) {
  try {
    $response = Invoke-RestMethod -Uri $target.Url -Method Get -TimeoutSec 5
    if ($response.status -eq "UP") {
      Write-Host ("[OK] {0} {1}" -f $target.Name, $target.Url) -ForegroundColor Green
    }
    else {
      Write-Host ("[WARN] {0} status={1}" -f $target.Name, $response.status) -ForegroundColor Yellow
    }
  }
  catch {
    Write-Host ("[FAIL] {0} {1}" -f $target.Name, $target.Url) -ForegroundColor Red
  }
}
