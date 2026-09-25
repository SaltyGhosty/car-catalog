/**
 * UNICO punto da cui partono le fetch verso il backend.
 * - URL base da VITE_API_URL (Render) con fallback locale
 * - token JWT in localStorage (documentato in Privacy e Cookie Policy)
 * - errori normalizzati in ApiError { status, message, campi }
 */
// In produzione VITE_API_URL = URL del backend su Render; in sviluppo vuoto → proxy di Vite (vite.config.js)
const BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '')
const TOKEN_KEY = 'cc_token'

export const tokenStore = {
  get() {
    try { return localStorage.getItem(TOKEN_KEY) } catch { return null }
  },
  set(token) {
    try { localStorage.setItem(TOKEN_KEY, token) } catch { /* storage non disponibile */ }
  },
  clear() {
    try { localStorage.removeItem(TOKEN_KEY) } catch { /* storage non disponibile */ }
  },
}

export class ApiError extends Error {
  constructor(status, message, campi = {}, retryAfter = null) {
    super(message)
    this.status = status
    this.campi = campi
    this.retryAfter = retryAfter // secondi, per il blocco del login (429)
  }
}

async function request(path, { method = 'GET', body, signal } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    })
  } catch (err) {
    if (err.name === 'AbortError') throw err
    throw new ApiError(0, 'Server non raggiungibile. Riprova tra poco.')
  }

  if (res.status === 401 && token) {
    // token scaduto o account eliminato: logout "silenzioso"
    tokenStore.clear()
    window.dispatchEvent(new Event('auth:logout'))
  }
  if (res.status === 204) return null

  const data = await res.json().catch(() => null)
  if (!res.ok) {
    const retry = Number(res.headers.get('Retry-After')) || null
    throw new ApiError(res.status, data?.messaggio ?? `Errore ${res.status}`, data?.campi ?? {}, retry)
  }
  return data
}

const query = (params) => {
  const qs = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') qs.set(k, String(v))
  })
  const s = qs.toString()
  return s ? `?${s}` : ''
}

export const api = {
  auth: {
    login: (email, password) => request('/api/auth/login', { method: 'POST', body: { email, password } }),
    registrazione: (dati) => request('/api/auth/registrazione', { method: 'POST', body: dati }),
    passwordDimenticata: (email) => request('/api/auth/password-dimenticata', { method: 'POST', body: { email } }),
    reimpostaPassword: (token, nuovaPassword) =>
      request('/api/auth/reimposta-password', { method: 'POST', body: { token, nuovaPassword } }),
  },
  me: {
    get: () => request('/api/me'),
    aggiorna: (nome) => request('/api/me', { method: 'PUT', body: { nome } }),
    elimina: (password) => request('/api/me', { method: 'DELETE', body: { password } }),
  },
  auto: {
    cerca: (params, signal) => request(`/api/auto${query(params)}`, { signal }),
    dettaglio: (id) => request(`/api/auto/${encodeURIComponent(id)}`),
    filtri: () => request('/api/auto/filtri'),
  },
  preferiti: {
    lista: () => request('/api/preferiti'),
    aggiungi: (autoId) => request('/api/preferiti', { method: 'POST', body: { autoId } }),
    rimuovi: (id) => request(`/api/preferiti/${encodeURIComponent(id)}`, { method: 'DELETE' }),
  },
  avvisi: {
    lista: () => request('/api/avvisi'),
    crea: (autoId, soglia) => request('/api/avvisi', { method: 'POST', body: { autoId, soglia } }),
    aggiornaSoglia: (id, soglia) => request(`/api/avvisi/${encodeURIComponent(id)}`, { method: 'PATCH', body: { soglia } }),
    elimina: (id) => request(`/api/avvisi/${encodeURIComponent(id)}`, { method: 'DELETE' }),
    disattiva: (token) => request('/api/avvisi/disattiva', { method: 'POST', body: { token } }),
  },
  admin: {
    statistiche: () => request('/api/admin/statistiche'),
    utenti: () => request('/api/admin/utenti'),
    avvisi: () => request('/api/admin/avvisi'),
    preferiti: () => request('/api/admin/preferiti'),
    elenco: (params, signal) => request(`/api/admin/auto${query(params)}`, { signal }),
    crea: (auto) => request('/api/admin/auto', { method: 'POST', body: auto }),
    aggiorna: (id, auto) => request(`/api/admin/auto/${encodeURIComponent(id)}`, { method: 'PUT', body: auto }),
    suggerisciImmagine: (marca, modello) => request(`/api/admin/immagini/suggerimento${query({ marca, modello })}`),
    cambiaPrezzo: (id, prezzo) =>
      request(`/api/admin/auto/${encodeURIComponent(id)}/prezzo`, { method: 'PATCH', body: { prezzo } }),
  },
}

export const formatEuro = (v) =>
  new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(Number(v))

export const formatKm = (v) => `${new Intl.NumberFormat('it-IT').format(v)} km`
