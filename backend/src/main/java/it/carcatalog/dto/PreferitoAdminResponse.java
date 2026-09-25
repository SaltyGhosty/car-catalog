package it.carcatalog.dto;

import it.carcatalog.model.Preferito;

import java.math.BigDecimal;
import java.time.Instant;

public record PreferitoAdminResponse(Long id, Long utenteId, String utenteEmail, Long autoId, String marca,
                                     String modello, BigDecimal prezzo, String statoAuto, Instant createdAt) {
    public static PreferitoAdminResponse from(Preferito p) {
        var u = p.getUtente();
        var a = p.getAuto();
        return new PreferitoAdminResponse(p.getId(), u.getId(), u.getEmail(), a.getId(), a.getMarca(), a.getModello(),
                a.getPrezzo(), a.getStato().name(), p.getCreatedAt());
    }
}
