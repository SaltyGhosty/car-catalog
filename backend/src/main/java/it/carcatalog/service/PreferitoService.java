package it.carcatalog.service;

import it.carcatalog.dto.PreferitoResponse;
import it.carcatalog.exception.ConflittoException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.model.Auto;
import it.carcatalog.model.Preferito;
import it.carcatalog.model.StatoAuto;
import it.carcatalog.repository.AutoRepository;
import it.carcatalog.repository.PreferitoRepository;
import it.carcatalog.repository.UtenteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreferitoService {

    private final PreferitoRepository preferitoRepository;
    private final AutoRepository autoRepository;
    private final UtenteRepository utenteRepository;

    public PreferitoService(PreferitoRepository preferitoRepository, AutoRepository autoRepository,
                            UtenteRepository utenteRepository) {
        this.preferitoRepository = preferitoRepository;
        this.autoRepository = autoRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public List<PreferitoResponse> lista(Long utenteId) {
        return preferitoRepository.findByUtenteIdOrderByCreatedAtDesc(utenteId).stream()
                .filter(p -> p.getAuto().isPubblicata()) // un'auto tornata in bozza non si vede più
                .map(PreferitoResponse::from)
                .toList();
    }

    /** Idempotente: se è già nei preferiti restituisce quello esistente. */
    @Transactional
    public PreferitoResponse aggiungi(Long utenteId, Long autoId) {
        Auto auto = autoRepository.findByIdAndStato(autoId, StatoAuto.PUBBLICATA)
                .orElseThrow(() -> new RisorsaNonTrovataException("Auto non trovata"));
        var esistente = preferitoRepository.findByUtenteIdAndAutoId(utenteId, autoId);
        if (esistente.isPresent()) {
            return PreferitoResponse.from(esistente.get());
        }
        try {
            Preferito p = preferitoRepository.saveAndFlush(new Preferito(utenteRepository.getReferenceById(utenteId), auto));
            return PreferitoResponse.from(p);
        } catch (DataIntegrityViolationException e) {
            throw new ConflittoException("Auto già nei preferiti");
        }
    }

    /** Cerca per (id, proprietario): il preferito di un altro utente → 404. */
    @Transactional
    public void rimuovi(Long utenteId, Long preferitoId) {
        Preferito p = preferitoRepository.findByIdAndUtenteId(preferitoId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Preferito non trovato"));
        preferitoRepository.delete(p);
    }
}
