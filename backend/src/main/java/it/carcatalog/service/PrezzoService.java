package it.carcatalog.service;

import it.carcatalog.dto.AutoAdminResponse;
import it.carcatalog.event.PrezzoCambiatoEvent;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.model.Auto;
import it.carcatalog.repository.AutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unico punto in cui cambia il prezzo di un'auto.
 * Pubblica PrezzoCambiatoEvent: il listener parte SOLO dopo il commit (vedi AvvisoPrezzoListener),
 * quindi se il salvataggio fallisce non viene inviato nulla.
 */
@Service
public class PrezzoService {

    private static final Logger log = LoggerFactory.getLogger(PrezzoService.class);

    private final AutoRepository autoRepository;
    private final ApplicationEventPublisher publisher;

    public PrezzoService(AutoRepository autoRepository, ApplicationEventPublisher publisher) {
        this.autoRepository = autoRepository;
        this.publisher = publisher;
    }

    @Transactional
    public AutoAdminResponse cambiaPrezzo(Long autoId, BigDecimal nuovoPrezzo) {
        Auto auto = autoRepository.findById(autoId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Auto non trovata"));
        applicaNuovoPrezzo(auto, nuovoPrezzo);
        return AutoAdminResponse.from(auto);
    }

    /**
     * Deve essere chiamato dentro una transazione esistente (MANDATORY), altrimenti l'evento
     * "after commit" non avrebbe una transazione a cui agganciarsi.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void applicaNuovoPrezzo(Auto auto, BigDecimal nuovoPrezzo) {
        BigDecimal nuovo = nuovoPrezzo.setScale(2, RoundingMode.HALF_UP);
        if (auto.getPrezzo().compareTo(nuovo) == 0) {
            return; // stesso prezzo salvato di nuovo: nessun evento, nessuna mail
        }
        BigDecimal vecchio = auto.cambiaPrezzo(nuovo);
        log.info("Prezzo auto id={} cambiato {} -> {}", auto.getId(), vecchio, nuovo);
        publisher.publishEvent(new PrezzoCambiatoEvent(auto.getId(), vecchio, nuovo, auto.isPubblicata()));
    }
}
