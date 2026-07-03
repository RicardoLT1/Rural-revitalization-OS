Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location (Join-Path $Root "xiangyun-operation-service")
mvn exec:java "-Dexec.mainClass=com.xiangyun.operation.OperationServiceApplication"
