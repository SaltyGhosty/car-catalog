package it.carcatalog.web;

import it.carcatalog.dto.AvvisoAdminResponse;
import it.carcatalog.dto.PreferitoAdminResponse;
import it.carcatalog.dto.StatisticheResponse;
import it.carcatalog.dto.UtenteAdminResponse;
import it.carcatalog.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** L'amministratore "vede tutto": utenti, preferiti e avvisi di tutti. Un USER riceve 403. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/statistiche")
    public StatisticheResponse statistiche() {
        return adminService.statistiche();
    }

    @GetMapping("/utenti")
    public List<UtenteAdminResponse> utenti() {
        return adminService.utenti();
    }

    @GetMapping("/avvisi")
    public List<AvvisoAdminResponse> avvisi() {
        return adminService.avvisi();
    }

    @GetMapping("/preferiti")
    public List<PreferitoAdminResponse> preferiti() {
        return adminService.preferiti();
    }
}
