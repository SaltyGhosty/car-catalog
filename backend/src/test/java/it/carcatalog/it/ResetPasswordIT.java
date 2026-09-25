package it.carcatalog.it;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** "Password dimenticata": link monouso via mail, nessuna enumerazione degli account. */
class ResetPasswordIT extends ApiTestBase {

    private String richiediELeggiToken(String email) throws Exception {
        assertEquals(204, chiama("POST", "/api/auth/password-dimenticata", "{\"email\":\"" + email + "\"}", null).status());
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService, timeout(3000)).inviaResetPassword(eq(email), any(), token.capture());
        return token.getValue();
    }

    private int login(String email, String password) throws Exception {
        return chiama("POST", "/api/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", null).status();
    }

    @Test
    void flussoCompletoConLinkMonouso() throws Exception {
        reset(emailService);
        String email = emailUnica();
        registra(email);
        String token = richiediELeggiToken(email);

        String body = "{\"token\":\"" + token + "\",\"nuovaPassword\":\"nuova-password-sicura\"}";
        assertEquals(204, chiama("POST", "/api/auth/reimposta-password", body, null).status());
        assertEquals(200, login(email, "nuova-password-sicura"));
        assertEquals(401, login(email, "password-sicura-1")); // la vecchia non vale più

        Risposta riuso = chiama("POST", "/api/auth/reimposta-password", body, null);
        assertEquals(400, riuso.status()); // monouso
    }

    @Test
    void emailInesistenteStessaRispostaENessunaMail() throws Exception {
        reset(emailService);
        assertEquals(204, chiama("POST", "/api/auth/password-dimenticata", "{\"email\":\"nessuno@test.it\"}", null).status());
        verify(emailService, after(800).never()).inviaResetPassword(any(), any(), any());
    }

    @Test
    void tokenInventatoRifiutato() throws Exception {
        String body = "{\"token\":\"" + "A".repeat(43) + "\",\"nuovaPassword\":\"nuova-password-sicura\"}";
        assertEquals(400, chiama("POST", "/api/auth/reimposta-password", body, null).status());
    }

    @Test
    void richiesteRavvicinateProduconoUnaSolaMail() throws Exception {
        reset(emailService);
        String email = emailUnica();
        registra(email);
        richiediELeggiToken(email);
        chiama("POST", "/api/auth/password-dimenticata", "{\"email\":\"" + email + "\"}", null);
        verify(emailService, after(800).times(1)).inviaResetPassword(eq(email), any(), any());
    }
}
