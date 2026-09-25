import { useState } from 'react'
import { SagomaAuto } from './SagomaAuto.jsx'

/**
 * Foto dell'auto con fallback alla sagoma se manca o non si carica.
 * L'URL arriva dal backend già validato (solo /auto/*.webp o upload.wikimedia.org).
 */
export default function FotoAuto({ auto, className = '', eager = false }) {
  const [errore, setErrore] = useState(false)
  if (!auto.immagineUrl || errore) {
    return (
      <div className={`grid place-items-center bg-[#e6e1db] px-8 ${className}`}>
        <SagomaAuto className="w-full max-w-[240px]" />
      </div>
    )
  }
  return (
    <img
      src={auto.immagineUrl}
      alt={`${auto.marca} ${auto.modello}`}
      loading={eager ? 'eager' : 'lazy'}
      decoding="async"
      referrerPolicy="no-referrer"
      onError={() => setErrore(true)}
      className={`bg-[#e6e1db] object-cover ${className}`}
    />
  )
}

/** Attribuzione richiesta dalle licenze Creative Commons delle foto. */
export function CreditoFoto({ auto }) {
  if (!auto.immagineCredito) return null
  return (
    <p className="text-xs text-ink-soft">
      {auto.immagineCredito}
      {auto.immagineUrl?.startsWith('/auto/') && ' · ritagliata'}
      {auto.immagineFonte && (
        <>
          {' · '}
          <a href={auto.immagineFonte} target="_blank" rel="noopener noreferrer" className="underline hover:text-ink">
            file originale
          </a>
        </>
      )}
    </p>
  )
}
