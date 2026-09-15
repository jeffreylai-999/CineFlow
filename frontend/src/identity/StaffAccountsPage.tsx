import { useEffect, useState, type FormEvent } from 'react'
import type { IdentityClient, StaffAccount, StaffSession } from '@/identity/api/identityClient.ts'
import { staffAccountErrorMessage } from '@/identity/staffAccountErrorMessage.ts'
import { staffRoleLabel } from '@/identity/staffRoleLabel.ts'
import { Button } from '@/components/ui/button.tsx'
import { Field, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field.tsx'
import { Input } from '@/components/ui/input.tsx'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'

type StaffAccountsPageProps = {
  session: StaffSession
  client: IdentityClient
  onLogout: () => void
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; accounts: StaffAccount[] }
  | { status: 'error'; message: string }

export function StaffAccountsPage({ session, client, onLogout }: StaffAccountsPageProps) {
  const [state, setState] = useState<LoadState>({ status: 'loading' })
  const [actionError, setActionError] = useState<string | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [creating, setCreating] = useState(false)
  const [resetPasswords, setResetPasswords] = useState<Record<number, string>>({})
  const [pendingId, setPendingId] = useState<number | null>(null)

  async function reload() {
    const next = await client.listStaffAccounts()
    setState({ status: 'ready', accounts: next })
  }

  useEffect(() => {
    let cancelled = false
    client
      .listStaffAccounts()
      .then((accounts) => {
        if (!cancelled) {
          setState({ status: 'ready', accounts })
        }
      })
      .catch(() => {
        if (!cancelled) {
          setState({
            status: 'error',
            message: 'Unable to load Staff accounts. Try again shortly.',
          })
        }
      })
    return () => {
      cancelled = true
    }
  }, [client])

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setCreating(true)
    setActionError(null)
    try {
      await client.createStaffAccount(username, password)
      setUsername('')
      setPassword('')
      await reload()
    } catch (cause) {
      setActionError(staffAccountErrorMessage(cause))
    } finally {
      setCreating(false)
    }
  }

  async function handleReset(account: StaffAccount) {
    setPendingId(account.id)
    setActionError(null)
    try {
      await client.resetStaffPassword(account.id, resetPasswords[account.id] ?? '')
      setResetPasswords((current) => ({ ...current, [account.id]: '' }))
      await reload()
    } catch (cause) {
      setActionError(staffAccountErrorMessage(cause))
    } finally {
      setPendingId(null)
    }
  }

  async function handleDeactivate(account: StaffAccount) {
    setPendingId(account.id)
    setActionError(null)
    try {
      await client.deactivateStaffAccount(account.id)
      await reload()
    } catch (cause) {
      setActionError(staffAccountErrorMessage(cause))
    } finally {
      setPendingId(null)
    }
  }

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="px-6 py-8">
        <h1 className="text-2xl font-semibold">Staff accounts</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Create Booking Staff with a chosen password. Deactivation and password reset revoke existing
          sessions.
        </p>

        {actionError ? (
          <p className="mt-4 text-sm text-destructive" role="alert">
            {actionError}
          </p>
        ) : null}

        <form className="mt-8 max-w-md" onSubmit={(event) => void handleCreate(event)}>
          <FieldSet>
            <FieldLegend>Create Booking Staff</FieldLegend>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="new-username">New username</FieldLabel>
                <Input
                  id="new-username"
                  name="username"
                  autoComplete="off"
                  minLength={3}
                  maxLength={32}
                  pattern="[a-z][a-z0-9._-]{2,31}"
                  title="3-32 lowercase letters, digits, dots, underscores, or hyphens"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  required
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="new-password">New password</FieldLabel>
                <Input
                  id="new-password"
                  name="password"
                  type="password"
                  autoComplete="new-password"
                  minLength={12}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                />
              </Field>
              <Button type="submit" disabled={creating}>
                {creating ? 'Creating…' : 'Create Booking Staff'}
              </Button>
            </FieldGroup>
          </FieldSet>
        </form>

        {state.status === 'loading' ? (
          <p className="mt-8" role="status">
            Loading Staff accounts…
          </p>
        ) : null}
        {state.status === 'error' ? (
          <p className="mt-8" role="alert">
            {state.message}
          </p>
        ) : null}

        {state.status === 'ready' ? (
          <div className="mt-8 overflow-x-auto">
            <table className="w-full min-w-[40rem] border-collapse text-left text-sm">
              <caption className="sr-only">Staff accounts</caption>
              <thead>
                <tr className="border-b border-border">
                  <th className="py-2 pr-4 font-medium" scope="col">
                    Username
                  </th>
                  <th className="py-2 pr-4 font-medium" scope="col">
                    Role
                  </th>
                  <th className="py-2 pr-4 font-medium" scope="col">
                    Status
                  </th>
                  <th className="py-2 font-medium" scope="col">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {state.accounts.map((account) => (
                  <tr key={account.id} className="border-b border-border">
                    <td className="py-3 pr-4 font-medium">{account.username}</td>
                    <td className="py-3 pr-4">{staffRoleLabel(account.role)}</td>
                    <td className="py-3 pr-4">{account.active ? 'Active' : 'Deactivated'}</td>
                    <td className="py-3">
                      <AccountActions
                        account={account}
                        pending={pendingId === account.id}
                        resetPassword={resetPasswords[account.id] ?? ''}
                        onResetPasswordChange={(value) =>
                          setResetPasswords((current) => ({ ...current, [account.id]: value }))
                        }
                        onReset={() => void handleReset(account)}
                        onDeactivate={() => void handleDeactivate(account)}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </StaffShell>
  )
}

type AccountActionsProps = {
  account: StaffAccount
  pending: boolean
  resetPassword: string
  onResetPasswordChange: (value: string) => void
  onReset: () => void
  onDeactivate: () => void
}

function AccountActions({
  account,
  pending,
  resetPassword,
  onResetPasswordChange,
  onReset,
  onDeactivate,
}: AccountActionsProps) {
  if (account.role !== 'BOOKING_STAFF') {
    return null
  }
  const resetId = `reset-password-${account.id}`
  return (
    <div className="flex flex-wrap items-end gap-2">
      <form
        className="flex flex-wrap items-end gap-2"
        onSubmit={(event) => {
          event.preventDefault()
          onReset()
        }}
      >
        <Field className="w-52">
          <FieldLabel htmlFor={resetId}>New password for {account.username}</FieldLabel>
          <Input
            id={resetId}
            name={resetId}
            type="password"
            autoComplete="new-password"
            minLength={12}
            required
            value={resetPassword}
            onChange={(event) => onResetPasswordChange(event.target.value)}
          />
        </Field>
        <Button type="submit" variant="outline" disabled={pending}>
          Reset password for {account.username}
        </Button>
      </form>
      {account.active ? (
        <Button type="button" variant="destructive" disabled={pending} onClick={onDeactivate}>
          Deactivate {account.username}
        </Button>
      ) : null}
    </div>
  )
}
