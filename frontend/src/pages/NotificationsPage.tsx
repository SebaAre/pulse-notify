import { FormEvent, useState } from 'react'
import { format } from 'date-fns'
import { Search } from 'lucide-react'
import { useNotifications } from '@/hooks/useNotifications'
import type { NotificationChannel, NotificationStatus } from '@/types/notification'
import { cn } from '@/lib/utils'

const channelStyles: Record<NotificationChannel, string> = {
  EMAIL: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
  SMS: 'bg-purple-500/10 text-purple-600 dark:text-purple-400',
  PUSH: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
}

const statusStyles: Record<NotificationStatus, string> = {
  PENDING: 'bg-muted text-muted-foreground',
  SENT: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
  FAILED: 'bg-red-500/10 text-red-600 dark:text-red-400',
}

export function NotificationsPage() {
  const [input, setInput] = useState('')
  const [recipient, setRecipient] = useState('')

  const { data, isLoading, isError, error } = useNotifications(recipient)

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setRecipient(input.trim())
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Notifications</h2>
        <p className="text-sm text-muted-foreground">
          Search notifications by recipient email or phone.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex gap-2 max-w-md">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="recipient@example.com"
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

      <div className="rounded-lg border bg-card overflow-hidden">
        {!recipient && (
          <EmptyState message="Enter a recipient and press Search to list notifications." />
        )}
        {recipient && isLoading && <EmptyState message="Loading..." />}
        {recipient && isError && (
          <EmptyState
            message={`Error: ${error instanceof Error ? error.message : 'unknown'}`}
            tone="error"
          />
        )}
        {recipient && data && data.length === 0 && (
          <EmptyState message={`No notifications for "${recipient}".`} />
        )}
        {recipient && data && data.length > 0 && (
          <table className="w-full text-sm">
            <thead className="bg-muted/50 text-xs uppercase text-muted-foreground">
              <tr>
                <th className="text-left px-4 py-3 font-medium">Subject</th>
                <th className="text-left px-4 py-3 font-medium">Channel</th>
                <th className="text-left px-4 py-3 font-medium">Status</th>
                <th className="text-left px-4 py-3 font-medium">Created</th>
              </tr>
            </thead>
            <tbody>
              {data.map((n) => (
                <tr key={n.id} className="border-t hover:bg-muted/30">
                  <td className="px-4 py-3">
                    {n.subject ?? <span className="text-muted-foreground italic">no subject</span>}
                  </td>
                  <td className="px-4 py-3">
                    <Badge className={channelStyles[n.channel]}>{n.channel}</Badge>
                  </td>
                  <td className="px-4 py-3">
                    <Badge className={statusStyles[n.status]}>{n.status}</Badge>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">
                    {format(new Date(n.createdAt), 'PP p')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

function Badge({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
        className,
      )}
    >
      {children}
    </span>
  )
}

function EmptyState({
  message,
  tone = 'muted',
}: {
  message: string
  tone?: 'muted' | 'error'
}) {
  return (
    <div
      className={cn(
        'p-8 text-center text-sm',
        tone === 'error' ? 'text-red-600' : 'text-muted-foreground',
      )}
    >
      {message}
    </div>
  )
}
