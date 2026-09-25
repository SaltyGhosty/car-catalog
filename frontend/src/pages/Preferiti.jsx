import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import { api } from '../api.js'
import AutoCard from '../components/AutoCard.jsx'
import Messaggio from '../components/Messaggio.jsx'

export default function Preferiti() {
  const [lista, setLista] = useState(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    api.preferiti.lista().then(setLista).catch((e) => setErrore(e.message))
  }, [])

  async function rimuovi(autoId) {
    const p = lista.find((x) => x.auto.id === autoId)
    try {
      await api.preferiti.rimuovi(p.id)
      setLista((l) => l.filter((x) => x.id !== p.id))
    } catch (e) {
      setErrore(e.message)
    }
  }

  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-bold">I tuoi preferiti</h1>
      <Messaggio tipo="errore">{errore}</Messaggio>
      {lista?.length === 0 && (
        <p className="text-stone-500">Nessun preferito. <Link to="/" className="text-brand-700 hover:underline">Sfoglia il catalogo</Link>.</p>
      )}
      <motion.div layout className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        <AnimatePresence mode="popLayout">
          {lista?.map((p) => (
            <AutoCard key={p.id} auto={p.auto} loggato preferito onTogglePreferito={rimuovi} />
          ))}
        </AnimatePresence>
      </motion.div>
    </section>
  )
}
