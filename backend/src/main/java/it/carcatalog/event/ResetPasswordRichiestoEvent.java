package it.carcatalog.event;

/** Pubblicato quando un utente chiede di reimpostare la password. Il token in chiaro vive solo qui e nella mail. */
public record ResetPasswordRichiestoEvent(String email, String nome, String token) {
    @Override
    public String toString() {
        return "ResetPasswordRichiestoEvent[***]";
    }
}
