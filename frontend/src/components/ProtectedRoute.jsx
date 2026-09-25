import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'

/** Solo UX: la vera protezione è sul backend (401/403/404). */
export default function ProtectedRoute({ admin = false }) {
  const { utente, isAdmin, caricamento } = useAuth()
  const location = useLocation()

  if (caricamento) return <p className="text-stone-500">Caricamento…</p>
  if (!utente) return <Navigate to="/login" replace state={{ da: location.pathname }} />
  if (admin && !isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}
