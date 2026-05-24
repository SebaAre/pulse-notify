import type { NotificationChannel } from './notification'

export interface Template {
  id: string
  name: string
  channel: NotificationChannel
  subject: string | null
  body: string
  createdAt: string
  updatedAt: string
}

export interface CreateTemplateRequest {
  name: string
  channel: NotificationChannel
  subject?: string
  body: string
}
