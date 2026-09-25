package it.carcatalog.repository;

import it.carcatalog.model.Avviso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AvvisoRepository extends JpaRepository<Avviso, Long> {

    @EntityGraph(attributePaths = "auto")
    List<Avviso> findByUtenteIdOrderByCreatedAtDesc(Long utenteId);

    /** Anti-IDOR: si cerca SEMPRE per id + proprietario → 404 se l'avviso è di un altro. */
    @EntityGraph(attributePaths = "auto")
    Optional<Avviso> findByIdAndUtenteId(Long id, Long utenteId);

    boolean existsByUtenteIdAndAutoId(Long utenteId, Long autoId);

    long countByInviato(boolean inviato);

    /** Solo per ADMIN: tutti gli avvisi con utente e auto in un'unica query. */
    @EntityGraph(attributePaths = {"utente", "auto"})
    List<Avviso> findAllByOrderByCreatedAtDesc();

    /** [utenteId, conteggio] */
    @Query("select a.utente.id, count(a) from Avviso a group by a.utente.id")
    List<Object[]> contaPerUtente();

    /**
     * Avvisi "attraversati" dal cambio di prezzo: il vecchio prezzo era SOPRA la soglia
     * e il nuovo è UGUALE o SOTTO. Se il prezzo non scende sotto la soglia, o era già sotto,
     * non viene restituito nulla.
     */
    @Query("""
            select a.id from Avviso a
            where a.auto.id = :autoId
              and a.inviato = false
              and a.soglia < :vecchioPrezzo
              and a.soglia >= :nuovoPrezzo
            """)
    List<Long> findIdAttraversati(@Param("autoId") Long autoId,
                                  @Param("vecchioPrezzo") BigDecimal vecchioPrezzo,
                                  @Param("nuovoPrezzo") BigDecimal nuovoPrezzo);

    /**
     * Update atomico "compare-and-set": solo UNA transazione può portare inviato da false a true.
     * Si invia la mail solo se il valore di ritorno è esattamente 1.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Avviso a set a.inviato = true where a.id = :id and a.inviato = false")
    int segnaComeInviato(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Avviso a set a.tokenDisattivazioneHash = :hash where a.id = :id")
    int impostaTokenHash(@Param("id") Long id, @Param("hash") String hash);

    /** Carica i dati necessari alla mail in un'unica query (utente + auto). */
    @Query("select a from Avviso a join fetch a.utente join fetch a.auto where a.id = :id")
    Optional<Avviso> findConUtenteEAuto(@Param("id") Long id);

    Optional<Avviso> findByTokenDisattivazioneHash(String hash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Avviso a where a.utente.id = :utenteId")
    int deleteAllByUtenteId(@Param("utenteId") Long utenteId);
}
