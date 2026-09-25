const stili = {
  errore: 'border-red-200 bg-red-50 text-red-800',
  ok: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  info: 'border-stone-200 bg-stone-50 text-stone-700',
}

export default function Messaggio({ tipo = 'info', children }) {
  if (!children) return null
  return (
    <div role={tipo === 'errore' ? 'alert' : 'status'} className={`rounded-lg border px-3 py-2 text-sm ${stili[tipo]}`}>
      {children}
    </div>
  )
}
