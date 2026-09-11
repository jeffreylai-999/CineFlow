import type { ReactNode } from 'react'

type CustomerShellProps = {
  children: ReactNode
}

/** Online Customer audience shell (mobile-first guided booking surface). */
export function CustomerShell({ children }: CustomerShellProps) {
  return (
    <div className="min-h-svh bg-background text-foreground">
      <main>{children}</main>
    </div>
  )
}
