export type UserRole = 'USER' | 'STAFF' | 'ADMIN'

export interface UserProfile {
  id: string
  username: string
  displayName: string
  role: UserRole
  villageId: string
  permissions: string[]
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: UserProfile
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
