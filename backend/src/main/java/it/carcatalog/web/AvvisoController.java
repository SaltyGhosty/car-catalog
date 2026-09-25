package it.carcatalog.web;

import it.carcatalog.dto.AggiornaSogliaRequest;
import it.carcatalog.dto.AvvisoRequest;
import it.carcatalog.dto.AvvisoResponse;
import it.carcatalog.dto.DisattivaAvvisoRequest;
import it.carcatalog.security.UtenteAutenticato;
import it.carcatalog.service.AvvisoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Anti-IDOR: /api/avvisi/13 di un altro utente → 404, identico a un id inesistente.
 * L'utente arriva sempre da @AuthenticationPrincipal, mai da path o body.
 */
@RestController
@RequestMapping("/api/avvisi")
public class AvvisoController {

    private final AvvisoService avvisoService;

    public AvvisoController(AvvisoService avvisoService) {
        this.avvisoService = avvisoService;
    }

    @GetMapping
    public List<AvvisoResponse> lista(@AuthenticationPrincipal UtenteAutenticato me) {
        return avvisoService.lista(me.id());
    }

    @GetMapping("/{id}")
    public AvvisoResponse dettaglio(@AuthenticationPrincipal UtenteAutenticato me, @PathVariable Long id) {
        return avvisoService.dettaglio(me.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvvisoResponse crea(@AuthenticationPrincipal UtenteAutenticato me, @Valid @RequestBody AvvisoRequest req) {
        return avvisoService.crea(me.id(), req);
    }

    @PatchMapping("/{id}")
    public AvvisoResponse aggiornaSoglia(@AuthenticationPrincipal UtenteAutenticato me, @PathVariable Long id,
                                         @Valid @RequestBody AggiornaSogliaRequest req) {
        return avvisoService.aggiornaSoglia(me.id(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@AuthenticationPrincipal UtenteAutenticato me, @PathVariable Long id) {
        avvisoService.elimina(me.id(), id);
    }

    /** Pubblico: il token monouso del link in email identifica l'avviso (POST, così i link-scanner non lo "consumano"). */
    @PostMapping("/disattiva")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disattiva(@Valid @RequestBody DisattivaAvvisoRequest req) {
        avvisoService.disattivaConToken(req.token());
    }
}
