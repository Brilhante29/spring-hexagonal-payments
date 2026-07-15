#!/bin/sh
set -eu
export PGDATA="${PGDATA:-/tmp/payments-postgres}"
export DB_URL="jdbc:postgresql://127.0.0.1:5432/payments" DB_USER=postgres DB_PASSWORD=""
export BASE_URL=http://127.0.0.1:8080 DURATION="${DURATION:-10s}" VUS="${VUS:-32}"
export RESULT_PATH="${RESULT_PATH:-/tmp/payments-baseline.json}" COVERAGE_PERCENT="$(cat /app/coverage-percent.txt)"
cleanup() { if [ -n "${APP_PID:-}" ]; then kill "$APP_PID" 2>/dev/null || true; fi; pg_ctl -D "$PGDATA" -m fast stop >/dev/null 2>&1 || true; }
trap cleanup EXIT INT TERM
rm -rf "$PGDATA"
initdb -D "$PGDATA" --auth-local=trust --auth-host=trust >/dev/null 2>&1
pg_ctl -D "$PGDATA" -o "-F -p 5432 -c listen_addresses=127.0.0.1" -w start >/dev/null
createdb -h 127.0.0.1 -U postgres payments
java -jar /app/app.jar >/tmp/payments-app.log 2>&1 & APP_PID=$!
attempt=0
until wget -q -O /dev/null http://127.0.0.1:8080/actuator/health 2>/dev/null; do
  attempt=$((attempt + 1)); if [ "$attempt" -ge 60 ]; then cat /tmp/payments-app.log; exit 1; fi; sleep 1
done
mkdir -p "$(dirname "$RESULT_PATH")"
k6 run /app/k6.js
