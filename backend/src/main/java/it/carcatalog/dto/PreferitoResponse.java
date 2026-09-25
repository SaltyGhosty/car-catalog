package it.carcatalog.dto;

import it.carcatalog.model.Preferito;

import java.time.Instant;

public record PreferitoResponse(Long id, AutoPubblicaResponse auto, Instant createdAt) {
    public static PreferitoResponse from(Preferito p) {
        return new PreferitoResponse(p.getId(), AutoPubblicaResponse.from(p.getAuto()), p.getCreatedAt());
    }
}
