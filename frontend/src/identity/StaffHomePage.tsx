import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'

type StaffHomePageProps = {
  session: StaffSession
  onLogout: () => void
}

export function StaffHomePage({ session, onLogout }: StaffHomePageProps) {
  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="px-6 py-8">
        <h1 className="text-2xl font-semibold">Staff portal</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Short-lived access stays in this browser tab. Refresh uses a Secure HttpOnly cookie.
        </p>
      </div>
    </StaffShell>
  )
}
