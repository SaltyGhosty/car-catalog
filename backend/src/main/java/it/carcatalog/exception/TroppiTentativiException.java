package it.carcatalog.exception;

/** → 429 Too Many Requests, con header Retry-After. */
public class TroppiTentativiException extends RuntimeException {

    private final long secondi;

    public TroppiTentativiException(long secondi) {
        super("Troppi tentativi di accesso. Riprova tra " + secondi + " secondi");
        this.secondi = secondi;
    }

    public long getSecondi() {
        return secondi;
    }
}
