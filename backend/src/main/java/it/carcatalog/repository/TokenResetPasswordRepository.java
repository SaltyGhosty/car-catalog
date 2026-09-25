package it.carcatalog.repository;

import it.carcatalog.model.TokenResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface TokenResetPasswordRepository extends JpaRepository<TokenResetPassword, Long> {

    @Query("select t from TokenResetPassword t join fetch t.utente where t.tokenHash = :hash")
    Optional<TokenResetPassword> findByTokenHash(@Param("hash") String hash);

    boolean existsByUtenteIdAndCreatedAtAfter(Long utenteId, Instant dopo);

    /** Consumo atomico: solo una richiesta può usare il token (stesso schema del flag "inviato"). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TokenResetPassword t set t.usato = true where t.id = :id and t.usato = false and t.scadenza > :ora")
    int consuma(@Param("id") Long id, @Param("ora") Instant ora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TokenResetPassword t where t.utente.id = :utenteId")
    int deleteAllByUtenteId(@Param("utenteId") Long utenteId);
}
