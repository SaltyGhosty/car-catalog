package it.carcatalog.service;

import it.carcatalog.dto.ImmagineSuggerita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * "API di auto" per le foto: cerca il modello su Wikipedia e restituisce la foto principale
 * da Wikimedia Commons con autore e licenza (necessari per le licenze CC BY-SA).
 *
 * Sicurezza: host FISSO (en.wikipedia.org), l'utente passa solo testo che RestClient codifica
 * come parametro → nessun SSRF. Endpoint usato solo dagli ADMIN. Timeout brevi.
 */
@Service
public class WikimediaService {

    private static final Logger log = LoggerFactory.getLogger(WikimediaService.class);
    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {};
    private static final Pattern TAG_HTML = Pattern.compile("<[^>]+>");
    private static final int LARGHEZZA = 960;

    private final RestClient client;

    public WikimediaService(@Value("${app.wikimedia.user-agent}") String userAgent) {
        var rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofSeconds(3));
        rf.setReadTimeout(Duration.ofSeconds(6));
        // Wikimedia richiede uno User-Agent descrittivo con un contatto
        this.client = RestClient.builder()
                .baseUrl("https://en.wikipedia.org")
                .requestFactory(rf)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }

    public Optional<ImmagineSuggerita> suggerisci(String marca, String modello) {
        try {
            // 1) cerca l'articolo del modello ("Volkswagen Golf", "Fiat Panda"...)
            String query = (marca + " " + primeParole(modello, 2)).trim();
            Map<String, Object> ricerca = client.get()
                    .uri(u -> u.path("/w/rest.php/v1/search/page").queryParam("q", query).queryParam("limit", 5).build())
                    .retrieve().body(JSON);

            for (Map<String, Object> pagina : lista(ricerca, "pages")) {
                Map<String, Object> thumb = mappa(pagina, "thumbnail");
                String thumbUrl = thumb == null ? null : (String) thumb.get("url");
                String file = nomeFile(thumbUrl);
                if (file == null) continue;

                // 2) dati del file su Commons: URL ridimensionato, autore, licenza
                Optional<ImmagineSuggerita> foto = datiFile(file, (String) pagina.get("title"));
                if (foto.isPresent()) return foto;
            }
            return Optional.empty();
        } catch (RestClientException | ClassCastException e) {
            log.warn("Ricerca immagine Wikimedia fallita ({})", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private Optional<ImmagineSuggerita> datiFile(String file, String articolo) {
        Map<String, Object> risposta = client.get()
                .uri(u -> u.path("/w/api.php")
                        .queryParam("action", "query").queryParam("format", "json")
                        .queryParam("prop", "imageinfo").queryParam("iiprop", "url|extmetadata")
                        .queryParam("iiurlwidth", LARGHEZZA).queryParam("titles", "File:" + file).build())
                .retrieve().body(JSON);

        Map<String, Object> pages = mappa(mappa(risposta, "query"), "pages");
        if (pages == null) return Optional.empty();
        for (Object p : pages.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> info = (List<Map<String, Object>>) ((Map<String, Object>) p).get("imageinfo");
            if (info == null || info.isEmpty()) continue;
            Map<String, Object> ii = info.getFirst();
            Map<String, Object> meta = mappa(ii, "extmetadata");
            String licenza = valore(meta, "LicenseShortName");
            String autore = TAG_HTML.matcher(valore(meta, "Artist")).replaceAll("").replace("©", "").trim();
            String url = String.valueOf(ii.get("thumburl")).replace("://thumb.wikimedia.org/", "://upload.wikimedia.org/");
            String fonte = String.valueOf(ii.get("descriptionurl"));
            int q = url.indexOf('?');
            if (q > 0) url = url.substring(0, q);

            // Solo licenze libere e solo dagli host attesi (stessa whitelist di AutoRequest)
            if (!licenzaLibera(licenza) || !url.startsWith("https://upload.wikimedia.org/wikipedia/commons/")
                    || !fonte.startsWith("https://commons.wikimedia.org/wiki/")) {
                continue;
            }
            if (autore.length() > 80) autore = autore.substring(0, 80);
            String credito = "Foto: " + (autore.isBlank() ? "autore sconosciuto" : autore) + " — " + licenza + ", via Wikimedia Commons";
            return Optional.of(new ImmagineSuggerita(url, credito, fonte, articolo));
        }
        return Optional.empty();
    }

    static boolean licenzaLibera(String licenza) {
        String l = licenza.toLowerCase();
        return l.startsWith("cc by") || l.startsWith("cc0") || l.contains("public domain") || l.startsWith("gfdl");
    }

    /** ".../commons/thumb/a/ab/Nome_file.jpg/60px-Nome_file.jpg" → "Nome_file.jpg" */
    static String nomeFile(String thumbUrl) {
        if (thumbUrl == null || !thumbUrl.contains("/commons/thumb/")) return null;
        String[] parti = thumbUrl.split("\\?")[0].split("/");
        return parti.length < 2 ? null : URLDecoder.decode(parti[parti.length - 2], StandardCharsets.UTF_8);
    }

    private static String primeParole(String s, int n) {
        String[] parole = s.trim().split("\\s+");
        return String.join(" ", java.util.Arrays.copyOf(parole, Math.min(n, parole.length)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mappa(Map<String, Object> m, String chiave) {
        return m == null ? null : (Map<String, Object>) m.get(chiave);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Map<String, Object> m, String chiave) {
        Object v = m == null ? null : m.get(chiave);
        return v == null ? List.of() : (List<Map<String, Object>>) v;
    }

    private static String valore(Map<String, Object> meta, String chiave) {
        Map<String, Object> campo = mappa(meta, chiave);
        return campo == null || campo.get("value") == null ? "" : String.valueOf(campo.get("value"));
    }
}
