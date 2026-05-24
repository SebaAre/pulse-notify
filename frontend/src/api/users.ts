import { apiClient } from './client'
import type { User } from '@/types/user'

export const usersApi = {
  getByEmail: async (email: string): Promise<User> => {
    const { data } = await apiClient.get<User>('/users', {
      params: { email },
    })
    return data
  },

  getById: async (id: string): Promise<User> => {
    const { data } = await apiClient.get<User>(`/users/${id}`)
    return data
  },
}
