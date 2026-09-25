import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import Logo from './Logo.jsx'

const navClass = ({ isActive }) =>
  `relative px-1 py-2 text-sm font-semibold transition ${
    isActive
      ? 'text-ink after:absolute after:inset-x-0 after:-bottom-[4px] md:after:-bottom-[13px] after:h-[3px] after:rounded-full after:bg-brand-600'
      : 'text-ink-soft hover:text-ink'
  }`

export default function Layout() {
  const { utente, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="flex min-h-dvh flex-col">
      <div className="z-30 px-4 pt-3 md:sticky md:top-0">
        <header className="card mx-auto flex max-w-6xl flex-wrap items-center gap-x-8 gap-y-2 px-5 py-3">
          <Link to="/" aria-label="Car Catalog, home"><Logo /></Link>
          <nav className="order-last flex w-full items-center gap-6 overflow-x-auto whitespace-nowrap md:order-none md:w-auto md:flex-1" aria-label="Principale">
            <NavLink to="/" end className={navClass}>Home</NavLink>
            <NavLink to="/auto-usate" className={navClass}>Auto usate</NavLink>
            {utente && <NavLink to="/preferiti" className={navClass}>Preferiti</NavLink>}
            {utente && <NavLink to="/avvisi" className={navClass}>Avvisi prezzo</NavLink>}
            {isAdmin && <NavLink to="/admin" className={navClass}>Pannello admin</NavLink>}
          </nav>
          <div className="ml-auto flex items-center gap-2">
            {utente ? (
              <>
                <Link to="/profilo" className="flex items-center gap-2 rounded-full py-1 pr-3 pl-1 text-sm font-semibold hover:bg-ground">
                  <span className="grid size-8 place-items-center rounded-full bg-ink text-xs text-white" aria-hidden="true">
                    {utente.nome.charAt(0).toUpperCase()}
                  </span>
                  {utente.nome}
                </Link>
                <button className="btn-ghost px-4 py-2" onClick={() => { logout(); navigate('/') }}>Esci</button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-ghost px-4 py-2">Accedi</Link>
                <Link to="/registrazione" className="btn-dark px-4 py-2">Registrati</Link>
              </>
            )}
          </div>
        </header>
      </div>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      {/* Privacy e Cookie Policy raggiungibili da OGNI pagina */}
      <footer className="mt-8 border-t border-line bg-white">
        <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 sm:grid-cols-[2fr_1fr_1fr]">
          <div className="space-y-3">
            <Logo />
            <p className="max-w-sm text-sm text-ink-soft">
              Auto usate selezionate, con avvisi via email quando il prezzo scende sotto la tua soglia.
            </p>
            <p className="max-w-sm text-xs text-ink-soft">
              Foto da Wikimedia Commons con licenze libere: autore e licenza nella scheda di ogni auto.
            </p>
          </div>
          <nav className="space-y-2 text-sm" aria-label="Catalogo">
            <p className="font-bold">Catalogo</p>
            <Link to="/auto-usate" className="block text-ink-soft hover:text-ink">Tutte le auto</Link>
            <Link to="/auto-usate?prezzoMax=15000" className="block text-ink-soft hover:text-ink">Fino a 15.000 €</Link>
            <Link to="/auto-usate?kmMax=20000" className="block text-ink-soft hover:text-ink">Meno di 20.000 km</Link>
          </nav>
          <nav className="space-y-2 text-sm" aria-label="Note legali">
            <p className="font-bold">Note legali</p>
            <Link to="/privacy" className="block text-ink-soft hover:text-ink">Privacy Policy</Link>
            <Link to="/cookie" className="block text-ink-soft hover:text-ink">Cookie Policy</Link>
          </nav>
        </div>
        <p className="border-t border-line py-4 text-center text-xs text-ink-soft">
          © {new Date().getFullYear()} Car Catalog — progetto didattico
        </p>
      </footer>
    </div>
  )
}
