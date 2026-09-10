## Agent skills

### Issue tracker

Issues are tracked with GitHub Issues. See `docs/agents/issue-tracker.md`.

### Issue delivery flow

For issue work, follow `Backlog → Ready → In progress → In review → Done`. Read `docs/agents/issue-tracker.md` before claiming, implementing, reviewing, or closing an issue so Project #2 and its newly unblocked frontier stay current.

### Branch naming

Use `feat/<short-slug>` for implementation branches (for example `feat/sanitize-legacy`). Do not use `issue-N-...` or other prefixes.

### Pull request titles

Use `#<issue-number> - <issue title>` (for example `#2 - Sanitize and preserve the legacy application`).

### Frontend package manager

Use `pnpm` for the React/Vite frontend. Do not use `npm` or `yarn` for install or scripts.

### Java toolchain

Use OpenJDK **25** (current LTS) for the modern Spring Boot / Gradle stack. Do not use Java 8 except when inspecting `legacy/`.

### Triage labels

The canonical Matt Pocock triage labels are used unchanged. See `docs/agents/triage-labels.md`.

### Domain docs

This repository uses a single-context domain documentation layout. See `docs/agents/domain.md`.
