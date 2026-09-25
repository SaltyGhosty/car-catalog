package it.carcatalog.repository;

import java.math.BigDecimal;

/** Proiezione per min/max del catalogo pubblicato. */
public interface IntervalliAuto {
    BigDecimal getPrezzoMin();
    BigDecimal getPrezzoMax();
    Integer getAnnoMin();
    Integer getAnnoMax();
    Long getTotale();
}
