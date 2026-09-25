package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReimpostaPasswordRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "Link non valido") String token,
        @NotBlank @Size(min = 10, max = 72, message = "La password deve avere tra 10 e 72 caratteri") String nuovaPassword
) {
    @Override
    public String toString() {
        return "ReimpostaPasswordRequest[***]";
    }
}
