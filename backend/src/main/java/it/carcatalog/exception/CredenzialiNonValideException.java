package it.carcatalog.exception;

/** → 401 con messaggio generico (non riveliamo se l'email esiste). */
public class CredenzialiNonValideException extends RuntimeException {
    public CredenzialiNonValideException() {
        super("Email o password non corretti");
    }

    public CredenzialiNonValideException(int tentativiRimasti) {
        super("Email o password non corretti. "
                + (tentativiRimasti == 1 ? "Ti resta 1 tentativo" : "Ti restano " + tentativiRimasti + " tentativi"));
    }
}
