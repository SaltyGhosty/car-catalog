package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

/**
 * DTO di registrazione. NON contiene "ruolo": eventuali campi extra nel JSON (ruolo, id, ...)
 * vengono ignorati da Jackson perché non esistono qui. Il ruolo lo decide il server.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record RegistrazioneRequest(
        @NotBlank @Email @Size(max = 254)
        String email,

        @NotBlank
        @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]{1,59}$",
                message = "Il nome può contenere solo lettere, spazi, apostrofi, punti e trattini (2-60 caratteri)")
        String nome,

        @NotBlank
        @Size(min = 10, max = 72, message = "La password deve avere tra 10 e 72 caratteri")
        String password,

        @AssertTrue(message = "Devi dichiarare di aver letto la Privacy Policy")
        boolean privacyAccettata
) {
    /** Evita che la password finisca nei log se qualcuno stampa il DTO. */
    @Override
    public String toString() {
        return "RegistrazioneRequest[***]";
    }
}
