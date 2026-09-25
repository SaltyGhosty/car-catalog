package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Solo il nome è modificabile: email, ruolo e id non sono nemmeno presenti nel DTO. */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record AggiornaProfiloRequest(
        @NotBlank
        @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]{1,59}$",
                message = "Il nome può contenere solo lettere, spazi, apostrofi, punti e trattini (2-60 caratteri)")
        String nome
) {
}
