import { apiClient } from './client'
import type {
  CreateNotificationRequest,
  Notification,
  NotificationStats,
} from '@/types/notification'

export const notificationsApi = {
  list: async (recipient: string): Promise<Notification[]> => {
    const { data } = await apiClient.get<Notification[]>('/notifications', {
      params: { recipient },
    })
    return data
  },

  getById: async (id: string): Promise<Notification> => {
    const { data } = await apiClient.get<Notification>(`/notifications/${id}`)
    return data
  },

  create: async (
    request: CreateNotificationRequest,
  ): Promise<Notification> => {
    const { data } = await apiClient.post<Notification>(
      '/notifications',
      request,
    )
    return data
  },

  stats: async (): Promise<NotificationStats> => {
    const { data } = await apiClient.get<NotificationStats>(
      '/notifications/stats',
    )
    return data
  },
}
