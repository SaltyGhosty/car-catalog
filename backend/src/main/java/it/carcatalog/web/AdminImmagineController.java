package it.carcatalog.web;

import it.carcatalog.dto.ImmagineSuggerita;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.service.WikimediaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Suggerisce una foto con licenza libera per marca + modello (solo ADMIN). */
@RestController
@RequestMapping("/api/admin/immagini")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImmagineController {

    private final WikimediaService wikimediaService;

    public AdminImmagineController(WikimediaService wikimediaService) {
        this.wikimediaService = wikimediaService;
    }

    @GetMapping("/suggerimento")
    public ImmagineSuggerita suggerimento(@RequestParam String marca, @RequestParam String modello) {
        if (marca.isBlank() || modello.isBlank() || marca.length() > 50 || modello.length() > 80) {
            throw new RichiestaNonValidaException("Indica marca e modello");
        }
        return wikimediaService.suggerisci(marca, modello)
                .orElseThrow(() -> new RisorsaNonTrovataException("Nessuna foto con licenza libera trovata per questo modello"));
    }
}
