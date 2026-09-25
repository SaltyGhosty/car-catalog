package it.carcatalog.security;

import it.carcatalog.repository.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Legge "Authorization: Bearer ...", valida il token e ricarica l'utente dal DB.
 * NON è un @Component: viene creato in SecurityConfig per non essere registrato due volte.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    public JwtAuthFilter(JwtService jwtService, UtenteRepository utenteRepository) {
        this.jwtService = jwtService;
        this.utenteRepository = utenteRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.estraiUtenteId(header.substring(7))
                    .flatMap(utenteRepository::findById)
                    .ifPresent(utente -> {
                        var principal = new UtenteAutenticato(utente.getId(), utente.getRuolo());
                        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
