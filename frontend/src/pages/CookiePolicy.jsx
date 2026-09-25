import { Link } from 'react-router'
import { LEGAL } from '../config/legal.js'

export default function CookiePolicy() {
  return (
    <article className="prose-legal card mx-auto max-w-3xl p-8">
      <h1 className="text-3xl font-bold">Cookie Policy</h1>
      <p className="mt-1 text-xs text-stone-500">Ultimo aggiornamento: {LEGAL.ultimoAggiornamento}</p>

      <h2>In breve</h2>
      <p>
        Car Catalog <strong>non utilizza cookie</strong>: né tecnici, né analitici, né di profilazione, e non include
        strumenti di terze parti (analytics, pubblicità, social plugin, font esterni). Per questo non ti mostriamo un banner
        di consenso. Alcune foto possono essere caricate da Wikimedia Commons (upload.wikimedia.org), che non riceve
        dati dell’account e non è usato per tracciamento da parte nostra.
      </p>

      <h2>Cosa salviamo nel tuo browser</h2>
      <p>Usiamo solo il <strong>localStorage</strong> del browser, esclusivamente per un elemento tecnico necessario:</p>
      <table>
        <thead><tr><th>Chiave</th><th>Contenuto</th><th>Finalità</th><th>Durata</th></tr></thead>
        <tbody>
          <tr>
            <td><code>cc_token</code></td>
            <td>Token di accesso firmato (JWT) con il solo identificativo numerico del tuo account e la scadenza</td>
            <td>Mantenerti autenticato tra una pagina e l’altra</td>
            <td>Valido 2 ore; cancellato quando fai “Esci” o elimini l’account</td>
          </tr>
        </tbody>
      </table>
      <p>
        Viene creato solo dopo il login o la registrazione, cioè quando sei tu a richiedere il servizio. Trattandosi di uno
        strumento strettamente necessario, ai sensi dell’art. 122 del Codice Privacy e delle Linee guida del Garante del
        10 giugno 2021 non richiede consenso. Il token non contiene email, nome o password.
      </p>

      <h2>Come rimuoverlo</h2>
      <ul>
        <li>Clicca su “Esci” in alto a destra: il token viene eliminato subito.</li>
        <li>In alternativa cancella i “dati dei siti” dalle impostazioni del browser.</li>
      </ul>

      <h2>Contatti</h2>
      <p>
        Titolare: {LEGAL.titolare} — {LEGAL.contatto}. Per i dettagli sul trattamento dei dati leggi la{' '}
        <Link to="/privacy" className="underline">Privacy Policy</Link>.
      </p>
    </article>
  )
}
