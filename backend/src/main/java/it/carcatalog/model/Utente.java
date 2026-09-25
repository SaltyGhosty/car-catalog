package it.carcatalog.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Ruolo ruolo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Utente() {
    }

    public Utente(String email, String nome, String passwordHash, Ruolo ruolo) {
        this.email = email;
        this.nome = nome;
        this.passwordHash = passwordHash;
        this.ruolo = ruolo;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Ruolo getRuolo() { return ruolo; }
    public Instant getCreatedAt() { return createdAt; }

    /** Mai stampare email o hash della password nei log. */
    @Override
    public String toString() {
        return "Utente{id=" + id + ", ruolo=" + ruolo + "}";
    }
}
