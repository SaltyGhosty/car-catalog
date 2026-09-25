import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import { api, formatEuro } from '../api.js'
import FotoAuto, { CreditoFoto } from '../components/FotoAuto.jsx'
import Messaggio from '../components/Messaggio.jsx'

const VUOTA = {
  marca: '', modello: '', anno: 2024, chilometri: 0, descrizione: '', prezzo: '', prezzoAcquisto: '', stato: 'BOZZA',
  immagineUrl: '', immagineCredito: '', immagineFonte: '',
}
const nulla = (v) => (v && v.trim() ? v.trim() : null)

function AutoForm({ iniziale, onSalvata, onAnnulla }) {
  const [form, setForm] = useState(() => ({ ...VUOTA, ...Object.fromEntries(Object.entries(iniziale ?? {}).map(([k, v]) => [k, v ?? ''])) }))
  const [cercaFoto, setCercaFoto] = useState('')
  const [errori, setErrori] = useState({})
  const [errore, setErrore] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function submit(e) {
    e.preventDefault()
    setErrori({}); setErrore('')
    // Mandiamo SOLO i campi del DTO AutoRequest
    const body = {
      marca: form.marca, modello: form.modello, anno: Number(form.anno), chilometri: Number(form.chilometri),
      descrizione: form.descrizione, prezzo: Number(form.prezzo), prezzoAcquisto: Number(form.prezzoAcquisto),
      stato: form.stato,
      immagineUrl: nulla(form.immagineUrl), immagineCredito: nulla(form.immagineCredito), immagineFonte: nulla(form.immagineFonte),
    }
    try {
      const salvata = iniziale ? await api.admin.aggiorna(iniziale.id, body) : await api.admin.crea(body)
      onSalvata(salvata)
    } catch (err) {
      setErrore(err.message); setErrori(err.campi ?? {})
    }
  }

  // "API di auto": il backend cerca il modello su Wikipedia e restituisce foto + autore + licenza
  async function suggerisciFoto() {
    setCercaFoto('Cerco su Wikimedia Commons…')
    try {
      const f = await api.admin.suggerisciImmagine(form.marca, form.modello)
      setForm((x) => ({ ...x, immagineUrl: f.url, immagineCredito: f.credito, immagineFonte: f.fonte }))
      setCercaFoto(`Trovata nell’articolo “${f.articolo}”`)
    } catch (err) {
      setCercaFoto(err.message)
    }
  }

  const campo = (k, label, props = {}) => (
    <div>
      <label htmlFor={`f-${k}`} className="label">{label}</label>
      <input id={`f-${k}`} className="input" value={form[k]} onChange={set(k)} required {...props} />
      {errori[k] && <p className="mt-1 text-xs text-red-600">{errori[k]}</p>}
    </div>
  )

  return (
    <motion.form initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
                 onSubmit={submit} className="card grid gap-4 overflow-hidden p-6 sm:grid-cols-2">
      <h2 className="text-lg font-semibold sm:col-span-2">{iniziale ? `Modifica #${iniziale.id}` : 'Nuova auto'}</h2>
      {campo('marca', 'Marca', { maxLength: 50 })}
      {campo('modello', 'Modello', { maxLength: 80 })}
      {campo('anno', 'Anno', { type: 'number', min: 1950, max: 2100 })}
      {campo('chilometri', 'Chilometri', { type: 'number', min: 0 })}
      {campo('prezzo', 'Prezzo di vendita (€)', { type: 'number', min: 1, step: '0.01' })}
      {campo('prezzoAcquisto', 'Prezzo di acquisto (€)', { type: 'number', min: 0, step: '0.01' })}
      <div className="sm:col-span-2">
        <label htmlFor="f-descr" className="label">Descrizione (solo testo)</label>
        <textarea id="f-descr" rows={4} maxLength={4000} className="input" required
                  value={form.descrizione} onChange={set('descrizione')} />
        {errori.descrizione && <p className="mt-1 text-xs text-red-600">{errori.descrizione}</p>}
      </div>
      <div className="space-y-3 rounded-xl border border-line p-4 sm:col-span-2">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <p className="text-sm font-bold">Foto</p>
          <button type="button" className="btn-ghost px-4 py-2" onClick={suggerisciFoto} disabled={!form.marca || !form.modello}>
            Cerca foto su Wikimedia
          </button>
        </div>
        {cercaFoto && <p className="text-xs text-ink-soft">{cercaFoto}</p>}
        <div className="grid gap-4 sm:grid-cols-[200px_1fr]">
          <FotoAuto auto={{ ...form, immagineUrl: form.immagineUrl }} className="aspect-[16/10] w-full rounded-lg" />
          <div className="space-y-2">
            {campo('immagineUrl', 'URL immagine', { required: false, placeholder: 'https://upload.wikimedia.org/…' })}
            <CreditoFoto auto={form} />
            {errori.attribuzioneCompleta && <p className="text-xs text-red-600">{errori.attribuzioneCompleta}</p>}
          </div>
        </div>
      </div>
      <div>
        <label htmlFor="f-stato" className="label">Stato</label>
        <select id="f-stato" className="input" value={form.stato} onChange={set('stato')}>
          <option value="BOZZA">Bozza (non visibile)</option>
          <option value="PUBBLICATA">Pubblicata</option>
        </select>
      </div>
      <div className="sm:col-span-2"><Messaggio tipo="errore">{errore}</Messaggio></div>
      <div className="flex gap-2 sm:col-span-2">
        <button className="btn-primary">Salva</button>
        <button type="button" className="btn-ghost" onClick={onAnnulla}>Annulla</button>
      </div>
    </motion.form>
  )
}

function PrezzoInline({ auto, onAggiornata, onErrore }) {
  const [valore, setValore] = useState(String(auto.prezzo))
  const cambiato = Number(valore) !== Number(auto.prezzo)

  async function salva(e) {
    e.preventDefault()
    try {
      onAggiornata(await api.admin.cambiaPrezzo(auto.id, Number(valore)))
    } catch (err) {
      onErrore(err.message)
    }
  }

  return (
    <form onSubmit={salva} className="flex items-center gap-1">
      <input type="number" min="1" step="0.01" aria-label={`Prezzo di ${auto.marca} ${auto.modello}`}
             className="input w-28 py-1" value={valore} onChange={(e) => setValore(e.target.value)} />
      {cambiato && <button className="btn-primary px-2 py-1 text-xs">OK</button>}
    </form>
  )
}

function GestioneAuto() {
  const [dati, setDati] = useState(null)
  const [stato, setStato] = useState('')
  const [modifica, setModifica] = useState(null) // null | 'nuova' | auto
  const [errore, setErrore] = useState('')

  const carica = useCallback((signal) => {
    api.admin.elenco({ stato, ordina: 'recenti', dimensione: 50 }, signal)
      .then(setDati)
      .catch((e) => { if (e.name !== 'AbortError') setErrore(e.message) })
  }, [stato])

  useEffect(() => {
    const ctrl = new AbortController()
    carica(ctrl.signal)
    return () => ctrl.abort()
  }, [carica])

  const sostituisci = (a) =>
    setDati((d) => ({ ...d, contenuto: d.contenuto.map((x) => (x.id === a.id ? a : x)) }))

  return (
    <section className="space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <p className="text-sm text-ink-soft">Bozze e prezzo d’acquisto inclusi. I ribassi attivano gli avvisi dopo il salvataggio.</p>
        <div className="flex gap-2">
          <select className="input w-44" value={stato} onChange={(e) => setStato(e.target.value)} aria-label="Filtra per stato">
            <option value="">Tutte</option>
            <option value="PUBBLICATA">Pubblicate</option>
            <option value="BOZZA">Bozze</option>
          </select>
          <button className="btn-primary" onClick={() => setModifica('nuova')}>+ Nuova auto</button>
        </div>
      </header>

      <Messaggio tipo="errore">{errore}</Messaggio>

      <AnimatePresence>
        {modifica && (
          <AutoForm key={modifica === 'nuova' ? 'nuova' : modifica.id}
                    iniziale={modifica === 'nuova' ? null : modifica}
                    onAnnulla={() => setModifica(null)}
                    onSalvata={() => { setModifica(null); carica() }} />
        )}
      </AnimatePresence>

      <div className="card overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-stone-200 bg-stone-50 text-stone-500">
            <tr>
              <th className="px-4 py-3 font-medium">Auto</th>
              <th className="px-4 py-3 font-medium">Stato</th>
              <th className="px-4 py-3 font-medium">Acquisto</th>
              <th className="px-4 py-3 font-medium">Prezzo</th>
              <th className="px-4 py-3 font-medium">Margine</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {dati?.contenuto.map((a) => (
              <tr key={a.id} className="border-b border-stone-100 last:border-0">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <FotoAuto auto={a} className="h-10 w-16 shrink-0 rounded-md" />
                    <div>
                      <p className="font-medium">{a.marca} {a.modello}</p>
                      <p className="text-xs text-stone-500">{a.anno} · #{a.id}</p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                    a.stato === 'PUBBLICATA' ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>
                    {a.stato === 'PUBBLICATA' ? 'Pubblicata' : 'Bozza'}
                  </span>
                </td>
                <td className="px-4 py-3 text-stone-600">{formatEuro(a.prezzoAcquisto)}</td>
                <td className="px-4 py-3">
                  <PrezzoInline key={`${a.id}-${a.version}`} auto={a} onAggiornata={sostituisci} onErrore={setErrore} />
                </td>
                <td className="px-4 py-3 text-stone-600">{formatEuro(a.prezzo - a.prezzoAcquisto)}</td>
                <td className="px-4 py-3 text-right">
                  <button className="text-brand-700 hover:underline" onClick={() => setModifica(a)}>Modifica</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

// ------------------------------------------------------------------
// Pannello amministratore: "vede tutto"
// ------------------------------------------------------------------

const data = (iso) => new Date(iso).toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' })

function Tabella({ colonne, righe, vuoto }) {
  if (!righe) return <p className="text-sm text-ink-soft">Caricamento…</p>
  if (righe.length === 0) return <div className="card p-8 text-center text-sm text-ink-soft">{vuoto}</div>
  return (
    <div className="card overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-line bg-ground/60 text-xs text-ink-soft uppercase">
          <tr>{colonne.map((c) => <th key={c.k} className="px-4 py-3 font-semibold tracking-wide">{c.t}</th>)}</tr>
        </thead>
        <tbody>
          {righe.map((r) => (
            <tr key={r.id} className="border-b border-line/60 last:border-0 hover:bg-ground/40">
              {colonne.map((c) => <td key={c.k} className="px-4 py-3 align-top">{c.r ? c.r(r) : r[c.k]}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

const Pill = ({ ok, si, no }) => (
  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${ok ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>
    {ok ? si : no}
  </span>
)

function Panoramica({ stat }) {
  if (!stat) return <p className="text-sm text-ink-soft">Caricamento…</p>
  const margine = Number(stat.valoreListino) - Number(stat.valoreAcquisto)
  const kpi = [
    { t: 'Utenti registrati', v: stat.utenti, n: `${stat.amministratori} amministratori` },
    { t: 'Auto pubblicate', v: stat.autoPubblicate, n: `${stat.autoBozze} in bozza` },
    { t: 'Preferiti salvati', v: stat.preferiti, n: 'da tutti gli utenti' },
    { t: 'Avvisi attivi', v: stat.avvisiAttivi, n: `${stat.avvisiInviati} notifiche già inviate` },
  ]
  return (
    <div className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpi.map((k) => (
          <div key={k.t} className="card p-5">
            <p className="text-xs font-semibold text-ink-soft">{k.t}</p>
            <p className="mt-1 text-3xl font-extrabold tabular-nums">{k.v}</p>
            <p className="text-xs text-ink-soft">{k.n}</p>
          </div>
        ))}
      </div>
      <div className="card grid gap-4 p-5 sm:grid-cols-3">
        <div><p className="text-xs font-semibold text-ink-soft">Valore di listino (pubblicate)</p><p className="text-xl font-bold tabular-nums">{formatEuro(stat.valoreListino)}</p></div>
        <div><p className="text-xs font-semibold text-ink-soft">Costo d’acquisto</p><p className="text-xl font-bold tabular-nums">{formatEuro(stat.valoreAcquisto)}</p></div>
        <div><p className="text-xs font-semibold text-ink-soft">Margine potenziale</p><p className="text-xl font-bold text-emerald-700 tabular-nums">{formatEuro(margine)}</p></div>
      </div>
    </div>
  )
}

function Utenti({ utenti, avvisi, preferiti }) {
  const [aperto, setAperto] = useState(null)
  const colonne = [
    { k: 'nome', t: 'Utente', r: (u) => (<div><p className="font-semibold">{u.nome}</p><p className="text-xs text-ink-soft">{u.email}</p></div>) },
    { k: 'ruolo', t: 'Ruolo', r: (u) => <Pill ok={u.ruolo === 'USER'} si="Utente" no="Admin" /> },
    { k: 'createdAt', t: 'Iscritto', r: (u) => data(u.createdAt) },
    { k: 'preferiti', t: 'Preferiti', r: (u) => <span className="tabular-nums">{u.preferiti}</span> },
    { k: 'avvisi', t: 'Avvisi', r: (u) => <span className="tabular-nums">{u.avvisi}</span> },
    { k: 'x', t: '', r: (u) => (
      <button className="text-sm font-semibold text-brand-600 hover:underline" onClick={() => setAperto(aperto === u.id ? null : u.id)}>
        {aperto === u.id ? 'Chiudi' : 'Dettagli'}
      </button>) },
  ]
  const sel = utenti?.find((u) => u.id === aperto)
  return (
    <div className="space-y-4">
      <Tabella colonne={colonne} righe={utenti} vuoto="Nessun utente registrato." />
      {sel && (
        <div className="card grid gap-6 p-5 md:grid-cols-2">
          <div>
            <p className="mb-2 font-bold">Preferiti di {sel.nome}</p>
            <ul className="space-y-1 text-sm">
              {preferiti?.filter((p) => p.utenteId === sel.id).map((p) => <li key={p.id}>{p.marca} {p.modello} · {formatEuro(p.prezzo)}</li>)}
              {!preferiti?.some((p) => p.utenteId === sel.id) && <li className="text-ink-soft">Nessuno</li>}
            </ul>
          </div>
          <div>
            <p className="mb-2 font-bold">Avvisi di {sel.nome}</p>
            <ul className="space-y-1 text-sm">
              {avvisi?.filter((a) => a.utenteId === sel.id).map((a) => (
                <li key={a.id}>{a.marca} {a.modello} · soglia {formatEuro(a.soglia)} · {a.inviato ? 'inviato' : 'in attesa'}</li>))}
              {!avvisi?.some((a) => a.utenteId === sel.id) && <li className="text-ink-soft">Nessuno</li>}
            </ul>
          </div>
        </div>
      )}
    </div>
  )
}

const TABS = [
  { k: 'panoramica', t: 'Panoramica' },
  { k: 'auto', t: 'Auto' },
  { k: 'utenti', t: 'Utenti' },
  { k: 'avvisi', t: 'Avvisi' },
  { k: 'preferiti', t: 'Preferiti' },
]

export default function Admin() {
  const [params, setParams] = useSearchParams()
  const tab = TABS.some((t) => t.k === params.get('tab')) ? params.get('tab') : 'panoramica'
  const [dati, setDati] = useState({})
  const [errore, setErrore] = useState('')

  useEffect(() => {
    Promise.all([api.admin.statistiche(), api.admin.utenti(), api.admin.avvisi(), api.admin.preferiti()])
      .then(([stat, utenti, avvisi, preferiti]) => setDati({ stat, utenti, avvisi, preferiti }))
      .catch((e) => setErrore(e.message))
  }, [tab])

  return (
    <section className="space-y-6">
      <header className="space-y-4">
        <h1 className="text-2xl font-extrabold">Pannello amministratore</h1>
        <nav className="flex gap-1 overflow-x-auto rounded-full bg-white p-1 shadow-sm" aria-label="Sezioni">
          {TABS.map((t) => (
            <button key={t.k} onClick={() => setParams({ tab: t.k })} aria-current={tab === t.k ? 'page' : undefined}
                    className={`rounded-full px-4 py-2 text-sm font-semibold whitespace-nowrap transition ${
                      tab === t.k ? 'bg-ink text-white' : 'text-ink-soft hover:text-ink'}`}>
              {t.t}
              {t.k === 'utenti' && dati.utenti && <span className="ml-1.5 opacity-70">{dati.utenti.length}</span>}
              {t.k === 'avvisi' && dati.avvisi && <span className="ml-1.5 opacity-70">{dati.avvisi.length}</span>}
              {t.k === 'preferiti' && dati.preferiti && <span className="ml-1.5 opacity-70">{dati.preferiti.length}</span>}
            </button>
          ))}
        </nav>
      </header>

      <Messaggio tipo="errore">{errore}</Messaggio>

      {tab === 'panoramica' && <Panoramica stat={dati.stat} />}
      {tab === 'auto' && <GestioneAuto />}
      {tab === 'utenti' && <Utenti utenti={dati.utenti} avvisi={dati.avvisi} preferiti={dati.preferiti} />}
      {tab === 'avvisi' && (
        <Tabella vuoto="Nessun avviso di prezzo." righe={dati.avvisi} colonne={[
          { k: 'u', t: 'Utente', r: (a) => (<div><p className="font-semibold">{a.utenteNome}</p><p className="text-xs text-ink-soft">{a.utenteEmail}</p></div>) },
          { k: 'a', t: 'Auto', r: (a) => `${a.marca} ${a.modello}` },
          { k: 'p', t: 'Prezzo attuale', r: (a) => <span className="tabular-nums">{formatEuro(a.prezzoAttuale)}</span> },
          { k: 's', t: 'Soglia', r: (a) => <span className="tabular-nums">{formatEuro(a.soglia)}</span> },
          { k: 'i', t: 'Stato', r: (a) => <Pill ok={!a.inviato} si="In attesa" no="Inviato" /> },
          { k: 'd', t: 'Creato', r: (a) => data(a.createdAt) },
        ]} />
      )}
      {tab === 'preferiti' && (
        <Tabella vuoto="Nessun preferito salvato." righe={dati.preferiti} colonne={[
          { k: 'u', t: 'Utente', r: (p) => p.utenteEmail },
          { k: 'a', t: 'Auto', r: (p) => `${p.marca} ${p.modello}` },
          { k: 'p', t: 'Prezzo', r: (p) => <span className="tabular-nums">{formatEuro(p.prezzo)}</span> },
          { k: 's', t: 'Stato auto', r: (p) => <Pill ok={p.statoAuto === 'PUBBLICATA'} si="Pubblicata" no="Bozza" /> },
          { k: 'd', t: 'Salvato', r: (p) => data(p.createdAt) },
        ]} />
      )}
    </section>
  )
}
