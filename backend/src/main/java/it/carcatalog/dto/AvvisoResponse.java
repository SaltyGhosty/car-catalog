package it.carcatalog.dto;

import it.carcatalog.model.Avviso;

import java.math.BigDecimal;
import java.time.Instant;

public record AvvisoResponse(
        Long id,
        Long autoId,
        String marca,
        String modello,
        BigDecimal prezzoAttuale,
        BigDecimal soglia,
        boolean inviato,
        Instant createdAt
) {
    public static AvvisoResponse from(Avviso a) {
        var auto = a.getAuto();
        return new AvvisoResponse(a.getId(), auto.getId(), auto.getMarca(), auto.getModello(),
                auto.getPrezzo(), a.getSoglia(), a.isInviato(), a.getCreatedAt());
    }
}
