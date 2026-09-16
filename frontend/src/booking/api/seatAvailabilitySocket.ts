import { Client } from '@stomp/stompjs'

export type SeatAvailabilitySocket = {
  connect: (showtimeId: number, onAvailabilityChanged: () => void) => void
  disconnect: () => Promise<void>
}

type StompLike = {
  activate: () => void
  deactivate: () => Promise<void> | void
  onConnect: (() => void) | undefined
  onStompError: (() => void) | undefined
  onWebSocketClose: (() => void) | undefined
  subscribe: (destination: string, callback: () => void) => void
}

export type SeatAvailabilitySocketOptions = {
  createClient?: () => StompLike
}

export function createSeatAvailabilitySocket(
  options: SeatAvailabilitySocketOptions = {},
): SeatAvailabilitySocket {
  let active: StompLike | null = null

  return {
    connect(showtimeId, onAvailabilityChanged) {
      void active?.deactivate()
      const client = options.createClient ? options.createClient() : createBrowserClient()
      active = client
      client.onConnect = () => {
        if (active !== client) {
          return
        }
        client.subscribe(`/topic/showtimes/${showtimeId}/availability`, onAvailabilityChanged)
        onAvailabilityChanged()
      }
      client.activate()
    },
    async disconnect() {
      const current = active
      active = null
      if (current) {
        current.onConnect = undefined
        current.onStompError = undefined
        current.onWebSocketClose = undefined
        await current.deactivate()
      }
    },
  }
}

function createBrowserClient(): StompLike {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const client = new Client({
    brokerURL: `${protocol}://${window.location.host}/ws`,
    reconnectDelay: 1_000,
  })
  const adapter: StompLike = {
    onConnect: undefined,
    onStompError: undefined,
    onWebSocketClose: undefined,
    activate() {
      client.onConnect = () => adapter.onConnect?.()
      client.onStompError = () => adapter.onStompError?.()
      client.onWebSocketClose = () => adapter.onWebSocketClose?.()
      client.activate()
    },
    deactivate() {
      return client.deactivate()
    },
    subscribe(destination, callback) {
      client.subscribe(destination, callback)
    },
  }
  return adapter
}
