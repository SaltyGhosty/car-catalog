package it.carcatalog.exception;

/** → 404. Usata anche quando la risorsa esiste ma appartiene a un altro utente (anti-IDOR). */
public class RisorsaNonTrovataException extends RuntimeException {
    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
