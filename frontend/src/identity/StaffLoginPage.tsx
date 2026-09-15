import { useState, type FormEvent } from 'react'
import { staffSignInErrorMessage } from '@/identity/staffSignInErrorMessage.ts'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'
import { Button } from '@/components/ui/button.tsx'
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field.tsx'
import { Input } from '@/components/ui/input.tsx'

type StaffLoginPageProps = {
  onSignedIn?: () => void
}

export function StaffLoginPage({ onSignedIn }: StaffLoginPageProps) {
  const { signIn } = useStaffAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await signIn(username, password)
      onSignedIn?.()
    } catch (cause) {
      setError(staffSignInErrorMessage(cause))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-svh w-full max-w-md flex-col justify-center gap-6 px-4 py-10">
      <header className="space-y-2">
        <p className="text-sm font-semibold tracking-[0.08em] text-primary uppercase">CineFlow</p>
        <h1 className="text-2xl font-semibold">Staff sign in</h1>
        <p className="text-sm text-muted-foreground">Booking Staff and Administrator access only.</p>
      </header>
      <form onSubmit={(event) => void handleSubmit(event)}>
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="username">Username</FieldLabel>
            <Input
              id="username"
              name="username"
              autoComplete="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="password">Password</FieldLabel>
            <Input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </Field>
          {error ? <FieldError>{error}</FieldError> : null}
          <Button type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </FieldGroup>
      </form>
    </div>
  )
}
