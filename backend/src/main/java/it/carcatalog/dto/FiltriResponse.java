package it.carcatalog.dto;

import java.math.BigDecimal;
import java.util.List;

/** Valori disponibili per costruire i filtri della pagina "Auto usate". */
public record FiltriResponse(
        List<MarcaConteggio> marche,
        BigDecimal prezzoMin,
        BigDecimal prezzoMax,
        Integer annoMin,
        Integer annoMax,
        long totale
) {
}
