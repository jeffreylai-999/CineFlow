-- Cinema-wide Booking Limit and public catalog Showtimes for the fixture Movie.
CREATE TABLE cineflow.cinema_settings (
    id             SMALLINT PRIMARY KEY,
    booking_limit  INTEGER NOT NULL,
    CONSTRAINT cinema_settings_singleton CHECK (id = 1),
    CONSTRAINT cinema_settings_booking_limit_check CHECK (booking_limit BETWEEN 1 AND 40)
);

INSERT INTO cineflow.cinema_settings (id, booking_limit) VALUES (1, 10);

CREATE INDEX showtimes_starts_at_idx ON cineflow.showtimes (starts_at);
CREATE INDEX showtimes_movie_starts_idx ON cineflow.showtimes (movie_id, starts_at);

WITH new_hall AS (
    INSERT INTO cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
    VALUES ('Fixture Hall', 4, 8, FALSE, TIMESTAMPTZ '2026-01-15 10:00:00+00')
    RETURNING id, row_count, seats_per_row
),
inserted_seats AS (
    INSERT INTO cineflow.seats (hall_id, row_label, seat_number, disabled)
    SELECT
        new_hall.id,
        chr(64 + row_number),
        seat_number,
        (row_number = 1 AND seat_number = 8)
    FROM new_hall
    CROSS JOIN generate_series(1, new_hall.row_count) AS row_number
    CROSS JOIN generate_series(1, new_hall.seats_per_row) AS seat_number
    RETURNING hall_id
)
UPDATE cineflow.halls
SET seat_map_locked = TRUE
WHERE id IN (SELECT hall_id FROM inserted_seats);

INSERT INTO cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
SELECT h.id, m.id, TIMESTAMPTZ '2099-06-20 11:30:00+00', 28.00, 18.00
FROM cineflow.halls h
JOIN cineflow.movies m ON m.source_provider = 'fixture' AND m.external_id = 'nebula-express'
WHERE h.name = 'Fixture Hall';
