import { Link } from 'react-router'

export default function NotFound() {
  return (
    <div className="py-20 text-center">
      <p className="text-6xl font-bold text-stone-300">404</p>
      <p className="mt-2 text-stone-600">Pagina non trovata.</p>
      <Link to="/" className="btn-primary mt-6">Torna al catalogo</Link>
    </div>
  )
}
