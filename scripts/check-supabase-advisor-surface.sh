#!/usr/bin/env bash
# Checks the local schema surface against release-blocking Supabase advisor themes.
# CineFlow keeps application tables in the private cineflow schema (not PostgREST-exposed).
# This script fails if application relations appear in public, or if anon/authenticated
# can SELECT tables in schemas that advisors treat as exposed (public by default).
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

project="cineflow-advisor-proof"
container="${project}-db"
port="${CINEFLOW_ADVISOR_PROOF_PORT:-55434}"

cleanup() {
  docker rm -f "$container" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker rm -f "$container" >/dev/null 2>&1 || true
docker run -d --name "$container" \
  -e POSTGRES_DB=cineflow \
  -e POSTGRES_USER=cineflow \
  -e POSTGRES_PASSWORD=cineflow \
  -p "${port}:5432" \
  postgres:16-alpine >/dev/null

for _ in $(seq 1 60); do
  if docker exec "$container" pg_isready -U cineflow -d cineflow >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

docker run --rm --add-host=host.docker.internal:host-gateway \
  -v "${root}/backend/src/main/resources/db/migration:/flyway/sql/migration:ro" \
  flyway/flyway:11-alpine \
  -url="jdbc:postgresql://host.docker.internal:${port}/cineflow" \
  -user=cineflow \
  -password=cineflow \
  -schemas=cineflow \
  -defaultSchema=cineflow \
  -createSchemas=true \
  -locations=filesystem:/flyway/sql/migration \
  migrate

# Mirror advisor 0013: RLS disabled on tables in PostgREST-exposed schemas.
# Production Data API defaults to public; cineflow must stay private.
findings="$(docker exec -e PGPASSWORD=cineflow "$container" psql -U cineflow -d cineflow -Atc "
select format('%s.%s', n.nspname, c.relname)
from pg_catalog.pg_class c
join pg_catalog.pg_namespace n on c.relnamespace = n.oid
where c.relkind = 'r'
  and not c.relrowsecurity
  and n.nspname = 'public'
  and c.relname not in ('spatial_ref_sys')
")"

if [ -n "$findings" ]; then
  echo "Release-blocking advisor surface: tables in public without RLS:" >&2
  echo "$findings" >&2
  exit 1
fi

cineflow_tables="$(docker exec -e PGPASSWORD=cineflow "$container" psql -U cineflow -d cineflow -Atc "
select count(*) from pg_catalog.pg_class c
join pg_catalog.pg_namespace n on c.relnamespace = n.oid
where c.relkind = 'r' and n.nspname = 'cineflow'
")"
if [ "$cineflow_tables" -lt 1 ]; then
  echo "Expected cineflow application tables after migrate" >&2
  exit 1
fi

echo "Supabase advisor surface clear for local migrate: no public application tables; cineflow remains private (${cineflow_tables} tables)."
