-- Online Customer email retention: anonymize the email seven days after the Showtime
-- while preserving the Booking, Payment, booked Seats, Ticket, and Admission history.

CREATE OR REPLACE FUNCTION cineflow.anonymize_expired_booking_emails(as_of TIMESTAMPTZ DEFAULT now())
RETURNS INTEGER
LANGUAGE plpgsql
AS $function$
DECLARE
    anonymized_count INTEGER;
BEGIN
    UPDATE cineflow.bookings b
    SET email = 'anonymized@cineflow.invalid'
    FROM cineflow.showtimes s
    WHERE b.showtime_id = s.id
      AND s.starts_at <= as_of - interval '7 days'
      AND b.email <> 'anonymized@cineflow.invalid';
    GET DIAGNOSTICS anonymized_count = ROW_COUNT;
    RETURN anonymized_count;
END;
$function$;

-- Bounds every anonymization run to the rows still awaiting it: anonymized Bookings
-- leave this partial index, so the hourly scan does not grow with retained history.
CREATE INDEX bookings_pending_anonymization_idx
    ON cineflow.bookings (showtime_id)
    WHERE email <> 'anonymized@cineflow.invalid';

-- Supabase Cron runs the function hourly and records each run in cron.job_run_details.
-- Local and CI PostgreSQL has no pg_cron, so scheduling is skipped there; the function
-- above is still migrated and tested everywhere.
DO $schedule$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_cron') THEN
        CREATE EXTENSION IF NOT EXISTS pg_cron;
        PERFORM cron.schedule(
            'anonymize-expired-booking-emails',
            '17 * * * *',
            'SELECT cineflow.anonymize_expired_booking_emails()'
        );
    END IF;
END;
$schedule$;
