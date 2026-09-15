-- Local and test fixture only. The prod Flyway location does not include this script.
INSERT INTO cineflow.staff_accounts (
    username,
    password_hash,
    role,
    active,
    created_at
) VALUES (
    'booking.staff',
    '$2b$12$fjuTfnbHHQpXBdhjGl6NZ.j9dyjTLrCRr87JskFhkr1cUduAbhW6S',
    'BOOKING_STAFF',
    TRUE,
    TIMESTAMPTZ '2026-01-15 10:00:00+00'
);
