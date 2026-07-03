Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location (Join-Path $Root "xiangyun-auth-service")
mvn exec:java "-Dexec.mainClass=com.xiangyun.auth.AuthServiceApplication"
