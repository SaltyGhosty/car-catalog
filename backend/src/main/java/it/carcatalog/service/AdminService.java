package it.carcatalog.service;

import it.carcatalog.dto.AvvisoAdminResponse;
import it.carcatalog.dto.PreferitoAdminResponse;
import it.carcatalog.dto.StatisticheResponse;
import it.carcatalog.dto.UtenteAdminResponse;
import it.carcatalog.model.Ruolo;
import it.carcatalog.model.StatoAuto;
import it.carcatalog.repository.AutoRepository;
import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.repository.PreferitoRepository;
import it.carcatalog.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Vista completa per l'amministratore: utenti, preferiti, avvisi e statistiche. Sola lettura. */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UtenteRepository utenteRepository;
    private final AutoRepository autoRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoRepository avvisoRepository;

    public AdminService(UtenteRepository utenteRepository, AutoRepository autoRepository,
                        PreferitoRepository preferitoRepository, AvvisoRepository avvisoRepository) {
        this.utenteRepository = utenteRepository;
        this.autoRepository = autoRepository;
        this.preferitoRepository = preferitoRepository;
        this.avvisoRepository = avvisoRepository;
    }

    public StatisticheResponse statistiche() {
        long admin = utenteRepository.countByRuolo(Ruolo.ADMIN);
        return new StatisticheResponse(
                utenteRepository.count() - admin, admin,
                autoRepository.countByStato(StatoAuto.PUBBLICATA), autoRepository.countByStato(StatoAuto.BOZZA),
                preferitoRepository.count(),
                avvisoRepository.countByInviato(false), avvisoRepository.countByInviato(true),
                autoRepository.sommaPrezzi(StatoAuto.PUBBLICATA), autoRepository.sommaPrezziAcquisto(StatoAuto.PUBBLICATA));
    }

    public List<UtenteAdminResponse> utenti() {
        Map<Long, Long> preferiti = conteggi(preferitoRepository.contaPerUtente());
        Map<Long, Long> avvisi = conteggi(avvisoRepository.contaPerUtente());
        return utenteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(u -> new UtenteAdminResponse(u.getId(), u.getEmail(), u.getNome(), u.getRuolo().name(), u.getCreatedAt(),
                        preferiti.getOrDefault(u.getId(), 0L), avvisi.getOrDefault(u.getId(), 0L)))
                .toList();
    }

    public List<AvvisoAdminResponse> avvisi() {
        return avvisoRepository.findAllByOrderByCreatedAtDesc().stream().map(AvvisoAdminResponse::from).toList();
    }

    public List<PreferitoAdminResponse> preferiti() {
        return preferitoRepository.findAllByOrderByCreatedAtDesc().stream().map(PreferitoAdminResponse::from).toList();
    }

    private static Map<Long, Long> conteggi(List<Object[]> righe) {
        Map<Long, Long> m = new HashMap<>();
        for (Object[] r : righe) {
            m.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
        }
        return m;
    }
}
