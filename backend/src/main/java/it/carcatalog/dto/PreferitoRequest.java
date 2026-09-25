package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record PreferitoRequest(@NotNull @Positive Long autoId) {
}
