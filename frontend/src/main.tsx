import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import App from '@/App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime:        60_000,
      retry:            1,
      refetchOnWindowFocus: false,
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
        <Toaster
          position="top-center"
          toastOptions={{
            duration: 3200,
            style: {
              borderRadius: '999px',
              fontWeight: 600,
              fontSize: '0.9rem',
              background: '#fff',
              color: '#1a3d2b',
              border: '1px solid #b7e4c7',
              boxShadow: '0 4px 20px rgba(52,120,80,.15)',
            },
            success: { iconTheme: { primary: '#52b788', secondary: '#fff' } },
            error:   { iconTheme: { primary: '#e57373', secondary: '#fff' } },
          }}
        />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
