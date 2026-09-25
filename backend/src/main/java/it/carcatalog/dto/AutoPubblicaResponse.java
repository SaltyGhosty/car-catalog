package it.carcatalog.dto;

import it.carcatalog.model.Auto;

import java.math.BigDecimal;

/** Vista pubblica: NON espone prezzo d'acquisto né stato. */
public record AutoPubblicaResponse(
        Long id,
        String marca,
        String modello,
        int anno,
        int chilometri,
        String descrizione,
        BigDecimal prezzo,
        String immagineUrl,
        String immagineCredito,
        String immagineFonte
) {
    public static AutoPubblicaResponse from(Auto a) {
        return new AutoPubblicaResponse(a.getId(), a.getMarca(), a.getModello(), a.getAnno(),
                a.getChilometri(), a.getDescrizione(), a.getPrezzo(), a.getImmagineUrl(), a.getImmagineCredito(),
                a.getImmagineFonte());
    }
}
