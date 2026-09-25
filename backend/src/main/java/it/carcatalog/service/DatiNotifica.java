package it.carcatalog.service;

import java.math.BigDecimal;

/** Snapshot dei dati per la mail, caricati nella transazione di "claim". */
public record DatiNotifica(Long avvisoId, String email, String nome, String marca, String modello,
                           BigDecimal nuovoPrezzo, BigDecimal soglia, String tokenDisattivazione) {
    @Override
    public String toString() {
        return "DatiNotifica[avvisoId=" + avvisoId + "]"; // niente email/token nei log
    }
}
