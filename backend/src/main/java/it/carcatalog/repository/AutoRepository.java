package it.carcatalog.repository;

import it.carcatalog.dto.MarcaConteggio;
import it.carcatalog.model.Auto;
import it.carcatalog.model.StatoAuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutoRepository extends JpaRepository<Auto, Long>, JpaSpecificationExecutor<Auto> {

    Optional<Auto> findByIdAndStato(Long id, StatoAuto stato);

    long countByStato(StatoAuto stato);

    @Query("select coalesce(sum(a.prezzo), 0) from Auto a where a.stato = :stato")
    java.math.BigDecimal sommaPrezzi(@Param("stato") StatoAuto stato);

    @Query("select coalesce(sum(a.prezzoAcquisto), 0) from Auto a where a.stato = :stato")
    java.math.BigDecimal sommaPrezziAcquisto(@Param("stato") StatoAuto stato);

    @Query("""
            select new it.carcatalog.dto.MarcaConteggio(a.marca, count(a))
            from Auto a where a.stato = :stato
            group by a.marca order by a.marca
            """)
    List<MarcaConteggio> contaPerMarca(@Param("stato") StatoAuto stato);

    @Query("""
            select min(a.prezzo) as prezzoMin, max(a.prezzo) as prezzoMax,
                   min(a.anno) as annoMin, max(a.anno) as annoMax, count(a) as totale
            from Auto a where a.stato = :stato
            """)
    IntervalliAuto intervalli(@Param("stato") StatoAuto stato);
}
