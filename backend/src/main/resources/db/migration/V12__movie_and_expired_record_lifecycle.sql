-- Movie archival after the last Showtime ends, plus expired Seat Hold / refresh-token / Cron
-- history cleanup. All functions are idempotent and accept as_of for controlled-clock tests.

CREATE OR REPLACE FUNCTION cineflow.archive_eligible_movies(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    archived_count INTEGER := 0;
    movie_id BIGINT;
BEGIN
    FOR movie_id IN
        SELECT m.id
        FROM cineflow.movies m
        WHERE m.archived_at IS NULL
          AND EXISTS (
              SELECT 1
              FROM cineflow.showtimes s
              WHERE s.movie_id = m.id
                AND s.starts_at + make_interval(mins => m.runtime_minutes) <= as_of
          )
          AND NOT EXISTS (
              SELECT 1
              FROM cineflow.showtimes s
              WHERE s.movie_id = m.id
                AND s.starts_at + make_interval(mins => m.runtime_minutes) > as_of
          )
    LOOP
        UPDATE cineflow.movies
        SET archived_at = as_of
        WHERE id = movie_id
          AND archived_at IS NULL;

        IF FOUND THEN
            INSERT INTO cineflow.audit_events (
                occurred_at, actor_staff_id, action, subject_type, subject_id, correlation_id)
            VALUES (as_of, NULL, 'MOVIE_ARCHIVED', 'movie', movie_id::text, NULL);
            archived_count := archived_count + 1;
        END IF;
    END LOOP;

    RETURN archived_count;
END;
$function$;

-- Expired HOLD claims are already treated as available by application queries; this only
-- reduces storage and cannot change Booking correctness.
CREATE OR REPLACE FUNCTION cineflow.cleanup_expired_seat_holds(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    removed_holds INTEGER;
BEGIN
    DELETE FROM cineflow.seat_claims
    WHERE claim_kind = 'HOLD'
      AND expires_at <= as_of;

    DELETE FROM cineflow.seat_holds
    WHERE expires_at <= as_of;
    GET DIAGNOSTICS removed_holds = ROW_COUNT;

    RETURN removed_holds;
END;
$function$;

CREATE OR REPLACE FUNCTION cineflow.cleanup_expired_refresh_tokens(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    removed_tokens INTEGER;
BEGIN
    UPDATE cineflow.refresh_tokens
    SET replaced_by_id = NULL
    WHERE replaced_by_id IN (
        SELECT id FROM cineflow.refresh_tokens WHERE expires_at < as_of
    );

    DELETE FROM cineflow.refresh_tokens
    WHERE expires_at < as_of;
    GET DIAGNOSTICS removed_tokens = ROW_COUNT;

    RETURN removed_tokens;
END;
$function$;

-- Supabase retains Cron run history; prune entries older than seven days when pg_cron exists.
CREATE OR REPLACE FUNCTION cineflow.cleanup_cron_job_history(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    removed_rows INTEGER := 0;
BEGIN
    IF to_regclass('cron.job_run_details') IS NULL THEN
        RETURN 0;
    END IF;

    DELETE FROM cron.job_run_details
    WHERE end_time < as_of - interval '7 days';
    GET DIAGNOSTICS removed_rows = ROW_COUNT;
    RETURN removed_rows;
END;
$function$;

CREATE INDEX IF NOT EXISTS movies_pending_archival_idx
    ON cineflow.movies (id)
    WHERE archived_at IS NULL;

DO $schedule$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_cron') THEN
        CREATE EXTENSION IF NOT EXISTS pg_cron;
        PERFORM cron.schedule(
            'archive-eligible-movies',
            '23 * * * *',
            'SELECT cineflow.archive_eligible_movies()'
        );
        PERFORM cron.schedule(
            'cleanup-expired-seat-holds',
            '29 * * * *',
            'SELECT cineflow.cleanup_expired_seat_holds()'
        );
        PERFORM cron.schedule(
            'cleanup-expired-refresh-tokens',
            '37 * * * *',
            'SELECT cineflow.cleanup_expired_refresh_tokens()'
        );
        PERFORM cron.schedule(
            'cleanup-cron-job-history',
            '47 3 * * *',
            'SELECT cineflow.cleanup_cron_job_history()'
        );
    END IF;
END;
$schedule$;
