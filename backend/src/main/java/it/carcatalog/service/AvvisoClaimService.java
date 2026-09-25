package it.carcatalog.service;

import it.carcatalog.model.Avviso;
import it.carcatalog.repository.AvvisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * "Prenota" l'invio di un avviso in modo atomico.
 * Bean separato dal listener perché @Transactional funziona solo tramite proxy (no self-invocation).
 */
@Service
public class AvvisoClaimService {

    private final AvvisoRepository avvisoRepository;

    public AvvisoClaimService(AvvisoRepository avvisoRepository) {
        this.avvisoRepository = avvisoRepository;
    }

    /**
     * UPDATE avvisi SET inviato = true WHERE id = ? AND inviato = false
     * Se due eventi ravvicinati arrivano insieme, solo UNO ottiene 1 riga aggiornata:
     * l'altro ottiene 0 e non invia nulla → niente mail doppie.
     * Il flag viene salvato PRIMA dell'invio: se poi Gmail fallisce la mail è persa, ma non ci sono loop/doppioni.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<DatiNotifica> reclama(Long avvisoId, BigDecimal nuovoPrezzo) {
        if (avvisoRepository.segnaComeInviato(avvisoId) != 1) {
            return Optional.empty();
        }
        String token = TokenUtil.nuovoToken();
        avvisoRepository.impostaTokenHash(avvisoId, TokenUtil.sha256Hex(token));
        Avviso a = avvisoRepository.findConUtenteEAuto(avvisoId).orElseThrow();
        return Optional.of(new DatiNotifica(a.getId(), a.getUtente().getEmail(), a.getUtente().getNome(),
                a.getAuto().getMarca(), a.getAuto().getModello(), nuovoPrezzo, a.getSoglia(), token));
    }
}
