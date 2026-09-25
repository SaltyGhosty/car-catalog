package it.carcatalog.security;

import it.carcatalog.model.Ruolo;

/** Principal minimale messo nel SecurityContext: niente email, niente password. */
public record UtenteAutenticato(Long id, Ruolo ruolo) {
    public boolean isAdmin() {
        return ruolo == Ruolo.ADMIN;
    }
}
