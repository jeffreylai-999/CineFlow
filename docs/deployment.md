# Deploy CineFlow to Render and Supabase

CineFlow ships as **one Docker image**: Spring Boot serves the compiled React page and the REST interface from a single origin. PostgreSQL lives in Supabase, not on Render's ephemeral filesystem.

Pair **Render Singapore** with **Supabase `ap-southeast-1` (Singapore)** so Cinema Time (`Asia/Kuala_Lumpur`) stays close to both the app and the database.

## Secrets stay out of git

Do not commit database passwords, JDBC URLs with credentials, or `.env` files. Render Blueprint variables marked `sync: false` are entered in the dashboard and never written back to the repo.

| Variable | Production value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` (set by `render.yaml`) |
| `CINEFLOW_DATASOURCE_URL` | Session-pooler JDBC URL, port **5432**, `sslmode=require` |
| `CINEFLOW_DATASOURCE_USERNAME` | `postgres.<project-ref>` |
| `CINEFLOW_DATASOURCE_PASSWORD` | Database password from Supabase |
| `CINEFLOW_DB_POOL_SIZE` | `3` (must stay between 1 and 5) |
| `CINEFLOW_JWT_SECRET` | Unique HS256 secret, at least 32 bytes |
| `CINEFLOW_BOOTSTRAP_ADMIN_USERNAME` | Initial Administrator username |
| `CINEFLOW_BOOTSTRAP_ADMIN_PASSWORD` | Initial Administrator password, at least 12 characters |
| `CINEFLOW_AUTH_COOKIE_SECURE` | `true` (set by `render.yaml`) |
| `CINEFLOW_TMDB_ACCESS_TOKEN` | TMDB API Read Access Token. Stays in Spring Boot; never shipped to React |
| `CINEFLOW_OMDB_API_KEY` | OMDb API key. Stays in Spring Boot; never shipped to React |
| `PORT` | Set by Render; Spring Boot binds `server.port` to it |

The `prod` profile refuses to start if the JDBC URL uses the transaction pooler (`:6543`), omits TLS, or points at the IPv6-only direct host `db.<ref>.supabase.co`. Render's Free web services are IPv4-only, so the persistent-backend endpoint is Supavisor's **session** pooler.

Staff login, refresh, Seat Hold, Payment simulation, and Booking retrieval rate limits key on the client address derived from `X-Forwarded-For` at the configured trusted-proxy depth (`cineflow.http.trusted-proxy-depth`, default `1` for Render). Render appends the connecting client, so depth `1` selects the rightmost hop and ignores caller-controlled values to its left. Administrator Movie search is rate-limited per signed-in staff account.

Example URL shape (password is a separate env var, not embedded):

```text
jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
```

Take the real hostname from the Supabase **Connect** dialog's JDBC **Session pooler** tab. Do not use port `6543`.

## Create the Supabase project

1. Create a Free project named `CineFlow` in `ap-southeast-1`.
2. Save the database password in a password manager. Reset it from Database Settings if it is lost.
3. Leave application tables in the private `cineflow` schema. Flyway creates that schema on first boot; it is not exposed by Supabase's Data API (which defaults to `public`).
4. Do **not** schedule a demo-data reset. Public demonstration rows persist until someone changes them.
5. Keep an independent `pg_dump` of the `cineflow` schema. Free hosting is not a durability guarantee. Prove dump and restore locally with sanitized fixtures via `bash scripts/prove-backup-restore.sh` (see [`docs/release-verification.md`](release-verification.md)).

```bash
# Store the password in ~/.pgpass (chmod 0600), never in the command or shell history:
# hostname:port:database:username:password
# aws-0-ap-southeast-1.pooler.supabase.com:5432:postgres:postgres.<project-ref>:<password>

pg_dump "postgresql://postgres.<project-ref>@aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require" \
  --schema=cineflow --file=cineflow.dump
```

A `pg_dump --schema=cineflow` plain SQL file already contains `CREATE SCHEMA cineflow;`. Do **not** pre-create that schema before replay — with `ON_ERROR_STOP` the duplicate aborts the restore. `pg_dump --schema` also omits `CREATE EXTENSION` for `btree_gist` even though Flyway installs it into `cineflow`, so inject the extension immediately after the dump’s own schema creation (same approach as `scripts/prove-backup-restore.sh`):

```bash
awk '
  /^CREATE SCHEMA cineflow;/ {
    print
    print "CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA cineflow;"
    next
  }
  { print }
' cineflow.dump | psql "postgresql://postgres.<project-ref>@aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require" \
  -v ON_ERROR_STOP=1
```

## Apply the Render Blueprint

1. In the Render Dashboard, create a Blueprint from this repository. `render.yaml` defines a Free Docker web service named `cineflow` in Singapore, with readiness checks at `/actuator/health/readiness`.
2. When prompted, paste the three `CINEFLOW_DATASOURCE_*` values plus `CINEFLOW_JWT_SECRET`, the bootstrap Administrator username and password, `CINEFLOW_TMDB_ACCESS_TOKEN`, and `CINEFLOW_OMDB_API_KEY`. Leave them out of git. The Blueprint sets `CINEFLOW_AUTH_COOKIE_SECURE=true`.
3. Wait for the first deploy. Flyway applies all packaged migrations once (`V1__movie_catalog.sql` through `V12__movie_and_expired_record_lifecycle.sql`, including hall seat maps, catalog provider settings, Showtimes, the cinema-wide Booking Limit, Seat Holds, online checkout, email anonymization, counter sales, Admissions, and Movie lifecycle Cron jobs); later deploys reuse the same schema. Production does not seed Booking Staff; the first Administrator comes from the bootstrap secrets.
4. Confirm one HTTPS origin:
   - `https://<service>.onrender.com/` serves the Movie catalog page
   - `https://<service>.onrender.com/api/movies` returns Movies that have current or future Showtimes
   - `https://<service>.onrender.com/actuator/health/readiness` returns `{"status":"UP"}`

Auto-deploy runs when CI checks on the linked branch pass (`autoDeployTrigger: checksPass`).

Do not attach Render Free Postgres. That database expires after 30 days.

## Memory

Render Free provides **512 MB**. The image sets a container-aware heap (`-XX:MaxRAMPercentage=60.0`) and Serial GC. CI and `scripts/smoke-production-image.sh` boot the production image with a 512 MB cap, bind `PORT=18080`, and fail if the process is OOM-killed.

```bash
bash scripts/smoke-production-image.sh
```

## Cold start

A Free web service **spins down after 15 minutes** without inbound HTTP or WebSocket messages. The next request wakes it in about **one minute**. Render shows a loading page during that wake-up. Local files written inside the container are discarded on spin-down, restart, or deploy — only Supabase is durable.

These timings come from [Render's Free instance docs](https://render.com/docs/free). After the Blueprint is live, confirm a first request after idle shows that wake-up, then a ready Movie catalog page.

STOMP clients (later issues) must reconnect after a sleep. Movie catalog HTTP traffic is enough to wake the service.

## Restore a paused Supabase Free project

Supabase pauses a Free project that does not see enough database activity over **seven days**. The owner receives a warning email about a week before pause, then a confirmation email.

To restore (available for **one year** after pause):

1. Open the [Supabase Dashboard](https://supabase.com/dashboard).
2. Select the organization, then the paused **CineFlow** project.
3. Click **Resume project** and confirm.

The project returns with its data and configuration. Render will fail readiness until Postgres accepts connections again; the next inbound request after resume should reach a healthy app once Flyway validates the existing schema (it will not re-insert fixtures).

If the one-year window has passed, create a new project and restore from the independent `pg_dump`.

## Cron jobs

Database-only automation runs as idempotent PostgreSQL functions scheduled by [Supabase Cron](https://supabase.com/docs/guides/cron) (pg_cron). Each migration schedules its own job when the `pg_cron` extension is available, so local PostgreSQL and CI containers migrate cleanly without it.

| Job | Schedule | Function | Purpose |
| --- | --- | --- | --- |
| `anonymize-expired-booking-emails` | `17 * * * *` (hourly) | `cineflow.anonymize_expired_booking_emails()` | Replace the Online Customer email with `anonymized@cineflow.invalid` seven days after the Showtime. Booking, Payment, booked Seats, Ticket, and Admission history are preserved |
| `archive-eligible-movies` | `23 * * * *` (hourly) | `cineflow.archive_eligible_movies()` | Archive Movies that have at least one ended Showtime and no current or future Showtime. Writes a SYSTEM `MOVIE_ARCHIVED` Audit Event |
| `cleanup-expired-seat-holds` | `29 * * * *` (hourly) | `cineflow.cleanup_expired_seat_holds()` | Delete expired Seat Holds and HOLD claims. Availability already treats expired holds as free; cleanup only reduces storage |
| `cleanup-expired-refresh-tokens` | `37 * * * *` (hourly) | `cineflow.cleanup_expired_refresh_tokens()` | Delete refresh tokens past `expires_at` |
| `cleanup-cron-job-history` | `47 3 * * *` (daily) | `cineflow.cleanup_cron_job_history()` | Remove `cron.job_run_details` rows older than seven days |

These jobs are database-only. Edge Functions stay reserved for automation that needs outbound HTTP, with call secrets stored in Supabase Vault. No job resets public demonstration data.

Inspect job definitions and run history in the `cron` schema:

```sql
select jobid, jobname, schedule, command from cron.job;
select jobid, status, return_message, start_time, end_time
from cron.job_run_details
order by start_time desc
limit 20;
```

Run a function manually (for example after restoring a paused project) with:

```sql
select cineflow.anonymize_expired_booking_emails();
select cineflow.archive_eligible_movies();
select cineflow.cleanup_expired_seat_holds();
select cineflow.cleanup_expired_refresh_tokens();
select cineflow.cleanup_cron_job_history();
```

Each function takes an optional `as_of TIMESTAMPTZ` argument (defaulting to `now()`) so retention boundaries can be exercised in tests. They return the number of rows affected and are idempotent: repeated runs change nothing further.

TMDB descriptive metadata must be refreshed within six months. The Administrator Movies page warns during the final thirty days so Staff can refresh from the original source or archive the Movie.

## Local stand-in

```bash
docker compose up --build
```

Local Compose uses PostgreSQL 16 without TLS and without the `prod` profile. That is intentional: the production guard would reject a non-pooler URL.
