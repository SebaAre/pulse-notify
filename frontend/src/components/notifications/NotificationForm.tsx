import { FormEvent, useState } from 'react'
import { useCreateNotification } from '@/hooks/useCreateNotification'
import type { NotificationChannel } from '@/types/notification'

const channels: NotificationChannel[] = ['EMAIL', 'SMS', 'PUSH']

interface Props {
  defaultRecipient?: string
  onSuccess?: () => void
  onCancel?: () => void
}

export function NotificationForm({ defaultRecipient = '', onSuccess, onCancel }: Props) {
  const [recipient, setRecipient] = useState(defaultRecipient)
  const [subject, setSubject] = useState('')
  const [channel, setChannel] = useState<NotificationChannel>('EMAIL')
  const [messageBody, setMessageBody] = useState('')

  const { mutate, isPending, isError, error, reset } = useCreateNotification()

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    mutate(
      {
        recipient: recipient.trim(),
        subject: subject.trim() || undefined,
        channel,
        messageBody: messageBody.trim(),
      },
      {
        onSuccess: () => {
          setSubject('')
          setMessageBody('')
          onSuccess?.()
        },
      },
    )
  }

  const canSubmit = recipient.trim() && messageBody.trim() && !isPending

  return (
    <form onSubmit={handleSubmit} className="rounded-lg border bg-card p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold">New notification</h3>
        {onCancel && (
          <button
            type="button"
            onClick={() => {
              reset()
              onCancel()
            }}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            Cancel
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <Field label="Recipient" required>
          <input
            type="text"
            value={recipient}
            onChange={(e) => setRecipient(e.target.value)}
            placeholder="user@example.com"
            className={inputClass}
            required
          />
        </Field>

        <Field label="Channel" required>
          <select
            value={channel}
            onChange={(e) => setChannel(e.target.value as NotificationChannel)}
            className={inputClass}
          >
            {channels.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </Field>

        <Field label="Subject" className="sm:col-span-2">
          <input
            type="text"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            placeholder="Optional"
            maxLength={255}
            className={inputClass}
          />
        </Field>

        <Field label="Message" className="sm:col-span-2" required>
          <textarea
            value={messageBody}
            onChange={(e) => setMessageBody(e.target.value)}
            rows={3}
            className={`${inputClass} resize-y`}
            required
          />
        </Field>
      </div>

      {isError && (
        <div className="text-sm text-red-600">
          {error instanceof Error ? error.message : 'Failed to create notification'}
        </div>
      )}

      <div className="flex justify-end gap-2">
        <button
          type="submit"
          disabled={!canSubmit}
          className="h-9 px-4 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isPending ? 'Sending...' : 'Send'}
        </button>
      </div>
    </form>
  )
}

const inputClass =
  'w-full h-9 px-3 rounded-md border bg-background text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring'

function Field({
  label,
  children,
  required,
  className,
}: {
  label: string
  children: React.ReactNode
  required?: boolean
  className?: string
}) {
  return (
    <label className={`flex flex-col gap-1 ${className ?? ''}`}>
      <span className="text-xs font-medium text-muted-foreground">
        {label}
        {required && <span className="text-red-500 ml-0.5">*</span>}
      </span>
      {children}
    </label>
  )
}
