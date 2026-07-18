Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$backendRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$databaseServices = @(
  "xiangyun-auth-service",
  "xiangyun-operation-service",
  "xiangyun-analysis-service"
)

foreach ($service in $databaseServices) {
  $resources = Join-Path $backendRoot "$service/src/main/resources"
  $baseConfig = Get-Content -Raw -LiteralPath (Join-Path $resources "application.yml")
  if ($baseConfig -notmatch "(?m)^\s+baseline-version:\s+0\s*$") {
    throw "$service must use Flyway baseline-version 0 for shared-schema cold starts."
  }

  $migrationRoot = Join-Path $resources "db/migration"
  $unexpectedSeed = Get-ChildItem -LiteralPath $migrationRoot -File |
    Where-Object { $_.Name -match "(?i)(seed|demo)" }
  if ($unexpectedSeed) {
    throw "$service has demo seed files in the structural migration location: $($unexpectedSeed.Name -join ', ')"
  }

  $demoRoot = Join-Path $resources "db/demo"
  if (-not (Test-Path -LiteralPath $demoRoot)) {
    throw "$service is missing its isolated db/demo location."
  }

  $demoConfig = Get-Content -Raw -LiteralPath (Join-Path $resources "application-demo.yml")
  if (-not $demoConfig.Contains("classpath:db/migration") -or -not $demoConfig.Contains("classpath:db/demo")) {
    throw "$service demo profile must resolve both structural migrations and demo seeds."
  }
  if (-not $demoConfig.Contains('on-profile: "demo & !prod"')) {
    throw "$service demo profile must be disabled whenever the prod profile is active."
  }

  foreach ($profile in @("dev", "test", "prod")) {
    $profileConfig = Get-Content -Raw -LiteralPath (Join-Path $resources "application-$profile.yml")
    if ($profileConfig.Contains("classpath:db/demo")) {
      throw "$service $profile profile must not resolve demo seeds."
    }
  }
}

$requiredProdVariables = @{
  "xiangyun-auth-service" = @(
    "MYSQL_URL", "MYSQL_USER", "MYSQL_PASSWORD", "REDIS_HOST", "REDIS_PORT",
    "REDIS_PASSWORD", "NACOS_ADDR", "NACOS_NAMESPACE", "JWT_SECRET",
    "XIANGYUN_INTERNAL_SECRET"
  )
  "xiangyun-operation-service" = @(
    "MYSQL_URL", "MYSQL_USER", "MYSQL_PASSWORD", "REDIS_HOST", "REDIS_PORT",
    "REDIS_PASSWORD", "RABBITMQ_HOST", "RABBITMQ_PORT", "RABBITMQ_USERNAME",
    "RABBITMQ_PASSWORD", "NACOS_ADDR", "NACOS_NAMESPACE",
    "XIANGYUN_INTERNAL_SECRET", "XIANGYUN_UPLOAD_ROOT"
  )
  "xiangyun-analysis-service" = @(
    "MYSQL_URL", "MYSQL_USER", "MYSQL_PASSWORD", "REDIS_HOST", "REDIS_PORT",
    "REDIS_PASSWORD", "RABBITMQ_HOST", "RABBITMQ_PORT", "RABBITMQ_USERNAME",
    "RABBITMQ_PASSWORD", "NACOS_ADDR", "NACOS_NAMESPACE",
    "XIANGYUN_INTERNAL_SECRET"
  )
  "xiangyun-gateway" = @(
    "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD", "NACOS_ADDR",
    "NACOS_NAMESPACE", "JWT_SECRET", "XIANGYUN_INTERNAL_SECRET"
  )
}

foreach ($service in $requiredProdVariables.Keys) {
  $prodConfigPath = Join-Path $backendRoot "$service/src/main/resources/application-prod.yml"
  $prodConfig = Get-Content -Raw -LiteralPath $prodConfigPath
  foreach ($variable in $requiredProdVariables[$service]) {
    $requiredPlaceholder = '${' + $variable + '}'
    if (-not $prodConfig.Contains($requiredPlaceholder)) {
      throw "$service prod profile must require $requiredPlaceholder without a repository fallback."
    }
  }
}

foreach ($scriptName in @("start-auth.ps1", "start-operation.ps1", "start-analysis.ps1", "start-gateway.ps1")) {
  $startScript = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot $scriptName)
  if ($startScript -notmatch '\[string\]\$Profile\s*=\s*"demo"') {
    throw "$scriptName must default to the demo profile used by docker-compose.demo.yml."
  }
  if ($startScript -notmatch 'mvn clean compile exec:java') {
    throw "$scriptName must clean stale resources before starting the service."
  }
}

Write-Host "[OK] Spring profile, Flyway seed, production-secret, and cold-start boundaries are consistent." -ForegroundColor Green
