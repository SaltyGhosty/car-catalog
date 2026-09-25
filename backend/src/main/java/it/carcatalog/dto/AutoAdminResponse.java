package it.carcatalog.dto;

import it.carcatalog.model.Auto;

import java.math.BigDecimal;
import java.time.Instant;

/** Vista amministratore: include bozze, prezzo d'acquisto e versione (optimistic locking). */
public record AutoAdminResponse(
        Long id,
        String marca,
        String modello,
        int anno,
        int chilometri,
        String descrizione,
        BigDecimal prezzo,
        BigDecimal prezzoAcquisto,
        String immagineUrl,
        String immagineCredito,
        String immagineFonte,
        String stato,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static AutoAdminResponse from(Auto a) {
        return new AutoAdminResponse(a.getId(), a.getMarca(), a.getModello(), a.getAnno(), a.getChilometri(),
                a.getDescrizione(), a.getPrezzo(), a.getPrezzoAcquisto(), a.getImmagineUrl(), a.getImmagineCredito(),
                a.getImmagineFonte(), a.getStato().name(),
                a.getCreatedAt(), a.getUpdatedAt(), a.getVersion());
    }
}
