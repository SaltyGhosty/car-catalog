package it.carcatalog.event;

import java.math.BigDecimal;

/** Evento immutabile pubblicato dentro la transazione che cambia il prezzo. */
public record PrezzoCambiatoEvent(Long autoId, BigDecimal vecchioPrezzo, BigDecimal nuovoPrezzo, boolean pubblicata) {

    public boolean isRibasso() {
        return nuovoPrezzo.compareTo(vecchioPrezzo) < 0;
    }
}
