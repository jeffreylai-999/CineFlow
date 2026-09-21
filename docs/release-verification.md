# Release verification (#18)

Record of the release gates for the Customer, Booking Staff, and Administrator journeys.
Automated evidence lives in CI and the scripts named below; manual accessibility passes that
axe cannot detect are recorded here.

## Seams under test

| Seam | How verified |
| --- | --- |
| Backend module HTTP/STOMP + PostgreSQL | `./gradlew test` (unit + Testcontainers ITs from an empty database via Flyway) |
| Frontend routes / components | `pnpm lint`, `pnpm typecheck`, `pnpm test` (Vitest browser mode, Playwright Chromium provider, ADR 0005) |
| Production image + Render Free memory | `scripts/smoke-production-image.sh` (CI `container` job) |
| Flyway from empty database | Testcontainers ITs; `scripts/check-supabase-advisor-surface.sh` and `scripts/prove-backup-restore.sh` both migrate empty Postgres |
| Supabase advisors (release-blocking surface) | `scripts/check-supabase-advisor-surface.sh` — no application tables in `public`; `cineflow` stays private |
| axe-core on every route | Browser-mode tests call `axe.run` after interactive regions are shown (Admission starts the camera; no Dialog/Menu primitives are mounted today) |
| Seat-state contrast | Measured ratios recorded beside tokens in `frontend/src/styles/cinematic-focus.css`; enforced by `parseSeatContrastComments` tests |
| Keyboard Focus Order / Focus Visible | Recorded walkthrough below; Seat Map focus ring asserted in `SeatSelectionPage` tests |
| Meaningful Sequence | Recorded reading-order pass below; Seat Map before Booking summary asserted in DOM |
| Supported browsers | Agreed level: current or previous Chrome, Edge, Firefox, Safari. Automated suite runs Chromium; see browser section |
| Backup + manual restore | `scripts/prove-backup-restore.sh` with sanitized fixtures |
| Credentials / personal data / authorization / rate limits | Backend ITs for auth, role checks, rate limits; dump scan rejects personal-looking patterns |
| Playwright end-to-end suites | Deferred — `docs/future-enhancements.md` (Playwright remains the Vitest browser-mode provider) |

## Automated gate results

Recorded on branch `feat/release-verification` (local run before PR CI):

| Gate | Command | Result |
| --- | --- | --- |
| Backend tests | `cd backend && ./gradlew test --no-daemon` | Pass |
| Frontend lint | `cd frontend && pnpm lint` | Pass |
| Frontend typecheck | `cd frontend && pnpm typecheck` | Pass |
| Frontend browser tests | `cd frontend && pnpm test` | Pass (162 tests) |
| Frontend production build | `cd frontend && pnpm build` | Pass |
| Production image smoke | `bash scripts/smoke-production-image.sh` | CI `container` job |
| Flyway + advisor surface | `bash scripts/check-supabase-advisor-surface.sh` | Pass (empty DB → v12; zero `public` tables without RLS) |
| Backup / restore | `bash scripts/prove-backup-restore.sh` | Pass (Nebula Express fixture survives dump → drop → restore) |

Hosted Supabase Studio advisors for Auth/Realtime/Storage objects that CineFlow does not use remain outside this application schema. Application data stays in `cineflow`, which is not on the PostgREST `public` API surface (ADR 0003). Re-check Studio Security/Performance advisors after any change that exposes a new schema.

## Accessibility — automated floor

Every Customer and Staff route under test runs `axe-core` directly in the browser-mode suite and fails on `serious` / `critical` impacts. Routes covered: catalog, Seat selection, checkout (form + confirmed Ticket), ticket retrieval, Staff login/home/accounts, Admin movies, Halls, Showtimes, Seat Grid, counter sales list/sale, Admission (camera started first).

## Accessibility — keyboard walkthrough (Focus Order, Focus Visible)

Completed for this release against the Vitest browser harness and the live component trees. Pointer was not used for the activation steps below.

| Flow | Focus Order | Focus Visible | Notes |
| --- | --- | --- | --- |
| Catalog browsing | Landmark → Movie headings / Showtime links follow DOM order | Focus ring via shared `Button` / link styles (`focus-visible:ring-*`) | No dialogs to open |
| Seat selection | Booking steps → Seat Map buttons (native tab order) → Ticket Type selects → Hold / Continue | Seat buttons use `focus-visible:ring-3 focus-visible:ring-ring/50`; asserted in tests | Native tab order accepted per spec |
| Checkout | Steps → email / card fields → Pay | Same focus-visible tokens | Confirmed Ticket state also axe-clean |
| Administration | Staff nav → movie / hall / showtime / accounts controls | Shared button/input focus styles | Archive / import controls are in-page, not modal |
| Admission | Heading → Start camera → Booking Reference → Admit | Camera region announced; manual fallback remains keyboard reachable | Scanner UI opened before axe |

## Accessibility — reading-order pass (Meaningful Sequence)

Completed by reading the rendered DOM against the layout for the same five flows. A keyboard-only pass cannot satisfy this criterion.

| Flow | Method | Result |
| --- | --- | --- |
| Catalog | DOM order of headings, posters, Showtimes matches visual stack | Pass |
| Seat selection | Seat Map (`role="group"`) precedes Booking summary (`complementary`) in DOM; mobile dock is visually below via CSS, still after the map in DOM | Pass — asserted in `SeatSelectionPage` test |
| Checkout | Form fields then confirmation Ticket content follow visual order | Pass |
| Administration | Page heading → filters/actions → tables/lists | Pass |
| Admission | Instructions → camera / reference → result | Pass |

Docked Booking summary and Seat Map remain the divergence risk; CSS reorders only on the visual plane (sticky / fixed), not via `order`/`flex-direction` that would invert DOM reading order.

## Seat-state contrast ratios

Measured values live beside the token definitions in `frontend/src/styles/cinematic-focus.css` (for example `--seat-held-border` ≈ 6.15:1 against held fill). `parseSeatContrastComments` requires every Seat-state token to carry a recorded ratio and to meet AA thresholds (3:1 non-text borders, 4.5:1 text-on-fill pairs).

## Supported browsers

Agreed product level (spec story 108): current or previous Chrome, Edge, Firefox, or Safari.

Verification for this release:

- Automated: full React route/component suite in Vitest browser mode on **Chromium** (Playwright provider).
- Shared primitives and Seat Map use standard focus, `<button>`, and form controls without browser-specific APIs beyond `getUserMedia` for Admission (with a keyboard Booking Reference fallback).
- Safari/Firefox/Edge parity for this release is accepted at the “same evergreen Web platform APIs” level; no Safari-only or Firefox-only code paths are present. Re-smoke those browsers manually when Admission camera behaviour changes.

## Security and personal data

No release-blocking defect found in the automated suite for this release:

- Invalid credentials return stable `auth.invalid_credentials` without leaking which field failed (`IdentityAuthIT`).
- Role enforcement hides Administrator surfaces from Booking Staff (`StaffRoutes` tests + backend authorization ITs).
- Rate limits cover login, refresh, Seat Hold, checkout Payment, Booking retrieval, and catalog search.
- Production migrations do not seed Booking Staff (`StaffIdentityMigrationTest`).
- Backup dump scan rejects personal-looking email domains and credential-like patterns; Online Customer emails anonymize to `anonymized@cineflow.invalid` after seven days.

## Backup and restore

```bash
bash scripts/prove-backup-restore.sh
```

Procedure mirrors `docs/deployment.md` (`pg_dump --schema=cineflow`). The proof recreates `btree_gist` in `cineflow` after schema drop because `pg_dump --schema` omits that extension while Flyway installs it into the application schema.

## Playwright end-to-end

Deferred as a future enhancement. See `docs/future-enhancements.md`. This does not remove Playwright as the Vitest browser-mode provider.
