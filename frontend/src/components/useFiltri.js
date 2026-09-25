import { useEffect, useState } from 'react'
import { api } from '../api.js'

let cache = null

/** Marche con conteggio e intervalli prezzo/anno (GET /api/auto/filtri), caricati una volta. */
export function useFiltri() {
  const [filtri, setFiltri] = useState(cache)
  useEffect(() => {
    if (cache) return
    api.auto.filtri().then((f) => { cache = f; setFiltri(f) }).catch(() => {})
  }, [])
  return filtri
}
