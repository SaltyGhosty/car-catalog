package it.carcatalog.repository;

import it.carcatalog.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRuolo(it.carcatalog.model.Ruolo ruolo);

    java.util.List<Utente> findAllByOrderByCreatedAtDesc();
}
