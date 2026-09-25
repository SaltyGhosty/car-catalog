import { useState } from 'react'
import { api, ApiError, formatEuro } from '../api.js'
import Messaggio from './Messaggio.jsx'

/**
 * Crea o aggiorna l'avviso di prezzo per un'auto.
 * Il body contiene SOLO autoId e soglia: utente e "inviato" li decide il server.
 */
export default function SogliaForm({ auto, avviso, onSalvato, onEliminato }) {
  const [soglia, setSoglia] = useState(avviso ? String(avviso.soglia) : String(Math.floor(auto.prezzo * 0.9)))
  const [errore, setErrore] = useState('')
  const [ok, setOk] = useState('')
  const [invio, setInvio] = useState(false)

  const valore = Number(soglia)
  const valida = Number.isFinite(valore) && valore >= 1 && valore < Number(auto.prezzo)

  async function salva(e) {
    e.preventDefault()
    setErrore(''); setOk('')
    if (!valida) { setErrore(`Inserisci una soglia inferiore a ${formatEuro(auto.prezzo)}`); return }
    setInvio(true)
    try {
      const salvato = avviso
        ? await api.avvisi.aggiornaSoglia(avviso.id, valore)
        : await api.avvisi.crea(auto.id, valore)
      setOk(`Ti avviseremo via email quando il prezzo scenderà a ${formatEuro(salvato.soglia)} o meno.`)
      onSalvato?.(salvato)
    } catch (err) {
      setErrore(err instanceof ApiError ? (Object.values(err.campi)[0] ?? err.message) : 'Errore imprevisto')
    } finally {
      setInvio(false)
    }
  }

  async function elimina() {
    setInvio(true)
    try {
      await api.avvisi.elimina(avviso.id)
      onEliminato?.(avviso.id)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInvio(false)
    }
  }

  return (
    <form onSubmit={salva} className="space-y-3">
      <label htmlFor="soglia" className="label">Avvisami quando il prezzo scende sotto</label>
      <div className="flex gap-2">
        <div className="relative flex-1">
          <span className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-stone-400">€</span>
          <input id="soglia" type="number" inputMode="numeric" min="1" step="any"
                 max={Number(auto.prezzo) - 1} value={soglia}
                 onChange={(e) => setSoglia(e.target.value)} className="input pl-7" required />
        </div>
        <button className="btn-primary" disabled={invio}>{avviso ? 'Aggiorna' : 'Crea avviso'}</button>
      </div>
      {avviso?.inviato && (
        <p className="text-xs text-stone-500">
          Notifica già inviata. Cambiando la soglia l’avviso si riattiva.
        </p>
      )}
      <Messaggio tipo="errore">{errore}</Messaggio>
      <Messaggio tipo="ok">{ok}</Messaggio>
      {avviso && (
        <button type="button" onClick={elimina} disabled={invio} className="text-sm text-red-600 hover:underline">
          Elimina avviso
        </button>
      )}
    </form>
  )
}
