import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AppShell } from '@/components/layout/AppShell'
import { DashboardPage } from '@/pages/DashboardPage'
import { NotificationsPage } from '@/pages/NotificationsPage'
import { TemplatesPage } from '@/pages/TemplatesPage'
import { AuditPage } from '@/pages/AuditPage'
import { UsersPage } from '@/pages/UsersPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppShell />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/templates" element={<TemplatesPage />} />
          <Route path="/audit" element={<AuditPage />} />
          <Route path="/users" element={<UsersPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
