import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { motion } from 'motion/react'
import { useAuth } from '../auth/AuthContext.jsx'
import Messaggio from '../components/Messaggio.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [errore, setErrore] = useState('')
  const [invio, setInvio] = useState(false)
  const [bloccatoFino, setBloccatoFino] = useState(0)
  const [ora, setOra] = useState(() => Date.now())
  const secondi = Math.max(0, Math.ceil((bloccatoFino - ora) / 1000))

  // Countdown del blocco dopo 3 tentativi falliti (il vero limite è sul server)
  useEffect(() => {
    if (!bloccatoFino) return
    const t = setInterval(() => {
      const adesso = Date.now()
      setOra(adesso)
      if (adesso >= bloccatoFino) { setBloccatoFino(0); setErrore('') }
    }, 250)
    return () => clearInterval(t)
  }, [bloccatoFino])

  async function submit(e) {
    e.preventDefault()
    setErrore(''); setInvio(true)
    try {
      await login(form.email, form.password)
      navigate(location.state?.da ?? '/', { replace: true })
    } catch (err) {
      if (err.status === 429 && err.retryAfter) {
        const adesso = Date.now()
        setOra(adesso)
        setBloccatoFino(adesso + err.retryAfter * 1000)
        setErrore('Troppi tentativi falliti: l’accesso a questo account è bloccato per 1 minuto.')
      } else {
        setErrore(err.message)
      }
    } finally {
      setInvio(false)
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="card mx-auto max-w-md p-8">
      <h1 className="mb-6 text-2xl font-bold">Accedi</h1>
      <form onSubmit={submit} className="space-y-4">
        <div>
          <label htmlFor="email" className="label">Email</label>
          <input id="email" type="email" autoComplete="email" required className="input"
                 value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </div>
        <div>
          <div className="flex items-baseline justify-between">
            <label htmlFor="pw" className="label">Password</label>
            <Link to="/password-dimenticata" className="text-xs font-semibold text-brand-600 hover:underline">
              Hai dimenticato la password?
            </Link>
          </div>
          <input id="pw" type="password" autoComplete="current-password" required className="input"
                 value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </div>
        <Messaggio tipo="errore">{errore}</Messaggio>
        <button className="btn-primary w-full" disabled={invio || secondi > 0}>
          {secondi > 0 ? `Riprova tra ${secondi} s` : invio ? 'Accesso…' : 'Accedi'}
        </button>
      </form>
      <p className="mt-4 text-sm text-stone-500">
        Non hai un account? <Link to="/registrazione" className="text-brand-700 hover:underline">Registrati</Link>
      </p>
    </motion.div>
  )
}
