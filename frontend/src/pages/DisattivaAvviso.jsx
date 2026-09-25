import { useState } from 'react'
import { Link } from 'react-router'
import { api } from '../api.js'
import Messaggio from '../components/Messaggio.jsx'

/**
 * Link dalla mail: /avvisi/disattiva#token=...
 * Il token sta nel fragment (#), quindi non arriva a nessun server nei log.
 * La disattivazione richiede un click (POST): gli scanner antispam che "aprono" i link non la consumano.
 */
function leggiToken() {
  const token = new URLSearchParams(window.location.hash.slice(1)).get('token')
  if (token) window.history.replaceState(null, '', window.location.pathname) // toglie il token dalla barra
  return token
}

export default function DisattivaAvviso() {
  const [token] = useState(leggiToken)
  const [stato, setStato] = useState(token ? 'pronto' : 'mancante')
  const [errore, setErrore] = useState('')

  async function conferma() {
    setStato('invio')
    try {
      await api.avvisi.disattiva(token)
      setStato('fatto')
    } catch (e) {
      setErrore(e.message)
      setStato('errore')
    }
  }

  return (
    <div className="card mx-auto max-w-md space-y-4 p-8 text-center">
      <h1 className="text-2xl font-bold">Disattiva avviso</h1>
      {stato === 'mancante' && <Messaggio tipo="errore">Link non valido.</Messaggio>}
      {(stato === 'pronto' || stato === 'invio') && (
        <>
          <p className="text-stone-600">Vuoi smettere di ricevere notifiche per questo avviso di prezzo?</p>
          <button className="btn-primary" onClick={conferma} disabled={stato === 'invio'}>Conferma disattivazione</button>
        </>
      )}
      {stato === 'fatto' && <Messaggio tipo="ok">Avviso disattivato ed eliminato.</Messaggio>}
      {stato === 'errore' && <Messaggio tipo="errore">{errore}</Messaggio>}
      <Link to="/" className="block text-sm text-brand-700 hover:underline">Torna al catalogo</Link>
    </div>
  )
}
