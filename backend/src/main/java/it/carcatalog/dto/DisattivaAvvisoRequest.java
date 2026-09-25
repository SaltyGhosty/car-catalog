package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Token base64url di 32 byte casuali (43 caratteri). */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record DisattivaAvvisoRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "Token non valido") String token
) {
    @Override
    public String toString() {
        return "DisattivaAvvisoRequest[***]";
    }
}
