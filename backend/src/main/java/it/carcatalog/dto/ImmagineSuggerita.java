package it.carcatalog.dto;

/** Foto trovata su Wikimedia Commons, con i dati di attribuzione richiesti dalla licenza. */
public record ImmagineSuggerita(String url, String credito, String fonte, String articolo) {
}
