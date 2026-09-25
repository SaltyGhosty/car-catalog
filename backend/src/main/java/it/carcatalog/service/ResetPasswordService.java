package it.carcatalog.service;

import it.carcatalog.event.ResetPasswordRichiestoEvent;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.model.TokenResetPassword;
import it.carcatalog.repository.TokenResetPasswordRepository;
import it.carcatalog.repository.UtenteRepository;
import it.carcatalog.security.LoginRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * "Password dimenticata":
 * - la risposta è SEMPRE la stessa, esista o no l'email (niente enumerazione degli account);
 * - token casuale da 256 bit, in DB solo l'hash, valido 30 minuti, monouso (update atomico);
 * - al massimo una richiesta al minuto per account;
 * - dopo il reset il limitatore di login dell'account viene azzerato.
 */
@Service
public class ResetPasswordService {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);
    static final Duration VALIDITA = Duration.ofMinutes(30);
    private static final Duration INTERVALLO_MINIMO = Duration.ofMinutes(1);
    private static final String LINK_NON_VALIDO = "Il link non è valido o è scaduto. Richiedine uno nuovo.";

    private final UtenteRepository utenteRepository;
    private final TokenResetPasswordRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;
    private final LoginRateLimiter rateLimiter;

    public ResetPasswordService(UtenteRepository utenteRepository, TokenResetPasswordRepository tokenRepository,
                                PasswordEncoder passwordEncoder, ApplicationEventPublisher publisher,
                                LoginRateLimiter rateLimiter) {
        this.utenteRepository = utenteRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public void richiedi(String emailGrezza) {
        String email = AuthService.normalizzaEmail(emailGrezza);
        utenteRepository.findByEmail(email).ifPresent(utente -> {
            Instant ora = Instant.now();
            if (tokenRepository.existsByUtenteIdAndCreatedAtAfter(utente.getId(), ora.minus(INTERVALLO_MINIMO))) {
                return; // richieste troppo ravvicinate: ignorate in silenzio
            }
            String token = TokenUtil.nuovoToken();
            tokenRepository.save(new TokenResetPassword(utente, TokenUtil.sha256Hex(token), ora.plus(VALIDITA)));
            publisher.publishEvent(new ResetPasswordRichiestoEvent(utente.getEmail(), utente.getNome(), token));
            log.info("Richiesta reset password per utente id={}", utente.getId());
        });
    }

    @Transactional
    public void reimposta(String token, String nuovaPassword) {
        if (nuovaPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RichiestaNonValidaException("La password è troppo lunga");
        }
        TokenResetPassword t = tokenRepository.findByTokenHash(TokenUtil.sha256Hex(token))
                .orElseThrow(() -> new RichiestaNonValidaException(LINK_NON_VALIDO));
        if (tokenRepository.consuma(t.getId(), Instant.now()) != 1) {
            throw new RichiestaNonValidaException(LINK_NON_VALIDO); // già usato o scaduto
        }
        var utente = utenteRepository.findById(t.getUtente().getId())
                .orElseThrow(() -> new RichiestaNonValidaException(LINK_NON_VALIDO));
        utente.setPasswordHash(passwordEncoder.encode(nuovaPassword));
        rateLimiter.tentativoRiuscito(utente.getEmail());
        log.info("Password reimpostata per utente id={}", utente.getId());
    }
}
