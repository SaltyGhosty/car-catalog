package it.carcatalog.dto;

import it.carcatalog.model.Utente;

public record UtenteResponse(Long id, String email, String nome, String ruolo) {
    public static UtenteResponse from(Utente u) {
        return new UtenteResponse(u.getId(), u.getEmail(), u.getNome(), u.getRuolo().name());
    }
}
