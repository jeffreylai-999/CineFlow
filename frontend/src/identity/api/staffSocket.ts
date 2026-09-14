import { Client } from '@stomp/stompjs'

export type StaffSocket = {
  connect: (accessToken: string) => Promise<void>
  disconnect: () => Promise<void>
}

type StompLike = {
  activate: () => void
  deactivate: () => Promise<void> | void
  onConnect: (() => void) | undefined
  onStompError: (() => void) | undefined
  onWebSocketClose: (() => void) | undefined
}

export type StaffSocketOptions = {
  createClient?: (accessToken: string) => StompLike
}

export function createStaffSocket(options: StaffSocketOptions = {}): StaffSocket {
  let active: StompLike | null = null

  async function disconnect() {
    const current = active
    active = null
    if (current) {
      await current.deactivate()
    }
  }

  return {
    async connect(accessToken: string) {
      await disconnect()
      const client = options.createClient
        ? options.createClient(accessToken)
        : createBrowserClient(accessToken)
      active = client
      await new Promise<void>((resolve, reject) => {
        client.onConnect = () => resolve()
        client.onStompError = () => reject(new Error('STOMP connection rejected'))
        client.onWebSocketClose = () => reject(new Error('STOMP socket closed'))
        client.activate()
      })
    },
    disconnect,
  }
}

function createBrowserClient(accessToken: string): StompLike {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const client = new Client({
    brokerURL: `${protocol}://${window.location.host}/ws`,
    connectHeaders: { Authorization: `Bearer ${accessToken}` },
    reconnectDelay: 0,
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
  }
  return adapter
}
