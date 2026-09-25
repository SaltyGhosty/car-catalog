package it.carcatalog.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Richiesta di reimpostazione password. Si salva solo l'hash SHA-256 del token inviato via mail:
 * chi legge il database non può usarlo. Monouso e con scadenza.
 */
@Entity
@Table(name = "token_reset_password")
public class TokenResetPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant scadenza;

    @Column(nullable = false)
    private boolean usato = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TokenResetPassword() {
    }

    public TokenResetPassword(Utente utente, String tokenHash, Instant scadenza) {
        this.utente = utente;
        this.tokenHash = tokenHash;
        this.scadenza = scadenza;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Utente getUtente() { return utente; }
    public Instant getScadenza() { return scadenza; }
    public boolean isUsato() { return usato; }
    public Instant getCreatedAt() { return createdAt; }
}
