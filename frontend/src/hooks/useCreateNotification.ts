import { useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationsApi } from '@/api/notifications'
import { notificationKeys } from './useNotifications'

export function useCreateNotification() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: notificationsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.all })
    },
  })
}
