-- Movie archival after the last Showtime ends, plus expired Seat Hold / refresh-token / Cron
-- history cleanup. All functions are idempotent and accept as_of for controlled-clock tests.

CREATE OR REPLACE FUNCTION cineflow.archive_eligible_movies(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    archived_count INTEGER := 0;
    candidate_id BIGINT;
    runtime INTEGER;
BEGIN
    -- Candidate scan is unlocked; each archive serializes on the Movie row with the same
    -- FOR UPDATE point used by reject_showtime_on_archived_movie, then re-checks eligibility.
    FOR candidate_id IN
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
        SELECT runtime_minutes INTO runtime
        FROM cineflow.movies
        WHERE id = candidate_id
          AND archived_at IS NULL
        FOR UPDATE;

        IF NOT FOUND THEN
            CONTINUE;
        END IF;

        IF NOT EXISTS (
              SELECT 1
              FROM cineflow.showtimes s
              WHERE s.movie_id = candidate_id
                AND s.starts_at + make_interval(mins => runtime) <= as_of
          )
          OR EXISTS (
              SELECT 1
              FROM cineflow.showtimes s
              WHERE s.movie_id = candidate_id
                AND s.starts_at + make_interval(mins => runtime) > as_of
          )
        THEN
            CONTINUE;
        END IF;

        UPDATE cineflow.movies
        SET archived_at = as_of
        WHERE id = candidate_id
          AND archived_at IS NULL;

        IF FOUND THEN
            INSERT INTO cineflow.audit_events (
                occurred_at, actor_staff_id, action, subject_type, subject_id, correlation_id)
            VALUES (as_of, NULL, 'MOVIE_ARCHIVED', 'movie', candidate_id::text, NULL);
            archived_count := archived_count + 1;
        END IF;
    END LOOP;

    RETURN archived_count;
END;
$function$;

-- Expired HOLD claims are already treated as available by application queries; this only
-- reduces storage and cannot change Booking correctness.
-- Lock expired holds first (same order as BookingService.requireHeldSeats), then delete
-- their claims and holds so this job cannot deadlock checkout.
CREATE OR REPLACE FUNCTION cineflow.cleanup_expired_seat_holds(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    removed_holds INTEGER;
    expired_hold_ids UUID[];
BEGIN
    SELECT coalesce(array_agg(id), ARRAY[]::UUID[])
    INTO expired_hold_ids
    FROM (
        SELECT id
        FROM cineflow.seat_holds
        WHERE expires_at <= as_of
        FOR UPDATE
    ) locked;

    DELETE FROM cineflow.seat_claims
    WHERE claim_kind = 'HOLD'
      AND (
          hold_id = ANY (expired_hold_ids)
          OR (hold_id IS NULL AND expires_at <= as_of)
      );

    DELETE FROM cineflow.seat_holds
    WHERE id = ANY (expired_hold_ids);
    GET DIAGNOSTICS removed_holds = ROW_COUNT;

    RETURN removed_holds;
END;
$function$;

-- Retain expired refresh-token rows while any unexpired token remains in the family so
-- IdentityService.refresh can still detect reuse of rotated tombstones (issue #17:
-- cleanup must not affect correctness).
CREATE OR REPLACE FUNCTION cineflow.cleanup_expired_refresh_tokens(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    removed_tokens INTEGER;
BEGIN
    WITH deletable AS (
        SELECT t.id
        FROM cineflow.refresh_tokens t
        WHERE t.expires_at < as_of
          AND NOT EXISTS (
              SELECT 1
              FROM cineflow.refresh_tokens live
              WHERE live.family_id = t.family_id
                AND live.expires_at >= as_of
          )
    )
    UPDATE cineflow.refresh_tokens
    SET replaced_by_id = NULL
    WHERE replaced_by_id IN (SELECT id FROM deletable);

    DELETE FROM cineflow.refresh_tokens t
    WHERE t.expires_at < as_of
      AND NOT EXISTS (
          SELECT 1
          FROM cineflow.refresh_tokens live
          WHERE live.family_id = t.family_id
            AND live.expires_at >= as_of
      );
    GET DIAGNOSTICS removed_tokens = ROW_COUNT;

    DELETE FROM cineflow.refresh_token_families f
    WHERE NOT EXISTS (
        SELECT 1
        FROM cineflow.refresh_tokens t
        WHERE t.family_id = f.id
    );

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
