import type { ReactElement } from 'react'
import { MemoryRouter } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import type { CatalogClient, Movie } from '@/catalog/api/catalogClient.ts'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { ShowtimesPage } from '@/scheduling/ShowtimesPage.tsx'
import {
  SchedulingRequestError,
  type HallSummary,
  type SchedulingClient,
  type Showtime,
} from '@/scheduling/api/schedulingClient.ts'

const administrator: StaffSession = {
  accessToken: 'admin-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

const nebula: Movie = {
  id: 8,
  title: 'Nebula Express',
  synopsis: 'A courier crew races a sealed cargo.',
  genre: 'Adventure',
  runtimeMinutes: 90,
  ageRating: 'PG-13',
  posterUrl: null,
}

const hall: HallSummary = {
  id: 4,
  name: 'Hall 1',
  rowCount: 6,
  seatsPerRow: 10,
  archivedAt: null,
}

const scheduled: Showtime = {
  id: 11,
  movieId: 8,
  movieTitle: 'Nebula Express',
  runtimeMinutes: 90,
  hallId: 4,
  hallName: 'Hall 1',
  startsAt: '2026-09-20T11:30:00Z',
  startsAtCinemaTime: '2026-09-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  occupancyEndsAt: '2026-09-20T13:15:00Z',
  adultPriceMyr: 28,
  childPriceMyr: 18,
}

function catalogStub(): CatalogClient {
  return {
    listMovies: vi.fn().mockResolvedValue([nebula]),
  }
}

function clientStub(overrides: Partial<SchedulingClient> = {}): SchedulingClient {
  return {
    listHalls: vi.fn().mockResolvedValue([hall]),
    createHall: vi.fn(),
    getHall: vi.fn(),
    setSeatDisabled: vi.fn(),
    archiveHall: vi.fn(),
    listShowtimes: vi.fn().mockResolvedValue([]),
    createShowtime: vi.fn().mockResolvedValue(scheduled),
    updateShowtimePrices: vi.fn(),
    removeShowtime: vi.fn(),
    ...overrides,
  }
}

function renderShowtimes(ui: ReactElement) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

describe('ShowtimesPage', () => {
  it('sends a zoneless Cinema Time payload without converting in the browser', async () => {
    const client = clientStub()

    await renderShowtimes(
      <ShowtimesPage
        session={administrator}
        client={client}
        catalogClient={catalogStub()}
        onLogout={() => undefined}
      />,
    )

    await expect.element(page.getByRole('heading', { name: 'Showtimes' })).toBeInTheDocument()
    await expect
      .element(page.getByRole('group', { name: 'Showtime in Cinema Time (Asia/Kuala_Lumpur)' }))
      .toBeInTheDocument()
    await page.getByLabelText('Movie').selectOptions('8')
    await page.getByLabelText('Hall').selectOptions('4')
    await page.getByLabelText('Date').fill('2026-09-20')
    await page.getByLabelText('Time').fill('19:30')
    await page.getByLabelText('Adult Ticket Price (MYR)').fill('28.00')
    await page.getByLabelText('Child Ticket Price (MYR)').fill('18.00')
    await page.getByRole('button', { name: 'Schedule Showtime' }).click()

    await expect
      .element(page.getByRole('status'))
      .toHaveTextContent('Scheduled Nebula Express at 2026-09-20T19:30:00.')
    await expect.element(page.getByText('Hall 1 · 2026-09-20T19:30:00 Asia/Kuala_Lumpur')).toBeInTheDocument()
    expect(client.createShowtime).toHaveBeenCalledWith({
      movieId: 8,
      hallId: 4,
      startsAtLocal: '2026-09-20T19:30',
      timeZone: 'Asia/Kuala_Lumpur',
      adultPriceMyr: 28,
      childPriceMyr: 18,
    })
  })

  it('surfaces a Hall overlap without hiding the existing schedule', async () => {
    const client = clientStub({
      listShowtimes: vi.fn().mockResolvedValue([scheduled]),
      createShowtime: vi
        .fn()
        .mockRejectedValue(new SchedulingRequestError(409, 'scheduling.showtime_overlap')),
    })

    await renderShowtimes(
      <ShowtimesPage
        session={administrator}
        client={client}
        catalogClient={catalogStub()}
        onLogout={() => undefined}
      />,
    )
    await expect.element(page.getByText('Hall 1 · 2026-09-20T19:30:00 Asia/Kuala_Lumpur')).toBeInTheDocument()
    await page.getByLabelText('Movie').selectOptions('8')
    await page.getByLabelText('Hall').selectOptions('4')
    await page.getByLabelText('Date').fill('2026-09-20')
    await page.getByLabelText('Time').fill('20:00')
    await page.getByRole('button', { name: 'Schedule Showtime' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'That Hall is occupied through the movie runtime and the fifteen-minute Cleaning Buffer.',
      )
    await expect.element(page.getByText('Hall 1 · 2026-09-20T19:30:00 Asia/Kuala_Lumpur')).toBeInTheDocument()
  })

  it('lets an Administrator change Ticket Prices after scheduling', async () => {
    const updated: Showtime = {
      ...scheduled,
      adultPriceMyr: 30.5,
      childPriceMyr: 16,
    }
    const client = clientStub({
      listShowtimes: vi.fn().mockResolvedValue([scheduled]),
      updateShowtimePrices: vi.fn().mockResolvedValue(updated),
    })

    await renderShowtimes(
      <ShowtimesPage
        session={administrator}
        client={client}
        catalogClient={catalogStub()}
        onLogout={() => undefined}
      />,
    )

    const prices = page.getByRole('group', {
      name: 'Ticket Prices for Nebula Express at 2026-09-20T19:30:00',
    })
    await prices.getByLabelText('Adult Ticket Price (MYR)').fill('30.50')
    await prices.getByLabelText('Child Ticket Price (MYR)').fill('16.00')
    await prices.getByRole('button', { name: 'Update Ticket Prices' }).click()

    await expect
      .element(page.getByRole('status'))
      .toHaveTextContent('Updated Ticket Prices for Nebula Express.')
    await expect.element(page.getByText('Adult RM 30.50 · Child RM 16.00')).toBeInTheDocument()
    expect(client.updateShowtimePrices).toHaveBeenCalledWith(11, {
      adultPriceMyr: 30.5,
      childPriceMyr: 16,
    })
  })

  it('has no serious axe violations on the Showtimes route', async () => {
    const screen = await renderShowtimes(
      <ShowtimesPage
        session={administrator}
        client={clientStub({ listShowtimes: vi.fn().mockResolvedValue([scheduled]) })}
        catalogClient={catalogStub()}
        onLogout={() => undefined}
      />,
    )
    await expect.element(page.getByText('Hall 1 · 2026-09-20T19:30:00 Asia/Kuala_Lumpur')).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
