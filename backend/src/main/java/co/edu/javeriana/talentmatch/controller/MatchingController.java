package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.MatchingDtos.MiPuntajeDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.MatchingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Puntaje Final del empleado frente a una vacante (RF-15 ... RF-17). */
@RestController
@RequestMapping("/api/v1")
public class MatchingController {

    private final MatchingService matching;

    public MatchingController(MatchingService matching) {
        this.matching = matching;
    }

    @GetMapping("/vacantes/{id}/mi-puntaje")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public MiPuntajeDto miPuntaje(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return matching.miPuntaje(Sesion.idUsuario(token), id);
    }
}
