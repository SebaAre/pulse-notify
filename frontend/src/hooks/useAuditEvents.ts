import { useQuery } from '@tanstack/react-query'
import { auditApi } from '@/api/audit'

export const auditKeys = {
  all: ['audit'] as const,
  byNotification: (notificationId: string) =>
    [...auditKeys.all, 'notification', notificationId] as const,
}

export function useAuditEvents(notificationId: string) {
  return useQuery({
    queryKey: auditKeys.byNotification(notificationId),
    queryFn: () => auditApi.getByNotificationId(notificationId),
    enabled: notificationId.length > 0,
  })
}
