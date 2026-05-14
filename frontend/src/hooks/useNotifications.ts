import { useQuery } from '@tanstack/react-query'
import { notificationsApi } from '@/api/notifications'

export const notificationKeys = {
  all: ['notifications'] as const,
  list: (recipient: string) => [...notificationKeys.all, 'list', recipient] as const,
  detail: (id: string) => [...notificationKeys.all, 'detail', id] as const,
}

export function useNotifications(recipient: string) {
  return useQuery({
    queryKey: notificationKeys.list(recipient),
    queryFn: () => notificationsApi.list(recipient),
    enabled: recipient.length > 0,
  })
}
