-- Staff-Assisted Bookings: anonymous Walk-in Customer sales recording received Cash or Card Payments.
ALTER TABLE cineflow.bookings
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE cineflow.payments
    DROP CONSTRAINT payments_method_check;

ALTER TABLE cineflow.payments
    ADD CONSTRAINT payments_method_check CHECK (method IN ('CARD_SIMULATED', 'CASH', 'CARD'));
