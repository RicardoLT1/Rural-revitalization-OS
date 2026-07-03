Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ports = @(
  @{ Name = "MySQL"; Port = 3307 },
  @{ Name = "Redis"; Port = 6379 },
  @{ Name = "Nacos"; Port = 8848 }
)

foreach ($item in $ports) {
  $connection = Test-NetConnection -ComputerName 127.0.0.1 -Port $item.Port -WarningAction SilentlyContinue
  if ($connection.TcpTestSucceeded) {
    Write-Host ("[OK] {0} 127.0.0.1:{1}" -f $item.Name, $item.Port) -ForegroundColor Green
  }
  else {
    Write-Host ("[FAIL] {0} 127.0.0.1:{1}" -f $item.Name, $item.Port) -ForegroundColor Red
  }
}
