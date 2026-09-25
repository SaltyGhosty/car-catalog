import { Link } from 'react-router'
import { motion } from 'motion/react'
import { formatEuro, formatKm } from '../api.js'
import { Cuore } from './AutoCard.jsx'
import FotoAuto from './FotoAuto.jsx'

/** Annuncio in formato lista (foto a sinistra, dati a destra), come nei portali di auto usate. */
export default function AutoRiga({ auto, preferito, onTogglePreferito, loggato }) {
  return (
    <motion.article
      layout
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.2 }}
      className="card group relative overflow-hidden transition-shadow hover:shadow-[0_8px_24px_rgba(29,29,27,0.12)]"
    >
      <Link to={`/auto/${auto.id}`} className="grid sm:grid-cols-[300px_1fr]">
        <div className="aspect-[16/10] overflow-hidden sm:aspect-auto sm:h-full sm:min-h-[190px]">
          <FotoAuto auto={auto} className="size-full transition-transform duration-300 group-hover:scale-[1.03]" />
        </div>
        <div className="flex flex-col gap-3 p-5">
          <div className="pr-12">
            <h3 className="text-lg leading-snug font-bold">{auto.marca} {auto.modello}</h3>
            <p className="mt-1 line-clamp-2 text-sm text-ink-soft">{auto.descrizione}</p>
          </div>
          <ul className="flex flex-wrap gap-1.5 text-xs font-semibold text-ink-soft">
            <li className="rounded-md bg-ground px-2 py-1">{auto.anno}</li>
            <li className="rounded-md bg-ground px-2 py-1 tabular-nums">{formatKm(auto.chilometri)}</li>
          </ul>
          <div className="mt-auto flex items-end justify-between gap-3">
            <p className="text-2xl font-extrabold tracking-tight tabular-nums">{formatEuro(auto.prezzo)}</p>
            <span className="text-sm font-semibold text-brand-600 group-hover:underline">Vedi annuncio →</span>
          </div>
        </div>
      </Link>
      {loggato && (
        <motion.button
          whileTap={{ scale: 0.85 }}
          onClick={() => onTogglePreferito(auto.id)}
          aria-pressed={preferito}
          aria-label={preferito ? 'Rimuovi dai preferiti' : 'Aggiungi ai preferiti'}
          className={`absolute top-3 right-3 grid size-10 place-items-center rounded-full bg-white shadow-sm transition hover:scale-105 sm:top-4 sm:right-4 ${
            preferito ? 'text-brand-600' : 'text-ink'}`}
        >
          <Cuore pieno={preferito} />
        </motion.button>
      )}
    </motion.article>
  )
}
