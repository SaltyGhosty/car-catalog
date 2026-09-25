package it.carcatalog.web;

import it.carcatalog.dto.ErroreResponse;
import it.carcatalog.exception.ConflittoException;
import it.carcatalog.exception.CredenzialiNonValideException;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.exception.TroppiTentativiException;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Risposte d'errore uniformi, senza stack trace o dettagli interni verso il client. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroreResponse> validazione(MethodArgumentNotValidException ex) {
        Map<String, String> campi = new LinkedHashMap<>();
        // Non rimandiamo indietro il valore rifiutato (potrebbe essere una password)
        ex.getBindingResult().getFieldErrors().forEach(fe -> campi.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErroreResponse(400, "Dati non validi", campi, Instant.now()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            RichiestaNonValidaException.class})
    ResponseEntity<ErroreResponse> badRequest(Exception ex) {
        String msg = ex instanceof RichiestaNonValidaException ? ex.getMessage() : "Richiesta non valida";
        return risposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(RisorsaNonTrovataException.class)
    ResponseEntity<ErroreResponse> nonTrovato(RisorsaNonTrovataException ex) {
        return risposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErroreResponse> nessunaRisorsa() {
        return risposta(HttpStatus.NOT_FOUND, "Risorsa non trovata");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErroreResponse> metodo() {
        return risposta(HttpStatus.METHOD_NOT_ALLOWED, "Metodo non consentito");
    }

    @ExceptionHandler(CredenzialiNonValideException.class)
    ResponseEntity<ErroreResponse> credenziali(CredenzialiNonValideException ex) {
        return risposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TroppiTentativiException.class)
    ResponseEntity<ErroreResponse> troppiTentativi(TroppiTentativiException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getSecondi()))
                .body(ErroreResponse.of(429, ex.getMessage()));
    }

    @ExceptionHandler(ConflittoException.class)
    ResponseEntity<ErroreResponse> conflitto(ConflittoException ex) {
        return risposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ErroreResponse> concorrenza() {
        return risposta(HttpStatus.CONFLICT, "L'auto è stata modificata da un'altra richiesta: ricarica e riprova");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroreResponse> integrita() {
        return risposta(HttpStatus.CONFLICT, "Operazione in conflitto con dati esistenti");
    }

    /** Necessario: senza questo, un @PreAuthorize negato finirebbe nell'handler generico come 500. */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErroreResponse> accessoNegato() {
        return risposta(HttpStatus.FORBIDDEN, "Operazione non consentita");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErroreResponse> nonAutenticato() {
        return risposta(HttpStatus.UNAUTHORIZED, "Autenticazione richiesta");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroreResponse> generico(Exception ex) {
        log.error("Errore non gestito: {}", ex.getClass().getName(), ex);
        return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno");
    }

    private static ResponseEntity<ErroreResponse> risposta(HttpStatus status, String messaggio) {
        return ResponseEntity.status(status).body(ErroreResponse.of(status.value(), messaggio));
    }
}
