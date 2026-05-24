import { useQuery } from '@tanstack/react-query'
import { templatesApi } from '@/api/templates'

export const templateKeys = {
  all: ['templates'] as const,
  list: () => [...templateKeys.all, 'list'] as const,
  detail: (id: string) => [...templateKeys.all, 'detail', id] as const,
}

export function useTemplates() {
  return useQuery({
    queryKey: templateKeys.list(),
    queryFn: templatesApi.list,
  })
}
