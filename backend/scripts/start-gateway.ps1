Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root
mvn -pl xiangyun-common -DskipTests install
Set-Location (Join-Path $Root "xiangyun-gateway")
mvn exec:java "-Dexec.mainClass=com.xiangyun.gateway.GatewayApplication"
