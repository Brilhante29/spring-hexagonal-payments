#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
RESULTS="$ROOT/benchmarks/results"
IMAGE="${IMAGE:-spring-hexagonal-payments}"
DURATION_SECONDS="${DURATION_SECONDS:-10}"
VUS="${VUS:-32}"
WARMUP_REQUESTS="${WARMUP_REQUESTS:-200}"

mkdir -p "$RESULTS"
docker build -t "$IMAGE" "$ROOT"
docker run --rm \
  --mount "type=bind,source=$RESULTS,target=/results" \
  -e "DURATION=${DURATION_SECONDS}s" \
  -e "VUS=$VUS" \
  -e "WARMUP_REQUESTS=$WARMUP_REQUESTS" \
  -e "REPEAT=1" \
  -e "RESULT_PATH=/results/payments-baseline.json" \
  "$IMAGE"
