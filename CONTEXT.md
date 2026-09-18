# CineFlow

CineFlow covers customer cinema bookings and the staff operations required to offer and manage them.

## Language

**Cinema**:
The single physical venue operated through CineFlow and containing multiple halls.
_Avoid_: Branch, tenant, location

**Cinema Time**:
The `Asia/Kuala_Lumpur` local time used for showtimes, booking cutoffs, and counter-sales cutoffs.
_Avoid_: Browser time, UTC display time

**Customer**:
A person receiving a booking, either online or through Booking Staff.
_Avoid_: Member, account holder

**Online Customer**:
A customer who books without an account and is identified by an email address and booking reference. The email is anonymized seven days after the showtime.
_Avoid_: Registered customer, member

**Walk-in Customer**:
An anonymous customer served through a staff-assisted booking. CineFlow stores no personal details for this customer.
_Avoid_: Guest account, counter user

**Staff**:
An authenticated cinema employee assigned either the Booking Staff or Administrator role.
_Avoid_: Customer, member

**Booking Staff**:
A staff role that creates staff-assisted bookings and performs Admission, without changing cinema configuration.
_Avoid_: Staff role, standard user

**Administrator**:
A staff role that manages movies, halls, showtimes, cinema settings, and Booking Staff accounts. Administrators configure bookings; they do not perform Admission.
_Avoid_: Superuser, admin user

**Movie**:
A film offered by the cinema and scheduled through showtimes.
_Avoid_: Film record, event

**Hall**:
A cinema auditorium in which showtimes take place.
_Avoid_: Room, screen

**Seat**:
A fixed, labelled place within a hall that a customer can select for a showtime.
_Avoid_: Slot, position

**Disabled Seat**:
A seat excluded from new holds and bookings. A seat with an active hold or future booking cannot be disabled.
_Avoid_: Deleted seat, occupied seat

**Unavailable Seat**:
A seat a customer cannot select, being under another customer's seat hold, allocated to another customer's booking, or a disabled seat. The customer interface presents these as one state and does not reveal which applies. Staff see the underlying seat hold, booking, or disabled seat directly.
_Avoid_: Booked seat, disabled seat, occupied seat

**Seat Map**:
The immutable rows and numbered seats established when a hall is created. Individual seats may later be enabled or disabled without changing the layout.
_Avoid_: Showtime layout, capacity

**Showtime**:
A scheduled presentation of one movie in one hall at a specific date and time.
_Avoid_: Screening, session

**Booking**:
A confirmed allocation of seats to a customer for one showtime. A booking is final and cannot be cancelled.
_Avoid_: Reservation, order

**Seat Hold**:
A temporary claim on selected seats while checkout is in progress. It expires after ten minutes unless a booking is confirmed; a failed payment does not extend or release it early.
_Avoid_: Booking, reservation

**Seat Selection**:
The seats a customer has chosen but not yet claimed. It grants no entitlement, blocks nobody else, and does not survive a page reload or a return visit. A seat hold replaces it when checkout begins.
_Avoid_: Seat hold, reservation, basket

**Staff-Assisted Booking**:
A booking created by Booking Staff for a customer using the same availability and confirmation rules as online checkout.
_Avoid_: Walk-in sale, counter ticket

**Ticket Type**:
An Adult or Child pricing category selected for a booked seat.
_Avoid_: Customer type, fare class

**Ticket Price**:
The final tax-inclusive amount in Malaysian Ringgit set by an Administrator for one ticket type at one showtime.
_Avoid_: Movie price, dynamic price, subtotal

**Payment**:
The recorded settlement required to confirm a booking. Online card payments are simulated; Booking Staff records received Cash or Card payments.
_Avoid_: Transaction, charge

**Booking Reference**:
A unique identifier a customer or staff member uses to retrieve a booking.
_Avoid_: Booking ID, confirmation number

**Ticket**:
A printable confirmation for one booking whose QR code represents every seat in that booking.
_Avoid_: Seat ticket, receipt

**Admission**:
The one-time check-in of an entire booking by Booking Staff using its ticket or booking reference. A repeated check-in is identified as already admitted.
_Avoid_: Individual seat check-in, ticket redemption

**Booking Cutoff**:
The point fifteen minutes before a showtime when new online bookings and seat holds are no longer allowed.
_Avoid_: Showtime start, sales closure

**Booking Limit**:
The cinema-wide maximum number of seats allowed in one booking. It defaults to ten and may be changed by an Administrator.
_Avoid_: Hall capacity, seat availability

**Counter Sales Cutoff**:
The point fifteen minutes after a showtime starts when new staff-assisted bookings are no longer allowed.
_Avoid_: Booking cutoff, movie ending

**Cleaning Buffer**:
The fixed fifteen-minute period after a movie ends before the same hall can host another showtime.
_Avoid_: Intermission, movie duration

**Archived Movie or Hall**:
A movie or hall retained for booking history but unavailable for new showtimes.
_Avoid_: Deleted record, inactive booking
