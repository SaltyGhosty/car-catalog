package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * NON contiene userId né inviato: l'utente è preso dal JWT, inviato parte sempre da false.
 * Campi aggiunti a mano nel JSON vengono ignorati.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record AvvisoRequest(
        @NotNull @Positive Long autoId,
        @NotNull @DecimalMin("1.00") @Digits(integer = 10, fraction = 2) BigDecimal soglia
) {
}
