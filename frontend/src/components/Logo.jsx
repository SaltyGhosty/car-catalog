export default function Logo() {
  return (
    <span className="flex items-center gap-2 text-xl font-extrabold tracking-tight">
      <svg viewBox="0 0 32 32" className="size-8" aria-hidden="true">
        <rect width="32" height="32" rx="9" fill="var(--color-brand-600)" />
        <path d="M7 19.5l2.3-5.6A2.2 2.2 0 0 1 11.3 12.5h9.4a2.2 2.2 0 0 1 2 1.4L25 19.5V23h-2.6v-1.8H9.6V23H7z" fill="#fff" />
        <circle cx="11" cy="19" r="1.3" fill="var(--color-brand-600)" />
        <circle cx="21" cy="19" r="1.3" fill="var(--color-brand-600)" />
      </svg>
      <span>car<span className="text-brand-600">catalog</span></span>
    </span>
  )
}
