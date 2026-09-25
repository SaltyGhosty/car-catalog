import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'

/** Mappa autoId → preferitoId dell'utente loggato, con toggle ottimistico. */
export function usePreferiti() {
  const { utente } = useAuth()
  const [mappa, setMappa] = useState(() => new Map())

  useEffect(() => {
    if (!utente) return
    let attivo = true
    api.preferiti.lista()
      .then((lista) => attivo && setMappa(new Map(lista.map((p) => [p.auto.id, p.id]))))
      .catch(() => {})
    return () => { attivo = false }
  }, [utente])

  const toggle = useCallback(async (autoId) => {
    const preferitoId = mappa.get(autoId)
    if (preferitoId) {
      await api.preferiti.rimuovi(preferitoId)
      setMappa((m) => { const n = new Map(m); n.delete(autoId); return n })
    } else {
      const p = await api.preferiti.aggiungi(autoId)
      setMappa((m) => new Map(m).set(autoId, p.id))
    }
  }, [mappa])

  return { preferiti: utente ? mappa : new Map(), toggle }
}
