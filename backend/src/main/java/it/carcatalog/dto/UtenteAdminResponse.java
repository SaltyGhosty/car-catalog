package it.carcatalog.dto;

import java.time.Instant;

/** Riga della tabella utenti per l'amministratore (mai l'hash della password). */
public record UtenteAdminResponse(Long id, String email, String nome, String ruolo, Instant createdAt,
                                  long preferiti, long avvisi) {
}
