/** Sagoma generica di un'auto, usata come segnaposto finché non ci sono foto. */
export function SagomaAuto({ className = '' }) {
  return (
    <svg viewBox="0 0 240 100" className={className} aria-hidden="true">
      <ellipse cx="120" cy="88" rx="100" ry="6" fill="rgba(29,29,27,0.12)" />
      <path fill="rgba(29,29,27,0.78)"
            d="M22 70c0-8 4-13 12-15l28-6 26-18c6-4 12-6 20-6h40c8 0 14 3 19 8l17 16 30 5c9 2 14 7 14 15v7c0 3-2 5-5 5h-12a20 20 0 0 0-39 0H86a20 20 0 0 0-39 0H28c-4 0-6-2-6-5z" />
      <path fill="rgba(255,255,255,0.55)" d="M96 32h28v20H72l16-14c2-4 5-6 8-6zm34 0h22c5 0 9 2 12 5l14 15h-48z" />
      <circle cx="66" cy="80" r="14" fill="#1d1d1b" /><circle cx="66" cy="80" r="6" fill="#bdb6ae" />
      <circle cx="178" cy="80" r="14" fill="#1d1d1b" /><circle cx="178" cy="80" r="6" fill="#bdb6ae" />
    </svg>
  )
}
