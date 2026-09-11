# Test frontend components in a real browser

CineFlow runs its frontend component and route tests in Vitest browser mode with the Playwright provider, rather than rendering into jsdom. Browser mode left experimental status in Vitest 4, and the Vitest documentation recommends it over simulated environments for component testing specifically because jsdom does not reproduce focus management, real event propagation, or layout. Since CineFlow tests whole routes rather than isolated components, its tests exercise dialogs, menus, and comboboxes directly, which is where simulated environments are weakest.

The alternative was jsdom plus a maintained set of mocks. jsdom does not implement pointer capture, `scrollIntoView`, or `ResizeObserver`, all of which these primitives call, and the relevant jsdom issues have been open for years. Maintaining mocks for browser APIs that will not arrive, against an upstream that has closed the matching report as not planned, is a permanent cost for a lower-fidelity result. Base UI's own jsdom behavior is unverified either way, which argues the same direction: the test strategy should not depend on an untested compatibility story.

The accessibility consequence decided it. No library in this stack claims WCAG conformance — shadcn, Base UI, and Radix all claim WAI-ARIA Authoring Practices adherence and assistive-technology testing only, and both primitive libraries state explicitly that visible focus indication and color contrast are the consuming application's responsibility. Automated tooling cannot close that gap: Deque measures roughly 57% of issues as automatically detectable, and reports zero automated detection for Focus Order, Focus Visible, Non-text Contrast, and Meaningful Sequence — precisely the criteria the seat map and the dialogs depend on. Color contrast is the single largest automatically detectable category, and `jest-axe` and `vitest-axe` disable contrast checking under jsdom entirely, so jsdom silently forfeits it.

## Consequences

CI installs Playwright browsers, and test runs are slower than an equivalent jsdom suite. Accepted in exchange for fidelity and for recovering the contrast category.

Automated accessibility checks run through `@axe-core/playwright` against every route, with dialogs and menus opened first, because axe does not inspect hidden regions such as closed dialogs.

Because the four criteria above are not automatable, each release also requires a keyboard-only walkthrough of catalog browsing, seat selection, checkout, administration, and admission. Seat-state contrast ratios are measured by hand and recorded as comments beside the token definitions rather than assumed to pass.
