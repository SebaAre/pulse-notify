import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="min-h-screen bg-background text-foreground flex items-center justify-center">
          <div className="rounded-lg border bg-card p-8 shadow-sm">
            <h1 className="text-3xl font-bold text-primary">PulseNotify</h1>
            <p className="text-muted-foreground mt-2">
              Hello world!
            </p>
          </div>
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App