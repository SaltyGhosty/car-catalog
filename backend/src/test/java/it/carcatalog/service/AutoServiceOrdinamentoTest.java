package it.carcatalog.service;

import it.carcatalog.exception.RichiestaNonValidaException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class AutoServiceOrdinamentoTest {

    @Test
    void campoAmmessoVieneMappato() {
        var p = AutoService.paginazione("prezzo", "desc", 0, 12);
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("prezzo").getDirection());
    }

    @Test
    void campoNonAmmessoVieneRifiutato() {
        assertThrows(RichiestaNonValidaException.class,
                () -> AutoService.paginazione("prezzo_acquisto", "asc", 0, 12));
        assertThrows(RichiestaNonValidaException.class,
                () -> AutoService.paginazione("prezzo; drop table auto", "asc", 0, 12));
    }

    @Test
    void direzioneNonAmmessaVieneRifiutata() {
        assertThrows(RichiestaNonValidaException.class, () -> AutoService.paginazione("anno", "sideways", 0, 12));
    }

    @Test
    void dimensionePaginaLimitata() {
        assertEquals(50, AutoService.paginazione("anno", "asc", 0, 10_000).getPageSize());
    }
}
