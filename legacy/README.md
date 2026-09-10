# Legacy CineFlow (2017)

This directory preserves the original Java Swing cinema ticketing application as a behavioral reference for the modernization. It is not used by the modern Spring Boot / React stack.

## Authors

Original authors:

- Jeffrey Lai (Lai Chii Xian)
- Cheong Pui Yee (KSY Cheong)

Source files retain their original `@author` tags. Modernization work lives outside this directory and is documented in `docs/specs/cineflow-modernization.md`.

## Prerequisites

- Java 8 JDK with Apache Derby client libraries
- Apache Ant (NetBeans Ant projects), or NetBeans IDE 8.x
- A local Apache Derby Network Server listening on `localhost:1527`
- Optional: JasperReports runtime already vendored under `legacy/lib/`

Copy `derbyclient.jar` from your JDK (`db/lib/derbyclient.jar`) into `legacy/lib/derbyclient.jar` before building. The NetBeans project references that relative path.

## Database setup

1. Start Derby Network Server.
2. Create a database named `cinemas` reachable at `jdbc:derby://localhost:1527/cinemas`.
3. Create a Derby user that matches the placeholders in source:
   - JDBC user: `APP_USER`
   - JDBC password: `changeme`
4. Apply `legacy/table.sql` to create schema and sanitized seed data.

Embedded Derby runtime folders such as `derby-cinemas/` are intentionally excluded from source control. Recreate the database from `table.sql` instead of copying old runtime files.

## Build and run

From `legacy/`:

```text
ant -f build.xml
```

Or open `legacy/` as a NetBeans project and use Run.

Main entry UI: `src/ui/Login.java`.

## Demo credentials (sanitized)

| Staff ID     | Password  | Role          |
|--------------|-----------|---------------|
| STF0000001   | changeme  | Administrator |
| STF0000002   | changeme  | Staff         |

Customer, Hall, and Movie delete dialogs confirm with the phrase `confirm-delete`, not a staff password.

Seed customers and staff rows use clearly fictional `example.com` identities. Do not treat them as real personal data.

## Limitations

- Desktop-only Swing UI; no web client.
- Hard-coded JDBC placeholders in data-access classes and `persistence.xml`.
- Plaintext staff passwords in the Derby schema (historical design).
- Machine-local report logos resolve to `images/logo.jpg` relative to the working directory; supply your own image if you need rendered reports.
- No automated tests.
- Built against Java 8 / NetBeans Ant conventions; not part of the modern Gradle build.

## Sanitization scan

From the repository root:

```powershell
powershell -NoProfile -File scripts/verify-legacy-sanitization.ps1
```

The script fails if any known pre-sanitization secret or personal-fixture line (tracked only as SHA-256 digests) reappears in the tree.
