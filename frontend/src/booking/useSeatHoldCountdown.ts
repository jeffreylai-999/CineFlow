import { useEffect, useState } from 'react'
import { type SeatHold } from '@/booking/api/customerClient.ts'

/**
 * Counts down the remaining Seat Hold time using the server-issued duration
 * (expiresAt - serverTime) measured against local monotonic time, so clock
 * skew between server and browser does not distort the countdown. Expiry is
 * reported exactly once, then the countdown stops.
 */
export function useSeatHoldCountdown(
  hold: SeatHold | null,
  receivedAt: number,
  onExpired?: () => void,
): number | null {
  const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null)

  useEffect(() => {
    if (!hold) {
      return
    }
    const duration = Date.parse(hold.expiresAt) - Date.parse(hold.serverTime)
    let expired = false
    let interval: number | undefined
    const updateRemainingTime = () => {
      const elapsed = performance.now() - receivedAt
      const remaining = Math.max(0, Math.ceil((duration - elapsed) / 1_000))
      setRemainingSeconds(remaining)
      if (remaining === 0) {
        expired = true
        if (interval !== undefined) {
          window.clearInterval(interval)
          interval = undefined
        }
        onExpired?.()
      }
    }
    updateRemainingTime()
    if (!expired) {
      interval = window.setInterval(updateRemainingTime, 1_000)
    }
    return () => {
      if (interval !== undefined) {
        window.clearInterval(interval)
      }
    }
  }, [hold, receivedAt, onExpired])

  return remainingSeconds
}
