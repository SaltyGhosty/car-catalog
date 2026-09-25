import { useEffect, useRef, useState } from 'react'
import { useSearchParams } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import AutoCard, { SagomaAuto } from '../components/AutoCard.jsx'
import Messaggio from '../components/Messaggio.jsx'
import { usePreferiti } from '../components/usePreferiti.js'

// Stesse chiavi della whitelist del backend (AutoService.ORDINAMENTI_AMMESSI)
const ORDINAMENTI = [
  { value: 'recenti:desc', label: 'Più recenti' },
  { value: 'prezzo:asc', label: 'Prezzo più basso' },
  { value: 'prezzo:desc', label: 'Prezzo più alto' },
  { value: 'anno:desc', label: 'Anno più recente' },
  { value: 'chilometri:asc', label: 'Meno chilometri' },
  { value: 'marca:asc', label: 'Marca A-Z' },
]
const FASCE = [
  { value: '', label: 'Tutte' },
  { value: '15000', label: 'Fino a 15.000 €' },
  { value: '25000', label: 'Fino a 25.000 €' },
]
const MARCHE = ['Fiat', 'Volkswagen', 'Toyota', 'BMW', 'Renault', 'Peugeot']

export default function Catalogo() {
  const { utente } = useAuth()
  const { preferiti, toggle } = usePreferiti()
  const [params, setParams] = useSearchParams()
  const [testo, setTesto] = useState(params.get('q') ?? '')
  const [dati, setDati] = useState(null)
  const [errore, setErrore] = useState('')
  const risultati = useRef(null)

  const q = params.get('q') ?? ''
  const prezzoMax = params.get('prezzoMax') ?? ''
  const ordinamento = params.get('ordina') ?? 'recenti:desc'
  const pagina = Number(params.get('pagina') ?? 0)

  const aggiorna = (patch) => {
    const n = new URLSearchParams(params)
    Object.entries(patch).forEach(([k, v]) => (v === '' || v == null ? n.delete(k) : n.set(k, v)))
    if (!('pagina' in patch)) n.delete('pagina')
    setParams(n, { replace: true })
  }

  // Ricerca con debounce di 300 ms
  useEffect(() => {
    const t = setTimeout(() => { if (testo !== q) aggiorna({ q: testo }) }, 300)
    return () => clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [testo])

  useEffect(() => {
    const ctrl = new AbortController()
    const [ordina, direzione] = ordinamento.split(':')
    api.auto
      .cerca({ q, prezzoMax, ordina, direzione, pagina, dimensione: 9 }, ctrl.signal)
      .then((d) => { setDati(d); setErrore('') })
      .catch((e) => { if (e.name !== 'AbortError') setErrore(e.message) })
    return () => ctrl.abort()
  }, [q, prezzoMax, ordinamento, pagina])

  async function onToggle(autoId) {
    try { await toggle(autoId) } catch (e) { setErrore(e.message) }
  }

  const totale = dati?.totaleElementi ?? 0

  return (
    <div className="space-y-10">
      {/* ---------- Hero con box di ricerca ---------- */}
      <section className="relative overflow-hidden rounded-3xl bg-[#26282b] text-white">
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_20%_110%,#4a4f55_0%,transparent_60%)]" />
        <SagomaAuto className="pointer-events-none absolute -bottom-6 -left-10 w-[620px] max-w-none opacity-[0.12] invert" />
        <div className="relative grid gap-8 p-6 sm:p-10 lg:grid-cols-[1fr_400px] lg:items-center">
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-3">
            <p className="text-xs font-bold tracking-[0.18em] text-white/60 uppercase">Auto usate selezionate</p>
            <h1 className="text-4xl leading-tight font-extrabold sm:text-5xl">Qual è la tua prossima auto?</h1>
            <p className="max-w-md text-white/70">
              {utente
                ? 'Salva le auto che ti piacciono e ricevi un’email quando il prezzo scende sotto la tua soglia.'
                : 'Sfoglia il catalogo senza registrarti. Con un account salvi i preferiti e imposti avvisi di prezzo.'}
            </p>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.08 }}
                      className="card space-y-4 p-5 text-ink">
            <div className="flex gap-1 rounded-full bg-ground p-1" role="group" aria-label="Fascia di prezzo">
              {FASCE.map((f) => (
                <button key={f.label} onClick={() => aggiorna({ prezzoMax: f.value })}
                        aria-pressed={prezzoMax === f.value}
                        className={`flex-1 rounded-full px-2 py-2 text-xs font-bold transition ${
                          prezzoMax === f.value ? 'bg-white shadow-sm' : 'text-ink-soft hover:text-ink'}`}>
                  {f.label}
                </button>
              ))}
            </div>
            <div>
              <label htmlFor="q" className="label">Marca o modello</label>
              <input id="q" type="search" className="input" placeholder="Es. Golf, Yaris, BMW…" maxLength={60}
                     value={testo} onChange={(e) => setTesto(e.target.value)} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="max" className="label">Prezzo massimo</label>
                <input id="max" type="number" min="0" step="any" className="input" placeholder="€"
                       value={prezzoMax} onChange={(e) => aggiorna({ prezzoMax: e.target.value })} />
              </div>
              <div>
                <label htmlFor="ord" className="label">Ordina per</label>
                <select id="ord" className="input" value={ordinamento} onChange={(e) => aggiorna({ ordina: e.target.value })}>
                  {ORDINAMENTI.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
              </div>
            </div>
            <button className="btn-primary w-full py-3 text-base"
                    onClick={() => risultati.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })}>
              Mostra {totale.toLocaleString('it-IT')} {totale === 1 ? 'auto' : 'auto'}
            </button>
          </motion.div>
        </div>
      </section>

      {/* ---------- Marche ---------- */}
      <section className="space-y-3">
        <h2 className="text-lg font-bold">Cerca per marca</h2>
        <div className="flex flex-wrap gap-2">
          {MARCHE.map((m) => {
            const attiva = q.toLowerCase() === m.toLowerCase()
            return (
              <button key={m} className={attiva ? 'chip chip-active' : 'chip'}
                      onClick={() => { const v = attiva ? '' : m; setTesto(v); aggiorna({ q: v }) }}>
                {m}
              </button>
            )
          })}
        </div>
      </section>

      {/* ---------- Risultati ---------- */}
      <section ref={risultati} className="scroll-mt-24 space-y-5">
        <div className="flex flex-wrap items-end justify-between gap-2">
          <h2 className="text-2xl font-extrabold">
            {totale.toLocaleString('it-IT')} auto in vendita
            {q && <span className="font-semibold text-ink-soft"> per “{q}”</span>}
          </h2>
          {(q || prezzoMax) && (
            <button className="text-sm font-semibold text-brand-600 hover:underline"
                    onClick={() => { setTesto(''); setParams({}, { replace: true }) }}>
              Rimuovi filtri
            </button>
          )}
        </div>

        <Messaggio tipo="errore">{errore}</Messaggio>

        {dati && dati.contenuto.length === 0 && (
          <div className="card p-10 text-center text-ink-soft">Nessuna auto corrisponde alla ricerca.</div>
        )}

        <motion.div layout className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          <AnimatePresence mode="popLayout">
            {dati?.contenuto.map((auto) => (
              <AutoCard key={auto.id} auto={auto} loggato={Boolean(utente)}
                        preferito={preferiti.has(auto.id)} onTogglePreferito={onToggle} />
            ))}
          </AnimatePresence>
        </motion.div>

        {dati && dati.totalePagine > 1 && (
          <nav className="flex items-center justify-center gap-3" aria-label="Paginazione">
            <button className="btn-ghost" disabled={pagina === 0} onClick={() => aggiorna({ pagina: pagina - 1 })}>
              ← Precedente
            </button>
            <span className="text-sm text-ink-soft tabular-nums">Pagina {pagina + 1} di {dati.totalePagine}</span>
            <button className="btn-ghost" disabled={pagina + 1 >= dati.totalePagine}
                    onClick={() => aggiorna({ pagina: pagina + 1 })}>
              Successiva →
            </button>
          </nav>
        )}
      </section>
    </div>
  )
}
