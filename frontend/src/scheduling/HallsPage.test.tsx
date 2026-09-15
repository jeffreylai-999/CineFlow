import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { HallsPage } from '@/scheduling/HallsPage.tsx'
import {
  SchedulingRequestError,
  type Hall,
  type HallSummary,
  type SchedulingClient,
  type Seat,
} from '@/scheduling/api/schedulingClient.ts'

const administrator: StaffSession = {
  accessToken: 'admin-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

const hallOne: Hall = {
  id: 10,
  name: 'Hall 1',
  rowCount: 2,
  seatsPerRow: 2,
  archivedAt: null,
  seats: [
    { id: 1, rowLabel: 'A', seatNumber: 1, label: 'A1', disabled: false },
    { id: 2, rowLabel: 'A', seatNumber: 2, label: 'A2', disabled: false },
    { id: 3, rowLabel: 'B', seatNumber: 1, label: 'B1', disabled: false },
    { id: 4, rowLabel: 'B', seatNumber: 2, label: 'B2', disabled: false },
  ],
}

function clientStub(overrides: Partial<SchedulingClient> = {}): SchedulingClient {
  return {
    listHalls: vi.fn().mockResolvedValue([]),
    createHall: vi.fn(),
    getHall: vi.fn(),
    setSeatDisabled: vi.fn(),
    archiveHall: vi.fn(),
    ...overrides,
  }
}

function deferred<T>() {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((next) => {
    resolve = next
  })
  return { promise, resolve }
}

describe('HallsPage', () => {
  it('lets an Administrator create a Hall and operate its Seat Map', async () => {
    const created = hallOne
    const client = clientStub({
      createHall: vi.fn().mockResolvedValue(created),
      setSeatDisabled: vi.fn().mockResolvedValue({ ...created.seats[0], disabled: true }),
      archiveHall: vi.fn().mockResolvedValue({ ...created, archivedAt: '2026-09-15T12:00:00Z' }),
    })

    await render(<HallsPage session={administrator} client={client} onLogout={() => undefined} />)

    await expect.element(page.getByRole('heading', { name: 'Halls' })).toBeInTheDocument()
    await expect.element(page.getByText('No Halls yet.')).toBeInTheDocument()

    await page.getByLabelText('Name').fill('Hall 1')
    await page.getByLabelText('Rows').fill('2')
    await page.getByLabelText('Seats per row').fill('2')
    await page.getByRole('button', { name: 'Create Hall' }).click()

    await expect.element(page.getByRole('heading', { name: 'Hall 1 Seat Map' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat A1, enabled' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat B2, enabled' })).toBeInTheDocument()
    expect(client.createHall).toHaveBeenCalledWith({ name: 'Hall 1', rowCount: 2, seatsPerRow: 2 })

    await page.getByRole('button', { name: 'Seat A1, enabled' }).click()
    await expect.element(page.getByRole('button', { name: 'Seat A1, disabled' })).toBeInTheDocument()
    expect(client.setSeatDisabled).toHaveBeenCalledWith(10, 1, true)

    await page.getByRole('button', { name: 'Archive Hall' }).click()
    await expect.element(page.getByText(/Archived — new Showtimes are blocked/)).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Hall 1 (archived)' })).toBeInTheDocument()
  })

  it('explains when a Seat cannot be disabled', async () => {
    const client = clientStub({
      listHalls: vi.fn().mockResolvedValue([hallOne]),
      getHall: vi.fn().mockResolvedValue(hallOne),
      setSeatDisabled: vi
        .fn()
        .mockRejectedValue(new SchedulingRequestError(409, 'scheduling.seat_not_disableable')),
    })

    await render(<HallsPage session={administrator} client={client} onLogout={() => undefined} />)
    await page.getByRole('button', { name: 'Hall 1' }).click()
    await page.getByRole('button', { name: 'Seat A1, enabled' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'This Seat cannot be disabled because it has an active Seat Hold or a future Booking.',
      )
  })

  it('has no serious axe violations on the Halls route', async () => {
    const client = clientStub({
      listHalls: vi.fn().mockResolvedValue([hallOne]),
      getHall: vi.fn().mockResolvedValue(hallOne),
    })
    const screen = await render(
      <HallsPage session={administrator} client={client} onLogout={() => undefined} />,
    )
    await page.getByRole('button', { name: 'Hall 1' }).click()
    await expect.element(page.getByRole('button', { name: 'Seat A1, enabled' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })

  it('does not accept Create Hall until existing Halls have loaded', async () => {
    const pending = deferred<HallSummary[]>()
    const client = clientStub({
      listHalls: vi.fn().mockReturnValue(pending.promise),
    })

    await render(<HallsPage session={administrator} client={client} onLogout={() => undefined} />)

    await expect.element(page.getByRole('button', { name: 'Create Hall' })).toBeDisabled()

    pending.resolve([])

    await expect.element(page.getByText('No Halls yet.')).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Create Hall' })).toBeEnabled()
  })

  it('keeps the first Seat update when a second toggle is still in flight', async () => {
    const first = deferred<Seat>()
    const second = deferred<Seat>()
    const client = clientStub({
      listHalls: vi.fn().mockResolvedValue([hallOne]),
      getHall: vi.fn().mockResolvedValue(hallOne),
      setSeatDisabled: vi.fn().mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise),
    })

    await render(<HallsPage session={administrator} client={client} onLogout={() => undefined} />)
    await page.getByRole('button', { name: 'Hall 1' }).click()
    await page.getByRole('button', { name: 'Seat A1, enabled' }).click()
    await page.getByRole('button', { name: 'Seat A2, enabled' }).click()

    first.resolve({ ...hallOne.seats[0], disabled: true })
    await expect.element(page.getByRole('button', { name: 'Seat A1, disabled' })).toBeInTheDocument()

    second.resolve({ ...hallOne.seats[1], disabled: true })
    await expect.element(page.getByRole('button', { name: 'Seat A2, disabled' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat A1, disabled' })).toBeInTheDocument()
  })
})
