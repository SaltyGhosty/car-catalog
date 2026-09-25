package it.carcatalog.exception;

/** → 409 */
public class ConflittoException extends RuntimeException {
    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}
