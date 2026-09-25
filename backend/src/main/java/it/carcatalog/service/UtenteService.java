package it.carcatalog.service;

import it.carcatalog.dto.AggiornaProfiloRequest;
import it.carcatalog.dto.UtenteResponse;
import it.carcatalog.exception.CredenzialiNonValideException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.model.Utente;
import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.repository.PreferitoRepository;
import it.carcatalog.repository.TokenResetPasswordRepository;
import it.carcatalog.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtenteService {

    private static final Logger log = LoggerFactory.getLogger(UtenteService.class);

    private final UtenteRepository utenteRepository;
    private final AvvisoRepository avvisoRepository;
    private final PreferitoRepository preferitoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenResetPasswordRepository tokenResetRepository;

    public UtenteService(UtenteRepository utenteRepository, AvvisoRepository avvisoRepository,
                         PreferitoRepository preferitoRepository, PasswordEncoder passwordEncoder,
                         TokenResetPasswordRepository tokenResetRepository) {
        this.tokenResetRepository = tokenResetRepository;
        this.utenteRepository = utenteRepository;
        this.avvisoRepository = avvisoRepository;
        this.preferitoRepository = preferitoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UtenteResponse profilo(Long utenteId) {
        return UtenteResponse.from(carica(utenteId));
    }

    @Transactional
    public UtenteResponse aggiornaProfilo(Long utenteId, AggiornaProfiloRequest req) {
        Utente u = carica(utenteId);
        u.setNome(req.nome().trim());
        return UtenteResponse.from(u);
    }

    /**
     * Cancellazione account (diritto all'oblio): avvisi, preferiti e utente in un'unica transazione.
     */
    @Transactional
    public void eliminaAccount(Long utenteId, String password) {
        Utente u = carica(utenteId);
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new CredenzialiNonValideException();
        }
        int avvisi = avvisoRepository.deleteAllByUtenteId(utenteId);
        int preferiti = preferitoRepository.deleteAllByUtenteId(utenteId);
        tokenResetRepository.deleteAllByUtenteId(utenteId);
        utenteRepository.deleteById(utenteId);
        log.info("Account id={} eliminato (avvisi={}, preferiti={})", utenteId, avvisi, preferiti);
    }

    private Utente carica(Long id) {
        return utenteRepository.findById(id).orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));
    }
}
