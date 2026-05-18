import { useStats } from '@/hooks/useStats'
import type { NotificationChannel, NotificationStatus } from '@/types/notification'
import { cn } from '@/lib/utils'

const statusStyles: Record<NotificationStatus, string> = {
  PENDING: 'bg-muted text-muted-foreground',
  SENT: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
  FAILED: 'bg-red-500/10 text-red-600 dark:text-red-400',
}

const channelStyles: Record<NotificationChannel, string> = {
  EMAIL: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
  SMS: 'bg-purple-500/10 text-purple-600 dark:text-purple-400',
  PUSH: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
}

export function DashboardPage() {
  const { data, isLoading, isError, error } = useStats()

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Dashboard</h2>
        <p className="text-sm text-muted-foreground">
          Notification volume across the platform.
        </p>
      </div>

      {isLoading && <Message text="Loading stats..." />}
      {isError && (
        <Message
          text={`Error: ${error instanceof Error ? error.message : 'unknown'}`}
          tone="error"
        />
      )}

      {data && (
        <div className="space-y-6">
          <div className="rounded-lg border bg-card p-6">
            <div className="text-xs uppercase tracking-wide text-muted-foreground">
              Total notifications
            </div>
            <div className="mt-2 text-4xl font-semibold tabular-nums">
              {data.total}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <Breakdown
              title="By status"
              entries={Object.entries(data.byStatus) as [NotificationStatus, number][]}
              styles={statusStyles}
            />
            <Breakdown
              title="By channel"
              entries={Object.entries(data.byChannel) as [NotificationChannel, number][]}
              styles={channelStyles}
            />
          </div>
        </div>
      )}
    </div>
  )
}

function Breakdown<K extends string>({
  title,
  entries,
  styles,
}: {
  title: string
  entries: [K, number][]
  styles: Record<K, string>
}) {
  return (
    <div className="rounded-lg border bg-card p-6 space-y-4">
      <h3 className="text-sm font-semibold">{title}</h3>
      <ul className="space-y-3">
        {entries.map(([key, count]) => (
          <li key={key} className="flex items-center justify-between">
            <span
              className={cn(
                'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
                styles[key],
              )}
            >
              {key}
            </span>
            <span className="text-lg font-semibold tabular-nums">{count}</span>
          </li>
        ))}
      </ul>
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
