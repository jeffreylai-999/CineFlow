# Domain docs

Before exploring, read `CONTEXT.md` and relevant records under `docs/adr/` when they exist. Missing files are created lazily by domain-modeling workflows and should not block other work.

This is a single-context repository:

```text
/
├── CONTEXT.md
├── docs/adr/
├── legacy/          # preserved 2017 Java Swing reference (not the modern app)
└── src/             # modern application modules (added by later tickets)
```

Use terminology defined in `CONTEXT.md`. If a proposal contradicts an ADR, identify the conflict explicitly instead of silently overriding the decision.
