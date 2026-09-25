import { useState } from 'react'
import { useNavigate } from 'react-router'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Messaggio from '../components/Messaggio.jsx'

export default function Profilo() {
  const { utente, setUtente, logout } = useAuth()
  const navigate = useNavigate()
  const [nome, setNome] = useState(utente.nome)
  const [msg, setMsg] = useState({ tipo: 'ok', testo: '' })
  const [conferma, setConferma] = useState(false)
  const [password, setPassword] = useState('')

  async function salvaNome(e) {
    e.preventDefault()
    try {
      setUtente(await api.me.aggiorna(nome))
      setMsg({ tipo: 'ok', testo: 'Profilo aggiornato' })
    } catch (err) {
      setMsg({ tipo: 'errore', testo: Object.values(err.campi ?? {})[0] ?? err.message })
    }
  }

  async function eliminaAccount(e) {
    e.preventDefault()
    try {
      await api.me.elimina(password)
      logout()
      navigate('/', { replace: true })
    } catch (err) {
      setMsg({ tipo: 'errore', testo: err.message })
    }
  }

  return (
    <section className="mx-auto max-w-xl space-y-6">
      <h1 className="text-2xl font-bold">Il tuo profilo</h1>
      <Messaggio tipo={msg.tipo}>{msg.testo}</Messaggio>

      <form onSubmit={salvaNome} className="card space-y-4 p-6">
        <div>
          <p className="label">Email</p>
          <p className="text-sm text-stone-700">{utente.email}</p>
        </div>
        <div>
          <label htmlFor="nome" className="label">Nome</label>
          <input id="nome" className="input" maxLength={60} value={nome} onChange={(e) => setNome(e.target.value)} />
        </div>
        <button className="btn-primary">Salva</button>
      </form>

      <div className="card space-y-3 border-red-200 p-6">
        <h2 className="font-semibold text-red-700">Elimina account</h2>
        <p className="text-sm text-stone-600">
          Cancella definitivamente account, preferiti e avvisi di prezzo. L’operazione non è reversibile.
        </p>
        {!conferma ? (
          <button className="btn-danger" onClick={() => setConferma(true)}>Elimina il mio account</button>
        ) : (
          <form onSubmit={eliminaAccount} className="space-y-3">
            <label htmlFor="pwdel" className="label">Conferma con la tua password</label>
            <input id="pwdel" type="password" autoComplete="current-password" className="input" required
                   value={password} onChange={(e) => setPassword(e.target.value)} />
            <div className="flex gap-2">
              <button className="btn-danger">Elimina definitivamente</button>
              <button type="button" className="btn-ghost" onClick={() => setConferma(false)}>Annulla</button>
            </div>
          </form>
        )}
      </div>
    </section>
  )
}
