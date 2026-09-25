package it.carcatalog.it;

import it.carcatalog.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base dei test di integrazione: avvia l'app VERA su una porta casuale (sicurezza, JPA, eventi, @Async)
 * e la chiama via HTTP come farebbe il frontend. L'unica cosa finta è l'invio della mail.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class ApiTestBase {

    /** Sostituisce l'invio reale: nei test contiamo quante mail "partirebbero". */
    @MockitoBean
    protected EmailService emailService;

    @Autowired
    private Environment env;

    private final HttpClient http = HttpClient.newHttpClient();

    protected record Risposta(int status, String body, HttpResponse<String> raw) {
        String campo(String nome) {
            Matcher m = Pattern.compile("\"" + nome + "\"\\s*:\\s*(\"([^\"]*)\"|[-0-9.]+|true|false|null)").matcher(body);
            if (!m.find()) return null;
            return m.group(2) != null ? m.group(2) : m.group(1);
        }

        long id() {
            return Long.parseLong(campo("id"));
        }
    }

    protected Risposta chiama(String metodo, String path, String json, String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + env.getProperty("local.server.port") + path))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Risposta(r.statusCode(), r.body(), r);
    }

    protected static String emailUnica() {
        return "utente-" + UUID.randomUUID().toString().substring(0, 8) + "@test.it";
    }

    /** Registra un utente e restituisce il token. */
    protected String registra(String email) throws Exception {
        Risposta r = chiama("POST", "/api/auth/registrazione", """
                {"email":"%s","nome":"Mario","password":"password-sicura-1","privacyAccettata":true}""".formatted(email), null);
        if (r.status() != 201) throw new AssertionError("Registrazione fallita: " + r.status() + " " + r.body());
        return r.campo("token");
    }

    protected String tokenAdmin() throws Exception {
        Risposta r = chiama("POST", "/api/auth/login",
                "{\"email\":\"admin@test.local\",\"password\":\"admin-test-password-123\"}", null);
        if (r.status() != 200) throw new AssertionError("Login admin fallito: " + r.status() + " " + r.body());
        return r.campo("token");
    }

    /** Crea un'auto pubblicata tramite l'API admin e restituisce l'id. */
    protected long creaAuto(String admin, int prezzo) throws Exception {
        Risposta r = chiama("POST", "/api/admin/auto", """
                {"marca":"Volkswagen","modello":"Golf 8","anno":2021,"chilometri":42000,
                 "descrizione":"Auto di test","prezzo":%d,"prezzoAcquisto":15000,"stato":"PUBBLICATA"}""".formatted(prezzo), admin);
        if (r.status() != 201) throw new AssertionError("Creazione auto fallita: " + r.status() + " " + r.body());
        return r.id();
    }
}
