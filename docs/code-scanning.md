# Code scanning

CineFlow uses GitHub CodeQL for static analysis. Results appear under **Security → Code scanning**. This is separate from application CI (tests, lint, builds, containers) tracked in #3.

## Workflow

- Workflow: [`.github/workflows/codeql.yml`](../.github/workflows/codeql.yml)
- Config: [`.github/codeql/codeql-config.yml`](../.github/codeql/codeql-config.yml)
- Triggers: pushes and pull requests targeting `main`, plus a weekly schedule
- Languages today: `java-kotlin` over `legacy/src` (`build-mode: none`)
- Extending later: add matrix rows and `paths` entries for modern Java/Kotlin and JavaScript/TypeScript when those trees exist (#3)

## Required permissions

The workflow declares these GitHub Actions job permissions:

| Permission | Access | Why |
|------------|--------|-----|
| `security-events` | write | Upload SARIF so alerts appear in Code scanning |
| `contents` | read | Check out the repository |
| `actions` | read | Use GitHub Actions (required on private repositories) |

Repository settings:

1. **Settings → Code security** — leave CodeQL / code scanning enabled (advanced setup via the workflow above).
2. Do not enable Default setup at the same time as this advanced workflow; pick one.
3. To block merges when the severity policy fails, add a ruleset or branch protection rule that requires the **Code scanning results** check on pull requests to `main`.

Public repositories can use CodeQL code scanning without GitHub Advanced Security. Private repositories need Advanced Security for code scanning alerts.

## Severity policy (blocking)

Chosen policy: **GitHub's default Code scanning results thresholds**.

After SARIF upload, GitHub's pull request **Code scanning results** check fails when newly introduced alerts include:

- non-security severity `error`, or
- security severity `critical` or `high`

Lower severities (`warning` / `note`, security `medium` / `low`) annotate the PR but do not fail that check under this policy.

The Analyze job can still succeed when only lower-severity alerts are found; the blocking signal is the **Code scanning results** check. The workflow sets `wait-for-processing: true` on `github/codeql-action/analyze` so that check can finish before the job ends. Keep repository Code security settings on the default severity thresholds unless the project deliberately tightens or relaxes them.
