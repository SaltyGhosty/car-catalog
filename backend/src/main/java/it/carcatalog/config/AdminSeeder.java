package it.carcatalog.config;

import it.carcatalog.model.Ruolo;
import it.carcatalog.model.Utente;
import it.carcatalog.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Crea l'amministratore al primo avvio, con password presa SOLO da ADMIN_PASSWORD.
 * È l'unico modo per ottenere il ruolo ADMIN: la registrazione pubblica crea sempre USER.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email}") String adminEmail,
                       @Value("${app.admin.password:}") String adminPassword) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPassword == null || adminPassword.length() < 12) {
            log.warn("ADMIN_PASSWORD assente o più corta di 12 caratteri: amministratore non creato");
            return;
        }
        if (utenteRepository.existsByEmail(adminEmail)) {
            return;
        }
        utenteRepository.save(new Utente(adminEmail, "Amministratore", passwordEncoder.encode(adminPassword), Ruolo.ADMIN));
        log.info("Account amministratore creato"); // niente email/password nei log
    }
}
