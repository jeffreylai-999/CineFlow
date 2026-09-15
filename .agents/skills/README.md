# Vendored Matt Pocock skills

These are the invokable Cursor project skills from [mattpocock/skills](https://github.com/mattpocock/skills), copied into `.agents/skills/` so cloud and Project agents can load them. Cursor discovers project skills from this path automatically.

This is **not** the same as `docs/agents/` (issue tracker, triage labels, domain layout). Those files are the output of `/setup-matt-pocock-skills`.

| Field | Value |
| --- | --- |
| Upstream | https://github.com/mattpocock/skills |
| Revision | [`959a8e9f1edc3adbe2f7e3054bb6fbefa6696260`](https://github.com/mattpocock/skills/commit/959a8e9f1edc3adbe2f7e3054bb6fbefa6696260) |
| License | MIT (see [LICENSE](./LICENSE)) |
| Installer | `npx skills@latest add mattpocock/skills -a cursor --copy` |

Included: the published engineering and productivity pack (`ask-matt`, `setup-matt-pocock-skills`, `grill-with-docs`, `triage`, `implement`, and the rest listed in `skills-lock.json`). Course/tooling extras from that repo (for example `scaffold-exercises`, `setup-pre-commit`) were left out.

Refresh from upstream with `npx skills update -p -y` at the repo root.
