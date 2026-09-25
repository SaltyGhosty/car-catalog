package it.carcatalog.it;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Login: 3 tentativi, poi blocco (nel profilo test il cooldown è 2 secondi invece di 60). */
class LoginLimiteIT extends ApiTestBase {

    private Risposta login(String email, String password) throws Exception {
        return chiama("POST", "/api/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", null);
    }

    @Test
    void treTentativiSbagliatiPoiBloccoAncheConPasswordGiusta() throws Exception {
        String email = emailUnica();
        registra(email);

        assertEquals(401, login(email, "sbagliata-1").status());
        assertEquals(401, login(email, "sbagliata-2").status());
        Risposta terzo = login(email, "sbagliata-3");
        assertEquals(429, terzo.status());
        assertEquals("2", terzo.raw().headers().firstValue("Retry-After").orElseThrow());

        assertEquals(429, login(email, "password-sicura-1").status()); // bloccato anche se giusta

        Thread.sleep(2_200);
        assertEquals(200, login(email, "password-sicura-1").status()); // dopo il cooldown si riparte
    }

    @Test
    void messaggioConTentativiRimastiUgualePerEmailInesistenti() throws Exception {
        Risposta esistente = login(registraERestituisci(), "sbagliata");
        Risposta inesistente = login(emailUnica(), "sbagliata");
        assertEquals(401, esistente.status());
        assertEquals(401, inesistente.status());
        assertEquals(esistente.campo("messaggio"), inesistente.campo("messaggio")); // nessun indizio sull'esistenza
    }

    private String registraERestituisci() throws Exception {
        String email = emailUnica();
        registra(email);
        return email;
    }
}
