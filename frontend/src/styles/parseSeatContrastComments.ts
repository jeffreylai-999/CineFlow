export type MeasuredSeatContrast = {
  token: string
  ratio: number
  note: string
}

/** Tokens that define Seat-state colour against a paired surface (WCAG 1.4.11 / 1.4.3). */
export const REQUIRED_SEAT_CONTRAST_TOKENS = [
  '--seat-available-border',
  '--seat-disabled',
  '--seat-disabled-border',
  '--seat-unavailable',
  '--seat-unavailable-border',
  '--seat-selected',
  '--seat-selected-border',
  '--seat-held',
  '--seat-held-border',
  '--seat-booked',
  '--seat-booked-border',
] as const

/**
 * Extracts measured contrast ratios recorded beside Seat-state token definitions.
 * Expected line shape: `--seat-…: #hex; /* … = 4.5:1` or `≈ 4.5:1` / `≈4.5:1`.
 */
export function parseSeatContrastComments(css: string): MeasuredSeatContrast[] {
  const results: MeasuredSeatContrast[] = []
  const linePattern =
    /(--seat-[\w-]+)\s*:\s*[^;]+;\s*\/\*\s*([^*]*?(\d+(?:\.\d+)?)\s*:?\s*1)[^*]*\*\//g

  for (const match of css.matchAll(linePattern)) {
    const token = match[1]
    const note = match[2].trim()
    const ratio = Number(match[3])
    if (!Number.isFinite(ratio)) {
      continue
    }
    results.push({ token, ratio, note })
  }

  return results
}

export function missingRequiredSeatContrastTokens(
  measured: readonly MeasuredSeatContrast[],
): string[] {
  const present = new Set(measured.map((entry) => entry.token))
  return REQUIRED_SEAT_CONTRAST_TOKENS.filter((token) => !present.has(token))
}

/** Non-text UI (borders) need 3:1; text-on-fill pairs need 4.5:1. */
export function seatContrastMeetsAa(token: string, ratio: number): boolean {
  if (token.endsWith('-border') || token === '--seat-available-border') {
    return ratio >= 3
  }
  return ratio >= 4.5
}
