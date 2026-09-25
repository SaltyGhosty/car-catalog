package it.carcatalog.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Avviso di prezzo: lega un utente a un'auto con una soglia.
 * Il flag "inviato" NON è mai modificabile dal client: lo cambia solo la query atomica
 * in AvvisoRepository#segnaComeInviato.
 */
@Entity
@Table(name = "avvisi",
        uniqueConstraints = @UniqueConstraint(name = "uk_avviso_utente_auto", columnNames = {"utente_id", "auto_id"}))
public class Avviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id", nullable = false)
    private Auto auto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal soglia;

    @Column(nullable = false)
    private boolean inviato = false;

    /** SHA-256 (hex) del token monouso inserito nel link della mail. Il token in chiaro non viene salvato. */
    @Column(name = "token_disattivazione_hash", length = 64, unique = true)
    private String tokenDisattivazioneHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Avviso() {
    }

    public Avviso(Utente utente, Auto auto, BigDecimal soglia) {
        this.utente = utente;
        this.auto = auto;
        this.soglia = soglia;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Utente getUtente() { return utente; }
    public Auto getAuto() { return auto; }
    public BigDecimal getSoglia() { return soglia; }
    public boolean isInviato() { return inviato; }
    public Instant getCreatedAt() { return createdAt; }

    /**
     * Cambiare soglia "riarma" l'avviso: l'utente sta chiedendo esplicitamente una nuova notifica.
     */
    public void aggiornaSoglia(BigDecimal nuovaSoglia) {
        this.soglia = nuovaSoglia;
        this.inviato = false;
    }
}
