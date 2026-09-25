import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import { api, formatEuro } from '../api.js'
import Messaggio from '../components/Messaggio.jsx'
import SogliaForm from '../components/SogliaForm.jsx'

export default function Avvisi() {
  const [avvisi, setAvvisi] = useState(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    api.avvisi.lista().then(setAvvisi).catch((e) => setErrore(e.message))
  }, [])

  const sostituisci = (nuovo) => setAvvisi((l) => l.map((a) => (a.id === nuovo.id ? nuovo : a)))
  const togli = (id) => setAvvisi((l) => l.filter((a) => a.id !== id))

  return (
    <section className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Avvisi di prezzo</h1>
        <p className="text-sm text-stone-500">Ricevi un’email una sola volta, quando il prezzo scende alla soglia o sotto.</p>
      </header>
      <Messaggio tipo="errore">{errore}</Messaggio>
      {avvisi?.length === 0 && (
        <p className="text-stone-500">Nessun avviso attivo. Aprine uno dalla pagina di un’auto.</p>
      )}
      <ul className="grid gap-4 md:grid-cols-2">
        <AnimatePresence>
          {avvisi?.map((a) => (
            <motion.li key={a.id} layout initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
                       exit={{ opacity: 0, x: -20 }} className="card space-y-4 p-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <Link to={`/auto/${a.autoId}`} className="font-semibold hover:underline">{a.marca} {a.modello}</Link>
                  <p className="text-sm text-stone-500">Prezzo attuale {formatEuro(a.prezzoAttuale)}</p>
                </div>
                <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                  a.inviato ? 'bg-stone-100 text-stone-600' : 'bg-brand-50 text-brand-700'}`}>
                  {a.inviato ? 'Notifica inviata' : 'In attesa'}
                </span>
              </div>
              <SogliaForm auto={{ id: a.autoId, prezzo: a.prezzoAttuale }} avviso={a}
                          onSalvato={sostituisci} onEliminato={togli} />
            </motion.li>
          ))}
        </AnimatePresence>
      </ul>
    </section>
  )
}
