import type { ReactNode } from 'react'

type StaffShellProps = {
  children: ReactNode
}

/** Booking Staff / Administrator workspace shell (sidebar arrives with later tickets). */
export function StaffShell({ children }: StaffShellProps) {
  return (
    <div className="min-h-svh bg-background text-foreground">
      <div className="flex min-h-svh">
        <aside
          className="hidden w-56 shrink-0 border-r border-sidebar-border bg-sidebar p-4 text-sidebar-foreground md:block"
          aria-label="Staff navigation"
        >
          <p className="text-sm font-semibold tracking-wide text-sidebar-primary uppercase">
            Staff
          </p>
        </aside>
        <main className="flex-1">{children}</main>
      </div>
    </div>
  )
}
