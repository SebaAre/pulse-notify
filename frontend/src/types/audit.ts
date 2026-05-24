export interface AuditEvent {
  notificationId: string
  timestamp: string
  eventType: string
  channel: string | null
  recipient: string | null
  attemptNumber: number | null
  errorCode: string | null
  errorMessage: string | null
  providerMessageId: string | null
}
