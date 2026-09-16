import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page, userEvent } from 'vitest/browser'
import axe from 'axe-core'
import { SeatGrid, type SeatGridItem } from '@/scheduling/SeatGrid.tsx'

const seats: SeatGridItem[] = [
  {
    id: 1,
    label: 'A1',
    rowLabel: 'A',
    seatNumber: 1,
    visualState: 'enabled',
    pressed: false,
    accessibleName: 'Seat A1, enabled',
  },
  {
    id: 2,
    label: 'A2',
    rowLabel: 'A',
    seatNumber: 2,
    visualState: 'disabled',
    pressed: true,
    accessibleName: 'Seat A2, disabled',
  },
]

describe('SeatGrid', () => {
  it('exposes Seat status without relying on color and stays keyboard operable', async () => {
    const onSeatActivate = vi.fn()
    await render(<SeatGrid seats={seats} onSeatActivate={onSeatActivate} />)

    const enabled = page.getByRole('button', { name: 'Seat A1, enabled' })
    const disabled = page.getByRole('button', { name: 'Seat A2, disabled' })
    await expect.element(enabled).toBeInTheDocument()
    await expect.element(disabled).toBeInTheDocument()
    await expect.element(enabled).toHaveAttribute('data-state', 'enabled')
    await expect.element(disabled).toHaveAttribute('data-state', 'disabled')
    await expect.element(page.getByText('A1')).toBeInTheDocument()
    await expect.element(page.getByText('○')).toBeInTheDocument()
    await expect.element(page.getByText('✕')).toBeInTheDocument()

    await enabled.click()
    expect(onSeatActivate).toHaveBeenCalledWith(1)

    onSeatActivate.mockClear()
    enabled.element().focus()
    await userEvent.keyboard('{Enter}')
    expect(onSeatActivate).toHaveBeenCalledWith(1)
  })

  it('renders customer available, selected, and unavailable states with marks and labels', async () => {
    const onSeatActivate = vi.fn()
    await render(
      <SeatGrid
        seats={[
          {
            id: 1,
            label: 'A1',
            rowLabel: 'A',
            seatNumber: 1,
            visualState: 'available',
            pressed: false,
            accessibleName: 'Seat A1, available',
          },
          {
            id: 2,
            label: 'A2',
            rowLabel: 'A',
            seatNumber: 2,
            visualState: 'selected',
            pressed: true,
            accessibleName: 'Seat A2, selected',
          },
          {
            id: 3,
            label: 'A3',
            rowLabel: 'A',
            seatNumber: 3,
            visualState: 'unavailable',
            pressed: false,
            accessibleName: 'Seat A3, unavailable',
          },
        ]}
        onSeatActivate={onSeatActivate}
      />,
    )

    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toHaveAttribute(
      'data-state',
      'available',
    )
    await expect.element(page.getByRole('button', { name: 'Seat A2, selected' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await expect.element(page.getByRole('button', { name: 'Seat A3, unavailable' })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
    await expect.element(page.getByText('●')).toBeInTheDocument()
    await expect.element(page.getByText('■')).toBeInTheDocument()
    await expect.element(page.getByText('A3')).toBeInTheDocument()
  })

  it('has no serious axe violations', async () => {
    const screen = await render(<SeatGrid seats={seats} onSeatActivate={() => undefined} />)
    await expect.element(page.getByRole('button', { name: 'Seat A1, enabled' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
