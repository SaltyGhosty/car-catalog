package it.carcatalog.service;

import it.carcatalog.dto.AuthResponse;
import it.carcatalog.dto.LoginRequest;
import it.carcatalog.dto.RegistrazioneRequest;
import it.carcatalog.dto.UtenteResponse;
import it.carcatalog.exception.ConflittoException;
import it.carcatalog.exception.CredenzialiNonValideException;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.exception.TroppiTentativiException;
import it.carcatalog.model.Ruolo;
import it.carcatalog.model.Utente;
import it.carcatalog.repository.UtenteRepository;
import it.carcatalog.security.JwtService;
import it.carcatalog.security.LoginRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;
    /** Hash fittizio: se l'email non esiste confrontiamo comunque, così i tempi di risposta non rivelano nulla. */
    private final String hashFittizio;

    public AuthService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       LoginRateLimiter rateLimiter) {
        this.utenteRepository = utenteRepository;
        this.rateLimiter = rateLimiter;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFittizio = passwordEncoder.encode("password-fittizia-per-timing");
    }

    public static String normalizzaEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponse registra(RegistrazioneRequest req) {
        // BCrypt considera solo i primi 72 byte: rifiutiamo password più lunghe (anche con caratteri multibyte)
        if (req.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RichiestaNonValidaException("La password è troppo lunga");
        }
        String email = normalizzaEmail(req.email());
        if (utenteRepository.existsByEmail(email)) {
            throw new ConflittoException("Impossibile completare la registrazione con questa email");
        }
        // Il ruolo è deciso QUI, dal server. Nessun campo della richiesta può cambiarlo.
        Utente utente = new Utente(email, req.nome().trim(), passwordEncoder.encode(req.password()), Ruolo.USER);
        try {
            utente = utenteRepository.saveAndFlush(utente);
        } catch (DataIntegrityViolationException e) {
            throw new ConflittoException("Impossibile completare la registrazione con questa email");
        }
        log.info("Nuovo utente registrato id={}", utente.getId());
        return new AuthResponse(jwtService.genera(utente), UtenteResponse.from(utente));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = normalizzaEmail(req.email());
        rateLimiter.inizioTentativo(email); // 429 se l'account è in cooldown

        Optional<Utente> utente = utenteRepository.findByEmail(email);
        String hash = utente.map(Utente::getPasswordHash).orElse(hashFittizio);
        boolean ok = passwordEncoder.matches(req.password(), hash);
        if (utente.isEmpty() || !ok) {
            int rimasti = rateLimiter.tentativoFallito(email);
            log.info("Tentativo di login fallito (tentativi rimasti: {})", rimasti); // niente email nel log
            if (rimasti == 0) {
                throw new TroppiTentativiException(rateLimiter.secondiCooldown());
            }
            throw new CredenzialiNonValideException(rimasti);
        }
        rateLimiter.tentativoRiuscito(email);
        return new AuthResponse(jwtService.genera(utente.get()), UtenteResponse.from(utente.get()));
    }
}
