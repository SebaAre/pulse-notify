import { FormEvent, useState } from 'react'
import { format } from 'date-fns'
import { Search } from 'lucide-react'
import { useAuditEvents } from '@/hooks/useAuditEvents'
import type { AuditEvent } from '@/types/audit'
import { cn } from '@/lib/utils'

export function AuditPage() {
  const [input, setInput] = useState('')
  const [notificationId, setNotificationId] = useState('')

  const { data, isLoading, isError, error } = useAuditEvents(notificationId)

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setNotificationId(input.trim())
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Audit log</h2>
        <p className="text-sm text-muted-foreground">
          Immutable event trail for a notification — sourced from DynamoDB.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex gap-2 max-w-xl">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="notification UUID"
            className="w-full h-9 pl-9 pr-3 rounded-md border bg-background text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring font-mono"
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
        {!notificationId && (
          <EmptyState message="Paste a notification UUID to see its event trail." />
        )}
        {notificationId && isLoading && <EmptyState message="Loading..." />}
        {notificationId && isError && (
          <EmptyState
            message={`Error: ${error instanceof Error ? error.message : 'unknown'}`}
            tone="error"
          />
        )}
        {notificationId && data && data.length === 0 && (
          <EmptyState message="No events found for that notification." />
        )}
        {notificationId && data && data.length > 0 && (
          <ul className="divide-y">
            {data.map((event, idx) => (
              <Event key={`${event.timestamp}-${idx}`} event={event} />
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

function Event({ event }: { event: AuditEvent }) {
  return (
    <li className="px-4 py-3 hover:bg-muted/30">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3 min-w-0">
          <span
            className={cn(
              'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium shrink-0',
              eventTypeStyle(event.eventType),
            )}
          >
            {event.eventType}
          </span>
          {event.channel && (
            <span className="text-xs text-muted-foreground shrink-0">
              {event.channel}
            </span>
          )}
          {event.recipient && (
            <span className="text-sm text-muted-foreground truncate">
              → {event.recipient}
            </span>
          )}
          {event.attemptNumber !== null && event.attemptNumber !== undefined && (
            <span className="text-xs text-muted-foreground shrink-0">
              attempt #{event.attemptNumber}
            </span>
          )}
        </div>
        <span className="text-xs text-muted-foreground shrink-0 tabular-nums">
          {format(new Date(event.timestamp), 'PP p')}
        </span>
      </div>
      {event.errorMessage && (
        <div className="mt-1 text-xs text-red-600">
          {event.errorCode && <span className="font-mono">{event.errorCode}: </span>}
          {event.errorMessage}
        </div>
      )}
    </li>
  )
}

function eventTypeStyle(eventType: string): string {
  if (eventType.includes('completed')) {
    return 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
  }
  if (eventType.includes('failed')) {
    return 'bg-red-500/10 text-red-600 dark:text-red-400'
  }
  if (eventType.includes('attempted')) {
    return 'bg-amber-500/10 text-amber-600 dark:text-amber-400'
  }
  if (eventType.includes('requested')) {
    return 'bg-blue-500/10 text-blue-600 dark:text-blue-400'
  }
  return 'bg-muted text-muted-foreground'
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
