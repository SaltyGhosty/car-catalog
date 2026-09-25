package it.carcatalog.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "auto")
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modello;

    @Column(nullable = false)
    private int anno;

    @Column(nullable = false)
    private int chilometri;

    /** Testo puro: il frontend lo mostra come testo, mai come HTML. */
    @Column(nullable = false, length = 4000)
    private String descrizione;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzo;

    /** Visibile solo agli amministratori. */
    @Column(name = "prezzo_acquisto", nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzoAcquisto;

    /** Percorso locale (/auto/xxx.webp) o URL https://upload.wikimedia.org/... */
    @Column(name = "immagine_url", length = 500)
    private String immagineUrl;

    /** Attribuzione obbligatoria per le licenze CC BY-SA (autore + licenza). */
    @Column(name = "immagine_credito", length = 300)
    private String immagineCredito;

    /** Pagina del file su Wikimedia Commons (link di attribuzione). */
    @Column(name = "immagine_fonte", length = 500)
    private String immagineFonte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatoAuto stato = StatoAuto.BOZZA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: due modifiche concorrenti dell'admin non si sovrascrivono in silenzio. */
    @Version
    private long version;

    protected Auto() {
    }

    public Auto(String marca, String modello, int anno, int chilometri, String descrizione,
                BigDecimal prezzo, BigDecimal prezzoAcquisto, StatoAuto stato) {
        this.marca = marca;
        this.modello = modello;
        this.anno = anno;
        this.chilometri = chilometri;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.prezzoAcquisto = prezzoAcquisto;
        this.stato = stato;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isPubblicata() {
        return stato == StatoAuto.PUBBLICATA;
    }

    public Long getId() { return id; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModello() { return modello; }
    public void setModello(String modello) { this.modello = modello; }
    public int getAnno() { return anno; }
    public void setAnno(int anno) { this.anno = anno; }
    public int getChilometri() { return chilometri; }
    public void setChilometri(int chilometri) { this.chilometri = chilometri; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public BigDecimal getPrezzo() { return prezzo; }
    public BigDecimal getPrezzoAcquisto() { return prezzoAcquisto; }
    public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) { this.prezzoAcquisto = prezzoAcquisto; }
    public String getImmagineUrl() { return immagineUrl; }
    public String getImmagineCredito() { return immagineCredito; }
    public String getImmagineFonte() { return immagineFonte; }

    public void impostaImmagine(String url, String credito, String fonte) {
        this.immagineUrl = url;
        this.immagineCredito = credito;
        this.immagineFonte = fonte;
    }

    public StatoAuto getStato() { return stato; }
    public void setStato(StatoAuto stato) { this.stato = stato; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    /**
     * Unico punto per cambiare prezzo (non esiste setPrezzo): viene chiamato solo da PrezzoService,
     * che pubblica l'evento per gli avvisi. Ritorna il prezzo precedente.
     */
    public BigDecimal cambiaPrezzo(BigDecimal nuovoPrezzo) {
        BigDecimal vecchio = this.prezzo;
        this.prezzo = nuovoPrezzo;
        return vecchio;
    }
}
