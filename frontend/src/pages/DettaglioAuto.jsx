import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { motion } from 'motion/react'
import { api, formatEuro, formatKm } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { Cuore } from '../components/AutoCard.jsx'
import FotoAuto, { CreditoFoto } from '../components/FotoAuto.jsx'
import Messaggio from '../components/Messaggio.jsx'
import SogliaForm from '../components/SogliaForm.jsx'
import { usePreferiti } from '../components/usePreferiti.js'

export default function DettaglioAuto() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { utente } = useAuth()
  const { preferiti, toggle } = usePreferiti()
  const [auto, setAuto] = useState(null)
  const [avviso, setAvviso] = useState(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    api.auto.dettaglio(id).then(setAuto).catch((e) => setErrore(e.message))
  }, [id])

  useEffect(() => {
    if (!utente) return
    api.avvisi.lista()
      .then((lista) => setAvviso(lista.find((a) => String(a.autoId) === String(id)) ?? null))
      .catch(() => {})
  }, [utente, id])

  if (errore) return <Messaggio tipo="errore">{errore}</Messaggio>
  if (!auto) return <p className="text-stone-500">Caricamento…</p>

  const preferito = preferiti.has(auto.id)

  return (
    <motion.article initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <div className="card overflow-hidden">
        <FotoAuto auto={auto} eager className="aspect-[16/10] w-full" />
        <div className="space-y-6 p-6 sm:p-8">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <button onClick={() => navigate(-1)} className="text-sm font-semibold text-ink-soft hover:text-ink">← Torna ai risultati</button>
            <CreditoFoto auto={auto} />
          </div>
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm font-bold tracking-wider text-ink-soft uppercase">{auto.marca}</p>
              <h1 className="text-3xl font-extrabold sm:text-4xl">{auto.modello}</h1>
            </div>
            <p className="text-3xl font-extrabold tabular-nums">{formatEuro(auto.prezzo)}</p>
          </div>
          <dl className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div className="rounded-xl bg-ground p-3"><dt className="text-xs font-semibold text-ink-soft">Anno</dt><dd className="font-bold">{auto.anno}</dd></div>
            <div className="rounded-xl bg-ground p-3"><dt className="text-xs font-semibold text-ink-soft">Chilometri</dt><dd className="font-bold tabular-nums">{formatKm(auto.chilometri)}</dd></div>
          </dl>
          <div className="space-y-2">
            <h2 className="text-lg font-bold">Descrizione</h2>
            {/* Testo puro: whitespace-pre-line mantiene gli a capo senza interpretare HTML */}
            <p className="whitespace-pre-line text-ink-soft">{auto.descrizione}</p>
          </div>
        </div>
      </div>

      <aside className="card h-fit space-y-5 p-6 lg:sticky lg:top-24">
        {utente ? (
          <>
            <motion.button whileTap={{ scale: 0.97 }} onClick={() => toggle(auto.id).catch((e) => setErrore(e.message))}
                           className={preferito ? 'btn-ghost w-full text-rose-600' : 'btn-ghost w-full'}
                           aria-pressed={preferito}>
              <Cuore pieno={preferito} /> {preferito ? 'Nei preferiti' : 'Aggiungi ai preferiti'}
            </motion.button>
            <hr className="border-stone-200" />
            <SogliaForm key={avviso?.id ?? 'nuovo'} auto={auto} avviso={avviso}
                        onSalvato={setAvviso} onEliminato={() => setAvviso(null)} />
          </>
        ) : (
          <p className="text-sm text-stone-600">
            <Link to="/login" className="font-semibold text-brand-600 hover:underline">Accedi</Link> per salvare
            l’auto nei preferiti e ricevere un’email quando il prezzo scende.
          </p>
        )}
      </aside>
    </motion.article>
  )
}
