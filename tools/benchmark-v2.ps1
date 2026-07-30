param(
  [string]$Image = "spring-hexagonal-payments:benchmark",
  [int]$DurationSeconds = 10,
  [int]$VirtualUsers = 32,
  [int]$WarmupRequests = 200,
  [ValidateRange(1, 10)]
  [int]$Repeat = 3,
  [ValidateSet("local", "github-actions", "other-ci")]
  [string]$Producer = "local",
  [string]$CiRunUrl = "",
  [string]$OutputPath = "benchmarks/publication/payments-baseline-v2.json"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
  $sourceCommit = (& git rev-parse HEAD).Trim()
  $dirty = @(& git status --porcelain)
  if ($dirty.Count -gt 0) { throw "V2 benchmark requires a clean tree before execution." }
  if ($sourceCommit -notmatch "^[0-9a-f]{40}$") { throw "Could not resolve a full source commit." }
  if ($Producer -ne "local" -and [string]::IsNullOrWhiteSpace($CiRunUrl)) {
    throw "-CiRunUrl is required for a non-local producer."
  }

  function Get-RequiredProperty {
    param([object]$Object, [string]$Name)
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) { throw "Missing required benchmark property: $Name" }
    return $property.Value
  }

  function Get-CombinedDigest {
    param([string[]]$RelativePaths)
    $lines = foreach ($relative in ($RelativePaths | Sort-Object)) {
      $file = Join-Path $root $relative
      if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { throw "Digest input missing: $relative" }
      $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $file).Hash.ToLowerInvariant()
      "${relative}|${hash}"
    }
    $bytes = [Text.Encoding]::UTF8.GetBytes(($lines -join "`n") + "`n")
    $digest = [Security.Cryptography.SHA256]::Create().ComputeHash($bytes)
    return "sha256:" + (([BitConverter]::ToString($digest) -replace "-", "").ToLowerInvariant())
  }

  function Get-Median {
    param([double[]]$Values)
    $ordered = @($Values | Sort-Object)
    $middle = [int][Math]::Floor($ordered.Count / 2)
    if (($ordered.Count % 2) -eq 1) { return [double]$ordered[$middle] }
    return ([double]$ordered[$middle - 1] + [double]$ordered[$middle]) / 2
  }

  $fixtureDigest = Get-CombinedDigest @(
    "benchmarks/k6.js",
    "src/main/resources/db/migration/V1__create_payments.sql"
  )
  $configDigest = Get-CombinedDigest @(
    "Dockerfile",
    "build.gradle.kts",
    "gradle.lockfile",
    "tools/benchmark.ps1"
  )
  $lockHash = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $root "gradle.lockfile")).Hash.ToLowerInvariant()
  $startedAt = [DateTime]::UtcNow
  $timer = [Diagnostics.Stopwatch]::StartNew()

  & docker build -t $Image $root
  if ($LASTEXITCODE -ne 0) { throw "Docker image build failed." }
  $resultNames = New-Object System.Collections.Generic.List[string]
  $rawResults = New-Object System.Collections.Generic.List[object]
  for ($run = 1; $run -le $Repeat; $run++) {
    $resultName = if ($run -eq 1) { "payments-baseline.json" } elseif ($run -eq 2) { "payments-confirmation.json" } else { "payments-publication-run-$run.json" }
    $resultNames.Add($resultName)
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root "tools/benchmark.ps1") -Image $Image -DurationSeconds $DurationSeconds -VirtualUsers $VirtualUsers -WarmupRequests $WarmupRequests -Repeat $run -ResultName $resultName -SkipBuild
    if ($LASTEXITCODE -ne 0) { throw "Docker benchmark failed on repetition $run." }
    $resultPath = Join-Path $root "benchmarks/results/$resultName"
    $rawResults.Add((Get-Content -Raw -LiteralPath $resultPath | ConvertFrom-Json))
  }
  $timer.Stop()

  $p99Samples = @($rawResults | ForEach-Object { [double](Get-RequiredProperty $_.summary "p99_latency_ms") })
  $throughputSamples = @($rawResults | ForEach-Object { [double](Get-RequiredProperty $_.summary "throughput_rps") })
  $coverageSamples = @($rawResults | ForEach-Object { [double](Get-RequiredProperty $_.summary "coverage_percent") })
  $checksSamples = @($rawResults | ForEach-Object { [double](Get-RequiredProperty $_.summary "checks_rate") })
  $failureSamples = @($rawResults | ForEach-Object { [double](Get-RequiredProperty $_.summary "http_failure_rate") })
  $representative = $rawResults[0].summary
  $p99 = [Math]::Round((Get-Median $p99Samples), 3)
  $throughput = [Math]::Round((($throughputSamples | Measure-Object -Average).Average), 2)
  $coverage = [Math]::Round((($coverageSamples | Measure-Object -Minimum).Minimum), 2)
  $checks = [Math]::Round((($checksSamples | Measure-Object -Minimum).Minimum), 4)
  $httpFailures = [Math]::Round((($failureSamples | Measure-Object -Maximum).Maximum), 4)
  $measured = [int](Get-RequiredProperty $rawResults[0].summary "measured_requests")
  $warmup = [int](Get-RequiredProperty $rawResults[0].summary "warmup_requests")
  $imageDigest = (& docker image inspect --format "{{.Id}}" $Image).Trim()
  if ($imageDigest -notmatch "^sha256:[0-9a-f]{64}$") { throw "Docker did not return a content digest." }

  $runSummaries = @($rawResults | ForEach-Object {
    [ordered]@{
      p99_latency_ms = [double]$_.summary.p99_latency_ms
      throughput_rps = [double]$_.summary.throughput_rps
      measured_requests = [int]$_.summary.measured_requests
      coverage_percent = [double]$_.summary.coverage_percent
      checks_rate = [double]$_.summary.checks_rate
      http_failure_rate = [double]$_.summary.http_failure_rate
    }
  })
  $aggregateSummary = [ordered]@{
    aggregation = "median_p99_mean_throughput_min_coverage_min_checks_max_failures"
    repetitions = $Repeat
    p99_latency_ms = $p99
    throughput_rps = $throughput
    measured_requests_per_run = $measured
    warmup_requests_per_run = $warmup
    coverage_percent = $coverage
    checks_rate = $checks
    http_failure_rate = $httpFailures
    runs = $runSummaries
  }
  $failureCount = [int]($httpFailures -gt 0)
  $coverageFailure = [int]($coverage -lt 75)
  $checksFailure = [int]($checks -ne 1)
  $metrics = @(
    [ordered]@{ name = "p99_latency_ms"; value = $p99; unit = "milliseconds"; direction = "lower_is_better"; samples = @($p99Samples); failures = $failureCount; summary = $aggregateSummary },
    [ordered]@{ name = "throughput_rps"; value = $throughput; unit = "requests_per_second"; direction = "higher_is_better"; samples = @($throughputSamples); failures = $failureCount; summary = $aggregateSummary },
    [ordered]@{ name = "core_coverage_percent"; value = $coverage; unit = "percent"; direction = "target"; samples = @($coverageSamples); failures = $coverageFailure; summary = $aggregateSummary },
    [ordered]@{ name = "checks_rate"; value = $checks; unit = "ratio"; direction = "target"; samples = @($checksSamples); failures = $checksFailure; summary = $aggregateSummary },
    [ordered]@{ name = "http_failure_rate"; value = $httpFailures; unit = "ratio"; direction = "target"; samples = @($failureSamples); failures = $failureCount; summary = $aggregateSummary }
  )
  $artifactDigest = Get-CombinedDigest @($resultNames | ForEach-Object { "benchmarks/results/$_" })
  $provenance = [ordered]@{
    source_commit = $sourceCommit
    clean_tree = $true
    image_ref = $Image
    image_digest = $imageDigest
    dependency_lock_digest = "sha256:$lockHash"
    producer = $Producer
    artifact_digest = $artifactDigest
  }
  if ($CiRunUrl) { $provenance.ci_run_url = $CiRunUrl }
  $output = Join-Path $root $OutputPath
  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $output) | Out-Null
  $v2 = [ordered]@{
    schema_version = 2
    run_id = [guid]::NewGuid().ToString()
    project = "spring-hexagonal-payments"
    benchmark_id = "payments.authorization.v1"
    workload = [ordered]@{
      version = "1.0.0"
      fixture_digest = $fixtureDigest
      config_digest = $configDigest
      warmup_iterations = $warmup
      measured_iterations = $measured
      concurrency = $VirtualUsers
    }
    metrics = $metrics
    execution = [ordered]@{
      command = "powershell -NoProfile -ExecutionPolicy Bypass -File tools/benchmark-v2.ps1 -Image $Image -DurationSeconds $DurationSeconds -VirtualUsers $VirtualUsers -WarmupRequests $WarmupRequests -Repeat $Repeat"
      started_at = $startedAt.ToString("o")
      duration_seconds = [Math]::Round($timer.Elapsed.TotalSeconds, 3)
      exit_code = 0
      repeat = $Repeat
    }
    environment = [ordered]@{
      runtime = "Java 25, Kotlin 2.4.10, Spring Boot 4.1.0, PostgreSQL 18.4, k6 2.1.0"
      architecture = "Linux x86_64 Docker container"
      hardware_class = "local-docker"
    }
    provenance = $provenance
    comparability_key = "payments-authorization:1.0.0:spring-boot-4.1.0:jdbc-postgresql:k6-2.1.0:x86_64"
  }
  [IO.File]::WriteAllText((Join-Path $root $OutputPath),(($v2 | ConvertTo-Json -Depth 12) + [Environment]::NewLine),(New-Object Text.UTF8Encoding($false)))
  $v2 | ConvertTo-Json -Depth 12
  Write-Host "v2_result=$(Join-Path $root $OutputPath)"
  Write-Host "source_commit=$sourceCommit"
  Write-Host "image_digest=$imageDigest"
  Write-Host "artifact_digest=$artifactDigest"
} finally {
  Pop-Location
}