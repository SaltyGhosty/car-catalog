package it.carcatalog.dto;

import java.math.BigDecimal;

/**
 * Filtri di ricerca del catalogo. Tutti opzionali; vengono tradotti in predicati
 * Criteria API con parametri bindati (vedi AutoService#filtro).
 */
public record FiltroAuto(
        String q,
        String marca,
        BigDecimal prezzoMin,
        BigDecimal prezzoMax,
        Integer annoMin,
        Integer kmMax
) {
    public static FiltroAuto vuoto() {
        return new FiltroAuto(null, null, null, null, null, null);
    }
}
