package it.carcatalog.security;

import it.carcatalog.exception.TroppiTentativiException;
import it.carcatalog.service.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite di tentativi di login per account: dopo {@code max} password sbagliate
 * l'email è bloccata per {@code cooldown} (default 3 tentativi, 1 minuto).
 *
 * - Il tentativo viene "prenotato" PRIMA di verificare la password, in modo atomico:
 *   anche con richieste parallele non si superano i 3 tentativi.
 * - Si conta anche per email inesistenti, così la risposta non rivela quali account esistono.
 * - La chiave è l'hash SHA-256 dell'email: in memoria non restano indirizzi in chiaro.
 * - Stato in memoria: va bene con una sola istanza (Render free). Con più istanze servirebbe Redis o il DB.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_VOCI = 10_000;

    private record Stato(int tentativi, Instant bloccatoFino, Instant ultimo) {
    }

    private final int max;
    private final Duration cooldown;
    private final Clock clock;
    private final ConcurrentHashMap<String, Stato> stati = new ConcurrentHashMap<>();

    @Autowired // due costruttori: questo è quello usato da Spring, l'altro dai test con orologio finto
    public LoginRateLimiter(@Value("${app.login.max-tentativi:3}") int max,
                            @Value("${app.login.cooldown:PT1M}") Duration cooldown) {
        this(max, cooldown, Clock.systemUTC());
    }

    LoginRateLimiter(int max, Duration cooldown, Clock clock) {
        this.max = max;
        this.cooldown = cooldown;
        this.clock = clock;
    }

    /** Da chiamare prima di controllare la password. Lancia 429 se l'account è bloccato. */
    public void inizioTentativo(String email) {
        Instant ora = clock.instant();
        stati.compute(chiave(email), (k, s) -> {
            if (s != null && s.bloccatoFino() != null) {
                if (ora.isBefore(s.bloccatoFino())) {
                    throw new TroppiTentativiException(secondiMancanti(s.bloccatoFino(), ora));
                }
                s = null; // cooldown finito: si riparte da zero
            }
            int tentativi = s == null ? 0 : s.tentativi();
            if (tentativi >= max) { // tentativi paralleli oltre il limite
                throw new TroppiTentativiException(cooldown.toSeconds());
            }
            return new Stato(tentativi + 1, null, ora);
        });
        if (stati.size() > MAX_VOCI) pulisci(ora);
    }

    /** Password sbagliata: restituisce i tentativi rimasti (0 = appena bloccato). */
    public int tentativoFallito(String email) {
        Instant ora = clock.instant();
        Stato s = stati.computeIfPresent(chiave(email), (k, v) ->
                v.tentativi() >= max ? new Stato(v.tentativi(), ora.plus(cooldown), ora) : v);
        return s == null ? max : Math.max(0, max - s.tentativi());
    }

    public void tentativoRiuscito(String email) {
        stati.remove(chiave(email));
    }

    public long secondiCooldown() {
        return cooldown.toSeconds();
    }

    private void pulisci(Instant ora) {
        stati.entrySet().removeIf(e -> {
            Stato s = e.getValue();
            return s.bloccatoFino() != null ? ora.isAfter(s.bloccatoFino()) : ora.isAfter(s.ultimo().plus(cooldown));
        });
    }

    private static long secondiMancanti(Instant fino, Instant ora) {
        return Math.max(1, (long) Math.ceil(Duration.between(ora, fino).toMillis() / 1000.0));
    }

    private static String chiave(String email) {
        return TokenUtil.sha256Hex(email.trim().toLowerCase());
    }
}
