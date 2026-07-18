Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
& (Join-Path $PSScriptRoot "check-profile-separation.ps1")
Set-Location $Root
$env:SPRING_PROFILES_ACTIVE = "test"
mvn clean test
