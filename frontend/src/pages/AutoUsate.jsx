import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import { api, formatEuro } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import AutoCard from '../components/AutoCard.jsx'
import AutoRiga from '../components/AutoRiga.jsx'
import Messaggio from '../components/Messaggio.jsx'
import { usePreferiti } from '../components/usePreferiti.js'
import { useFiltri } from '../components/useFiltri.js'

// Stesse chiavi della whitelist del backend (AutoService.ORDINAMENTI_AMMESSI)
const ORDINAMENTI = [
  { value: 'recenti:desc', label: 'Più recenti' },
  { value: 'prezzo:asc', label: 'Prezzo più basso' },
  { value: 'prezzo:desc', label: 'Prezzo più alto' },
  { value: 'anno:desc', label: 'Anno più recente' },
  { value: 'chilometri:asc', label: 'Meno chilometri' },
  { value: 'marca:asc', label: 'Marca A-Z' },
]
const PREZZI = [5000, 10000, 15000, 20000, 25000, 30000, 40000, 50000]
const KM = [10000, 20000, 50000, 75000, 100000, 150000]
const CAMPI = ['q', 'marca', 'prezzoMin', 'prezzoMax', 'annoMin', 'kmMax']
const PER_PAGINA = 8

const eur = (v) => formatEuro(v)
const km = (v) => `${new Intl.NumberFormat('it-IT').format(v)} km`

export default function AutoUsate() {
  const { utente } = useAuth()
  const { preferiti, toggle } = usePreferiti()
  const filtri = useFiltri()
  const [params, setParams] = useSearchParams()
  const [testo, setTesto] = useState(params.get('q') ?? '')
  const [dati, setDati] = useState(null)
  const [caricataPer, setCaricataPer] = useState(null)
  const [errore, setErrore] = useState('')
  const [pannello, setPannello] = useState(false) // filtri su mobile
  const [vista, setVista] = useState('lista')

  const valori = Object.fromEntries(CAMPI.map((k) => [k, params.get(k) ?? '']))
  const ordinamento = params.get('ordina') ?? 'recenti:desc'
  const pagina = Number(params.get('pagina') ?? 0)
  const chiave = params.toString()
  const caricamento = caricataPer !== chiave

  const aggiorna = (patch) => {
    const n = new URLSearchParams(params)
    Object.entries(patch).forEach(([k, v]) => (v === '' || v == null ? n.delete(k) : n.set(k, v)))
    if (!('pagina' in patch)) n.delete('pagina')
    setParams(n, { replace: true })
  }

  // Testo libero con debounce
  useEffect(() => {
    const t = setTimeout(() => { if (testo !== valori.q) aggiorna({ q: testo }) }, 300)
    return () => clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [testo])

  useEffect(() => {
    const ctrl = new AbortController()
    const p = new URLSearchParams(chiave)
    const [ordina, direzione] = (p.get('ordina') ?? 'recenti:desc').split(':')
    const richiesta = Object.fromEntries(CAMPI.map((k) => [k, p.get(k) ?? '']))
    api.auto
      .cerca({ ...richiesta, ordina, direzione, pagina: p.get('pagina') ?? 0, dimensione: PER_PAGINA }, ctrl.signal)
      .then((d) => { setDati(d); setErrore('') })
      .catch((e) => { if (e.name !== 'AbortError') setErrore(e.message) })
      .finally(() => { if (!ctrl.signal.aborted) setCaricataPer(chiave) })
    return () => ctrl.abort()
  }, [chiave])

  const anni = useMemo(() => {
    if (!filtri?.annoMin) return []
    return Array.from({ length: filtri.annoMax - filtri.annoMin + 1 }, (_, i) => filtri.annoMax - i)
  }, [filtri])

  const attivi = [
    valori.q && { k: 'q', label: `“${valori.q}”` },
    valori.marca && { k: 'marca', label: valori.marca },
    valori.prezzoMin && { k: 'prezzoMin', label: `Da ${eur(valori.prezzoMin)}` },
    valori.prezzoMax && { k: 'prezzoMax', label: `Fino a ${eur(valori.prezzoMax)}` },
    valori.annoMin && { k: 'annoMin', label: `Dal ${valori.annoMin}` },
    valori.kmMax && { k: 'kmMax', label: `Max ${km(valori.kmMax)}` },
  ].filter(Boolean)

  const azzera = () => { setTesto(''); setParams(ordinamento !== 'recenti:desc' ? { ordina: ordinamento } : {}, { replace: true }) }
  const onToggle = (id) => toggle(id).catch((e) => setErrore(e.message))
  const totale = dati?.totaleElementi ?? 0

  return (
    <div className="grid gap-6 lg:grid-cols-[280px_1fr] lg:items-start">
      {/* ---------- Filtri ---------- */}
      <aside className={`${pannello ? 'block' : 'hidden'} card space-y-5 p-5 lg:sticky lg:top-24 lg:block`} aria-label="Filtri">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-extrabold">Filtri</h2>
          {attivi.length > 0 && (
            <button onClick={azzera} className="text-sm font-semibold text-brand-600 hover:underline">Azzera</button>
          )}
        </div>

        <div>
          <label htmlFor="f-q" className="label">Cerca</label>
          <input id="f-q" type="search" className="input" placeholder="Modello, allestimento…" maxLength={60}
                 value={testo} onChange={(e) => setTesto(e.target.value)} />
        </div>

        <div>
          <label htmlFor="f-marca" className="label">Marca</label>
          <select id="f-marca" className="input" value={valori.marca} onChange={(e) => aggiorna({ marca: e.target.value })}>
            <option value="">Tutte</option>
            {filtri?.marche.map((m) => <option key={m.marca} value={m.marca}>{m.marca} ({m.conteggio})</option>)}
          </select>
        </div>

        <fieldset>
          <legend className="label">Prezzo</legend>
          <div className="grid grid-cols-2 gap-2">
            <select aria-label="Prezzo minimo" className="input" value={valori.prezzoMin}
                    onChange={(e) => aggiorna({ prezzoMin: e.target.value })}>
              <option value="">Da</option>
              {PREZZI.map((p) => <option key={p} value={p}>{eur(p)}</option>)}
            </select>
            <select aria-label="Prezzo massimo" className="input" value={valori.prezzoMax}
                    onChange={(e) => aggiorna({ prezzoMax: e.target.value })}>
              <option value="">A</option>
              {PREZZI.map((p) => <option key={p} value={p}>{eur(p)}</option>)}
            </select>
          </div>
        </fieldset>

        <div>
          <label htmlFor="f-anno" className="label">Anno (da)</label>
          <select id="f-anno" className="input" value={valori.annoMin} onChange={(e) => aggiorna({ annoMin: e.target.value })}>
            <option value="">Qualsiasi</option>
            {anni.map((a) => <option key={a} value={a}>{a}</option>)}
          </select>
        </div>

        <fieldset>
          <legend className="label">Chilometri (massimo)</legend>
          <div className="flex flex-wrap gap-1.5">
            {KM.map((k) => {
              const on = valori.kmMax === String(k)
              return (
                <button key={k} type="button" aria-pressed={on} onClick={() => aggiorna({ kmMax: on ? '' : k })}
                        className={`rounded-full border px-3 py-1.5 text-xs font-semibold transition ${
                          on ? 'border-ink bg-ink text-white' : 'border-line bg-white hover:border-ink'}`}>
                  {new Intl.NumberFormat('it-IT', { notation: 'compact' }).format(k)} km
                </button>
              )
            })}
          </div>
        </fieldset>

        <button className="btn-primary w-full lg:hidden" onClick={() => setPannello(false)}>
          Mostra {totale} risultati
        </button>
      </aside>

      {/* ---------- Risultati ---------- */}
      <section className="min-w-0 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-extrabold">
            <span className="tabular-nums">{totale}</span> auto usate
            {valori.marca && <span className="text-ink-soft"> {valori.marca}</span>}
          </h1>
          <div className="flex items-center gap-2">
            <button className="btn-ghost px-4 py-2 lg:hidden" onClick={() => setPannello(!pannello)} aria-expanded={pannello}>
              Filtri{attivi.length ? ` (${attivi.length})` : ''}
            </button>
            <label htmlFor="ord" className="sr-only">Ordina per</label>
            <select id="ord" className="input w-auto py-2" value={ordinamento} onChange={(e) => aggiorna({ ordina: e.target.value })}>
              {ORDINAMENTI.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
            <div className="hidden rounded-full bg-white p-1 shadow-sm sm:flex" role="group" aria-label="Vista">
              {['lista', 'griglia'].map((v) => (
                <button key={v} onClick={() => setVista(v)} aria-pressed={vista === v}
                        className={`rounded-full px-3 py-1.5 text-xs font-bold capitalize ${vista === v ? 'bg-ink text-white' : 'text-ink-soft'}`}>
                  {v}
                </button>
              ))}
            </div>
          </div>
        </div>

        {attivi.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {attivi.map((f) => (
              <button key={f.k} onClick={() => { if (f.k === 'q') setTesto(''); aggiorna({ [f.k]: '' }) }}
                      className="inline-flex items-center gap-1.5 rounded-full bg-ink px-3 py-1.5 text-xs font-semibold text-white hover:bg-black"
                      aria-label={`Rimuovi filtro ${f.label}`}>
                {f.label} <span aria-hidden="true">×</span>
              </button>
            ))}
          </div>
        )}

        <Messaggio tipo="errore">{errore}</Messaggio>

        {!caricamento && dati?.contenuto.length === 0 && (
          <div className="card space-y-3 p-10 text-center">
            <p className="font-bold">Nessuna auto con questi filtri</p>
            <p className="text-sm text-ink-soft">Prova ad allargare il prezzo o a togliere qualche filtro.</p>
            <button className="btn-dark" onClick={azzera}>Azzera i filtri</button>
          </div>
        )}

        <motion.div layout className={`${vista === 'griglia' ? 'grid gap-5 sm:grid-cols-2 xl:grid-cols-3' : 'space-y-4'} ${
          caricamento ? 'opacity-60' : ''} transition-opacity`}>
          <AnimatePresence mode="popLayout">
            {dati?.contenuto.map((auto) =>
              vista === 'griglia' ? (
                <AutoCard key={auto.id} auto={auto} loggato={Boolean(utente)} preferito={preferiti.has(auto.id)} onTogglePreferito={onToggle} />
              ) : (
                <AutoRiga key={auto.id} auto={auto} loggato={Boolean(utente)} preferito={preferiti.has(auto.id)} onTogglePreferito={onToggle} />
              ),
            )}
          </AnimatePresence>
        </motion.div>

        {dati && dati.totalePagine > 1 && (
          <nav className="flex flex-wrap items-center justify-center gap-2 pt-2" aria-label="Paginazione">
            <button className="btn-ghost px-4 py-2" disabled={pagina === 0}
                    onClick={() => { aggiorna({ pagina: pagina - 1 }); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>
              ←
            </button>
            {Array.from({ length: dati.totalePagine }, (_, i) => (
              <button key={i} aria-current={i === pagina ? 'page' : undefined}
                      onClick={() => { aggiorna({ pagina: i }); window.scrollTo({ top: 0, behavior: 'smooth' }) }}
                      className={`grid size-10 place-items-center rounded-full text-sm font-bold tabular-nums ${
                        i === pagina ? 'bg-ink text-white' : 'bg-white hover:bg-line'}`}>
                {i + 1}
              </button>
            ))}
            <button className="btn-ghost px-4 py-2" disabled={pagina + 1 >= dati.totalePagine}
                    onClick={() => { aggiorna({ pagina: pagina + 1 }); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>
              →
            </button>
          </nav>
        )}
      </section>
    </div>
  )
}
