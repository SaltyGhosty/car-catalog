package it.carcatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.carcatalog.model.StatoAuto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Creazione / modifica auto (solo ADMIN). */
@JsonIgnoreProperties(ignoreUnknown = true) // campi extra (ruolo, inviato, userId...) ignorati
public record AutoRequest(
        @NotBlank @Size(max = 50) String marca,
        @NotBlank @Size(max = 80) String modello,
        @Min(1950) @Max(2100) int anno,
        @PositiveOrZero @Max(2_000_000) int chilometri,
        @NotBlank @Size(max = 4000) String descrizione,
        @NotNull @DecimalMin("1.00") @Digits(integer = 10, fraction = 2) BigDecimal prezzo,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal prezzoAcquisto,
        @NotNull StatoAuto stato,

        /** Solo immagini locali (/auto/*.webp) o da Wikimedia Commons: niente host arbitrari (tracking, contenuti non licenziati). */
        @Size(max = 500)
        @Pattern(regexp = "^(/auto/[a-z0-9-]+\\.webp|https://upload\\.wikimedia\\.org/wikipedia/commons/[^\\s\"'<>]+)$",
                message = "Immagine non valida: usa una foto da Wikimedia Commons")
        String immagineUrl,

        @Size(max = 300) String immagineCredito,

        @Size(max = 500)
        @Pattern(regexp = "^https://commons\\.wikimedia\\.org/wiki/[^\\s\"'<>]+$", message = "Fonte non valida")
        String immagineFonte
) {
    /** Se c'è una foto, l'attribuzione è obbligatoria (licenze CC BY-SA). */
    @AssertTrue(message = "Per usare una foto servono credito e pagina di origine")
    public boolean isAttribuzioneCompleta() {
        return immagineUrl == null || immagineUrl.isBlank() || immagineUrl.startsWith("/auto/")
                || (immagineCredito != null && !immagineCredito.isBlank() && immagineFonte != null && !immagineFonte.isBlank());
    }
}
