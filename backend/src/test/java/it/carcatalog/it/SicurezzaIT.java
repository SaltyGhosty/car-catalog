package it.carcatalog.it;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Requisiti di sicurezza "da difendere", verificati via HTTP sull'app reale. */
class SicurezzaIT extends ApiTestBase {

    @Test
    void utenteNormaleCheCambiaIlPrezzoRiceve403() throws Exception {
        String admin = tokenAdmin();
        long auto = creaAuto(admin, 20000);
        String utente = registra(emailUnica());

        assertEquals(403, chiama("PATCH", "/api/admin/auto/" + auto + "/prezzo", "{\"prezzo\":1}", utente).status());
        assertEquals(401, chiama("PATCH", "/api/admin/auto/" + auto + "/prezzo", "{\"prezzo\":1}", null).status());
        assertEquals(200, chiama("PATCH", "/api/admin/auto/" + auto + "/prezzo", "{\"prezzo\":19000}", admin).status());
    }

    @Test
    void ilRuoloNelBodyDellaRegistrazioneVieneIgnorato() throws Exception {
        Risposta r = chiama("POST", "/api/auth/registrazione", """
                {"email":"%s","nome":"Furbo","password":"password-sicura-1","privacyAccettata":true,
                 "ruolo":"ADMIN","role":"ADMIN","id":1}""".formatted(emailUnica()), null);
        assertEquals(201, r.status());
        assertEquals("USER", r.campo("ruolo"));
        assertEquals(403, chiama("GET", "/api/admin/utenti", null, r.campo("token")).status());
    }

    @Test
    void avvisoDiUnAltroUtenteRisponde404ENonRivelaNulla() throws Exception {
        long auto = creaAuto(tokenAdmin(), 20000);
        String mario = registra(emailUnica());
        String luigi = registra(emailUnica());

        // Mario prova a manomettere il body con userId e inviato: vengono ignorati
        Risposta creato = chiama("POST", "/api/avvisi",
                "{\"autoId\":" + auto + ",\"soglia\":18000,\"userId\":999,\"utenteId\":999,\"inviato\":true}", mario);
        assertEquals(201, creato.status());
        assertEquals("false", creato.campo("inviato"));
        long idAvviso = creato.id();

        Risposta altrui = chiama("GET", "/api/avvisi/" + idAvviso, null, luigi);
        Risposta inesistente = chiama("GET", "/api/avvisi/987654", null, luigi);
        assertEquals(404, altrui.status());
        assertEquals(404, inesistente.status());
        assertEquals(inesistente.campo("messaggio"), altrui.campo("messaggio")); // stessa risposta: non si capisce che esiste

        assertEquals(404, chiama("DELETE", "/api/avvisi/" + idAvviso, null, luigi).status());
        assertEquals(404, chiama("PATCH", "/api/avvisi/" + idAvviso, "{\"soglia\":1000}", luigi).status());
        assertEquals(200, chiama("GET", "/api/avvisi/" + idAvviso, null, mario).status());
    }

    @Test
    void ordinamentoFuoriWhitelistRifiutato() throws Exception {
        assertEquals(400, chiama("GET", "/api/auto?ordina=prezzoAcquisto", null, null).status());
        assertEquals(400, chiama("GET", "/api/auto?ordina=prezzo%3Bdrop%20table%20auto", null, null).status());
        assertEquals(200, chiama("GET", "/api/auto?ordina=prezzo&direzione=desc", null, null).status());
    }

    @Test
    void catalogoPubblicoNonEsponePrezzoAcquistoNeBozze() throws Exception {
        String admin = tokenAdmin();
        Risposta bozza = chiama("POST", "/api/admin/auto", """
                {"marca":"Tesla","modello":"Bozza segreta","anno":2022,"chilometri":1,"descrizione":"x",
                 "prezzo":30000,"prezzoAcquisto":25000,"stato":"BOZZA"}""", admin);
        assertEquals(201, bozza.status());

        Risposta pubblico = chiama("GET", "/api/auto?dimensione=50", null, null);
        assertFalse(pubblico.body().contains("prezzoAcquisto"));
        assertFalse(pubblico.body().contains("Bozza segreta"));
        assertEquals(404, chiama("GET", "/api/auto/" + bozza.id(), null, null).status());
    }

    @Test
    void healthPubblicoMaGliAltriEndpointActuatorNo() throws Exception {
        assertEquals(200, chiama("GET", "/actuator/health", null, null).status());
        assertNotEquals(200, chiama("GET", "/actuator/env", null, null).status());
    }

    @Test
    void adminVedeUtentiAvvisiPreferitiEStatistiche() throws Exception {
        String admin = tokenAdmin();
        String email = emailUnica();
        String utente = registra(email);
        long auto = creaAuto(admin, 20000);
        chiama("POST", "/api/preferiti", "{\"autoId\":" + auto + "}", utente);

        Risposta utenti = chiama("GET", "/api/admin/utenti", null, admin);
        assertEquals(200, utenti.status());
        assertTrue(utenti.body().contains(email));
        assertFalse(utenti.body().contains("password"));
        assertEquals(200, chiama("GET", "/api/admin/avvisi", null, admin).status());
        assertTrue(chiama("GET", "/api/admin/preferiti", null, admin).body().contains(email));
        assertEquals(200, chiama("GET", "/api/admin/statistiche", null, admin).status());
    }
}
