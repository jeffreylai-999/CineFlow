-- Temporary customer Seat Holds group the Seat claims acquired for one checkout.
CREATE TABLE cineflow.seat_holds (
    id          UUID PRIMARY KEY,
    showtime_id BIGINT      NOT NULL REFERENCES cineflow.showtimes (id),
    expires_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX seat_holds_expires_at_idx
    ON cineflow.seat_holds (expires_at);

ALTER TABLE cineflow.seat_claims
    ADD COLUMN hold_id UUID REFERENCES cineflow.seat_holds (id);

CREATE INDEX seat_claims_hold_id_idx
    ON cineflow.seat_claims (hold_id)
    WHERE hold_id IS NOT NULL;
