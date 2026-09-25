import { Link } from 'react-router'
import { motion } from 'motion/react'
import { formatEuro, formatKm } from '../api.js'
import FotoAuto from './FotoAuto.jsx'

export { SagomaAuto } from './SagomaAuto.jsx'

export function Cuore({ pieno }) {
  return (
    <svg viewBox="0 0 24 24" className="size-5" aria-hidden="true"
         fill={pieno ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2">
      <path strokeLinejoin="round"
            d="M12 20.5s-7.5-4.6-9.2-9.3C1.7 8 3.8 4.5 7.3 4.5c2 0 3.4 1.1 4.7 2.8 1.3-1.7 2.7-2.8 4.7-2.8 3.5 0 5.6 3.5 4.5 6.7-1.7 4.7-9.2 9.3-9.2 9.3z" />
    </svg>
  )
}

export default function AutoCard({ auto, preferito, onTogglePreferito, loggato }) {
  return (
    <motion.article
      layout
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.96 }}
      transition={{ duration: 0.25 }}
      className="card group relative flex flex-col overflow-hidden transition-shadow hover:shadow-[0_8px_24px_rgba(29,29,27,0.12)]"
    >
      <Link to={`/auto/${auto.id}`} className="flex flex-1 flex-col" aria-label={`${auto.marca} ${auto.modello}, ${formatEuro(auto.prezzo)}`}>
        <div className="aspect-[16/10] overflow-hidden">
          <FotoAuto auto={auto} className="size-full transition-transform duration-300 group-hover:scale-[1.04]" />
        </div>
        <div className="flex flex-1 flex-col gap-3 p-4">
          <div>
            <h3 className="text-base leading-snug font-bold">{auto.marca} {auto.modello}</h3>
            {/* Descrizione = testo puro, niente HTML */}
            <p className="mt-0.5 line-clamp-1 text-sm text-ink-soft">{auto.descrizione}</p>
          </div>
          <p className="text-2xl font-extrabold tracking-tight tabular-nums">{formatEuro(auto.prezzo)}</p>
          <ul className="mt-auto flex flex-wrap gap-1.5 text-xs font-semibold text-ink-soft">
            <li className="rounded-md bg-ground px-2 py-1">{auto.anno}</li>
            <li className="rounded-md bg-ground px-2 py-1 tabular-nums">{formatKm(auto.chilometri)}</li>
          </ul>
        </div>
      </Link>
      {loggato && (
        <motion.button
          whileTap={{ scale: 0.85 }}
          onClick={() => onTogglePreferito(auto.id)}
          aria-pressed={preferito}
          aria-label={preferito ? 'Rimuovi dai preferiti' : 'Aggiungi ai preferiti'}
          className={`absolute top-3 right-3 grid size-10 place-items-center rounded-full bg-white shadow-sm transition hover:scale-105 ${
            preferito ? 'text-brand-600' : 'text-ink'
          }`}
        >
          <Cuore pieno={preferito} />
        </motion.button>
      )}
    </motion.article>
  )
}
