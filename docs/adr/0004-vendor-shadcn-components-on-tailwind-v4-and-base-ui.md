# Vendor shadcn/ui components on Tailwind v4 and Base UI

The CineFlow web interface uses Tailwind CSS v4 through the `@tailwindcss/vite` plugin, React 19, and component source vendored into the repository with the shadcn CLI rather than consumed as a component-library dependency. The Cinematic Focus visual direction and the WCAG 2.2 AA target both require editing component internals, so owning the source outright is preferred to restyling and forking a packaged library. Components are added only when a screen needs one; the catalog is never installed wholesale.

Vendored components sit on Base UI, which became the shadcn default in July 2026. Radix is not deprecated and every component still ships for both, so `shadcn init -b radix` remains a genuine escape hatch. Two component choices exist specifically to keep the primitive tree single-sourced: `combobox` is used for provider search instead of `command`, because `command` depends on `cmdk` and therefore installs Radix packages into a Base UI project, and `toast` is used instead of `sonner`, which pulls `next-themes` into a Vite application that has no use for it.

Theming follows shadcn's own token set unchanged as a base layer, with domain tokens such as `--seat-held` layered on top and defined only for concepts the CineFlow glossary names. Deep navy surfaces are applied by overriding `--background`, `--card`, and `--popover`, because no shipped base color is navy. Only a dark theme is defined for the first release; a light palette is a later token file rather than a refactor, since every token is already semantic.

## Considered options

A versioned component library such as MUI or Mantine was rejected because a bespoke visual direction means fighting its theme system, and its internals cannot be patched for an accessibility gap without a fork anyway. Bare primitives with entirely hand-written components were rejected as more work than vendoring for the same result. Tailwind v3 was rejected because Tailwind v4's browser floor — Chrome 111, Safari 16.4, Firefox 128 — clears CineFlow's current-and-previous-major browser requirement with years of margin, and the current shadcn registry and documentation target v4.

## Consequences

Vendored components receive no upstream bug or security fixes automatically, and there is no lockfile entry recording which upstream revision each file came from. Components are therefore vendored in commits that touch nothing else, so that the file history answers the question.

CineFlow initializes with `style: "new-york"`, `tailwind.baseColor: "zinc"`, and `tailwind.cssVariables: true`. `new-york` is the only style the documentation still offers, `default` having been deprecated. `zinc` is the coolest of the offered base colors and so the shortest distance from the navy the theme overrides to; no offered base color is navy. `cssVariables` must be true, because the theme is expressed as token overrides rather than utility classes.

The documentation states that all three of those settings cannot be changed after `init`, but they are not equally fixed. `cssVariables` is the hardest: switching it means deleting and re-installing every component. `style` has no documented migration. `baseColor` is the exception — `shadcn migrate base-color` is documented as switching a theme from one base color to another, so that choice is reversible at a cost rather than permanent. Treat `style` and `cssVariables` as the real one-way doors.

The published `components.json` schema and the documentation disagree on the legal values of `style` and `baseColor`: the documentation offers one style and seven base colors, the schema a longer list of each. Verify the intended values against the installed CLI before running `init` rather than against either document.

The `shadcn` package remains a build dependency, because generated CSS imports `shadcn/tailwind.css`. It is retained rather than ejected, since ejecting inlines roughly 630 lines of base CSS that nothing in CineFlow edits. The CLI's `eject` command reverses this in one step if theme overrides begin to conflict with it.

The registry contains no grid, seat map, or two-dimensional selection component, and neither does Base UI. The seat map, the seat-hold countdown, and QR rendering and scanning are built from primitives regardless of this decision.
