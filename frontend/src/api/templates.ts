import { apiClient } from './client'
import type { CreateTemplateRequest, Template } from '@/types/template'

export const templatesApi = {
  list: async (): Promise<Template[]> => {
    const { data } = await apiClient.get<Template[]>('/templates')
    return data
  },

  getById: async (id: string): Promise<Template> => {
    const { data } = await apiClient.get<Template>(`/templates/${id}`)
    return data
  },

  create: async (request: CreateTemplateRequest): Promise<Template> => {
    const { data } = await apiClient.post<Template>('/templates', request)
    return data
  },

  update: async (
    id: string,
    request: CreateTemplateRequest,
  ): Promise<Template> => {
    const { data } = await apiClient.put<Template>(`/templates/${id}`, request)
    return data
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/templates/${id}`)
  },
}
