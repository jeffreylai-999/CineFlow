#!/usr/bin/env bash
# Starts the production image under Render Free's 512 MB cap, proves PORT binding,
# one-origin SPA + REST, and that catalog fixtures survive an application restart.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

compose=(docker compose -p cineflow-smoke -f docker-compose.yml -f docker-compose.smoke.yml)
export CINEFLOW_IMAGE="${CINEFLOW_IMAGE:-cineflow:smoke}"
base_url="${CINEFLOW_SMOKE_URL:-http://127.0.0.1:18080}"

cleanup() {
  "${compose[@]}" down --remove-orphans --volumes >/dev/null 2>&1 || true
}
trap cleanup EXIT

if ! docker image inspect "$CINEFLOW_IMAGE" >/dev/null 2>&1; then
  docker build -t "$CINEFLOW_IMAGE" .
fi

"${compose[@]}" up -d --no-build db app

wait_for() {
  local path="$1"
  local attempt
  for attempt in $(seq 1 90); do
    if curl -fsS "${base_url}${path}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for ${base_url}${path}" >&2
  "${compose[@]}" logs app >&2 || true
  return 1
}

wait_for /actuator/health/readiness

readiness="$(curl -fsS "${base_url}/actuator/health/readiness")"
printf '%s' "$readiness" | grep -q '"status":"UP"'

html="$(curl -fsS "${base_url}/")"
printf '%s' "$html" | grep -q '<div id="root">'

movies="$(curl -fsS "${base_url}/api/movies")"
printf '%s' "$movies" | grep -q 'Nebula Express'

app_id="$("${compose[@]}" ps -q app)"
oom="$(docker inspect -f '{{.State.OOMKilled}}' "$app_id")"
if [ "$oom" != "false" ]; then
  echo "Application container was OOM-killed inside the 512 MB limit" >&2
  exit 1
fi

"${compose[@]}" restart app
wait_for /actuator/health/readiness
movies_after="$(curl -fsS "${base_url}/api/movies")"
printf '%s' "$movies_after" | grep -q 'Nebula Express'

echo "Production image stayed healthy on PORT 18080 within 512 MB; catalog data survived restart."
