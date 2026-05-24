import { FormEvent, useState } from 'react'
import { format } from 'date-fns'
import { Mail, Phone, Clock, CheckCircle2, XCircle } from 'lucide-react'
import { useUserByEmail } from '@/hooks/useUser'
import type { User } from '@/types/user'
import { cn } from '@/lib/utils'

export function UsersPage() {
  const [input, setInput] = useState('')
  const [email, setEmail] = useState('')

  const { data, isLoading, isError, error } = useUserByEmail(email)

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setEmail(input.trim())
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Users</h2>
        <p className="text-sm text-muted-foreground">
          Look up user preferences and channel configuration.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex gap-2 max-w-md">
        <div className="relative flex-1">
          <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="email"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="user@example.com"
            className="w-full h-9 pl-9 pr-3 rounded-md border bg-background text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
          />
        </div>
        <button
          type="submit"
          disabled={!input.trim()}
          className="h-9 px-4 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Search
        </button>
      </form>

      {!email && (
        <Message text="Enter an email and press Search to look up a user." />
      )}
      {email && isLoading && <Message text="Loading..." />}
      {email && isError && (
        <Message
          text={`Error: ${error instanceof Error ? error.message : 'unknown'}`}
          tone="error"
        />
      )}
      {email && data && <UserCard user={data} />}
    </div>
  )
}

function UserCard({ user }: { user: User }) {
  return (
    <div className="rounded-lg border bg-card p-6 space-y-4 max-w-2xl">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold">{user.displayName}</h3>
          <p className="text-sm text-muted-foreground">{user.email}</p>
        </div>
        {user.active ? (
          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 px-2 py-0.5 text-xs font-medium">
            <CheckCircle2 className="h-3 w-3" />
            Active
          </span>
        ) : (
          <span className="inline-flex items-center gap-1 rounded-full bg-muted text-muted-foreground px-2 py-0.5 text-xs font-medium">
            <XCircle className="h-3 w-3" />
            Inactive
          </span>
        )}
      </div>

      <dl className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
        <Field icon={<Phone className="h-4 w-4" />} label="Phone">
          {user.phone ?? <span className="italic text-muted-foreground">not set</span>}
        </Field>
        <Field icon={<Clock className="h-4 w-4" />} label="Timezone">
          {user.timezone}
        </Field>
      </dl>

      <div className="pt-3 border-t text-xs text-muted-foreground space-y-1">
        <div>Created: {format(new Date(user.createdAt), 'PP p')}</div>
        <div>Updated: {format(new Date(user.updatedAt), 'PP p')}</div>
        <div className="font-mono">ID: {user.id}</div>
      </div>
    </div>
  )
}

function Field({
  icon,
  label,
  children,
}: {
  icon: React.ReactNode
  label: string
  children: React.ReactNode
}) {
  return (
    <div>
      <dt className="flex items-center gap-1.5 text-xs uppercase tracking-wide text-muted-foreground">
        {icon}
        {label}
      </dt>
      <dd className="mt-1 text-sm">{children}</dd>
    </div>
  )
}

function Message({
  text,
  tone = 'muted',
}: {
  text: string
  tone?: 'muted' | 'error'
}) {
  return (
    <div
      className={cn(
        'rounded-lg border bg-card p-8 text-center text-sm',
        tone === 'error' ? 'text-red-600' : 'text-muted-foreground',
      )}
    >
      {text}
    </div>
  )
}
