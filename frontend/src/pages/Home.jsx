import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { motion } from 'motion/react'
import { api, formatEuro } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import AutoCard from '../components/AutoCard.jsx'
import Messaggio from '../components/Messaggio.jsx'
import { usePreferiti } from '../components/usePreferiti.js'
import { useFiltri } from '../components/useFiltri.js'
import heroImg from '../assets/hero.webp'

const FASCE = [
  { value: '', label: 'Tutte' },
  { value: '15000', label: 'Fino a 15.000 €' },
  { value: '25000', label: 'Fino a 25.000 €' },
]

export default function Home() {
  const { utente } = useAuth()
  const navigate = useNavigate()
  const filtri = useFiltri()
  const { preferiti, toggle } = usePreferiti()
  const [ricerca, setRicerca] = useState({ q: '', marca: '', prezzoMax: '' })
  const [ultime, setUltime] = useState(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    api.auto.cerca({ ordina: 'recenti', dimensione: 6 }).then((d) => setUltime(d.contenuto)).catch((e) => setErrore(e.message))
  }, [])

  const vaiAiRisultati = (e) => {
    e?.preventDefault()
    const qs = new URLSearchParams(Object.entries(ricerca).filter(([, v]) => v))
    navigate(`/auto-usate${qs.size ? `?${qs}` : ''}`)
  }

  return (
    <div className="space-y-12">
      {/* ---------- Hero con box di ricerca ---------- */}
      <section className="relative overflow-hidden rounded-3xl bg-[#26282b] text-white">
        <img src={heroImg} alt="" aria-hidden="true"
             className="absolute inset-0 size-full object-cover opacity-45" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#1d1d1b] via-[#1d1d1b]/75 to-transparent" />
        <div className="relative grid gap-8 p-6 sm:p-10 lg:grid-cols-[1fr_400px] lg:items-center">
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-3">
            <p className="text-xs font-bold tracking-[0.18em] text-white/70 uppercase">Auto usate selezionate</p>
            <h1 className="text-4xl leading-tight font-extrabold sm:text-5xl">Qual è la tua prossima auto?</h1>
            <p className="max-w-md text-white/80">
              {utente
                ? 'Salva le auto che ti piacciono e ricevi un’email quando il prezzo scende sotto la tua soglia.'
                : 'Sfoglia il catalogo senza registrarti. Con un account salvi i preferiti e imposti avvisi di prezzo.'}
            </p>
          </motion.div>

          <motion.form onSubmit={vaiAiRisultati} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
                       transition={{ delay: 0.08 }} className="card space-y-4 p-5 text-ink">
            <div className="flex gap-1 rounded-full bg-ground p-1" role="group" aria-label="Fascia di prezzo">
              {FASCE.map((f) => (
                <button type="button" key={f.label} onClick={() => setRicerca({ ...ricerca, prezzoMax: f.value })}
                        aria-pressed={ricerca.prezzoMax === f.value}
                        className={`flex-1 rounded-full px-2 py-2 text-xs font-bold transition ${
                          ricerca.prezzoMax === f.value ? 'bg-white shadow-sm' : 'text-ink-soft hover:text-ink'}`}>
                  {f.label}
                </button>
              ))}
            </div>
            <div>
              <label htmlFor="h-marca" className="label">Marca</label>
              <select id="h-marca" className="input" value={ricerca.marca}
                      onChange={(e) => setRicerca({ ...ricerca, marca: e.target.value })}>
                <option value="">Tutte le marche</option>
                {filtri?.marche.map((m) => <option key={m.marca} value={m.marca}>{m.marca} ({m.conteggio})</option>)}
              </select>
            </div>
            <div>
              <label htmlFor="h-q" className="label">Modello o parola chiave</label>
              <input id="h-q" type="search" className="input" placeholder="Es. Golf, ibrida, Yaris…" maxLength={60}
                     value={ricerca.q} onChange={(e) => setRicerca({ ...ricerca, q: e.target.value })} />
            </div>
            <button className="btn-primary w-full py-3 text-base">
              Cerca {filtri ? `tra ${filtri.totale} auto` : ''}
            </button>
          </motion.form>
        </div>
      </section>

      {/* ---------- Marche ---------- */}
      <section className="space-y-4">
        <div className="flex items-end justify-between">
          <h2 className="text-xl font-extrabold">Cerca per marca</h2>
          <Link to="/auto-usate" className="text-sm font-semibold text-brand-600 hover:underline">Tutte le auto →</Link>
        </div>
        <div className="flex flex-wrap gap-2">
          {filtri?.marche.map((m) => (
            <Link key={m.marca} to={`/auto-usate?marca=${encodeURIComponent(m.marca)}`} className="chip gap-2">
              {m.marca} <span className="text-xs font-semibold text-ink-soft">{m.conteggio}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* ---------- Scorciatoie ---------- */}
      <section className="grid gap-4 sm:grid-cols-3">
        {[
          { to: '/auto-usate?prezzoMax=15000&ordina=prezzo:asc', titolo: 'Sotto i 15.000 €', testo: 'Le più economiche per iniziare' },
          { to: '/auto-usate?kmMax=20000', titolo: 'Meno di 20.000 km', testo: 'Quasi nuove, garanzia residua' },
          { to: '/auto-usate?q=hybrid', titolo: 'Ibride', testo: 'Consumi bassi in città' },
        ].map((s) => (
          <Link key={s.to} to={s.to} className="card group flex items-center justify-between p-5 transition hover:shadow-md">
            <div>
              <p className="font-bold">{s.titolo}</p>
              <p className="text-sm text-ink-soft">{s.testo}</p>
            </div>
            <span className="text-xl text-brand-600 transition group-hover:translate-x-1" aria-hidden="true">→</span>
          </Link>
        ))}
      </section>

      {/* ---------- Ultimi arrivi ---------- */}
      <section className="space-y-5">
        <div className="flex items-end justify-between">
          <h2 className="text-xl font-extrabold">Ultimi arrivi</h2>
          {filtri && (
            <p className="text-sm text-ink-soft tabular-nums">
              Prezzi da {formatEuro(filtri.prezzoMin)} a {formatEuro(filtri.prezzoMax)}
            </p>
          )}
        </div>
        <Messaggio tipo="errore">{errore}</Messaggio>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {ultime?.map((auto) => (
            <AutoCard key={auto.id} auto={auto} loggato={Boolean(utente)} preferito={preferiti.has(auto.id)}
                      onTogglePreferito={(id) => toggle(id).catch((e) => setErrore(e.message))} />
          ))}
        </div>
        <div className="text-center">
          <Link to="/auto-usate" className="btn-dark px-8 py-3">Vedi tutte le {filtri?.totale ?? ''} auto usate</Link>
        </div>
      </section>
    </div>
  )
}
