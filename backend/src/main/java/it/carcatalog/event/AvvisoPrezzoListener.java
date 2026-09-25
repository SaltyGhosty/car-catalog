package it.carcatalog.event;

import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.service.AvvisoClaimService;
import it.carcatalog.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class AvvisoPrezzoListener {

    private static final Logger log = LoggerFactory.getLogger(AvvisoPrezzoListener.class);

    private final AvvisoRepository avvisoRepository;
    private final AvvisoClaimService claimService;
    private final EmailService emailService;

    public AvvisoPrezzoListener(AvvisoRepository avvisoRepository, AvvisoClaimService claimService,
                                EmailService emailService) {
        this.avvisoRepository = avvisoRepository;
        this.claimService = claimService;
        this.emailService = emailService;
    }

    /**
     * AFTER_COMMIT: parte solo se la transazione del cambio prezzo è andata a buon fine.
     * @Async: gira su un thread del pool "mailExecutor", la risposta HTTP all'admin non aspetta Gmail.
     */
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPrezzoCambiato(PrezzoCambiatoEvent evento) {
        if (!evento.pubblicata() || !evento.isRibasso()) {
            return; // rialzo o auto in bozza: nessuna soglia può essere "attraversata" verso il basso
        }
        List<Long> ids = avvisoRepository.findIdAttraversati(evento.autoId(), evento.vecchioPrezzo(), evento.nuovoPrezzo());
        for (Long id : ids) {
            try {
                claimService.reclama(id, evento.nuovoPrezzo()).ifPresent(dati -> {
                    try {
                        emailService.inviaAvvisoPrezzo(dati);
                        log.info("Notifica prezzo inviata per avviso id={}", id);
                    } catch (Exception e) {
                        // L'avviso resta inviato=true: la mail è considerata persa (niente retry → niente doppioni).
                        // Logghiamo solo la classe dell'eccezione: il messaggio SMTP può contenere l'indirizzo email.
                        log.warn("Invio notifica fallito per avviso id={} ({})", id, e.getClass().getSimpleName());
                    }
                });
            } catch (RuntimeException e) {
                // es. avviso eliminato nel frattempo: si passa al successivo senza bloccare gli altri
                log.warn("Impossibile elaborare avviso id={} ({})", id, e.getClass().getSimpleName());
            }
        }
    }
}
