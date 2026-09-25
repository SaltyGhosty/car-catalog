package it.carcatalog.dto;

import java.math.BigDecimal;

/** Panoramica per l'amministratore. */
public record StatisticheResponse(
        long utenti,
        long amministratori,
        long autoPubblicate,
        long autoBozze,
        long preferiti,
        long avvisiAttivi,
        long avvisiInviati,
        BigDecimal valoreListino,
        BigDecimal valoreAcquisto
) {
}
