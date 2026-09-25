package it.carcatalog.repository;

import it.carcatalog.model.Preferito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PreferitoRepository extends JpaRepository<Preferito, Long> {

    @EntityGraph(attributePaths = "auto")
    List<Preferito> findByUtenteIdOrderByCreatedAtDesc(Long utenteId);

    /** Anti-IDOR: si cerca SEMPRE per id + proprietario. */
    Optional<Preferito> findByIdAndUtenteId(Long id, Long utenteId);

    Optional<Preferito> findByUtenteIdAndAutoId(Long utenteId, Long autoId);

    boolean existsByUtenteIdAndAutoId(Long utenteId, Long autoId);

    /** Solo per ADMIN: tutti i preferiti con utente e auto in un'unica query. */
    @EntityGraph(attributePaths = {"utente", "auto"})
    List<Preferito> findAllByOrderByCreatedAtDesc();

    /** [utenteId, conteggio] */
    @Query("select p.utente.id, count(p) from Preferito p group by p.utente.id")
    List<Object[]> contaPerUtente();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Preferito p where p.utente.id = :utenteId")
    int deleteAllByUtenteId(@Param("utenteId") Long utenteId);
}
