export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH'
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED'

export interface Notification {
  id: string
  recipient: string
  subject: string | null
  channel: NotificationChannel
  messageBody: string
  status: NotificationStatus
  createdAt: string
  updatedAt: string
}

export interface CreateNotificationRequest {
  recipient: string
  subject?: string
  channel: NotificationChannel
  messageBody: string
}
