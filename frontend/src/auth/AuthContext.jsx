import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, tokenStore } from '../api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [utente, setUtente] = useState(null)
  const [caricamento, setCaricamento] = useState(() => Boolean(tokenStore.get()))

  useEffect(() => {
    if (!tokenStore.get()) return
    api.me.get()
      .then(setUtente)
      .catch(() => tokenStore.clear())
      .finally(() => setCaricamento(false))
  }, [])

  useEffect(() => {
    const onLogout = () => setUtente(null)
    window.addEventListener('auth:logout', onLogout)
    return () => window.removeEventListener('auth:logout', onLogout)
  }, [])

  const salvaSessione = useCallback(({ token, utente }) => {
    tokenStore.set(token)
    setUtente(utente)
    return utente
  }, [])

  const login = useCallback(
    async (email, password) => salvaSessione(await api.auth.login(email, password)),
    [salvaSessione],
  )
  const registrazione = useCallback(
    async (dati) => salvaSessione(await api.auth.registrazione(dati)),
    [salvaSessione],
  )
  const logout = useCallback(() => {
    tokenStore.clear()
    setUtente(null)
  }, [])

  const value = useMemo(
    () => ({ utente, setUtente, caricamento, login, registrazione, logout, isAdmin: utente?.ruolo === 'ADMIN' }),
    [utente, caricamento, login, registrazione, logout],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve essere usato dentro <AuthProvider>')
  return ctx
}
