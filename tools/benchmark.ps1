param(
    [string]$Image = "spring-hexagonal-payments",
    [int]$DurationSeconds = 10,
    [int]$VirtualUsers = 32,
    [int]$WarmupRequests = 200,
    [int]$Repeat = 1,
    [ValidatePattern("^[A-Za-z0-9._-]+$")]
    [string]$ResultName = "payments-baseline.json",
    [switch]$SkipBuild
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$results = Join-Path $root "benchmarks/results"
New-Item -ItemType Directory -Force -Path $results | Out-Null
$resolvedResults = (Resolve-Path -LiteralPath $results).Path

if (-not $SkipBuild) {
    docker build -t $Image $root
    if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }
}

docker run --rm `
    --mount "type=bind,source=$resolvedResults,target=/results" `
    -e "DURATION=${DurationSeconds}s" `
    -e "VUS=$VirtualUsers" `
    -e "WARMUP_REQUESTS=$WarmupRequests" `
    -e "REPEAT=$Repeat" `
    -e "RESULT_PATH=/results/$ResultName" `
    $Image
if ($LASTEXITCODE -ne 0) { throw "Docker benchmark failed" }
