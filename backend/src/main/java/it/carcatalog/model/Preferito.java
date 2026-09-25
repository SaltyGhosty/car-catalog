package it.carcatalog.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "preferiti",
        uniqueConstraints = @UniqueConstraint(name = "uk_preferito_utente_auto", columnNames = {"utente_id", "auto_id"}))
public class Preferito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id", nullable = false)
    private Auto auto;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Preferito() {
    }

    public Preferito(Utente utente, Auto auto) {
        this.utente = utente;
        this.auto = auto;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Utente getUtente() { return utente; }
    public Auto getAuto() { return auto; }
    public Instant getCreatedAt() { return createdAt; }
}
