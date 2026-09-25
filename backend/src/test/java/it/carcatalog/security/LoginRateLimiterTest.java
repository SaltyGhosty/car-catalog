package it.carcatalog.security;

import it.carcatalog.exception.TroppiTentativiException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    /** Orologio controllabile per simulare il passare del tempo senza aspettare. */
    static class Orologio extends Clock {
        Instant ora = Instant.parse("2026-01-01T10:00:00Z");
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return ora; }
    }

    private void fallisci(LoginRateLimiter l, String email) {
        l.inizioTentativo(email);
        l.tentativoFallito(email);
    }

    @Test
    void treErroriBloccanoPerUnMinutoPoiSiRiparte() {
        Orologio clock = new Orologio();
        LoginRateLimiter l = new LoginRateLimiter(3, Duration.ofMinutes(1), clock);

        l.inizioTentativo("a@b.it");
        assertEquals(2, l.tentativoFallito("a@b.it"));
        l.inizioTentativo("a@b.it");
        assertEquals(1, l.tentativoFallito("a@b.it"));
        l.inizioTentativo("a@b.it");
        assertEquals(0, l.tentativoFallito("a@b.it")); // terzo errore → bloccato

        TroppiTentativiException ex = assertThrows(TroppiTentativiException.class, () -> l.inizioTentativo("A@B.it "));
        assertEquals(60, ex.getSecondi());

        clock.ora = clock.ora.plusSeconds(59);
        assertThrows(TroppiTentativiException.class, () -> l.inizioTentativo("a@b.it"));

        clock.ora = clock.ora.plusSeconds(2); // cooldown finito
        assertDoesNotThrow(() -> l.inizioTentativo("a@b.it"));
    }

    @Test
    void loginRiuscitoAzzeraIlContatore() {
        LoginRateLimiter l = new LoginRateLimiter(3, Duration.ofMinutes(1), new Orologio());
        fallisci(l, "x@y.it");
        fallisci(l, "x@y.it");
        l.inizioTentativo("x@y.it");
        l.tentativoRiuscito("x@y.it");
        fallisci(l, "x@y.it");
        fallisci(l, "x@y.it");
        assertDoesNotThrow(() -> l.inizioTentativo("x@y.it"));
    }

    @Test
    void accountDiversiNonSiInfluenzano() {
        LoginRateLimiter l = new LoginRateLimiter(3, Duration.ofMinutes(1), new Orologio());
        fallisci(l, "uno@y.it");
        fallisci(l, "uno@y.it");
        fallisci(l, "uno@y.it");
        assertThrows(TroppiTentativiException.class, () -> l.inizioTentativo("uno@y.it"));
        assertDoesNotThrow(() -> l.inizioTentativo("due@y.it"));
    }
}
