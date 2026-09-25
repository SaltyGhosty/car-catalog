import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { motion } from 'motion/react'
import { useAuth } from '../auth/AuthContext.jsx'
import Messaggio from '../components/Messaggio.jsx'

export default function Registrazione() {
  const { registrazione } = useAuth()
  const navigate = useNavigate()
  // NB: nessun campo "ruolo": lo decide il server
  const [form, setForm] = useState({ nome: '', email: '', password: '', privacyAccettata: false })
  const [errori, setErrori] = useState({})
  const [errore, setErrore] = useState('')
  const [invio, setInvio] = useState(false)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  async function submit(e) {
    e.preventDefault()
    setErrore(''); setErrori({}); setInvio(true)
    try {
      await registrazione(form)
      navigate('/', { replace: true })
    } catch (err) {
      setErrore(err.message)
      setErrori(err.campi ?? {})
    } finally {
      setInvio(false)
    }
  }

  const campo = (id, label, props) => (
    <div>
      <label htmlFor={id} className="label">{label}</label>
      <input id={id} className="input" required value={form[id]} onChange={set(id)} {...props} />
      {errori[id] && <p className="mt-1 text-xs text-red-600">{errori[id]}</p>}
    </div>
  )

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="card mx-auto max-w-md p-8">
      <h1 className="mb-6 text-2xl font-bold">Crea un account</h1>
      <form onSubmit={submit} className="space-y-4" noValidate>
        {campo('nome', 'Nome', { autoComplete: 'given-name', maxLength: 60 })}
        {campo('email', 'Email', { type: 'email', autoComplete: 'email', maxLength: 254 })}
        {campo('password', 'Password (min. 10 caratteri)', { type: 'password', autoComplete: 'new-password', minLength: 10, maxLength: 72 })}
        <label className="flex items-start gap-2 text-sm text-stone-600">
          <input type="checkbox" className="mt-1" checked={form.privacyAccettata} onChange={set('privacyAccettata')} />
          <span>
            Ho letto la <Link to="/privacy" target="_blank" className="text-brand-700 underline">Privacy Policy</Link> e
            la <Link to="/cookie" target="_blank" className="text-brand-700 underline">Cookie Policy</Link>.
          </span>
        </label>
        {errori.privacyAccettata && <p className="text-xs text-red-600">{errori.privacyAccettata}</p>}
        <Messaggio tipo="errore">{errore}</Messaggio>
        <button className="btn-primary w-full" disabled={invio || !form.privacyAccettata}>
          {invio ? 'Registrazione…' : 'Registrati'}
        </button>
      </form>
    </motion.div>
  )
}
