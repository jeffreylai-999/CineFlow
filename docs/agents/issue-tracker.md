# Issue tracker: GitHub

Issues and specs for this repo live as GitHub issues. Use the `gh` CLI for all operations.

## Conventions

- Create: `gh issue create --title "..." --body "..."`
- Read: `gh issue view <number> --comments`
- List: `gh issue list --state open`
- Comment: `gh issue comment <number> --body "..."`
- Label: `gh issue edit <number> --add-label "..."` or `--remove-label "..."`
- Close: `gh issue close <number> --comment "..."`

Infer the repository from `git remote -v`. This configuration becomes operational after the folder is connected to a GitHub remote.

## Pull requests as a triage surface

PRs as a request surface: no.

## Publishing and fetching

When a skill says "publish to the issue tracker," create a GitHub issue.
When it says "fetch the relevant ticket," run `gh issue view <number> --comments`.

## Project workflow

Track issues in [CineFlow Project #2](https://github.com/users/jeffreylai-999/projects/2).

- `Backlog`: the issue has an open blocker.
- `Ready`: every blocking issue is closed, so the issue can be claimed.
- `In progress`: implementation has started. Assign the issue to the current user and update this status as one claim operation.
- `In review`: a pull request linked to the issue is ready for review.
- `Done`: the issue is closed or its linked pull request is merged.

Before implementing an issue, confirm that it belongs to Project #2. Resolve the project item, Status field, and option IDs with `gh project item-list` and `gh project field-list`, then update Status with `gh project item-edit`. When an issue closes, move each newly unblocked dependent issue from `Backlog` to `Ready`.

## Wayfinding

Use a labelled GitHub issue as the map and linked sub-issues as decision tickets. Represent blocking relationships with GitHub issue dependencies where available, falling back to `Blocked by: #<number>`. Claim work by assigning it to the current user and resolve it with a closing comment.
