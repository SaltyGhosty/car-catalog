package it.carcatalog.dto;

import java.time.Instant;
import java.util.Map;

public record ErroreResponse(int status, String messaggio, Map<String, String> campi, Instant timestamp) {
    public static ErroreResponse of(int status, String messaggio) {
        return new ErroreResponse(status, messaggio, Map.of(), Instant.now());
    }
}
