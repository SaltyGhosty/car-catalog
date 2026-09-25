package it.carcatalog.web;

import it.carcatalog.dto.AggiornaProfiloRequest;
import it.carcatalog.dto.EliminaAccountRequest;
import it.carcatalog.dto.UtenteResponse;
import it.carcatalog.security.UtenteAutenticato;
import it.carcatalog.service.UtenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** "/api/me": l'utente agisce SOLO su se stesso, l'id arriva dal token. */
@RestController
@RequestMapping("/api/me")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping
    public UtenteResponse profilo(@AuthenticationPrincipal UtenteAutenticato me) {
        return utenteService.profilo(me.id());
    }

    @PutMapping
    public UtenteResponse aggiorna(@AuthenticationPrincipal UtenteAutenticato me,
                                   @Valid @RequestBody AggiornaProfiloRequest req) {
        return utenteService.aggiornaProfilo(me.id(), req);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@AuthenticationPrincipal UtenteAutenticato me,
                        @Valid @RequestBody EliminaAccountRequest req) {
        utenteService.eliminaAccount(me.id(), req.password());
    }
}
