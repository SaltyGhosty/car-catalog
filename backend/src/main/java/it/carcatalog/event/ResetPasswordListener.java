package it.carcatalog.event;

import it.carcatalog.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Invia la mail di reset solo dopo il commit del token, in modo asincrono. */
@Component
public class ResetPasswordListener {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordListener.class);

    private final EmailService emailService;

    public ResetPasswordListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResetRichiesto(ResetPasswordRichiestoEvent evento) {
        try {
            emailService.inviaResetPassword(evento.email(), evento.nome(), evento.token());
        } catch (Exception e) {
            log.warn("Invio mail di reset password fallito ({})", e.getClass().getSimpleName());
        }
    }
}
