package it.carcatalog.web;

import it.carcatalog.dto.AuthResponse;
import it.carcatalog.dto.LoginRequest;
import it.carcatalog.dto.RegistrazioneRequest;
import it.carcatalog.dto.PasswordDimenticataRequest;
import it.carcatalog.dto.ReimpostaPasswordRequest;
import it.carcatalog.service.AuthService;
import it.carcatalog.service.ResetPasswordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(AuthService authService, ResetPasswordService resetPasswordService) {
        this.authService = authService;
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/registrazione")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrazione(@Valid @RequestBody RegistrazioneRequest req) {
        return authService.registra(req);
    }

    /** Risposta identica (204) che l'email esista o no. */
    @PostMapping("/password-dimenticata")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void passwordDimenticata(@Valid @RequestBody PasswordDimenticataRequest req) {
        resetPasswordService.richiedi(req.email());
    }

    @PostMapping("/reimposta-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reimpostaPassword(@Valid @RequestBody ReimpostaPasswordRequest req) {
        resetPasswordService.reimposta(req.token(), req.nuovaPassword());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
