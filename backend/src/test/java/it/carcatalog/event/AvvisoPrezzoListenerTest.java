package it.carcatalog.event;

import it.carcatalog.repository.AvvisoRepository;
import it.carcatalog.service.AvvisoClaimService;
import it.carcatalog.service.DatiNotifica;
import it.carcatalog.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class AvvisoPrezzoListenerTest {

    private AvvisoRepository repo;
    private AvvisoClaimService claim;
    private EmailService email;
    private AvvisoPrezzoListener listener;

    @BeforeEach
    void setUp() {
        repo = mock(AvvisoRepository.class);
        claim = mock(AvvisoClaimService.class);
        email = mock(EmailService.class);
        listener = new AvvisoPrezzoListener(repo, claim, email);
    }

    private static DatiNotifica dati(long id) {
        return new DatiNotifica(id, "a@b.it", "Mario", "Fiat", "Panda", new BigDecimal("9000"), new BigDecimal("9500"), "t");
    }

    @Test
    void rialzoNonFaNulla() {
        listener.onPrezzoCambiato(new PrezzoCambiatoEvent(1L, new BigDecimal("9000"), new BigDecimal("12000"), true));
        verifyNoInteractions(repo, claim, email);
    }

    @Test
    void autoInBozzaNonNotifica() {
        listener.onPrezzoCambiato(new PrezzoCambiatoEvent(1L, new BigDecimal("12000"), new BigDecimal("9000"), false));
        verifyNoInteractions(repo, claim, email);
    }

    @Test
    void inviaSoloSeIlClaimAtomicoRiesce() throws Exception {
        when(repo.findIdAttraversati(any(), any(), any())).thenReturn(List.of(10L, 11L));
        when(claim.reclama(eq(10L), any())).thenReturn(Optional.of(dati(10L)));
        when(claim.reclama(eq(11L), any())).thenReturn(Optional.empty()); // già inviato da un evento concorrente

        listener.onPrezzoCambiato(new PrezzoCambiatoEvent(1L, new BigDecimal("12000"), new BigDecimal("9000"), true));

        verify(email, times(1)).inviaAvvisoPrezzo(any());
    }

    @Test
    void erroreGmailNonPropagaENonRiprova() throws Exception {
        when(repo.findIdAttraversati(any(), any(), any())).thenReturn(List.of(10L));
        when(claim.reclama(eq(10L), any())).thenReturn(Optional.of(dati(10L)));
        doThrow(new RuntimeException("smtp down")).when(email).inviaAvvisoPrezzo(any());

        listener.onPrezzoCambiato(new PrezzoCambiatoEvent(1L, new BigDecimal("12000"), new BigDecimal("9000"), true));

        verify(claim, times(1)).reclama(eq(10L), any()); // nessun retry, il flag resta true
    }
}
