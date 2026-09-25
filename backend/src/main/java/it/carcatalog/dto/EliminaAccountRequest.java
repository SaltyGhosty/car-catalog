package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Conferma con password per evitare cancellazioni con un token rubato o da CSRF. */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record EliminaAccountRequest(@NotBlank @Size(max = 72) String password) {
    @Override
    public String toString() {
        return "EliminaAccountRequest[***]";
    }
}
