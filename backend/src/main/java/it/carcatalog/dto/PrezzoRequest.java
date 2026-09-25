package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record PrezzoRequest(
        @NotNull @DecimalMin("1.00") @Digits(integer = 10, fraction = 2) BigDecimal prezzo
) {
}
