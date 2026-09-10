# CineFlow Modernization Specification

## Problem Statement

CineFlow began as a 2017 Java Swing cinema ticketing project tied to Java 8, NetBeans, Ant, Apache Derby, machine-specific dependencies, and a desktop-only interface. The application stores credentials and realistic personal data in source-controlled files, mixes interface logic with database access, and has no automated tests or reproducible deployment.

The project owner wants to rebuild CineFlow as a modern full-stack learning project within one to three months. The result must demonstrate current Java, React, PostgreSQL, security, concurrency, testing, automation, containerization, and deployment practices while preserving the original application and its authorship.

## Solution

Build a responsive cinema booking and operations platform for one Cinema. Online Customers can browse Movies, select a Showtime and Seats, complete a simulated Payment, and receive a printable Ticket. Booking Staff can create anonymous Staff-Assisted Bookings and admit a whole Booking by scanning its Ticket or entering its Booking Reference. Administrators can manage Staff, Movies, Halls, immutable Seat Maps, Showtimes, Ticket Prices, movie metadata providers, and cinema-wide settings.

The modern application consists of a Java 25 Spring Boot modular monolith, a React and TypeScript single-page application built with Vite, and Supabase PostgreSQL. Spring Boot serves the compiled React application, REST interface, and STOMP WebSocket endpoint from one Render Docker service. Supabase Cron schedules idempotent PostgreSQL automation. Selected Edge Functions handle automation that requires external APIs. React remains a client-rendered SPA with no server-side rendering.

## User Stories

### Online Customer catalog

1. As an Online Customer, I want to browse Movies with current or future Showtimes, so that I can choose something I can book.
2. As an Online Customer, I want to see each Movie's poster, title, synopsis, genre, runtime, and age rating, so that I can decide whether it interests me.
3. As an Online Customer, I want archived Movies excluded from the public catalog, so that I do not see unavailable choices.
4. As an Online Customer, I want Showtimes displayed in Cinema Time, so that the schedule matches the physical Cinema.
5. As an Online Customer, I want Showtimes grouped by date, so that I can compare screening times.
6. As an Online Customer, I want Adult and Child Ticket Prices displayed in Malaysian Ringgit, so that I know the final tax-inclusive cost.
7. As an Online Customer, I want TMDB attribution displayed with imported metadata, so that CineFlow complies with the provider's terms.
8. As an Online Customer, I want the catalog to remain available when a metadata provider is unavailable, so that an external outage does not block Booking.

### Online Customer booking

9. As an Online Customer, I want to book without creating an account, so that checkout stays short.
10. As an Online Customer, I want to choose a Showtime before its Booking Cutoff, so that I can begin checkout while online sales remain open.
11. As an Online Customer, I want CineFlow to reject new online checkout after the Booking Cutoff, so that late online sales follow Cinema policy.
12. As an Online Customer, I want to see the fixed Seat Map for my Showtime, so that I can select labelled Seats.
13. As an Online Customer, I want available, held, booked, disabled, and selected Seats to have distinct text or shape indicators, so that color is not the only status cue.
14. As an Online Customer, I want live Seat changes through WebSockets, so that I see activity from other customers without refreshing the page.
15. As an Online Customer, I want CineFlow to refresh authoritative availability through REST after a WebSocket event or reconnect, so that missed or reordered events do not show stale state.
16. As an Online Customer, I want to select no more than the Booking Limit, so that one Booking follows Cinema policy.
17. As an Online Customer, I want the interface to explain the current Booking Limit, so that I understand why additional Seats cannot be selected.
18. As an Online Customer, I want to assign an Adult or Child Ticket Type to every selected Seat, so that CineFlow calculates the correct amount.
19. As an Online Customer, I want to see the final tax-inclusive total before Payment, so that I know exactly what the Booking costs.
20. As an Online Customer, I want my selected Seats held for ten minutes, so that another Customer cannot take them during checkout.
21. As an Online Customer, I want to see the remaining Seat Hold time, so that I know when the Seats will return to availability.
22. As an Online Customer, I want CineFlow to reject Seats that another Customer acquired first, so that the system never confirms duplicate allocation.
23. As an Online Customer, I want an expired Seat Hold to release its Seats, so that abandoned checkout does not block inventory.
24. As an Online Customer, I want to enter an email address for Booking retrieval, so that I can find the Booking without an account.
25. As an Online Customer, I want to complete a simulated card Payment, so that I can exercise a realistic checkout without transferring money.
26. As an Online Customer, I want a failed simulated Payment to leave the Seat Hold active for its original remaining time, so that I can retry without selecting Seats again.
27. As an Online Customer, I want duplicate Payment submissions to return the same result, so that retries do not create duplicate Bookings.
28. As an Online Customer, I want successful Payment and Seat allocation committed atomically, so that Payment cannot succeed without a confirmed Booking.
29. As an Online Customer, I want a unique Booking Reference after confirmation, so that I can retrieve my Booking.
30. As an Online Customer, I want one printable Ticket for the whole Booking, so that my group can enter together.
31. As an Online Customer, I want the Ticket QR code to contain an opaque Admission token, so that it exposes no email or predictable identifier.
32. As an Online Customer, I want to display the Ticket on a phone or print it, so that I can choose how to present it at the Cinema.
33. As an Online Customer, I want to retrieve a Booking with its Booking Reference and email before anonymization, so that I can recover the Ticket.
34. As an Online Customer, I want CineFlow to anonymize my email seven days after the Showtime, so that the system does not retain personal data longer than agreed.
35. As an Online Customer, I want a guided Showtime, Seats, and Payment sequence with a persistent summary, so that I can complete Booking on a phone.

### Booking Staff and Admission

36. As a Booking Staff member, I want to sign in with a Staff account, so that CineFlow applies my role.
37. As a Booking Staff member, I want to view Bookings by Booking Reference, Showtime, or Seat, so that I can help Customers.
38. As a Booking Staff member, I want to create a Staff-Assisted Booking for a Walk-in Customer, so that counter sales use the same Seat availability rules.
39. As a Booking Staff member, I want a Staff-Assisted Booking to store no Walk-in Customer details, so that counter sales remain anonymous.
40. As a Booking Staff member, I want to create a Staff-Assisted Booking until the Counter Sales Cutoff, so that late arrivals can buy Seats.
41. As a Booking Staff member, I want CineFlow to reject a Staff-Assisted Booking after the Counter Sales Cutoff, so that counter sales stop fifteen minutes after the Showtime begins.
42. As a Booking Staff member, I want to record received Cash or Card Payment, so that the Booking has a settlement record without external processing.
43. As a Booking Staff member, I want Staff-Assisted Bookings to obey the Booking Limit, pricing, availability, and double-booking rules, so that both sales channels remain consistent.
44. As a Booking Staff member, I want to print the Ticket for a Walk-in Customer, so that no email is required.
45. As a Booking Staff member, I want to scan a Ticket QR code, so that I can admit its entire Booking.
46. As a Booking Staff member, I want to enter a Booking Reference when scanning fails, so that camera problems do not block Admission.
47. As a Booking Staff member, I want CineFlow to mark a Booking admitted once, so that the system records entry.
48. As a Booking Staff member, I want a repeated Admission attempt to report that the Booking was already admitted, so that Ticket reuse is visible.
49. As a Booking Staff member, I want confirmed Bookings to remain final, so that the portal does not offer cancellation or refund actions.
50. As a Booking Staff member, I want my portal to hide Administrator functions, so that I cannot change Cinema configuration.

### Administrator identity and access

51. As a bootstrap Administrator, I want to create Booking Staff accounts, so that authorized employees can use the portal.
52. As an Administrator, I want to deactivate Booking Staff accounts, so that former employees lose access.
53. As an Administrator, I want to issue a Staff password reset, so that recovery does not require email or security questions.
54. As an Administrator, I want inactive Staff accounts blocked from login and token refresh, so that deactivation takes effect across sessions.
55. As an Administrator, I want Booking Staff and Administrator permissions enforced by Spring Boot, so that hidden interface controls are not the security boundary.

### Administrator movie catalog

56. As an Administrator, I want to select TMDB or OMDb from configured providers, so that I control which provider handles new searches and imports.
57. As an Administrator, I want unavailable providers excluded from selection when deployment credentials are missing, so that CineFlow does not expose unusable options.
58. As an Administrator, I want provider credentials supplied through deployment secrets, so that I never enter or view API keys in the portal.
59. As an Administrator, I want to search the active provider and import a Movie, so that I do not retype descriptive metadata.
60. As an Administrator, I want each Movie to retain its source provider and external identifier, so that refreshes return to the correct source.
61. As an Administrator, I want existing Movies to keep their source after the active provider changes, so that CineFlow does not make unsafe cross-provider matches.
62. As an Administrator, I want provider-owned title, synopsis, genre, and poster metadata refreshed from its source, so that cached data remains compliant.
63. As an Administrator, I want to set or correct scheduling-critical runtime and age rating locally, so that incomplete provider data does not break operations.
64. As an Administrator, I want warnings before provider data reaches its retention limit, so that I can refresh or archive the Movie.
65. As an Administrator, I want existing imported Movies available during provider outages, so that scheduling and Booking continue.
66. As an Administrator, I want a failed provider search or refresh to produce a clear recoverable error, so that I can retry or select the fallback provider.
67. As an Administrator, I want CineFlow to archive a Movie automatically after its last Showtime ends and no current or future Showtime remains, so that the public catalog stays current.
68. As an Administrator, I want archived Movies retained with historical Bookings, so that archival does not erase records.

### Administrator halls, seats, and showtimes

69. As an Administrator, I want to create a Hall by specifying rows and Seats per row, so that CineFlow generates a labelled Seat Map.
70. As an Administrator, I want the Seat Map geometry locked after Hall creation, so that Seat identities remain stable.
71. As an Administrator, I want to enable or disable an individual Seat, so that I can reflect unavailable physical seating.
72. As an Administrator, I want CineFlow to reject disabling a Seat with an active Seat Hold or future Booking, so that confirmed Tickets remain valid.
73. As an Administrator, I want to archive a Hall, so that it cannot receive new Showtimes while history remains intact.
74. As an Administrator, I want to create a Showtime for one Movie and one Hall in Cinema Time, so that Customers can book it.
75. As an Administrator, I want to set Adult and Child Ticket Prices for each Showtime, so that pricing can vary by schedule.
76. As an Administrator, I want CineFlow to reject overlapping Showtimes in one Hall, so that the schedule remains physically possible.
77. As an Administrator, I want overlap detection to include Movie runtime and the fifteen-minute Cleaning Buffer, so that staff have turnover time.
78. As an Administrator, I want to remove an unused future Showtime, so that scheduling mistakes can be corrected.
79. As an Administrator, I want CineFlow to protect Showtimes and related data once Bookings exist, so that history cannot be deleted.
80. As an Administrator, I want to configure one cinema-wide Booking Limit that defaults to ten, so that group size policy remains simple.

### Security, automation, and operations

81. As a Staff member, I want a fifteen-minute access token held only in browser memory, so that authorization remains short-lived.
82. As a Staff member, I want an eight-hour refresh session in a secure HttpOnly cookie, so that I can work through a shift without exposing the refresh token to JavaScript.
83. As a Staff member, I want refresh tokens rotated on every use, so that replayed tokens can be detected.
84. As a Staff member, I want logout to revoke my refresh-token family, so that the session cannot resume.
85. As an Administrator, I want token-family reuse to revoke the affected family and create an Audit Event, so that suspected theft is contained and traceable.
86. As a Staff member, I want protected STOMP connections authenticated with the in-memory access token, so that live data follows the same permissions as REST.
87. As a Staff member, I want the client to refresh authentication and reconnect when a protected WebSocket expires, so that live updates recover during a shift.
88. As a Cinema operator, I want rate limits on login, token refresh, Booking retrieval, Seat Holds, Payment simulation, and provider search, so that brute-force and resource abuse are constrained.
89. As a Cinema operator, I want meaningful Staff actions recorded as Audit Events, so that I can trace authentication, configuration changes, provider switches, Staff-Assisted Bookings, and Admissions.
90. As a Cinema operator, I want Supabase Cron to run movie archival, email anonymization, and expired-record cleanup, so that data maintenance does not depend on a sleeping Render process.
91. As a Cinema operator, I want automation functions to be idempotent and their job runs inspectable, so that retries remain safe.
92. As a Cinema operator, I want public demonstration data retained without a scheduled reset, so that activity persists between visits.
93. As a Cinema operator, I want structured application logs with correlation IDs, so that I can connect safe frontend errors to backend diagnostics.
94. As a deployment owner, I want health and readiness endpoints, so that Render can detect startup and dependency failures.
95. As a deployment owner, I want Flyway to apply versioned PostgreSQL migrations, so that schema and Cron changes remain reviewable.
96. As a deployment owner, I want sanitized deterministic seed data, so that local and public environments start without realistic personal information.
97. As a deployment owner, I want Spring Boot to use a small Hikari connection pool over TLS, so that the Supabase Free database is not exhausted.
98. As a deployment owner, I want application tables isolated from Supabase's public Data API, so that React cannot bypass Spring Boot authorization.
99. As a deployment owner, I want provider keys, database credentials, JWT signing keys, and bootstrap credentials stored outside source control, so that deployments do not leak secrets.
100. As a deployment owner, I want one Docker image containing Spring Boot and the Vite build, so that Render serves one origin and one WebSocket endpoint.
101. As a deployment owner, I want the container to run within Render Free's memory limit, so that the public demonstration remains deployable.
102. As a deployment owner, I want manual recovery instructions for a paused Supabase Free project, so that the demonstration can be restored after inactivity.

### Interface quality and project maintenance

103. As an Online Customer, I want a mobile-first Cinematic Focus interface, so that Booking works well on a phone.
104. As a Staff member, I want a desktop-first sidebar workspace that remains usable on a tablet, so that operational navigation stays stable.
105. As a keyboard user, I want to complete catalog browsing, Seat selection, checkout, administration, and Admission without a pointer, so that CineFlow meets its accessibility target.
106. As a screen-reader user, I want labels, validation, status changes, and Seat state announced clearly, so that visual layout is not required.
107. As a user, I want safe, consistent Problem Details responses translated into clear interface messages, so that failures do not expose internals.
108. As a user of a current or previous Chrome, Edge, Firefox, or Safari release, I want the application to function correctly, so that I can use a supported browser.
109. As a maintainer, I want generated OpenAPI documentation for REST operations, so that backend behavior stays discoverable.
110. As a maintainer, I want GitHub Actions to run backend tests, frontend linting, type checks, component tests, and production builds, so that pull requests receive consistent verification.
111. As a maintainer, I want scenario-based test requirements instead of a global coverage percentage, so that tests protect behavior rather than a metric.
112. As a maintainer, I want setup, architecture, deployment, screenshots, attribution, and known limitations documented, so that another developer can run and understand CineFlow.
113. As a maintainer, I want the original application preserved with both authors credited, so that the modernization does not erase project history.
114. As a maintainer, I want modern work distinguished from the legacy application, so that reviewers can evaluate the learning outcome accurately.

## Implementation Decisions

### Architecture and modules

- Preserve the original Java application as a `legacy` area. Build the modern backend and frontend as separate development modules in the same repository, with deployment configuration at the repository root.
- Use Java 25, Spring Boot, Gradle, React, TypeScript, Vite, and Supabase PostgreSQL.
- Build one Spring Boot modular monolith. Package code by business module rather than technical layer.
- Define Catalog, Scheduling, Booking, Payment, Admission, Identity and Access, Staff Administration, Automation, Audit, and Web Interface modules.
- Let each module expose one small interface to its callers. Keep persistence models, framework types, provider payloads, and internal helpers behind those interfaces.
- Use REST with JSON for request-response interactions and STOMP over WebSockets for live Seat availability.
- Serve the compiled Vite output from Spring Boot. Deploy one Docker image to Render from one origin.
- Keep React as a client-rendered SPA. Do not add server-side rendering.

### Web interface

- Apply the Cinematic Focus visual direction: deep navy surfaces, vivid red actions, large Movie imagery, restrained motion, and accessible contrast.
- Use a guided Customer flow with Showtime, Seats, and Payment steps plus a persistent Booking summary.
- Use a stable sidebar workspace for Staff. Hide navigation entries by role while enforcing every permission in Spring Boot.
- Build the Customer interface mobile-first. Build the Staff portal desktop-first and retain tablet usability.
- Meet WCAG 2.2 AA. Support keyboard operation, visible focus, semantic landmarks, announced errors, and Seat states that do not rely on color.
- Support the current and previous major versions of Chrome, Edge, Firefox, and Safari.

### Domain time and money

- Treat `Asia/Kuala_Lumpur` as Cinema Time. Store instants as timezone-aware timestamps and convert at the interface.
- Store money as exact decimal values with currency fixed to MYR.
- Treat configured Ticket Prices as final tax-inclusive amounts.
- Calculate a Showtime's occupied interval from its start, locally maintained Movie runtime, and fifteen-minute Cleaning Buffer.
- Reject overlapping occupied intervals for the same Hall at both the domain and database levels.

### Booking and concurrency

- Use PostgreSQL as the source of truth for Seat allocation.
- Represent each Showtime and Seat claim in one allocation structure with a unique constraint across Showtime and Seat. A claim begins as a Seat Hold with an expiry and converts atomically into a confirmed Booking allocation.
- Treat expired claims as available during every availability and acquisition operation. Cron cleanup reduces storage but does not determine correctness.
- Acquire multiple Seats in deterministic Seat order inside a short transaction to reduce deadlock risk.
- Keep simulated Payment work outside locking transactions. Use a short final transaction to validate the Seat Hold, record Payment, create the Booking, convert claims, and issue the Booking Reference and Admission token.
- Make Payment confirmation idempotent through a client-supplied idempotency key with a unique database constraint.
- Publish Seat changes after the transaction commits. WebSocket messages tell clients to refresh the authoritative REST representation.
- Enforce the ten-minute Seat Hold, cinema-wide Booking Limit, Booking Cutoff, and Counter Sales Cutoff in the backend using Cinema Time.
- Generate unpredictable Booking References suitable for lookup and store Admission tokens as hashes. One Admission token covers the whole Booking.
- Keep confirmed Bookings immutable. Do not implement cancellation, refunds, or partial Admission.

### PostgreSQL schema

- Use lowercase snake_case identifiers, `bigint` identity primary keys for internal rows, exact numeric money columns, boolean flags, text with check constraints for small state sets, and timezone-aware timestamps.
- Use opaque random public identifiers only where records cross the trust boundary, including Booking Reference and Admission token.
- Model Cinema settings, Staff accounts, refresh-token families, Movies, Halls, Seats, Showtimes, Ticket Prices, Seat claims, Bookings, booked Seats, Payments, Admissions, and Audit Events.
- Store each imported Movie's source provider, external identifier, source refresh time, descriptive metadata, local runtime, local age rating, and archival time. Enforce uniqueness across provider and external identifier.
- Keep a Hall's Seat Map immutable after creation. Allow only enabled or disabled status changes, and reject disabling when active claims or future Bookings exist.
- Preserve historical foreign keys. Use restrictive deletion rules for records referenced by Bookings. Archive Movies and Halls instead of deleting them.
- Add indexes for every foreign key and for the lookup patterns used by catalog dates, active Movies, Hall schedules, Booking Reference, Showtime Seat availability, expiring claims, Staff status, source refresh deadlines, and Audit Event time.
- Use partial indexes for active Movies, active Staff, unexpired Seat Holds, and other queries that consistently filter archival or status columns.
- Keep transactions short and lock related Seat rows in deterministic order.
- Use Flyway for schema, constraints, functions, extensions, Cron registration, and sanitized fixtures.

### Supabase

- Use Supabase PostgreSQL, Cron, and selected Edge Functions. Keep Supabase Auth, Realtime, Storage, and direct React data access outside the initial scope.
- Put application tables in a private schema that Supabase's Data API does not expose. Grant a dedicated application database role only the privileges Spring Boot needs.
- Enable RLS on any table placed in an exposed schema. Grant no browser policy because React accesses data through Spring Boot.
- Use a direct TLS connection for migrations where IPv6 is available. Use Supavisor session mode on port 5432 for persistent Spring Boot traffic on an IPv4-only host. Do not use transaction mode as the main JPA connection.
- Keep the Spring Hikari pool small and below Supabase Free connection limits.
- Implement idempotent PostgreSQL functions for Movie archival, Online Customer email anonymization, expired Seat Hold cleanup, expired refresh-token cleanup, and Cron history cleanup.
- Archive only Movies that have had at least one Showtime, whose last Showtime has ended, and that have no current or future Showtime.
- Retain public demonstration data. Do not schedule a reset.
- Use Edge Functions only when automation requires external HTTP work. Store Edge Function call secrets in Supabase Vault.
- Treat Supabase Free pausing as a known deployment limitation and document manual restoration.

### Movie metadata providers

- Implement the `MovieMetadataProvider` interface with TMDB and OMDb adapters.
- Let an Administrator select one active provider from adapters whose deployment credentials exist.
- Do not fail over automatically. Keep failures and provider selection visible.
- Retain provider provenance per Movie and refresh through the original adapter.
- Treat descriptive provider fields as refreshable. Let Administrators own runtime and age rating because scheduling depends on them.
- Keep existing imported Movies usable during provider outages.
- Warn before provider retention deadlines. Refresh from the original source or archive the Movie when a compliant refresh remains impossible.
- Meet TMDB attribution, branding, caching, and non-commercial-use requirements.

### Identity and security

- Bootstrap the first Administrator from deployment configuration. Store only a strong password hash.
- Let Administrators create, deactivate, and reset Booking Staff accounts. Do not support self-registration or email recovery.
- Issue fifteen-minute signed JWT access tokens to browser memory.
- Store eight-hour opaque refresh tokens in Secure, HttpOnly cookies. Hash refresh tokens in PostgreSQL.
- Rotate the refresh token on every refresh. Track token families, detect reuse, revoke the family on reuse, and emit an Audit Event.
- Revoke token families on logout, password reset, account deactivation, and relevant role changes.
- Authenticate protected STOMP connections through the in-memory access token and reconnect after refresh.
- Apply role checks at module interfaces and transport adapters.
- Rate-limit login, refresh, Booking retrieval, Seat Hold acquisition, Payment simulation, and provider search. Return stable retry information.
- Keep database credentials, signing keys, bootstrap credentials, and provider keys in deployment secrets.

### REST, WebSocket, and errors

- Publish generated OpenAPI documentation.
- Return RFC Problem Details documents with stable application error codes and correlation IDs.
- Define stable errors for cutoffs, expired Seat Holds, Seat conflicts, Booking Limit violations, failed simulated Payment, invalid Booking retrieval, repeated Admission, unavailable providers, schedule conflicts, disabled Seats, and authorization failures.
- Return no raw exception messages, SQL details, credentials, or personal data.
- Scope Seat WebSocket topics by Showtime. Send minimal invalidation events after committed changes.
- Require clients to fetch the current REST Seat Map on initial load, after reconnect, and after each invalidation.

### Deployment and operations

- Use a multi-stage Docker build that creates the Vite production assets and packages them into the Spring Boot application.
- Run the container within Render Free's 512 MB memory limit with an explicit container-aware JVM heap. Verify memory through a production-image smoke test.
- Bind to Render's provided port, terminate public traffic with HTTPS, and use `wss` for STOMP.
- Configure Render health and readiness checks.
- Keep PostgreSQL outside the application container. Store no durable state on Render's ephemeral filesystem.
- Choose the closest practical Supabase and Render regions.
- Provide structured JSON logs and propagate one correlation ID across HTTP requests and Audit Events.
- Keep public demo data across deployments. Maintain sanitized fixtures and a documented manual restoration procedure.

### Documentation and attribution

- Credit both original authors in the legacy documentation and explain which work belongs to the modernization.
- Document local prerequisites, Gradle and Vite commands, Supabase setup, environment variables, Flyway migrations, Docker Compose, Render deployment, Cron jobs, demo credentials, supported browsers, and known free-tier limitations.
- Include architecture and module diagrams, screenshots of the Customer and Staff flows, OpenAPI access instructions, TMDB attribution, and security limitations.

## Testing Decisions

- Test external behavior through module interfaces. Tests should assert outcomes, persisted state, emitted messages, and safe errors rather than private methods or framework wiring.
- Use the running Spring Boot HTTP and STOMP interface against PostgreSQL Testcontainers as the primary backend seam.
- Cover authentication, token rotation and reuse, role enforcement, Movie import, provider selection, schedule overlap, immutable Seat Maps, cutoffs, Seat Hold expiry, concurrent Seat acquisition, Payment idempotency, Booking confirmation, retrieval, anonymization, Staff-Assisted Booking, Admission, archival, and audit behavior through that seam.
- Use concurrency tests that start competing Seat acquisitions and prove that one transaction wins while the others receive a stable Seat-conflict response.
- Use controlled clocks for Cinema Time, Seat Hold expiry, cutoffs, Cleaning Buffers, token expiry, and anonymization.
- Test PostgreSQL constraints through integration behavior, including uniqueness, restrictive deletion, schedule exclusion, immutable Seat identity, and foreign-key preservation.
- Render full React routes with React Testing Library. Replace backend access through one typed client adapter and STOMP through one socket adapter.
- Verify the guided Booking flow, countdown behavior, live invalidation refresh, validation, role-sensitive navigation, Ticket rendering, Admission feedback, loading states, safe errors, keyboard use, focus movement, and non-color Seat states.
- Run one contract suite against both `MovieMetadataProvider` adapters using controlled HTTP fixtures. Cover mapping, absent optional fields, provenance, errors, quotas, refresh, and source-specific behavior.
- Invoke automation functions against controlled PostgreSQL state. Prove idempotence and business outcomes. Test Cron definitions as thin wiring rather than duplicating function behavior.
- Use backend unit tests only for dense combinatorial policies such as price calculation, schedule ranges, and cutoff evaluation when integration tests would obscure failures or run too slowly.
- The legacy project has no existing automated tests that provide prior art.
- GitHub Actions must run backend unit and integration tests, frontend linting, TypeScript checks, React component tests, production builds, migration verification, and Docker image construction.
- Judge completeness through required scenarios. Do not enforce a global line-coverage percentage.
- Defer Playwright end-to-end tests until the initial implementation is complete. Track them as a future enhancement.

## Out of Scope

- Customer accounts, registration, profiles, loyalty, and Customer authentication.
- Real card processing, payment-provider integration, refunds, cancellation, and chargebacks.
- Customer email delivery and self-service cancellation.
- Multiple Cinema branches, tenant isolation, multiple currencies, and interface localization.
- Promotions, sales reports, dynamic pricing, per-Seat pricing, reviews, trailers, cast management, and streaming availability.
- Arbitrary visual Seat Map editing, changing Seat identities after Hall creation, and per-Showtime Seat layouts.
- Partial Booking Admission and separate Tickets for each Seat.
- Automatic movie-provider failover, cross-provider metadata merging, and provider plugins.
- Supabase Auth, Realtime, Storage, and direct browser database access.
- React server-side rendering and separate Vercel deployment.
- Microservices, Kubernetes, and vendor-specific infrastructure beyond Render and Supabase configuration.
- Migration of Derby records or personal-looking legacy seed data.
- Automatic public-demo data resets.
- Playwright end-to-end tests in the initial implementation.
- Production guarantees, uptime SLA, and unattended recovery from free-tier pauses.

## Further Notes

- Preserve the legacy application as a primary behavioral reference, not as a module reused by the modern application.
- The public demo uses shared sandbox Staff credentials while retaining visitor changes. This creates a vandalism and data-growth risk. Audit Events, rate limits, restrictive deletion, sanitized fixtures, backups, and manual restoration reduce the impact but do not provide production-grade isolation.
- Supabase Free can pause after low activity. Render Free can sleep after inactivity and has a tight Java memory budget. Document the expected cold start and restoration steps.
- Keep independent logical backups because free hosting does not provide production durability.
- `CONTEXT.md` is the source of truth for domain vocabulary. The ADRs are the source of truth for provider selection, token architecture, and Supabase scope.
- The initial modernization is complete when the core Online Customer, Booking Staff, and Administrator journeys work; required automated tests and CI pass; the Docker image runs locally and on Render; and project documentation is complete.
