package it.carcatalog.service;

import it.carcatalog.dto.AggiornaSogliaRequest;
import it.carcatalog.dto.AvvisoRequest;
import it.carcatalog.dto.AvvisoResponse;
import it.carcatalog.exception.ConflittoException;
import it.carcatalog.exception.RichiestaNonValidaException;
import it.carcatalog.exception.RisorsaNonTrovataException;
import it.carcatalog.model.Auto;
import it.carcatalog.model.Avviso;
import it.carcatalog.model.StatoAuto;
import it.carcatalog.repository.AutoRepository;
import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.repository.UtenteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tutte le operazioni ricevono l'id utente dal JWT (mai dal body) e cercano
 * SEMPRE per (id, proprietario): l'avviso di un altro utente risulta "non trovato" (404).
 */
@Service
public class AvvisoService {

    private static final String NON_TROVATO = "Avviso non trovato";

    private final AvvisoRepository avvisoRepository;
    private final AutoRepository autoRepository;
    private final UtenteRepository utenteRepository;

    public AvvisoService(AvvisoRepository avvisoRepository, AutoRepository autoRepository,
                         UtenteRepository utenteRepository) {
        this.avvisoRepository = avvisoRepository;
        this.autoRepository = autoRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public List<AvvisoResponse> lista(Long utenteId) {
        return avvisoRepository.findByUtenteIdOrderByCreatedAtDesc(utenteId).stream().map(AvvisoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AvvisoResponse dettaglio(Long utenteId, Long avvisoId) {
        return AvvisoResponse.from(caricaDelProprietario(utenteId, avvisoId));
    }

    @Transactional
    public AvvisoResponse crea(Long utenteId, AvvisoRequest req) {
        Auto auto = autoRepository.findByIdAndStato(req.autoId(), StatoAuto.PUBBLICATA)
                .orElseThrow(() -> new RisorsaNonTrovataException("Auto non trovata"));
        verificaSoglia(req.soglia(), auto);
        if (avvisoRepository.existsByUtenteIdAndAutoId(utenteId, auto.getId())) {
            throw new ConflittoException("Hai già un avviso per questa auto: modifica la soglia esistente");
        }
        // inviato parte sempre da false (default dell'entity), l'utente viene dal token
        Avviso avviso = new Avviso(utenteRepository.getReferenceById(utenteId), auto, req.soglia());
        try {
            return AvvisoResponse.from(avvisoRepository.saveAndFlush(avviso));
        } catch (DataIntegrityViolationException e) {
            throw new ConflittoException("Hai già un avviso per questa auto");
        }
    }

    @Transactional
    public AvvisoResponse aggiornaSoglia(Long utenteId, Long avvisoId, AggiornaSogliaRequest req) {
        Avviso avviso = caricaDelProprietario(utenteId, avvisoId);
        verificaSoglia(req.soglia(), avviso.getAuto());
        avviso.aggiornaSoglia(req.soglia());
        return AvvisoResponse.from(avviso);
    }

    @Transactional
    public void elimina(Long utenteId, Long avvisoId) {
        avvisoRepository.delete(caricaDelProprietario(utenteId, avvisoId));
    }

    /** Link nella mail: il token è monouso perché l'avviso viene eliminato. */
    @Transactional
    public void disattivaConToken(String token) {
        Avviso avviso = avvisoRepository.findByTokenDisattivazioneHash(TokenUtil.sha256Hex(token))
                .orElseThrow(() -> new RisorsaNonTrovataException("Link non valido o già utilizzato"));
        avvisoRepository.delete(avviso);
    }

    private Avviso caricaDelProprietario(Long utenteId, Long avvisoId) {
        return avvisoRepository.findByIdAndUtenteId(avvisoId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException(NON_TROVATO));
    }

    /** L'avviso scatta quando il prezzo passa da sopra a sotto: la soglia deve essere sotto il prezzo attuale. */
    private static void verificaSoglia(BigDecimal soglia, Auto auto) {
        if (soglia.compareTo(auto.getPrezzo()) >= 0) {
            throw new RichiestaNonValidaException("La soglia deve essere inferiore al prezzo attuale");
        }
    }
}
