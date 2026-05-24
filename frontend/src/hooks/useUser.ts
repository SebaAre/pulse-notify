import { useQuery } from '@tanstack/react-query'
import { usersApi } from '@/api/users'

export const userKeys = {
  all: ['users'] as const,
  byEmail: (email: string) => [...userKeys.all, 'email', email] as const,
  byId: (id: string) => [...userKeys.all, 'id', id] as const,
}

export function useUserByEmail(email: string) {
  return useQuery({
    queryKey: userKeys.byEmail(email),
    queryFn: () => usersApi.getByEmail(email),
    enabled: email.length > 0,
  })
}
