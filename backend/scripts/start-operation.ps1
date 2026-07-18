param(
  [ValidateSet("dev", "demo", "prod")]
  [string]$Profile = "demo"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$env:SPRING_PROFILES_ACTIVE = $Profile

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root
Write-Host ("Starting Operation with Spring profile: {0}" -f $Profile)
mvn -pl xiangyun-common -DskipTests install
Set-Location (Join-Path $Root "xiangyun-operation-service")
mvn clean compile exec:java "-Dexec.mainClass=com.xiangyun.operation.OperationServiceApplication"
