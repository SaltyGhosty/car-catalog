package it.carcatalog.dto;

import it.carcatalog.model.Avviso;

import java.math.BigDecimal;
import java.time.Instant;

public record AvvisoAdminResponse(Long id, Long utenteId, String utenteEmail, String utenteNome,
                                  Long autoId, String marca, String modello, BigDecimal prezzoAttuale,
                                  BigDecimal soglia, boolean inviato, Instant createdAt) {
    public static AvvisoAdminResponse from(Avviso a) {
        var u = a.getUtente();
        var auto = a.getAuto();
        return new AvvisoAdminResponse(a.getId(), u.getId(), u.getEmail(), u.getNome(), auto.getId(), auto.getMarca(),
                auto.getModello(), auto.getPrezzo(), a.getSoglia(), a.isInviato(), a.getCreatedAt());
    }
}
