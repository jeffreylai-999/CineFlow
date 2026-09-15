import { describe, expect, it, vi } from 'vitest'
import { createStaffSocket } from '@/identity/api/staffSocket.ts'

type FakeClient = {
  activate: () => void
  deactivate: () => Promise<void> | void
  onConnect: (() => void) | undefined
  onStompError: (() => void) | undefined
  onWebSocketClose: (() => void) | undefined
}

function fakeClient(): FakeClient {
  return {
    onConnect: undefined,
    onStompError: undefined,
    onWebSocketClose: undefined,
    activate() {
      queueMicrotask(() => this.onConnect?.())
    },
    deactivate() {
      return undefined
    },
  }
}

describe('createStaffSocket', () => {
  it('notifies when a live socket drops after connect', async () => {
    const client = fakeClient()
    const socket = createStaffSocket({ createClient: () => client })
    const onDisconnected = vi.fn()

    await socket.connect('token', onDisconnected)
    client.onWebSocketClose?.()

    expect(onDisconnected).toHaveBeenCalledOnce()
  })

  it('does not notify after an intentional disconnect', async () => {
    const client = fakeClient()
    const socket = createStaffSocket({ createClient: () => client })
    const onDisconnected = vi.fn()

    await socket.connect('token', onDisconnected)
    await socket.disconnect()
    client.onWebSocketClose?.()

    expect(onDisconnected).not.toHaveBeenCalled()
  })
})
