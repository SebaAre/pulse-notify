import { apiClient } from './client'
import type { AuditEvent } from '@/types/audit'

export const auditApi = {
  getByNotificationId: async (
    notificationId: string,
  ): Promise<AuditEvent[]> => {
    const { data } = await apiClient.get<AuditEvent[]>(
      `/audit/${notificationId}`,
    )
    return data
  },
}
