package it.carcatalog.web;

import it.carcatalog.dto.AutoAdminResponse;
import it.carcatalog.dto.AutoRequest;
import it.carcatalog.dto.PaginaResponse;
import it.carcatalog.dto.PrezzoRequest;
import it.carcatalog.model.StatoAuto;
import it.carcatalog.service.AutoService;
import it.carcatalog.service.PrezzoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Doppia protezione: regola URL "/api/admin/**" → ROLE_ADMIN in SecurityConfig
 * + @PreAuthorize qui. Un utente USER che prova a cambiare il prezzo riceve 403.
 */
@RestController
@RequestMapping("/api/admin/auto")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAutoController {

    private final AutoService autoService;
    private final PrezzoService prezzoService;

    public AdminAutoController(AutoService autoService, PrezzoService prezzoService) {
        this.autoService = autoService;
        this.prezzoService = prezzoService;
    }

    @GetMapping
    public PaginaResponse<AutoAdminResponse> elenco(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatoAuto stato,
            @RequestParam(defaultValue = "recenti") String ordina,
            @RequestParam(required = false) String direzione,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int dimensione) {
        return autoService.cercaTutte(q, stato, ordina, direzione, pagina, dimensione);
    }

    @GetMapping("/{id}")
    public AutoAdminResponse dettaglio(@PathVariable Long id) {
        return autoService.dettaglioAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutoAdminResponse crea(@Valid @RequestBody AutoRequest req) {
        return autoService.crea(req);
    }

    @PutMapping("/{id}")
    public AutoAdminResponse aggiorna(@PathVariable Long id, @Valid @RequestBody AutoRequest req) {
        return autoService.aggiorna(id, req);
    }

    @PatchMapping("/{id}/prezzo")
    public AutoAdminResponse cambiaPrezzo(@PathVariable Long id, @Valid @RequestBody PrezzoRequest req) {
        return prezzoService.cambiaPrezzo(id, req.prezzo());
    }
}
