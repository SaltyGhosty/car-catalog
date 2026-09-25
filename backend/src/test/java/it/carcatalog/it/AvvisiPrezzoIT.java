package it.carcatalog.it;

import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.service.AvvisoClaimService;
import it.carcatalog.service.PrezzoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Sistema di notifiche: attraversamento soglia, AFTER_COMMIT, flag atomico. */
class AvvisiPrezzoIT extends ApiTestBase {

    @Autowired PrezzoService prezzoService;
    @Autowired AvvisoClaimService claimService;
    @Autowired AvvisoRepository avvisoRepository;
    @Autowired TransactionTemplate tx;

    private String admin;
    private long auto;
    private long avviso;

    @BeforeEach
    void prepara() throws Exception {
        reset(emailService);
        admin = tokenAdmin();
        auto = creaAuto(admin, 21500);
        String utente = registra(emailUnica());
        avviso = chiama("POST", "/api/avvisi", "{\"autoId\":" + auto + ",\"soglia\":20000}", utente).id();
    }

    private int prezzo(int valore) throws Exception {
        return chiama("PATCH", "/api/admin/auto/" + auto + "/prezzo", "{\"prezzo\":" + valore + "}", admin).status();
    }

    @Test
    void unaSolaMailQuandoIlPrezzoAttraversaLaSoglia() throws Exception {
        assertEquals(200, prezzo(21500)); // stesso prezzo → nessun evento
        assertEquals(200, prezzo(20500)); // scende ma resta sopra → niente
        verify(emailService, after(800).never()).inviaAvvisoPrezzo(any());

        assertEquals(200, prezzo(19500)); // da sopra a sotto → UNA mail
        verify(emailService, timeout(3000).times(1)).inviaAvvisoPrezzo(any());

        prezzo(19000);                    // lo riabbassa → niente
        prezzo(22000);                    // risale…
        prezzo(18000);                    // …e riscende → niente (resta inviato)
        verify(emailService, after(1000).times(1)).inviaAvvisoPrezzo(any());
        assertTrue(avvisoRepository.findById(avviso).orElseThrow().isInviato());
    }

    @Test
    void soglieUgualeAlNuovoPrezzoScatta() throws Exception {
        assertEquals(200, prezzo(20000)); // "uguale o sotto"
        verify(emailService, timeout(3000).times(1)).inviaAvvisoPrezzo(any());
    }

    @Test
    void seLaTransazioneFallisceNonParteNessunaMail() throws Exception {
        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(s -> {
            prezzoService.cambiaPrezzo(auto, new BigDecimal("15000"));
            throw new IllegalStateException("errore dopo il cambio prezzo → rollback");
        }));
        verify(emailService, after(1000).never()).inviaAvvisoPrezzo(any());
        assertFalse(avvisoRepository.findById(avviso).orElseThrow().isInviato());
    }

    @Test
    void seGmailFallisceLAvvisoRestaInviato() throws Exception {
        doThrow(new RuntimeException("SMTP non raggiungibile")).when(emailService).inviaAvvisoPrezzo(any());
        assertEquals(200, prezzo(19000));
        verify(emailService, timeout(3000).times(1)).inviaAvvisoPrezzo(any());
        Thread.sleep(300);
        assertTrue(avvisoRepository.findById(avviso).orElseThrow().isInviato()); // nessun retry, nessun loop
    }

    @Test
    void ilClaimAtomicoRiesceUnaSolaVolta() throws Exception {
        assertTrue(claimService.reclama(avviso, new BigDecimal("19000")).isPresent());
        assertTrue(claimService.reclama(avviso, new BigDecimal("19000")).isEmpty());
    }

    @Test
    void cambiRavvicinatiInParalleloProduconoUnaSolaMail() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch via = new CountDownLatch(1);
        for (int p : new int[]{19900, 19500, 19000, 18500}) {
            pool.submit(() -> {
                via.await();
                return prezzo(p); // alcune possono ricevere 409 (optimistic lock): va bene
            });
        }
        via.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        verify(emailService, timeout(3000).times(1)).inviaAvvisoPrezzo(any());
        verify(emailService, after(1000).times(1)).inviaAvvisoPrezzo(any());
    }
}
