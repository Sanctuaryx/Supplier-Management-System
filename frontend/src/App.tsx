import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PotentialSuppliersPage } from './components/PotentialSuppliersPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-white">
        <header className="bg-black text-white">
          <div className="w-full px-8 py-5 flex items-center gap-5">
            <span className="font-bold text-xl tracking-[0.3em] uppercase">Inditex</span>
            <span className="w-px h-5 bg-gray-600" />
            <span className="text-gray-400 text-sm tracking-widest uppercase">Supplier Management</span>
          </div>
        </header>
        <main>
          <PotentialSuppliersPage />
        </main>
      </div>
    </QueryClientProvider>
  )
}
