import { useState } from 'react'
import { Link } from 'react-router'
import { motion } from 'motion/react'
import { api } from '../api.js'
import Messaggio from '../components/Messaggio.jsx'

export default function PasswordDimenticata() {
  const [email, setEmail] = useState('')
  const [inviata, setInviata] = useState(false)
  const [errore, setErrore] = useState('')
  const [invio, setInvio] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setErrore(''); setInvio(true)
    try {
      await api.auth.passwordDimenticata(email)
      setInviata(true)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInvio(false)
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="card mx-auto max-w-md space-y-5 p-8">
      <div>
        <h1 className="text-2xl font-bold">Password dimenticata</h1>
        <p className="mt-1 text-sm text-ink-soft">
          Inserisci l’email del tuo account: ti invieremo un link per sceglierne una nuova.
        </p>
      </div>
      {inviata ? (
        // Stesso messaggio che l'account esista o no: non riveliamo quali email sono registrate
        <Messaggio tipo="ok">
          Se l’indirizzo è registrato, riceverai a breve un’email con il link. Il link vale 30 minuti e si usa una sola volta.
        </Messaggio>
      ) : (
        <form onSubmit={submit} className="space-y-4">
          <div>
            <label htmlFor="email" className="label">Email</label>
            <input id="email" type="email" autoComplete="email" required maxLength={254} className="input"
                   value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <Messaggio tipo="errore">{errore}</Messaggio>
          <button className="btn-primary w-full" disabled={invio}>{invio ? 'Invio…' : 'Inviami il link'}</button>
        </form>
      )}
      <Link to="/login" className="block text-sm font-semibold text-brand-600 hover:underline">← Torna all’accesso</Link>
    </motion.div>
  )
}
