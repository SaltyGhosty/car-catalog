package it.carcatalog.exception;

/** → 400 */
public class RichiestaNonValidaException extends RuntimeException {
    public RichiestaNonValidaException(String messaggio) {
        super(messaggio);
    }
}
