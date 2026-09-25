package it.carcatalog.service;

import it.carcatalog.dto.AutoAdminResponse;
import it.carcatalog.dto.AutoPubblicaResponse;
import it.carcatalog.dto.AutoRequest;
import it.carcatalog.dto.FiltriResponse;
import it.carcatalog.dto.FiltroAuto;
import it.carcatalog.dto.PaginaResponse;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.model.Auto;
import it.carcatalog.model.StatoAuto;
import it.carcatalog.repository.AutoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AutoService {

    /**
     * WHITELIST dei campi ordinabili: chiave che arriva dal client → proprietà JPA.
     * Il valore del client NON finisce mai nella query: se non è in questa mappa → 400.
     */
    static final Map<String, String> ORDINAMENTI_AMMESSI = Map.of(
            "prezzo", "prezzo",
            "anno", "anno",
            "chilometri", "chilometri",
            "marca", "marca",
            "recenti", "createdAt"
    );
    private static final int MAX_DIMENSIONE_PAGINA = 50;

    private final AutoRepository autoRepository;
    private final PrezzoService prezzoService;

    public AutoService(AutoRepository autoRepository, PrezzoService prezzoService) {
        this.autoRepository = autoRepository;
        this.prezzoService = prezzoService;
    }

    // ---------- Catalogo pubblico ----------

    @Transactional(readOnly = true)
    public PaginaResponse<AutoPubblicaResponse> cercaPubblicate(FiltroAuto f, String ordina,
                                                                  String direzione, int pagina, int dimensione) {
        var spec = filtro(f, StatoAuto.PUBBLICATA);
        var page = autoRepository.findAll(spec, paginazione(ordina, direzione, pagina, dimensione));
        return PaginaResponse.from(page, AutoPubblicaResponse::from);
    }

    /** Marche con conteggio e intervalli di prezzo/anno per costruire i filtri lato client. */
    @Transactional(readOnly = true)
    public FiltriResponse filtriDisponibili() {
        var i = autoRepository.intervalli(StatoAuto.PUBBLICATA);
        return new FiltriResponse(autoRepository.contaPerMarca(StatoAuto.PUBBLICATA),
                i.getPrezzoMin(), i.getPrezzoMax(), i.getAnnoMin(), i.getAnnoMax(),
                i.getTotale() == null ? 0 : i.getTotale());
    }

    @Transactional(readOnly = true)
    public AutoPubblicaResponse dettaglioPubblico(Long id) {
        return autoRepository.findByIdAndStato(id, StatoAuto.PUBBLICATA)
                .map(AutoPubblicaResponse::from)
                .orElseThrow(() -> new RisorsaNonTrovataException("Auto non trovata"));
    }

    // ---------- Amministrazione ----------

    @Transactional(readOnly = true)
    public PaginaResponse<AutoAdminResponse> cercaTutte(String q, StatoAuto stato, String ordina,
                                                          String direzione, int pagina, int dimensione) {
        var f = new FiltroAuto(q, null, null, null, null, null);
        var page = autoRepository.findAll(filtro(f, stato), paginazione(ordina, direzione, pagina, dimensione));
        return PaginaResponse.from(page, AutoAdminResponse::from);
    }

    @Transactional(readOnly = true)
    public AutoAdminResponse dettaglioAdmin(Long id) {
        return AutoAdminResponse.from(carica(id));
    }

    @Transactional
    public AutoAdminResponse crea(AutoRequest r) {
        Auto auto = new Auto(r.marca().trim(), r.modello().trim(), r.anno(), r.chilometri(), r.descrizione().trim(),
                r.prezzo(), r.prezzoAcquisto(), r.stato());
        auto.impostaImmagine(vuotoANull(r.immagineUrl()), vuotoANull(r.immagineCredito()), vuotoANull(r.immagineFonte()));
        return AutoAdminResponse.from(autoRepository.save(auto));
    }

    @Transactional
    public AutoAdminResponse aggiorna(Long id, AutoRequest r) {
        Auto auto = carica(id);
        auto.setMarca(r.marca().trim());
        auto.setModello(r.modello().trim());
        auto.setAnno(r.anno());
        auto.setChilometri(r.chilometri());
        auto.setDescrizione(r.descrizione().trim());
        auto.setPrezzoAcquisto(r.prezzoAcquisto());
        auto.setStato(r.stato());
        auto.impostaImmagine(vuotoANull(r.immagineUrl()), vuotoANull(r.immagineCredito()), vuotoANull(r.immagineFonte()));
        // Anche la modifica completa passa dalla stessa logica di cambio prezzo (evento + avvisi)
        prezzoService.applicaNuovoPrezzo(auto, r.prezzo());
        return AutoAdminResponse.from(auto);
    }

    // ---------- Helper ----------

    private Auto carica(Long id) {
        return autoRepository.findById(id).orElseThrow(() -> new RisorsaNonTrovataException("Auto non trovata"));
    }

    static Pageable paginazione(String ordina, String direzione, int pagina, int dimensione) {
        String chiave = ordina == null || ordina.isBlank() ? "recenti" : ordina.trim().toLowerCase(Locale.ROOT);
        String proprieta = ORDINAMENTI_AMMESSI.get(chiave);
        if (proprieta == null) {
            throw new RichiestaNonValidaException("Ordinamento non ammesso. Valori validi: " + ORDINAMENTI_AMMESSI.keySet());
        }
        Sort.Direction dir;
        if (direzione == null || direzione.isBlank()) {
            dir = "createdAt".equals(proprieta) ? Sort.Direction.DESC : Sort.Direction.ASC;
        } else if (direzione.equalsIgnoreCase("asc")) {
            dir = Sort.Direction.ASC;
        } else if (direzione.equalsIgnoreCase("desc")) {
            dir = Sort.Direction.DESC;
        } else {
            throw new RichiestaNonValidaException("Direzione non ammessa: usa asc o desc");
        }
        int size = Math.min(Math.max(dimensione, 1), MAX_DIMENSIONE_PAGINA);
        // "id" come secondo criterio rende la paginazione stabile
        return PageRequest.of(Math.max(pagina, 0), size, Sort.by(dir, proprieta).and(Sort.by(Sort.Direction.ASC, "id")));
    }

    /** Filtro con Criteria API: tutti i valori sono parametri bindati, nessuna concatenazione di stringhe SQL. */
    private static Specification<Auto> filtro(FiltroAuto f, StatoAuto stato) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (stato != null) {
                p.add(cb.equal(root.get("stato"), stato));
            }
            if (f.q() != null && !f.q().isBlank()) {
                String like = "%" + escapeLike(f.q().trim().toLowerCase(Locale.ROOT)) + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("marca")), like, '\\'),
                        cb.like(cb.lower(root.get("modello")), like, '\\'),
                        cb.like(cb.lower(cb.concat(cb.concat(root.<String>get("marca"), " "), root.<String>get("modello"))), like, '\\')));
            }
            if (f.marca() != null && !f.marca().isBlank()) {
                p.add(cb.equal(cb.lower(root.get("marca")), f.marca().trim().toLowerCase(Locale.ROOT)));
            }
            if (f.prezzoMin() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("prezzo"), f.prezzoMin()));
            }
            if (f.prezzoMax() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("prezzo"), f.prezzoMax()));
            }
            if (f.annoMin() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("anno"), f.annoMin()));
            }
            if (f.kmMax() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("chilometri"), f.kmMax()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static String vuotoANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String escapeLike(String s) {
        String limitata = s.length() > 60 ? s.substring(0, 60) : s;
        return limitata.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
