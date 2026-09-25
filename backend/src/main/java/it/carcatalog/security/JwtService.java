package it.carcatalog.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.carcatalog.model.Utente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Il JWT contiene SOLO: sub (id utente), iat, exp. Niente email, nome o ruolo:
 * il ruolo viene riletto dal DB a ogni richiesta, così un declassamento o una
 * cancellazione dell'account hanno effetto immediato.
 */
@Service
public class JwtService {

    private static final String ISSUER = "car-catalog";

    private final SecretKey chiave;
    private final Duration validita;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.validita:PT2H}") Duration validita) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve essere lungo almeno 32 byte (256 bit)");
        }
        this.chiave = Keys.hmacShaKeyFor(bytes);
        this.validita = validita;
    }

    public String genera(Utente utente) {
        Instant ora = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(utente.getId()))
                .issuedAt(Date.from(ora))
                .expiration(Date.from(ora.plus(validita)))
                .signWith(chiave, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<Long> estraiUtenteId(String token) {
        try {
            String sub = Jwts.parser()
                    .verifyWith(chiave)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.valueOf(sub));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
