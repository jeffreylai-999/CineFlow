# CineFlow

CineFlow is a cinema booking platform. This repository holds the preserved 2017 Java Swing application and the in-progress modernization to Spring Boot, React, and Supabase PostgreSQL.

## Layout

| Path | Purpose |
|------|---------|
| `legacy/` | Original desktop application (sanitized reference) |
| `backend/` | Java 25 Spring Boot modular monolith |
| `frontend/` | React + TypeScript SPA (Vite) |
| `docs/` | Specs, ADRs, research, and agent workflow |
| `CONTEXT.md` | Domain language |
| `scripts/` | Repository checks, including legacy sanitization scan |
| `docker-compose.yml` | Local PostgreSQL + application containers |

## Prerequisites

- OpenJDK 25
- Node.js 24+ and [pnpm](https://pnpm.io/) 11+
- Docker Desktop (for Compose, Testcontainers, and image builds)

## Local development

### Database only

```bash
docker compose up db -d
```

Copy `.env.example` values into your shell or a local `.env` (gitignored). Defaults match Compose:

- JDBC URL `jdbc:postgresql://localhost:5432/cineflow`
- User/password `cineflow` / `cineflow`
- JWT signing secret and bootstrap Administrator credentials (`administrator` / `AdminPassw0rd!`)
- Local/dev Booking Staff fixture `booking.staff` / `StaffPassw0rd!` (not seeded in production)

Set `CINEFLOW_AUTH_COOKIE_SECURE=false` for local HTTP. Production must use HTTPS and a unique `CINEFLOW_JWT_SECRET`.

Administrator Movie search and import use the active metadata provider. TMDB uses `CINEFLOW_TMDB_ACCESS_TOKEN` and OMDb uses `CINEFLOW_OMDB_API_KEY`. Those secrets stay in Spring Boot and never reach the React bundle. The portal lists providers whose credentials are configured, and also keeps the stored selection listed when that provider has no credentials so its radio stays selected. CineFlow does not fail over automatically; search and import then return a safe not-configured error until an Administrator selects a configured provider. Import carries the search hit’s provider and is rejected if the active selection has changed. Without a configured provider, the public catalog still lists stored Movies.

TMDB use is non-commercial. The public catalog Credits footer uses the official TMDB short logo and the required attribution notice. Cached descriptive metadata is refreshed from the original source rather than treated as a permanent local copy.

### Backend

Build the SPA once so Spring Boot can serve it from the same origin (optional for API-only work; required for `bootJar`/`bootRun` with the UI):

```bash
cd frontend && pnpm install && pnpm build && cd ..
cd backend
./gradlew bootRun
```

Useful URLs once running:

- Catalog API: `http://localhost:8080/api/movies`
- Staff sign-in: `http://localhost:8080/staff/login`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Readiness: `http://localhost:8080/actuator/health/readiness`

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Vite proxies `/api` to the Spring Boot process on port 8080. Component tests use Vitest browser mode with Playwright Chromium (ADR 0005); install browsers once with `pnpm exec playwright install chromium`.

Frontend UI: Tailwind v4 (`@tailwindcss/vite`) and shadcn on Base UI (ADR 0004). `components.json` keeps `style: "new-york"`, `tailwind.baseColor: "zinc"`, and `tailwind.cssVariables: true` after verifying those values against shadcn CLI 4.21.0 (`new-york` is in the schema enum and styles index; `zinc` resolves at the colors registry; `--css-variables` is the CLI default). Interactive `init -t vite` only offers Nova-family presets, so init is recorded via that checked `components.json` plus Base UI component adds from the `base-nova` registry. Vendor each component in its own commit.

### Full stack in containers

Copy `.env.example` to `.env` first. Compose interpolates the JWT signing secret, bootstrap Administrator credentials, and `CINEFLOW_AUTH_COOKIE_SECURE` from that file (cookie Secure defaults to `false` for local HTTP).

```bash
docker compose up --build
```

Open `http://localhost:8080` for the SPA served from Spring Boot.

## Render and Supabase

Public demonstration hosting is one Render Free Docker service plus Supabase PostgreSQL in Singapore. Secrets stay in the Render dashboard (`sync: false` in `render.yaml`), not in git.

See [`docs/deployment.md`](docs/deployment.md) for the session-pooler JDBC URL, TLS and pool-size guard, cold-start behaviour, and how to resume a paused Free Supabase project.

```bash
bash scripts/smoke-production-image.sh
```

That script boots the production image under a 512 MB cap, binds Render's `PORT` contract, and checks that the Movie fixture survives an application restart.

## Tests and CI

```bash
cd backend && ./gradlew test
cd frontend && pnpm lint && pnpm typecheck && pnpm test && pnpm build
docker build -t cineflow:local .
```

GitHub Actions runs the same checks on pull requests and `main`, including the production-image smoke test.

## Legacy application

See [`legacy/README.md`](legacy/README.md) for authors, prerequisites, Derby setup, demo credentials, and limitations.

## Modernization

Track work through [GitHub issue #1](https://github.com/jeffreylai-999/CineFlow/issues/1) and the linked sub-issues. The product specification is in `docs/specs/cineflow-modernization.md`.

## Code scanning

Pull requests and pushes to `main` run GitHub CodeQL. Alerts show under **Security → Code scanning**. Permissions and the blocking severity policy are documented in [`docs/code-scanning.md`](docs/code-scanning.md).
