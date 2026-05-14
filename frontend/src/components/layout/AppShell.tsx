import { NavLink, Outlet } from 'react-router-dom'
import { Bell, Inbox, LayoutDashboard, Users } from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/notifications', label: 'Notifications', icon: Inbox },
  { to: '/users', label: 'Users', icon: Users },
]

export function AppShell() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <aside className="fixed inset-y-0 left-0 w-60 border-r bg-card flex flex-col">
        <div className="h-14 flex items-center gap-2 px-4 border-b">
          <Bell className="h-5 w-5 text-primary" />
          <span className="font-semibold">PulseNotify</span>
        </div>
        <nav className="flex-1 p-2 space-y-1">
          {navItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors',
                  isActive
                    ? 'bg-primary/10 text-primary font-medium'
                    : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="pl-60">
        <header className="h-14 border-b bg-card/50 backdrop-blur flex items-center justify-between px-6">
          <h1 className="text-sm font-medium text-muted-foreground">
            Notification platform
          </h1>
          <div className="text-xs text-muted-foreground">dev</div>
        </header>
        <main className="p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
