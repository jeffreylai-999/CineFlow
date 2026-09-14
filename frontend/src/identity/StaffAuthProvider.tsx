import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import type { IdentityClient, StaffSession } from '@/identity/api/identityClient.ts'
import type { StaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthContext, type StaffAuthValue } from '@/identity/staffAuthContext.ts'

type StaffAuthProviderProps = {
  client: IdentityClient
  socket: StaffSocket
  children: ReactNode
}

export function StaffAuthProvider({ client, socket, children }: StaffAuthProviderProps) {
  const [session, setSession] = useState<StaffSession | null>(null)
  const [ready, setReady] = useState(false)

  const retrying = useRef(false)

  useEffect(() => {
    let cancelled = false
    client
      .refresh()
      .then((next) => {
        if (!cancelled) {
          setSession(next)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setSession(null)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setReady(true)
        }
      })
    return () => {
      cancelled = true
    }
  }, [client])

  useEffect(() => {
    if (!session) {
      return
    }
    const delayMs = Math.max((session.expiresInSeconds - 60) * 1000, 5_000)
    const timer = window.setTimeout(() => {
      client
        .refresh()
        .then((next) => setSession(next))
        .catch(() => setSession(null))
    }, delayMs)
    return () => window.clearTimeout(timer)
  }, [client, session])

  useEffect(() => {
    if (!session) {
      void socket.disconnect()
      return
    }
    let cancelled = false
    void socket.connect(session.accessToken).catch(async () => {
      if (cancelled || retrying.current) {
        return
      }
      retrying.current = true
      try {
        const next = await client.refresh()
        if (!cancelled) {
          setSession(next)
        }
      } catch {
        if (!cancelled) {
          setSession(null)
        }
      } finally {
        retrying.current = false
      }
    })
    return () => {
      cancelled = true
    }
  }, [client, session, socket])

  const value = useMemo<StaffAuthValue>(
    () => ({
      session,
      ready,
      async signIn(username: string, password: string) {
        const next = await client.login(username, password)
        setSession(next)
      },
      async signOut() {
        try {
          await client.logout()
        } finally {
          setSession(null)
          await socket.disconnect()
        }
      },
    }),
    [client, ready, session, socket],
  )

  return <StaffAuthContext.Provider value={value}>{children}</StaffAuthContext.Provider>
}
