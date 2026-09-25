package it.carcatalog.web;

import it.carcatalog.dto.PreferitoRequest;
import it.carcatalog.dto.PreferitoResponse;
import it.carcatalog.security.UtenteAutenticato;
import it.carcatalog.service.PreferitoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferiti")
public class PreferitoController {

    private final PreferitoService preferitoService;

    public PreferitoController(PreferitoService preferitoService) {
        this.preferitoService = preferitoService;
    }

    @GetMapping
    public List<PreferitoResponse> lista(@AuthenticationPrincipal UtenteAutenticato me) {
        return preferitoService.lista(me.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreferitoResponse aggiungi(@AuthenticationPrincipal UtenteAutenticato me,
                                      @Valid @RequestBody PreferitoRequest req) {
        return preferitoService.aggiungi(me.id(), req.autoId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@AuthenticationPrincipal UtenteAutenticato me, @PathVariable Long id) {
        preferitoService.rimuovi(me.id(), id);
    }
}
