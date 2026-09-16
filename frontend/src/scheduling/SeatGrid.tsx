import { cn } from '@/lib/utils.ts'
import './seat-grid.css'

export type SeatVisualState = 'enabled' | 'disabled' | 'available' | 'unavailable' | 'selected'

export type SeatGridItem = {
  id: number
  label: string
  rowLabel: string
  seatNumber: number
  visualState: SeatVisualState
  pressed: boolean
}

type SeatGridProps = {
  seats: SeatGridItem[]
  onSeatActivate: (seatId: number) => void
}

const STATE_MARK: Record<SeatVisualState, string> = {
  enabled: '○',
  available: '○',
  selected: '●',
  unavailable: '■',
  disabled: '✕',
}

export function SeatGrid({ seats, onSeatActivate }: SeatGridProps) {
  const rows = groupRows(seats)
  return (
    <div className="flex flex-col gap-2" role="group" aria-label="Seat Map">
      {rows.map(([rowLabel, rowSeats]) => (
        <div key={rowLabel} className="flex items-center gap-2">
          <span className="w-6 text-sm font-medium text-muted-foreground" aria-hidden="true">
            {rowLabel}
          </span>
          <div className="flex flex-wrap gap-2">
            {rowSeats.map((seat) => (
              <button
                key={seat.id}
                type="button"
                aria-pressed={seat.pressed}
                aria-disabled={seat.visualState === 'unavailable' ? true : undefined}
                aria-label={seatAccessibleName(seat.label, seat.visualState)}
                data-state={seat.visualState}
                className={cn(
                  'flex size-12 flex-col items-center justify-center rounded-md border-2 text-xs font-medium',
                  'focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none',
                  seatClasses(seat.visualState),
                )}
                onClick={() => onSeatActivate(seat.id)}
              >
                <span aria-hidden="true">{STATE_MARK[seat.visualState]}</span>
                <span>{seat.label}</span>
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}

function seatAccessibleName(label: string, visualState: SeatVisualState): string {
  return `Seat ${label}, ${visualState}`
}

function seatClasses(state: SeatVisualState): string {
  switch (state) {
    case 'enabled':
    case 'available':
      return 'border-solid border-seat-available-border bg-secondary text-secondary-foreground'
    case 'selected':
      return 'border-solid border-seat-selected-border bg-seat-selected text-seat-selected-foreground'
    case 'unavailable':
      return 'border-dotted border-seat-unavailable-border bg-seat-unavailable text-seat-unavailable-foreground'
    case 'disabled':
      return 'border-dashed border-seat-disabled-border bg-seat-disabled text-seat-disabled-foreground'
    default: {
      const exhaustive: never = state
      return exhaustive
    }
  }
}

function groupRows(seats: SeatGridItem[]): Array<[string, SeatGridItem[]]> {
  const rows = new Map<string, SeatGridItem[]>()
  for (const seat of seats) {
    const row = rows.get(seat.rowLabel) ?? []
    row.push(seat)
    rows.set(seat.rowLabel, row)
  }
  return [...rows.entries()].map(([rowLabel, rowSeats]) => [
    rowLabel,
    [...rowSeats].sort((left, right) => left.seatNumber - right.seatNumber),
  ])
}
