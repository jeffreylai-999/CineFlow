import type { StaffRole } from '@/identity/api/identityClient.ts'
import type { ReactNode } from 'react'
import { NavLink } from 'react-router'
import { Button } from '@/components/ui/button.tsx'
import { staffRoleLabel } from '@/identity/staffRoleLabel.ts'

type StaffShellProps = {
  children: ReactNode
  role: StaffRole
  username: string
  onLogout: () => void
}

export function StaffShell({ children, role, username, onLogout }: StaffShellProps) {
  return (
    <div className="min-h-svh bg-background text-foreground">
      <div className="flex min-h-svh">
        <aside
          className="w-56 shrink-0 border-r border-sidebar-border bg-sidebar p-4 text-sidebar-foreground"
          aria-label="Staff navigation"
        >
          <p className="text-sm font-semibold tracking-wide text-sidebar-primary uppercase">Staff</p>
          <nav className="mt-6 flex flex-col gap-2 text-sm">
            <NavLink className="rounded-md px-2 py-1 hover:bg-sidebar-accent" to="/staff">
              Overview
            </NavLink>
            {role === 'ADMINISTRATOR' ? (
              <>
                <NavLink className="rounded-md px-2 py-1 hover:bg-sidebar-accent" to="/staff/halls">
                  Halls
                </NavLink>
                <NavLink className="rounded-md px-2 py-1 hover:bg-sidebar-accent" to="/staff/accounts">
                  Staff accounts
                </NavLink>
              </>
            ) : null}
          </nav>
        </aside>
        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex items-center justify-between border-b border-border px-4 py-3">
            <p className="text-sm text-muted-foreground">
              Signed in as <span className="text-foreground">{username}</span> ({staffRoleLabel(role)})
            </p>
            <Button type="button" variant="outline" onClick={onLogout}>
              Sign out
            </Button>
          </header>
          <main className="flex-1">{children}</main>
        </div>
      </div>
    </div>
  )
}
