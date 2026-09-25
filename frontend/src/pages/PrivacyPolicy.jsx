import { Link } from 'react-router'
import { LEGAL } from '../config/legal.js'

export default function PrivacyPolicy() {
  return (
    <article className="prose-legal card mx-auto max-w-3xl p-8">
      <h1 className="text-3xl font-bold">Privacy Policy</h1>
      <p className="mt-1 text-xs text-stone-500">Ultimo aggiornamento: {LEGAL.ultimoAggiornamento}</p>

      <p className="mt-4">
        Questa informativa, resa ai sensi degli artt. 13 e 14 del Regolamento (UE) 2016/679 (“GDPR”), descrive come
        Car Catalog tratta i dati personali di chi usa il sito.
      </p>

      <h2>1. Titolare del trattamento</h2>
      <p>{LEGAL.titolare} — contatto: {LEGAL.contatto}</p>

      <h2>2. Quali dati raccogliamo</h2>
      <table>
        <thead><tr><th>Dato</th><th>Quando</th><th>Perché</th></tr></thead>
        <tbody>
          <tr><td>Indirizzo email</td><td>Registrazione</td><td>Identificarti al login e inviarti gli avvisi di prezzo che hai richiesto</td></tr>
          <tr><td>Nome</td><td>Registrazione / profilo</td><td>Personalizzare l’interfaccia e le email</td></tr>
          <tr><td>Password</td><td>Registrazione</td><td>Autenticazione. Salviamo solo un’impronta cifrata (hash BCrypt), mai la password in chiaro</td></tr>
          <tr><td>Auto preferite</td><td>Quando aggiungi un preferito</td><td>Mostrarti l’elenco delle auto salvate</td></tr>
          <tr><td>Avvisi di prezzo: auto, soglia, stato “notifica inviata”</td><td>Quando crei un avviso</td><td>Inviarti una sola email quando il prezzo scende alla soglia</td></tr>
          <tr><td>Richiesta di reimpostazione password (solo l’impronta del link, data di scadenza)</td><td>Quando usi “Password dimenticata”</td><td>Verificare il link monouso inviato via email; scade dopo 30 minuti</td></tr>
          <tr><td>Token di accesso (JWT)</td><td>Login</td><td>Mantenere la sessione; salvato nel localStorage del browser (vedi <Link to="/cookie" className="underline">Cookie Policy</Link>)</td></tr>
          <tr><td>Dati tecnici di connessione (indirizzo IP, orario, URL richiesto)</td><td>Ogni richiesta</td><td>Registrati dal fornitore di hosting per sicurezza e funzionamento</td></tr>
        </tbody>
      </table>
      <p>Non raccogliamo dati di pagamento, dati di localizzazione né categorie particolari di dati. La consultazione del catalogo non richiede registrazione.</p>

      <h2>3. Basi giuridiche</h2>
      <ul>
        <li><strong>Esecuzione del servizio richiesto</strong> (art. 6.1.b GDPR): account, preferiti e avvisi di prezzo.</li>
        <li><strong>Legittimo interesse</strong> (art. 6.1.f GDPR): sicurezza del sito e prevenzione abusi (log tecnici).</li>
      </ul>
      <p>Non effettuiamo profilazione, marketing né invio di newsletter. Le uniche email sono le notifiche di prezzo che imposti tu.</p>

      <h2>4. Chi tratta i dati per nostro conto</h2>
      <ul>
        <li><strong>Render Services, Inc.</strong> — hosting dell’applicazione e del database PostgreSQL (regione UE, Francoforte).</li>
        <li><strong>Wikimedia Foundation</strong> — alcune foto delle auto possono essere caricate direttamente da
          upload.wikimedia.org: in quel caso il tuo browser comunica a Wikimedia indirizzo IP e user agent, come per
          qualsiasi immagine esterna. Le foto del catalogo iniziale sono invece servite dal nostro sito.</li>
        <li><strong>Google (Gmail SMTP)</strong> — invio delle email di notifica. Il trasferimento verso gli USA avviene sulla base del
          EU-US Data Privacy Framework e/o delle Clausole Contrattuali Standard.</li>
      </ul>
      <p>
        I dati non vengono venduti né comunicati ad altri soggetti. Gli amministratori del servizio possono consultare
        account (nome ed email), preferiti e avvisi di prezzo per gestione, assistenza e sicurezza.
      </p>

      <h2>5. Per quanto tempo</h2>
      <ul>
        <li>Account, preferiti e avvisi: finché l’account resta attivo. Alla cancellazione vengono eliminati immediatamente.</li>
        <li>Un singolo avviso: finché non lo elimini dall’area “Avvisi” o dal link monouso presente nell’email.</li>
        <li>Token di accesso: scade dopo 2 ore; viene rimosso dal browser al logout.</li>
        <li>Log tecnici: secondo i tempi di conservazione del fornitore di hosting.</li>
      </ul>

      <h2>6. Sicurezza</h2>
      <p>
        Connessioni cifrate HTTPS, password salvate con hash BCrypt, token firmati con durata limitata, controllo che ogni
        utente possa accedere solo ai propri preferiti e avvisi. Password ed email non vengono mai scritte nei log applicativi.
      </p>

      <h2>7. I tuoi diritti</h2>
      <p>
        Puoi esercitare in ogni momento i diritti degli artt. 15-22 GDPR: accesso, rettifica, cancellazione, limitazione,
        portabilità e opposizione. Puoi modificare il nome e <strong>cancellare l’account in autonomia</strong> dalla pagina
        “Profilo”; per le altre richieste scrivi a {LEGAL.contatto}. Hai inoltre diritto di proporre reclamo al Garante per la
        protezione dei dati personali (<span className="whitespace-nowrap">www.garanteprivacy.it</span>).
      </p>

      <h2>8. Modifiche</h2>
      <p>Eventuali modifiche a questa informativa saranno pubblicate su questa pagina con la data di aggiornamento.</p>
    </article>
  )
}
