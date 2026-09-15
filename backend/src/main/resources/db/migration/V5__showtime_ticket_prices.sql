-- Schedule Showtimes with tax-inclusive Adult and Child prices and Hall occupancy.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE cineflow.showtimes
    ADD COLUMN adult_price_myr NUMERIC(8, 2) NOT NULL DEFAULT 1.00,
    ADD COLUMN child_price_myr NUMERIC(8, 2) NOT NULL DEFAULT 1.00,
    ADD COLUMN occupancy tstzrange;

CREATE FUNCTION cineflow.showtime_occupancy(p_starts_at TIMESTAMPTZ, p_movie_id BIGINT)
RETURNS tstzrange
LANGUAGE sql
STABLE
AS $$
    SELECT tstzrange(
        p_starts_at,
        p_starts_at + make_interval(mins => m.runtime_minutes + 15),
        '[)'
    )
    FROM cineflow.movies m
    WHERE m.id = p_movie_id
$$;

CREATE FUNCTION cineflow.assign_showtime_occupancy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.occupancy := cineflow.showtime_occupancy(NEW.starts_at, NEW.movie_id);
    IF NEW.occupancy IS NULL THEN
        RAISE EXCEPTION 'scheduling.movie_not_found';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER showtimes_assign_occupancy
    BEFORE INSERT OR UPDATE OF starts_at, movie_id ON cineflow.showtimes
    FOR EACH ROW
    EXECUTE FUNCTION cineflow.assign_showtime_occupancy();

UPDATE cineflow.showtimes
SET occupancy = cineflow.showtime_occupancy(starts_at, movie_id);

ALTER TABLE cineflow.showtimes
    ALTER COLUMN occupancy SET NOT NULL,
    ADD CONSTRAINT showtimes_prices_positive CHECK (adult_price_myr > 0 AND child_price_myr > 0),
    ADD CONSTRAINT showtimes_hall_occupancy_excl
        EXCLUDE USING gist (hall_id WITH =, occupancy WITH &&);

CREATE FUNCTION cineflow.refresh_showtime_occupancy_for_movie()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.runtime_minutes IS DISTINCT FROM OLD.runtime_minutes THEN
        UPDATE cineflow.showtimes
        SET occupancy = cineflow.showtime_occupancy(starts_at, movie_id)
        WHERE movie_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER movies_refresh_showtime_occupancy
    AFTER UPDATE OF runtime_minutes ON cineflow.movies
    FOR EACH ROW
    EXECUTE FUNCTION cineflow.refresh_showtime_occupancy_for_movie();

CREATE FUNCTION cineflow.reject_showtime_on_archived_movie()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    movie_archived BOOLEAN;
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.movie_id IS NOT DISTINCT FROM OLD.movie_id THEN
        RETURN NEW;
    END IF;
    SELECT archived_at IS NOT NULL INTO STRICT movie_archived
    FROM cineflow.movies
    WHERE id = NEW.movie_id
    FOR UPDATE;
    IF movie_archived THEN
        RAISE EXCEPTION 'scheduling.movie_archived';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER showtimes_require_active_movie
    BEFORE INSERT ON cineflow.showtimes
    FOR EACH ROW
    EXECUTE FUNCTION cineflow.reject_showtime_on_archived_movie();

CREATE TRIGGER showtimes_require_active_movie_on_move
    BEFORE UPDATE OF movie_id ON cineflow.showtimes
    FOR EACH ROW
    EXECUTE FUNCTION cineflow.reject_showtime_on_archived_movie();

CREATE FUNCTION cineflow.reject_protected_showtime_delete()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM cineflow.seat_claims
        WHERE showtime_id = OLD.id
          AND claim_kind = 'BOOKING'
    ) THEN
        RAISE EXCEPTION 'scheduling.showtime_has_bookings';
    END IF;
    RETURN OLD;
END;
$$;

CREATE TRIGGER showtimes_reject_booked_delete
    BEFORE DELETE ON cineflow.showtimes
    FOR EACH ROW
    EXECUTE FUNCTION cineflow.reject_protected_showtime_delete();

CREATE OR REPLACE FUNCTION cineflow.reject_showtime_on_archived_hall()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    hall_archived BOOLEAN;
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.hall_id IS NOT DISTINCT FROM OLD.hall_id THEN
        RETURN NEW;
    END IF;
    SELECT archived_at IS NOT NULL INTO STRICT hall_archived
    FROM cineflow.halls
    WHERE id = NEW.hall_id
    FOR UPDATE;
    IF hall_archived THEN
        RAISE EXCEPTION 'scheduling.hall_archived';
    END IF;
    RETURN NEW;
END;
$$;
