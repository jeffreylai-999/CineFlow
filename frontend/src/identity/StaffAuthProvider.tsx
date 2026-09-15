import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import type { IdentityClient, StaffSession } from '@/identity/api/identityClient.ts'
import type { StaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthContext, type StaffAuthValue } from '@/identity/staffAuthContext.ts'

const MAX_SOCKET_RECOVERIES = 3

type StaffAuthProviderProps = {
  client: IdentityClient
  socket: StaffSocket
  children: ReactNode
}

export function StaffAuthProvider({ client, socket, children }: StaffAuthProviderProps) {
  const [session, setSession] = useState<StaffSession | null>(null)
  const [ready, setReady] = useState(false)

  const retrying = useRef(false)
  const recoveries = useRef(0)

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
    let cancelled = false
    const delayMs = Math.max(session.expiresInSeconds - 60, 0) * 1000
    const timer = window.setTimeout(() => {
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
    }, delayMs)
    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [client, session])

  useEffect(() => {
    if (!session) {
      recoveries.current = 0
      void socket.disconnect()
      return
    }
    let cancelled = false
    let backoffTimer: number | undefined

    function recover() {
      if (cancelled || retrying.current) {
        return
      }
      if (recoveries.current >= MAX_SOCKET_RECOVERIES) {
        return
      }
      recoveries.current += 1
      retrying.current = true
      const delayMs = recoveries.current === 1 ? 0 : Math.min(1000 * 2 ** (recoveries.current - 2), 8_000)
      backoffTimer = window.setTimeout(() => {
        void client
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
            retrying.current = false
          })
      }, delayMs)
    }

    void socket
      .connect(session.accessToken, recover)
      .then(() => {
        if (!cancelled) {
          recoveries.current = 0
        }
      })
      .catch(recover)
    return () => {
      cancelled = true
      retrying.current = false
      if (backoffTimer !== undefined) {
        window.clearTimeout(backoffTimer)
      }
      void socket.disconnect()
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
