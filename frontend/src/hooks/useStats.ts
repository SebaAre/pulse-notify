import { useQuery } from '@tanstack/react-query'
import { notificationsApi } from '@/api/notifications'
import { notificationKeys } from './useNotifications'

export function useStats() {
  return useQuery({
    queryKey: notificationKeys.stats(),
    queryFn: notificationsApi.stats,
  })
}
