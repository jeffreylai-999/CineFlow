import { useEffect, useMemo, useState, type FormEvent } from 'react'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'
import { Button } from '@/components/ui/button.tsx'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card.tsx'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field.tsx'
import { Input } from '@/components/ui/input.tsx'
import { SeatGrid, type SeatGridItem } from '@/scheduling/SeatGrid.tsx'
import {
  isSchedulingRequestError,
  type Hall,
  type HallSummary,
  type SchedulingClient,
} from '@/scheduling/api/schedulingClient.ts'

type HallsPageProps = {
  session: StaffSession
  client: SchedulingClient
  onLogout: () => void
}

export function HallsPage({ session, client, onLogout }: HallsPageProps) {
  const [halls, setHalls] = useState<HallSummary[]>([])
  const [selected, setSelected] = useState<Hall | null>(null)
  const [name, setName] = useState('')
  const [rowCount, setRowCount] = useState('8')
  const [seatsPerRow, setSeatsPerRow] = useState('12')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    client
      .listHalls()
      .then((next) => {
        if (!cancelled) {
          setHalls(next)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('Unable to load Halls. Try again shortly.')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [client])

  const gridSeats = useMemo(
    () => (selected ? selected.seats.map(toGridSeat) : []),
    [selected],
  )

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const hall = await client.createHall({
        name: name.trim(),
        rowCount: Number(rowCount),
        seatsPerRow: Number(seatsPerRow),
      })
      setHalls((current) =>
        [...current.filter((item) => item.id !== hall.id), toSummary(hall)].sort((left, right) =>
          left.name.localeCompare(right.name),
        ),
      )
      setSelected(hall)
      setName('')
    } catch {
      setError('Unable to create the Hall. Check the rows and Seats per row.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleSelect(hallId: number) {
    setError(null)
    try {
      setSelected(await client.getHall(hallId))
    } catch {
      setError('Unable to load the Seat Map. Try again shortly.')
    }
  }

  async function handleSeatActivate(seatId: number) {
    if (!selected) {
      return
    }
    const hallId = selected.id
    const seat = selected.seats.find((item) => item.id === seatId)
    if (!seat) {
      return
    }
    setError(null)
    try {
      const updated = await client.setSeatDisabled(hallId, seatId, !seat.disabled)
      setSelected((current) => {
        if (!current || current.id !== hallId) {
          return current
        }
        return {
          ...current,
          seats: current.seats.map((item) => (item.id === updated.id ? updated : item)),
        }
      })
    } catch (cause) {
      if (isSchedulingRequestError(cause) && cause.code === 'scheduling.seat_not_disableable') {
        setError('This Seat cannot be disabled because it has an active Seat Hold or a future Booking.')
        return
      }
      setError('Unable to update the Seat. Try again shortly.')
    }
  }

  async function handleArchive() {
    if (!selected || selected.archivedAt) {
      return
    }
    setError(null)
    try {
      const hall = await client.archiveHall(selected.id)
      setSelected(hall)
      setHalls((current) => current.map((item) => (item.id === hall.id ? toSummary(hall) : item)))
    } catch {
      setError('Unable to archive the Hall. Try again shortly.')
    }
  }

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="flex flex-col gap-6 px-6 py-8">
        <header className="space-y-2">
          <h1 className="text-2xl font-semibold">Halls</h1>
          <p className="text-sm text-muted-foreground">
            Create a Hall from rows and Seats per row. The Seat Map stays labelled and cannot change
            shape after creation.
          </p>
        </header>

        <Card>
          <CardHeader>
            <CardTitle>Create Hall</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={(event) => void handleCreate(event)}>
              <FieldGroup>
                <Field>
                  <FieldLabel htmlFor="hall-name">Name</FieldLabel>
                  <Input
                    id="hall-name"
                    name="name"
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    required
                    maxLength={80}
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="hall-rows">Rows</FieldLabel>
                  <Input
                    id="hall-rows"
                    name="rowCount"
                    type="number"
                    min={1}
                    max={26}
                    value={rowCount}
                    onChange={(event) => setRowCount(event.target.value)}
                    required
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="hall-seats">Seats per row</FieldLabel>
                  <Input
                    id="hall-seats"
                    name="seatsPerRow"
                    type="number"
                    min={1}
                    max={40}
                    value={seatsPerRow}
                    onChange={(event) => setSeatsPerRow(event.target.value)}
                    required
                  />
                </Field>
                <Button type="submit" disabled={submitting || loading}>
                  {submitting ? 'Creating…' : 'Create Hall'}
                </Button>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>

        {loading ? <p role="status">Loading Halls…</p> : null}
        {error ? (
          <p role="alert" className="text-sm text-destructive">
            {error}
          </p>
        ) : null}

        <section className="space-y-3" aria-labelledby="hall-list-heading">
          <h2 id="hall-list-heading" className="text-lg font-medium">
            Existing Halls
          </h2>
          {halls.length === 0 && !loading ? <p>No Halls yet.</p> : null}
          <ul className="flex flex-col gap-2">
            {halls.map((hall) => (
              <li key={hall.id}>
                <Button
                  type="button"
                  variant={selected?.id === hall.id ? 'default' : 'outline'}
                  onClick={() => void handleSelect(hall.id)}
                >
                  {hall.name}
                  {hall.archivedAt ? ' (archived)' : ''}
                </Button>
              </li>
            ))}
          </ul>
        </section>

        {selected ? (
          <section className="space-y-4" aria-labelledby="seat-map-heading">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 id="seat-map-heading" className="text-lg font-medium">
                  {selected.name} Seat Map
                </h2>
                <p className="text-sm text-muted-foreground">
                  {selected.rowCount} rows · {selected.seatsPerRow} Seats per row
                  {selected.archivedAt ? ' · Archived — new Showtimes are blocked' : ''}
                </p>
              </div>
              {selected.archivedAt ? null : (
                <Button type="button" variant="outline" onClick={() => void handleArchive()}>
                  Archive Hall
                </Button>
              )}
            </div>
            <p className="text-sm text-muted-foreground">
              Enabled Seats use a solid border and ○. Disabled Seats use a dashed border and ✕.
              Activate a Seat to enable or disable it.
            </p>
            <SeatGrid seats={gridSeats} onSeatActivate={(seatId) => void handleSeatActivate(seatId)} />
          </section>
        ) : null}
      </div>
    </StaffShell>
  )
}

function toSummary(hall: Hall): HallSummary {
  return {
    id: hall.id,
    name: hall.name,
    rowCount: hall.rowCount,
    seatsPerRow: hall.seatsPerRow,
    archivedAt: hall.archivedAt,
  }
}

function toGridSeat(seat: Hall['seats'][number]): SeatGridItem {
  return {
    id: seat.id,
    label: seat.label,
    rowLabel: seat.rowLabel,
    seatNumber: seat.seatNumber,
    visualState: seat.disabled ? 'disabled' : 'enabled',
    pressed: seat.disabled,
    accessibleName: seat.disabled ? `Seat ${seat.label}, disabled` : `Seat ${seat.label}, enabled`,
  }
}
