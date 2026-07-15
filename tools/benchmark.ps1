param(
    [string]$Image = "spring-hexagonal-payments",
    [int]$DurationSeconds = 10,
    [int]$VirtualUsers = 32,
    [int]$WarmupRequests = 200
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$results = Join-Path $root "benchmarks/results"
New-Item -ItemType Directory -Force -Path $results | Out-Null
$resolvedResults = (Resolve-Path -LiteralPath $results).Path

docker build -t $Image $root
if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }

docker run --rm `
    --mount "type=bind,source=$resolvedResults,target=/results" `
    -e "DURATION=${DurationSeconds}s" `
    -e "VUS=$VirtualUsers" `
    -e "WARMUP_REQUESTS=$WarmupRequests" `
    -e "REPEAT=1" `
    -e "RESULT_PATH=/results/payments-baseline.json" `
    $Image
if ($LASTEXITCODE -ne 0) { throw "Docker benchmark failed" }
