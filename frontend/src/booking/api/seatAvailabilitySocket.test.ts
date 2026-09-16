import { describe, expect, it, vi } from 'vitest'
import { createSeatAvailabilitySocket } from '@/booking/api/seatAvailabilitySocket.ts'

type FakeClient = {
  activate: () => void
  deactivate: () => Promise<void> | void
  onConnect: (() => void) | undefined
  onStompError: (() => void) | undefined
  onWebSocketClose: (() => void) | undefined
  subscribe: (destination: string, callback: () => void) => void
}

function fakeClient(): FakeClient {
  return {
    onConnect: undefined,
    onStompError: undefined,
    onWebSocketClose: undefined,
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(),
  }
}

describe('createSeatAvailabilitySocket', () => {
  it('refreshes availability after connecting, reconnecting, and receiving an invalidation', () => {
    const client = fakeClient()
    const socket = createSeatAvailabilitySocket({ createClient: () => client })
    const refresh = vi.fn()

    socket.connect(11, refresh)
    client.onConnect?.()
    client.onConnect?.()
    const invalidation = vi.mocked(client.subscribe).mock.calls[0]?.[1]
    invalidation?.()

    expect(client.subscribe).toHaveBeenCalledWith(
      '/topic/showtimes/11/availability',
      expect.any(Function),
    )
    expect(refresh).toHaveBeenCalledTimes(3)
  })
})
