export interface User {
  id: string
  email: string
  phone: string | null
  displayName: string
  timezone: string
  active: boolean
  createdAt: string
  updatedAt: string
}
