package it.carcatalog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String mittente;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String mittente,
                        @Value("${app.allowed-origin}") String frontendUrl) {
        this.mailSender = mailSender;
        this.mittente = mittente;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    public void inviaAvvisoPrezzo(DatiNotifica d) {
        if (mittente == null || mittente.isBlank()) {
            log.info("MAIL_USERNAME non configurato: notifica per avviso id={} non inviata", d.avvisoId());
            return;
        }
        // Token nel FRAGMENT (#): non viene inviato ai server né finisce nei loro access log.
        String link = frontendUrl + "/avvisi/disattiva#token=" + d.tokenDisattivazione();

        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(mittente);
            helper.setTo(d.email());
            helper.setSubject(senzaACapo("Prezzo in calo: " + d.marca() + " " + d.modello()));
            helper.setText(testoSemplice(d, link), html(d, link));
        } catch (MessagingException e) {
            throw new MailPreparationException("Impossibile preparare la mail", e);
        }
        mailSender.send(msg); // MailException (unchecked) se Gmail non risponde
    }

    /** Mail con il link monouso per reimpostare la password (valido 30 minuti). */
    public void inviaResetPassword(String email, String nome, String token) {
        if (mittente == null || mittente.isBlank()) {
            log.info("MAIL_USERNAME non configurato: mail di reset password non inviata");
            return;
        }
        String link = frontendUrl + "/reimposta-password#token=" + token;
        String html = """
                <!doctype html>
                <html lang="it"><body style="font-family:Arial,sans-serif;color:#111">
                  <h2>Ciao %s,</h2>
                  <p>abbiamo ricevuto una richiesta per reimpostare la password del tuo account Car Catalog.</p>
                  <p><a href="%s">Scegli una nuova password</a></p>
                  <p style="font-size:12px;color:#666">Il link vale 30 minuti e si può usare una sola volta.
                     Se non sei stato tu, ignora questa email: la tua password resta invariata.</p>
                </body></html>
                """.formatted(esc(nome), esc(link));
        String testo = "Ciao " + nome + ",\n\nper scegliere una nuova password apri questo link (valido 30 minuti):\n"
                + link + "\n\nSe non sei stato tu, ignora questa email.\n";
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(mittente);
            helper.setTo(email);
            helper.setSubject("Reimposta la tua password");
            helper.setText(testo, html);
        } catch (MessagingException e) {
            throw new MailPreparationException("Impossibile preparare la mail", e);
        }
        mailSender.send(msg);
    }

    /** Ogni valore dinamico passa da esc(): nessun HTML iniettabile tramite nome utente o dati auto. */
    static String html(DatiNotifica d, String link) {
        return """
                <!doctype html>
                <html lang="it"><body style="font-family:Arial,sans-serif;color:#111">
                  <h2>Ciao %s,</h2>
                  <p>il prezzo di <strong>%s %s</strong> è sceso a <strong>%s</strong>,
                     sotto la tua soglia di %s.</p>
                  <p><a href="%s">Disattiva questo avviso</a></p>
                  <p style="font-size:12px;color:#666">Ricevi questa email perché hai impostato un avviso di prezzo su Car Catalog.
                     Il link di disattivazione è personale e utilizzabile una sola volta.</p>
                </body></html>
                """.formatted(esc(d.nome()), esc(d.marca()), esc(d.modello()), esc(euro(d.nuovoPrezzo())),
                esc(euro(d.soglia())), esc(link));
    }

    static String testoSemplice(DatiNotifica d, String link) {
        return "Ciao " + d.nome() + ",\n\nil prezzo di " + d.marca() + " " + d.modello() + " è sceso a "
                + euro(d.nuovoPrezzo()) + ", sotto la tua soglia di " + euro(d.soglia()) + ".\n\n"
                + "Disattiva l'avviso: " + link + "\n";
    }

    static String esc(String s) {
        return s == null ? "" : HtmlUtils.htmlEscape(s, "UTF-8");
    }

    private static String senzaACapo(String s) {
        return s.replaceAll("[\\r\\n]", " ");
    }

    private static String euro(BigDecimal v) {
        return NumberFormat.getCurrencyInstance(Locale.ITALY).format(v);
    }
}
