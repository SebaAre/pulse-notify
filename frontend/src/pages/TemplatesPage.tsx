import { format } from 'date-fns'
import { useTemplates } from '@/hooks/useTemplates'
import type { NotificationChannel } from '@/types/notification'
import { cn } from '@/lib/utils'

const channelStyles: Record<NotificationChannel, string> = {
  EMAIL: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
  SMS: 'bg-purple-500/10 text-purple-600 dark:text-purple-400',
  PUSH: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
}

export function TemplatesPage() {
  const { data, isLoading, isError, error } = useTemplates()

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Templates</h2>
        <p className="text-sm text-muted-foreground">
          Notification templates rendered by template-service.
        </p>
      </div>

      <div className="rounded-lg border bg-card overflow-hidden">
        {isLoading && <EmptyState message="Loading templates..." />}
        {isError && (
          <EmptyState
            message={`Error: ${error instanceof Error ? error.message : 'unknown'}`}
            tone="error"
          />
        )}
        {data && data.length === 0 && (
          <EmptyState message="No templates yet." />
        )}
        {data && data.length > 0 && (
          <table className="w-full text-sm">
            <thead className="bg-muted/50 text-xs uppercase text-muted-foreground">
              <tr>
                <th className="text-left px-4 py-3 font-medium">Name</th>
                <th className="text-left px-4 py-3 font-medium">Channel</th>
                <th className="text-left px-4 py-3 font-medium">Subject</th>
                <th className="text-left px-4 py-3 font-medium">Updated</th>
              </tr>
            </thead>
            <tbody>
              {data.map((t) => (
                <tr key={t.id} className="border-t hover:bg-muted/30">
                  <td className="px-4 py-3 font-medium">{t.name}</td>
                  <td className="px-4 py-3">
                    <Badge className={channelStyles[t.channel]}>{t.channel}</Badge>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">
                    {t.subject ?? (
                      <span className="italic">no subject</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">
                    {format(new Date(t.updatedAt), 'PP p')}
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
