#!/usr/bin/env bash
# Proves logical backup and manual restore of the sanitized cineflow schema.
# Starts an empty PostgreSQL, applies Flyway migrations (including fixtures),
# dumps the schema, drops it, restores the dump, and checks fixture rows.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

project="cineflow-backup-proof"
container="${project}-db"
port="${CINEFLOW_BACKUP_PROOF_PORT:-55433}"
dump_dir="$(mktemp -d "${TMPDIR:-/tmp}/cineflow-backup.XXXXXX")"
dump_file="${dump_dir}/cineflow.dump.sql"

cleanup() {
  docker rm -f "$container" >/dev/null 2>&1 || true
  rm -rf "$dump_dir"
}
trap cleanup EXIT

docker rm -f "$container" >/dev/null 2>&1 || true
docker run -d --name "$container" \
  -e POSTGRES_DB=cineflow \
  -e POSTGRES_USER=cineflow \
  -e POSTGRES_PASSWORD=cineflow \
  -p "${port}:5432" \
  postgres:16-alpine >/dev/null

echo "Waiting for PostgreSQL on localhost:${port}…"
for _ in $(seq 1 60); do
  if docker exec "$container" pg_isready -U cineflow -d cineflow >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
docker exec "$container" pg_isready -U cineflow -d cineflow >/dev/null

echo "Applying Flyway migrations to an empty database…"
docker run --rm --add-host=host.docker.internal:host-gateway \
  -v "${root}/backend/src/main/resources/db/migration:/flyway/sql/migration:ro" \
  -v "${root}/backend/src/main/resources/db/dev:/flyway/sql/dev:ro" \
  flyway/flyway:11-alpine \
  -url="jdbc:postgresql://host.docker.internal:${port}/cineflow" \
  -user=cineflow \
  -password=cineflow \
  -schemas=cineflow \
  -defaultSchema=cineflow \
  -createSchemas=true \
  -locations=filesystem:/flyway/sql/migration,filesystem:/flyway/sql/dev \
  migrate

movie_count="$(docker exec -e PGPASSWORD=cineflow "$container" \
  psql -U cineflow -d cineflow -Atc "select count(*) from cineflow.movies where title = 'Nebula Express'")"
if [ "$movie_count" != "1" ]; then
  echo "Expected sanitized Nebula Express fixture after migrate; got count=${movie_count}" >&2
  exit 1
fi

echo "Dumping cineflow schema…"
docker exec -e PGPASSWORD=cineflow "$container" \
  pg_dump -U cineflow -d cineflow --schema=cineflow --no-owner --no-privileges \
  >"$dump_file"

if grep -Eiq 'gmail\.com|hotmail\.com|password[[:space:]]*=|[[:digit:]]{3}-[[:digit:]]{2}-[[:digit:]]{4}' "$dump_file"; then
  echo "Dump appears to contain personal-looking or credential-like data" >&2
  exit 1
fi

echo "Dropping schema and restoring from dump…"
docker exec -e PGPASSWORD=cineflow "$container" \
  psql -U cineflow -d cineflow -v ON_ERROR_STOP=1 -c "drop schema cineflow cascade;"

# pg_dump --schema omits CREATE EXTENSION for btree_gist even though Flyway installs it
# into cineflow; recreate it immediately after the schema so gist exclusion restores.
awk '
  /^CREATE SCHEMA cineflow;/ {
    print
    print "CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA cineflow;"
    next
  }
  { print }
' "$dump_file" | docker exec -i -e PGPASSWORD=cineflow "$container" \
  psql -U cineflow -d cineflow -v ON_ERROR_STOP=1

restored="$(docker exec -e PGPASSWORD=cineflow "$container" \
  psql -U cineflow -d cineflow -Atc "select count(*) from cineflow.movies where title = 'Nebula Express'")"
if [ "$restored" != "1" ]; then
  echo "Restore failed: Nebula Express missing (count=${restored})" >&2
  exit 1
fi

flyway_rows="$(docker exec -e PGPASSWORD=cineflow "$container" \
  psql -U cineflow -d cineflow -Atc "select count(*) from cineflow.flyway_schema_history")"
if [ "$flyway_rows" -lt 1 ]; then
  echo "Restore failed: flyway_schema_history empty" >&2
  exit 1
fi

echo "Backup and manual restore proved for sanitized cineflow schema (Nebula Express fixture present)."
