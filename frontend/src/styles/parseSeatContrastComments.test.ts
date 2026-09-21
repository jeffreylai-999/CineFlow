import { describe, expect, it } from 'vitest'
import cinematicFocusCss from '@/styles/cinematic-focus.css?raw'
import {
  missingRequiredSeatContrastTokens,
  parseSeatContrastComments,
  seatContrastMeetsAa,
} from '@/styles/parseSeatContrastComments.ts'

describe('parseSeatContrastComments', () => {
  it('reads measured ratios from independent comment literals', () => {
    const css = `
      --seat-held-border: #e0a83c; /* dotted border against held fill ≈ 6.15:1 */
      --seat-disabled: #3a2433; /* disabled-foreground on disabled = 10.21:1 */
    `
    expect(parseSeatContrastComments(css)).toEqual([
      {
        token: '--seat-held-border',
        ratio: 6.15,
        note: 'dotted border against held fill ≈ 6.15:1',
      },
      {
        token: '--seat-disabled',
        ratio: 10.21,
        note: 'disabled-foreground on disabled = 10.21:1',
      },
    ])
  })

  it('treats border tokens as non-text UI and fills as text pairs for AA', () => {
    expect(seatContrastMeetsAa('--seat-held-border', 3)).toBe(true)
    expect(seatContrastMeetsAa('--seat-held-border', 2.9)).toBe(false)
    expect(seatContrastMeetsAa('--seat-held', 4.5)).toBe(true)
    expect(seatContrastMeetsAa('--seat-held', 4.4)).toBe(false)
  })
})

describe('Seat-state contrast comments', () => {
  it('records a measured ratio beside every required Seat token', () => {
    const measured = parseSeatContrastComments(cinematicFocusCss)
    expect(missingRequiredSeatContrastTokens(measured)).toEqual([])
  })

  it('keeps every recorded Seat-state ratio at or above WCAG AA for its role', () => {
    const measured = parseSeatContrastComments(cinematicFocusCss)
    for (const entry of measured) {
      expect(
        seatContrastMeetsAa(entry.token, entry.ratio),
        `${entry.token} ratio ${entry.ratio}:1 (${entry.note})`,
      ).toBe(true)
    }
  })
})
