package it.carcatalog.web;

import it.carcatalog.dto.AutoPubblicaResponse;
import it.carcatalog.dto.FiltriResponse;
import it.carcatalog.dto.FiltroAuto;
import it.carcatalog.dto.PaginaResponse;
import it.carcatalog.service.AutoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/** Catalogo pubblico: solo auto PUBBLICATE, senza prezzo d'acquisto. */
@RestController
@RequestMapping("/api/auto")
public class AutoController {

    private final AutoService autoService;

    public AutoController(AutoService autoService) {
        this.autoService = autoService;
    }

    @GetMapping
    public PaginaResponse<AutoPubblicaResponse> cerca(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) BigDecimal prezzoMin,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @RequestParam(required = false) Integer annoMin,
            @RequestParam(required = false) Integer kmMax,
            @RequestParam(defaultValue = "recenti") String ordina,
            @RequestParam(required = false) String direzione,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int dimensione) {
        var filtro = new FiltroAuto(limita(q), limita(marca), prezzoMin, prezzoMax, annoMin, kmMax);
        return autoService.cercaPubblicate(filtro, ordina, direzione, pagina, dimensione);
    }

    /** Marche disponibili (con conteggio) e intervalli di prezzo/anno per i filtri. */
    @GetMapping("/filtri")
    public FiltriResponse filtri() {
        return autoService.filtriDisponibili();
    }

    @GetMapping("/{id}")
    public AutoPubblicaResponse dettaglio(@PathVariable Long id) {
        return autoService.dettaglioPubblico(id);
    }

    private static String limita(String s) {
        return s == null ? null : (s.length() > 60 ? s.substring(0, 60) : s);
    }
}
