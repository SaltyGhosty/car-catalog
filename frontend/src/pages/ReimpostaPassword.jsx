import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { motion } from 'motion/react'
import { api } from '../api.js'
import Messaggio from '../components/Messaggio.jsx'

/** Il token arriva nel fragment (#token=...): non finisce nei log dei server né nel Referer. */
function leggiToken() {
  const token = new URLSearchParams(window.location.hash.slice(1)).get('token')
  if (token) window.history.replaceState(null, '', window.location.pathname)
  return token
}

export default function ReimpostaPassword() {
  const navigate = useNavigate()
  const [token] = useState(leggiToken)
  const [form, setForm] = useState({ password: '', conferma: '' })
  const [errore, setErrore] = useState('')
  const [fatto, setFatto] = useState(false)
  const [invio, setInvio] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setErrore('')
    if (form.password.length < 10) { setErrore('La password deve avere almeno 10 caratteri'); return }
    if (form.password !== form.conferma) { setErrore('Le due password non coincidono'); return }
    setInvio(true)
    try {
      await api.auth.reimpostaPassword(token, form.password)
      setFatto(true)
      setTimeout(() => navigate('/login', { replace: true }), 2500)
    } catch (err) {
      setErrore(Object.values(err.campi ?? {})[0] ?? err.message)
    } finally {
      setInvio(false)
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="card mx-auto max-w-md space-y-5 p-8">
      <h1 className="text-2xl font-bold">Scegli una nuova password</h1>
      {!token && (
        <Messaggio tipo="errore">
          Link non valido. <Link to="/password-dimenticata" className="underline">Richiedine uno nuovo</Link>.
        </Messaggio>
      )}
      {fatto && <Messaggio tipo="ok">Password aggiornata. Ti porto alla pagina di accesso…</Messaggio>}
      {token && !fatto && (
        <form onSubmit={submit} className="space-y-4">
          <div>
            <label htmlFor="pw1" className="label">Nuova password (min. 10 caratteri)</label>
            <input id="pw1" type="password" autoComplete="new-password" required minLength={10} maxLength={72} className="input"
                   value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div>
            <label htmlFor="pw2" className="label">Ripeti la password</label>
            <input id="pw2" type="password" autoComplete="new-password" required maxLength={72} className="input"
                   value={form.conferma} onChange={(e) => setForm({ ...form, conferma: e.target.value })} />
          </div>
          <Messaggio tipo="errore">{errore}</Messaggio>
          <button className="btn-primary w-full" disabled={invio}>{invio ? 'Salvataggio…' : 'Salva la nuova password'}</button>
        </form>
      )}
    </motion.div>
  )
}
