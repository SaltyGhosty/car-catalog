import { Route, Routes } from 'react-router'
import Layout from './components/Layout.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Admin from './pages/Admin.jsx'
import AutoUsate from './pages/AutoUsate.jsx'
import Avvisi from './pages/Avvisi.jsx'
import CookiePolicy from './pages/CookiePolicy.jsx'
import DettaglioAuto from './pages/DettaglioAuto.jsx'
import DisattivaAvviso from './pages/DisattivaAvviso.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import PasswordDimenticata from './pages/PasswordDimenticata.jsx'
import ReimpostaPassword from './pages/ReimpostaPassword.jsx'
import NotFound from './pages/NotFound.jsx'
import Preferiti from './pages/Preferiti.jsx'
import PrivacyPolicy from './pages/PrivacyPolicy.jsx'
import Profilo from './pages/Profilo.jsx'
import Registrazione from './pages/Registrazione.jsx'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="auto-usate" element={<AutoUsate />} />
        <Route path="auto/:id" element={<DettaglioAuto />} />
        <Route path="login" element={<Login />} />
        <Route path="registrazione" element={<Registrazione />} />
        <Route path="password-dimenticata" element={<PasswordDimenticata />} />
        <Route path="reimposta-password" element={<ReimpostaPassword />} />
        <Route path="avvisi/disattiva" element={<DisattivaAvviso />} />
        <Route path="privacy" element={<PrivacyPolicy />} />
        <Route path="cookie" element={<CookiePolicy />} />

        <Route element={<ProtectedRoute />}>
          <Route path="preferiti" element={<Preferiti />} />
          <Route path="avvisi" element={<Avvisi />} />
          <Route path="profilo" element={<Profilo />} />
        </Route>
        <Route element={<ProtectedRoute admin />}>
          <Route path="admin" element={<Admin />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
